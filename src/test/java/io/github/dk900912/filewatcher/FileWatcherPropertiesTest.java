package io.github.dk900912.filewatcher;

import io.github.dk900912.filewatcher.filter.MatchingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.dk900912.filewatcher.filter.MatchingStrategy.ANY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author dukui
 */
public class FileWatcherPropertiesTest {

    private FileWatcherProperties properties;

    @BeforeEach
    void setup() {
        List<String> dirs = Collections.singletonList(System.getProperty("user.dir"));
        Map<MatchingStrategy, Set<String>> strategy = Map.of(MatchingStrategy.ANY, Set.of());
        Duration pollInterval = Duration.ofSeconds(1);
        Duration quietPeriod = Duration.ofMillis(400);

        properties = new FileWatcherProperties(
                true,
                "Custom Watcher",
                dirs,
                strategy,
                true,
                3,
                pollInterval,
                quietPeriod
        );
    }

    @Test
    void testDefaultValues() {
        properties = new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")));
        assertTrue(properties.getDaemon());
        assertEquals("File Watcher", properties.getName());
        assertEquals(-1, properties.getRemainingScans().get());
        assertFalse(properties.getSnapshotEnabled());
        assertEquals(System.getProperty("user.dir"), properties.getDirectories().getFirst());
        assertEquals(Map.of(ANY, Set.of()), properties.getAcceptedStrategy());
        assertEquals(Duration.ofMillis(1000), properties.getPollInterval().get());
        assertEquals(Duration.ofMillis(400), properties.getQuietPeriod().get());
    }

    @Test
    void testGettersConsistency() {
        List<String> dirs = Collections.singletonList(System.getProperty("user.dir"));
        Map<MatchingStrategy, Set<String>> strategy = Map.of(MatchingStrategy.ANY, Set.of());
        Duration pollInterval = Duration.ofSeconds(1);
        Duration quietPeriod = Duration.ofMillis(400);

        assertTrue(properties.getDaemon());
        assertEquals("Custom Watcher", properties.getName());
        assertEquals(dirs, properties.getDirectories());
        assertEquals(strategy, properties.getAcceptedStrategy());
        assertTrue(properties.getSnapshotEnabled());
        assertEquals(3, properties.getRemainingScans().get());
        assertEquals(pollInterval, properties.getPollInterval().get());
        assertEquals(quietPeriod, properties.getQuietPeriod().get());
    }

    @Test
    void testDirectories_ValidExisting() {
        properties = new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")));
        assertEquals(1, properties.getDirectories().size());
        assertEquals(System.getProperty("user.dir"), properties.getDirectories().getFirst());
    }

    @Test
    void testDirectories_NonExisting() {
        List<String> dirs = Arrays.asList("/path1", "/path2");
        assertThrows(IllegalArgumentException.class, () ->
                new FileWatcherProperties(dirs)
        );
    }

    @Test
    void testDirectories_NullInput() {
        assertThrows(IllegalArgumentException.class, () ->
                new FileWatcherProperties(null)
        );
    }

    @Test
    void testDirectories_EmptyList() {
        assertThrows(IllegalArgumentException.class, () ->
                new FileWatcherProperties(Collections.emptyList())
        );
    }

    @Test
    void testDirectories_DuplicatePaths() {
        List<String> dirs = Arrays.asList(System.getProperty("user.dir"), System.getProperty("user.dir"));
        properties = new FileWatcherProperties(dirs);
        assertEquals(1, properties.getDirectories().size());
    }

    @Test
    void testAcceptedStrategy_ValidInput() {
        Map<MatchingStrategy, Set<String>> strategy = Map.of(
                MatchingStrategy.REGEX, Set.of(".*\\.txt", ".*\\.xml")
        );
        properties = new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")), strategy);
        assertEquals(strategy, properties.getAcceptedStrategy());
    }

    @Test
    void testSetAcceptedStrategy_EmptyMap() {
        assertThrows(IllegalArgumentException.class, () ->
                new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")), Map.of())
        );
    }

    @Test
    void testSetAcceptedStrategy_EmptyPatterns() {
        Map<MatchingStrategy, Set<String>> strategy = Map.of(
                MatchingStrategy.REGEX, Set.of()
        );
        assertThrows(IllegalArgumentException.class, () ->
                new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")), strategy)
        );
    }

    @Test
    void testSetAcceptedStrategy_InvalidCombination() {
        Map<MatchingStrategy, Set<String>> strategy = new HashMap<>();
        strategy.put(MatchingStrategy.SUFFIX, Set.of(".jpg", ".txt"));
        strategy.put(MatchingStrategy.REGEX, Set.of(".*\\.txt"));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")), strategy)
        );
        assertEquals("AcceptedStrategy must contain exactly one key after filtering out ANY", exception.getMessage());
    }

    @Test
    void testSetAcceptedStrategy_SingleNonAnyStrategyWithEmptyPatterns() {
        Map<MatchingStrategy, Set<String>> strategy = new HashMap<>();
        strategy.put(MatchingStrategy.SUFFIX, Set.of());
        final IllegalArgumentException acceptedStrategyMustNotBeEmpty = assertThrows(IllegalArgumentException.class, () ->
                new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")), strategy)
        );
        assertEquals("AcceptedStrategy must contain non-empty value for the key", acceptedStrategyMustNotBeEmpty.getMessage());
    }

    @Test
    void testSetAcceptedStrategy_SingleAnyStrategyWithEmptyPatterns() {
        Map<MatchingStrategy, Set<String>> strategy = new HashMap<>();
        strategy.put(MatchingStrategy.ANY, Set.of());
        properties = new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")), strategy);
        assertEquals(strategy, properties.getAcceptedStrategy());
    }

    @Test
    void testSetAcceptedStrategy_MultipleStrategiesWithNonEmptyPatterns() {
        Map<MatchingStrategy, Set<String>> strategy = new HashMap<>();
        strategy.put(MatchingStrategy.SUFFIX, Set.of(".jpg", ".txt"));
        strategy.put(MatchingStrategy.REGEX, Set.of(".*\\.txt"));
        strategy.put(MatchingStrategy.ANY, Set.of("**/*.xml"));
        final IllegalArgumentException acceptedStrategyMustBeOnlyOne = assertThrows(IllegalArgumentException.class, () ->
                new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")), strategy)
        );
        assertEquals("AcceptedStrategy must contain exactly one key after filtering out ANY", acceptedStrategyMustBeOnlyOne.getMessage());
    }
}
