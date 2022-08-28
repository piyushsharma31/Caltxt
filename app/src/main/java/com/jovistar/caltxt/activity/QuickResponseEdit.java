package com.jovistar.caltxt.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.service.RuleAlarmReceiver;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;
import java.util.Calendar;

public class QuickResponseEdit extends AppCompatActivity {
    private static final String TAG = "ActivityQckResponseEdit";

    ListView listview = null;
    StableArrayAdapter adapter = null;
    long THIRTY_MINUTES = 30 * 60 * 1000;
    //	static Timer autoResponseResetTimer = null;
    static int lastSelectedAutoResponse = -1;

//	ArrayList<XQrp> quickResponseList = Persistence.getInstance(getApplicationContext()).restoreXQRP();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acknowledgement_list);

        listview = findViewById(R.id.acknowledgementlistview);
        listview.setAdapter(adapter = new StableArrayAdapter(
                this,
                R.layout.acknowledgement_list_item,
                Persistence.getInstance(this).restoreXQRP()));

        //Automatic response duration expired?
        XQrp qrp = Persistence.getInstance(this).getQuickAutoResponse();
        if ((qrp != null && qrp.getAutoResponseEndTime() <= Calendar.getInstance().getTimeInMillis())
                /*|| !Settings.delivery_by_sms*//*reset Automatic response if sms option not set*/) {
            //reset all Automatic responses
            resetAutoResponse(getApplicationContext());
	    	/*Persistence.getInstance(this).resetAutoResponse();
			//reset profile status for Automatic response
			Addressbook.getMyProfile().resetStatusAutoResponding();
			autoResponseResetTimer.cancel();*/
        } else {
            qrp = (XQrp) getIntent().getSerializableExtra("IDTOBJECT");
            if (qrp != null) {//extend case
                openAutoResponseOptions(qrp, true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class StableArrayAdapter extends ArrayAdapter<XQrp> {

        Context context;

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  ArrayList<XQrp> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
//			quickResponseList = objects;
        }

        @Override
        public XQrp getItem(int position) {
            ArrayList<XQrp> quickResponseList = Persistence.getInstance(getApplicationContext()).restoreXQRP();

            return quickResponseList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
//			String item = getItem(position).getQuickResponseValue();
//			return list.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View rowView = convertView;
            TextView textView = null;
            TextView textTime = null;
//			TextView textDate = null;
//			TextView textDummy = null;
            AppCompatImageButton send_button = null;

            final XQrp rowitem = getItem(position);
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.acknowledgement_list_item, null, true);
/*				rowView.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
					}
				});*/

//				if(rowitem!=null) {
//				send_button = (ImageButton) rowView.findViewById(R.id.ack_send);
//				send_button.setImageResource(R.drawable.ic_action_edit_g);
                rowView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ArrayList<XQrp> quickResponseList = Persistence.getInstance(getApplicationContext()).restoreXQRP();
                        final XQrp qrp = quickResponseList.get(Integer.parseInt(v.getTag().toString()));
                        lastSelectedAutoResponse = Integer.parseInt(v.getTag().toString());

//						if(Settings.delivery_by_sms && qrp.getAutoResponseEndTime()==0) {
                        if (Settings.isSMSEnabled(context) && qrp.getAutoResponseEndTime() == 0) {
//							Notify.toast(Globals.getCustomAppContext(), "To set automatic response, enable SMS in message delivery settings", Toast.LENGTH_LONG);
                            AlertDialog.Builder builder = new AlertDialog.Builder(QuickResponseEdit.this, R.style.CaltxtAlertDialogTheme);
                            builder.setPositiveButton(R.string.action_caltxt_autoresponse_sms_on,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
											/*Turn ON delivery by SMS and then show duration dialog
											SignupProfile.setPreference(QuickResponseEdit.this,
													getString(R.string.preference_key_caltxt_delivery_sms), 
													getString(R.string.preference_value_caltxt_delivery_sms));
											Settings.delivery_by_sms = true;*/
                                            openAutoResponseOptions(qrp, false);
                                        }
                                    });
                            builder.setNegativeButton(R.string.action_caltxt_autoresponse_sms_off,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
											/*Turn OFF delivery by SMS and then show duration dialog
											 * commented 08-DEC-16, setting via setSMSEnabled
											SignupProfile.setPreference(QuickResponseEdit.this,
													getString(R.string.preference_key_caltxt_delivery_sms), 
													getString(R.string.preference_value_caltxt_delivery_data));
											Settings.delivery_by_sms = false;*/
                                            Settings.setSMSEnabled(false, context);
                                            openAutoResponseOptions(qrp, false);
											/*Intent it = new Intent(QuickResponseEdit.this,
													Settings.class);
											startActivity(it);*/
                                        }
                                    });
                            builder.setMessage(R.string.prompt_caltxt_autoresponse_sms_alert).setTitle(R.string.prompt_caltxt_autoresponse_sms_off);
//							builder.setIcon(R.drawable.ic_warning_white_24dp);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            Notify.toast(v, getContext(), "Automatic response will be sent to online Caltxt contacts only", Toast.LENGTH_LONG);
                            openAutoResponseOptions(qrp, false);
                        }

                        return;
                    }
                });
//				}
                send_button = rowView.findViewById(R.id.ack_send);
//				send_button.setVisibility(View.INVISIBLE);
                send_button.setImageResource(R.drawable.ic_mode_edit_black_24dp);
                send_button.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {
//						final XQrp qrp = getItem(position);
                        ArrayList<XQrp> quickResponseList = Persistence.getInstance(getApplicationContext()).restoreXQRP();
                        final XQrp qrp = quickResponseList.get(Integer.parseInt(v.getTag().toString()));
//						final XQrp qrp = getItem(Integer.parseInt(v.getTag().toString()));
                        AlertDialog.Builder builder = new AlertDialog.Builder(QuickResponseEdit.this, R.style.CaltxtAlertDialogTheme);
                        builder.setTitle("Edit");

                        // Set up the input
                        final EditTextCaltxt input = new EditTextCaltxt(QuickResponseEdit.this);
                        input.setText(qrp.getQuickResponseValue());
                        input.setSingleLine(false);
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (input.getText().toString().length() == 0) {
                                    Notify.toast(v, getApplicationContext(), "Quick response cannot be blank", Toast.LENGTH_LONG);
                                    return;
                                }
                                Persistence.getInstance(context).updateXQRP(input.getText().toString(), qrp.getRowId());
                                qrp.setQuickResponseValue(input.getText().toString());
                                adapter.notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                });
            }

            rowView.setTag(position);
            if (send_button == null) {
                send_button = rowView.findViewById(R.id.ack_send);
            }
            send_button.setTag(position);
            textView = rowView.findViewById(R.id.ack_name);
            textTime = rowView.findViewById(R.id.expire_time_text);
//			textDate = (TextView) rowView.findViewById(R.id.date_text);
//			textDummy = (TextView) rowView.findViewById(R.id.dummy_text);
            textView.setText(rowitem.toString());
            if (rowitem.getAutoResponseEndTime() > 0) {
                textTime.setVisibility(View.VISIBLE);
//				textView.setTypeface(null, Typeface.BOLD);
                textTime.setTextColor(Color.RED);
                textTime.setText(
                        getResources().getString(R.string.auto_response_expires) +
                        DateUtils.getRelativeDateTimeString(context, rowitem.getAutoResponseEndTime(),
                                DateUtils.DAY_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString());
//				textDummy.setText("\t\t");
//				textDate.setTextColor(Color.RED);
//				textDate.setText("Tomorrow");
            } else {
                textTime.setTextColor(getResources().getColor(R.color.darkgrey));
                textTime.setText(null);
//				textDate.setText(null);
//				textDummy.setText(null);
                textTime.setVisibility(View.GONE);
//				textDate.setVisibility(View.GONE);
//				textDummy.setVisibility(View.GONE);
            }
            return rowView;
        }

    }

    public static void promptExtendAutoResponse(Context context) {

        ArrayList<XQrp> quickResponseList = Persistence.getInstance(context).restoreXQRP();
        final XQrp qrp = quickResponseList.get(lastSelectedAutoResponse);
        Intent it = new Intent(context, ToastAutoResponseExtend.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        it.putExtra("IDTOBJECT", qrp);
        context.startActivity(it);
    }

    private void openAutoResponseOptions(final XQrp qrp, boolean extend) {
//		String auto_response_none = getResources().getString(R.string.preference_value_autoresponse_default_none);
//    	String auto_response_value = qrp.getQuickResponseValue();
        Log.d(TAG, "openAutoResponseOptions end time " + qrp.getAutoResponseEndTime()
                + ", extend " + extend);

        //check if row is already SET as Automatic response
//    	if(qrp.getAutoResponseEndTime()>0) {
//	    	auto_response_value = auto_response_none;
//    	}

//    	if(!extend) {
//    		resetAutoResponse();
//    	}
		/*Addressbook.getMyProfile().resetStatusAutoResponding();
    	//reset all Automatic responses
    	Persistence.getInstance(context).resetAutoResponse();
    	for(int i=0; i<list.size();i++) {
    		list.get(i).setAutoResponseEndTime(0);
    	}*/

        //if row is already SET
        if (qrp.getAutoResponseEndTime() > 0 && !extend) {
//    	if(!auto_response_value.equals(auto_response_none)) {
            resetAutoResponse(getApplicationContext());

            adapter.notifyDataSetChanged();
            String actionTaken = getApplicationContext().getString(R.string.preference_summary_autoresponse_disabled);
            Notify.toast(listview, getApplicationContext(),
                    actionTaken,
                    Toast.LENGTH_LONG);
        } else {
            //if row is NOT already SET. Reset any other Automatic response first (only one can exist)
            resetAutoResponse(getApplicationContext());

            AlertDialog.Builder builder = new AlertDialog.Builder(QuickResponseEdit.this, R.style.CaltxtAlertDialogTheme);
            builder.setTitle(R.string.preference_auto_response_value_select);
            builder.setItems(R.array.preference_autoresponse_array, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    long endtm = Calendar.getInstance().getTimeInMillis();
                    String auto_response_value = "15 minutes";
                    if (which == 0) {//15 min
                        endtm = Calendar.getInstance().getTimeInMillis() + 900 * 1000;
                        auto_response_value = "15 minutes";
                    } else if (which == 1) {//30 min
                        endtm = Calendar.getInstance().getTimeInMillis() + 1801 * 1000;
                        auto_response_value = "30 minutes";
                    } else if (which == 2) {//1 hour
                        endtm = Calendar.getInstance().getTimeInMillis() + 3601 * 1000;
                        auto_response_value = "1 hour";
                    } else if (which == 3) {//2 hours
                        endtm = Calendar.getInstance().getTimeInMillis() + 7201 * 1000;
                        auto_response_value = "2 hours";
                    } else if (which == 4) {//3 hours
                        endtm = Calendar.getInstance().getTimeInMillis() + 10803 * 1000;
                        auto_response_value = "3 hours";
                    }
                    Persistence.getInstance(QuickResponseEdit.this).setQuickResponseAsAuto(qrp.getRowId(), endtm);
			    	/*autoResponseResetTimer = new Timer();
			    	autoResponseResetTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							//reset profile status for Automatic response
					    	resetAutoResponse();
//					    	Globals.playAlarm();
					    	promptExtendAutoResponse();
						}
			    	}, new Date(endtm));*/
                    qrp.setAutoResponseEndTime(endtm);
                    RuleAlarmReceiver.SetAutoResponseAlarm(getApplicationContext(), qrp.getPersistenceId(), endtm, listview);

                    //set profile status for Automatic response
                    Addressbook.getInstance(getApplicationContext()).getMyProfile().setStatusAutoResponding();
                    //set the status to BUSY and headline to Automatic response text
//					Addressbook.getMyProfile().setStatusBusy();
                    Addressbook.getInstance(getApplicationContext()).changeMyStatus(qrp.getQuickResponseValue(),
                            Addressbook.getInstance(getApplicationContext()).getMyProfile().getPlace());
/*
//					Addressbook.getMyProfile().setHeadline(qrp.getQuickResponseValue());
//					SignupProfile.setPreference(CaltxtApp.getCustomAppContext(),
//					getString(R.string.profile_key_status_headline),
//					qrp.getQuickResponseValue());
			        SignupProfile.setPreference(CaltxtApp.getCustomAppContext(),
			        		getString(R.string.profile_key_status_icon), 
			        		R.drawable.ic_busy_white_24dp);

			        SignupProfile.setPreference(CaltxtApp.getCustomAppContext(),
			        		getString(R.string.profile_key_status), 
			        		Addressbook.getMyProfile().getStatus());

					SignupProfile.setPreference(CaltxtApp.getCustomAppContext(),
							getString(R.string.profile_key_status_headline),
							qrp.getQuickResponseValue());
					RebootService.is_status_dirty = true;
					//update status on server also
					ModelFacade.getInstance().fxAsyncServiceRequest(
							ModelFacade.getInstance().SVC_CALTXT_USER,
							ModelFacade.getInstance().OP_SET, Addressbook.getMyProfile(), 
							new CCWService());
*/
                    // addAction moved to changeMyStatus
//					RebootService.getConnection(getApplicationContext()).addAction(Constants.myStatusChangeProperty, null, qrp.getQuickResponseValue());
//					RebootService.BroadcastStatus();//- commented 5/APR/16 - integrated in Adapter::getView()
                    finish();
                }
            });
            builder.create().show();
        }
    }

    public static void resetAutoResponse(Context context) {
        XQrp qrp = Persistence.getInstance(context).getQuickAutoResponse();

        if (qrp == null) {
            return;
        }
        Log.d(TAG, "resetAutoResponse");

        RuleAlarmReceiver.cancel(context, (int) qrp.getPersistenceId());

        Persistence.getInstance(context).resetAutoResponse();
        ArrayList<XQrp> quickResponseList = Persistence.getInstance(context).restoreXQRP();
        //reset profile status for Automatic response
        Addressbook.getInstance(context).getMyProfile().resetStatusAutoResponding();
        for (int i = 0; i < quickResponseList.size(); i++) {
            quickResponseList.get(i).setAutoResponseEndTime(0);
        }
		/*if(autoResponseResetTimer!=null) {
			autoResponseResetTimer.cancel();
			autoResponseResetTimer = null;
		}*/

		/*restore previous status headline
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(CaltxtApp.getCustomAppContext());
		Addressbook.getMyProfile().setHeadline(settings.getString(
				CaltxtApp.getCustomAppContext().getString(R.string.profile_key_status_headline), XMob.STRING_STATUS_DEFAULT));
        Addressbook.get().setMyStatusFromIconResource(settings.getInt(
        		CaltxtApp.getCustomAppContext().getString(R.string.profile_key_status_icon), R.drawable.ic_available_white_24dp));
*/
        Addressbook.getInstance(context).getMyProfile().setStatusAvailable();
        Addressbook.getInstance(context).changeMyStatus(XMob.STRING_STATUS_AVAILABLE,
                Addressbook.getInstance(context).getMyProfile().getPlace());
/*
		Addressbook.getMyProfile().setHeadline(XMob.STRING_STATUS_AVAILABLE);
		SignupProfile.setPreference(CaltxtApp.getCustomAppContext(),
				CaltxtApp.getCustomAppContext().getString(R.string.profile_key_status_headline),
				XMob.STRING_STATUS_AVAILABLE);

        SignupProfile.setPreference(CaltxtApp.getCustomAppContext(),
        		CaltxtApp.getCustomAppContext().getString(R.string.profile_key_status_icon), 
        		R.drawable.ic_available_white_24dp);

        SignupProfile.setPreference(CaltxtApp.getCustomAppContext(),
        		CaltxtApp.getCustomAppContext().getString(R.string.profile_key_status), 
        		Addressbook.getMyProfile().getStatus());

        ModelFacade.getInstance().fxAsyncServiceRequest(
				ModelFacade.getInstance().SVC_CALTXT_USER,
				ModelFacade.getInstance().OP_SET, Addressbook.getMyProfile(), 
				new CCWService());
*/
        // addAction moved to changeMyStatus
//		RebootService.getConnection(context).addAction(Constants.myStatusChangeProperty, null, XMob.STRING_STATUS_DEFAULT);
//		RebootService.BroadcastStatus();//- commented 5/APR/16 - integrated in Adapter::getView()
    }
}
