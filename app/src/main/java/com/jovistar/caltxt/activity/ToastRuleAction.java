package com.jovistar.caltxt.activity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XRul;
import com.jovistar.caltxt.network.voice.CallManager;
import com.jovistar.caltxt.network.voice.CaltxtHandler;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.XMob;

import java.util.Calendar;

public class ToastRuleAction extends AppCompatActivity {
    private static final String TAG = "ToastRuleAction";

    boolean interactive = false;

    View thisView;
    CountDownTimer timer;
    AlertDialog dialog = null;
    boolean PAUSED = false;
    boolean ALERTSHOW = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PAUSED = false;

        final XRul rule = (XRul) getIntent().getSerializableExtra("IDTOBJECT");
        Log.d(TAG, "Rule " + rule.toString());

//		CaltxtToast.wakeDevice();//USE FLAG_SHOW_WHEN_LOCKED instead, 05-DEC-16
//		CaltxtToast.unlockKeyguardAndLitScreen(this);
        getWindow().addFlags(
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | /*14-SEP-16, WHY*/
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        getWindow().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);

//		getWindow().addFlags(
//				/*WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//						|*/ WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        /*getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_autoresponse_extend);

		interactive = false;
*/
        thisView = getWindow().getDecorView();

        AlertDialog.Builder builder = new AlertDialog.Builder(ToastRuleAction.this, R.style.CaltxtAlertDialogTheme);
        builder.setCancelable(false);//modal
        builder.setPositiveButton(rule.getAction().equals(XRul.RULES_ACTION_TYPE_TEXT)
                        ? R.string.action_send : (rule.getAction().equals(XRul.RULES_ACTION_TYPE_CALL)
                        ? R.string.action_call : (rule.getAction().equals(XRul.RULES_ACTION_TYPE_MUTE_PHONE)
                        ? R.string.action_mute : R.string.action_unmute)),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1, int id1) {
//						Notify.stopPlayAlarm();

                        if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_TEXT)) {
                            //initiate text
                            XMob callee = Addressbook.getInstance(getApplicationContext()).getRegistered(rule.getActionFor());
                            if (callee == null) {
                                callee = new XMob();
                                callee.setUsername(rule.getActionFor());
                            }
                            CaltxtHandler.get(ToastRuleAction.this).publishTriggerAlert(callee.getUsername(),
                                    rule.getActionValue(), "", "");
//							CaltxtHandler.get(ToastRuleAction.this).initiateMessage((XMob) callee, rule.getActionValue(),
//									XCtx.PRIORITY_NORMAL);
                        } else if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_MUTE_PHONE)) {
                            CallManager.getInstance().vibrateModeRinger(getApplicationContext());
                        } else if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_UNMUTE_PHONE)) {
                            CallManager.getInstance().normalModeRinger(getApplicationContext());
                        } else if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_CALL)) {
                            //make a call
                            XMob callee = Addressbook.getInstance(getApplicationContext()).getRegistered(rule.getActionFor());
                            if (callee == null) {
                                callee = new XMob();
                                callee.setUsername(rule.getActionFor());
                            }
                            Intent caltxtInput = new Intent(ToastRuleAction.this, CaltxtInputActivity.class);
                            caltxtInput.putExtra("IDTOBJECT", callee);
//							caltxtInput.putExtra("VIEW", Constants.INPUT_VIEW);
                            startActivity(caltxtInput);
                        }
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//						Notify.stopPlayAlarm();
                        dialog.cancel();
                    }
                });
        if (rule.getEvent().equals(XRul.RULES_EVENT_TYPE_TIME)) {
            builder.setNeutralButton(R.string.prompt_snooze,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//							Notify.stopPlayAlarm();
                            rule.setActionWhen(Calendar.getInstance().getTimeInMillis() + 5 * 60 * 1000);
                            Notify.toast(thisView, getBaseContext(), "Trigger delayed for 5 minutes", Toast.LENGTH_LONG);
                            Persistence.getInstance(ToastRuleAction.this).update(rule);
                            IFTTT.resetTimedActions(getApplicationContext(), rule);
                            dialog.cancel();
                        }
                    });
        }

        if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_TEXT)) {
            //send text
            if (rule.isAlwaysAsk()) {
//				Log.d(TAG, "playAlarm");
//				Notify.playAlarm(getApplicationContext());

                String[] additionalArgs = {"<b>" + Addressbook.getInstance(getApplicationContext()).getName(rule.getActionFor()) + "</b>" + " (<i>" + rule.getActionValue() + "</i>)"};
                String actionTaken = getString(R.string.action_text_reminder_body, (Object[]) additionalArgs);
                builder.setMessage(Html.fromHtml(actionTaken)).setTitle(R.string.action_text_reminder_header);
            }
        } else if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_CALL)) {
            //call
//			if(rule.isAlwaysAsk()) {
//			Log.d(TAG, "playAlarm");
//				Notify.playAlarm(getApplicationContext());

            String[] additionalArgs = {"<b>" + Addressbook.getInstance(getApplicationContext()).getName(rule.getActionFor()) + "</b>"};
            String actionTaken = getString(R.string.action_call_reminder_body, (Object[]) additionalArgs);
            builder.setMessage(Html.fromHtml(actionTaken)).setTitle(R.string.action_call_reminder_header);
//			}

        } else if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_MUTE_PHONE)
                || rule.getAction().equals(XRul.RULES_ACTION_TYPE_UNMUTE_PHONE)) {
            //mute
            if (rule.isAlwaysAsk()) {
//				Log.d(TAG, "playAlarm");
//				Notify.playAlarm(getApplicationContext());

                builder.setMessage(Html.fromHtml("<b>" + rule.getAction() + " this phone now</b>")).setTitle(R.string.action_mute_reminder_header);
            }

        }

        Drawable d1 = getResources().getDrawable(R.drawable.ic_alarm_white_24dp);
        if (rule.getEvent().equals(XRul.RULES_EVENT_TYPE_STATUS)) {
            d1 = getResources().getDrawable(Addressbook.getInstance(getApplicationContext()).getStatusResourceIDByName(rule.getEventValue()));
            builder.setTitle(R.string.action_status_change_alert);
        }
        Drawable wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.darkgreen));
        d1.invalidateSelf();
        builder.setIcon(d1);
        dialog = builder.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Notify.stopPlayAlarm();
                ALERTSHOW = false;
                Log.d(TAG, "onCancel stopPlayAlarm");
                timer.cancel();
                finish();
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Notify.playAlarm(getApplicationContext());
                ALERTSHOW = true;
                Log.d(TAG, "onShow playAlarm");
            }
        });
        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                Notify.stopPlayAlarm();
                ALERTSHOW = false;
                Log.d(TAG, "onDismiss stopPlayAlarm");
//				CaltxtToast.releaseWakeDevice();//05-DEC-16, why wake device, only lit up screen
                timer.cancel();
                finish();
            }

        });

        timer = new CountDownTimer(Constants.CALTXT_INPUT_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
                if (PAUSED) {
                    dialog.dismiss();
//					Notify.stopPlayAlarm();
                    timer.cancel();
                    finish();

                    Intent it = new Intent(getBaseContext(), ToastRuleAction.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    it.putExtra("IDTOBJECT", rule);
                    startActivity(it);
                    Log.d(TAG, "onTick, show()");
                } else {
                    if (ALERTSHOW == false)
                        dialog.show();
                }
            }

            public void onFinish() {
//				CaltxtToast.releaseWakeDevice();//05-DEC-16, why wake device, only lit up screen
                dialog.dismiss();
                finish();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume " + this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//		Notify.stopPlayAlarm();
        if (ALERTSHOW) {
            // alert dialog is coming up, therefore the activity hosting it is pausing
            PAUSED = true;
        } else {
            // alert dialog is not up yet, so no need to flag activity PAUSED (hidden behind another activity)
        }

//		dialog.show();
        Log.d(TAG, "onPause " + this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy " + this);
//		Notify.stopPlayAlarm();
//		dialog.dismiss();
        //		CaltxtToast.releaseWakeDevice();//release in QuickResponseEdit
    }

/*	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int id = item.getItemId();
		if (android.R.id.home == id) {
			Log.d(TAG, "onHome");
			Notify.stopPlayAlarm();
			timer.cancel();
			finish();
			return true; // true = handled manually (consumed)
		} else {
			// Default behaviour for other items
			return super.onOptionsItemSelected(item);
		}
	}*/
}
