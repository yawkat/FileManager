package at.yawk.filemanager;

import android.app.Application;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import java.io.File;
import org.apache.log4j.Level;

/**
 * @author Yawkat
 */
public class FileApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        LogConfigurator l = new LogConfigurator();
        l.setFileName(new File(getFilesDir(), "fm.log").getAbsolutePath());
        l.setRootLevel(Level.DEBUG);
        l.configure();
    }
}
