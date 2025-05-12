package io.github.dk900912.filewatcher;

import io.github.dk900912.filewatcher.filter.MatchingStrategy;
import io.github.dk900912.filewatcher.utils.Assert;
import io.github.dk900912.filewatcher.utils.StringUtil;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.github.dk900912.filewatcher.filter.MatchingStrategy.ANY;

/**
 * @author dukui
 */
public class FileWatcherProperties {

    private static final Boolean DEFAULT_DAEMON = true;

    private static final String DEFAULT_NAME = "File Watcher";

    private static final Boolean DEFAULT_SNAPSHOT_ENABLED = false;

    private static final Map<MatchingStrategy, Set<String>> DEFAULT_ACCEPTED_STRATEGY = Map.of(ANY, Set.of());

    // Scan forever by default
    private static final Integer DEFAULT_REMAINING_SCANS = -1;

    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(1000);

    private static final Duration DEFAULT_QUIET_PERIOD = Duration.ofMillis(400);

    // Immutable at runtime
    private final Boolean daemon;

    // Immutable at runtime
    private final String name;

    // Immutable at runtime
    private final List<String> directories;

    // Immutable at runtime
    private final Map<MatchingStrategy, Set<String>> acceptedStrategy;

    // Immutable at runtime
    private final Boolean snapshotEnabled;

    // Mutable value at runtime
    private final AtomicInteger remainingScans = new AtomicInteger(DEFAULT_REMAINING_SCANS);

    // Mutable value at runtime
    private final AtomicReference<Duration> pollInterval = new AtomicReference<>(DEFAULT_POLL_INTERVAL);

    // Mutable value at runtime
    private final AtomicReference<Duration> quietPeriod = new AtomicReference<>(DEFAULT_QUIET_PERIOD);

    public FileWatcherProperties(List<String> directories) {
        this(
            DEFAULT_DAEMON,
            DEFAULT_NAME,
            directories,
            DEFAULT_ACCEPTED_STRATEGY,
            DEFAULT_SNAPSHOT_ENABLED,
            DEFAULT_REMAINING_SCANS,
            DEFAULT_POLL_INTERVAL,
            DEFAULT_QUIET_PERIOD
        );
    }

    public FileWatcherProperties(List<String> directories, Map<MatchingStrategy, Set<String>> acceptedStrategy) {
        this(
            DEFAULT_DAEMON,
            DEFAULT_NAME,
            directories,
            acceptedStrategy,
            DEFAULT_SNAPSHOT_ENABLED,
            DEFAULT_REMAINING_SCANS,
            DEFAULT_POLL_INTERVAL,
            DEFAULT_QUIET_PERIOD
        );
    }

    public FileWatcherProperties(Boolean daemon,
                                 String name,
                                 List<String> directories,
                                 Map<MatchingStrategy, Set<String>> acceptedStrategy,
                                 Boolean snapshotEnabled,
                                 Integer remainingScans,
                                 Duration pollInterval,
                                 Duration quietPeriod) {
        this.daemon = daemon == null ? DEFAULT_DAEMON : daemon;
        this.name = !StringUtil.hasLength(name) ? DEFAULT_NAME : name;
        // Validate directories
        Assert.isTrue(directories != null && !directories.isEmpty(), "Directories must not be null or empty");
        this.directories = directories.stream()
                .map(String::trim)
                .distinct()
                .peek(path -> {
                    File dir = new File(path);
                    Assert.isTrue(dir.isDirectory(), () -> "Directory '" + dir + "' must be a directory");
                })
                .toList();
        // Validate acceptedStrategy
        if (acceptedStrategy == null) {
            this.acceptedStrategy = DEFAULT_ACCEPTED_STRATEGY;
        } else {
            Assert.isTrue(!acceptedStrategy.isEmpty(), "AcceptedStrategy must not be empty");
            boolean onlyAny = acceptedStrategy.keySet().stream().allMatch(ANY::equals);
            if (!onlyAny) {
                Map<MatchingStrategy, Set<String>> filteredStrategy = acceptedStrategy.entrySet().stream()
                        .filter(entry -> !ANY.equals(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                Assert.isTrue(filteredStrategy.size() == 1,
                        "AcceptedStrategy must contain exactly one key after filtering out ANY");
                Assert.isTrue(filteredStrategy.values().stream().noneMatch(Set::isEmpty),
                        "AcceptedStrategy must contain non-empty value for the key");
                this.acceptedStrategy = filteredStrategy;
            } else {
                this.acceptedStrategy = DEFAULT_ACCEPTED_STRATEGY;
            }
        }
        this.snapshotEnabled = snapshotEnabled == null ? DEFAULT_SNAPSHOT_ENABLED : snapshotEnabled;

        // Validate remainingScans
        Assert.isTrue(remainingScans > 0 || remainingScans == -1, "RemainingScans must be positive or -1");
        this.remainingScans.set(remainingScans);

        // Validate pollInterval & quietPeriod
        Assert.isTrue(pollInterval.toMillis() > 0, "PollInterval must be positive");
        Assert.isTrue(quietPeriod.toMillis() > 0, "QuietPeriod must be positive");
        Assert.isTrue(pollInterval.toMillis() > quietPeriod.toMillis(), "PollInterval must be greater than QuietPeriod");
        this.pollInterval.set(pollInterval);
        this.quietPeriod.set(quietPeriod);
    }

    public Boolean getDaemon() {
        return this.daemon;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getDirectories() {
        return this.directories;
    }

    public Map<MatchingStrategy, Set<String>> getAcceptedStrategy() {
        return this.acceptedStrategy;
    }

    public Boolean getSnapshotEnabled() {
        return this.snapshotEnabled;
    }

    public AtomicInteger getRemainingScans() {
        return this.remainingScans;
    }

    public void setRemainingScans(Integer remainingScans) {
        if (remainingScans > 0 || remainingScans == -1) {
            this.remainingScans.set(remainingScans);
        }
    }

    public AtomicReference<Duration> getPollInterval() {
        return this.pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        if (pollInterval.toMillis() > 0 && pollInterval.toMillis() > getQuietPeriod().get().toMillis()) {
            this.pollInterval.set(pollInterval);
        }
    }

    public AtomicReference<Duration> getQuietPeriod() {
        return this.quietPeriod;
    }

    public void setQuietPeriod(Duration quietPeriod) {
        if (quietPeriod.toMillis() > 0 && getPollInterval().get().toMillis() > quietPeriod.toMillis()) {
            this.quietPeriod.set(quietPeriod);
        }
    }
}