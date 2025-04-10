package io.github.dk900912.filewatcher.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author dukui
 */
public class SuffixFilterTest {

    private SuffixFilter filter;

    @BeforeEach
    public void setUp() {
        Set<String> suffixes = new HashSet<>(Arrays.asList(".jpeg", ".PNG", "jpg"));
        filter = new SuffixFilter(suffixes);
    }

    @Test
    void accept() {}

    @Test
    public void accept_NoDotInFileName_ReturnsFalse() {
        File file = new File("example");
        assertFalse(filter.accept(file));
    }

    @Test
    public void accept_MatchingSuffix_ReturnsTrue() {
        File file = new File("example.jpeg");
        assertTrue(filter.accept(file));
    }

    @Test
    public void accept_NonMatchingSuffix_ReturnsFalse() {
        File file = new File("example.txt");
        assertFalse(filter.accept(file));
    }

    @Test
    public void accept_CaseInsensitiveMatching_ReturnsTrue() {
        File file = new File("example.JPG");
        assertTrue(filter.accept(file));
    }

    @Test
    public void accept_SuffixWithLeadingDot_ReturnsTrue() {
        File file = new File("example.png");
        assertTrue(filter.accept(file));
    }
}
