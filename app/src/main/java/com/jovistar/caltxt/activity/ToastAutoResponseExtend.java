package com.jovistar.caltxt.activity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.notification.Notify;

public class ToastAutoResponseExtend extends AppCompatActivity {
    private static final String TAG = "ToastAutoResponseExtend";

    boolean interactive = false;

    private CountDownTimer timer;

//	boolean KEYGUARD_DISABLED = false;
//	WakeLock fullWakeLock = null;
//	WakeLock partialWakeLock = null;
//	KeyguardLock keyguardLock = null;

    View thisView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final XQrp qrp = (XQrp) getIntent().getSerializableExtra("IDTOBJECT");

//		CaltxtToast.wakeDevice();//USE FLAG_SHOW_WHEN_LOCKED instead, 05-DEC-16
//		CaltxtToast.unlockKeyguardAndLitScreen(this);
        getWindow().addFlags(
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | /*14-SEP-16, WHY*/
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

//		getWindow().addFlags(
//				/*WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//						| */WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        /*getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_autoresponse_extend);

		interactive = false;

		thisView = getWindow().getDecorView();

		Button toast_yes_button = (Button) findViewById(R.id.toast_yes_button);
		Button toast_no_button = (Button) findViewById(R.id.toast_no_button);
		TextView toast_body_text = (TextView) findViewById(R.id.toast_body_text);
		TextView toast_subject_text = (TextView) findViewById(R.id.toast_subject_text);
		TextView toast_head_text = (TextView) findViewById(R.id.toast_head_text);
		toast_subject_text.setText(getString(R.string.prompt_caltxt_autoresponse_expired));
		toast_head_text.setText(getString(R.string.prompt_caltxt_autoresponse_extend));
		toast_body_text.setText(qrp.getQuickResponseValue());
//		thisView.setBackgroundResource(R.drawable.bg_shadow_green_black_text);
		getWindow().setTitleColor(getResources().getColor(android.R.color.darker_gray));
		getWindow().setTitle(getString(R.string.prompt_caltxt_autoresponse_expired));
//		getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_shadow_grey));

		toast_no_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Globals.stopPlayAlarm();
				CaltxtToast.releaseWakeDevice();
				finish();
			}
		});

		toast_yes_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Globals.stopPlayAlarm();
				finish();
				Intent it = new Intent(ToastAutoResponseExtend.this, QuickResponseEdit.class);
				it.putExtra("IDTOBJECT", qrp);
				startActivity(it);
			}
		});
*/
        AlertDialog.Builder builder = new AlertDialog.Builder(ToastAutoResponseExtend.this, R.style.CaltxtAlertDialogTheme);
        builder.setPositiveButton(R.string.prompt_confirm_yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Notify.stopPlayAlarm();
                        finish();
                        Intent it = new Intent(ToastAutoResponseExtend.this, QuickResponseEdit.class);
                        it.putExtra("IDTOBJECT", qrp);
                        startActivity(it);
                    }
                });
        builder.setNegativeButton(R.string.prompt_confirm_no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Notify.stopPlayAlarm();
//						CaltxtToast.releaseWakeDevice();//05-DEC-16, why wake device, only lit up screen
                        finish();
                    }
                });
        builder.setMessage("Automatic response period expired for " + "\"" + qrp.getQuickResponseValue() + "\"").setTitle(R.string.prompt_caltxt_autoresponse_extend);
        builder.setIcon(R.mipmap.ic_launcher);
        final AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                Notify.stopPlayAlarm();
//				CaltxtToast.releaseWakeDevice();//05-DEC-16, why wake device, only lit up screen
                timer.cancel();
                finish();
            }

        });
        dialog.show();

        timer = new CountDownTimer(Constants.CALTXT_INPUT_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
//				CaltxtToast.releaseWakeDevice();//05-DEC-16, why wake device, only lit up screen
                finish();
            }
        }.start();

        Notify.playAlarm(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Notify.stopPlayAlarm();
//		CaltxtToast.releaseWakeDevice();//release in QuickResponseEdit
        super.onDestroy();
    }
}
