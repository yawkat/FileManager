package at.yawk.filemanager.file.sort;

import android.content.Context;
import at.yawk.filemanager.R;

import at.yawk.filemanager.file.File;
import java.util.Comparator;
import java.util.Random;

/**
 * @author Jonas Konrad (yawkat)
 */
public abstract class FileSorter implements Comparator<File> {
    public static final FileSorter MODIFY_DATE = new ModifyDate();
    public static final FileSorter SIZE = new Size();
    public static final FileSorter NAME = new Name();
    public static final FileSorter DIRECTORIES_FIRST = new Directories();

    public static FileSorter intelligentName(Context context) {
        return new IntelligentNameSorter(context);
    }

    public FileSorter reverse() {
        return new Reverse(this);
    }

    public FileSorter withChild(FileSorter child) {
        return new Below(this, child);
    }
}

