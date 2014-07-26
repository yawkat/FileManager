package at.yawk.filemanager.file.sort;

import at.yawk.filemanager.file.File;

/**
 * @author yawkat
 */
class Reverse extends FileSorter {
    private final FileSorter original;

    Reverse(FileSorter original) {
        this.original = original;
    }

    @Override
    public int compare(File lhs, File rhs) {
        return -original.compare(lhs, rhs);
    }
}
