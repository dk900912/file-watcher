package io.github.dk900912.filewatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author dukui
 */
public class FileSystemWatcherTest {

    private FileWatcherProperties properties;

    private FileSystemWatcher watcher;

    @BeforeEach
    public void setUp() throws Exception {
        properties = new FileWatcherProperties(List.of(System.getProperty("user.dir")));
        watcher = new FileSystemWatcher(properties);
    }

    @Test
    public void testStartAndStop() throws Exception {
        watcher.addListener(changeSet -> {/* Do nothing */});
        
        watcher.start();
        Thread watcherThread = (Thread) TestUtils.getPrivateField(watcher, "watchThread");
        assertNotNull(watcherThread);
        assertTrue(watcherThread.isAlive());
        assertEquals("File Watcher", watcherThread.getName());
        assertTrue(watcherThread.isDaemon());

        TimeUnit.SECONDS.sleep(2);

        watcher.stop();
        assertFalse(watcherThread.isAlive());
    }

    @Test
    public void testStartAndStopByRemainingScans() throws Exception {
        watcher.addListener(changeSet -> {/* Do nothing */});

        watcher.start();
        Thread watcherThread = (Thread) TestUtils.getPrivateField(watcher, "watchThread");
        assertNotNull(watcherThread);
        assertTrue(watcherThread.isAlive());
        assertEquals("File Watcher", watcherThread.getName());
        assertTrue(watcherThread.isDaemon());

        properties.setQuietPeriod(Duration.ofMillis(200));
        properties.setPollInterval(Duration.ofMillis(500));
        properties.setRemainingScans(5);

        TimeUnit.SECONDS.sleep(5);

        assertFalse(watcherThread.isAlive());
    }

    static class TestUtils {
        public static Object getPrivateField(Object instance, String fieldName) throws Exception {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        }
    }
}