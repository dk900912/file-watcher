package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.FileWatcherProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author dukui
 */
public class FileFilterFactoryTest {

    private FileWatcherProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")));
    }

    @Test
    void testCreate_NullAcceptedStrategy() {
        FileFilterFactory.create(properties);
    }

    @Test
    void testCreate_ValidAcceptedStrategy() {
        Map<MatchingStrategy, Set<String>> strategy = new HashMap<>();
        strategy.put(MatchingStrategy.SUFFIX, Set.of(".jpg", ".txt"));
        properties = new FileWatcherProperties(Collections.singletonList(System.getProperty("user.dir")), strategy);
        FileFilterFactory.create(properties);
    }

}
