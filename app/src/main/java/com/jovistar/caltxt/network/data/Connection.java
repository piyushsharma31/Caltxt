package com.jovistar.caltxt.network.data;

import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.firebase.client.ConnectionFirebase;
import com.jovistar.caltxt.mqtt.client.ConnectionMqtt;
import com.jovistar.caltxt.phone.Addressbook;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by jovika on 10/22/2017.
 */

public class Connection {

    private static Connection con = null;
    long lastFirebasePing = 0;

    public static Connection get() {
        if (con == null) {
            con = new Connection();
        }
        return con;
    }

    ConnectionFirebase connectionFirebase = new ConnectionFirebase();
    ConnectionMqtt connectionMqtt = ConnectionMqtt.getConnection(Caltxt.getCustomAppContext());

    /**
     * Collection of {@link PropertyChangeListener}
     **/
    private ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    // added by Piyush (START) to notify Activities subscribing to
    // PropertyChangeListener of message received, sent
    public void addAction(String property, Object ovalue, Object nvalue) {
        notifyListeners(new PropertyChangeEvent(this, property, ovalue, nvalue));
    }

    /**
     * Notify {@link PropertyChangeListener} objects that the object has been
     * updated
     *
     * @param propertyChangeEvent
     */
    private void notifyListeners(PropertyChangeEvent propertyChangeEvent) {

        for (Iterator<PropertyChangeListener> it = listeners.iterator(); it.hasNext(); ) {
            PropertyChangeListener listener = it.next();
            listener.propertyChange(propertyChangeEvent);
        }

    }

    /**
     * Register a {@link PropertyChangeListener} to this object
     *
     * @param listener the listener to register
     */
    public void registerChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    /**
     * Remove a registered {@link PropertyChangeListener}
     *
     * @param listener A reference to the listener to remove
     */
    public void removeChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void submitPingSelfFirebase() {
        if (SignupProfile.isNumberVerifiedUserAdded(Caltxt.getCustomAppContext())) {
            if(lastFirebasePing<= Constants.firebase_defaultPingInterval) {
                // keep successive self pings 30 seconds apart
                connectionFirebase.submitPingContact(Addressbook.getMyProfile().getUsername(), Addressbook.getMyProfile().getUsername());
                lastFirebasePing = Calendar.getInstance().getTimeInMillis();
            }
        }
    }

    public void submitPingContact(String from, String contact) {
        if (Addressbook.getInstance(Caltxt.getCustomAppContext()).isConnected(contact) && connectionMqtt.isConnected()) {
            connectionMqtt.submitPingContact(from, contact);
        } else {
            connectionFirebase.submitPingContact(from, contact);
        }
    }

    public void submitAckPingContact(String from, String contact) {
        if (Addressbook.getInstance(Caltxt.getCustomAppContext()).isConnected(contact) && connectionMqtt.isConnected()) {
            connectionMqtt.submitAckPingContact(from, contact);
        } else {
            connectionFirebase.submitAckPingContact(from, contact);
        }
    }

    public void submitForcedPingContact(String from, String contact) {
        if (Addressbook.getInstance(Caltxt.getCustomAppContext()).isConnected(contact) && connectionMqtt.isConnected()) {
            connectionMqtt.submitForcedPingContact(from, contact);
        } else {
            connectionFirebase.submitForcedPingContact(from, contact);
        }
    }

    public void publishXCtx(String to, String message, long pid) {
        // commented 7-dec-2018, always send via Firebase
        //if (connectionMqtt.isConnected()) {
//            connectionMqtt.publishXCtx(to, message, pid);
//        } else {
            ConnectionFirebase.publishXCtx(to, message, pid);
//        }
    }

    public boolean isConnected() {
        return (connectionMqtt!=null && connectionMqtt.isConnected()) || ConnectionFirebase.isConnected();
    }

}
