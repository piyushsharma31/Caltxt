package com.jovistar.caltxt.firebase.client;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jovistar.caltxt.activity.CaltxtPager;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.voice.CaltxtHandler;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;

/**
 * Created by jovika on 11/21/2017.
 */

public class DatabaseFirebase {
    private static final String TAG = "DatabaseFirebase";

    public static void checkAndSyncAddressbook() {

        ArrayList<String> contacts = Addressbook.getPhoneAddressbook();
        // store the key(mobile number), value(token) pair in real time DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.caltxt_users_firebase);

        for (int i = 0; i < contacts.size(); i++) {
            final String contact = XMob.toFQMN(contacts.get(i), Addressbook.getMyCountryCode());
            try {
                Float.parseFloat(contact);
            } catch (Exception e) {
                continue;
            }

            final Query query = ref.child(contact);
            // User data change listener
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    CaltxtPager.OP_GETALL_SVC_CALTXT_USER_COMPLETE = true;

                    query.removeEventListener(this);
                    String value = dataSnapshot.getValue(String.class);

                    // Check for null, or self
                    if (value == null || Addressbook.isItMe(contact)) {
//                        Log.e(TAG, "checkAndSyncAddressbook, Not found " + contact);
                        return;
                    }

                    Log.d(TAG, "checkAndSyncAddressbook, Found! " + contact);
                    XMob mob = Addressbook.getInstance(Caltxt.getCustomAppContext()).getContact(contact);
                    if (mob != null) {
                        mob.setStatus(XMob.STATUS_OFFLINE);
                        Addressbook.getInstance(Caltxt.getCustomAppContext()).update(mob);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.e(TAG, "checkAndSyncAddressbook, Failed to read user " + error.toException());
                }
            });
        }

        Log.d(TAG, "checkAndSyncAddressbook, Registered contacts size  " + Addressbook.getInstance(Caltxt.getCustomAppContext()).getCaltxtContactsCount());
        Connection.get().addAction(Constants.firebaseAddressbookSyncCompleteProperty, contacts, contacts);
    }

    public static void getNextRegisteredContacts(String lastContact) {
        // store the key(mobile number), value(token) pair in real time DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.caltxt_users_firebase);

        final Query query = ref.orderByKey().startAt(lastContact == null ? "0" : lastContact)
                .limitToFirst(Constants.MAX_SEARCH_RESULT_SIZE);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post
                    CaltxtPager.OP_GETALL_SVC_CALTXT_USER_COMPLETE = true;

                    String value = postSnapshot.getKey();

                    // Check for null
                    if (value == null) {
//                    Log.e(TAG, "getNextRegisteredContacts, Null found " + value);
                        continue;
                    }

                    Log.d(TAG, "getNextRegisteredContacts, Registered contact " + value);
                    Connection.get().addAction(Constants.usersSearchResultProperty, value, value);
                }
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
        // User data change listener
        /*query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CaltxtPager.OP_GETALL_SVC_CALTXT_USER_COMPLETE = true;

                query.removeEventListener(this);
                String value = dataSnapshot.getKey();

                // Check for null
                if (value == null) {
//                    Log.e(TAG, "getNextRegisteredContacts, Null found " + value);
                    return;
                }

                Log.d(TAG, "getNextRegisteredContacts, Registered contact " + value);
                Connection.get().addAction(Constants.usersSearchResultProperty, value, value);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "getNextRegisteredContacts, Failed to read user " + error.toException());
            }
        });*/
    }

    public static void getMatchingContactsByValue(String match) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.caltxt_users_firebase);
//        final Query query = null;

        // User data change listener
        String match_start = match + "000000000000".substring(match.length());
        String match_end = match + "999999999999".substring(match.length());
//        Log.d(TAG, "getMatchingContactsByValue, match_start " + match_start);
//        Log.d(TAG, "getMatchingContactsByValue, match_end " + match_end);
        final Query query = ref.orderByKey().startAt(match_start).endAt(match_end);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post
                    CaltxtPager.OP_GETALL_SVC_CALTXT_USER_COMPLETE = true;

                    String value = postSnapshot.getKey();

                    // Check for null
                    if (value == null) {
//                    Log.e(TAG, "getMatchingContactsByValue, onChildAdded Null found " + value);
                        continue;
                    }

                    Log.d(TAG, "getMatchingContactsByValue, onChildAdded value " + value);
                    Connection.get().addAction(Constants.usersMatchResultProperty, value, value);
                }
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        /*query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CaltxtPager.OP_GETALL_SVC_CALTXT_USER_COMPLETE = true;
                query.removeEventListener(this);

                String value = dataSnapshot.getKey();

                // Check for null
                if (value == null) {
//                    Log.e(TAG, "getMatchingContactsByValue, onChildAdded Null found " + value);
                    return;
                }

                Log.d(TAG, "getMatchingContactsByValue, onChildAdded value " + value);
                Connection.get().addAction(Constants.usersMatchResultProperty, value, value);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                String value = dataSnapshot.getKey();
                Log.d(TAG, "getMatchingContactsByValue, onChildChanged value " + value);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "getMatchingContactsByValue, Failed to read user " + error.toException());
            }
        });*/
    }

    public static void getCaltxtFAQ() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.caltxt_faq_firebase);
        final Query query = ref.orderByKey().startAt("0");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post
                    String key = postSnapshot.getKey();
                    String value = (String) postSnapshot.getValue();

                    // Check for null
                    if (key == null) {
                        continue;
                    }

                    Log.d(TAG, "getCaltxtFAQ, FAQ " + key + ", " + value);
                    Connection.get().addAction(Constants.caltxtFAQProperty, key, value);
                }
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        // User data change listener
        /*query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String key = dataSnapshot.getKey();
                String value = (String) dataSnapshot.getValue();

                // Check for null
                if (key == null) {
                    return;
                }

                Log.d(TAG, "getCaltxtFAQ, FAQ " + key + ", " + value);
                Connection.get().addAction(Constants.caltxtFAQProperty, key, value);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "getCaltxtFAQ, Failed to read FAQ " + error.toException());
            }
        });*/
    }

    public static void sendCaltxtFeedback(final String feedbak) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.caltxt_admin_firebase);

        // User data change listener
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Long value = (Long) dataSnapshot.getValue();

                Log.d(TAG, "getCaltxtAdmin, Admin " + key + ", " + value);
                // Check for null
                if (key == null) {
                    return;
                }

                XMob mob = new XMob();
                mob.setUsername(XMob.toFQMN(value.toString(), Addressbook.getMyCountryCode()));
                mob.setStatusOffline();
                CaltxtHandler.get(Caltxt.getCustomAppContext()).initiateMessage(mob, feedbak, "", XCtx.PRIORITY_NORMAL);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "getCaltxtAdmin, Failed to read Admin user id " + error.toException());
            }
        });
    }
}
