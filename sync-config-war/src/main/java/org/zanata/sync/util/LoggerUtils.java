package org.zanata.sync.util;

import java.io.File;

import org.slf4j.LoggerFactory;
import com.google.common.base.Optional;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LoggerUtils {
    // public static void main(String[] args) {
    // File newFile = new File("/tmp/foo.log");
    // Logger foo = createLoggerFor("test", newFile, Optional.absent());
    // foo.info("test11");
    // }
    private final static String MAX_FILE_SIZE = "5MB";

    private static Logger createLoggerFor(String name, File file,
            Optional<String> maxFileSize) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%msg%n");
        ple.setContext(lc);
        ple.start();

        RollingFileAppender<ILoggingEvent> fileAppender =
                new RollingFileAppender<ILoggingEvent>();
        fileAppender.setFile(file.getPath());
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);
        fileAppender.setAppend(false);

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(lc);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(5);
        rollingPolicy.setFileNamePattern(file.getName() + ".%i.json.zip");
        rollingPolicy.start();

        SizeBasedTriggeringPolicy triggeringPolicy =
                new SizeBasedTriggeringPolicy();
        if (maxFileSize.isPresent()) {
            triggeringPolicy.setMaxFileSize(maxFileSize.get());
        } else {
            triggeringPolicy.setMaxFileSize(MAX_FILE_SIZE);
        }
        triggeringPolicy.start();

        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setTriggeringPolicy(triggeringPolicy);
        fileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(name);
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false); /* set to true if root should log too */

        return logger;
    }

}
