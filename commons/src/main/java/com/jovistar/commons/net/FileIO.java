package com.jovistar.commons.net;


import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
//import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
//import java.io.ByteArrayInputStream;
//import javax.microedition.io.HttpConnection;
//import javax.microedition.io.ConnectionNotFoundException;

//import javax.microedition.lcdui.Image;
import com.jovistar.commons.ui.IDisplayObject;
//import javax.microedition.io.Connector;
//import javax.microedition.io.file.FileConnection;


public class FileIO {

	private static FileIO instance;
	public static FileIO getInstance(){
		if(instance==null)
			instance=new FileIO();
		return instance;
	}
    public static InputStream getFileInputStream(String uri, IDisplayObject caller) throws IOException/*CCMException*/ {

        File fc = null;
        InputStream dis = null;
        try {
            fc = new File(uri);
            if (!fc.exists()) {
                throw new IOException(uri + " does not exists");
            }
            dis = new FileInputStream(fc);
        } catch (IOException e) {
        	throw e;
//            throw new CCMException(uri + " could not load");
        } finally {
//            try {
                if (fc != null) {
//                    fc.close();
                    fc = null;
                }
//            } catch (IOException es) {
//            }
        }
        return dis;
    }

    public static ByteArrayOutputStream createByteArrayOutStreamFromFile(String uri,
            IDisplayObject caller) throws IOException/*CCMException*/ {

        InputStream dis = getFileInputStream(uri,caller);
        int count = 0, totalrx = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = null;
        StringBuffer sb = new StringBuffer();
        try {
//            if(dis.available()<=0)
                b = new byte[NetworkIO.BUFFER_SIZE_LOCAL];
  //          else
    //            b = new byte[dis.available()];

            while ((count = dis.read(b)) != -1) {
                totalrx += count;
                sb.setLength(0);
                sb.append("read ");
                NetworkIO.appendSizeInKB(totalrx, sb);
///                caller.setTitle(sb.toString());
                bos.write(b, 0, count);
            }
        } catch (IOException e) {
        	throw e;
//            throw new CCMException(e.getMessage());
        } finally {
            if(dis!=null) {
                try{dis.close();}catch(IOException e){}
            }
            dis = null;
            b = null;
            sb = null;
        }
        return bos;
    }
/*
    public void createFileFromByteArray(String uri, byte[] barray) throws CCMException {

        File fc = null;
        DataOutputStream dos = null;
        try {
            fc = (File) Connector.open(uri, Connector.WRITE);
            dos = fc.openDataOutputStream();
            dos.write(barray);
            dos.flush();
        } catch (IOException e) {
            throw new CCMException(uri + " could not create");
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (fc != null) {
                    fc.close();
                }
            } catch (IOException es) {
            }
        }
    }

    public Image createImageFromFile(String uri) throws CCMException {

        Image image = null;
        FileConnection fc = null;
        InputStream fis = null;
        try {
            fc = (FileConnection) Connector.open(uri, Connector.READ);
            if (!fc.exists()) {
                throw new IOException(uri + " does not exists");
            }
            fis = fc.openInputStream();
            image = Image.createImage(fis);
        } catch (Exception e) {
            throw new CCMException(uri + " image could not load");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fc != null) {
                    fc.close();
                }
            } catch (IOException es) {
            }
        }
        return image;
    }*/
}
