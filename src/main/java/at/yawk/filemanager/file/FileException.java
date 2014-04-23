package at.yawk.filemanager.file;

import android.content.Context;

import java.io.IOException;

/**
* @author Jonas Konrad (yawkat)
*/
public class FileException extends IOException {
    private int key = -1;

    public FileException(Exception actual) {
        super(actual);
    }

    public FileException(int key) {
        this.key = key;
    }

    public String getMessage(Context context) {
        if (key == -1) {
            return super.getLocalizedMessage();
        } else {
            return context.getString(key);
        }
    }
}
