package com.jovistar.caltxt.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.jovistar.caltxt.phone.Addressbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;

public class FileCache {
    private static final String TAG = "FileCache";

    //Cache for storing standard image for contacts without profile image
    private File cacheDir;
    String localdir = "caltxt";
    Context mContext;

    public FileCache(Context context) {
        this.mContext = context;
        // Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
//			Log.e(TAG, "context:"+context);
            cacheDir = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    localdir);
        } else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public String getAbsolutePath(String filename) {
        File f = new File(cacheDir, filename);
        String s = f.getAbsolutePath();
//		Log.v(TAG, "getUri fname:" + fname+", "+s);
        f = null;
        return s;
    }

    public File get(String filename) {
        // I identify images by hashcode. Not a perfect solution, good for the
        // demo.
        // String filename=String.valueOf(url.hashCode());
        // Another possible solution (thanks to grantland)
//        if(cache.containsKey(url))
//        	filename = cache.get(url);

        File f = new File(cacheDir, filename);
//		Log.v(TAG, "get url:" + url);
        return f;
    }

    public void put(String filename, Bitmap bmp) {
//		fileCache.put(url, url);
        Log.v(TAG, "updated url:" + filename);
        File f = get(filename);
        try {
            OutputStream os = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
        } catch (IOException ex) {
            Log.e(TAG, "IOException:" + ex.toString());
        }
    }

    /*
        public void put(String url, String file) {
            cache.put(url, file);
        }
    */
    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files) {
            if (false == f.getName().startsWith(Addressbook.getInstance(mContext).getMyProfile().getIcon())) {
                f.delete();
            }
        }
//		cache.clear();
    }

    public void delete(final String regex) {
        File[] files = cacheDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir,
                                  final String name) {
                return name.contains(regex);
            }
        });
        if (files == null)
            return;
        for (File f : files) {
            f.delete();
            Log.d(TAG, "delete file:" + f.getName());
        }
    }
}
