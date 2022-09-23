package com.jovistar.caltxt.activity;

import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.firebase.storage.DownloadService;
import com.jovistar.caltxt.firebase.storage.UploadService;
import com.jovistar.caltxt.images.ImageLoader;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.voice.CaltxtHandler;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.XMob;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//import androidx.core.content.LocalBroadcastManager;

public class CaltxtToast extends AppCompatActivity implements PropertyChangeListener {
    private static final String TAG = "CaltxtToast";

    int viewType = Constants.TOAST_ACTION_VIEW;

    int finalHeight = 0, finalWidth = 0;
    boolean interactive = false;

    private CountDownTimer timer;

    CaltxtHandler caltxtHandler = CaltxtHandler.get(this);
    String username;

    static boolean KEYGUARD_DISABLED = false;
    static WakeLock fullWakeLock = null;
    static WakeLock partialWakeLock = null;
    static KeyguardLock keyguardLock = null;

    View thisView;
    private AutoCompleteTextView mContextEdit;
    private ImageButton mCallerImage;
    private ImageView mCallerImageStatusOverlay, mCallerImageStatusOverlayBorder, mAlertImage, mBodyTextTimestampIcon;
    private TextView mHeaderText;
    private TextView mSubjectText;
    private TextView mSubject2Text;
    private TextView mBodyText;
    private TextView mBody2Text;
    private TextView mBodyTextTimestamp;
    private TextView mAlertText;
    private View mFooter2Border, mButtonDividerView;
    private AppCompatButton mCallButton, mMessageButton;
    //    AppCompatImageButton mExpandPicButton;
    AppCompatImageView mCaltxtPic, mAckPic;
    AppCompatImageButton mCancelBtn;
    View bodyframe = null;
    View body2frame = null;
    View footer1frame = null;
    View footer2frame = null;
    ProgressBar progressBar;//, progressBarCaltxtPhotoDownload, progressBarAckPhotoDownload;
    //    IDTObject dto = null;
    XCtx dto = null;
    Addressbook addressbook;

    // Local broadcast receiver
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive:" + intent);
//            hideProgressDialog();

            switch (intent.getAction()) {
                case DownloadService.DOWNLOAD_COMPLETED:
                    // Get number of bytes downloaded
                    long numBytes = intent.getLongExtra(DownloadService.EXTRA_BYTES_DOWNLOADED, 0);
                    String downloadPath = intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH);

                    // Alert success
                    /*showMessageDialog(getString(R.string.download_success), String.format(Locale.getDefault(),
                            "%d bytes downloaded from %s",
                            numBytes,
                            intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH)));*/
//                    mCaltxtPic.setVisibility(View.VISIBLE);
//                    progressBarCaltxtPhotoDownload.setVisibility(View.GONE);
                    if (dto.getBodyPicURL().equals(downloadPath)) {
                        ImageLoader.getInstance(getApplicationContext()).DisplayImage(
                                dto.getBodyPicURL(), mCaltxtPic,
                                200,
                                R.drawable.ic_terrain_white_24dp, false);
                    } else if (dto.getFooterPicURL().equals(downloadPath)) {
                        ImageLoader.getInstance(getApplicationContext()).DisplayImage(
                                dto.getFooterPicURL(), mAckPic,
                                200,
                                R.drawable.ic_terrain_white_24dp, false);
                    }

                    break;
                case DownloadService.DOWNLOAD_ERROR:
                    // Alert failure
                    /*showMessageDialog("Error", String.format(Locale.getDefault(),
                            "Failed to download from %s",
                            intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH)));*/
                    break;
                case UploadService.UPLOAD_COMPLETED:
                case UploadService.UPLOAD_ERROR:
//                    onUploadResultIntent(intent);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewType = getIntent().getIntExtra("ACTION", 0);
        if (viewType == 0) {
            finish();
            Log.v(TAG, "onCreate FINISH");
            return;
        }

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, DownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, UploadService.getIntentFilter());

        addressbook = Addressbook.getInstance(getApplicationContext());

        dto = (XCtx) getIntent().getSerializableExtra("IDTOBJECT");
        Log.i(TAG, "onCreate " + dto);

        username = dto.getUsernameCaller();
        if (Addressbook.isItMe(username)) {
            username = XMob.toFQMN((dto.getNumberCallee()), Addressbook.getMyCountryCode());
        }

        Connection.get().registerChangeListener(this);

//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // make it non modal only when call is received or made
        if (dto.getCallState() == XCtx.OUT_CALL || dto.getCallState() == XCtx.IN_CALL) {
            // Caltxt arrived, put flag not touch modal so that user can interact with call (to answer/disconnect)
            // Make it non-modal, so that others can receive touch events
            getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);

            // ...but notify us that it happened.
            getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
//            Log.v(TAG, "onCreate NON MODAL "+dto);
        } else {
            // make it MODAL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                setFinishOnTouchOutside(false);
                Log.v(TAG, "onCreate MODAL setFinishOnTouchOutside FALSE " + dto);
            } else {
                // previous API will have it modal by default
                Log.v(TAG, "onCreate MODAL setFinishOnTouchOutside TRUE " + dto);
            }

            // for incoming message, open locked keyboard, turn ON display
            // for incoming calls, display will be unlocked and turned on auto by OS
            /*getWindow().addFlags(
//				LayoutParams.FLAG_KEEP_SCREEN_ON |
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    LayoutParams.FLAG_DISMISS_KEYGUARD |
//				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            LayoutParams.FLAG_TURN_SCREEN_ON
            );*/
//            Log.v(TAG, "onCreate MODAL "+dto);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.caltxt);

        interactive = false;

//        progressBarCaltxtPhotoDownload = (ProgressBar) findViewById(R.id.caltxt_pic_progress_bar);
//        progressBarAckPhotoDownload = (ProgressBar) findViewById(R.id.ack_pic_progress_bar);
        progressBar = findViewById(R.id.toast_progress_bar);
        mCallerImage = findViewById(R.id.caltxt_person_img);
        mAlertImage = findViewById(R.id.toast_footer2_alert_img);
        mCallerImageStatusOverlay = findViewById(R.id.caltxt_person_img_overlay);
        mCallerImageStatusOverlayBorder = findViewById(R.id.caltxt_person_img_overlay_border);
        mHeaderText = findViewById(R.id.toast_head_text);
        mCancelBtn = findViewById(R.id.toast_head_cancel_img);
        mSubjectText = findViewById(R.id.toast_head_subject_text);
        mSubject2Text = findViewById(R.id.toast_head_subject2_text);
        mFooter2Border = findViewById(R.id.toast_footer2_border);
        footer2frame = findViewById(R.id.toast_footer2);
        footer1frame = findViewById(R.id.toast_footer);
        mBodyText = findViewById(R.id.toast_body_text);
        mBody2Text = findViewById(R.id.toast_body2_text);
        mBodyTextTimestamp = findViewById(R.id.toast_body_text_time);
        mBodyTextTimestampIcon = findViewById(R.id.toast_body_text_time_icon);

        mCallButton = findViewById(R.id.toast_footer_call_img);

		/* tint drawable mCallButton */
        Drawable d1 = getResources().getDrawable(R.drawable.ic_call_black_24dp);
        Drawable wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mCallButton.setCompoundDrawablesWithIntrinsicBounds(null, wrappedDrawable, null, null);

        // mCallButton.setBackgroundResource(android.R.drawable.btn_default);
        mMessageButton = findViewById(R.id.toast_footer_send_img);
//        mExpandPicButton = (AppCompatImageButton) findViewById(R.id.toast_body_pic_icon);
        mCaltxtPic = findViewById(R.id.caltxt_pic);
        mAckPic = findViewById(R.id.ack_pic);

		/* tint drawable mMessageButton */
        d1 = getResources().getDrawable(R.drawable.ic_send_black_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mMessageButton.setCompoundDrawablesWithIntrinsicBounds(null, wrappedDrawable, null, null);

        mButtonDividerView = findViewById(R.id.button_divider);
        mAlertText = findViewById(R.id.toast_footer2_alert);
        mContextEdit = findViewById(R.id.toast_body2_edit);
        bodyframe = findViewById(R.id.toast_body);
        body2frame = findViewById(R.id.toast_body2);
        footer1frame = findViewById(R.id.toast_footer);
//		footer2frame.setVisibility(View.GONE);

        thisView = findViewById(R.id.toasty);
//        thisView = getWindow().getDecorView();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                caltxtHandler.getCaltxtHistory());
        // "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789!?@$+-=%#*().,/:;[]~"
        mContextEdit.setAdapter(adapter);

        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

		/* below few lines to capture imageview h and w */
        ViewTreeObserver vto = mCallerImage.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mCallerImage.getViewTreeObserver().removeOnPreDrawListener(this);
                finalHeight = mCallerImage.getMeasuredHeight();
                finalWidth = mCallerImage.getMeasuredWidth();
                ImageLoader.getInstance(getApplicationContext()).DisplayImage(
                        username + XMob.IMAGE_FILE_EXTN, mCallerImage,
                        finalWidth/* Constants.LIST_IMG_HEIGHT */,
                        R.drawable.ic_person_white_24dp, true);

                return true;
            }
        });

		/*XMob mob = Addressbook.get().getContact(username);
        if(mob==null) {
			mob = new XMob();
			mob.setUsername(username);
		}*/
        mCallerImageStatusOverlayBorder.setBackgroundResource(android.R.color.transparent);
        mCallerImageStatusOverlay.setBackgroundResource(android.R.color.transparent);
        mCallerImageStatusOverlay.setImageResource(0);
//		mCallerImageStatusOverlay.setImageDrawable(Addressbook.get().getContactStatusOverlayIconResource((username)));
        // 30-JUN-17 commented, onClick onLongCLick defined in openReplyAndCallToast
/*        mCallButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String edittext = mContextEdit.getEditableText().toString();
                if (dto.getCName().equals("XCtx")) {// Call is disconnected and reply message is being sent
                    if (edittext.length() > 0) {
                        ((XCtx) dto).setCallState(XCtx.IN_CALL_REPLY);
                        ((XCtx) dto).setAck(edittext);
                        Logbook.get(getApplicationContext()).update(((XCtx) dto));
                        caltxtHandler.sendXCtxReply((XCtx) dto, edittext);
						// DISABLE SMS for now 26/MAR/2016
//						if (Addressbook.get().isCaltxtContactAndConnected(username)) {
//							caltxtHandler.sendReplyXCtx((XCtx) dto, edittext);
//						} else if (Addressbook.get().isCaltxtContact(username)
//								&& Settings.delivery_by_sms) {
//							caltxtHandler.sendReplyXCtxBySMS(((XCtx) dto).getUsernameCaller(),
//									(XCtx) dto, edittext);
//						}
                    }
//                } else if (dto.getCName().equals("XMob")) {// commented 21-FEB-17
//                    caltxtHandler.initiateContextCall((XMob) dto, edittext, XCtx.PRIORITY_EMERGENCY);// commented 21-FEB-17
                }
                finish();
                return true;
            }
        });
        mCallButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String edittext = mContextEdit.getEditableText().toString();
                if (dto.getCName().equals("XCtx")) {// Call is disconnected and
                    // reply message is being
                    // sent
                    if (edittext.length() > 0) {
                        ((XCtx) dto).setCallState(XCtx.IN_CALL_REPLY);
                        ((XCtx) dto).setAck(edittext);
                        Logbook.get(getApplicationContext()).update(((XCtx) dto));
                        caltxtHandler.sendXCtxReply((XCtx) dto, edittext);
						// DISABLE SMS for now 26/MAR/2016
//						if (Addressbook.get().isCaltxtContactAndConnected(username)) {
//							caltxtHandler.sendReplyXCtx((XCtx) dto, edittext);
//						} else if (Addressbook.get().isCaltxtContact(username)
//								&& Settings.delivery_by_sms) {
//							caltxtHandler.sendReplyXCtxBySMS(
//									((XCtx) dto).getUsernameCaller(),
//									(XCtx) dto, edittext);
//						}
                    }
//                } else if (dto.getCName().equals("XMob")) {// New Call with a message is being made
//                    caltxtHandler.initiateContextCall((XMob) dto, edittext, XCtx.PRIORITY_NORMAL);
                }
                finish();
            }
        });
        mMessageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String edittext = mContextEdit.getEditableText()
                        .toString();
//                if (dto.getCName().equals("XMob")) {// New message is being made
//                    if (edittext.length() > 0)
//                        caltxtHandler.initiateMessage((XMob) dto, edittext,
//                                XCtx.PRIORITY_NORMAL);
//                } else
                if (dto.getCName().equals("XCtx")) {// reply message is
                    // being made
                    if (edittext.length() > 0)
                        caltxtHandler.initiateMessageReply((XCtx) dto, edittext, "");
                }
                finish();
            }
        });*/
        mContextEdit.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                interactive = true;
                String str = s.toString();
                if (str.contains("^")) {
                    mContextEdit.setText(str.replace("^", ""));
                    return;
                }
                if (mContextEdit.getText().length() > 0) {
                    if (addressbook.isCaltxtContactAndConnected(username)) {
                        // mAlertText.setText("Caltxt will be delivered over data");
//					} else if (Settings.delivery_by_sms) {
                    } else if (Settings.isSMSEnabled(CaltxtToast.this)) {
/*						footer2frame.setVisibility(View.VISIBLE);//to make alert invisible (all caltxt sent via data only)
                        mFooter2Border.setVisibility(View.VISIBLE);
						mAlertText.setVisibility(View.VISIBLE);
						mAlertImage.setVisibility(View.VISIBLE);
						mAlertText.setText(R.string.input_text_sms_disclaimer);*/
                    } else {
/*						footer2frame.setVisibility(View.VISIBLE);//to make alert invisible (all caltxt sent via data only)
                        mFooter2Border.setVisibility(View.VISIBLE);
						mAlertText.setVisibility(View.VISIBLE);
						mAlertImage.setVisibility(View.VISIBLE);
						mAlertText.setText(R.string.input_text_disclaimer);*/
                    }
                } else if (mContextEdit.getText().length() == 0) {
                    footer2frame.setVisibility(View.GONE);
                    mFooter2Border.setVisibility(View.GONE);
                    mAlertImage.setVisibility(View.GONE);
                    mAlertText.setVisibility(View.GONE);
                    mAlertText.setText("");
                }
            }
        });

        setToastView(dto.getPersistenceId());

        if (viewType == Constants.TOAST_ACTION_SEND_VIEW) {
            int ret = CaltxtHandler.get(getApplicationContext()).sendXCtx(username, dto);

            if (ret == 0 && !Addressbook.isItMe(username)) {
                Log.d(TAG, "");
                // not registered, so not caltxt is sent, therefore initiate call here only
                XMob mob = Addressbook.getInstance(getApplicationContext()).get(username);
                CaltxtHandler.get(CaltxtToast.this).initiateNormalCall(
                        mob==null?"+"+username:mob.getNumber(),
                        CaltxtToast.this);
                finish();
            }
        }

        Log.d(TAG, "onCreate " + dto);
    }

    @Override
    protected void onResume() {
        if (dto.getCName().equals("XCtx")) {
            mBodyTextTimestamp.setText(dto.getSubject(getApplicationContext()));
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        if (viewType == Constants.TOAST_ACTION_SEND_VIEW) {
        Connection.get().removeChangeListener(this);
//        }
//		releaseWakeDevice();//USE FLAG_SHOW_WHEN_LOCKED instead, 05-DEC-16; why wake device, only lit up screen
        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        Log.d(TAG, "BLOCKTEST onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
//            finish();
            Log.d(TAG, "onTouchEvent ACTION_OUTSIDE");
            return true;
        }
        Log.d(TAG, "onTouchEvent ACTION_INSIDE");

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Acquires a partial wake lock for this client
     * If you hold a partial wake lock, the CPU will continue to run,
     * regardless of any display timeouts or the state of the screen
     * and even after the user presses the power button. In all other
     * wake locks, the CPU will run, but the user can still put the
     * device to sleep using the power button.
     *
     *  NEVER USE WAKE LOCK in an Activity,
     *  use FLAG_KEEP_SCREEN_ON instead there
     *
     * @return public synchronized static void acquirePartialWakeLock(Context context) {
    if (partialWakeLock == null) {
    PowerManager pm = (PowerManager) context.
    getSystemService(Context.POWER_SERVICE);
    partialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
    "PARTIAL LOCK");
    }
    partialWakeLock.acquire();
    Log.d(TAG, "acquireWakeLock");
    }
     */

    /**
     * Releases the currently held wake lock for this client
     *
     * @return public synchronized static void releasePartialWakeLock() {
     * if (partialWakeLock != null) {
     * partialWakeLock.release();
     * }
     * Log.d(TAG, "releaseWakeLock");
     * }
     */
/*
    // always use this in Activity, nowhere else
    public static void unlockKeyguardAndLitScreen(Activity activity) {
        activity.getWindow().addFlags(
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | // 14-SEP-16, WHY
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
    }
*/
    /*
    public static void lockKeyguard() {
        //DISCARDED, 28-Feb
        //device will get locked again automatically if set
        //so re-enable now required

        if(keyguardLock!=null && KEYGUARD_DISABLED) {
            keyguardLock.reenableKeyguard();
            KEYGUARD_DISABLED = false;
        }
    }

    public static void unlockKeyguard() {
        Context context = CaltxtApp.getCustomAppContext();

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
        KEYGUARD_DISABLED = true;
    }

    //Called whenever we need to wake up the device
    public static void wakeDevice(Context context) {
        if (fullWakeLock == null) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            fullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_DIM_WAKE_LOCK
                            | PowerManager.ACQUIRE_CAUSES_WAKEUP),
                    "FULL WAKE LOCK");
        }
        fullWakeLock.acquire();
        Log.d(TAG, "wakeDevice");
    }

    public static void releaseWakeDevice() {
        if (fullWakeLock != null && fullWakeLock.isHeld()) {
            fullWakeLock.release();
        }

        Log.d(TAG, "releaseWakeDevice");
    }
*/
    /*
        private void setInputView(IDTObject dto) {

            mFooter2Border.setVisibility(View.GONE);
            mBodyText.setVisibility(View.GONE);
            mBodyTextTimestampIcon.setVisibility(View.GONE);
            mBodyTextTimestamp.setVisibility(View.GONE);
            mBody2Text.setVisibility(View.GONE);
            mAlertText.setVisibility(View.GONE);
            mAlertImage.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            mContextEdit.setFocusableInTouchMode(true);
            mContextEdit.requestFocus();

    //		findViewById(R.id.toast_body).setVisibility(View.GONE);
            findViewById(R.id.toast_body2).setBackgroundResource(android.R.color.transparent);

            thisView.setBackgroundResource(R.drawable.calloutfrom_lightgreen_white_text);
    //		getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            if (dto.getCName().equals("XCtx")) {
    //			username = ((XCtx) dto).getUsernameCaller();
                if (Addressbook.get().isRegistered(username)) {// WHYNOTSEND
    //				setTitle("Send message");
                    mMessageButton.setVisibility(View.GONE);
                    mButtonDividerView.setVisibility(View.GONE);
    //				mCallButton.setBackgroundResource(R.drawable.ic_send_black_24dp);
    //				mCallButton.setImageResource(R.drawable.ic_send_black_24dp);
    //				mCallButton.setBackgroundColor(Color.TRANSPARENT);
                    Drawable d1 = CaltxtApp.getCustomAppContext().getResources().getDrawable(R.drawable.ic_send_black_24dp);
                    Drawable wrappedDrawable = DrawableCompat.wrap(d1);
                    wrappedDrawable = wrappedDrawable.mutate();
                    DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
                    d1.invalidateSelf();
                    mCallButton.setCompoundDrawablesWithIntrinsicBounds(null, wrappedDrawable, null, null);
                    mCallButton.setText(R.string.action_send);

                    mContextEdit.setHint(R.string.action_call_reply_hint);
                    mContextEdit.setText(((XCtx) dto).getAck());
                    mContextEdit.selectAll();
                    mHeaderText.setText(((XCtx) dto).getHeader());
                    mSubjectText.setText(Addressbook.get().getHeadline(username));
                    if(((XCtx)dto).getOccupation().length()==0){
                        mSubject2Text.setVisibility(View.GONE);
                    } else {
                        mSubject2Text.setVisibility(View.VISIBLE);
                        mSubject2Text.setText(((XCtx)dto).getOccupation());
                    }
                    mBodyText.setVisibility(View.GONE);
                } else {
                    // if callee not registered Caltxt user, close; don't send ACK
                    finish();
                    return;
                }
            } else if (dto.getCName().equals("XMob")) {
    //			username = ((XMob) dto).getUsername();
                if (Addressbook.get().isRegistered(username)) {
                    mHeaderText.setText(((XMob) dto).getHeader());
                    mSubjectText.setText(Addressbook.get().getHeadline(username));
                    if(((XMob)dto).getOccupation().length()==0){
                        mSubject2Text.setVisibility(View.GONE);
                    } else {
                        mSubject2Text.setVisibility(View.VISIBLE);
                        mSubject2Text.setText(((XMob)dto).getOccupation());
                    }
                    mBodyText.setVisibility(View.GONE);
    //				setTitle("New Call");
                } else {// callee not registered
                    // do a NORMAL CALL
                    caltxtHandler.initiateNormalCall(((XMob) dto).getNumber(), this);
                    finish();
                    return;
                }
            } else {
                finish();
                return;
            }
            // PING contact to check if offline or online
            RebootService.getConnection().submitForcedPingContact(username);
            // if user does not start typing message in 8 seconds, this window
            // disappears.
             //13-SEP-2016 : NOT REQUIRED. User will close it!
    //		new CountDownTimer(Constants.CALTXT_INPUT_TIMEOUT, 1000) {
    //			public void onTick(long millisUntilFinished) {
    //			}
    //
    //			public void onFinish() {
    //				if (!interactive)
    //					finish();
    //			}
    //		}.start();
        }
    */
    public void setToastView(long pid) {

        //get latest instance from db
        final XCtx ctx = (XCtx) Persistence.getInstance(getApplicationContext()).get(pid, "XCtx");
/*
        if (ctx.getCallState() == XCtx.IN_MESSAGE_RECEIVED
                || ctx.getCallState() == XCtx.IN_MESSAGE_REPLY) {

            Log.e(TAG, "setToastView timeout getWindow().addFlags");*/
//			wakeDevice();//USE FLAG_SHOW_WHEN_LOCKED instead, 05-DEC-16

//            unlockKeyguardAndLitScreen(this);
//        } else {

//        }

        mCallerImageStatusOverlayBorder.setBackgroundResource(R.drawable.circle_white);
        mCallerImageStatusOverlay.setBackgroundResource(addressbook.geStatusBackgroundResource(addressbook.getContact(username)));
        mCallerImageStatusOverlay.setImageResource(addressbook.getContactStatusIconResource(addressbook.getContact(username)));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String timeout_string = settings.getString(
                getString(R.string.preference_key_caltxt_timeout),
                getString(R.string.preference_value_caltxt_toast_timeout_default));
        final int timeout = Integer.parseInt(timeout_string);
        Log.d(TAG, "setToastView timeout " + timeout);
//		final String alignment = settings.getString(
//				getString(R.string.preference_key_caltxt_align),
//				getString(R.string.preference_value_caltxt_alignment_default));

        progressBar.setVisibility(View.GONE);
        mAlertText.setVisibility(View.GONE);
        mAlertImage.setVisibility(View.GONE);
        footer2frame.setVisibility(View.GONE);
        footer1frame.setVisibility(View.GONE);
        mContextEdit.setVisibility(View.GONE);

        if (ctx.getCallState() == XCtx.OUT_CALL && !ctx.isDelivered()) {
            footer2frame.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            mAlertText.setVisibility(View.VISIBLE);

        } else if (ctx.getCallState() == XCtx.IN_CALL) {

//			actionMessageAndReject(ctx);

        } else if (ctx.getCallState() == XCtx.IN_MESSAGE_RECEIVED
                || ctx.getCallState() == XCtx.IN_MESSAGE_REPLY
                || ctx.getCallState() == XCtx.IN_CALL_REPLY
                || ctx.getCallState() == XCtx.IN_CALL_REPLY_RECEIVED
                || ctx.getCallState() == XCtx.IN_CALL_REJECT_AUTOREPLY
                || ctx.getCallState() == XCtx.IN_CALL_BLOCKED
                || ctx.getCallState() == XCtx.IN_CALL_REJECT_DND
                || ctx.getCallState() == XCtx.IN_CALL_MISSED) {

            footer1frame.setVisibility(View.VISIBLE);
            openReplyAndCallToast(ctx);

        } else {

            Drawable d1 = getResources().getDrawable(R.drawable.ic_warning_white_24dp);
            Drawable wrappedDrawable = DrawableCompat.wrap(d1);
            wrappedDrawable = wrappedDrawable.mutate();
//			DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.red));
            d1.invalidateSelf();
            mAlertImage.setImageDrawable(d1);
        }

//		if((ctx.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername()))) {
        if (Addressbook.isItMe(ctx.getUsernameCaller())) {
            mSubjectText.setText(/*"+" +*/ ctx.getNumberCallee());
            XMob mob = addressbook.getRegistered(ctx.getNumberCallee());
            if (mob !=null && mob.getOccupation() != null && mob.getOccupation().length() > 0) {
                mSubject2Text.setVisibility(View.VISIBLE);
                mSubject2Text.setText(mob.getOccupation());
            } else {
                mSubject2Text.setVisibility(View.GONE);
            }
            if (ctx.getAck().length() > 0) {
                thisView.setBackgroundResource(R.drawable.calltxtto_lightblue_white_text);
//				mCancelBtn.setVisibility(View.VISIBLE);
            } else {
                thisView.setBackgroundResource(R.drawable.calltxtfrom_green_white_text);
//				mCancelBtn.setVisibility(View.GONE);
            }
        } else {
            mSubjectText.setText(/*"+" +*/ ctx.getUsernameCaller());
            if (ctx.getOccupation().length() == 0) {
                mSubject2Text.setVisibility(View.GONE);
            } else {
                mSubject2Text.setVisibility(View.VISIBLE);
                mSubject2Text.setText(ctx.getOccupation());
            }
            if (ctx.getAck().length() > 0) {
                thisView.setBackgroundResource(R.drawable.calltxtfrom_green_white_text);
//				mCancelBtn.setVisibility(View.GONE);
            } else {
                thisView.setBackgroundResource(R.drawable.calltxtto_lightblue_white_text);
//				mCancelBtn.setVisibility(View.VISIBLE);
            }
        }

//		bodyframe.setVisibility(View.VISIBLE);
        mBodyTextTimestampIcon.setVisibility(View.VISIBLE);
        mBodyTextTimestamp.setVisibility(View.VISIBLE);
        mBodyTextTimestamp.setText(ctx.getSubject(getBaseContext()));
        mBodyTextTimestampIcon.setImageResource(ctx.getSubjectIconResource());
        mHeaderText.setText(ctx.getHeader());

        if (ctx.getCallState() == XCtx.IN_MESSAGE_REPLY
                || ctx.getCallState() == XCtx.IN_MESSAGE_RECEIVED
                || ctx.getCallState() == XCtx.OUT_MESSAGE
                || ctx.getCallState() == XCtx.OUT_MESSAGE_TRIGGER
                || ctx.getCallState() == XCtx.OUT_MESSAGE_ADMIN
                || ctx.getCallState() == XCtx.OUT_MESSAGE_AD) {
            //31MAY17, no need to show message icon additionally
            mBodyTextTimestampIcon.setVisibility(View.GONE);
        }

        if (ctx.getBody().trim().length() == 0) {
            mBodyText.setVisibility(View.GONE);
        } else {
            mBodyText.setText(ctx.getBody());
        }
        if (ctx.getFooter().trim().length() == 0) {
            mBody2Text.setVisibility(View.GONE);
            body2frame.setVisibility(View.GONE);
        } else {
            mBody2Text.setText(Html.fromHtml("<b>me: </b>" + ctx.getFooter()));
            body2frame.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "getBodyPicURL " + ctx.getBodyPicURL());
//        progressBarCaltxtPhotoDownload.setVisibility(View.GONE);
//        progressBarAckPhotoDownload.setVisibility(View.GONE);
        if (ctx.getBodyPicURL().length() == 0) {
            mCaltxtPic.setVisibility(View.GONE);
//            progressBarCaltxtPhotoDownload.setVisibility(View.GONE);
//            mExpandPicButton.setVisibility(View.GONE);
        } else {
//            mCaltxtPic.setVisibility(View.GONE);
//            progressBarCaltxtPhotoDownload.setVisibility(View.VISIBLE);
//            mExpandPicButton.setVisibility(View.VISIBLE);
//            mExpandPicButton.setImageResource(R.drawable.ic_expand_less_white_24dp);
/*
            mExpandPicButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mCaltxtPic.getVisibility()==View.VISIBLE) {
                        mCaltxtPic.setVisibility(View.GONE);
                        mExpandPicButton.setImageResource(R.drawable.ic_expand_more_white_24dp);
                    } else {
                        mCaltxtPic.setVisibility(View.VISIBLE);
                        mExpandPicButton.setImageResource(R.drawable.ic_expand_less_white_24dp);
                    }
                }
            });
*/
            ImageLoader.getInstance(getApplicationContext()).DisplayImage(
                    ctx.getBodyPicURL(), mCaltxtPic,
                    200,
                    R.drawable.ic_terrain_white_24dp, false);
        }

        Log.d(TAG, "getFooterPicURL " + ctx.getFooterPicURL());
        if (ctx.getFooterPicURL().length() == 0) {
            mAckPic.setVisibility(View.GONE);
//            progressBarAckPhotoDownload.setVisibility(View.GONE);
//            mExpandPicButton.setVisibility(View.GONE);
        } else {
            mAckPic.setVisibility(View.VISIBLE);
//            progressBarAckPhotoDownload.setVisibility(View.GONE);
//            mExpandPicButton.setVisibility(View.VISIBLE);
//            mExpandPicButton.setImageResource(R.drawable.ic_expand_less_white_24dp);

            /*mExpandPicButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mCaltxtPic.getVisibility()==View.VISIBLE) {
                        mCaltxtPic.setVisibility(View.GONE);
                        mExpandPicButton.setImageResource(R.drawable.ic_expand_more_white_24dp);
                    } else {
                        mCaltxtPic.setVisibility(View.VISIBLE);
                        mExpandPicButton.setImageResource(R.drawable.ic_expand_less_white_24dp);
                    }
                }
            });*/

            ImageLoader.getInstance(getApplicationContext()).DisplayImage(
                    ctx.getFooterPicURL(), mAckPic,
                    200,
                    R.drawable.ic_terrain_white_24dp, false);
        }

		/*this shows toast every 1 sec to last until CALTXT_TOAST_TIMEOUT sec*/
        timer = new CountDownTimer(timeout * 1000, 1000) {
            boolean isred = true;
            boolean toggle = true;

            public void onTick(long millisUntilFinished) {

                toggle = !toggle;

                if (ctx.getCallPriority() == XCtx.PRIORITY_EMERGENCY) {
//    				if((ctx.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername()))) {
                    if (Addressbook.isItMe(ctx.getUsernameCaller())) {
                        if (ctx.getAck().length() == 0) {
                            if (isred) {
                                isred = false;
                                thisView.setBackgroundResource(R.drawable.calltxtfrom_red_white_text);
                            } else {
                                isred = true;
                                thisView.setBackgroundResource(R.drawable.calltxtfrom_green_white_text);
                            }
                        } else {
                            if (isred) {
                                isred = false;
                                thisView.setBackgroundResource(R.drawable.calltxtto_red_white_text);
                            } else {
                                isred = true;
                                thisView.setBackgroundResource(R.drawable.calltxtto_lightblue_white_text);
                            }
                        }
                    } else {
                        if (ctx.getAck().length() == 0) {
                            if (isred) {
                                isred = false;
                                thisView.setBackgroundResource(R.drawable.calltxtto_red_white_text);
                            } else {
                                isred = true;
                                thisView.setBackgroundResource(R.drawable.calltxtto_lightblue_white_text);
                            }
                        } else {
                            if (isred) {
                                isred = false;
                                thisView.setBackgroundResource(R.drawable.calltxtfrom_red_white_text);
                            } else {
                                isred = true;
                                thisView.setBackgroundResource(R.drawable.calltxtfrom_green_white_text);
                            }
                        }
                    }
                }
            }

            public void onFinish() {
                finish();
            }
        };

        timer.start();
    }

    private void openReplyAndCallToast(final XCtx ctx) {
		/* tint drawable */
        Drawable d1 = getResources().getDrawable(R.drawable.ic_reply_black_24dp);
        Drawable wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mMessageButton.setCompoundDrawablesWithIntrinsicBounds(null, d1, null, null);
        mMessageButton.setText(R.string.action_reply);
//		mMessageButton.setImageResource(R.drawable.ic_reply_white_24dp);
        mMessageButton.setOnClickListener(new OnClickListener() {
            boolean clickTypeSendMsg = false;

            @Override
            public void onClick(View v) {
                if (clickTypeSendMsg) {
                    final String edittext = mContextEdit.getEditableText().toString();
                    if (edittext.length() > 0) {
                        if (ctx.getAck().length() > 0) {
                            XMob mob = addressbook.getRegistered(ctx.getUsernameCaller());
//		        			if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                            if (Addressbook.isItMe(ctx.getNumberCallee())) {
                                mob = addressbook.getRegistered(ctx.getUsernameCaller());
                            } else {
                                mob = addressbook.getRegistered(ctx.getNumberCallee());
                            }

                            CaltxtHandler.get(CaltxtToast.this).initiateMessage(mob, mContextEdit.getEditableText().toString(), "", XCtx.PRIORITY_NORMAL);
                        } else {
                            CaltxtHandler.get(CaltxtToast.this).initiateMessageReply(ctx, mContextEdit.getEditableText().toString(), "");
                        }
                        Log.e(TAG, "clickTypeSendMsg " + ctx);
                    }

                    finish();
                } else {
                    Drawable d1 = getResources().getDrawable(R.drawable.ic_call_black_24dp);
                    Drawable wrappedDrawable = DrawableCompat.wrap(d1);
                    wrappedDrawable = wrappedDrawable.mutate();
                    DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
                    d1.invalidateSelf();
                    mCallButton.setCompoundDrawablesWithIntrinsicBounds(null, d1, null, null);
                    mCallButton.setText(R.string.action_call);
                    mCallButton.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            final String edittext = mContextEdit.getEditableText().toString();
                            XMob mob = addressbook.getRegistered(ctx.getUsernameCaller());
//		        			if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                            if (Addressbook.isItMe(ctx.getNumberCallee())) {
                                mob = addressbook.getRegistered(ctx.getUsernameCaller());
                            } else {
                                mob = addressbook.getRegistered(ctx.getNumberCallee());
                            }

                            CaltxtHandler.get(CaltxtToast.this).initiateContextCall(mob, edittext, "", XCtx.PRIORITY_NORMAL);
//                            finish();
                        }
                    });
                    mCallButton.setOnLongClickListener(new OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            final String edittext = mContextEdit.getEditableText().toString();
                            XMob mob = addressbook.getRegistered(ctx.getUsernameCaller());
//		        			if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                            if (Addressbook.isItMe(ctx.getNumberCallee())) {
                                mob = addressbook.getRegistered(ctx.getUsernameCaller());
                            } else {
                                mob = addressbook.getRegistered(ctx.getNumberCallee());
                            }

                            CaltxtHandler.get(CaltxtToast.this).initiateContextCall(mob, edittext, "", XCtx.PRIORITY_EMERGENCY);
//                            finish();
                            return true;
                        }
                    });

                    body2frame.setVisibility(View.VISIBLE);
                    body2frame.setBackgroundResource(android.R.color.transparent);
                    mContextEdit.setVisibility(View.VISIBLE);
                    mContextEdit.setFocusableInTouchMode(true);
                    mContextEdit.requestFocus();
                    d1 = getResources().getDrawable(R.drawable.ic_send_black_24dp);
					/* tint drawable */
                    wrappedDrawable = DrawableCompat.wrap(d1);
                    wrappedDrawable = wrappedDrawable.mutate();
                    DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
                    d1.invalidateSelf();
                    mMessageButton.setCompoundDrawablesWithIntrinsicBounds(null, d1, null, null);
                    mMessageButton.setText(R.string.action_send);
//					mMessageButton.setImageResource(R.drawable.ic_send_black_24dp);
                    clickTypeSendMsg = true;
                    timer.cancel();
                    getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);/* soft keyboard open*/
                }
            }
        });

        d1 = getResources().getDrawable(R.drawable.ic_call_black_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mCallButton.setCompoundDrawablesWithIntrinsicBounds(null, d1, null, null);
        mCallButton.setText(R.string.action_call);
        mCallButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                XMob mob = addressbook.getRegistered(ctx.getUsernameCaller());
//    			if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                if (Addressbook.isItMe(ctx.getNumberCallee())) {
                    mob = addressbook.getRegistered(ctx.getUsernameCaller());
                } else {
                    mob = addressbook.getRegistered(ctx.getNumberCallee());
                }

                CaltxtHandler.get(CaltxtToast.this).initiateContextCall(mob,
                        (ctx.getAck().length() == 0 ? (ctx.getCaltxt().length() == 0 ? "" : "RE: " + ctx.getCaltxt()) : "RE: " + ctx.getAck()),
                        "", XCtx.PRIORITY_NORMAL);
//                finish();
            }
        });
        mCallButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                XMob mob = addressbook.getRegistered(ctx.getUsernameCaller());
//    			if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                if (Addressbook.isItMe(ctx.getNumberCallee())) {
                    mob = addressbook.getRegistered(ctx.getUsernameCaller());
                } else {
                    mob = addressbook.getRegistered(ctx.getNumberCallee());
                }

                caltxtHandler.initiateContextCall(mob,
                        (ctx.getAck().length() == 0 ? (ctx.getCaltxt().length() == 0 ? "" : "RE: " + ctx.getCaltxt()) : "RE: " + ctx.getAck()),
                        "", XCtx.PRIORITY_EMERGENCY);
//                finish();
                return true;
            }
        });
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        final String propertyName = event.getPropertyName();
        Log.d(TAG, "propertyChange propertyName " + propertyName);

        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                if (propertyName.equals(Constants.messageArrivedProperty)
                        || propertyName.equals(Constants.ConnectionStatusProperty)) {

                    XMob mob = addressbook.getRegistered(username);
                    if (mob == null) {
                        mob = new XMob();
                        mob.setUsername(username);
                    }
                    mCallerImageStatusOverlayBorder.setBackgroundResource(R.drawable.circle_white);
                    mCallerImageStatusOverlay.setBackgroundResource(addressbook.geStatusBackgroundResource(mob));
                    mCallerImageStatusOverlay.setImageResource(addressbook.getContactStatusIconResource(mob));
//			        mCallerImageStatusOverlay.setImageDrawable(Addressbook.get().getContactStatusOverlayIconResource(username));
                    mHeaderText.setText(mob.getName());
                    mSubjectText.setText(mob.getHeadline());
                    if (mob != null && mob.getOccupation().length() > 0) {
                        mSubject2Text.setVisibility(View.VISIBLE);
                        mSubject2Text.setText(mob.getOccupation());
                    } else {
                        mSubject2Text.setVisibility(View.GONE);
                    }

                    if (addressbook.isRegisteredAndConnected(username)) {
                        mFooter2Border.setVisibility(View.GONE);
                        mAlertImage.setVisibility(View.GONE);
                        mAlertText.setVisibility(View.GONE);
                        mAlertText.setText("");
                    } else if (Settings.isSMSEnabled(CaltxtToast.this)) {
                    } else {
                    }
                } else if (
                    // All except "Publishing" events are ignored to initiate a normal call
//                        propertyName.equals(Constants.mqttPublishedProperty)
                        propertyName.equals(Constants.mqttPublishingProperty)
//                        || propertyName.equals(Constants.smsPublishedProperty)
                                || propertyName.equals(Constants.smsPublishingProperty)
//                        || propertyName.equals(Constants.firebasePublishedProperty)
                                || propertyName.equals(Constants.firebasePublishingProperty)
//                        || propertyName.equals(Constants.messageErrorProperty)
                        ) {

                    if (XCtx.isXCtx(event.getOldValue().toString())) {
                        footer2frame.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        mAlertText.setVisibility(View.VISIBLE);
                        mAlertText.setText(R.string.info_delivering_caltxt);
//                        mAlertText.setText("Sending text");
                    } else {
                        footer2frame.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        mAlertText.setVisibility(View.GONE);
                        mAlertText.setText("");
                    }
                    // 01-JUL-17, propertyChangeListner is registered only when outgoing call initiated
                    // 01-JUL-17, uncommented initiateNormalCall. call will not be initiated in scenario except outgoing call
                    if (XCtx.isXCtx(event.getNewValue().toString())) {
                        XCtx ctx = new XCtx();
                        ctx.init(event.getNewValue().toString());
                        final XCtx ctxx = CaltxtHandler.get(CaltxtToast.this).peekCaltxt(Constants.CALL_TYPE_OUTBOUND, XMob.toFQMN(ctx.getNumberCallee(), Addressbook.getMyCountryCode()));
                        // commented below 19-APR-17: call is initiated from callButton OnClick, not here
                        // uncommented 01-JUL-17, as propertyListener will come here only for outgoing calls
                        // and not for missed calls
                        if (ctxx != null && XMob.toFQMN(ctxx.getNumberCallee(), Addressbook.getMyCountryCode())
                                .equals(XMob.toFQMN(ctx.getNumberCallee(), Addressbook.getMyCountryCode()))
                                && ctx.getCallState() == XCtx.OUT_CALL) {
                            // start call in one second
                            // commented 13-JUL-17, the CountDownTimer, why wait for 1 seconds??
                            /*CountDownTimer tymr = new CountDownTimer(1000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }

                                public void onFinish() {
                                    if (propertyName.equals(Constants.messageErrorProperty)) {
//								Notify.toast(Globals.getCustomAppContext(),
//										ctxx.getNameCallee()+" not reachable. Try after sometime",
//										Toast.LENGTH_LONG);
                                    } else {
                                    }*/
                            Log.d(TAG, "propertyChange initiateNormalCall " + ctxx);
                            CaltxtHandler.get(CaltxtToast.this).initiateNormalCall(
                                    Addressbook.getInstance(getApplicationContext()).get(ctxx.getNumberCallee()).getNumber(),
                                    CaltxtToast.this);
                                /*}
                            };
                            tymr.start();*/
                        }
                        finish();
                    }
                } else if (propertyName.equals(Constants.mqttDeliveredProperty)
                        || propertyName.equals(Constants.smsDeliveredProperty)
                        || propertyName.equals(Constants.firebaseDeliveredProperty)
                        || propertyName.equals(Constants.messageErrorProperty)) {
                    if (XCtx.isXCtx(event.getNewValue().toString())) {
                        footer2frame.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        mAlertText.setVisibility(View.GONE);

                    }
                } else if (propertyName.equals(Constants.callIncomingEndProperty)
                        || propertyName.equals(Constants.callIncomingAnsweredProperty)
                        || propertyName.equals(Constants.callOutgoingEndProperty)) {
//			footer1frame.setVisibility(View.VISIBLE);
                    finish();
                } else if (propertyName.equals(Constants.callIncomingMissedProperty)) {
                    Log.d(TAG, "BLOCKTEST callIncomingMissedProperty");
/*			footer2frame.setVisibility(View.VISIBLE);
			mFooter2Border.setVisibility(View.VISIBLE);
			mAlertImage.setVisibility(View.VISIBLE);
			mAlertText.setVisibility(View.VISIBLE);*/
                    finish();
                } else if (propertyName.equals(Constants.callOutgoingStartProperty)) {
                    CountDownTimer timer = new CountDownTimer(3000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
			    	/* close after 3 seconds since we cannot know when call is answered */
                            finish();
                        }
                    };
                    timer.start();
                }
            }
        }));
    }

/*
    private void actionMessageAndReject(final XCtx ctx) {

        Drawable d1 = getResources().getDrawable(R.drawable.ic_call_end_black_24dp);
        Drawable wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mCallButton.setCompoundDrawablesWithIntrinsicBounds(null, d1, null, null);
        mCallButton.setText(R.string.action_decline);
        mCallButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

//				String username;
//    			if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
//				if(Addressbook.isItMe(ctx.getUsernameCallee())) {
//        			Blockbook.get().add(username=ctx.getUsernameCaller(), ctx.getNameCaller());
//    			} else {
//        			Blockbook.get().add(username=ctx.getUsernameCallee(), ctx.getNameCallee());
//    			}
//				RebootService.getConnection().addAction(Constants.contactBlockedProperty, username, null);

                ctx.setCallState(XCtx.IN_CALL_REPLY);
                Logbook.get(getApplicationContext()).update(ctx);
                CallManager.getInstance().endCall(getApplicationContext());
                finish();
            }
        });

        d1 = getResources().getDrawable(R.drawable.ic_reply_black_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mMessageButton.setCompoundDrawablesWithIntrinsicBounds(null, d1, null, null);
        mMessageButton.setText(R.string.action_message);
        mMessageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CallManager.getInstance().endCall(getApplicationContext());

                //if is contact OR context call, show Acknowledgment window
//				if(Addressbook.get().isContact(ctx.getUsernameCaller()) || ctx.isCaltxtCall()) {
                Intent ackList = new Intent(CaltxtToast.this, AcknowledgementList.class);
                ackList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ackList.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                ackList.putExtra("IDTOBJECT", ctx);
                startActivity(ackList);
//				}

                finish();
            }
        });

    }
*/
}
