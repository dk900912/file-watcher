package io.github.dk900912.filewatcher;

import io.github.dk900912.filewatcher.model.DirectorySnapshot;
import io.github.dk900912.filewatcher.model.FileSnapshot;
import io.github.dk900912.filewatcher.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Persistent repository for storing/restoring directory snapshots to maintain file monitoring
 * continuity across service interruptions. Key design purposes:
 *
 * <p>1. <b>Fast Service Restart</b> - Avoids full directory rescans by reloading previously saved
 * snapshots containing file metadata (paths, sizes, timestamps).
 *
 * <p>2. <b>Crash Resilience</b> - Preserves pre-crash state to detect changes occurred during
 * service downtime through snapshot comparison during recovery.
 *
 * @author dukui
 * @see FileSystemWatcher
 */
public class LocalSnapshotStateRepository implements SnapshotStateRepository {

    static final LocalSnapshotStateRepository INSTANCE = new LocalSnapshotStateRepository(Paths.get("file-watcher.snapshot"));

    private static final Logger logger = LoggerFactory.getLogger(LocalSnapshotStateRepository.class);

    private static final String SERIALIZATION_VERSION = "1.0";

    private final Path storage;

    public LocalSnapshotStateRepository(Path storage) {
        Assert.notNull(storage, "Storage path must not be null");
        this.storage = storage;
    }

    /*
     * ┌────────────── SAVE (Write Sequence) ──────────────┐
     * │  ╭───────── Root Structure ────────╮              │
     * │  │ 1. writeUTF(Version)            │              │
     * │  │ 2. writeInt(Directory Count)    │              │
     * │  ╰───────────┬─────────────────────╯              │
     * │              │                                    │
     * │              ▼                                    │
     * │  ╭───────── Per Directory ─────────╮              │
     * │  │ 3. writeUTF(Directory Path)     │              │
     * │  │ 4. writeObject(Snapshot Time)   │              │
     * │  │ 5. writeInt(File Count)         │              │
     * │  ╰───────────┬─────────────────────╯              │
     * │              │                                    │
     * │              ▼                                    │
     * │  ╭───────── Per File ──────────────╮              │
     * │  │ 6. writeUTF(File Path)          │              │
     * │  │ 7. writeBoolean(Existence)      │              │
     * │  │ 8. writeLong(File Size)         │              │
     * │  │ 9. writeLong(Last Modified)     │              │
     * │  ╰─────────────────────────────────╯              │
     * └───────────────────────────────────────────────────┘
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized void save(Object state) {
        if (!(state instanceof Map<?, ?>) || ((Map<?, ?>) state).isEmpty()) {
            logger.error("Unable to save snapshot state due to illegal state type");
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(storage, CREATE, WRITE, TRUNCATE_EXISTING))) {

            oos.writeUTF(SERIALIZATION_VERSION);

            Map<File, DirectorySnapshot> snapshots = (Map<File, DirectorySnapshot>) state;

            oos.writeInt(snapshots.size());

            for (Map.Entry<File, DirectorySnapshot> entry : snapshots.entrySet()) {
                serializeDirectorySnapshot(oos, entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            logger.error("Failed to save snapshot state", e);
        }
    }

    /*
     * ┌─────────── RESTORE (Read Sequence) ───────────────┐
     * │  ╭───────── Root Structure ────────╮              │
     * │  │ 1. readUTF(Version Check)       │              │
     * │  │ 2. readInt(Directory Count)     │              │
     * │  ╰───────────┬─────────────────────╯              │
     * │              │                                    │
     * │              ▼                                    │
     * │  ╭───────── Per Directory ─────────╮              │
     * │  │ 3. readUTF(Directory Path)      │              │
     * │  │ 4. readObject(Snapshot Time)    │              │
     * │  │ 5. readInt(File Count)          │              │
     * │  ╰───────────┬─────────────────────╯              │
     * │              │                                    │
     * │              ▼                                    │
     * │  ╭───────── Per File ──────────────╮              │
     * │  │ 6. readUTF(File Path)           │              │
     * │  │ 7. readBoolean(Existence)       │              │
     * │  │ 8. readLong(File Size)          │              │
     * │  │ 9. readLong(Last Modified)      │              │
     * │  ╰─────────────────────────────────╯              │
     * └───────────────────────────────────────────────────┘
     */
    @Override
    public synchronized Object restore() {
        if (!Files.exists(storage)) {
            logger.info("No snapshot file was found. A new snapshot will be created automatically upon the first save");
            return null;
        }

        if (storage.toFile().length() == 0) {
            logger.info("The snapshot file is empty, which renders restoration unnecessary. However, an empty snapshot is considered invalid and may indicate potential issues");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(storage, READ))) {
            String serialVer = ois.readUTF();
            if (!SERIALIZATION_VERSION.equals(serialVer)) {
                logger.error("Failed to restore snapshot state due to a serialization version mismatch");
                return null;
            }

            Map<File, DirectorySnapshot> snapshots = new LinkedHashMap<>();
            int size = ois.readInt();
            for (int i = 0; i < size; i++) {
                DirectorySnapshot ds = deserializeDirectorySnapshot(ois);
                snapshots.put(ds.getDirectory(), ds);
            }
            return snapshots;
        } catch (EOFException | StreamCorruptedException e) {
            logger.error("Corrupted snapshot detected. Deleting...");
            try {
                Files.delete(storage);
            } catch (IOException ex) {
                logger.error("Failed to delete corrupted snapshot file", ex);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to restore snapshot state", e);
        }
        return null;
    }

    private void serializeDirectorySnapshot(ObjectOutputStream oos, File directory, DirectorySnapshot snapshot) throws IOException {
        oos.writeUTF(directory.getAbsolutePath());

        oos.writeObject(snapshot.getTime());

        oos.writeInt(snapshot.getFiles().size());

        for (FileSnapshot file : snapshot.getFiles()) {
            oos.writeUTF(file.getFile().getAbsolutePath());
            oos.writeBoolean(file.exists());
            oos.writeLong(file.getLength());
            oos.writeLong(file.getLastModified());
        }
    }

    private DirectorySnapshot deserializeDirectorySnapshot(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String dirPath = ois.readUTF();
        LocalDateTime time = (LocalDateTime) ois.readObject();

        Set<FileSnapshot> files = new LinkedHashSet<>();
        int fileCount = ois.readInt();

        for (int i = 0; i < fileCount; i++) {
            String filePath = ois.readUTF();
            boolean exists = ois.readBoolean();
            long length = ois.readLong();
            long lastModified = ois.readLong();
            files.add(new FileSnapshot(new File(filePath), exists, length, lastModified));
        }
        return new DirectorySnapshot(new File(dirPath), time, files);
    }
}
