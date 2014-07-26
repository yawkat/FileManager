package at.yawk.filemanager.file.sort;

import at.yawk.filemanager.file.File;
import java.util.Random;

/**
 * @author yawkat
 */
public class RandomSorter extends FileSorter {
    private final Random random;

    public RandomSorter(long seed) {
        random = new Random(seed);
    }

    @Override
    public int compare(File lhs, File rhs) {
        return random.nextBoolean() ? 1 : -1;
    }
}
