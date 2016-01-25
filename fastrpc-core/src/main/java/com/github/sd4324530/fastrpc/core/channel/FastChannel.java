package com.github.sd4324530.fastrpc.core.channel;

import com.github.sd4324530.fastrpc.core.message.IMessage;
import com.github.sd4324530.fastrpc.core.serializer.ISerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author peiyu
 */
public class FastChannel implements IChannel {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private       AsynchronousSocketChannel channel;
    private final String                    id;
    private final ISerializer               serializer;
    private final long                      timeout;


    public FastChannel(AsynchronousSocketChannel channel, ISerializer serializer, long timeout) {
        this.channel = channel;
        this.serializer = serializer;
        this.timeout = timeout;
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public boolean isOpen() {
        return this.channel.isOpen();
    }

    @Override
    public <M extends IMessage> M read(Class<M> messageClazz) {
        if (this.isOpen()) {
            ByteBuffer messageLength = ByteBuffer.allocate(4);
            try {
                Integer integer = this.channel.read(messageLength).get(timeout, TimeUnit.MILLISECONDS);
                if (-1 == integer) {
                    log.debug("关闭连接 {} <-> {}", this.channel.getLocalAddress(), this.channel.getRemoteAddress());
                    close();
                    return null;
                }
                messageLength.flip();
                int length = messageLength.getInt();
                ByteBuffer message = ByteBuffer.allocate(length);
                this.channel.read(message).get();
                message.flip();
                return this.serializer.encoder(message.array(), messageClazz);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.error("读取数据异常", e);
            }
        }
        return null;
    }

    @Override
    public void write(IMessage message) {
        try {
            if (this.isOpen()) {
                byte[] bytes = this.serializer.decoder(message);
                ByteBuffer byteBuffer = ByteBuffer.allocate(4 + bytes.length);
                byteBuffer.putInt(bytes.length);
                byteBuffer.put(bytes);
                byteBuffer.flip();
                Integer integer = this.channel.write(byteBuffer).get(timeout, TimeUnit.MILLISECONDS);
                if (-1 == integer) {
                    log.warn("连接断了....");
                    log.warn("open:{}", this.isOpen());
                }
            }
        } catch (ExecutionException e) {
            log.warn("连接断了....");
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("写出数据异常", e);
            log.warn("open:{}", this.isOpen());
        }
    }

    @Override
    public void close() throws IOException {
        this.channel.close();
    }
}
