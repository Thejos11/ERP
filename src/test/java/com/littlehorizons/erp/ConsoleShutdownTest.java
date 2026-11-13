package com.littlehorizons.erp;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class ConsoleShutdownTest {
    @Test void mainClosesNormallyWhenInputEndsAtInitialMenu() throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Process process = new ProcessBuilder(java, "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8",
                "-Dstderr.encoding=UTF-8", "-cp", classpath,
                Main.class.getName()).redirectErrorStream(true).start();
        try {
            process.getOutputStream().close();
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Application must exit on EOF");
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue());
            assertTrue(output.contains("Sesión cerrada."));
            assertFalse(output.contains("Exception"));
            assertFalse(output.contains("error inesperado"));
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }
}
