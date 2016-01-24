package com.github.sd4324530.fastrpc.core.log.jdk;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author peiyu
 */
public class FastErrorLogHandler extends ConsoleHandler {

    private static final String KONG_GE = " ";

    public FastErrorLogHandler() {
        super();
        super.setOutputStream(System.err);
        super.setLevel(Level.ALL);
        super.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
//                System.out.println("FastErrorLogHandler:format");
                LocalDateTime now = LocalDateTime.now();
                String time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String thread = Thread.currentThread().getName();
                String lavel = getLogLevel(record.getLevel().getName());
                String message = record.getMessage();
                StringBuilder builder = new StringBuilder();
                builder.append(time).append(KONG_GE)
                        .append(record.getLoggerName()).append(KONG_GE)
                        .append("[").append(thread).append("]").append(KONG_GE)
                        .append(lavel).append(KONG_GE)
                        .append("-").append(KONG_GE).append(message).append("\n");
                String msg = builder.toString();
//                System.out.println("FastErrorLogHandler:" + msg);
//                System.err.println("FastErrorLogHandler:" + msg);
                return msg;
            }
        });

        super.setFilter(record -> {
            boolean result = record.getLevel().getName().equals("WARNING") || record.getLevel().getName().equals("SEVERE");
//            System.out.println("FastErrorLogHandler:" + result);
            return result;
        });
    }

    private String getLogLevel(String level) {
        switch (level) {
            case "WARNING" :
                return "WARN";
            case "SEVERE" :
                return "ERROR";
            default:
                throw new RuntimeException("非法的日志级别");
        }
    }
}
