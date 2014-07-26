package at.yawk.filemanager.file.sort;

import at.yawk.filemanager.file.File;
import at.yawk.filemanager.file.FileException;

/**
 * @author yawkat
 */
class ModifyDate extends FileSorter {
    @Override
    public int compare(File lhs, File rhs) {
        try {
            return ((Long) lhs.getModificationDate()).compareTo(rhs.getModificationDate());
        } catch (FileException e) {
            return 0;
        }
    }
}
