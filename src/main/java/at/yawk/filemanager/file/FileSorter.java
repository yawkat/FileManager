package at.yawk.filemanager.file;

import android.content.Context;
import at.yawk.filemanager.R;

import java.util.Comparator;
import java.util.Random;

/**
 * @author Jonas Konrad (yawkat)
 */
public abstract class FileSorter implements Comparator<File> {
    public static final FileSorter RANDOM = new RandomSorter();
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

class RandomSorter extends FileSorter {
    private final Random random = new Random();

    @Override
    public int compare(File lhs, File rhs) {
        return random.nextBoolean() ? 1 : -1;
    }
}

class IntelligentNameSorter extends FileSorter {
    private final Context context;

    IntelligentNameSorter(Context context) {
        this.context = context;
    }

    @Override
    public int compare(File lhs, File rhs) {
        return name(lhs).compareTo(name(rhs));
    }

    private String name(File file) {
        String n;
        try {
            n = file.getName();
        } catch (FileException e) {
            return "";
        }
        n = removeInvalid(n);
        n = n.toLowerCase();
        for (String pre : context.getResources().getStringArray(R.array.file_prefixes)) {
            if (n.startsWith(pre)) {
                n = n.substring(pre.length());
                break;
            }
        }
        return n;
    }

    private static String removeInvalid(CharSequence input) {
        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isSpaceChar(c)) {
                result.append(' ');
            } else if (Character.isLetterOrDigit(c)) {
                result.append(c);
            } else if (c == '.') {
                result.append(c);
            }
        }
        return result.toString();
    }
}

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

class Size extends FileSorter {
    @Override
    public int compare(File lhs, File rhs) {
        try {
            return ((Long) lhs.getSize()).compareTo(rhs.getSize());
        } catch (FileException e) {
            return 0;
        }
    }
}

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
