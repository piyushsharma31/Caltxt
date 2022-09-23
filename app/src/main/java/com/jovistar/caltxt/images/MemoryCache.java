package com.jovistar.caltxt.images;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

import java.io.InputStream;
import java.io.OutputStream;

public class MemoryCache {
    // On a normal/hdpi device this is a minimum of around 4MB (32/8).
    // A full screen GridView filled with images on a device with 800x480
    // resolution would use around
    // 1.5MB (800*480*4 bytes), so this would cache a minimum of around 2.5
    // pages of images in memory.
    int cacheSize = 4 * 1024 * 1024; // 4MiB
    LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(cacheSize) {
        protected int sizeOf(String key, Bitmap value) {
            if (android.os.Build.VERSION.SDK_INT >= 12) {
                return value.getByteCount();
            } else {
                return value.getRowBytes() * value.getHeight();
            }
        }
    };

    LruCache<Integer, Bitmap> resourceCache = new LruCache<Integer, Bitmap>(
            cacheSize) {
        protected int sizeOf(String key, Bitmap value) {
            if (android.os.Build.VERSION.SDK_INT >= 12) {
                return value.getByteCount();
            } else {
                return value.getRowBytes() * value.getHeight();
            }
        }
    };

    public Bitmap get(int id) {
        return resourceCache.get(id);
    }

    public Bitmap get(String id) {
        /*
		 * if(!cache.containsKey(id)) return null; SoftReference<Bitmap>
		 * ref=cache.get(id); return ref.get();
		 */
        return cache.get(id);
    }

    public void put(String id, Bitmap bitmap) {
        // cache.put(id, new SoftReference<Bitmap>(bitmap));
        cache.put(id, bitmap);
    }

    public void put(int id, Bitmap bitmap) {
        // cache.put(id, new SoftReference<Bitmap>(bitmap));
        resourceCache.put(id, bitmap);
    }

    public void clear() {
        // cache.clear();
        cache.evictAll();
        resourceCache.evictAll();
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public void delete(final String regex) {
        cache.remove(regex);
    }
}
