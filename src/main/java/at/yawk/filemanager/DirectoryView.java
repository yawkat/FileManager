package at.yawk.filemanager;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import at.yawk.filemanager.file.Copy;
import at.yawk.filemanager.file.File;
import at.yawk.filemanager.file.FileException;
import at.yawk.filemanager.file.Files;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class DirectoryView extends Filetivity {
    private static final int REQUEST_CODE_RETURN_THIS_FILE = 0;
    private static final int REQUEST_FILE_ACTION = 1;

    private static final int RESULT_CODE_IGNORE_FINISH = RESULT_FIRST_USER + 1;

    private File file;
    private ArrayAdapter<File> listAdapter;

    private Sink<File> fileActionSink = null;

    private boolean functional = false;

    /**
     * The selection mode of this activity. One of EXTRA_SELECTION_NONE, EXTRA_SELECTION_FILE and EXTRA_SELECTION_DIRECTORY.
     */
    private int returnExtra;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CODE_IGNORE_FINISH);

        try {
            file = loadAndValidateFile();
            if (file == null) { file = Files.createFromFileSystem("/storage"); /* TODO */ }

            initDirectoryView();
        } catch (FileException e) {
            showErrorDialogAndFinish(e);
        }
    }

    /**
     * Returns the directory this Activity should display or null if the default directory should be used. May throw an exception in specific cases (not a directory, does not exist);
     */
    private File loadAndValidateFile() throws FileException {
        File path = getFile(getIntent());
        if (path == null) { return null; }
        if (!path.exists()) { throw new FileException(R.string.error_does_not_exist); }
        if (!path.isDirectory()) { throw new FileException(R.string.error_no_directory); }
        return path;
    }

    protected File getFile(Intent intent) throws FileException {
        return intent.getParcelableExtra(EXTRA_FILE);
    }

    /**
     * Initializes this activity to show the contents of the current directory.
     */
    protected void initDirectoryView() {
        setContentView(R.layout.directory);
        functional = true;

        returnExtra = getIntent().getIntExtra(EXTRA_SELECTION, EXTRA_SELECTION_NONE);
        switch (returnExtra) {
        case EXTRA_SELECTION_FILE:
            findViewById(R.id.submit).setVisibility(View.GONE);
            // continue
        case EXTRA_SELECTION_DIRECTORY:
            findViewById(R.id.actions).setVisibility(View.VISIBLE);
            break;
        }

        int textResource = getIntent().getIntExtra(EXTRA_SELECTION_BUTTON_TITLE, 0);
        if (textResource != 0) {
            ((TextView) findViewById(R.id.submit)).setText(textResource);
        }

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit(file);
            }
        });

        listAdapter = createAdapter();
        ((ListView) findViewById(R.id.files)).setAdapter(listAdapter);
        try {
            ((TextView) findViewById(R.id.path)).setText(file.getPath());
        } catch (FileException ignored) {}
        refreshFileList();
    }

    protected ArrayAdapter<File> createAdapter() {
        return new FileListAdapter(this);
    }

    /**
     * Cancels the selection request.
     */
    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Returns the given file as the result of this activity (directory & file selection modes).
     */
    private void submit(File result) {
        Intent data = new Intent();
        data.putExtra(EXTRA_FILE, result);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * Refreshes the file list in this container asynchronously.
     */
    public void refreshFileList() {
        if (!functional) { return; }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loading).setVisibility(View.VISIBLE);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<File> children = getSortedChildren();
                    refreshFileListSync(children);
                } catch (final FileException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrorDialogAndFinish(e);
                        }
                    });
                }
            }
        }).start();
    }

    protected void refreshFileListSync(final List<File> children) throws FileException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loading).setVisibility(View.GONE);

                readdChildren(children);

                // only show loading icon with full alpha on first load
                findViewById(R.id.loading).setAlpha(0.5F);
            }
        });
    }

    private void readdChildren(List<File> children) {
        listAdapter.clear();
        listAdapter.addAll(children);
    }

    /**
     * Calculates and returns a sorted List of the files in this directory.
     */
    protected List<File> getSortedChildren() throws FileException {
        List<File> result = Lists.newArrayList(file.getChildren(getFilter()));
        Collections.sort(result, getSorter());
        return result;
    }

    /**
     * Performs the open action on the given file. This might not actually do anything in some cases (opening a file when in directory selection mode).
     */
    public void open(File file) throws FileException {
        if (file.isDirectory()) {
            Intent intent = new Intent();
            intent.setClass(this, getClass());
            intent.putExtra(EXTRA_FILE, file);
            intent.putExtra(EXTRA_NO_EXIT_ANIMATION, true);
            intent.putExtra(EXTRA_SELECTION, returnExtra);
            intent.putExtra(EXTRA_SELECTION_BUTTON_TITLE, getIntent().getIntExtra(EXTRA_SELECTION_BUTTON_TITLE, 0));
            if (returnExtra == EXTRA_SELECTION_NONE) {
                startActivity(intent);
            } else {
                startActivityForResult(intent, REQUEST_CODE_RETURN_THIS_FILE);
            }
            overridePendingTransition(0, 0);
        } else {
            switch (returnExtra) {
            case EXTRA_SELECTION_NONE:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(file.toUri(), MimeTypeMap.getSingleton().getMimeTypeFromExtension(Files.getSuffix(file)));
                startOrFail(intent);
                break;
            case EXTRA_SELECTION_FILE:
                submit(file);
                break;
            }
        }
    }

    public void share(File file) throws FileException {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, file.toUri());
        intent.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(Files.getSuffix(file)));
        startOrFail(Intent.createChooser(intent, getResources().getText(R.string.share)));
    }

    private void startOrFail(Intent intent) throws FileException {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            throw new FileException(R.string.error_unknown_file_type);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CODE_RETURN_THIS_FILE:
            if (resultCode != RESULT_CODE_IGNORE_FINISH) {
                setResult(resultCode, data);
                finish();
            }
            break;
        case REQUEST_FILE_ACTION:
            if (fileActionSink != null) {
                if (resultCode == RESULT_OK) {
                    fileActionSink.digest(data.<File>getParcelableExtra(EXTRA_FILE));
                }
                fileActionSink = null;
            }
            break;
        }
    }

    /**
     * Opens the option dialog for the given File.
     */
    public void options(final File file) throws FileException {
        final Dialog dialog = new Dialog(this);

        View view = getLayoutInflater().inflate(R.layout.file_settings, null);
        view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog p = new ProgressDialog(DirectoryView.this);
                p.setIndeterminate(true);
                p.setCancelable(false);
                p.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                p.setProgressNumberFormat("");
                p.setTitle(R.string.deleting);
                p.show();
                dialog.dismiss();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Files.deleteRecur(file);
                        } catch (FileException e) {
                            showErrorMessageToast(e);
                        }
                        p.dismiss();
                        refreshFileList();
                    }
                }).start();
            }
        });
        view.findViewById(R.id.rename).setOnClickListener(new RenameOnClickListener(file.getName(), R.string.rename, this) {
            @Override
            protected void dismiss() {
                dialog.dismiss();
            }

            @Override
            protected void performAction(String name) throws FileException {
                file.moveTo(file.getParent().getChild(name));
            }
        });
        view.findViewById(R.id.copy).setOnClickListener(new CopyOnClickListener(this, file, dialog, R.string.copying) {
            @Override
            protected void performAction(File from, File to, Copy.Progress progress) throws FileException {
                Copy.copy(from, to, progress);
            }
        });
        view.findViewById(R.id.move).setOnClickListener(new CopyOnClickListener(this, file, dialog, R.string.moving) {
            @Override
            protected void performAction(File from, File to, Copy.Progress progress) throws FileException {
                Copy.move(from, to, progress);
            }
        });
        view.findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    open(file);
                } catch (FileException e) {
                    showErrorMessageToast(e);
                }
            }
        });
        view.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    share(file);
                } catch (FileException e) {
                    showErrorMessageToast(e);
                }
            }
        });

        dialog.setTitle(file.getName());
        dialog.setContentView(view);
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFileList();
    }

    public void startFileAction(Sink<File> sink) {
        fileActionSink = sink;

        Intent intent = new Intent();
        intent.setClass(this, getClass());
        intent.putExtra(Constants.EXTRA_SELECTION, Constants.EXTRA_SELECTION_DIRECTORY);
        intent.putExtra(Constants.EXTRA_SELECTION_BUTTON_TITLE, R.string.copy_here);
        intent.putExtra(Constants.EXTRA_NO_EXIT_ANIMATION, false);
        startActivityForResult(intent, DirectoryView.REQUEST_FILE_ACTION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.directory, menu);
        try {
            menu.findItem(R.id.up).setEnabled(file.getParent() != null);
        } catch (FileException ignored) {}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.settings:
            Intent intent = new Intent();
            intent.setClass(this, Settings.class);
            startActivity(intent);
            break;
        case R.id.reload:
            refreshFileList();
            break;
        case R.id.up:
            try {
                File parent = file.getParent();
                if (parent == null) {
                    showErrorMessageToast(new FileException(R.string.error_no_directory));
                } else {
                    open(parent);
                }
            } catch (FileException e) {
                showErrorMessageToast(e);
            }
            break;
        case R.id.create_file:
            new RenameOnClickListener("", R.string.create_file, this) {
                @Override
                protected void performAction(String name) throws FileException {
                    File child = file.getChild(name);
                    if (child.exists()) {
                        throw new FileException(R.string.error_already_exists);
                    }
                    child.createFile();
                }
            }.run();
            break;
        case R.id.create_directory:
            new RenameOnClickListener("", R.string.create_directory, this) {
                @Override
                protected void performAction(String name) throws FileException {
                    File child = file.getChild(name);
                    if (child.exists()) {
                        throw new FileException(R.string.error_already_exists);
                    }
                    child.createDirectory();
                }
            }.run();
            break;
        }
        return true;
    }

}
