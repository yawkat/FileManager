package at.yawk.filemanager.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Various file-related operations.
 *
 * @author Jonas Konrad (yawkat)
 */
public class Files {
    private static final int BUFFER_SIZE = 4048;
    private static final String INVALID_CHARACTERS = "\\?*%|:\"<>";

    private Files() {}

    public static void copy(File from, File to, Progress progress) throws FileException {
        InputStream in = from.openInput();
        try {
            copy(in, to, from.getSize(), progress);
        } finally {
            try { in.close(); } catch (IOException ignored) {}
        }
    }

    public static void copy(InputStream from, File to, long inputLength, Progress progress) throws FileException {
        OutputStream out = to.openOutput();
        try {
            copy(from, out, inputLength, progress);
        } finally {
            try { out.close(); } catch (IOException ignored) {}
        }
    }

    public static void copy(InputStream from, OutputStream to, long inputLength, Progress progress) throws FileException {
        try {
            progress.post(0, inputLength);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            long done = 0;
            while ((len = from.read(buffer)) > 0) {
                Thread.yield();
                to.write(buffer, 0, len);
                done += len;
                progress.post(done, inputLength);
            }
        } catch (IOException e) {
            throw new FileException(e);
        }
    }

    public static String getSuffix(File file) throws FileException {
        return getSuffix(file.getName());
    }

    public static String getSuffix(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i == -1 ? fileName : fileName.substring(i + 1);
    }

    public static String removeInvalidPathCharacters(CharSequence fileName) {
        return removeInvalidCharacters(fileName, true);
    }

    public static String removeInvalidNameCharacters(CharSequence fileName) {
        return removeInvalidCharacters(fileName, false);
    }

    private static String removeInvalidCharacters(CharSequence fileName, boolean path) {
        StringBuilder result = new StringBuilder(fileName.length());
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (path ? isValidPathCharacter(c) : isValidNameCharacter(c)) { result.append(c); }
        }
        return result.toString();
    }

    public static File createFromFileSystem(String path) {
        return new JavaFile(new java.io.File(Files.removeInvalidPathCharacters(path)));
    }

    public static boolean isValidPathCharacter(char c) {
        return INVALID_CHARACTERS.indexOf(c) == -1;
    }

    public static boolean isValidNameCharacter(char c) {
        return c != '/' && isValidPathCharacter(c);
    }

    public static void deleteRecur(File file) throws FileException {
        if (file.isDirectory()) {
            for (File child : file.getChildren()) {
                deleteRecur(child);
            }
            if (file.getChildren().isEmpty()) { file.delete(); }
        } else {
            file.delete();
        }
    }

    public static long getSize(File file) throws FileException {
        if (file.isDirectory()) {
            long size = 0;
            for (File child : file.getChildren()) {
                try {
                    size += getSize(child);
                } catch (FileException ignored) {}
            }
            return size;
        } else {
            return file.getSize();
        }
    }

    public static interface Progress {
        void post(long current, long total);
    }
}
