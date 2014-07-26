package at.yawk.filemanager.file.sort;

import at.yawk.filemanager.file.File;

/**
 * @author yawkat
 */
class Below extends FileSorter {
    private final FileSorter top;
    private final FileSorter bottom;

    Below(FileSorter top, FileSorter bottom) {
        this.top = top;
        this.bottom = bottom;
    }

    @Override
    public int compare(File lhs, File rhs) {
        int i1 = top.compare(lhs, rhs);
        return i1 == 0 ? bottom.compare(lhs, rhs) : i1;
    }
}
