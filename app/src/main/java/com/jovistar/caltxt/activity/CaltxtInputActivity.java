package com.jovistar.caltxt.activity;

import android.annotation.SuppressLint;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.firebase.storage.DownloadService;
import com.jovistar.caltxt.firebase.storage.UploadService;
import com.jovistar.caltxt.images.ImageLoader;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.data.ConnectivityBroadcastReceiver;
import com.jovistar.caltxt.network.voice.CaltxtHandler;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Logbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

//import android.support.annotation.NonNull;
//import androidx.core.content.LocalBroadcastManager;

@SuppressLint("NewApi")
public class CaltxtInputActivity extends AppCompatActivity implements PropertyChangeListener {
    private static final String TAG = "CaltxtInputActivity";

    //	int viewType = Constants.INPUT_VIEW;
    private static final int RC_TAKE_PICTURE = 101;
    private static final int CAMERA_REQUEST = 102;
    static KeyguardLock keyguardLock = null;
    int finalHeight = 0, finalWidth = 0;
    boolean interactive = false;
    CaltxtHandler caltxtHandler = CaltxtHandler.get(this);
    String nnumber;// number to dial
    // username to fetch profile picture. will be used for dual sim profiles
    // dual sim profile will have same username (primary number) for both numbers
    String username;
    View thisView;
    ListView readyTextListView;
    AppCompatImageButton imageButton;
    //	androidx.appcompat.widget.AppCompatImageButton mCancelBtn;
    View bodyframe = null;
    //	View body2frame = null;
    View footer1frame = null;
    //	View footer2frame = null;
//	ProgressBar progressBar;
    IDTObject dto = null;
    private CountDownTimer timer;
    private boolean FIREBASE_SIGNED_IN_STATUS = false;
    private FirebaseAuth mAuth;
    private EditText mContextEdit;
    private ImageView mCallerImage, mStatusIcon;
    //	private ImageView mCallerImageStatusOverlay, mCallerImageStatusOverlayBorder;
    private ImageView /*mAlertImage, */mBodyTextTimestampIcon;
    private TextView mHeaderText;
    private ProgressBar progressBar;
    //	private TextView mSubjectText;
    private TextView mSubject2Text;
    //	private TextView mBodyText;
//	private TextView mBody2Text;
//	private TextView mBodyTextTimestamp;
    private TextView mAlertText;
    private View /*mFooter2Border, */mButtonDividerView;
    private AppCompatButton mCallButton, mMessageButton;
    private Uri mDownloadUri = null;
    private Uri mFileUri = null;
    private String mDownloadName = null;
    //    private ProgressDialog mProgressDialog;
    // Local broadcast receiver
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive:" + intent);
            hideProgressDialog();

            switch (intent.getAction()) {
                case DownloadService.DOWNLOAD_COMPLETED:
                    // Get number of bytes downloaded
                    long numBytes = intent.getLongExtra(DownloadService.EXTRA_BYTES_DOWNLOADED, 0);

                    // Alert success
                    showMessageDialog(getString(R.string.download_success), String.format(Locale.getDefault(),
                            "%d bytes downloaded from %s",
                            numBytes,
                            intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH)));
                    break;
                case DownloadService.DOWNLOAD_ERROR:
                    // Alert failure
                    showMessageDialog("Error", String.format(Locale.getDefault(),
                            "Failed to download from %s",
                            intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH)));
                    break;
                case UploadService.UPLOAD_COMPLETED:
                case UploadService.UPLOAD_ERROR:
                    onUploadResultIntent(intent);
                    break;
            }
        }
    };

    private void onUploadResultIntent(Intent intent) {
        progressBar.setVisibility(View.GONE);
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUri = intent.getParcelableExtra(UploadService.EXTRA_DOWNLOAD_URL);
        mFileUri = intent.getParcelableExtra(UploadService.EXTRA_FILE_URI);
        mDownloadName = intent.getStringExtra(UploadService.EXTRA_DOWNLOAD_NAME);

        Log.d(TAG, "onReceive: UPLOAD_COMPLETED downloadUrl " + mDownloadUri);
        Log.d(TAG, "onReceive: UPLOAD_COMPLETED fileUri " + mFileUri);
        Log.d(TAG, "onReceive: UPLOAD_COMPLETED downloadName " + mDownloadName);

        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser user) {
        if (mFileUri != null) {
            imageButton.setImageURI(mFileUri);
        }

        // Signed in or Signed out
        if (user != null) {
//            findViewById(R.id.layout_signin).setVisibility(View.GONE);
//            findViewById(R.id.layout_storage).setVisibility(View.VISIBLE);
        } else {
//            findViewById(R.id.layout_signin).setVisibility(View.VISIBLE);
//            findViewById(R.id.layout_storage).setVisibility(View.GONE);
        }

        // Download URL and Download button
        if (mDownloadUri != null) {
//            ((TextView) findViewById(R.id.picture_download_uri))
//                    .setText(mDownloadUri.toString());
//            findViewById(R.id.layout_download).setVisibility(View.VISIBLE);
        } else {
//            ((TextView) findViewById(R.id.picture_download_uri))
//                    .setText(null);
//            findViewById(R.id.layout_download).setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, DownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, UploadService.getIntentFilter());
    }

    private void uploadFromBitmap(Bitmap bitmap) {
        // store file
        File file = ImageLoader.getInstance(getApplicationContext()).getFile("Camera"
                + Calendar.getInstance().getTimeInMillis() + ".jpg", 0);
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            //copy selected image stream to file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();
            fos = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        uploadFromUri(Uri.fromFile(file));
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        updateUI(mAuth.getCurrentUser());
        mDownloadUri = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, UploadService.class)
                .putExtra(UploadService.EXTRA_FILE_URI, fileUri)
                .setAction(UploadService.ACTION_UPLOAD));

        progressBar.setVisibility(View.VISIBLE);
        // Show loading spinner
//        showProgressDialog(getString(R.string.progress_uploading));
    }

    private void beginDownload(String uri) {
        // Get path
//        String path = "photos/" + mFileUri.getLastPathSegment();

        // Kick off DownloadService to download the file
        Intent intent = new Intent(this, DownloadService.class)
                .putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, uri)
                .setAction(DownloadService.ACTION_DOWNLOAD);
        startService(intent);

        // Show loading spinner
//        showProgressDialog(getString(R.string.progress_downloading));
    }

    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        if (!Addressbook.getInstance(getBaseContext()).isRegistered(/*nnumber*/username)) {
            // not possible for unregistered contact
            Toast.makeText(this, "Please invite to join Caltxt first", Toast.LENGTH_SHORT).show();
            return;
        } else if (!ConnectivityBroadcastReceiver.haveNetworkConnection()) {
            // not connected, cannot send photo
            Toast.makeText(this, "Please check Internet connection", Toast.LENGTH_SHORT).show();
            return;
        } else if (FIREBASE_SIGNED_IN_STATUS = false) {
            // not signed in, cannot send photo
            Toast.makeText(this, "Please try after sometime", Toast.LENGTH_SHORT).show();
            return;
        }

        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(CaltxtInputActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    // Open camera for image
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else if (items[item].equals("Choose from Library")) {
                    // Pick an image from storage
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, RC_TAKE_PICTURE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                mFileUri = data.getData();

                if (mFileUri != null) {
                    uploadFromUri(mFileUri);
                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else {
//                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                uploadFromBitmap(bitmap);
            } else {
//                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
/*
    private void getCurrentUser() {
        // Sign in anonymously. Authentication is required to read or write from Firebase Storage.
//        showProgressDialog(getString(R.string.progress_auth));
        mAuth.signInAnonymously()
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "signInAnonymously:SUCCESS");
                        hideProgressDialog();
                        updateUI(authResult.getUser());
                        FIREBASE_SIGNED_IN_STATUS = true;
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "signInAnonymously:FAILURE", exception);
                        hideProgressDialog();
                        updateUI(null);
                        FIREBASE_SIGNED_IN_STATUS = false;
                    }
                });
    }
*/
    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }

//    private void showProgressDialog(String caption) {
        /*if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.setMessage(caption);
        mProgressDialog.show();*/
//		progressBar.setVisibility(View.VISIBLE);
//    }

    private void hideProgressDialog() {
        /*if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }*/
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.caltxt_input);

        Connection.get().registerChangeListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
//		toolbar.getBackground().setAlpha(0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Prepare your call");

//	    viewType = getIntent().getIntExtra("VIEW", 0);
        dto = (IDTObject) getIntent().getSerializableExtra("IDTOBJECT");

        if (dto.getCName().equals("XCtx")) {
            nnumber = username = ((XCtx) dto).getUsernameCaller();
//			if(Addressbook.getMyProfile().getUsername().equals(nnumber)
//					|| XMob.toFQMN(Addressbook.getMyProfile().getNumber2(), CaltxtApp.getMyCountryCode()).equals(nnumber)) {
            if (Addressbook.isItMe(nnumber)) {
                nnumber = ((XCtx) dto).getNumberCallee();
            }
        } else if (dto.getCName().equals("XMob")) {
            nnumber = ((XMob) dto).getNumber();
            username = ((XMob) dto).getUsername();
//			nnumber = ((XMob) dto).getUsername();
        }

        Log.d(TAG, "CaltxtInputActivity:OnCreate " + username);
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);

        interactive = false;

        progressBar = findViewById(R.id.caltxt_image_progress_bar);
        imageButton = findViewById(R.id.caltxt_image);
        mStatusIcon = findViewById(R.id.status_icon);
//		progressBar = (ProgressBar) findViewById(R.id.toast_progress_bar);
        mCallerImage = findViewById(R.id.caltxt_person_img);
//		mAlertImage = (ImageView) findViewById(R.id.toast_alert_alert_img);
//		mCallerImageStatusOverlay = (ImageView) findViewById(R.id.caltxt_person_img_overlay);
//		mCallerImageStatusOverlayBorder = (ImageView) findViewById(R.id.caltxt_person_img_overlay_border);
        mHeaderText = findViewById(R.id.toast_head_text);
//		mCancelBtn = (AppCompatImageButton) findViewById(R.id.toast_head_cancel_img);
//		mSubjectText = (TextView) findViewById(R.id.toast_head_subject_text);
        mSubject2Text = findViewById(R.id.toast_head_subject2_text);
//		mFooter2Border = findViewById(R.id.toast_alert_border);
//		footer2frame = (View) findViewById(R.id.toast_alert);
        footer1frame = findViewById(R.id.toast_footer);
//		mBodyText = (TextView) findViewById(R.id.toast_body_text);
//		mBody2Text = (TextView) findViewById(R.id.toast_body2_text);
//		mBodyTextTimestamp = (TextView) findViewById(R.id.toast_body_text_time);
//		mBodyTextTimestampIcon = (ImageView) findViewById(R.id.toast_body_text_time_icon);

        mCallButton = findViewById(R.id.toast_footer_call_img);

        progressBar.setVisibility(View.GONE);

		/* tint drawable mCallButton */
        Drawable d1 = getResources().getDrawable(R.drawable.ic_call_black_24dp);
        Drawable wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.darkgreen));
        d1.invalidateSelf();
        mCallButton.setCompoundDrawablesWithIntrinsicBounds(null, wrappedDrawable, null, null);

        ArrayList<String> list = caltxtHandler.getCaltxtHistory();
        if (list.isEmpty()) {
            caltxtHandler.addToCaltxtHistory("Please join the meeting now!");
            caltxtHandler.addToCaltxtHistory("Weekend plan?!");
            caltxtHandler.addToCaltxtHistory("Lets go out!");
            caltxtHandler.addToCaltxtHistory("Hey! be quick, you are missing the fun!");
            caltxtHandler.addToCaltxtHistory("Hi! This is " + Addressbook.getInstance(getApplicationContext()).getMyProfile().getName()
                    + (Addressbook.getInstance(getApplicationContext()).getMyProfile().getOccupation().length() > 0 ?
                    " (" + Addressbook.getInstance(getApplicationContext()).getMyProfile().getOccupation() + ")" :
                    "") + ", lets talk?!");
            caltxtHandler.addToCaltxtHistory("Urgent, please answer!");
            caltxtHandler.addToCaltxtHistory("When are you coming back?!");
            caltxtHandler.addToCaltxtHistory("Long time, lets catch up!");

            list = caltxtHandler.getCaltxtHistory();
        }
        readyTextListView = findViewById(R.id.listview);
        final StableArrayAdapter adapter = new StableArrayAdapter(
                this,
                R.layout.acknowledgement_list_item,
                list);
        readyTextListView.setAdapter(adapter);
/*		26-JAN-17 status icon is set in PropertyChangeListener adt
        if (dto.getCName().equals("XMob")) {
			mStatusIcon.setBackgroundResource(Addressbook.getInstance(getApplicationContext()).geStatusBackgroundResource((XMob)dto));
			mStatusIcon.setImageResource(Addressbook.getInstance(getApplicationContext()).getContactStatusIconResource((XMob)dto));
		} else if(dto.getCName().equals("XCtx")) {
			XMob m = Addressbook.getInstance(getApplicationContext()).getRegistered(username);
			mStatusIcon.setBackgroundResource(Addressbook.getInstance(getApplicationContext()).geStatusBackgroundResource(m));
			mStatusIcon.setImageResource(Addressbook.getInstance(getApplicationContext()).getContactStatusIconResource(m));
		}
*/
        // mCallButton.setBackgroundResource(android.R.drawable.btn_default);
        mMessageButton = findViewById(R.id.toast_footer_send_img);

		/* tint drawable mMessageButton */
        d1 = getResources().getDrawable(R.drawable.ic_send_black_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.darkgreen));
        d1.invalidateSelf();
        mMessageButton.setCompoundDrawablesWithIntrinsicBounds(null, wrappedDrawable, null, null);

        mButtonDividerView = findViewById(R.id.button_divider);
        mAlertText = findViewById(R.id.toast_alert);
        mContextEdit = findViewById(R.id.toast_body2_edit);
        bodyframe = findViewById(R.id.toast_body);
//		body2frame = (View) findViewById(R.id.toast_body2);
        footer1frame = findViewById(R.id.toast_footer);
//		footer2frame.setVisibility(View.GONE);
        mAlertText.setTextColor(getResources().getColor(R.color.lightyellow));

		/* tint drawable mContextEdit */
        d1 = getResources().getDrawable(R.drawable.ic_clear_white_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(android.R.color.transparent));
        d1.invalidateSelf();
        mContextEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, wrappedDrawable, null);

//		mContextEdit.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        thisView = getWindow().getDecorView();
/*
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line,
				caltxtHandler.getCaltxtHistory());
		// "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789!?@$+-=%#*().,/:;[]~"
		mContextEdit.setAdapter(adapter1);
*/
/*		mCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
*/
		/* below few lines to capture imageview h and w */
        ViewTreeObserver vto = mCallerImage.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mCallerImage.getViewTreeObserver().removeOnPreDrawListener(this);
                finalHeight = mCallerImage.getMeasuredHeight();
                finalWidth = mCallerImage.getMeasuredWidth();
                ImageLoader.getInstance(getApplicationContext()).DisplayImage(
                        XMob.toFQMN(username, Addressbook.getMyCountryCode()) + XMob.IMAGE_FILE_EXTN, mCallerImage,
                        finalWidth/* Constants.LIST_IMG_HEIGHT */,
                        R.drawable.ic_person_white_24dp, true);

                return true;
            }
        });

        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
		/*XMob mob = Addressbook.get().getContact(username);
		if(mob==null) {
			mob = new XMob();
			mob.setUsername(username);
		}*/
//		mCallerImageStatusOverlayBorder.setBackgroundResource(android.R.color.transparent);
//		mCallerImageStatusOverlay.setBackgroundResource(android.R.color.transparent);
//		mCallerImageStatusOverlay.setImageResource(0);
//		mCallerImageStatusOverlay.setImageDrawable(Addressbook.get().getContactStatusOverlayIconResource((username)));
        mCallButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String edittext = mContextEdit.getEditableText().toString();
                if (dto.getCName().equals("XCtx")) {// Call is disconnected and reply message is being sent
                    if (edittext.length() > 0) {
                        CaltxtHandler.get(CaltxtInputActivity.this).addToCaltxtHistory(edittext);

                        ((XCtx) dto).setCallState(XCtx.IN_CALL_REPLY);
                        ((XCtx) dto).setAck(edittext);
                        Logbook.get(getApplicationContext()).update(((XCtx) dto));
                        caltxtHandler.sendXCtxReply((XCtx) dto, edittext);
						/* DISABLE SMS for now 26/MAR/2016
						if (Addressbook.get().isCaltxtContactAndConnected(username)) {
							caltxtHandler.sendReplyXCtx((XCtx) dto, edittext);
						} else if (Addressbook.get().isCaltxtContact(username)
								&& Settings.delivery_by_sms) {
							caltxtHandler.sendReplyXCtxBySMS(((XCtx) dto).getUsernameCaller(),
									(XCtx) dto, edittext);
						}*/
                    }
                } else if (dto.getCName().equals("XMob")) {
                    caltxtHandler.initiateContextCall((XMob) dto, edittext, mDownloadUri == null ? null : mDownloadUri.toString(), XCtx.PRIORITY_EMERGENCY);
                }
//				finish();
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
                        CaltxtHandler.get(CaltxtInputActivity.this).addToCaltxtHistory(edittext);

                        ((XCtx) dto).setCallState(XCtx.IN_CALL_REPLY);
                        ((XCtx) dto).setAck(edittext);
                        Logbook.get(getApplicationContext()).update(((XCtx) dto));
                        caltxtHandler.sendXCtxReply((XCtx) dto, edittext);
						/* DISABLE SMS for now 26/MAR/2016
						if (Addressbook.get().isCaltxtContactAndConnected(username)) {
							caltxtHandler.sendReplyXCtx((XCtx) dto, edittext);
						} else if (Addressbook.get().isCaltxtContact(username)
								&& Settings.delivery_by_sms) {
							caltxtHandler.sendReplyXCtxBySMS(
									((XCtx) dto).getUsernameCaller(),
									(XCtx) dto, edittext);
						}*/
                    }
                } else if (dto.getCName().equals("XMob")) {// New Call with a message is being made
                    caltxtHandler.initiateContextCall((XMob) dto, edittext, mDownloadUri == null ? null : mDownloadUri.toString(), XCtx.PRIORITY_NORMAL);
                }
//				finish();
            }
        });
        mMessageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mDownloadUri == null && mContextEdit.getText().length() == 0) {
                    Notify.toast(mMessageButton, getBaseContext(),
                            "Nothing to send",
                            Toast.LENGTH_LONG);
                    return;
                }

                // remove listener so that call is not initiated in PropertyChangeListener
//				RebootService.getConnection(getApplicationContext()).removeChangeListener(CaltxtInputActivity.this);

                final String edittext = mContextEdit.getEditableText()
                        .toString();
                if (dto.getCName().equals("XMob")) {// New message is being made
                    CaltxtHandler.get(CaltxtInputActivity.this).addToCaltxtHistory(edittext);

                    if (edittext.length() > 0 || mDownloadUri != null) {
                        caltxtHandler.initiateMessage((XMob) dto, edittext, mDownloadUri == null ? null : mDownloadUri.toString(), XCtx.PRIORITY_NORMAL);
                    }
                } else if (dto.getCName().equals("XCtx")) {// reply message is
                    // being made
                    if (edittext.length() > 0 || mDownloadUri != null)
                        caltxtHandler.initiateMessageReply((XCtx) dto, edittext, mDownloadUri == null ? null : mDownloadUri.toString());
                }
                //09-Dec-2018, commented, wrong property sent at this time (not yet delivered, its just sent)
                //Connection.get().addAction(Constants.firebaseDeliveredProperty, null, null);
                finish();
            }
        });

        mContextEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mContextEdit.getRight() - mContextEdit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        mContextEdit.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
        mContextEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
					/* tint drawable mContextEdit */
                    Drawable d1 = getResources().getDrawable(R.drawable.ic_clear_white_24dp);
                    Drawable wrappedDrawable = DrawableCompat.wrap(d1);
                    wrappedDrawable = wrappedDrawable.mutate();
                    DrawableCompat.setTint(wrappedDrawable, getResources().getColor(android.R.color.transparent));
                    d1.invalidateSelf();
                    mContextEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, wrappedDrawable, null);
                } else {
					/* tint drawable mContextEdit */
                    Drawable d1 = getResources().getDrawable(R.drawable.ic_clear_white_24dp);
                    Drawable wrappedDrawable = DrawableCompat.wrap(d1);
                    wrappedDrawable = wrappedDrawable.mutate();
                    DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
                    d1.invalidateSelf();
                    mContextEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, wrappedDrawable, null);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                interactive = true;
                String str = s.toString();
                if (str.contains("^")) {
                    mContextEdit.setText(str.replace("^", ""));
                    return;
                }
                if (mContextEdit.getText().length() > 0) {
                    if (Addressbook.getInstance(getApplicationContext()).isRegisteredAndConnected(nnumber)) {
                        mAlertText.setText(R.string.input_text_alert_title);
//					} else if (Settings.delivery_by_sms) {
                    } else if (Settings.isSMSEnabled(CaltxtInputActivity.this)) {
//						footer2frame.setVisibility(View.VISIBLE);//to make alert invisible (all caltxt sent via data only)
//						mFooter2Border.setVisibility(View.VISIBLE);
//						mAlertText.setVisibility(View.VISIBLE);
//						mAlertImage.setVisibility(View.VISIBLE);
                        mAlertText.setText(R.string.input_text_sms_disclaimer);
                    } else {
//						footer2frame.setVisibility(View.VISIBLE);//to make alert invisible (all caltxt sent via data only)
//						mFooter2Border.setVisibility(View.VISIBLE);
//						mAlertText.setVisibility(View.VISIBLE);
//						mAlertImage.setVisibility(View.VISIBLE);
                        // caltxt will be sent via Firebase
                        // if user not registered, text will not be sent
                        if (!Addressbook.getInstance(getApplicationContext()).isRegistered(nnumber)) {
                            mAlertText.setText(R.string.input_text_cant_send_disclaimer);
                        }
//						mAlertText.setTextColor(Color.RED);
                    }
                } else if (mContextEdit.getText().length() == 0) {
//					footer2frame.setVisibility(View.GONE);
//					mFooter2Border.setVisibility(View.GONE);
//					mAlertImage.setVisibility(View.GONE);
//					mAlertText.setVisibility(View.GONE);
//					mAlertText.setText("");
                }
            }
        });

//		if(viewType==Constants.INPUT_VIEW) {
        setInputView(dto);
//		} else {
//			finish();
//		}

		/*When user call from standard dialer, Caltxt tries to abort the
		 * call and show this input screen. Some Android versions don't 
		 * allow call abort and therefore setResultData(null) does not 
		 * work and the call initiates! For those scenarios avoid showing 
		 * input screen while may be the call is underway in background*/
        // commented, 28-JUL-17, call made via android dialer are not interrupted
//        if (CallManager.getInstance().isAnyCallInProgress()) {
//            Log.d(TAG, "CaltxtInputActivity:OnCreate:finish");
//            finish();
//        }

//        if (!FIREBASE_SIGNED_IN_STATUS)
//            signInAnonymously();
        mCallerImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent activity = new Intent(CaltxtInputActivity.this, PhotoFullscreen.class);
                activity.putExtra("URL", ((XMob) dto).getIcon());
                startActivity(activity);
            }
        });
    }

    @Override
    protected void onResume() {
        if (dto.getCName().equals("XCtx")) {
//			mBodyTextTimestamp.setText(((XCtx)dto).getSubject());
        }

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Connection.get().removeChangeListener(this);

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        Log.d(TAG, "BLOCKTEST onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private void setInputView(IDTObject dto) {

//		mFooter2Border.setVisibility(View.GONE);
//		mBodyText.setVisibility(View.GONE);
//		mBodyTextTimestampIcon.setVisibility(View.GONE);
//		mBodyTextTimestamp.setVisibility(View.GONE);
//		mBody2Text.setVisibility(View.GONE);
//		mAlertText.setVisibility(View.GONE);
//		mAlertImage.setVisibility(View.GONE);
//		progressBar.setVisibility(View.GONE);
        mAlertText.setText(R.string.input_text_alert_title);

//		mContextEdit.setFocusableInTouchMode(true);
//		mContextEdit.requestFocus();

//		findViewById(R.id.toast_body).setVisibility(View.GONE);
//		findViewById(R.id.toast_body2).setBackgroundResource(android.R.color.transparent);

//		thisView.setBackgroundResource(R.drawable.calloutfrom_lightgreen_white_text);
//		getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        if (dto.getCName().equals("XCtx")) {
//			if (Addressbook.get().isRegistered/*isCaltxtContact*//* AndConnected */(username)) {// WHYNOTSEND
            mMessageButton.setVisibility(View.GONE);
            mButtonDividerView.setVisibility(View.GONE);

				/* tint drawable mCallButton */
            Drawable d1 = getResources().getDrawable(R.drawable.ic_send_black_24dp);
            Drawable wrappedDrawable = DrawableCompat.wrap(d1);
            wrappedDrawable = wrappedDrawable.mutate();
            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
            d1.invalidateSelf();
            mCallButton.setCompoundDrawablesWithIntrinsicBounds(null, wrappedDrawable, null, null);
            mCallButton.setText(R.string.action_send);

            mContextEdit.setHint(R.string.action_call_reply_hint);
            mContextEdit.setText(((XCtx) dto).getAck());
            mContextEdit.selectAll();
            String header = Addressbook.getInstance(getApplicationContext()).getName(/*nnumber*/username);
            if (header.equals(nnumber)) {
                header = nnumber;
            } else {
                header = header + " " + nnumber;
            }
            mHeaderText.setText(header);
            if (Addressbook.getInstance(getApplicationContext()).isRegistered(nnumber)) {
//					mSubjectText.setText(Addressbook.getInstance(getApplicationContext()).getHeadline(nnumber));
            } else {
//					mSubjectText.setText("Unknown");
            }
            if (((XCtx) dto).getOccupation().length() == 0) {
                mSubject2Text.setVisibility(View.GONE);
            } else {
                mSubject2Text.setVisibility(View.VISIBLE);
                mSubject2Text.setText(((XCtx) dto).getOccupation());
            }
//			} else {
            // if callee not registered Caltxt user, close; don't send ACK
//				finish();
//				return;
//			}
        } else if (dto.getCName().equals("XMob")) {
//			if (Addressbook.get().isRegistered/*isCaltxtContact*//* AndConnected */(username)) {
//            Log.e(TAG, "setInputView XMob.getHeadline " + Addressbook.getInstance(getApplicationContext()).getHeadline(nnumber));
            String header = Addressbook.getInstance(getApplicationContext()).getName(nnumber);
            if (header.equals(nnumber)) {
                header = nnumber;
            } else {
                header = header + " " + nnumber;
            }
            mHeaderText.setText(header);
            if (Addressbook.getInstance(getApplicationContext()).isRegistered(nnumber)) {
//					mSubjectText.setText(Addressbook.getInstance(getApplicationContext()).getHeadline(nnumber));
            } else {
//					mSubjectText.setText("Unknown");
            }
            if (((XMob) dto).getOccupation() != null && ((XMob) dto).getOccupation().length() > 0) {
                mSubject2Text.setVisibility(View.VISIBLE);
                mSubject2Text.setText(((XMob) dto).getOccupation());
            } else {
                mSubject2Text.setVisibility(View.GONE);
            }
//			} else {// callee not registered
            // do a NORMAL CALL
//				caltxtHandler.initiateNormalCall(((XMob) dto).getNumber(), this);
//				finish();
//				return;
//			}
        } else {
            finish();
            return;
        }

        if (Addressbook.getInstance(getApplicationContext()).isRegistered(nnumber)) {
            // PING contact to check if offline or online
//            RebootService.getConnection(getApplicationContext()).submitForcedPingContact(
//                    Addressbook.getInstance(getApplicationContext()).getMyProfile().getUsername(),
//                    nnumber);
            Connection.get().submitForcedPingContact(
                    Addressbook.getInstance(getApplicationContext()).getMyProfile().getUsername(),
                    nnumber);
        }
        // if user does not start typing message in 8 seconds, this window
        // disappears.
		/*
		 *  13-SEP-2016 : NOT REQUIRED. User will close it!
		new CountDownTimer(Constants.CALTXT_INPUT_TIMEOUT, 1000) {
			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				if (!interactive)
					finish();
			}
		}.start();
		 */
    }

    /*
    private void actionMessageAndReject(final XCtx ctx) {

        Drawable d1 = CaltxtApp.getCustomAppContext().getResources().getDrawable(R.drawable.ic_call_end_black_24dp);
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
//        			Blockbook.get().add(username=ctx.getUsernameCaller(), ctx.getNameCaller());
//    			} else {
//        			Blockbook.get().add(username=ctx.getUsernameCallee(), ctx.getNameCallee());
//    			}
//				RebootService.getConnection().addAction(Constants.contactBlockedProperty, username, null);

                ctx.setCallState(XCtx.IN_CALL_REPLY);
                Logbook.get().update(ctx);
                CallHandler.endCall();
                finish();
            }
        });

        d1 = CaltxtApp.getCustomAppContext().getResources().getDrawable(R.drawable.ic_reply_black_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mMessageButton.setCompoundDrawablesWithIntrinsicBounds(null, d1, null, null);
        mMessageButton.setText(R.string.action_message);
        mMessageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CallHandler.endCall();

                //if is contact OR context call, show Acknowledgment window
//				if(Addressbook.get().isContact(ctx.getUsernameCaller()) || ctx.isCaltxtCall()) {
                    Intent ackList = new Intent(CaltxtInputActivity.this, AcknowledgementList.class);
                    ackList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ackList.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    ackList.putExtra("IDTOBJECT", ctx);
                    startActivity(ackList);
//				}

                finish();
            }
        });

    }

    private void actionReplyAndCall(final XCtx ctx) {
        Drawable d1 = CaltxtApp.getCustomAppContext().getResources().getDrawable(R.drawable.ic_reply_black_24dp);
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
                if(clickTypeSendMsg) {
                    final String edittext = mContextEdit.getEditableText().toString();
                    if(edittext.length()>0) {
                        if(ctx.getAck().length()>0) {
                            CaltxtHandler.get().addToCaltxtHistory(edittext);

                            XMob mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                            if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                                mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                            } else {
                                mob = Addressbook.get().getRegistered(ctx.getUsernameCallee());
                            }

                            CaltxtHandler.get().initiateMessage(mob, edittext, XCtx.PRIORITY_NORMAL);
                        } else {
                            CaltxtHandler.get().initiateMessageReply(ctx, edittext);
                        }
                        Log.e(TAG, "clickTypeSendMsg "+ctx);
                    }

                    finish();
                } else {
                    Drawable d1 = CaltxtApp.getCustomAppContext().getResources().getDrawable(R.drawable.ic_call_black_24dp);
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
                            XMob mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                            if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                                mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                            } else {
                                mob = Addressbook.get().getRegistered(ctx.getUsernameCallee());
                            }

                            if(edittext.length()>0)
                                CaltxtHandler.get().addToCaltxtHistory(edittext);

                            CaltxtHandler.get().initiateContextCall(mob, edittext, XCtx.PRIORITY_NORMAL);
                            finish();
                        }
                    });
                    mCallButton.setOnLongClickListener(new OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            final String edittext = mContextEdit.getEditableText().toString();
                            XMob mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                            if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                                mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                            } else {
                                mob = Addressbook.get().getRegistered(ctx.getUsernameCallee());
                            }

                            if(edittext.length()>0)
                                CaltxtHandler.get().addToCaltxtHistory(edittext);

                            CaltxtHandler.get().initiateContextCall(mob, edittext, XCtx.PRIORITY_EMERGENCY);
                            finish();
                            return true;
                        }
                    });

//					body2frame.setVisibility(View.VISIBLE);
//					body2frame.setBackgroundResource(android.R.color.transparent);
                    mContextEdit.setVisibility(View.VISIBLE);
//					mContextEdit.setFocusableInTouchMode(true);
//					mContextEdit.requestFocus();
                    d1 = CaltxtApp.getCustomAppContext().getResources().getDrawable(R.drawable.ic_send_black_24dp);
                    wrappedDrawable = DrawableCompat.wrap(d1);
                    wrappedDrawable = wrappedDrawable.mutate();
                    DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
                    d1.invalidateSelf();
                    mMessageButton.setCompoundDrawablesWithIntrinsicBounds(null, d1, null, null);
                    mMessageButton.setText("SEND");
//					mMessageButton.setImageResource(R.drawable.ic_send_black_24dp);
                    clickTypeSendMsg = true;
                    timer.cancel();
                    getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
            }
        });

        d1 = CaltxtApp.getCustomAppContext().getResources().getDrawable(R.drawable.ic_call_black_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mCallButton.setCompoundDrawablesWithIntrinsicBounds(null, d1, null, null);
        mCallButton.setText(R.string.action_call);
        mCallButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                XMob mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                    mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                } else {
                    mob = Addressbook.get().getRegistered(ctx.getUsernameCallee());
                }

                CaltxtHandler.get().initiateContextCall(mob,
                        (ctx.getAck().length()==0?(ctx.getCaltxt().length()==0?"":"RE:"+ctx.getCaltxt()):"RE:"+ctx.getAck()),
                        XCtx.PRIORITY_NORMAL);
                finish();
            }
        });
        mCallButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                XMob mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                    mob = Addressbook.get().getRegistered(ctx.getUsernameCaller());
                } else {
                    mob = Addressbook.get().getRegistered(ctx.getUsernameCallee());
                }

                caltxtHandler.initiateContextCall(mob,
                        (ctx.getAck().length()==0?(ctx.getCaltxt().length()==0?"":"RE:"+ctx.getCaltxt()):"RE:"+ctx.getAck()),
                        XCtx.PRIORITY_EMERGENCY);
                finish();
                return true;
            }
        });
    }

    public static void showToastView(final XCtx ctx, final boolean alert) {
        if(alert) {
            Globals.playAlert();
        }
        Intent it = new Intent(Globals.getCustomAppContext(), CaltxtToast.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        it.putExtra("IDTOBJECT", ctx);
        Log.e(TAG, "showCaltxt "+ctx.toString());
        Globals.getCustomAppContext().startActivity(it);
    }

    public static void showInputView(final XCtx ctx, final boolean alert) {
        if(alert) {
            Globals.playAlert();
        }
        Intent it = new Intent(Globals.getCustomAppContext(), CaltxtToast.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        it.putExtra("IDTOBJECT", ctx);
        Log.e(TAG, "showCaltxt "+ctx.toString());
        Globals.getCustomAppContext().startActivity(it);
    }
*/
    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        final String propertyName = event.getPropertyName();

        Log.d(TAG, "propertyChange " + propertyName + " " + event.getNewValue());
        if (propertyName.equals(Constants.messageArrivedProperty)
				/*|| propertyName.equals(Constants.ConnectionStatusProperty)*/) {

            if (XMob.isXMob(event.getNewValue().toString())) {
                XMob mob = new XMob();
                mob.init(event.getNewValue().toString());
                Addressbook.getInstance(getApplicationContext()).update(mob);
//                Log.d(TAG, "propertyChange "+propertyName+" isXMob " + mob);
            }
        }

        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                if (propertyName.equals(Constants.messageArrivedProperty)) {
                    if (XMob.isXMob(event.getNewValue().toString())) {

                        XMob tmob = new XMob();
                        tmob.init(event.getNewValue().toString());
                        XMob mob = Addressbook.getInstance(getApplicationContext()).getContact(tmob.getUsername());

                        Log.d(TAG, "propertyChange " + propertyName + " runOnUiThread " + mob);
                        if(!tmob.getUsername().equals(username)) {
                            Log.d(TAG, "propertyChange received for user other than " + username);
                            return;
                        }

                        mStatusIcon.setBackgroundResource(Addressbook.getInstance(getApplicationContext()).geStatusBackgroundResource(mob));
                        mStatusIcon.setImageResource(Addressbook.getInstance(getApplicationContext()).getContactStatusIconResource(mob));

                        String header = mob.getName();
                        if (header.equals(XMob.toFQMN(nnumber, Addressbook.getMyCountryCode()))) {
                            header = nnumber;
                        } else {
                            header = header + " " + nnumber;
                        }
                        mHeaderText.setText(header);
                        if (Addressbook.getInstance(getApplicationContext()).isRegistered(nnumber)) {
//							mSubjectText.setText(mob.getHeadline());
                        } else {
//							mSubjectText.setText("Unknown");
                        }
                        if (mob.getOccupation() != null && mob.getOccupation().length() > 0) {
                            mSubject2Text.setVisibility(View.VISIBLE);
                            mSubject2Text.setText(mob.getOccupation());
                        } else {
                            mSubject2Text.setVisibility(View.GONE);
                        }

                        if (Addressbook.getInstance(getApplicationContext()).isRegistered(nnumber)) {
//					mFooter2Border.setVisibility(View.GONE);
//					mAlertImage.setVisibility(View.GONE);
//					mAlertText.setVisibility(View.GONE);
                            mAlertText.setText(R.string.input_text_alert_title);
//				} else if(Settings.delivery_by_sms) {
                        } else if (Settings.isSMSEnabled(CaltxtInputActivity.this)) {
//					footer2frame.setVisibility(View.VISIBLE);//to make alert invisible (all caltxt sent via data only)
//					mFooter2Border.setVisibility(View.VISIBLE);
//					mAlertText.setVisibility(View.VISIBLE);
//					mAlertImage.setVisibility(View.VISIBLE);
                            mAlertText.setText(R.string.input_text_sms_disclaimer);
                        } else {
//					footer2frame.setVisibility(View.VISIBLE);//to make alert invisible (all caltxt sent via data only)
//					mFooter2Border.setVisibility(View.VISIBLE);
//					mAlertText.setVisibility(View.VISIBLE);
//					mAlertImage.setVisibility(View.VISIBLE);
                            if (!Addressbook.getInstance(getApplicationContext()).isRegistered(nnumber)) {
                                mAlertText.setText(R.string.input_text_cant_send_disclaimer);
                            }
//					mAlertText.setTextColor(Color.RED);
                        }
                    }
                } else if (propertyName.equals(Constants.mqttPublishedProperty) ||
                        propertyName.equals(Constants.smsPublishedProperty)
                        || propertyName.equals(Constants.firebasePublishedProperty)) {

                    if (XCtx.isXCtx(event.getOldValue().toString())) {
//				footer2frame.setVisibility(View.VISIBLE);
//				progressBar.setVisibility(View.VISIBLE);
                        mAlertText.setVisibility(View.VISIBLE);
                        mAlertText.setText(R.string.text_sent);
                    } else {
//				footer2frame.setVisibility(View.GONE);
//				progressBar.setVisibility(View.GONE);
//				mAlertText.setVisibility(View.GONE);
//				mAlertText.setText("");
                    }
                } else if (
                    // All except "Publishing" events are ignored to initiate a normal call
//						propertyName.equals(Constants.mqttDeliveredProperty) ||
                        propertyName.equals(Constants.mqttPublishingProperty) ||
//						propertyName.equals(Constants.smsDeliveredProperty)||
                                propertyName.equals(Constants.smsPublishingProperty) ||
//						propertyName.equals(Constants.firebaseDeliveredProperty)||
                                propertyName.equals(Constants.firebasePublishingProperty)
//						propertyName.equals(Constants.messageErrorProperty)
                        ) {
					/*if (XCtx.isXCtx(event.getNewValue().toString())) {
//				footer2frame.setVisibility(View.GONE);
//				progressBar.setVisibility(View.GONE);
//				mAlertText.setVisibility(View.GONE);

						XCtx ctx = new XCtx();
						ctx.init(event.getNewValue().toString());
						final XCtx ctxx = CaltxtHandler.get(CaltxtInputActivity.this).peekCaltxt(Constants.CALL_TYPE_OUTBOUND, XMob.toFQMN(ctx.getNumberCallee(), Addressbook.getMyCountryCode()));
						if (ctxx != null && XMob.toFQMN(ctxx.getNumberCallee(), Addressbook.getMyCountryCode())
								.equals(XMob.toFQMN(ctx.getNumberCallee(), Addressbook.getMyCountryCode()))) {
							//start call in one second
							CountDownTimer tymr = new CountDownTimer(1000, 1000) {
								public void onTick(long millisUntilFinished) {
								}

								public void onFinish() {
									if (propertyName.equals(Constants.messageErrorProperty)) {
//								Notify.toast(Globals.getCustomAppContext(),
//										ctxx.getNameCallee()+" not reachable. Try after sometime",
//										Toast.LENGTH_LONG);
									} else {
									}
									CaltxtHandler.get(CaltxtInputActivity.this).initiateNormalCall(
											Addressbook.getInstance(getApplicationContext()).get(ctxx.getNumberCallee()).getNumber(),
											CaltxtInputActivity.this);
								}
							};
							tymr.start();
						}
						finish();
					}*/
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

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<Integer, String> mIdMap = new HashMap<Integer, String>();
        CaltxtInputActivity context;

        public StableArrayAdapter(CaltxtInputActivity context, int textViewResourceId,
                                  List<String> objects) {
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
                LayoutInflater inflator = context.getLayoutInflater();
                rowView = inflator.inflate(R.layout.acknowledgement_list_item, null);

                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.ack_name = rowView.findViewById(R.id.ack_name);
                viewHolder.ack_name_icon = rowView.findViewById(R.id.ack_name_icon);
                viewHolder.expire_time_text = rowView.findViewById(R.id.expire_time_text);
                viewHolder.ack_send = rowView.findViewById(R.id.ack_send);

                viewHolder.ack_name.setTextSize(14);
                viewHolder.expire_time_text.setVisibility(View.GONE);
                viewHolder.ack_name_icon.setVisibility(View.VISIBLE);

                //31MAY17, don't let user delete predefined texts
                //they will be replaced by new text when entered
                viewHolder.ack_send.setVisibility(View.GONE);

                rowView.setTag(viewHolder);

                Drawable d1 = getResources().getDrawable(R.drawable.ic_clear_black_24dp);
                Drawable wrappedDrawable = DrawableCompat.wrap(d1);
                wrappedDrawable = wrappedDrawable.mutate();
                DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
                d1.invalidateSelf();

                viewHolder.ack_send.setImageDrawable(d1);
                rowView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        final String msg = viewHolder.ack_name.getText().toString();
                        mContextEdit.setText(msg);
                    }

                });

                viewHolder.ack_send.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final String msg = viewHolder.ack_name.getText().toString();
                        //delete item
                        caltxtHandler.removeFromCaltxtHistory(msg);
                        ((StableArrayAdapter) readyTextListView.getAdapter()).remove(msg);
                        ((StableArrayAdapter) readyTextListView.getAdapter()).notifyDataSetChanged();
                        Log.d(TAG, "DELETE onClick " + msg);
                    }
                });
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.ack_name.setText(getItem(position));
            holder.expire_time_text.setVisibility(View.GONE);

//			rowView.setTag(position);
            return rowView;
        }

        class ViewHolder {
            protected TextView ack_name;
            protected ImageView ack_name_icon;
            protected TextView expire_time_text;
            AppCompatImageButton ack_send;
            int position;
        }
    }
}
