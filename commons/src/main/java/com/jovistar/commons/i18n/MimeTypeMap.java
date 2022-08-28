package com.jovistar.commons.i18n;

import java.util.Hashtable;

public class MimeTypeMap extends Hashtable {

    private static MimeTypeMap mtm;

    public static MimeTypeMap getInstance() {
        if (mtm == null) {
            return (mtm = new MimeTypeMap());
        }
        return mtm;
    }

    private MimeTypeMap() {

        //put(".3gp", "audio/3gpp");
        put(".3gp", "video/3gpp");
        put(".jad", "text/vnd.sun.j2me.app-descriptor");
        put(".jar", "application/java-archive");
        put(".amr", "audio/amr");
        put(".awb", "audio/amr-wb");
        put(".midi", "audio/midi");
        put(".mid", "audio/midi");
        put(".mp3", "audio/mpeg");
        //put(".mp4", "audio/mp4");
        put(".mp4", "video/mp4");
        //put(".wav", "audio/x-wav");
        put(".wav", "audio/wav");
        put(".bmp", "image/bmp");
        //put(".bmp", "image/x-bmp");
        put(".gif", "image/gif");
        put(".jpeg", "image/jpeg");
        put(".jpg", "image/jpeg");
        put(".png", "image/png");
        put(".tiff", "image/tiff");
        put(".tif", "image/tiff");
        //put(".txt", "text/plain");
    }
}
