package at.yawk.filemanager;

import android.content.Context;

import java.util.Locale;

/**
 * @author Jonas Konrad (yawkat)
 */
public class Units {
    private static final long UNIT_SCALE = 1000;
    private static final int MAXIMUM_DIGITS = 3;
    private static final int MAXIMUM_WHOLE_DIGITS = 3;
    private static final int DISPLAY_BASE = 10;

    private final Context context;

    public Units(Context context) {
        this.context = context;
    }

    public String format(long size) {
        String[] units = context.getResources().getStringArray(R.array.size_units);
        int unit = getUnitIndex(size);
        if (unit >= units.length) { unit = units.length - 1; }

        double s = (double) size / getUnitDivisor(unit);
        int fracs = getFractionDigitCount(getDigitCount((long) s));

        Number readable = readableDouble(s, fracs);

        return String.format(Locale.getDefault(), units[unit], readable);
    }

    private Number readableDouble(double value, int maximumFractionDigits) {
        long mul = pow(DISPLAY_BASE, maximumFractionDigits);
        double rounded = (double) Math.round(value * mul) / mul;
        if (((int) rounded) == rounded) {
            return (int) rounded;
        } else {
            return rounded;
        }
    }

    private long getUnitDivisor(int unit) {
        return pow(UNIT_SCALE, unit);
    }

    private long pow(long b, long e) {
        return (long) Math.pow(b, e);
    }

    private int getUnitIndex(long size) {
        if (getDigitCount(size) > MAXIMUM_WHOLE_DIGITS) {
            return getUnitIndex(size / UNIT_SCALE) + 1;
        } else {
            return 0;
        }
    }

    private int getFractionDigitCount(int wholeDigitCount) {
        return wholeDigitCount >= MAXIMUM_DIGITS ? 0 : MAXIMUM_DIGITS - wholeDigitCount;
    }

    private int getDigitCount(long number) {
        if (number < DISPLAY_BASE) {
            return 1;
        } else {
            return 1 + getDigitCount(number / DISPLAY_BASE);
        }
    }
}
