package io.github.dk900912.filewatcher;

import io.github.dk900912.filewatcher.filter.MatchingStrategy;
import io.github.dk900912.filewatcher.utils.Assert;
import io.github.dk900912.filewatcher.utils.StringUtil;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final SnapshotState DEFAULT_SNAPSHOT_STATE = new SnapshotState(false, null);

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
    private final SnapshotState snapshotState;

    // Mutable value at runtime
    private final AtomicInteger remainingScans = new AtomicInteger();

    // Mutable value at runtime
    private final AtomicReference<Duration> pollInterval = new AtomicReference<>();

    // Mutable value at runtime
    private final AtomicReference<Duration> quietPeriod = new AtomicReference<>();

    public FileWatcherProperties(List<String> directories) {
        this(
            DEFAULT_DAEMON,
            DEFAULT_NAME,
            directories,
            DEFAULT_ACCEPTED_STRATEGY,
            DEFAULT_SNAPSHOT_STATE,
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
            DEFAULT_SNAPSHOT_STATE,
            DEFAULT_REMAINING_SCANS,
            DEFAULT_POLL_INTERVAL,
            DEFAULT_QUIET_PERIOD
        );
    }

    public FileWatcherProperties(List<String> directories,
                                 Map<MatchingStrategy, Set<String>> acceptedStrategy,
                                 SnapshotState snapshotState) {
        this(
            DEFAULT_DAEMON,
            DEFAULT_NAME,
            directories,
            acceptedStrategy,
            snapshotState,
            DEFAULT_REMAINING_SCANS,
            DEFAULT_POLL_INTERVAL,
            DEFAULT_QUIET_PERIOD
        );
    }

    public FileWatcherProperties(Boolean daemon,
                                 String name,
                                 List<String> directories,
                                 Map<MatchingStrategy, Set<String>> acceptedStrategy,
                                 SnapshotState snapshotState,
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
                    Path dir = Paths.get(path);
                    Assert.isTrue(Files.exists(dir) && Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS),
                            "Directory '" + dir + "' must be a valid directory and symbolic links are not allowed");
                })
                .toList();
        // Validate acceptedStrategy
        if (acceptedStrategy == null || acceptedStrategy.isEmpty()) {
            this.acceptedStrategy = DEFAULT_ACCEPTED_STRATEGY;
        } else {
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
        // Validate snapshotState
        if (snapshotState != null && snapshotState.getEnabled()) {
            Assert.hasText(snapshotState.getRepository(), "SnapshotState's repository must not be empty");
            Path repository = Paths.get(snapshotState.getRepository());
            Assert.isTrue(Files.exists(repository) && Files.isRegularFile(repository, LinkOption.NOFOLLOW_LINKS),
                    "SnapshotState's repository '" + repository + "' must be an existing regular file and symbolic links are not allowed");
            this.snapshotState = snapshotState;
        } else {
            this.snapshotState = DEFAULT_SNAPSHOT_STATE;
        }

        // Validate remainingScans
        if (remainingScans != null) {
            Assert.isTrue(remainingScans > 0 || remainingScans == -1, "RemainingScans must be positive or -1");
            this.remainingScans.set(remainingScans);
        } else {
            this.remainingScans.set(DEFAULT_REMAINING_SCANS);
        }

        // Validate pollInterval & quietPeriod
        Duration _pollInterval;
        Duration _quietPeriod;
        _pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        _quietPeriod = quietPeriod == null ? DEFAULT_QUIET_PERIOD : quietPeriod;
        Assert.isTrue(_pollInterval.toMillis() > 0, "PollInterval must be positive");
        Assert.isTrue(_quietPeriod.toMillis() > 0, "QuietPeriod must be positive");
        Assert.isTrue(_pollInterval.toMillis() > _quietPeriod.toMillis(), "PollInterval must be greater than QuietPeriod");
        this.pollInterval.set(_pollInterval);
        this.quietPeriod.set(_quietPeriod);
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

    public SnapshotState getSnapshotState() {
        return this.snapshotState;
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

    public static class SnapshotState {

        private final Boolean enabled;

        private final String repository;

        public SnapshotState(Boolean enabled, String repository) {
            this.enabled = enabled;
            this.repository = repository;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public String getRepository() {
            return repository;
        }
    }
}