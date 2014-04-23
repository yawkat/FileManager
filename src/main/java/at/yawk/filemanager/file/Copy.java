package at.yawk.filemanager.file;

import at.yawk.filemanager.R;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jonas Konrad (yawkat)
 */
public class Copy {
    private Copy() {}

    public static void copy(File from, File to, final Progress progress) throws FileException {
        if (to.exists()) { throw new FileException(R.string.error_already_exists); }

        progress.deep(from.isDirectory());

        List<FilePair> files = Lists.newArrayList();
        buildFilePairTree(from, to, files);

        int fcount = files.size();

        progress.fileTreeBuilt(fcount);

        int fdone = 0;

        for (final FilePair pair : files) {
            int ffdone = fdone;
            if (pair.f1.isDirectory()) {
                pair.f2.createDirectory();
            } else {
                Files.copy(pair.f1, pair.f2, new Files.Progress() {
                    @Override
                    public void post(long current, long total) {
                        progress.fileProgress(pair.f1, current, total);
                    }
                });
            }
            fdone++;
            progress.fileDone(fdone, fcount);
        }
        progress.done();
    }

    public static void move(File from, File to, Progress progress) throws FileException {
        progress.deep(false);
        progress.fileTreeBuilt(1);
        progress.fileProgress(from);
        if (from.mightBeOnSameFileSystem(to) && from.moveTo(to)) {
            progress.fileDone(1, 1);
            progress.done();
        } else {
            copy(from, to, progress);
            delete(from, progress);
        }
    }

    public static void delete(File delete, Progress progress) throws FileException {
        if (!delete.exists()) { throw new FileException(R.string.error_does_not_exist); }

        progress.deep(delete.isDirectory());

        List<File> files = new ArrayList<File>();
        buildFileTree(delete, files);

        int fcount = files.size();

        progress.fileTreeBuilt(fcount);

        int fdone = 0;

        for (File file : files) {
            progress.fileProgress(file);
            file.delete();
            fdone++;
            progress.fileDone(fdone, fcount);
        }
    }

    private static void buildFilePairTree(File from, File to, Collection<FilePair> target) throws FileException {
        // add directories before its content (moving / copying)
        target.add(new FilePair(from, to));
        if (from.isDirectory()) {
            for (File child : from.getChildren()) {
                buildFilePairTree(child, to.getChild(child.getName()), target);
            }
        }
    }

    private static void buildFileTree(File file, Collection<File> target) throws FileException {
        // add directories after content (deleting)
        if (file.isDirectory()) {
            for (File child : file.getChildren()) {
                buildFileTree(child, target);
            }
        }
        target.add(file);
    }

    public static interface Progress {
        void deep(boolean deep);

        void fileTreeBuilt(int count);

        void fileProgress(File from);

        void fileProgress(File from, long bytesDone, long bytesTotal);

        void fileDone(int filesDone, int filesTotal);

        void done();
    }
}

class FilePair {
    File f1;
    File f2;

    FilePair(File f1, File f2) {
        this.f1 = f1;
        this.f2 = f2;
    }
}
