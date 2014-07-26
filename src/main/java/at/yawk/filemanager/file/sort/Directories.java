package at.yawk.filemanager.file.sort;

import at.yawk.filemanager.file.File;
import at.yawk.filemanager.file.FileException;

/**
 * @author yawkat
 */
class Directories extends FileSorter {
    @Override
    public int compare(File lhs, File rhs) {
        try {
            boolean b1 = lhs.isDirectory();
            boolean b2 = rhs.isDirectory();
            return b1 == b2 ? 0 : b1 ? -1 : 1;
        } catch (FileException e) {
            return 0;
        }
    }
}
