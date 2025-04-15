package io.github.dk900912.filewatcher.filter;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
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
        filter = new RegexFilter(".*\\.txt");
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
    public void accept_EmptyRegex_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new RegexFilter(""));
    }

    @Test
    public void accept_InvalidRegex_ThrowsPatternSyntaxException() {
        assertThrows(PatternSyntaxException.class, () -> new RegexFilter("["));
    }
}
