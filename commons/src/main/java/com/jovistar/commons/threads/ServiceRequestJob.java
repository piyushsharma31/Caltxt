package com.jovistar.commons.threads;


import java.io.InputStreamReader;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XR03;
import com.jovistar.commons.bo.XReqSts;
import com.jovistar.commons.bo.XRes;
import com.jovistar.commons.exception.CCMException;
import com.jovistar.commons.facade.ModelFacade;
import com.jovistar.commons.net.FileIO;
import com.jovistar.commons.net.NetworkIO;
import com.jovistar.commons.ui.IDisplayObject;
import com.jovistar.commons.util.XMLUtil;

import java.io.IOException;
//import javax.microedition.lcdui.Image;
import android.graphics.Bitmap;
import android.util.Log;
//import java.io.ByteArrayInputStream;
//import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
//import java.io.DataInputStream;
//import javax.microedition.pim.PIM;


public class ServiceRequestJob implements Runnable {

	/***** shared by all service request jobs ********/
	// private static CCMIDlet midlet;
	// private static Semaphore semaphore;
	private static int requestid;
	private static String service_host;
	private static int service_port;

	private IDTObject param;
	private IDisplayObject callerUI;
	private short op;
	private short svc;

	public static void init(String h, int p) {
		// semaphore = new Semaphore(1);
		requestid = 0;
		service_host = h;//Properties.getInstance().sys_server;
		service_port = p;//Integer.parseInt(Properties.getInstance().sys_port);
	}

	public short getOp() {
		return op;
	}

	public short getSvc() {
		return svc;
	}

	public ServiceRequestJob(short servicename, short oper, IDTObject param,
			IDisplayObject caller) {

		this.svc = servicename;
		this.op = oper;
		this.param = param;
		this.callerUI = caller;
	}

	/*
	 * public Cache getCache() { return
	 * Cache.getInstance(CCMIDlet.instance.systemPrefs.getInt("sys.cache", 12));
	 * }
	 */
	private XR03 buildRequest() {
		XR03 request = new XR03();
		request.id = (requestid++);
		request.sesid = ModelFacade.getInstance().getThisUserSessionId();
		request.svc = (svc);
		request.op = (op);
		request.unm = (ModelFacade.getInstance().getThisUsername());
		request.param = (param);
		return request;
	}

	public void run() {
		// semaphore.acquire();
		// CCLog.d("ServiceRequestJob","ServiceRequestJob, lock acquired, svc:op:"
		// + svc + ":" + op);
		// DTOListUI.m_ProgressDialog=ProgressDialog.show((Context) callerUI,
		// "", "Please wait", true);

		XRes response = null;
//		try {
			response = (XRes) execute();
//		} catch (Throwable e) {
//		} finally {
//		}

		try {
			// callerUI.setTitle(null);//to reset to previous title
//			Log.d("run", "GOING to CALLBACK with response " + response);
			callerUI.callback(response);
		} catch (CCMException e) {
			// IDisplayFactory.getInstance().getProgressUI().showError(e.getMessage());
			e = null;
		} finally {
			// CCLog.d("ServiceRequestJob","ServiceRequestJob, lock release, svc:op:"
			// + svc + ":" + op);
			// semaphore.release();
			// if (response != null) {
			// Object o = response.rslt;
			// response.rslt = null;
			// o = null;
			// }
			// response = null;
			// param = null;
			// callerUI = null;
			if(callerUI!=null)
				callerUI.idle();
		}
	}

	public XRes SyncExecute() {
/*		XRes response = null;
		try {
			response = (XRes) execute();
		} catch (Throwable e) {
		} finally {
		}
*/
		return (XRes) execute();
	}

	private String createUrl(String username, short svc, short op, String sesid) {
		StringBuffer url = new StringBuffer(80);
//		Log.v("SeviceRequestJob::createUrl", "srv:"+service_host+" port:"+service_port);
		url.append("http://").append(service_host).append(":")
				.append(service_port);
		url.append("/ccw/").append(username).append("/").append(svc)
				.append("/");
		url.append(op).append("/").append(sesid);

		String s = url.toString();
//		Log.v("SeviceRequestJob::createUrl", "URL:"+s);
		url = null;
		return s;
	}

	private IDTObject execute() /*throws CCMException*/ {

		XR03 request = buildRequest();
		XRes response = null;
		String exception_msg = "OK";

		// construct default response (OK), this will be sent, otherwise
		// exception thrown
		XRes res = new XRes();// IDTOFactory.getInstance().getNewXResponse();
		res.op = request.op;
		res.sesid = request.sesid;
		res.status = new XReqSts();// IDTOFactory.getInstance().getNewRequestStatus();
		res.status.cd = 1;
		res.status.cdstr = "OK";
		res.svc = request.svc;
		res.umn = request.unm;
		response = res;

		// IDTObject/*HashMap<String, Object> */response = null;
		byte[] data = null;
		// StringBuffer data = null;
		InputStreamReader isr = null;
		InputStream din = null;
		ByteArrayInputStream bin = null;// stream of request data

		if (request.svc != ModelFacade.getInstance().SVC_INBOX
				&& request.svc != ModelFacade.getInstance().SVC_IMAGE
				&& request.svc != ModelFacade.getInstance().SVC_AD) {
			if(callerUI!=null)
				callerUI.busy();
		}
		String url = createUrl(request.unm, request.svc, request.op,
				request.sesid);
//		Log.d("ServiceRequestJob", "ServiceRequestJob:execute:url:" + url);
		// CCMIDlet.instance.displayFactory.getProgressUI().showWait("preparing xml"+"op:"+op+"svc:"+svc);
		// callerUI.setTitle(CCMIDlet.instance.properties.title_reqesting);//moved
		// to mfacade
		try {
			// data =
			// CCMIDlet.instance.xmlUtils.getXMLFromObject2(request, 0);
			data = XMLUtil.getInstance().getWBXMLFromObject2(request, 0);
			bin = new ByteArrayInputStream(data/*
												 * data.toString().getBytes()
												 */);
//			Log.d("ServiceRequestJob", "ServiceRequestJob:execute:request: wbxml size" + data.length);
//			Log.d("ServiceRequestJob", "ServiceRequestJob:execute:request: wbxml data" + new String(data));

			if (request.svc == (ModelFacade.getInstance().SVC_PHONECONTACT)
					&& request.op == (ModelFacade.getInstance().OP_BACKUP)) {
				// din = NetworkIO.getInstance().backupPhonePIMOverHTTP(url,
				// NetworkIO.getInstance().MIMETYPE_UTF8_CHARSET,
				// PIM.CONTACT_LIST, callerUI);
			} else if (request.svc == (ModelFacade.getInstance().SVC_PHONECALENDAR)
					&& request.op == (ModelFacade.getInstance().OP_BACKUP)) {
				// din = NetworkIO.getInstance().backupPhonePIMOverHTTP(url,
				// NetworkIO.getInstance().MIMETYPE_UTF8_CHARSET,
				// PIM.EVENT_LIST, callerUI);
			} else if (request.svc == (ModelFacade.getInstance().SVC_PHONETODO)
					&& request.op == (ModelFacade.getInstance().OP_BACKUP)) {
				// din = NetworkIO.getInstance().backupPhonePIMOverHTTP(url,
				// NetworkIO.getInstance().MIMETYPE_UTF8_CHARSET,
				// PIM.TODO_LIST, callerUI);
			} else if (request.svc == (ModelFacade.getInstance().SVC_IMAGE)
					&& request.op == (ModelFacade.getInstance().OP_GET)) {
				Bitmap img = NetworkIO.getInstance().getImageOverHTTP(url,
						NetworkIO.getInstance().MIMETYPE_UTF8_CHARSET, bin,
						callerUI);
				res.rslt = img;
				// res.umn = ((XCtt)(request.param)).unm;
				res.umn = request.getSubject();
//			 Log.d("ServiceRequestJob", "SVC_IMAGE, OP_GET "+res.rslt);
			} else if (request.svc == (ModelFacade.getInstance().SVC_SHARE)
					&& request.op == (ModelFacade.getInstance().OP_GET)) {
				res.rslt = NetworkIO.getInstance().getShareOverHTTP(url,
						NetworkIO.getInstance().MIMETYPE_BINARY, bin, callerUI);
				// res.umn = ((XFile)(request.param)).name;
				res.umn = request.getHeader();
			} else if (request.svc == (ModelFacade.getInstance().SVC_USERIMAGE)
					&& request.op == (ModelFacade.getInstance().OP_GET)) {
				Bitmap img = NetworkIO.getInstance().getImageOverHTTP(url,
						NetworkIO.getInstance().MIMETYPE_UTF8_CHARSET, bin,
						callerUI);
				res.rslt = img;
				// res.umn = ((XCtt)(request.param)).unm;
				res.umn = request.getSubject();
			} else if (request.svc == (ModelFacade.getInstance().SVC_USERIMAGE)
					&& request.op == (ModelFacade.getInstance().OP_SET)) {

				// String fname = ((XCtt) request.param).vdocal;//filename
				// piggyback
				String fname = request.param.getBody();// filename piggyback
				InputStream fis = FileIO.getFileInputStream(fname, callerUI);

				// din = NetworkIO.getInstance().postImageOverHTTP(url,
				NetworkIO.getInstance().postImageOverHTTP(url,
						NetworkIO.getInstance().MIMETYPE_UTF8_CHARSET, fis,
						callerUI);
				if (fis != null) {
					fis.close();
				}
				// construct response (since no resp from server)
				res.rslt = null;
			} else if (request.svc == (ModelFacade.getInstance().SVC_SHARE)
					&& request.op == (ModelFacade.getInstance().OP_SET)) {

				// String fname = ((XFile) request.param).path;//local
				// filename piggyback
				String fname = request.getFooter();// local filename
													// piggyback
				InputStream fis = FileIO.getFileInputStream(fname, callerUI);

				// url += ((XFile) request.param).name;
				url += request.getSubject();
				// din = NetworkIO.getInstance().postImageOverHTTP(url,
				NetworkIO.getInstance().postImageOverHTTP(url,
						NetworkIO.getInstance().MIMETYPE_UTF8_CHARSET, fis,
						callerUI);
				if (fis != null) {
					fis.close();
				}
				// construct response (since no resp from server)
				res.rslt = null;
			} else if ((request.svc == (ModelFacade.getInstance().SVC_PHONECALENDAR)
					|| request.svc == (ModelFacade.getInstance().SVC_PHONECONTACT) || request.svc == (ModelFacade
					.getInstance().SVC_PHONETODO))
					&& request.op == (ModelFacade.getInstance().OP_RESTORE)) {
				// din = NetworkIO.getInstance().restorePIMOverHTTP(url,
				// NetworkIO.getInstance().MIMETYPE_UTF8_CHARSET, callerUI);
			} else {
//				Log.d("ServiceRequestJob", "postResourceOverHTTP");
				din = new ByteArrayInputStream(
						NetworkIO
								.getInstance()
								.postResourceOverHTTP(
										url,
										NetworkIO.getInstance().MIMETYPE_BINARY,
										bin/* , callerUI */).toByteArray());
				// din = NetworkIO.getInstance().postResourceOverHTTP(url,
				// NetworkIO.getInstance().MIMETYPE_TEXT_XML, bin,
				// callerUI);
				// callerUI.setTitle(Properties.getInstance().info_processing);
//				Log.d("ServiceRequestJob", "postResourceOverHTTP");
				// response =
				// CCMIDlet.instance.xmlUtils.getObjectFromXML(isr = new
				// InputStreamReader(din));
				response = (XRes) XMLUtil.getInstance().getObjectFromWBXML(
						(din));
			}
		} catch (IOException t) {
			exception_msg = t.getMessage();
			Log.e("ServiceRequestJob", "request:"+request.toString()+"\t exception:"+t.getLocalizedMessage());
			if (response != null) {
				response.status.cd=0;
				response.status.cdstr=t.getLocalizedMessage();
			}
//			t.printStackTrace();
			t = null;
		} finally {
			if (response == null) {
				response = new XRes();// IDTOFactory.getInstance().getNewXResponse();
				response.id = (0);
				response.op = (request.op);
				response.rslt = (null);// catch npe below in populate try
				response.svc = (request.svc);
				response.sesid = (request.sesid);
				XReqSts rs = new XReqSts();// IDTOFactory.getInstance().getNewRequestStatus();
				rs.cd = (0);
				rs.cdstr = (exception_msg == null ? /*
													 * CCMIDlet.instance.systemPrefs
													 * .get("text.err")
													 */"unknown error"
						: exception_msg);
				response.status = (rs);
			}
			data = null;
			url = null;
			try {
				if (isr != null) {
					isr.close();
				}
				if (din != null) {
					din.close();
				}
				if (bin != null) {
					bin.close();
				}
			} catch (IOException e) {
			}
			isr = null;
			din = null;
			bin = null;

//			if (res != null)
//				res.status = null;
//			res = null;

			// Log.d("ServiceRequestJob",e.getMessage());
		}

		return response;
	}
}
