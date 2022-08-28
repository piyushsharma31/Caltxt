package com.jovistar.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.kxml2.io.KXmlParser;
import org.kxml2.wap.WbxmlSerializer;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.kxml2.wap.WbxmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import com.jovistar.commons.bo.IDTOFactory;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.facade.ModelFacade;

public class XMLUtil {

	private static final String TAG = "XMLUtil";

//  CCMIDlet midlet;
  //to create vector for object appearing multiple times in xml document
  int multipletagobject;
  int multipletagstring;
  String previousObjectName = "";
  String previousStringName = "";

  private static XMLUtil instance;
  public static XMLUtil getInstance(){
  	if(instance==null)
  		instance=new XMLUtil();
  	return instance;
  }
  public XMLUtil() {
//      midlet = mlet;
  }

  public IDTObject getObjectFromWBXML(InputStream wbxmlin) throws IOException {

      XmlPullParser parser = new WbxmlParser();
      Document document = new Document();
      try{
          parser.setInput(wbxmlin, null);
          document.parse(parser);
          wbxmlin.close();
      }catch(Exception e){
      } finally {
          parser = null;
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      XmlSerializer xs = new KXmlSerializer();
      xs.setOutput(out, "UTF-8");
      document.write(xs);
//Log.d(TAG,"getObjectFromWBXML:size"+out.toString().length()+" data:"+out.toString());
      out.close();out = null;
      xs = null;

      multipletagobject = 0;
      previousObjectName = "";
      IDTObject obj = null;
      try{
    	  Element xmlElement = document.getRootElement();
          document = null;
          obj = getObjectFromXMLElement(xmlElement);
      }catch(RuntimeException e) {
      }
      return obj;
  }

  private IDTObject getObjectFromXML(InputStream ins) throws IOException {

      IDTObject obj = null;
      Element objectElement = null;
      XmlPullParser parser = new KXmlParser();
      Document document = new Document();
      try{
          parser.setInput(ins, null);
          document.parse(parser);
          ins.close();
          objectElement = document.getRootElement();
      }catch(Exception e){
      } finally {
          parser = null;
          document = null;
      }

//      XmlSerializer xs = new KXmlSerializer();
  //    xs.setOutput(System.out, "UTF-8");
    //  document.write(xs);
//CCMIDlet.instance.debug("**end**getObjectFromXML:document received*****");

      multipletagobject = 0;
      previousObjectName = "";
      obj = getObjectFromXMLElement(objectElement);
      return obj;
  }

  private IDTObject getObjectFromXMLElement(Element element) {

      /**lest two stringtag with one object sandwich does not result in vector
      **<username>root</username>
      **<result xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xAddressbook">
      **<username>root</username>**/
      previousStringName = "";
      multipletagstring = 0;

      StringBuffer className = null;

      if (element.getAttributeCount() > 0) {
          className = new StringBuffer(element.getAttributeValue(element.getNamespace(), "xsi:type"));
          className.setCharAt(0, Character.toUpperCase(className.charAt(0)));
      } else {
          className = new StringBuffer(element.getName());
      }

      int childCount = element.getChildCount();

      HashMap childs = new HashMap();
      Element child = null;
      String childname = null, childvalue = "";
      int childtype;

      for (int i = 0; i < childCount; i++) {
          child = element.getElement(i);
          childtype = element.getType(i);
          childname = child.getName();

          if (childtype == Node.ELEMENT) {
              // The child is an object <child><obj>obj</obj></child>
              if (child.getAttributeCount()>0 && child.getChildCount()>1
                      && !child.getAttributeValue(child.getNamespace(), "xsi:type").equals("xs:string")) {

                  IDTObject object = getObjectFromXMLElement(child);
                  if (previousObjectName.equals(childname)) {
                      multipletagobject++;
                  } else {
                      multipletagobject = 0;
                  }
                  if (multipletagobject == 1) {
                      ArrayList vobj = new ArrayList();
                      vobj.add(childs.get(childname));
                      vobj.add(object);
                      childs.remove(childname);
                      childs.put(childname, vobj);
                  } else if (multipletagobject > 1) {
                	  ArrayList vobj = (ArrayList) childs.get(childname);
                      vobj.add(object);
                      childs.remove(childname);
                      childs.put(childname, vobj);
                  } else {
                      childs.put(childname, object);
                  }
                  previousObjectName = childname;
              } else /*if(child.getAttributeCount()==0)*/ {//everything else is text/string
                  //The childcnt is 2 for <child>val1 val2<\child>,
                  //1 for <child>val</child>, 0 for <child/>, <child></child>
                  childvalue = "";
                  for(int j=0,sz=child.getChildCount();j<sz;j++)
                      childvalue = childvalue+child.getText(j);
                  //childvalue = childvalue.trim();
//Log.d(TAG,"getObjectFromXMLElement:STRING:"+childname+"="+childvalue);
                  if (previousStringName.equals(childname)) {
                      multipletagstring++;
                  } else {
                      multipletagstring = 0;
                  }
                  if (multipletagstring == 1) {
                	  ArrayList vobj = new ArrayList();
                      vobj.add(childs.get(childname));
                      vobj.add(childvalue);
                      childs.remove(childname);
                      childs.put(childname, vobj);
                  } else if (multipletagstring > 1) {
                	  ArrayList vobj = (ArrayList) childs.get(childname);
                      vobj.add(childvalue);
                      childs.remove(childname);
                      childs.put(childname, vobj);
                  } else {
                      childs.put(childname, childvalue);// == null ? "" : childvalue);
                  }
                  previousStringName = childname;
              }
          } else if (childtype == Node.TEXT) {
//          	Log.d(TAG,"childtype:TEXT");
          } else if (childtype == Node.COMMENT) {
//          	Log.d(TAG,"childtype:COMMENT");
          } else if (childtype == Node.PROCESSING_INSTRUCTION) {
//          	Log.d(TAG,"childtype:PROCESSING_INSTRUCTION");
          } else {
//          	Log.d(TAG,"childtype:UNKNOWN");
          }
      }

      // To create the classname specified object
      IDTObject obj = null;
      try {
          obj = IDTOFactory.getInstance().getInstance(className.toString(), ModelFacade.getInstance().getThisUsername());
          obj.populateFields(childs);
//          Log.d(TAG,"getObjectFromXMLElement:className:" + className+", obj:"+obj);
      } finally {
          childs.clear();
          childs = null;
          className = null;
      }

      return obj;
  }

  private StringBuffer getXMLFromObject2(IDTObject dto, int level) throws IOException {
      String className = dto.getCName();

      //create sufficient size of buffer
      StringBuffer objectToXML = new StringBuffer(100);
      // If it is an document beginning, show the xml header
      if (level == 0) {
          objectToXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");// Show objects data
          objectToXML.append(Strings.getInstance().CH_OPENTAG);
          objectToXML.append(className);
          objectToXML.append(Strings.getInstance().CH_CLOSETAG);
      }

      HashMap fields = dto.extractFields();
//      Enumeration e = fields.keys();
  	Iterator e = fields.keySet().iterator();

      while (e.hasNext()) {
      	Object key = e.next();
          Object value = fields.get(key);

          if (value instanceof IDTObject) {
              String cname = ((IDTObject) value).getCName();
              cname = Character.toLowerCase(cname.charAt(0)) + cname.substring(1);

              objectToXML.append(Strings.getInstance().CH_OPENTAG);
              objectToXML.append(key);
              objectToXML.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"");
              objectToXML.append(cname);
              objectToXML.append(Strings.getInstance().STR_CLOSETAG);

              objectToXML.append(getXMLFromObject2((IDTObject) value, level + 1));
          } else if (value instanceof ArrayList) {
              int size = ((ArrayList) value).size();

              for (int i = 0; i < size; i++) {
                  Object element = ((ArrayList) value).get(i);

                  if (element instanceof IDTObject) {
                      String cname = ((IDTObject) element).getCName();
                      cname = Character.toLowerCase(cname.charAt(0)) + cname.substring(1);

                      objectToXML.append(Strings.getInstance().CH_OPENTAG);
                      objectToXML.append(key);
                      objectToXML.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"");
                      objectToXML.append(cname);
                      objectToXML.append(Strings.getInstance().STR_CLOSETAG);
                      objectToXML.append(getXMLFromObject2((IDTObject) element, level + 1));
                  } else {
                      objectToXML.append(Strings.getInstance().CH_OPENTAG);
                      objectToXML.append(key);
                      objectToXML.append(Strings.getInstance().CH_CLOSETAG);
                      objectToXML.append(element);
                  }

                  if (i < size - 1) {
                      objectToXML.append(Strings.getInstance().STR_OPENTAG);
                      objectToXML.append(key);
                      objectToXML.append(Strings.getInstance().CH_CLOSETAG);
                  }
              }
          } else {
              objectToXML.append(Strings.getInstance().CH_OPENTAG);
              objectToXML.append(key);
              objectToXML.append(Strings.getInstance().CH_CLOSETAG);
              objectToXML.append(value);
          }

          objectToXML.append(Strings.getInstance().STR_OPENTAG);
          objectToXML.append(key);
          objectToXML.append(Strings.getInstance().CH_CLOSETAG);
      }

      if (level == 0) {
          objectToXML.append(Strings.getInstance().STR_OPENTAG);
          objectToXML.append(className);
          objectToXML.append(Strings.getInstance().CH_CLOSETAG);
      }

      fields.clear();
      fields = null;

//Log.d(TAG, "getXMLFromObject2 xml data:"+objectToXML.toString());
      //return removeAmpersand(objectToXML);
      return (objectToXML);
  }

  public byte[] getWBXMLFromObject2(IDTObject dto, int level) throws IOException {
      String sb = removeAmpersand(getXMLFromObject2(dto, level));
//Log.d(TAG,"XMLUtil:getWBXMLFromObject2 xml size" + sb.toString().length());
//Log.d(TAG,"XMLUtil:getWBXMLFromObject2 xml data" + sb.toString());
      return getWBXMLStream(sb.getBytes(StandardCharsets.UTF_8));
  }

  private String removeAmpersand(StringBuffer sb) {
      //char c = '&';
      int len = sb.length();
      for (int i = 0; i < len; i++) {
          if (sb.charAt(i) == '&') {
              sb.insert(i+1, "amp;");
          } else if (sb.charAt(i) == '\'') {
              sb.setCharAt(i, '&');
              sb.insert(i+1, "apos;");
/*            } else if (sb.charAt(i) == '"') {
              sb.insert(i+1, "&quot;");
          } else if (sb.charAt(i) == '<') {
              sb.insert(i+1, "&lt;");
          } else if (sb.charAt(i) == '>') {
              sb.insert(i+1, "&gt;");
              //sb.setCharAt(i, ' ');*/
          }
      }

      //also removing non-ascii char
      return (sb.toString().replaceAll("[^\\x20-\\x7e]", ""));
//      return sb;
  }
/*
  //convert incoming wbxml byte array to xml byte array
  private byte[] getXMLStream(byte[] wbxmlData) {
      byte[] xmlData = null;
      try {
          // Construct an InputStream on byte[]
          // to be used by WbxmlParser.
          ByteArrayInputStream in = new ByteArrayInputStream(wbxmlData);
          XmlPullParser parser = new WbxmlParser();
          parser.setInput(in, "UTF-8");
          //AbstractXmlParser parser = new WbxmlParser(in);

          // Construct a DOM Document to parse WBXML.
          Document document = new Document();
          document.parse(parser);
          in.close();

              // Make a Writer on which XmlWriter can write.
          //CharArrayWriter out = new CharArrayWriter();
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          //AbstractXmlWriter writer = new XmlWriter(out);
          //OutputStreamWriter writer = new OutputStreamWriter(out);
          XmlSerializer xs = new KXmlSerializer();
          xs.setOutput(out, "UTF-8");
          document.write(xs);
          //document.write(new XmlWriter(new OutputStreamWriter(System.out)));
          //document.write(writer);
          //writer.close();

          // Get  XML byte[] from CharArrayWriter filled by XmlWriter.
          xmlData = out.toString().getBytes();
          out.close();

      }//try
      catch (Exception e) {
//          e.printStackTrace();
      }
      return xmlData;
  }//getXMLStream
*/
  //convert incoming xml byte array to wbxml byte array
  private byte[] getWBXMLStream(byte[] xmlData) {
      ByteArrayInputStream in = null;
      XmlPullParser parser = null;
      XmlSerializer xs = null;
      Document document = null;
      ByteArrayOutputStream out = null;
      try {
          in = new ByteArrayInputStream((xmlData));

          parser = new KXmlParser();
          parser.setInput(in, "UTF-8");

          document = new Document();
          document.parse(parser);

          out = new ByteArrayOutputStream();
          xs = new WbxmlSerializer();
          xs.setOutput(out, null);
          document.write(xs);

          //xmlData = out.toString().getBytes();
          xmlData = out.toByteArray();
      } catch (IOException ioe) {
      } catch (XmlPullParserException e) {
//          e.printStackTrace();
      } finally {
          try{
              if(in!=null)
                  in.close();
              if(out!=null)
                  out.close();
              in = null;
              out = null;
          }catch(IOException e){}
          parser = null;
          xs =  null;
          document = null;
      }

      return xmlData;
  }
}
