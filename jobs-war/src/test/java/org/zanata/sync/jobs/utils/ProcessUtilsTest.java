package org.zanata.sync.jobs.utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtilsTest {
    private static final Logger log =
            LoggerFactory.getLogger(ProcessUtilsTest.class);

    @Test
    public void canRunCommand() throws IOException {
        List<String> output = ProcessUtils
                .runNativeCommand(Paths.get(System.getProperty("user.home")),
                        5000, "ls", "-l");

        Assertions.assertThat(output.size()).isGreaterThan(0);
    }

}
