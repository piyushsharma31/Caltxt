package com.jovistar.caltxt.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.network.voice.CaltxtHandler;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Logbook;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AcknowledgementList extends AppCompatActivity {
    private static final String TAG = "AcknowledgementList";

    boolean interactive = false;
    XCtx ctx = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acknowledgement_list);

//		CaltxtToast.unlockKeyguardAndLitScreen(this);
        getWindow().addFlags(
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | /*14-SEP-16, WHY*/
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
//		getWindow().addFlags(
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        ctx = (XCtx) getIntent().getSerializableExtra("IDTOBJECT");

        final ListView listview = findViewById(R.id.acknowledgementlistview);
//		final ImageButton send_button = (ImageButton) findViewById(R.id.ack_send);
//		final String[] values = getResources().getStringArray(R.array.call_reject_acknowledgement);

        ArrayList<XQrp> list = Persistence.getInstance(this).restoreXQRP();
        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        R.layout.acknowledgement_list_item, R.id.label, values);*/
        final StableArrayAdapter adapter = new StableArrayAdapter(
                this,
                R.layout.acknowledgement_list_item,
                list);
        listview.setAdapter(adapter);
        setTitle("Send Message to " + ctx.getNameCaller());

        // if user does not start typing message in 8 seconds, this window
        // disappears
        new CountDownTimer(Constants.CALTXT_INPUT_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (!interactive)
                    finish();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
//		DTOListUI.isInForeground=true;
//		Log.d(TAG, "onResume, isInForeground TRUE");
    }

    @Override
    protected void onPause() {
        super.onPause();
//		DTOListUI.isInForeground=false;
//		Log.d(TAG, "onResume, isInForeground TRUE");
    }

    private class StableArrayAdapter extends ArrayAdapter<XQrp> {

        HashMap<Integer, XQrp> mIdMap = new HashMap<Integer, XQrp>();
        Context context;

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<XQrp> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(i, objects.get(i));
            }
        }

        /*
                @Override
                public long getItemId(int position) {
                    String item = getItem(position);
                    return mIdMap.get(item);
                }
        */
        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.acknowledgement_list_item, parent, false);
                final TextView textView = rowView.findViewById(R.id.ack_name);
                textView.setText(getItem(position).getQuickResponseValue());
                AppCompatImageButton send_button = rowView.findViewById(R.id.ack_send);
                send_button.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final String msg = textView.getText().toString();
                        XMob mob = Addressbook.getInstance(getApplicationContext()).getContact(ctx.getUsernameCaller());
                        if (mob != null) {
                            ctx.setAck(msg);
                            ctx.setCallState(XCtx.IN_CALL_REPLY);
                            Logbook.get(context).update(ctx);
                            CaltxtHandler.get(AcknowledgementList.this).sendXCtxReply(ctx, msg);
                        }
                        Notify.notify_caltxt_missed_call_alert_cancel(context);
                        finish();
                    }
                });

                rowView.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        interactive = true;
//						Intent caltxtInput = new Intent(AcknowledgementList.this, CaltxtToast.class);
                        Intent caltxtInput = new Intent(AcknowledgementList.this, CaltxtInputActivity.class);
                        caltxtInput.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        caltxtInput.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        ctx.setAck(textView.getText().toString());
                        ctx.setCallState(XCtx.IN_CALL_REPLY);
                        caltxtInput.putExtra("IDTOBJECT", ctx);
//						caltxtInput.putExtra("VIEW", Constants.INPUT_VIEW);
                        startActivity(caltxtInput);
                        finish();
                        Notify.notify_caltxt_missed_call_alert_cancel(context);
                        return false;
                    }
                });
            }
            return rowView;
        }
    }
}
