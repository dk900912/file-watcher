package io.github.dk900912.filewatcher.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author dukui
 */
public class RegexFilterTest {

    private RegexFilter filter;

    @BeforeEach
    public void setUp() {
        Set<String> regexes = new HashSet<>();
        regexes.add(".*\\.txt");
        filter = new RegexFilter(regexes);
    }

    @Test
    public void accept_MatchingFilename_ReturnsTrue() {
        File file = new File("example.txt");
        assertTrue(filter.accept(file));
    }

    @Test
    public void accept_NonMatchingFilename_ReturnsFalse() {
        File file = new File("example.pdf");
        assertFalse(filter.accept(file));
    }

    @Test
    public void accept_EmptyFilename_ReturnsFalse() {
        File file = new File("");
        assertFalse(filter.accept(file));
    }

    @Test
    public void accept_InvalidRegex_ThrowsPatternSyntaxException() {
        Set<String> invalidRegexes = new HashSet<>();
        invalidRegexes.add("[");
        assertThrows(PatternSyntaxException.class, () -> new RegexFilter(invalidRegexes));
    }

    @Test
    public void accept_EmptyRegexCollection_ThrowsIllegalStateException() {
        Set<String> emptyRegexes = new HashSet<>();
        assertThrows(IllegalArgumentException.class, () -> new RegexFilter(emptyRegexes));
    }
}
