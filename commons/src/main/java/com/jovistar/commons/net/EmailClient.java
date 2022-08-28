package com.jovistar.commons.net;
/*
import javax.microedition.io.SocketConnection;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Date;

public class EmailClient implements Runnable {
    CCMIDlet midlet;
   //private EmailMIDlet parent;
   private Display display;
   private Form f;
   private StringItem si;
   private SocketConnection sc;
   private InputStream is;
   private OutputStream os;
   private String smtpServerAddress;

   private String from, to, subject, msg;

   public EmailClient (CCMIDlet mlt, String from, String to, String subject, String msg) {
      //parent = m;
      this.from = from;
      this.to = to;
      this.subject = subject;
      this.msg = msg;

      //display = Display.getDisplay(parent);
      f = new Form("Email Client");
      si = new StringItem("Response:" , " ");
      f.append(si);
      midlet = mlt;
      midlet.displayMgr.pushDisplayable(f);
   }

   public void start() {
       try{
           CCMIDlet.instance.jobrunner.run(this);
       }catch(CCMException e){
           CCMIDlet.instance.displayFactory.getProgressUI().showWarning(e.getMessage());
       }
   }

   public void run() {
 
      try {
         sc = (SocketConnection)
            Connector.open("socket://"+smtpServerAddress+":25");
         is = sc.openInputStream();
         os = sc.openOutputStream();

         os.write(("HELO there" + "\r\n").getBytes()); 
         os.write(("MAIL FROM: "+ from +"\r\n").getBytes());
         os.write(("RCPT TO: "+ to + "\r\n").getBytes());
         os.write("DATA\r\n".getBytes());
         // stamp the msg with date
         os.write(("Date: " + new Date() + "\r\n").getBytes()); 
         os.write(("From: "+from+"\r\n").getBytes());
         os.write(("To: "+to+"\r\n").getBytes());
         os.write(("Subject: "+subject+"\r\n").getBytes());
         os.write((msg+"\r\n").getBytes()); // message body
         os.write(".\r\n".getBytes());
         os.write("QUIT\r\n".getBytes());

         // debug
         StringBuffer sb = new StringBuffer();
         int c = 0;
         while (((c = is.read()) != -1) ) {
            sb.append((char) c);
         }
         si.setText("SMTP server response - " + sb.toString());      

      } catch(IOException e) {

         Alert a = new Alert ("TimeClient", "Cannot connect to SMTP server. Ping the server" + 
              "to make sure it is running...", null, AlertType.ERROR);
         a.setTimeout(Alert.FOREVER);
         midlet.displayMgr.pushDisplayable(a);
      } finally {
         try {
            if(is != null) {
               is.close();
            }
            if(os != null) {
               os.close();
            }
            if(sc != null) {
               sc.close();
            }
         } catch(IOException e) {
//            e.printStackTrace();
         }
      }    
   } 

   public void commandAction(Command c, Displayable s) {
      if (c == Alert.DISMISS_COMMAND) {
      }
   }
}
*/