package at.yawk.filemanager.file.sort;

import android.content.Context;
import at.yawk.filemanager.R;
import at.yawk.filemanager.file.File;
import at.yawk.filemanager.file.FileException;

/**
 * @author yawkat
 */
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
