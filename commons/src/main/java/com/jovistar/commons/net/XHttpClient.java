package com.jovistar.commons.net;

/*
public class XHttpClient {
//	The time it takes for our client to timeout
    public static final int BUFFER_SIZE_LOCAL = 819200;//max request chunk size, 800kpbs
    public static final int BUFFER_SIZE_REMOTE = 20480;//max response chunk size, 30kbps
	public static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds

//	Single instance of our HttpClient
	private static HttpClient mHttpClient;

//	 Get our single instance of our HttpClient object.
//	 @return an HttpClient object with connection parameters set
	private static HttpClient getHttpClient() {
		if (mHttpClient == null) {
			mHttpClient = new DefaultHttpClient();
			final HttpParams params = mHttpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
			ConnManagerParams.setTimeout(params, HTTP_TIMEOUT);
		}
		return mHttpClient;
	}

	private static String executeHttpPost(String url,
			ArrayList<NameValuePair> postParameters) throws Exception {
		BufferedReader in = null;
		try {
			HttpClient client = getHttpClient();
			HttpPost request = new HttpPost(url);
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
					postParameters);
			request.setEntity(formEntity);
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();

			String result = sb.toString();
			return result;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
		}
	}

	private static String executeHttpGet(String url) throws Exception {
		BufferedReader in = null;
		try {
			HttpClient client = getHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();

			String result = sb.toString();
			return result;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
		}
	}

    public ByteArrayOutputStream getShareOverHTTP(String uri, ByteArrayInputStream bis, IDisplayObject caller) throws CCMException {
        return postResourceOverHTTP(uri, bis);
    }

    public ByteArrayOutputStream postImageOverHTTP(String uri, InputStream bis, IDisplayObject caller) throws CCMException {
        return postResourceOverHTTP(uri, bis);
    }

    public static Bitmap getImageOverHTTP(String uri, ByteArrayInputStream bis, IDisplayObject caller) throws CCMException {
        Bitmap img = null;
        ByteArrayOutputStream bos = postResourceOverHTTP(uri, bis);
        ByteArrayInputStream imgbis = new ByteArrayInputStream(bos.toByteArray());
        Log.d("XHttpClient","NetworkIO:getImageOverHTTP byte recd:" + bos.size());
        img = BitmapFactory.decodeStream(imgbis);
        if(img==null){
            Log.d("XHttpClient","could not decode image uri:" + uri);
            //suppress exception to avoid error box for icons
            //throw new CCMException(midlet.properties.info_nophoto);
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

    public static ByteArrayOutputStream postResourceOverHTTP(String uri,
//            String mimeType,
            InputStream bais) throws CCMException {

		InputStream in = null;
		ByteArrayOutputStream baos = null;
		try {
			HttpClient client = getHttpClient();
			HttpPost request = new HttpPost(uri);
            Log.d("XHttpClient", "postResourceOverHTTP:txRequest available read "+bais.available());
			InputStreamEntity formEntity = new InputStreamEntity(bais, bais.available());
			request.setEntity(formEntity);
			HttpResponse response = client.execute(request);
			in = response.getEntity().getContent();
            Log.d("XHttpClient", "postResourceOverHTTP:txRequest"+in.available());

            baos = rxResponse(in);
            Log.d("XHttpClient", "postResourceOverHTTP:rxResponse");
		} catch(IOException e){
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
		}
        return baos;
    }

    public InputStream restorePIMOverHTTP(
            String uri,
            String mimeType, IDisplayObject caller) throws CCMException {
    	
    }

    public InputStream backupPhonePIMOverHTTP(
            String uri,
            String mimeType,
            int listType, IDisplayObject caller) throws CCMException {
    	
    }

    private void txRequest(InputStream localis, OutputStream remoteos)
            throws IOException {
        if (localis == null || remoteos == null) {
            Log.d("XHttpClient","txRequest: localis or remoteos null");
            return;
        }

        byte b[] = null;
        Log.d("XHttpClient","txRequest len:" + localis.available());
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
//            caller.setTitle(sb.toString());
        }
        //globalos.flush();//flush on OutputStream does nothing
        b = null;
        sb = null;
    }

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

    private static ByteArrayOutputStream rxResponse(InputStream remoteis)
            throws IOException {

//        ByteArrayInputStream bis = null;
        StringBuffer sb = new StringBuffer();
        //caller.setTitle(sb.append("waiting").toString());
        Log.d("XHttpClient","rxResponse len available:" + remoteis.available());
//        Log.d("XHttpClient","rxResponse len:" + length);

        byte b[] = null;
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
//                caller.setTitle(sb.toString());
            }
            bos.flush();
            Log.d("XHttpClient","rxResponse received:" + bos.size());
            Log.d("XHttpClient","rxResponse received count:" + totalrx);
            //globalos.flush();//flush on OutputStream does nothing
//            b = null;
//            b = bos.toByteArray();

//            bis = new ByteArrayInputStream(bos.toByteArray());
        } finally {
            b = null;
            sb = null;
        }
        return bos;
    }

}

*/