package at.yawk.filemanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;
import at.yawk.filemanager.file.FileException;
import at.yawk.filemanager.file.FileFilter;
import at.yawk.filemanager.file.FileSorter;

/**
 * @author Jonas Konrad (yawkat)
 */
public abstract class Filetivity extends Activity implements Constants {
    protected void showErrorDialogAndFinish(FileException error) {
        showErrorDialogAndFinish(error.getMessage(this));
    }

    protected void showErrorDialogAndFinish(String message) {
        Dialog dialog = new Dialog(this);
        dialog.setTitle(null);
        TextView v = new TextView(this);
        v.setText(message);
        dialog.setContentView(v);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
    }

    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    protected FileSorter getSorter() {
        FileSorter s = getSorter0();

        boolean reverse = getPreferences().getBoolean(PREFERENCE_SORTER_REVERSE, false);
        if (reverse) { s = s.reverse(); }

        boolean directoriesFirst = getPreferences().getBoolean(PREFERENCE_SORTER_DIRECTORIES_FIRST, true);
        if (directoriesFirst) { s = FileSorter.DIRECTORIES_FIRST.withChild(s); }

        return s;
    }

    protected boolean showDirectorySizeInBytes() {
        return getPreferences().getBoolean(PREFERENCE_SHOW_FOLDER_SIZE, false);
    }

    private FileSorter getSorter0() {
        int id = getPreferences().getInt(PREFERENCE_SORTER, -1);
        switch (id) {
        case SORTER_SIZE:
            return FileSorter.SIZE;
        case SORTER_MODIFY_DATE:
            return FileSorter.MODIFY_DATE;
        case SORTER_NAME:
            return FileSorter.NAME;
        default:
        case SORTER_NAME_INTELLIGENT:
            return FileSorter.intelligentName(this);
        case SORTER_RANDOM:
            return FileSorter.RANDOM;
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (getIntent().getBooleanExtra(EXTRA_NO_EXIT_ANIMATION, false)) {
            overridePendingTransition(0, 0);
        }
    }

    protected FileFilter getFilter() {
        boolean hidden = getPreferences().getBoolean(PREFERENCE_SHOW_HIDDEN, false);
        return hidden ? FileFilter.ANY : FileFilter.VISIBLE;
    }

    protected void showErrorMessageToast(FileException e) {
        Toast.makeText(this, e.getMessage(this), Toast.LENGTH_LONG).show();
    }
}
