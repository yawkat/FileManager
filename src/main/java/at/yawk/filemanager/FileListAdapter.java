package at.yawk.filemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import at.yawk.filemanager.file.File;
import at.yawk.filemanager.file.FileException;
import at.yawk.filemanager.file.Files;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Jonas Konrad (yawkat)
 */
class FileListAdapter extends ArrayAdapter<File> {
    private final Executor executor = Executors.newFixedThreadPool(3);

    private Cache<File, Entry> infoCache = CacheBuilder.newBuilder().build();

    private File selection = null;
    private View selectionView = null;
    private DirectoryView directoryView;

    FileListAdapter(DirectoryView directoryView) {
        super(directoryView, 0);
        this.directoryView = directoryView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) { convertView = LayoutInflater.from(getContext()).inflate(R.layout.file, null); }

        final File file = getItem(position);

        TextView nameView = (TextView) convertView.findViewById(R.id.name);
        TextView sizeView = (TextView) convertView.findViewById(R.id.size);
        ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);

        updateName(file, nameView);
        updateIcon(file, iconView);

        Entry entry = getEntry(file);
        entry.target = sizeView;
        entry.updateText();

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(v, file);

                try {
                    directoryView.open(file);
                } catch (FileException e) {
                    Toast.makeText(getContext(), e.getMessage(getContext()), Toast.LENGTH_SHORT).show();
                }
            }
        });
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                select(v, file);

                try {
                    directoryView.options(file);
                } catch (FileException e) {
                    Toast.makeText(getContext(), e.getMessage(getContext()), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        convertView.setBackgroundColor(0);
        if (file.equals(selection)) {
            select(convertView, file);
        }

        return convertView;
    }

    private void select(View view, File file) {
        selection = file;

        if (selectionView != null) { selectionView.setBackgroundColor(0); }
        selectionView = view;
        selectionView.setBackgroundColor(0x22555599);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for (int i = 0; i < getCount(); i++) {
            getEntry(getItem(i));
        }
    }

    private void updateName(File file, final TextView target) {
        try {
            final String text = file.getName();
            directoryView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    target.setText(text);
                }
            });
        } catch (FileException ignored) {
            directoryView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    target.setText("");
                }
            });
        }
    }

    private void updateIcon(File file, final ImageView target) {
        final int res = getFileIconResource(file);
        directoryView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                target.setImageResource(res);
            }
        });
    }

    private int getFileIconResource(File file) {
        try {
            String path = file.getPath();
            if (path.matches("/storage/(emulated/0|sdcard[0-9]+)")) {
                return R.drawable.drive_hdd;
            } else if (path.matches("/storage/extSdCard")) {
                return R.drawable.drive_sd;
            } else if (path.matches("/storage/UsbDrive[A-Z]")) {
                return R.drawable.drive_usb;
            } else if (file.isDirectory()) {
                return R.drawable.folder;
            } else {
                String suffix = Files.getSuffix(file).toLowerCase();
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
                if (mime != null) {
                    String mimeEscaped = mime.replace('+', '_').replace('-', '_').replace('.', '_');
                    int res = getContext().getResources().getIdentifier("drawable/mime_" + mimeEscaped.replace('/', '_'), null, this.getContext().getPackageName());
                    if (res != 0) {
                        return res;
                    }
                    if (mimeEscaped.indexOf('/') != -1) {
                        String type = mimeEscaped.substring(0, mimeEscaped.indexOf('/'));
                        int resGeneric = getContext().getResources().getIdentifier("drawable/mime_" + type + "_x_generic", null, this.getContext().getPackageName());
                        if (resGeneric != 0) {
                            return resGeneric;
                        }
                    }
                }
            }
        } catch (FileException ignored) {}
        return R.drawable.blank;
    }

    private Entry getEntry(final File file) {
        try {
            return infoCache.get(file, new Callable<Entry>() {
                @Override
                public Entry call() throws Exception {
                    final Entry e = new Entry();
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            e.load(file);
                        }
                    });
                    return e;
                }
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private class Entry {
        String info;
        TextView target;

        void load(File file) {
            try {
                if (file.isDirectory() && !directoryView.showDirectorySizeInBytes()) {
                    info = getContext().getString(R.string.file_count, file.getChildren(directoryView.getFilter()).size());
                } else {
                    info = new Units(getContext()).format(Files.getSize(file));
                }
            } catch (FileException ignored) {}
            updateText();
        }

        void updateText() {
            if (target != null) {
                directoryView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (info == null) { target.setText(R.string.nothing); } else { target.setText(info); }
                    }
                });
            }
        }
    }
}
