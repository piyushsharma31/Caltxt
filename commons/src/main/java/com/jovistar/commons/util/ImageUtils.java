package com.jovistar.commons.util;


import android.content.Context;

import com.jovistar.commons.constants.Constants;


/**
 * This class contains some functions
 * that are common during image manipulation
 * 
 * 
 * In a perfect world, you wouldnt need this class since
 * JSR-234 would do the same work faster and better :(
 * 
 */

public final class ImageUtils
{
    static ImageUtils instance;
    static Context mContext;

    public static ImageUtils getInstance(Context context) {
        mContext = context;
        if(instance==null)
            instance = new ImageUtils();
        return instance;
    }

    /*
    // blend two images:
    public static Image blend(Image img1, Image img2, int value256)
    {
        // 0. no change?
        if(value256 == 0xFF) return img1;
        if(value256 == 0x00) return img2;
        
        // 1. get blended image:
        int w1 = img1.getWidth();
        int h1 = img1.getHeight();
        int w2 = img2.getWidth();
        int h2 = img2.getHeight();
        
        int w0 = Math.min(w1, w2);
        int h0 = Math.min(h1, h2);        
        int [] data = new int[w0 * h0];
        int [] b1 = new int[w0];
        int [] b2 = new int[w0];
        
        value256 &= 0xFF;
        
        for(int offset = 0, i = 0; i < h0; i++) {
            img1.getRGB( b1, 0, w1, 0, i, w0, 1); // get one line from each
            img2.getRGB( b2, 0, w2, 0, i, w0, 1);
                        
            for(int j = 0; j < w0; j++)  // blend all pixels
                data[offset ++] = ColorUtils.blend( b1[j], b2[j], value256);
        }
        
        Image tmp = Image.createRGBImage(data, w0, h0, true);
        data = null; // can this help GC at this point?
        
        return Image.createImage(tmp);
    }
*/
    // reduce image dimension to half
/*    private static Bitmap halve(Bitmap org)
    {
        int w1 = org.getWidth();
        int h1 = org.getHeight();
        
        int w2 = w1 / 2;
        int h2 = h1 / 2;                
        
        int [] data = new int[w2 * h2];
        int [] buffer = new int[w1 * 2];
        
        for(int offset = 0, i = 0; i < h2; i++) {
            org.getRGB( buffer, 0, w1, 0, i * 2, w1, 2); // get two lines from the original
            
            int o1 = 0, o2 = 1;
            int o3 = w1, o4 = w1 + 1;
            
            for(int j = 0; j < w2; j++) {
                data[offset ++] = ColorUtils.mix( buffer[o1], buffer[o2],
                          buffer[o3], buffer[o4]);            
                o1 += 2;
                o2 += 2;
                o3 += 2;
                o4 += 2;
            }
        }
        
        Image tmp = Image.createRGBImage(data, w2, h2, true);
        data = null; // can this help GC at this point?
        
        Image ret = Image.createImage(tmp);
        return ret;
    }
 */
    public static boolean isImage(String filename) {
        if(filename==null)
            return false;
        int sz = Constants.getInstance().imagefilter.length;
        for(int i=0; i<sz;i++) {
            if(filename.endsWith(Constants.getInstance().imagefilter[i]))
                return true;
        }
        return false;
    }
/*
    public static Image resize(String imagename,
          int size_w, int size_h, boolean filter, boolean mipmap)
      throws CCMException
    {
        Image src_i = CCMIDlet.instance.fileIO.createImageFromFile(imagename);
        // set source size
        int w = src_i.getWidth();
        int h = src_i.getHeight();
        
        // no change??
        if(size_w >= w && size_h >= h) return src_i;

        // scale only after mip-mapping?
        if(mipmap) {
            while(w > size_w *2 && h > size_h * 2) {
                src_i = halve(src_i);
                w /= 2;
                h /= 2;
            }
        }
        
        int [] dst = new int[size_w * size_h];
        
        
        if(filter)
            resize_rgb_filtered(src_i, dst, w, h, size_w, size_h);
        else
            resize_rgb_unfiltered(src_i, dst, w, h, size_w, size_h);
        
        // not needed anymore
        src_i = null;
        
            
            
        Image tmp = Image.createRGBImage(dst, size_w, size_h, true);
                
        // not needed anymore
        dst = null;
        
        return Image.createImage(tmp);
    }
    
    // ------------------------------------------------

    private static final void resize_rgb_unfiltered(Image src_i, int [] dst, 
            int w0, int h0, int w1, int h1)
    {       
        int [] buffer = new int[w0];
        
        // scale with no filtering
        int index1 = 0;
        int index0_y = 0;
        
        for(int y = 0; y < h1; y++) {
            int y_ = index0_y / h1;
            int index0_x = 0;                        
            src_i.getRGB(buffer, 0, w0, 0, y_, w0, 1);
            
            for(int x = 0; x < w1; x++) {                
                int x_ = index0_x / w1;                
                dst[index1++] = buffer[x_];
                
                // for next pixel
                index0_x += w0;
            }
            // For next line
            index0_y += h0;
        }
    }

    private static final void resize_rgb_filtered(Image src_i, int [] dst,
              int w0, int h0, int w1, int h1)
    {
        int [] buffer1 = new int[w0];
        int [] buffer2 = new int[w0];
        
        // UNOPTIMIZED bilinear filtering:               
        //         
        // The pixel position is defined by y_a and y_b,
        // which are 24.8 fixed point numbers
        // 
        // for bilinear interpolation, we use y_a1 <= y_a <= y_b1
        // and x_a1 <= x_a <= x_b1, with y_d and x_d defining how long
        // from x/y_b1 we are.
        //
        // since we are resizing one line at a time, we will at most 
        // need two lines from the source image (y_a1 and y_b1).
        // this will save us some memory but will make the algorithm 
        // noticeably slower
        
        for(int index1 = 0, y = 0; y < h1; y++) {
            
            int y_a = ((y * h0) << 8) / h1;
            
            int y_a1 = y_a >> 8;            
            int y_d = y_a & 0xFF;
            
            int y_b1 = y_a1 + 1;            
            if(y_b1 >= h0) {
                y_b1 = h0-1;
                y_d = 0;
            }
            
            // get the two affected lines:
            src_i.getRGB(buffer1, 0, w0, 0, y_a1, w0, 1);            
            if(y_d != 0)
                src_i.getRGB(buffer2, 0, w0, 0, y_b1, w0, 1);
            
            for(int x = 0; x < w1; x++) {                 
                // get this and the next point
                int x_a = ((x * w0) << 8) / w1;
                int x_a1 = x_a >> 8;
                int x_d = x_a & 0xFF;
                
                
                int x_b1 = x_a1 + 1;                                
                if(x_b1 >= w0) {
                    x_b1 = w0-1;
                    x_d = 0;
                }
                
                
                // interpolate in x
                int c12, c34, c1234;
                int c1 = buffer1[x_a1];
                int c3 = buffer1[x_b1];
                
                // interpolate in y:
                if(y_d == 0) {   
                    c12 = c1;
                    c34 = c3;
                } else {
                    int c2 = buffer2[x_a1];
                    int c4 = buffer2[x_b1];
                    
                    c12 = ColorUtils.blend(c2, c1, y_d);
                    c34 = ColorUtils.blend(c4, c3, y_d);
                }
                
                // final result
                dst[index1++] = ColorUtils.blend(c34, c12, x_d);
            }
        }
        
    }
*/
/*8,6
    public static Image createThumbnail(String imagename, int thumbWidth, int thumbHeight)
        throws CCMException {
        Image image = readImageFromPhone(imagename);
        int sourceWidth = image.getWidth();
        int sourceHeight = image.getHeight();

//        int thumbWidth = 64;
  //      int thumbHeight = -1;

        if (thumbHeight == -1) {
            thumbHeight = thumbWidth * sourceHeight / sourceWidth;
        }

        Image thumb = Image.createImage(thumbWidth, thumbHeight);
        Graphics g = thumb.getGraphics();

        for (int y = 0; y < thumbHeight; y++) {
            for (int x = 0; x < thumbWidth; x++) {
                g.setClip(x, y, 1, 1);
                int dx = x * sourceWidth / thumbWidth;
                int dy = y * sourceHeight / thumbHeight;
                g.drawImage(image, x - dx, y - dy, Graphics.LEFT | Graphics.TOP);
            }
        }

        Image immutableThumb = Image.createImage(thumb);

        return immutableThumb;
    }
*/
//CCMIDlet.instance.displayFactory.getProgressUI().showInfo("drawing image..");
//        return getThumbnail(imagename, expectedWidth, expectedHeight);
/*
    final int sourceWidth = image.getWidth();
    final int sourceHeight = image.getHeight();
    int thumbWidth = -1;
    int thumbHeight = -1;

    // big width
    if (sourceWidth >= sourceHeight) {
    thumbWidth = expectedWidth - padding;
    thumbHeight = thumbWidth * sourceHeight / sourceWidth;
    // fits to height ?
    if (thumbHeight > (expectedHeight - padding)) {
    thumbHeight = expectedHeight - padding;
    thumbWidth = thumbHeight * sourceWidth / sourceHeight;
    }
    } else {
    // big height
    thumbHeight = expectedHeight - padding;
    thumbWidth = thumbHeight * sourceWidth / sourceHeight;
    // fits to width ?
    if (thumbWidth > (expectedWidth - padding)) {
    thumbWidth = expectedWidth - padding;
    thumbHeight = thumbWidth * sourceHeight / sourceWidth;
    }
    }

    // XXX As we do not have floating point, sometimes the thumbnail resolution gets bigger ...
    // we are trying hard to avoid that ..
    thumbHeight = (sourceHeight < thumbHeight) ? sourceHeight : thumbHeight;
    thumbWidth = (sourceWidth < thumbWidth) ? sourceWidth : thumbWidth;

    return getThumbnail(image, thumbWidth, thumbHeight);*/
//    }
/*12,4
    public static final Image getThumbnail(String imagename, int thumbWidth, int thumbHeight)
            throws CCMException {
        Image image = readImageFromPhone(imagename);

        int x, y, pos, tmp, z = 0;
        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();

        // integer ratio ..
        final int ratio = sourceWidth / thumbWidth;

        // buffer where we read in data from image source
        final int[] in = new int[sourceWidth];

        // buffer of output thumbnail image
        final int[] out = new int[thumbWidth * thumbHeight];

        final int[] cols = new int[thumbWidth];

        // pre-calculate columns we need to access from source image
        for (x = 0, pos = 0; x < thumbWidth; x++) {
            cols[x] = pos;

            // increase the value without fraction calculation
            pos += ratio;
            tmp = pos + (thumbWidth - x) * ratio;
            if (tmp > sourceWidth) {
                pos--;
            } else if (tmp < sourceWidth - ratio) {
                pos++;
            }
        }

        // read through the rows ..
        for (y = 0, pos = 0, z = 0; y < thumbHeight; y++) {

            // read a single row ..
            image.getRGB(in, 0, sourceWidth, 0, pos, sourceWidth, 1);

            for (x = 0; x < thumbWidth; x++, z++) {
                // write this row to thumbnail
                out[z] = in[cols[x]];
            }

            pos += ratio;
            tmp = pos + (thumbHeight - y) * ratio;
            if (tmp > sourceHeight) {
                pos--;
            } else if (tmp < sourceHeight - ratio) {
                pos++;
            }
        }
        return Image.createRGBImage(out, thumbWidth, thumbHeight, false);
    }
*/
/*7,5
    public static Image resizeImage(String imagename, int thumbWidth, int thumbHeight)
        throws CCMException {
        Image src = readImageFromPhone(imagename);
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Image tmp = Image.createImage(thumbWidth, srcHeight);
        Graphics g = tmp.getGraphics();
        int ratio = (srcWidth << 16) / thumbWidth;
        int pos = ratio / 2;

        //Horizontal Resize
        for (int x = 0; x < thumbWidth; x++) {
            g.setClip(x, 0, 1, srcHeight);
            g.drawImage(src, x - (pos >> 16), 0, Graphics.LEFT | Graphics.TOP);
            pos += ratio;
        }

        Image resizedImage = Image.createImage(thumbWidth, thumbHeight);
        g = resizedImage.getGraphics();
        ratio = (srcHeight << 16) / thumbHeight;
        pos = ratio / 2;

        //Vertical resize

        for (int y = 0; y < thumbHeight; y++) {
            g.setClip(0, y, thumbWidth, 1);
            g.drawImage(tmp, 0, y - (pos >> 16), Graphics.LEFT | Graphics.TOP);
            pos += ratio;
        }
        return resizedImage;

    }//resize image
 */

/*
	public static Bitmap scaleImage(Bitmap bitmap, ImageView view1, int boundBoxInDp) {
		// Get the ImageView and its bitmap
//	    ImageView view = (ImageView) findViewById(R.id.image_box);
//	    Drawable drawing = view.getDrawable();
//	    if (drawing == null) {
//	        return; // Checking for null & return, as suggested in comments
//	    }
//	    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

	    // Get current dimensions AND the desired bounding box
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int bounding = boundBoxInDp;//dpToPx(boundBoxInDp);
	    Log.i("Test", "original width = " + Integer.toString(width));
	    Log.i("Test", "original height = " + Integer.toString(height));
	    Log.i("Test", "bounding = " + Integer.toString(bounding));

	    // Determine how much to scale: the dimension requiring less scaling is
	    // closer to the its side. This way the image always stays inside your
	    // bounding box AND either x/y axis touches it.
	    float xScale = ((float) bounding) / width;
	    float yScale = ((float) bounding) / height;
	    float scale = (xScale <= yScale) ? xScale : yScale;
	    Log.i("Test", "xScale = " + Float.toString(xScale));
	    Log.i("Test", "yScale = " + Float.toString(yScale));
	    Log.i("Test", "scale = " + Float.toString(scale));

	    // Create a matrix for the scaling and add the scaling data
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale, scale);

	    // Create a new bitmap and convert it to a format understood by the ImageView
	    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	    width = scaledBitmap.getWidth(); // re-use
	    height = scaledBitmap.getHeight(); // re-use
//	    BitmapDrawable result = new BitmapDrawable(scaledBitmap);
	    Log.i("Test", "scaled width = " + Integer.toString(width));
	    Log.i("Test", "scaled height = " + Integer.toString(height));

//	    view.setImageDrawable(result);

	    // Now change ImageView's dimensions to match the scaled image
//	    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
//	    params.width = width;
//	    params.height = height;
//	    view.setLayoutParams(params);

	    Log.i("Test", "done");
	    return scaledBitmap;
	}

	private static int dpToPx(int dp) {
	    float density = mContext.getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
*/

}
