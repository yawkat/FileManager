package at.yawk.filemanager;

import android.app.Dialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.yawk.filemanager.file.Copy;
import at.yawk.filemanager.file.File;
import at.yawk.filemanager.file.FileException;
import lombok.extern.log4j.Log4j;

/**
 * @author Jonas Konrad (yawkat)
 */
@Log4j
public class CopyProgressViewer implements Copy.Progress {
    private final Units units;
    private final View contentView;
    private final Dialog dialog;
    private final File target;
    private boolean deep;
    private long lastUpdate;
    private DirectoryView directoryView;

    public CopyProgressViewer(DirectoryView directoryView, View contentView, Dialog dialog, File target) {
        this.contentView = contentView;
        this.dialog = dialog;
        this.target = target;
        units = new Units(directoryView);
        lastUpdate = 0L;
        this.directoryView = directoryView;
    }

    @Override
    public void deep(boolean deep) {
        this.deep = deep;
        if (deep) {
            directoryView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    contentView.findViewById(R.id.file_count_progress).setVisibility(View.VISIBLE);
                    contentView.findViewById(R.id.file_count_progress_text).setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void fileTreeBuilt(final int count) {
        if (deep) {
            directoryView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ProgressBar) contentView.findViewById(R.id.file_count_progress)).setIndeterminate(false);
                    ((ProgressBar) contentView.findViewById(R.id.file_count_progress)).setMax(count);
                    ((ProgressBar) contentView.findViewById(R.id.file_count_progress)).setProgress(0);
                    ((ProgressBar) contentView.findViewById(R.id.file_size_progress)).setIndeterminate(false);
                    ((ProgressBar) contentView.findViewById(R.id.file_size_progress)).setMax(1000);
                    ((ProgressBar) contentView.findViewById(R.id.file_size_progress)).setProgress(0);
                    ((TextView) contentView.findViewById(R.id.file_count_done)).setText("0");
                    ((TextView) contentView.findViewById(R.id.file_count_total)).setText(Integer.toString(count));
                    ((TextView) contentView.findViewById(R.id.file_size_done)).setText("0");
                    ((TextView) contentView.findViewById(R.id.file_size_total)).setText("?");
                }
            });
        }
    }

    @Override
    public void fileProgress(final File from) {
        directoryView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar) contentView.findViewById(R.id.file_size_progress)).setIndeterminate(true);

                try {
                    ((TextView) contentView.findViewById(R.id.name)).setText(from.getName());
                } catch (FileException ignored) {}
            }
        });
    }

    @Override
    public void fileDone(final int filesDone, final int filesTotal) {
        directoryView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar) contentView.findViewById(R.id.file_count_progress)).setProgress(filesDone);
                ((TextView) contentView.findViewById(R.id.file_count_done)).setText(Integer.toString(filesDone));
                ((TextView) contentView.findViewById(R.id.file_count_total)).setText(Integer.toString(filesTotal));
            }
        });
    }

    @Override
    public void fileProgress(final File from, final long bytesDone, final long bytesTotal) {
        long time = System.currentTimeMillis();
        if (time - lastUpdate > 200L) {
            log.debug("Updating...");
            lastUpdate = time;
            directoryView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ProgressBar) contentView.findViewById(R.id.file_size_progress)).setIndeterminate(false);
                    ((ProgressBar) contentView.findViewById(R.id.file_size_progress)).setProgress((int) (bytesDone * 1000 / bytesTotal));

                    ((TextView) contentView.findViewById(R.id.file_size_done)).setText(units.format(bytesDone));
                    ((TextView) contentView.findViewById(R.id.file_size_total)).setText(units.format(bytesTotal));

                    try {
                        ((TextView) contentView.findViewById(R.id.name)).setText(from.getName());
                    } catch (FileException ignored) {}
                }
            });
        }
    }

    @Override
    public void done() {
        dialog.dismiss();
        directoryView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    directoryView.open(target.getParent());
                } catch (FileException ignored) {}
            }
        });
    }
}
