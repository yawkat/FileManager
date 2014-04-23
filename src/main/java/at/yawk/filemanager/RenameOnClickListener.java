package at.yawk.filemanager;

import android.app.Dialog;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import at.yawk.filemanager.file.FileException;
import at.yawk.filemanager.file.Files;

/**
 * @author Jonas Konrad (yawkat)
 */
abstract class RenameOnClickListener implements View.OnClickListener, Runnable {
    private final String initialValue;
    private final int submitString;
    private final DirectoryView directoryView;

    protected RenameOnClickListener(String initialValue, int submitString, DirectoryView directoryView) {
        this.initialValue = initialValue;
        this.submitString = submitString;
        this.directoryView = directoryView;
    }

    @Override
    public void onClick(View v) {
        run();
    }

    @Override
    public void run() {
        final Dialog d = new Dialog(directoryView);
        d.setTitle(submitString);
        d.setCancelable(true);

        final View r = directoryView.getLayoutInflater().inflate(R.layout.rename_form, null);
        ((TextView) r.findViewById(R.id.name)).setText(initialValue);
        ((EditText) r.findViewById(R.id.name)).setSelection(initialValue.length());
        ((TextView) r.findViewById(R.id.name)).setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Files.isValidNameCharacter(source.charAt(i))) {
                                return "";
                            }
                        }
                        return null;
                    }
                }
        });
        r.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        r.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    CharSequence name = ((TextView) r.findViewById(R.id.name)).getText();
                    String sanitized = Files.removeInvalidNameCharacters(name);
                    performAction(sanitized);
                } catch (FileException e) {
                    directoryView.showErrorMessageToast(e);
                }
                d.dismiss();
                directoryView.refreshFileList();
            }
        });
        ((TextView) r.findViewById(R.id.submit)).setText(submitString);
        d.setContentView(r);

        d.show();

        dismiss();
    }

    protected void dismiss() {}

    protected abstract void performAction(String name) throws FileException;
}
