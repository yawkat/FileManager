package at.yawk.filemanager.file.sort;

import at.yawk.filemanager.file.File;
import at.yawk.filemanager.file.FileException;

/**
 * @author yawkat
 */
class Name extends FileSorter {
    @Override
    public int compare(File lhs, File rhs) {
        try {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        } catch (FileException e) {
            return 0;
        }
    }
}
