package at.yawk.filemanager.file;

import com.google.common.base.Predicate;

/**
 * @author Jonas Konrad (yawkat)
 */
public abstract class FileFilter implements Predicate<File> {
    public static final FileFilter ANY = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return true;
        }
    };
    public static final FileFilter VISIBLE = new FileFilter() {
        @Override
        public boolean accept(File file) throws FileException {
            return !file.getName().startsWith(".");
        }
    };

    public abstract boolean accept(File file) throws FileException;

    @Override
    public final boolean apply(File file) {
        try {
            return file != null && accept(file);
        } catch (FileException e) { return false; }
    }
}
