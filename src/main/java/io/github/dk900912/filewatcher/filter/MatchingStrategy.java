package io.github.dk900912.filewatcher.filter;

/**
 * @author dk900912
 */
public interface MatchingStrategy {

    MatchingType supports();

    boolean matches(String fileName);
}