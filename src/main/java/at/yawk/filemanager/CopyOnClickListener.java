package at.yawk.filemanager;

import android.app.Dialog;
import android.view.View;
import android.widget.Toast;
import at.yawk.filemanager.file.Copy;
import at.yawk.filemanager.file.File;
import at.yawk.filemanager.file.FileException;

/**
 * @author Jonas Konrad (yawkat)
 */
public abstract class CopyOnClickListener implements View.OnClickListener {
    private final File file;
    private final Dialog dialog;
    private final int title;
    private DirectoryView directoryView;

    public CopyOnClickListener(DirectoryView directoryView, File file, Dialog dialog, int title) {
        this.directoryView = directoryView;
        this.file = file;
        this.title = title;
        this.dialog = dialog;
    }

    @Override
    public void onClick(View v) {
        directoryView.startFileAction(new Sink<File>() {
            @Override
            public void digest(File element) {
                final File to;
                try {
                    to = element.getChild(file.getName());
                } catch (FileException e) {
                    Toast.makeText(directoryView, e.getMessage(directoryView), Toast.LENGTH_LONG).show();
                    return;
                }

                final Dialog d = new Dialog(directoryView);
                d.setCancelable(false);
                d.setTitle(title);
                final View p = directoryView.getLayoutInflater().inflate(R.layout.copy_progress, null);
                d.setContentView(p);
                d.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            performAction(file, to, new CopyProgressViewer(directoryView, p, d, to));
                        } catch (FileException e) {
                            Toast.makeText(directoryView, e.getMessage(directoryView), Toast.LENGTH_LONG).show();
                        }
                    }
                }).start();
            }
        });

        dialog.dismiss();
    }

    protected abstract void performAction(File from, File to, Copy.Progress progress) throws FileException;
}
