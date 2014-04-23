package at.yawk.filemanager.file;

import android.net.Uri;
import android.os.Parcel;
import at.yawk.filemanager.R;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Jonas Konrad (yawkat)
 */
class JavaFile implements File {
    private static final Function<java.io.File, File> CREATOR_FUNCTION = new Function<java.io.File, File>() {
        @Override
        public File apply(java.io.File file) {
            return new JavaFile(file);
        }
    };

    private final java.io.File actual;

    JavaFile(java.io.File actual) {
        assert actual != null;
        this.actual = actual;
    }

    @Override
    public Collection<File> getChildren() throws FileException {
        java.io.File f = actual;
        if (f.getPath().equals("/storage/emulated")) { f = new java.io.File(f, "0"); }
        java.io.File[] ch = f.listFiles();
        if (ch == null) { throw new FileException(R.string.error_no_access); }
        return Collections.unmodifiableCollection(Collections2.transform(Arrays.asList(ch), CREATOR_FUNCTION));
    }

    @Override
    public Collection<File> getChildren(FileFilter filter) throws FileException {
        return Collections2.filter(getChildren(), filter);
    }

    @Override
    public JavaFile getChild(String name) throws FileException {
        java.io.File c = new java.io.File(actual, name);
        if (c.getAbsolutePath().equals("/storage/emulated")) { c = new java.io.File(c, "0"); }
        return new JavaFile(c);
    }

    @Override
    public String getName() throws FileException {
        return actual.getName();
    }

    @Override
    public long getSize() throws FileException {
        return actual.length();
    }

    @Override
    public void delete() throws FileException {
        actual.delete();
    }

    @Override
    public void createFile() throws FileException {
        try {
            actual.createNewFile();
        } catch (IOException e) {
            throw new FileException(e);
        }
    }

    @Override
    public void createDirectory() throws FileException {
        actual.mkdir();
    }

    @Override
    public File getParent() throws FileException {
        java.io.File p = actual.getParentFile();
        if (p != null && p.getPath().equals("/storage/emulated")) { p = new java.io.File("/storage"); }
        return p == null ? null : new JavaFile(p);
    }

    @Override
    public String getPath() throws FileException {
        java.io.File f = actual;
        if (f.getAbsolutePath().equals("/storage/emulated")) { f = new java.io.File(f, "0"); }
        return f.getAbsolutePath();
    }

    @Override
    public InputStream openInput() throws FileException {
        try {
            return new FileInputStream(actual);
        } catch (FileNotFoundException e) {
            throw new FileException(e);
        }
    }

    @Override
    public OutputStream openOutput() throws FileException {
        try {
            return new FileOutputStream(actual);
        } catch (FileNotFoundException e) {
            throw new FileException(e);
        }
    }

    @Override
    public boolean mightBeOnSameFileSystem(File other) {
        return other instanceof JavaFile;
    }

    @Override
    public boolean moveTo(File target) throws FileException {
        if (!(target instanceof JavaFile)) { throw new FileException(R.string.error_not_same_file_system); }
        return actual.renameTo(((JavaFile) target).actual);
    }

    @Override
    public boolean exists() throws FileException {
        return actual.exists();
    }

    @Override
    public boolean isDirectory() throws FileException {
        return actual.isDirectory();
    }

    @Override
    public boolean isFile() throws FileException {
        return actual.isFile();
    }

    @Override
    public long getModificationDate() throws FileException {
        return actual.lastModified();
    }

    @Override
    public Uri toUri() {
        return Uri.fromFile(actual);
    }

    public int hashCode() {
        return actual.hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof JavaFile && ((JavaFile) o).actual.equals(this.actual);
    }

    // Parcel

    public static final Creator<File> CREATOR = new Creator<File>() {
        @Override
        public File createFromParcel(Parcel source) {
            return Files.createFromFileSystem(source.readString());
        }

        @Override
        public File[] newArray(int size) {
            return new File[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            dest.writeString(getPath());
        } catch (FileException ignored) {}
    }
}
