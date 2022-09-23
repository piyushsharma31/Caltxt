package com.jovistar.caltxt.network.voice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.CaltxtToast;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.firebase.client.FirebaseReceiver;
import com.jovistar.caltxt.mqtt.client.ConnectionMqtt;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Blockbook;
import com.jovistar.caltxt.phone.Logbook;
import com.jovistar.commons.bo.XMob;

import java.util.Calendar;
import java.util.Date;

/* Each caltxt which comes before or after the call ends is consumed when
 * call ends.
 * |---incoming or outgoing call--->|---caltxt message---|---call ends--->|
 */
public class CallHandler extends CallReceiver {
    private static final String TAG = "CallHandler";

    /*
        private void startCaltxtInputActivity(Context context, XMob mob) {
            Log.v(TAG, "startCaltxtInputActivity");
            Intent caltxtInput = new Intent(context, CaltxtInputActivity.class);
            caltxtInput.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            caltxtInput.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);//commented 10-DEC-16. why need?
            caltxtInput.putExtra("IDTOBJECT",mob);
    //		caltxtInput.putExtra("VIEW",Constants.INPUT_VIEW);
            context.startActivity(caltxtInput);
        }
    */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d(TAG, "onReceive intent:" + intent.getAction().toString());
        //process only if user is registered for service
        if (!SignupProfile.isSIM1Verified(context.getApplicationContext())
                && !SignupProfile.isSIM2Verified(context.getApplicationContext())) {
            return;
        }

        ConnectionMqtt.getConnection(context.getApplicationContext());//initialize MQTT connection if not connected

        CallManager callManager = CallManager.getInstance();

//		CallHandler.get(context.getApplicationContext());
/* 28APR17: moved CallManager.callControllerInit/listen to Caltxt.java since it was not initializing unless call is made/received
        if(!INITIALIZED) {
			callManager.callControllerInit(context.getApplicationContext());
			callManager.listen(context.getApplicationContext());
			Persistence.getInstance(context.getApplicationContext()).getAllXPLC();
			INITIALIZED = true;
//			Log.d("SettingsFragment", "CallReceiver:INITIALIZED onReceive");
		} else {
//			Log.d("SettingsFragment", "CallReceiver:INITIALIZED NOT onReceive");
		}
*/
//		Call call = null;
//		int state = 0;
        // We listen to two intents. The new outgoing call only tells us of an
        // outgoing call. We use it to get the number.
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            // 25112019, this condition ACTION_NEW_OUTGOING_CALL will not be called since PROCESS_OUTGOING_CALLS
            // permission is commented in manifest file according to new Google Policy Change, therefore
            // pushCall is called in PhoneStateReceiver::CALL_STATE_OFFHOOK
            // Try to read the phone number from previous receivers.
            String number = getResultData();
            if (number == null) {
                // We could not find any previous data. Use the original phone number in this case.
                number = intent.getExtras().getString(android.content.Intent.EXTRA_PHONE_NUMBER);
            }
            Log.d(TAG, "onReceive ACTION_NEW_OUTGOING_CALL:" + number);
//			callManager.outgoingCalls.add(call=new Call(number, Calendar.getInstance().getTimeInMillis()));
            callManager.pushCall(Constants.CALL_TYPE_OUTBOUND, number);
//			String fqmn = XMob.toFQMN(number, Addressbook.getMyCountryCode());
//			if (CaltxtHandler.get(context.getApplicationContext()).peekCaltxt(Constants.CALL_TYPE_OUTBOUND, fqmn)!=null) {
            // is a caltxt call
//				Log.d(TAG, "onReceive:ACTION_NEW_OUTGOING_CALL:CALL_TYPE_OUTBOUND"+number);
//				setResultData(number);//since phoneNumber is XMob.username
//			} else if(number.startsWith("*")//command code
//					|| number.length() < 10) {//special service numbers
//				Log.d(TAG, "onReceive:ACTION_NEW_OUTGOING_CALL:*"+call.number);
//				setResultData(number);
//			} else {// is not a caltxt call, SUPPRESS call and invoke caltxt input dialog
            /* To enable Android dialer to catch Context Call, uncomment below code
             * in this 'else', and comment below setResultData
             */
//				Log.d(TAG, "onReceive:ACTION_NEW_OUTGOING_CALL:SUPPRESS"+call.number);
            /*
             * remove the call object just added above;
             * it will be added once flow come here from CaltxtInput activity
             */

            /**
             * DO NOT change the standard call experience of the user.
             * Instead, show him a dialog suggesting to use Caltxt dialer.
             * 09-MAY-17
             if(Addressbook.getInstance(context).isRegistered(call.number)) {
             // 17MAY17, send missed call Caltxt atleast
             XMob mob = Addressbook.getInstance(context.getApplicationContext()).getRegistered(fqmn);
             // initiateContextCall will send caltxt and let call go through
             // initiateNormalCall will not be called through initiateContextCall
             // since CaltxtInputActivity is not active
             final XCtx ctx = new XCtx();
             ctx.setCallerSelf(Addressbook.getInstance(context).getMyProfile(), Addressbook.getInstance(context).getMyCountryCode());// set myself as caller
             ctx.setNameCallee(mob.getName());
             ctx.setNumberCallee(mob.getNumber());
             ctx.setCallPriority(XCtx.PRIORITY_NORMAL);
             ctx.setRecvToD(System.currentTimeMillis());//set call recieve / initiate time to NOW (as initiated)
             ctx.setStartToD(System.currentTimeMillis());//set call start time to NOW
             ctx.setCaltxt("");
             ctx.setCallState(XCtx.OUT_CALL);
             ctx.setCaltxtEtc(null);

             CaltxtHandler.get(context).pushCaltxt(Constants.CALL_TYPE_OUTBOUND, mob.getUsername(), ctx);

             CaltxtHandler.get(context).sendXCtx(mob.getUsername(), ctx);
             //					CaltxtHandler.get(context).initiateContextCall(
             //							mob, "", null, XCtx.PRIORITY_NORMAL);
             //					Toast.makeText(context, "You may use Caltxt to make your calls smarter!", Toast.LENGTH_LONG).show();
             }
             */
/*				callManager.outgoingCalls.remove(call);
				setResultData(null);// Canceling call operation; CALTXT CALL
				abortBroadcast();

				XMob mob = Addressbook.getInstance(context.getApplicationContext()).getRegistered(fqmn);
				if(mob==null) {
					mob = new XMob();
					mob.setName(fqmn);
					mob.setUsername(fqmn);
					mob.setNumber(number);
				}

				// check if PhoneState is IDLE yet or not
				// in case not IDLE, Activity will not be created by system (Redmi), so wait for state to be IDLE
				// in case IDLE, start CaltxtInputActivity
				if(callManager.isAnyCallInProgress()) {
					WAIT_FOR_IDLE_STATE_AND_START = true;
					mobile_to_call = mob;
//					Intent intent2 = new Intent(FirebaseReceiver.CALL_MOBILE);
//					intent2.putExtra("IDTOBJECT", mob);
//				intent2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//					context.sendBroadcast(intent2);
				} else {// Phone state is IDLE
					// show caltxt input activity
					if (Build.VERSION.SDK_INT >= 23) {
						WAIT_FOR_IDLE_STATE_AND_START = true;
						mobile_to_call = mob;
					} else {
						startCaltxtInputActivity(context.getApplicationContext(), mob);
					}
				}
//					Intent broadcastIntent = new Intent("com.jovistar.caltxt.message.ShowCaltxtInputScreen");
//					broadcastIntent.putExtra("XMOB", mob);
//					context.sendBroadcast(broadcastIntent);
				Log.d(TAG, "onReceive stateStr:"+callManager.stateStr+" lastStateStr:"+
						callManager.lastStateStr+" lastLastStateStr:"+callManager.lastLastStateStr);

				return;*/
//				}
//			}

            /***************************
             * OUTGOING CALL INITIATED *
             ***************************/
//			onOutgoingCallStarted(context.getApplicationContext(), call.number, new Date(call.time));
            Log.d(TAG, "onReceive stateStr:" + CallManager.stateStr + " lastStateStr:" +
                    CallManager.lastStateStr + " lastLastStateStr:" + CallManager.lastLastStateStr
                    + ", outgoing calls " + CallManager.getInstance().getOutgoingCallsInProgress());
        } else if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                && !(intent.getExtras().getString(TelephonyManager.EXTRA_STATE).equals(CallManager.stateStr))) {

            CallManager.lastLastStateStr = CallManager.lastStateStr;
            CallManager.lastStateStr = CallManager.stateStr;
            CallManager.stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);

            Log.d(TAG, "onReceive ACTION_PHONE_STATE_CHANGED stateStr:" + CallManager.stateStr + " lastStateStr:" +
                    CallManager.lastStateStr + " lastLastStateStr:" + CallManager.lastLastStateStr
                    + ", incoming calls " + CallManager.getInstance().getIncomingCallsInProgress()
                    + ", outgoing calls " + CallManager.getInstance().getOutgoingCallsInProgress());

            if (CallManager.stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                String number1 = null;

                if (CallManager.lastStateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    Object[] calls = CallManager.getInstance().getIncomingCallsInProgress().keySet().toArray();
                    if (calls.length > 0) {
                        long timeinms = CallManager.getInstance().peekCall(Constants.CALL_TYPE_INBOUND, (String) calls[0]);
                        number1 = (String) calls[0];
                        // get the latest call arrived
                        for (int i = 0; i < calls.length; i++) {
                            long timeinms1 = CallManager.getInstance().peekCall(Constants.CALL_TYPE_INBOUND, (String) calls[i]);
                            if (timeinms1 > timeinms) {
                                timeinms = timeinms1;
                                number1 = (String) calls[i];
                            }
                        }

                        timeinms = callManager.popCall(Constants.CALL_TYPE_INBOUND, number1);//reject or missed incoming call

                        /*****************************************
                         * INCOMING CALL DISCONNECTED OR MISSED *
                         ****************************************/
                        onMissedCall(context.getApplicationContext(), number1, new Date(timeinms));
                    }
                } else if (CallManager.lastStateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

                    if (CallManager.lastLastStateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        // RINGING --> OFFHOOK --> IDLE (incoming calls scenario)
                        // full scenario could be OFFHOOK --> RING --> OFFHOOK --> IDLE
                        // OR IDLE --> RING --> OFFHOOK --> IDLE
                        // therefore recurse through all incoming and outgoing calls array

                        Object[] incalls = CallManager.getInstance().getIncomingCallsInProgress().keySet().toArray();
                        if (incalls.length > 0) {
                            long timeinms = CallManager.getInstance().peekCall(Constants.CALL_TYPE_INBOUND, (String) incalls[0]);
                            number1 = (String) incalls[0];

                            for (int i = 0; i < incalls.length; i++) {
                                number1 = (String) incalls[i];
                                timeinms = callManager.popCall(Constants.CALL_TYPE_INBOUND, number1);

                                /***********************
                                 * INCOMING CALL ENDS *
                                 **********************/
                                if(timeinms>0) {
                                    onIncomingCallEnded(context.getApplicationContext(), number1, new Date(timeinms), new Date());
                                }

/*                                long timeinms1 = CallManager.getInstance().peekCall(Constants.CALL_TYPE_INBOUND, (String) incalls[i]);
                                if (timeinms1 < timeinms) {
                                    timeinms = timeinms1;
                                    number1 = (String) incalls[i];
                                }*/
                            }

//                            timeinms = callManager.popCall(Constants.CALL_TYPE_INBOUND, number1);

                            /***********************
                             * INCOMING CALL ENDS *
                             **********************/
//                            onIncomingCallEnded(context.getApplicationContext(), number1, new Date(timeinms), new Date());
                        }

                        Object[] outcalls = CallManager.getInstance().getIncomingCallsInProgress().keySet().toArray();
                        if (outcalls.length > 0) {
                            long timeinms = CallManager.getInstance().peekCall(Constants.CALL_TYPE_OUTBOUND, (String) outcalls[0]);
                            number1 = (String) outcalls[0];

                            for (int i = 0; i < outcalls.length; i++) {
                                number1 = (String) outcalls[i];
                                timeinms = callManager.popCall(Constants.CALL_TYPE_OUTBOUND, number1);

                                /***********************
                                 * OUTGOING CALL ENDS *
                                 **********************/
                                if (timeinms > 0) {
                                    onOutgoingCallEnded(context.getApplicationContext(), number1, new Date(timeinms), new Date());
                                }
                            }
                        }
                    } else if (CallManager.lastLastStateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        // IDLE --> OFFHOOK --> IDLE (outgoing call(s) scenario)

                        Object[] calls = CallManager.getInstance().getOutgoingCallsInProgress().keySet().toArray();

                        if (calls.length > 0) {
                            long timeinms = CallManager.getInstance().peekCall(Constants.CALL_TYPE_OUTBOUND, (String) calls[0]);
                            number1 = (String) calls[0];
                            // get the first call made
                            for (int i = 0; i < calls.length; i++) {
                                number1 = (String) calls[i];
                                timeinms = callManager.popCall(Constants.CALL_TYPE_OUTBOUND, number1);
                                /**************************
                                 * ALL OUTGOING CALL ENDS *
                                 **************************/
                                if(timeinms>0) {
                                    onOutgoingCallEnded(context.getApplicationContext(), number1, new Date(timeinms), new Date());
                                }

/*                                long timeinms1 = CallManager.getInstance().peekCall(Constants.CALL_TYPE_OUTBOUND, (String) calls[i]);
                                if (timeinms1 < timeinms) {
                                    timeinms = timeinms1;
                                    number1 = (String) calls[i];
                                }*/
                            }

//                            if (callManager.peekCall(Constants.CALL_TYPE_OUTBOUND, number1) > 0) {
//                                timeinms = callManager.popCall(Constants.CALL_TYPE_OUTBOUND, number1);
                                /***********************
                                 * OUTGOING CALL ENDS *
                                 **********************/
//                                onOutgoingCallEnded(context.getApplicationContext(), number1, new Date(timeinms), new Date());
//                            }
                        }
                    }
                }
                // if WAIT_FOR_IDLE_STATE_AND_START = true, start CaltxtInputActivity
                if (Caltxt.WAIT_FOR_IDLE_STATE_AND_START) {
                    Caltxt.WAIT_FOR_IDLE_STATE_AND_START = false;
                    Intent intent2 = new Intent(FirebaseReceiver.CALL_MOBILE);
                    intent2.putExtra("IDTOBJECT", Caltxt.mobile_to_call);
                    intent2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    context.sendBroadcast(intent2);
                }
            } else if (CallManager.stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                String number1 = null;

                if (CallManager.lastStateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    Object[] calls = CallManager.getInstance().getIncomingCallsInProgress().keySet().toArray();
                    if (calls.length > 0) {
                        long timeinms = CallManager.getInstance().peekCall(Constants.CALL_TYPE_INBOUND, (String) calls[0]);
                        number1 = (String) calls[0];
                        // get the latest call arrived
                        for (int i = 0; i < calls.length; i++) {
                            long timeinms1 = CallManager.getInstance().peekCall(Constants.CALL_TYPE_INBOUND, (String) calls[i]);
                            if (timeinms1 > timeinms) {
                                timeinms = timeinms1;
                                number1 = (String) calls[i];
                            }
                        }

                        timeinms = callManager.peekCall(Constants.CALL_TYPE_INBOUND, number1);

                        /**************************
                         * INCOMING CALL ANSWERED *
                         **************************/
                        onIncomingCallAnswered(context.getApplicationContext(), number1, new Date(timeinms));
                    }
                } else if (CallManager.lastStateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {

                    Object[] calls = CallManager.getInstance().getOutgoingCallsInProgress().keySet().toArray();
                    if (calls.length > 0) {
                        long timeinms = CallManager.getInstance().peekCall(Constants.CALL_TYPE_OUTBOUND, (String) calls[0]);
                        number1 = (String) calls[0];
                        // get the latest call made
                        for (int i = 0; i < calls.length; i++) {
                            long timeinms1 = CallManager.getInstance().peekCall(Constants.CALL_TYPE_OUTBOUND, (String) calls[i]);
                            if (timeinms1 > timeinms) {
                                timeinms = timeinms1;
                                number1 = (String) calls[i];
                            }
                        }

                        if (callManager.peekCall(Constants.CALL_TYPE_OUTBOUND, number1) > 0) {
                            timeinms = callManager.peekCall(Constants.CALL_TYPE_OUTBOUND, number1);
                            /***************************
                             * OUTGOING CALL INITIATED *
                             ***************************/
                            onOutgoingCallStarted(context.getApplicationContext(), number1, new Date(timeinms));
                        }
                    }

                }
            } else if (CallManager.stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                long timeinms = callManager.pushCall(Constants.CALL_TYPE_INBOUND, number);

                if (CallManager.lastStateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                } else if(CallManager.lastStateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

                }
                /*****************
                 * INCOMING CALL *
                 ****************/
                onIncomingCallRinging(context.getApplicationContext(), number, new Date(timeinms));
            }

        } else {
            Log.d(TAG, "onReceive else intent:" + intent.getAction().toString());
        }

        /*
         * CALL_STATE_IDLE:0
         * CALL_STATE_RINGING:1
         * CALL_STATE_OFFHOOK:2
         */
        if (CallManager.stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)
                && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {

            CallManager.lastLastStateStr = CallManager.lastStateStr = CallManager.stateStr = TelephonyManager.EXTRA_STATE_IDLE;
            callManager.getIncomingCallsInProgress().clear();
            callManager.getOutgoingCallsInProgress().clear();

            int temporaryStatusHolder = -1;
            try {
                temporaryStatusHolder = Integer.parseInt(SignupProfile.getPreference(context,
                        context.getString(R.string.preference_key_temporary_status)));
            } catch (NumberFormatException e) {

            }

            if (temporaryStatusHolder >= 0) {
                Addressbook.getInstance(context).getMyProfile().setStatus(temporaryStatusHolder);

                SignupProfile.setPreference(context, context.getString(R.string.preference_key_temporary_status), "-1");
            }

            String temporaryHeadlineHolder = SignupProfile.getPreference(context, context.getString(R.string.preference_key_temporary_headline));
            if (temporaryHeadlineHolder.trim().length() > 0) {
                Log.d(TAG, "onReceive temporaryHeadlineHolder " + temporaryHeadlineHolder
                        + ", temporaryStatusHolder " + temporaryStatusHolder);
                Addressbook.getInstance(context).changeMyStatus(temporaryHeadlineHolder,    // change back only status
                        Addressbook.getInstance(context).getMyProfile().getPlace());        // place remains same

                SignupProfile.setPreference(context, context.getString(R.string.preference_key_temporary_headline), "");
            }

            // to reinstate the status in FloatingActionButton in CaltxtPager
            Connection.get().addAction(TelephonyManager.EXTRA_STATE_IDLE, "", "");
        }
    }

    @Override
    protected void onIncomingCallRinging(final Context context, String number, Date start) {
        // change status to BUSY when on call, reset in ON_IDLE
        changeStatusToBusy(context);

        Addressbook addressbook = Addressbook.getInstance(context);

        String fqmn = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        final XCtx ctx = CaltxtHandler.get(context).peekCaltxt(Constants.CALL_TYPE_INBOUND, fqmn);

        if (ctx == null) {// Call-then-Caltxt
            //no caltxt received yet, THIS ISNT CALTXT CALL, create a log entry
            XCtx xctx = new XCtx();
            xctx.setCallState(XCtx.IN_CALL);
            xctx.setUsernameCaller(number);
            xctx.setNameCaller(addressbook.getName(number));
            xctx.setRecvToD(start.getTime());//set call receive time to NOW; initialized in XCtx constructor
            xctx.setNumberCallee(Addressbook.getMyProfile().getUsername());
            xctx.setNameCallee(Addressbook.getMyProfile().getName());
            long pid = CaltxtHandler.get(context).pushCaltxt(Constants.CALL_TYPE_INBOUND, fqmn, xctx);
            xctx.setPersistenceId(pid);
            Log.d(TAG, "onIncomingCallRinging normal call " + xctx.toString());
        } else {// Caltxt already IN, show it!
            //if its a call from dual SIM phone, remove the other number put in inBoundQueue
            if (ctx.getUsername2Caller().length() > 0) {

                if (ctx.getUsername2Caller().endsWith(fqmn)) {
                    //swap the SIM1 number with SIM2 number in XCtx
                    String n1 = ctx.getUsernameCaller();
                    ctx.setUsernameCaller(ctx.getUsername2Caller());
                    ctx.setUsername2Caller(n1);
                    Logbook.get(context).update(ctx);
                } else if (ctx.getUsernameCaller().endsWith(fqmn)) {
                }
            }

            if (ctx.getRecvToD() + Constants.CALTXT_MSG_LAG < System.currentTimeMillis()) {
                //there were missed call; caltxt was initiated by caller and canceled immediately
                ctx.setCallState(XCtx.IN_CALL_MISSED);
                Logbook.get(context).update(ctx);
            } else {
                Toast.makeText(context,
                        ctx.getNameCaller() + " calling"
                                + (ctx.getCaltxt().length() == 0 ? "" : (". \"" + ctx.getCaltxt() + "\"")), Toast.LENGTH_LONG).show();

                CountDownTimer timer = new CountDownTimer(1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {

                        Intent it = new Intent(context, CaltxtToast.class);
                        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        it.putExtra("IDTOBJECT", ctx);
                        it.putExtra("ACTION", Constants.TOAST_ACTION_VIEW);
                        context.startActivity(it);

                    }
                };
                timer.start();
            }
            Log.d(TAG, "onIncomingCallRinging ctx " + ctx.toString());
        }

        Log.d(TAG, "onIncomingCallRinging end");
    }

    @Override
    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        Log.d(TAG, "onIncomingCallAnswered start");

        // Reset the BUSY status in onReceive()
//		Addressbook.getMyProfile().setHeadline(temporaryHeadlineHolder);
//		Addressbook.getMyProfile().setStatus(temporaryStatusHolder);
//        Addressbook addressbook = Addressbook.getInstance(context);

        String fqmn = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        /* persist incoming call in log -START */
        XCtx ctx = CaltxtHandler.get(context).peekCaltxt(Constants.CALL_TYPE_INBOUND, fqmn);
        if (ctx == null) {// Call-then-Caltxt
            // should never come here. Caltxt already added in IncomingCall function
        } else {
            ctx.setStartToD(start.getTime());
            Logbook.get(context).update(ctx);
        }
        Connection.get().addAction(Constants.callIncomingAnsweredProperty, number, "");
        /* persist incoming call in log -END */
        Log.d(TAG, "onIncomingCallAnswered end");
    }

    @Override
    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        Addressbook addressbook = Addressbook.getInstance(context);
        Log.d(TAG, "onIncomingCallEnded start");
        // Reset the BUSY status in onReceive()
//		addressbook.getMyProfile().setHeadline(temporaryHeadlineHolder);
//		addressbook.getMyProfile().setStatus(temporaryStatusHolder);

        /* persist incoming call in log -START */
        XCtx ctx = CaltxtHandler.get(context).popCaltxt(Constants.CALL_TYPE_INBOUND, number);
        if (ctx == null/* || ctx.getContext().length()==0 UN-COMMENT TO STOP LOGGING NORMAL INCOMING CALLS*/) {
            // should never come here. Caltxt already added in IncomingCall function
            // create Caltxt object during IncomingCallRinging
        } else {// Caltxt-then-Call
            ctx.setCallState(XCtx.IN_CALL);
//			ctx.setStartTOD(start.getTime());//****SET IN onIncomingCallAnswered ****
            ctx.setEndToD(end.getTime()/*System.currentTimeMillis()*/);//set call end time to NOW
//			try {
//				Persistence.getInstance(context).update(ctx);
            Logbook.get(context).update(ctx);
//				RebootService.getConnection().addAction(Constants.messageUpdateProperty, ctx.getNumberCaller(), ctx.getSubject());
//			} catch (PersistenceException e) {
//			}
        }
        // hide caltxt toast
//		ActivityCaltxt.closeCaltxt();
/*		if(ActivityCaltxt.global_toast!=null) {
			ActivityCaltxt.global_toast.cancel();
			ActivityCaltxt.global_toast = null;
		}*/
//		inboundQ.clear();
        Connection.get().addAction(Constants.callIncomingEndProperty, ctx.getUsernameCaller(), ctx.getSubject());
//		RebootService.getConnection().addAction(Constants.callComplete, ctx.getNumberCaller(), ctx.getSubject());
        Log.d(TAG, "onIncomingCallEnded end");
    }

    @Override
    protected void onMissedCall(final Context context, String number, Date start) {
        Addressbook addressbook = Addressbook.getInstance(context);
//		Log.d(TAG, "onMissedCall start temporaryHeadlineHolder "+temporaryHeadlineHolder
//			+", temporaryStatusHolder "+temporaryStatusHolder);
        // Reset the BUSY status in onReceive()
//		addressbook.getMyProfile().setHeadline(temporaryHeadlineHolder);
//		addressbook.getMyProfile().setStatus(temporaryStatusHolder);

        boolean isCaltxtCall = false, isBlocked = false;
        //initiate ACK input dialog
        final XCtx ctx = CaltxtHandler.get(context).popCaltxt(Constants.CALL_TYPE_INBOUND, number);
        if (ctx == null) {
            // should never come here. Caltxt already added in IncomingCall function
            //This is NOT Caltxt call
			/*ctx = new XCtx();
			ctx.setUsernameCaller(number);
			ctx.setNameCaller(addressbook.getName(number));
			ctx.setRecvToD(start.getTime());//set call receive time to NOW; initialized in XCtx constructor
			ctx.setNumberCallee(addressbook.getMyProfile().getUsername());
			ctx.setNameCallee(addressbook.getMyProfile().getName());
			long pid = Logbook.get(context).prepend(ctx);
			ctx.setPersistenceId(pid);
			Log.d(TAG, "onMissedCall missed call "+ctx.toString());*/
        } else {
            //This IS Caltxt call
            isCaltxtCall = true;
            Logbook.get(context).update(ctx);
        }

        if (Addressbook.getMyProfile().isDND()) {
            ctx.setCallState(XCtx.IN_CALL_REJECT_DND);
        } else {
            ctx.setCallState(XCtx.IN_CALL_MISSED);
        }

//        XMob mob = (XMob) Blockbook.getInstance(context).get(XMob.toFQMN(ctx.getUsernameCaller(), Addressbook.getMyCountryCode()));
        if (Blockbook.getInstance(context).isBlocked(XMob.toFQMN(ctx.getUsernameCaller(), Addressbook.getMyCountryCode()))) {
            ctx.setCallState(XCtx.IN_CALL_BLOCKED);
            isBlocked = true;
            Log.d(TAG, "onMissedCall isBlocked");
        }

        XQrp autoResponse = Persistence.getInstance(context).getQuickAutoResponse();
        if (autoResponse != null) {

            if ((autoResponse.getAutoResponseEndTime() > Calendar.getInstance().getTimeInMillis()
                    && autoResponse.getQuickResponseValue().length() > 0)) {
                //auto response is SET
                Log.d(TAG, "onMissedCall autoResponse");
                ctx.setCallState(XCtx.IN_CALL_REJECT_AUTOREPLY);

                //SEND AUTO RESPONSE
                XMob mob = addressbook.getContact(number);
                /*11-11-14:comment below RETURN to let ACK pass once GPRS is reconnected after CALL disconnects*/
                if (mob != null && !isBlocked) {//is a contact and not blocked
                    ctx.setAck(autoResponse.getQuickResponseValue());
                    CaltxtHandler.get(context).sendXCtxReply(ctx, autoResponse.getQuickResponseValue());

                    //SHOW ALERT
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    boolean isScreenOn = pm.isScreenOn();
                    if (isScreenOn) {
                        Notify.notify_caltxt_autoresponse_sent(context, ctx.getNameCaller(),
                                autoResponse.getQuickResponseValue(), "", ctx.getRecvToD());

                        //	        		String [] additionalArgs = {autoResponse.getQuickResponseValue()};
                        //					String actionTaken = context.getString(R.string.alert_sent_autoresponse,
                        //							(Object[]) additionalArgs);
                        //					Notify.toast(context, actionTaken, Toast.LENGTH_LONG);
                    } else {
                        //notify in action center
                        //		        	if(ctx.getCaltxt().length()>0) {
                        Notify.notify_caltxt_autoresponse_sent(context, ctx.getNameCaller(),
                                autoResponse.getQuickResponseValue(), "", ctx.getRecvToD());
                        //		        	}
                    }
                }
            } else {
                //reset all auto responses
                Persistence.getInstance(context).resetAutoResponse();
                //reset profile status for auto response
                Addressbook.getMyProfile().resetStatusAutoResponding();
            }
        } else {
//				PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
//				boolean isScreenOn = pm.isScreenOn();
            if (isBlocked) {//caller blocked
                Notify.notify_caltxt_call_blocked(context, ctx.getNameCaller(),
                        ctx.getCaltxt(),
                        "Tap for blocked list", Calendar.getInstance().getTimeInMillis());
            } else if (Addressbook.getMyProfile().isDND()) {//DND mode
                Notify.notify_caltxt_call_dnd(context, ctx.getNameCaller(),
                        ctx.getCaltxt(),
                        "Tap to disable", Calendar.getInstance().getTimeInMillis());
            } else {

                //missed Caltxt call
                /*COMMENTED 09-JUL-16: missed call alert (for caltxt calls) is sent via CaltxtHandler
                 * non Caltxt calls missed alerts are anyway sent by the phone itself */
//	        	if(isCaltxtCall) {
//	        		Notify.notify_caltxt_missed_call(ctx.getNameCaller(),
//	        				ctx.getCaltxt(), "", ctx.getRecvToD());
//					Log.d(TAG, "MissedCall: "+ctx);
//	        	}

				/*if is contact OR context call, show Acknowledgment window
				if(Addressbook.get().isContact(ctx.getUsernameCaller()) || ctx.isCaltxtCall()) {
					Intent ackList = new Intent(context, AcknowledgementList.class);
					ackList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ackList.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
					ackList.putExtra("IDTOBJECT", ctx);
					context.startActivity(ackList);
				}*/
            }
        }

        Logbook.get(context).update(ctx);

		/*if((ctx.getCallState()==XCtx.IN_CALL_BLOCKED 
				|| ctx.getCallState()==XCtx.IN_CALL_REJECT_AUTOREPLY
				|| ctx.getCallState()==XCtx.IN_CALL_REJECT_DND)) {
*/
        final XCtx ctxxx = ctx;
        CountDownTimer timer = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                // send propertychange now so that Caltxt toast finishes self if showing up
                Connection.get().addAction(Constants.callIncomingMissedProperty, ctx.getUsernameCaller(), ctx.getSubject());

                //to show the missed call
                Intent it = new Intent(context, CaltxtToast.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                it.putExtra("IDTOBJECT", ctxxx);
                it.putExtra("ACTION", Constants.TOAST_ACTION_VIEW);
                context.startActivity(it);
            }
        };
        timer.start();
//		}
        Log.d(TAG, "onMissedCall end");
    }

    @Override
    protected void onOutgoingCallStarted(Context context, String number, Date start) {
//		Addressbook addressbook = Addressbook.getInstance(context);
        Log.d(TAG, "onOutgoingCallStarted start");

        // change status to BUSY when on call, reset in ON_IDLE
        changeStatusToBusy(context);

        String fqmn = XMob.toFQMN(number, Addressbook.getInstance(context).getMyCountryCode());

        final XCtx ctx = CaltxtHandler.get(context).peekCaltxt(Constants.CALL_TYPE_OUTBOUND, fqmn);
        if (ctx == null) {//XCTX created to register normal call in caltxt log

            // Android dialer is used to call. Send caltxt to registered users so that if call missed then
            // it appears in caltxt log also
            if (Addressbook.getInstance(context).isRegistered(number)) {
                // 17MAY17, send missed call Caltxt atleast
                XMob mob = Addressbook.getInstance(context.getApplicationContext()).getRegistered(fqmn);
                // initiateContextCall will send caltxt and let call go through
                // initiateNormalCall will not be called through initiateContextCall
                // since CaltxtInputActivity is not active
                final XCtx ctxx = new XCtx();
                ctxx.setCallerSelf(Addressbook.getInstance(context).getMyProfile(), Addressbook.getInstance(context).getMyCountryCode());// set myself as caller
                ctxx.setNameCallee(mob.getName());
                ctxx.setNumberCallee(mob.getNumber());
                ctxx.setCallPriority(XCtx.PRIORITY_NORMAL);
                ctxx.setRecvToD(System.currentTimeMillis());//set call recieve / initiate time to NOW (as initiated)
                ctxx.setStartToD(System.currentTimeMillis());//set call start time to NOW
                ctxx.setCaltxt("");
                ctxx.setCallState(XCtx.OUT_CALL);
                ctxx.setCaltxtEtc(null);

                CaltxtHandler.get(context).pushCaltxt(Constants.CALL_TYPE_OUTBOUND, mob.getUsername(), ctxx);

                CaltxtHandler.get(context).sendXCtx(mob.getUsername(), ctxx);
            }
        } else {
        }

        Log.d(TAG, "onOutgoingCallStarted end");
    }

    @Override
    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
        Addressbook addressbook = Addressbook.getInstance(context);
        Log.d(TAG, "onOutgoingCallEnded start");
        //set MY status back to available - show BUSY status when on call, otherwise as set by ME
//		addressbook.getMyProfile().setHeadline(temporaryHeadlineHolder);
//		addressbook.getMyProfile().setStatus(temporaryStatusHolder);

        //to unregister registerChangeListener in CaltxtHandler.initiateContextCall
//		RebootService.getConnection().removeChangeListener(CaltxtHandler.get());

        String fqmn = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        XCtx ctx = CaltxtHandler.get(context).popCaltxt(Constants.CALL_TYPE_OUTBOUND, fqmn);
        if (ctx == null) {
            ctx = new XCtx();
            ctx.setCallerSelf(Addressbook.getMyProfile(), Addressbook.getMyCountryCode());// set myself as caller
            ctx.setNameCallee(addressbook.getName(number));
            // XCtx should store number instead of username. Helps in correct number dialing.
            // For query to Addressbook, number can be converted into username
            ctx.setNumberCallee(number);
//			ctx.setUsernameCallee(fqmn);
            ctx.setCallPriority(XCtx.PRIORITY_NORMAL);
            ctx.setCallerSelf(Addressbook.getMyProfile(), Addressbook.getMyCountryCode());
            ctx.setCallState(XCtx.OUT_CALL);
            ctx.setStartToD(start.getTime());
            ctx.setEndToD(end.getTime());
            Logbook.get(context).prepend(ctx);
        } else {
            ctx.setStartToD(start.getTime());
            ctx.setEndToD(end.getTime());
//			Persistence.getInstance(context).update(ctx);
            Logbook.get(context).update(ctx);
        }
        // hide caltxt toast
//		ActivityCaltxt.closeCaltxt();
/*		if(ActivityCaltxt.global_toast!=null) {
			ActivityCaltxt.global_toast.cancel();
			ActivityCaltxt.global_toast = null;
		}*/
//		outboundQ.clear();
        Connection.get().addAction(Constants.callOutgoingEndProperty, ctx.getUsernameCaller(), ctx.getSubject());
//		RebootService.getConnection().addAction(Constants.callComplete, ctx.getNumberCaller(), ctx.getSubject());
        Log.d(TAG, "onOutgoingCallEnded end");
    }

    void changeStatusToBusy(Context context) {
        Addressbook addressbook = Addressbook.getInstance(context);
        // Show BUSY status when on call here, reset to old status in onReceive::CALL_STATE_IDLE
        if (Addressbook.getMyProfile().isAutoResponding() || Addressbook.getMyProfile().isDND()
                || Addressbook.getMyProfile().isBusy()) {
            // don't change status
        } else {

            // if current status is either DND or Automatic response or Busy, don't change status during
            // call state change (incoming, outgoing). If current state is other than these 2 states,
            // then change the status to "Busy"
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

            SignupProfile.setPreference(context,
                    context.getString(R.string.preference_key_temporary_headline),
                    settings.getString(context.getString(R.string.profile_key_status_headline),
                            XMob.STRING_STATUS_AVAILABLE));
//			Caltxt.temporaryHeadlineHolder = settings.getString(context.getString(R.string.profile_key_status_headline),
//					XMob.STRING_STATUS_AVAILABLE);
//			temporaryHeadlineHolder = Addressbook.getInstance(context).getMyProfile().getHeadline();

            SignupProfile.setPreference(context,
                    context.getString(R.string.preference_key_temporary_status),
                    Integer.toString(settings.getInt(context.getString(R.string.profile_key_status),
                            XMob.STATUS_AVAILABLE)));
//			Caltxt.temporaryStatusHolder = settings.getInt(context.getString(R.string.profile_key_status),
//					XMob.STATUS_AVAILABLE);
//			temporaryStatusHolder = addressbook.getMyProfile().getStatus();

//			addressbook.getMyProfile().setHeadline(XMob.STRING_STATUS_BUSY_ONCALL);
//			addressbook.getMyProfile().setHeadline(XMob.STRING_STATUS_BUSY);
//			addressbook.getMyProfile().setStatusBusy();
            addressbook.changeMyStatus(XMob.STRING_STATUS_BUSY, // change only status
                    Addressbook.getMyProfile().getPlace());        // place remains same

//			Log.d(TAG, "temporaryHeadlineHolder "+Caltxt.temporaryHeadlineHolder+", "+Caltxt.temporaryStatusHolder);
        }
    }
/*
	public void CheckNumberAndUpdatePhoneAddressbook(final ArrayList<XMob> fqmn_list) {
		new Thread(new Runnable() {
			@Override
			public void run() {
	            XPbk reg = new XPbk();
				reg.unm = Addressbook.getMyProfile().getUsername();
				reg.myPhStus = fqmn_list;
				XRes reso = ModelFacade.getInstance().fxServiceRequest(ModelFacade.getInstance().SVC_CALTXT_USER, 
	            		ModelFacade.getInstance().OP_GET, reg);
				Vector<XMob> pb;
				try{
	        		pb = (Vector)reso.rslt;
	        	} catch (ClassCastException e){
	        		XMob pbk = (XMob)reso.rslt;
	        		pb = new Vector();
	        		pb.add(pbk);
	        	}

				if(pb!=null) {
					Globals.syncContactsInDB(pb);
					int sz = pb.size();
					for(int i=0;i<sz;i++) {
						XMob m1 = (XMob) pb.get(i);
						//broadcast status
						RebootService.getConnection().publishDiscovery(m1.getUsername(), Addressbook.getMyProfile().toString());
					}
				}
			}
		}).start();
	}*/
}
