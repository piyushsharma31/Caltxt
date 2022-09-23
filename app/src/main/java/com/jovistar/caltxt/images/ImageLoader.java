package com.jovistar.caltxt.images;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.jovistar.caltxt.network.data.ConnectivityBroadcastReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private static final String TAG = "ImageLoader";

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    static ImageLoader il;
    Context mContext;

    public static ImageLoader getInstance(Context context) {
        if (il == null)
            il = new ImageLoader(context);
        return il;
    }

    private ImageLoader(Context context) {
        mContext = context;
        fileCache = new FileCache(mContext);
        executorService = Executors.newFixedThreadPool(5);
    }

    public void DisplayImage(String url, final ImageView imageView, int width, int defaultPhotoResource, boolean round) {

        if (url == null || url.trim().length() == 0 /*|| width==0*/)
            return;

        // url already requested; will update imageView when get update
//		if(imageViews.containsValue(url))
//			return;

        queuePhoto(url, imageView, width, defaultPhotoResource, round);
    }

    private void queuePhoto(String url, ImageView imageView, int width, int defaultImageResource, boolean round) {
        PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView, width, defaultImageResource, round);
        imageViews.put(imageView, url);
        executorService.submit(new PhotosLoader(photoToLoad));
    }

    // Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;
        public int width;
        public int defaultPhotoResource;
        boolean round;

        public PhotoToLoad(String u, ImageView i, int w, int defRes, boolean rnd) {
            url = u;
            imageView = i;
            width = w;
            defaultPhotoResource = defRes;
            round = rnd;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            String filename = photoToLoad.url;

            if (photoToLoad.url.startsWith("http")) {
                StorageReference sr = FirebaseStorage.getInstance().getReferenceFromUrl(photoToLoad.url);
                filename = sr.getName();
            }

            // check memoryCache
            Bitmap bitmap = memoryCache.get(filename);

            if (bitmap == null) {
                // not in memory-cache
                File f = fileCache.get(filename);

                // get from file-cache (this may be full size image - from Firebase storage)
                bitmap = decodeFile(f);

                if (bitmap == null) {
                    // not in file-cache

                    // set view with default image
                    photoToLoad.imageView.setImageResource(photoToLoad.defaultPhotoResource);
//					Log.i(TAG, "NOT found in file cache, filename:"+filename);

                } else {
                    // found in file-cache
                    if (bitmap.getHeight() > photoToLoad.width && photoToLoad.width > 0) {
                        // resize to request
                        bitmap = resize(bitmap, photoToLoad.width);
                        memoryCache.put(filename, bitmap);
                    }
                }
            } else {
                // found in memory-cache
//				Log.d(TAG, "PhotosLoader, photoToLoad.width "+photoToLoad.width+", bitmap.getHeight() "+bitmap.getHeight());

                if ((bitmap.getHeight() > photoToLoad.width) && photoToLoad.width > 0) {
                    // resize to smaller
                    bitmap = resize(bitmap, photoToLoad.width);
                    memoryCache.put(filename, bitmap);
                } else if (bitmap.getHeight() < photoToLoad.width || photoToLoad.width == 0) {
                    // image found in mem cache but of small size,
                    // look into file cache for larger image
                    File f = fileCache.get(filename);
                    if (f.exists()) {
                        // get from file cache (this may be full size image - from Firebase storage)
                        bitmap = decodeFile(f);
                        if ((bitmap.getHeight() > photoToLoad.width) && photoToLoad.width > 0) {
                            // resize to smaller
                            bitmap = resize(bitmap, photoToLoad.width);
                        }
                        memoryCache.put(filename, bitmap);
                    }
                }
            }

            if (bitmap != null) {
                BitmapDisplayer bd = new BitmapDisplayer(bitmap, photoToLoad);
                Activity a = null;//(Activity) photoToLoad.imageView.getContext();
                Context context = photoToLoad.imageView.getContext();
                // "while" is used to bubble up trough all the base context,
                // till the activity is found, or exit the loop when the root
                // context is found. Cause the root context will have a null
                // baseContext, leading to the end of the loop
                while (context instanceof ContextWrapper) {
                    if (context instanceof Activity) {
                        a = (Activity) context;
                    }
                    context = ((ContextWrapper) context).getBaseContext();
                }

                if (a != null) {
                    a.runOnUiThread(bd);
                }
                // set view with bitmap
//				if(photoToLoad.round) {
//					photoToLoad.imageView.setImageBitmap(getCircleBitmap(bitmap));
//				} else {
//					photoToLoad.imageView.setImageBitmap(bitmap);
//				}
//
//				photoToLoad.imageView.invalidate();
            } else {

                // bitmap not found, download it!!
                if (ConnectivityBroadcastReceiver.haveNetworkConnection()) {
                    getBitmapFirebase(photoToLoad);
                }
            }
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        return tag == null || !tag.equals(photoToLoad.url);
    }

    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;

            if (bitmap == null) {
                photoToLoad.imageView.setImageResource((photoToLoad.defaultPhotoResource));
            } else {

                if (photoToLoad.round) {
                    photoToLoad.imageView.setImageBitmap(getCircleBitmap(bitmap));
                } else {
                    photoToLoad.imageView.setImageBitmap(bitmap);
                }
            }

            photoToLoad.imageView.invalidate();
            imageViews.remove(photoToLoad.imageView);

        }
    }

    private void getBitmapFirebase(final PhotoToLoad ptl) {

        // Kick off DownloadService to download the file
        StorageReference httpsReference = null;
        if (ptl.url.contains("https://")) {
            httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(ptl.url);
        } else {
            httpsReference = FirebaseStorage.getInstance().getReference("media/photos/" + ptl.url);
        }

        final String filenam = httpsReference.getName();
//		Log.d(TAG, "getBitmapFirebase, httpsReference "+httpsReference);

        final long ONE_MEGABYTE = 1024 * 1024;
        httpsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
//				if(bytes==null) {
                //store default image resource in memoryCache only
//					imageViews.remove(ptl.imageView);
//				} else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

                // store full size bitmap in file cache
                fileCache.delete(filenam);
                fileCache.put(filenam, bitmap);

                // resize bitmap so that loading many of them does not result in OOM
                if (ptl.width > 0) {
                    bitmap = resize(bitmap, ptl.width);
                }

                // delete all size cache, since there is a new bitmap for this filename
                memoryCache.clear();

                //store resized image resource in memoryCache
                memoryCache.put(filenam, bitmap);

                //display photo
                BitmapDisplayer bd = new BitmapDisplayer(bitmap, ptl);
                Activity a = null;//(Activity) ptl.imageView.getContext();
                Context context = ptl.imageView.getContext();
                // "while" is used to bubble up trough all the base context,
                // till the activity is found, or exit the loop when the root
                // context is found. Cause the root context will have a null
                // baseContext, leading to the end of the loop
                while (context instanceof ContextWrapper) {
                    if (context instanceof Activity) {
                        a = (Activity) context;
                    }
                    context = ((ContextWrapper) context).getBaseContext();
                }

                if (a != null) {
                    a.runOnUiThread(bd);
                }
//					Log.d(TAG, "DownloadService, onSuccess url "+ptl.url);
//				}
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();

                // Handle any errors
//				if(exception.toString().contains("does not exist")) {
                if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    fileCache.delete(filenam);
                    memoryCache.delete(filenam);
                    //display default photo
//                    ptl.imageView.setImageResource(ptl.defaultPhotoResource);
                }

                imageViews.remove(ptl.imageView);

                // save defaultPhotoResource in memory
//				Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), ptl.defaultPhotoResource);
//				memoryCache.put(filenam, ThumbnailUtils.extractThumbnail(bitmap, ptl.width, ptl.width));

//				Log.d(TAG, "DownloadService, onFailure url "+ptl.url+", filenam "+filenam+", exception "+exception.getMessage());
            }
        });

        //		Intent intent = new Intent(mContext, DownloadService.class)
//				.putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, photoToLoad.url)
//				.setAction(DownloadService.ACTION_DOWNLOAD);
//		mContext.getApplicationContext().startService(intent);
//		Log.d(TAG, "DownloadService, url "+ptl.url);
    }

    /*
        private Bitmap getBitmapCCM(String filename, int height) {
            if(!CCWService.isCCMLoggedIn() ||
                    !ConnectivityBroadcastReceiver.haveNetworkConnection(mContext))
                return null;

            // from CCW
            Bitmap bitmap = null;
            XUsr user = new XUsr();
            user.setHeader(filename);
            user.setSubject(Integer.toString(height));
            user.setBody(Integer.toString(height));
            XRes res = ModelFacade.getInstance().fxServiceRequest(
                    ModelFacade.getInstance().SVC_IMAGE,
                    ModelFacade.getInstance().OP_GET, user);
            bitmap = (Bitmap) res.rslt;
            if (res.status.cd == 1 && bitmap != null) {
                RebootService.getConnection(mContext).addAction(Constants.contactPhotoChangeProperty, null, null);
            }

    //		Log.d(TAG, "getBitmapCCM, height "+bitmap.getHeight());
            return bitmap;
        }
    */
    public String getFileAbsolutePath(String fname) {
        return fileCache.getAbsolutePath(fname);
    }

    public void updateMemoryCache(String filename, Bitmap bmp) {
        // if filename is URL, then take file name
        if (filename.startsWith("https")) {
            StorageReference sr = FirebaseStorage.getInstance().getReferenceFromUrl(filename);
            filename = sr.getName();
        }

//		memoryCache.put(filename.substring(index>0?index:0), bmp);
        memoryCache.put(filename, bmp);
//		Log.d(TAG, "updateMemoryCache photoToLoad.filename:" + filename);
    }

    public void updateFileCache(String filename, Bitmap bmp) {
        if (filename.startsWith("https")) {
            StorageReference sr = FirebaseStorage.getInstance().getReferenceFromUrl(filename);
            filename = sr.getName();
        }
//        fileCache.put(filename.substring(index>0?index:0), bmp);
        fileCache.put(filename, bmp);
//		Log.d(TAG, "updateFileCache photoToLoad.filename:" + filename);
    }

    public File getFile(String filename, int w) {
//		Log.d(TAG, "file:" + fileCache == null ? "true" : "false");
        if (filename.startsWith("https")) {
            StorageReference sr = FirebaseStorage.getInstance().getReferenceFromUrl(filename);
            filename = sr.getName();
        }
//        return fileCache.get(filename.substring(index>0?index:0));
        return fileCache.get(filename);
    }

    private Bitmap decodeFile(File f) {
        FileInputStream fis = null;
        Bitmap bm = null;
        try {
            fis = new FileInputStream(f);
//			fis.markSupported();
            bm = BitmapFactory.decodeStream(fis);
//			fis.reset();
            fis.close();
            fis = null;
        } catch (FileNotFoundException e) {
//			e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }

    public void removeCache(String fil) {
//		memoryCache.clear();
        memoryCache.delete(fil);
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
/*
    @Override
	public void busy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void idle() {
		// TODO Auto-generated method stub

	}
*/

    private Bitmap resize(Bitmap bitmap, int height) {
        if (height == 0) {
            return bitmap;
        }

        // resize bitmap so that loading many of them does not result in OOM
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        if (inWidth > inHeight) {
            outWidth = height;
            outHeight = (inHeight * height) / inWidth;
        } else {
            outHeight = height;
            outWidth = (inWidth * height) / inHeight;
        }

        return Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and
            // width
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap decodeSampledBitmapFromResource(String filename, int reqWidth, int reqHeight)
            throws IOException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap bitmap = null;
        File f = new File(filename);// */fileCache.get(filename, reqWidth);
        FileInputStream fis1, fis2 = null;
        try {
            fis1 = new FileInputStream(f);
//			fis1.markSupported();
            BitmapFactory.decodeStream(fis1, null, options);
//			fis1.reset();
            fis1.close();
            fis1 = null;
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            fis2 = new FileInputStream(f);// InputStream created again, since
            // earlier is read once
            bitmap = BitmapFactory.decodeStream(fis2, null, options);
//			fis2.reset();
            fis2.close();
            fis2 = null;
        } catch (FileNotFoundException e) {
//			e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    private Bitmap decodeSampledBitmapFromResource(int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap rBitmap = BitmapFactory.decodeResource(mContext.getResources(), resId, options);
//	    Log.d(TAG, "decodeSampledBitmapFromResource, bitmap1 "+(rBitmap==null)+" h "+options.outHeight
//	    		+" w "+options.outWidth);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        rBitmap = BitmapFactory.decodeResource(mContext.getResources(), resId, options);
//	    Log.d(TAG, "decodeSampledBitmapFromResource, bitmap2 "+(rBitmap==null)
//	    		+" inSampleSize "+options.inSampleSize
//	    		+" reqWidth "+reqWidth+ " reqHeight "+reqHeight);
        return rBitmap;
    }

    private int getOrientation(Context context, Uri photoUri) {
		/* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION},
                null, null, null);

        if (cursor.getCount() != 1) {
            cursor.close();
            return -1;
        }

        cursor.moveToFirst();
        int i = cursor.getInt(0);
        cursor.close();
        return i;
    }

    /* gets smallest bitmap */
    private Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri, int height) {
        InputStream is1;
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        try {
            is1 = context.getContentResolver().openInputStream(photoUri);
            BitmapFactory.decodeStream(is1, null, dbo);
            is1.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        is1 = null;

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);
        rotatedWidth = dbo.outWidth;
        rotatedHeight = dbo.outHeight;

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        }

        Bitmap srcBitmap;
        InputStream is2;
        try {
            is2 = context.getContentResolver().openInputStream(photoUri);
        } catch (FileNotFoundException e) {
//			e.printStackTrace();
            return null;
        }
        if (rotatedWidth > height
                || rotatedHeight > height) {
            float widthRatio = ((float) rotatedWidth)
                    / ((float) height);
            float heightRatio = ((float) rotatedHeight)
                    / ((float) height);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            // options.inDensity = (int) maxRatio;
            // options.inTargetDensity = 1;
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is2, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is2);
        }
        try {
            is2.close();
        } catch (IOException e) {
//			e.printStackTrace();
        }
        is2 = null;

		/*
		 * if the orientation is not 0 (or -1, which means we don't know), we
		 * have to do a rotation.
		 */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
                    srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
        } else
            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
                    srcBitmap.getWidth(), srcBitmap.getHeight());

//		Log.d(TAG, "getCorrectlyOrientedImage, "+photoUri+ ", srcBitmap.getWidth:"+srcBitmap.getWidth()
//				+"srcBitmap.getHeight:"+srcBitmap.getHeight());
        return srcBitmap;
    }

    /*
     * private int getExifOrientation(String src) throws IOException { int
     * orientation = 1;
     *
     * try { if (Build.VERSION.SDK_INT >= 5) { Class<?> exifClass = Class
     * .forName("android.media.ExifInterface"); Constructor<?> exifConstructor =
     * exifClass .getConstructor(new Class[] { String.class }); Object
     * exifInstance = exifConstructor .newInstance(new Object[] { src }); Method
     * getAttributeInt = exifClass.getMethod("getAttributeInt", new Class[] {
     * String.class, int.class }); Field tagOrientationField = exifClass
     * .getField("TAG_ORIENTATION"); String tagOrientation = (String)
     * tagOrientationField.get(null); orientation = (Integer)
     * getAttributeInt.invoke(exifInstance, new Object[] { tagOrientation, 1 });
     * } } catch (ClassNotFoundException e) { e.printStackTrace(); } catch
     * (SecurityException e) { e.printStackTrace(); } catch
     * (NoSuchMethodException e) { e.printStackTrace(); } catch
     * (IllegalArgumentException e) { e.printStackTrace(); } catch
     * (InstantiationException e) { e.printStackTrace(); } catch
     * (IllegalAccessException e) { e.printStackTrace(); } catch
     * (InvocationTargetException e) { e.printStackTrace(); } catch
     * (NoSuchFieldException e) { e.printStackTrace(); }
     *
     * return orientation; }
     */
/*
	public static Bitmap getRoundedShape(Bitmap scaleBitmapImage,int width) {

			 int targetWidth = width;
			 int targetHeight = width;
			 Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
					 targetHeight,Bitmap.Config.ARGB_8888);
	
			 Canvas canvas = new Canvas(targetBitmap);
			 Path path = new Path();
			 path.addCircle(((float) targetWidth - 1) / 2,
					 ((float) targetHeight - 1) / 2,
					 (Math.min(((float) targetWidth),
							 ((float) targetHeight)) / 2),
					 Path.Direction.CCW);
			 canvas.clipPath(path);
			 canvas.drawColor(mContext.getResources().getColor(R.color.lightgreen_transparent_50pc));
			 Bitmap sourceBitmap = scaleBitmapImage;
			 canvas.drawBitmap(sourceBitmap,
					 new Rect(0, 0, sourceBitmap.getWidth(),
							 sourceBitmap.getHeight()),
					 new Rect(0, 0, targetWidth,
							 targetHeight), null);
			 return targetBitmap;
		 }
*/
    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

//		 bitmap.recycle();//uncommenting this throws exception (trying to use recycled BM

        return output;
    }

    public Drawable getTint(int drawableResource, int color) {
        Drawable d1 = mContext.getResources().getDrawable(drawableResource);
        Drawable wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(color));
        d1.invalidateSelf();
        return d1;
    }

    /*
        public Uri getImageContentUri(String filename) {
            File imageFile = new File(fileCache.getAbsolutePath(filename));
            String filePath = imageFile.getAbsolutePath();
            Cursor cursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Images.Media._ID },
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[] { filePath }, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                cursor.close();
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
           } else {
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return mContext.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        }

        private int getSquareCropDimensionForBitmap(Bitmap bitmap) {
            //If the bitmap is wider than it is tall
            //use the height as the square crop dimension
            if (bitmap.getWidth() >= bitmap.getHeight()) {
                return bitmap.getHeight();
            }
            //If the bitmap is taller than it is wide
            //use the width as the square crop dimension
            else {
                return bitmap.getWidth();
            }
        }

        public Drawable getSquareDrawableFromFile(String filename, int height, int defaultImageResource) {

            File f = fileCache.get(filename);
            if(f.exists()) {
    //			Uri uri = getImageContentUri(url);
    //			Bitmap bitmap = getCorrectlyOrientedImage(Globals.getCustomAppContext(), uri, width);
    //			Drawable d = new BitmapDrawable(Globals.getCustomAppContext().getResources(), bitmap);

                Bitmap bitmap = decodeFile(f);
    //			int dimension = getSquareCropDimensionForBitmap(bitmap);
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, height, height);
                Drawable d = new BitmapDrawable(mContext.getResources(), bitmap);
    //			Log.i(TAG, "getDrawableFromFile, uri:"+filename);
                return d;
            } else {
                return mContext.getResources().getDrawable(defaultImageResource);
            }
        }

        public Bitmap getSquareBitmapFromFile(String filename, int height, int defaultImageResource) {

            Bitmap bitmap = null;
            File f = fileCache.get(filename);
            if(f.exists()) {
                bitmap = decodeFile(f);
    //			int dimension = getSquareCropDimensionForBitmap(bitmap);
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, height, height);
            } else {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), defaultImageResource);
            }
            return bitmap;
        }

        public Bitmap getSquareBitmapFromUri(Uri uri, int height, int defaultImageResource) {

            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            } catch (FileNotFoundException e) {
    //			e.printStackTrace();
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), defaultImageResource);
            } catch (IOException e) {
    //			e.printStackTrace();
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), defaultImageResource);
            }

    //		int dimension = getSquareCropDimensionForBitmap(bitmap);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, height, height);

            return bitmap;
        }
    */
    public Bitmap getSquareBitmapFromUriBySize(Uri uri, int height/*0=same as original*/, int max_size/*in KB*/, int defaultImage) {

//		Uri uri = getImageUri(path);
        InputStream in = null;
        try {
//		    final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = mContext.getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    max_size) {
                scale++;
            }
//		    Log.d(TAG, "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = mContext.getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                int width = b.getWidth();
                if (height == 0) {
                    // maintain original size
                    height = b.getHeight();
                    width = b.getWidth();
                } else {
                    // keep size as suggested
                    width = height;
                }
//		        Log.d(TAG, "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(max_size
                        / (((double) width) / height));
                double x = (y / height) * width;

                /** choose below if whole image should be scaled to square **/
//		        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, (int) y, true);
                /** choose below if whole image should be center squared (may clip boundary to square image) **/
                Bitmap scaledBitmap = ThumbnailUtils.extractThumbnail(b, (int) x, (int) y);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

//		    Log.d(TAG, "bitmap size - width: " +b.getWidth() + ", height: " +
//		       b.getHeight());
//			b = ThumbnailUtils.extractThumbnail(b, height, height);
            return b;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return BitmapFactory.decodeResource(mContext.getResources(), defaultImage);
        }
    }

    public Drawable resize(Drawable image, int height) {
        if (height == 0) {
            return image;
        }

        Bitmap b = ((BitmapDrawable) image).getBitmap();

        // resize bitmap so that loading many of them does not result in OOM
        int outWidth;
        int outHeight;
        int inWidth = b.getWidth();
        int inHeight = b.getHeight();
        if (inWidth > inHeight) {
            outWidth = height;
            outHeight = (inHeight * height) / inWidth;
        } else {
            outHeight = height;
            outWidth = (inWidth * height) / inHeight;
        }

        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, outWidth, outHeight, false);
        return new BitmapDrawable(mContext.getResources(), bitmapResized);
    }

    // always reload the photo from server
    public void DisplayImageReload(String url, final ImageView imageView, int height, int defaultImageResource, boolean round) {

        // show local copy
        DisplayImage(url, imageView, height, defaultImageResource, round);

        if (ConnectivityBroadcastReceiver.haveNetworkConnection()) {
            // update from server
            PhotoToLoad p = new PhotoToLoad(url, imageView, height, defaultImageResource, round);
            imageViews.put(imageView, url);
            getBitmapFirebase(p);
        }
    }
}
