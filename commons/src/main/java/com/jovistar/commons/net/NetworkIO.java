package com.jovistar.commons.net;

import java.io.IOException;
//import java.io.DataInputStream;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
//import javax.microedition.io.Connector;
//import javax.microedition.io.HttpConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jovistar.commons.constants.Constants;
import com.jovistar.commons.ui.IDisplayObject;

public class NetworkIO {

	private static NetworkIO instance;

	public static NetworkIO getInstance() {
		if (instance == null)
			instance = new NetworkIO();
		return instance;
	}

	// TimerTask globalcox_timerTask;
	// Timer globalcox_timer;
	// boolean connectionNotEstablished = true;
	public static final int BUFFER_SIZE_LOCAL = 819200;// max request chunk
														// size, 800kpbs
	public final int BUFFER_SIZE_REMOTE = 20480;// max response chunk
														// size, 30kbps
	// MIME Types
	public final String MIMETYPE_UTF8_CHARSET = "UTF-8;q=0.7,*;q=0.7";
	public final String MIMETYPE_BINARY = "application/octet-stream";
	public final String MIMETYPE_TEXT_XML = "text/xml";
	// HTTP Contants
	private final String HTTPHDR_ACCEPT = "Accept";
	private final String HTTPHDR_USER_AGENT = "User-Agent";
	private String HTTPHDR_USER_AGENT_VALUE;
	private final String HTTPHDR_CONNECTION = "Connection";
	private final String HTTPHDR_CONNECTION_CLOSE = "close";
	private final String HTTPHDR_CACHE_CONTROL = "Cache-Control";
	private final String HTTPHDR_CACHE_CONTROL_NOTRANSFORM = "no-transform";
	private final String HTTPHDR_CONTENT_TYPE = "Content-Type";
//	HttpURLConnection globalcox = null;
//	InputStream globalis = null;
//	OutputStream globalos = null;

	public NetworkIO() {
		HTTPHDR_USER_AGENT_VALUE = "Android";
	}

	private HttpURLConnection connectHTTP(String uri, String reqtype, String mimeType) throws IOException {
		// connectionNotEstablished = true;//indicate connection not established
		// (untill read starts)
		// startConnectionWatch();
		URL url = new URL(uri);
//		Log.d("NetworkIO", "connectHTTP "+uri);

		// globalcox = (HttpConnection) Connector.open(uri, readwrite, true);
		HttpURLConnection globalcox = (HttpURLConnection) url.openConnection();
		globalcox.setConnectTimeout(1000*Constants.getInstance().CONNECTION_TIMEOUT_SEC);
		url = null;

		if (reqtype.equalsIgnoreCase("POST"))
			globalcox.setDoOutput(true);

		// globalcox.setChunkedStreamingMode(0);

		// globalcox.setRequestMethod(reqtype);
		globalcox.setRequestProperty(HTTPHDR_USER_AGENT,
				HTTPHDR_USER_AGENT_VALUE);
		globalcox
				.setRequestProperty(HTTPHDR_CONTENT_TYPE, MIMETYPE_BINARY/* MIMETYPE_TEXT_XML */);
		globalcox.setRequestProperty(HTTPHDR_ACCEPT, mimeType);
		/*
		 * HTTPHDR_CONNECTION_CLOSE to signal server that cx will be closed
		 * after this request/response getover
		 */
		globalcox.setRequestProperty(HTTPHDR_CONNECTION,
				HTTPHDR_CONNECTION_CLOSE);
		globalcox.setRequestProperty(HTTPHDR_CACHE_CONTROL,
				HTTPHDR_CACHE_CONTROL_NOTRANSFORM);
		return globalcox;
	}

	private void initialize() throws IOException {
	}

	private void txRequest(InputStream localis/* read from */,
			OutputStream remoteos/* write to *//* , IDisplayObject caller */)
			throws IOException {
		if (localis == null || remoteos == null) {
//			Log.d("NetworkIO", "txRequest: localis or remoteos null");
			return;
		}

        byte[] b = null;
//		Log.d("NetworkIO", "txRequest len:" + localis.available());
		if (localis.available() > 0) {
			b = new byte[localis.available()];
		} else {
			b = new byte[BUFFER_SIZE_LOCAL];
		}
		int c = 0, totaltx = 0;
		StringBuffer sb = new StringBuffer();
		while ((c = localis.read(b)) > 0) {
			remoteos.write(b, 0, c);
			totaltx += c;
			sb.setLength(0);
			sb.append("sent ");
			appendSizeInKB(totaltx, sb);
			// caller.setTitle(sb.toString());
		}
		// globalos.flush();//flush on OutputStream does nothing
		b = null;
		sb = null;
	}

	private ByteArrayOutputStream/* write to */rxResponse(InputStream remoteis/*
																			 * read
																			 * from
																			 */)
			throws IOException {

		// ByteArrayInputStream bis = null;
		StringBuffer sb = new StringBuffer();
		// caller.setTitle(sb.append("waiting").toString());
//		Log.d("NetworkIO", "rxResponse len available:" + remoteis.available());
		// Log.d("NetworkIO","rxResponse len:" + length);

        byte[] b = null;
		if (remoteis.available() > 0) {
			b = new byte[remoteis.available()];
		} else {
			b = new byte[BUFFER_SIZE_REMOTE];
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			int c = 0, totalrx = 0;
			while ((c = remoteis.read(b)) > 0) {
				bos.write(b, 0, c);
				totalrx += c;
				sb.setLength(0);
				sb.append("rcvd ");
				appendSizeInKB(totalrx, sb);
				// caller.setTitle(sb.toString());
			}
			bos.flush();
//			Log.d("NetworkIO", "rxResponse received:" + bos.size());
			// globalos.flush();//flush on OutputStream does nothing
			// b = null;
			// b = bos.toByteArray();

			// bis = new ByteArrayInputStream(bos.toByteArray());
		} finally {
			b = null;
			sb = null;
		}
		return bos;
	}
/*
	public void globalcloseCox() {
		if (globalis != null) {
			try {
				globalis.close();
			} catch (IOException ioe) {
			}
		}
		if (globalos != null) {
			try {
				globalos.close();
			} catch (IOException ioe) {
			}
		}
		if (globalcox != null) {
			globalcox.disconnect();
		}
		globalcox = null;
		globalis = null;
		globalos = null;
	}
*/
	public static void appendSizeInKB(long size, StringBuffer sb) {
		if (size < 1024) {
			sb.append(size).append(" b");
		} else if (size > 1024 && size < 1048576) {
			sb.append(size / 1024).append(" kb");
		} else {
			long m = size / 1048576;
			long k = (size % 1048576) / 1024;
			sb.append(m).append(".");
			sb.append(k).append(" mb");
		}
	}

	public ByteArrayOutputStream postResourceOverHTTP(String uri,
			String mimeType, InputStream bais/* , IDisplayObject caller */)
			throws IOException/*CCMException*/ {

		HttpURLConnection globalcox = null;
		OutputStream globalos = null;
		InputStream globalis = null;
		ByteArrayOutputStream bos = null;

		try {
			globalcox = connectHTTP(uri, "POST", mimeType);
//			Log.d("NetworkIO", "postResourceOverHTTP:connectHTTP");
			/*
			 * commented because this check consume time int rc =
			 * globalcox.getResponseCode();
			 * 
			 * //If an HTTP error was encountered, stop, indicate error if (rc
			 * != HttpConnection.HTTP_OK) { //Log error, throw IO exception
			 * throw new IOException("Network error:" + rc); }
			 */
//			initialize("POST", mimeType);
//			Log.d("NetworkIO", "postResourceOverHTTP:initialize "+uri);
			globalos = globalcox.getOutputStream();
//			Log.d("NetworkIO", "postResourceOverHTTP:getOutputStream");

			// write request xml to server
			txRequest(bais, globalos/* , caller */);
//			Log.d("NetworkIO", "postResourceOverHTTP:txRequest");

			// caller.setTitle(midlet.properties.title_receiving);
			// caller.setTitle("waiting");
			// globalis = globalcox.openDataInputStream();//this takes longer
			// than read
			globalis = globalcox.getInputStream();// this takes longer than read
//			Log.d("NetworkIO", "postResourceOverHTTP:getInputStream");
			// connectionNotEstablished = false;
			// int length = (int) globalcox.getLength();

			/******* READ STREAM AND RETURN BYTEARRAY STREAM ********/
			bos = rxResponse(globalis/* , globalcox.getContentLength(), caller */);
//			Log.d("NetworkIO", "postResourceOverHTTP:rxResponse");
		} catch (IOException e) {
			throw e;
//			throw new CCMException(e.getMessage());
		} finally {
//			globalcloseCox();
			if (globalis != null) {
				try {
					globalis.close();
				} catch (IOException ioe) {
				}
			}
			if (globalos != null) {
				try {
					globalos.close();
				} catch (IOException ioe) {
				}
			}
			if (globalcox != null) {
				globalcox.disconnect();
			}
			globalcox = null;
			globalis = null;
			globalos = null;
		}

		return bos;
	}

	/*
	 * public InputStream restorePIMOverHTTP( String uri, String mimeType,
	 * IDisplayObject caller) throws CCMException {
	 * 
	 * ByteArrayOutputStream bos = null; ByteArrayInputStream bis = null; try {
	 * bos = postResourceOverHTTP(uri, mimeType, null, caller);
	 * caller.setTitle(Properties.getInstance().title_restoring); bis = new
	 * ByteArrayInputStream(bos.toByteArray());
	 * 
	 * midlet.backup.restorePIMList(bis); } catch (IOException e) { throw new
	 * CCMException(e.getMessage()); } finally { if (bos != null) { try {
	 * bos.close(); } catch (IOException ioe) { } } bos = null; } return bis; }
	 * 
	 * public InputStream backupPhonePIMOverHTTP( String uri, String mimeType,
	 * int listType, IDisplayObject caller) throws CCMException {
	 * 
	 * //HttpConnection cox = null; //DataInputStream is = null;
	 * //DataOutputStream os = null;
	 * 
	 * try { globalcox = connectHTTP(uri, Connector.READ_WRITE, caller);
	 * initialize(HttpConnection.POST, mimeType); globalos =
	 * globalcox.openDataOutputStream(); try {
	 * caller.setTitle(midlet.properties.title_backingup);
	 * midlet.backup.backupPimLists(globalos, listType, ""); } catch
	 * (PIMException pe) { throw new CCMException(pe.getMessage()); }
	 * globalos.flush(); //commented because this check consume time // int rc =
	 * globalcox.getResponseCode();
	 * 
	 * //If an HTTP error was encountered, stop, indicate error // if (rc !=
	 * HttpConnection.HTTP_OK) { // throw new IOException("Network error:" +
	 * rc); // } globalis = globalcox.getInputStream(); } catch (IOException e)
	 * { throw new CCMException(e.getMessage()); } finally { globalcloseCox(); }
	 * 
	 * return globalis; }
	 */
	public Bitmap getImageOverHTTP(String uri, String mimeType,
			ByteArrayInputStream bis, IDisplayObject caller)
			throws IOException/*CCMException*/ {
		Bitmap img = null;
		ByteArrayOutputStream bos = postResourceOverHTTP(uri, mimeType, bis/*
																			 * ,
																			 * caller
																			 */);
		ByteArrayInputStream imgbis = new ByteArrayInputStream(
				bos.toByteArray());
//		Log.d("NetworkIO", "NetworkIO:getImageOverHTTP byte recd:" + bos.size());
		img = BitmapFactory.decodeStream(imgbis);
		if (img == null) {
//			Log.d("NetworkIO", "could not decode image uri:" + uri);
			// suppress exception to avoid error box for icons
			// throw new CCMException(midlet.properties.info_nophoto);
		}

		if (imgbis != null) {
			try {
				imgbis.close();
			} catch (IOException e) {
			}
		}
		if (bos != null) {
			try {
				bos.close();
			} catch (IOException e) {
			}
		}
		imgbis = null;
		bos = null;

		return img;
	}

	public ByteArrayOutputStream getShareOverHTTP(String uri, String mimeType,
			ByteArrayInputStream bis, IDisplayObject caller)
			throws IOException/*CCMException*/ {
		// let the caller decide what to do with incoming share
		return postResourceOverHTTP(uri, mimeType, bis/* , caller */);
	}

	public ByteArrayOutputStream postImageOverHTTP(String uri, String mimeType,
			InputStream bis, IDisplayObject caller) throws IOException/*CCMException*/ {
		// caller.setTitle(Properties.getInstance().title_uploading);
		return postResourceOverHTTP(uri, mimeType, bis/* , caller */);
	}
}
