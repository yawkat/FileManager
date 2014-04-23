package at.yawk.filemanager.file;

import android.net.Uri;
import android.os.Parcelable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import javax.annotation.Nullable;

/**
 * @author Jonas Konrad (yawkat)
 */
public interface File extends Parcelable {
    /**
     * Returns the children of this directory.
     *
     * @throws at.yawk.filemanager.file.FileException if this is not a directory or we have no read access for it.
     */
    Collection<File> getChildren() throws FileException;

    /**
     * Returns the children of this directory that match the given FileFilter.
     *
     * @throws at.yawk.filemanager.file.FileException if this is not a directory or we have no read access for it.
     */
    Collection<File> getChildren(FileFilter filter) throws FileException;

    /**
     * Returns a child of this directory by its name. May or may not exist.
     */
    File getChild(String name) throws FileException;

    /**
     * Returns the name of this File.
     */
    String getName() throws FileException;

    /**
     * Returns the size of this file in bytes.
     */
    long getSize() throws FileException;

    /**
     * Tries to delete this file.
     */
    void delete() throws FileException;

    /**
     * Creates this file.
     *
     * @throws at.yawk.filemanager.file.FileException if the creation was not successful.
     */
    void createFile() throws FileException;

    /**
     * Creates this directory.
     *
     * @throws at.yawk.filemanager.file.FileException if the creation was not successful.
     */
    void createDirectory() throws FileException;

    /**
     * Returns the parent directory of this file or null if this is a root directory.
     */
    @Nullable
    File getParent() throws FileException;

    /**
     * Returns the full display path of this file.
     */
    String getPath() throws FileException;

    /**
     * Opens an InputStream on this file.
     *
     * @throws at.yawk.filemanager.file.FileException If this is not a file or it doesn't exist.
     */
    InputStream openInput() throws FileException;

    /**
     * Opens an OutputStream on this file.
     *
     * @throws at.yawk.filemanager.file.FileException If the parent directory doesn't exist.
     */
    OutputStream openOutput() throws FileException;

    /**
     * Returns whether the given file might be on the same file system and could perhaps be moved with #moveTo.
     */
    boolean mightBeOnSameFileSystem(File other);

    /**
     * Attempts to move this file to the given location. Returns true if the move was successful, false otherwise.
     *
     * @throws at.yawk.filemanager.file.FileException if #mightBeOnSameFileSystem returns false.
     */
    boolean moveTo(File target) throws FileException;

    /**
     * Returns true if this File exists, false otherwise.
     */
    boolean exists() throws FileException;

    /**
     * Returns true if this File exists and is a directory.
     */
    boolean isDirectory() throws FileException;

    /**
     * Returns true if this File exists and is a normal file.
     */
    boolean isFile() throws FileException;

    /**
     * Returns the modification date of this file.
     */
    long getModificationDate() throws FileException;

    /**
     * Converts this file to an android Uri that can be used for access from other applications.
     */
    Uri toUri();
}
