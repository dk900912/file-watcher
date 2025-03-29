package io.github.dk900912.filewatcher.filter;

import io.github.dk900912.filewatcher.utils.Assert;

import java.util.regex.Pattern;

import static io.github.dk900912.filewatcher.filter.MatchingType.REGEX;

/**
 * @author dk900912
 */
public class RegexMatchingStrategy implements MatchingStrategy {

    private final Pattern pattern;

    public RegexMatchingStrategy(String regex) {
        Assert.hasLength(regex, "Regex must not be empty");
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public MatchingType supports() {
        return REGEX;
    }

    @Override
    public boolean matches(String fileName) {
        return pattern.matcher(fileName).matches();
    }
}
