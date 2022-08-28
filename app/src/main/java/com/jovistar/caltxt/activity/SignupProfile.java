package com.jovistar.caltxt.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.firebase.storage.DownloadService;
import com.jovistar.caltxt.firebase.storage.UploadService;
import com.jovistar.caltxt.images.ImageLoader;
import com.jovistar.caltxt.network.voice.TelephonyInfo;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.service.RebootService;
import com.jovistar.commons.bo.XMob;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//import android.support.annotation.NonNull;
//import android.support.v4.content.LocalBroadcastManager;
//import com.jovistar.commons.facade.ModelFacade;

/**
 * Activity which displays a Signup screen to the user, offering registration as
 * well.
 */
@SuppressLint("NewApi")
public class SignupProfile extends AppCompatActivity implements
        PropertyChangeListener, OnItemSelectedListener/*, OnItemClickListener*/ {

    private static final String TAG = "SignupProfile";
    int PIC_CROP = 9;
    ProgressDialog ringProgressDialog;

    boolean verificationPassSIM1 = false;
    boolean verificationPassSIM2 = false;
    private FirebaseAuth mAuth;
    String mVerificationId1, mVerificationId2;
    PhoneAuthProvider.ForceResendingToken mResendToken1, mResendToken2;

    public static int screenWidth = 0, screenHeight = 0;
    public static final int RESULT_GALLERY = 9;
    public static String smsContentSent = "";

    public static String semaphore = "";
    String name_old = "", status_old = "", occupation_old = "", company_old = "";

    boolean PROFILE_IMG_UPDATED = false;
    String userPicFileName = "";
    String userPicFileName2 = "";
    CountDownTimer mVerificationCountdownTimer;
    public static final String REGISTERED_PHONE_NUMBER_SIM_CHANGED = "2";
    public static final String REGISTERED_PHONE_NUMBER_VERIFIED = "1";//phone number verified, user added
    public static final String NEWUSER_PHONE_NUMBER_VERIFIED = "0";//phone number verified, user not added

    Uri profilePictureUri = null;

    // UI references.
    private EditText mNameView;
    private EditText mMobileNumberView, mMobileNumber2View, mOccupationView, mCompanyView;
    private ImageView mPicView;
    AppCompatImageButton mPicRemove, mPicEdit;
    private Spinner mCountriesSpinner;
    private ListPopupWindow lpw;
    private String[] listStatus;

    private ProgressBar mProgress;
    AppCompatButton mMobileNumberVerifyButton, mMobileNumber2VerifyButton;
    View cardOccupationView;
//    View inputPhoneDisclaimer;
    AppCompatButton mSignupButton;
    private TextView mInputPhonenoDescription, cardCCTitle;
    static int shortAnimTime = 0;
    Context mContext = this;
    int finalHeight = 0, finalWidth = 0;
    String cc_locale = "";// find out the country code from the phone itself

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the Signup form.
        setContentView(R.layout.signup_profile);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mOccupationView = findViewById(R.id.input_occupation);
        mCompanyView = findViewById(R.id.input_company);
        mNameView = findViewById(R.id.name);
//		mMobileCountryCodeView = (EditText) findViewById(R.id.country_code);
        mMobileNumberView = findViewById(R.id.phone_number);
        mMobileNumber2View = findViewById(R.id.phone_number2);
        mMobileNumberVerifyButton = findViewById(R.id.phone_number_verify_again);
        mMobileNumber2VerifyButton = findViewById(R.id.phone_number2_verify_again);
        mPicView = findViewById(R.id.profile_image);
        mPicEdit = findViewById(R.id.image_edit);
        mPicRemove = findViewById(R.id.image_remove);
        mSignupButton = findViewById(R.id.sign_up_button);
        mCountriesSpinner = findViewById(R.id.countries_spinner);
        cardOccupationView = findViewById(R.id.card_occupation_view);
//        inputPhoneDisclaimer = findViewById(R.id.input_phone_disclaimer);
        cardCCTitle = findViewById(R.id.card_cc_title);
        mInputPhonenoDescription = findViewById(R.id.input_phoneno_description);
        TextView tos = (TextView)findViewById(R.id.card_terms_description);
        tos.setText(Html.fromHtml(getResources().getString(R.string.signup_terms_agreement)));
        tos.setVisibility(View.GONE);

        mNameView.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (str.contains("^")) {
                    mNameView.setText(str.replace("^", ""));
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        mMobileNumberVerifyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                attemptSignup();
            }

        });
        mMobileNumber2VerifyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                attemptSignup();
            }

        });

        final XMob myProfile = Addressbook.getInstance(getApplicationContext()).getMyProfile();
        File file = null;
        userPicFileName = myProfile == null ? "tmp" + XMob.IMAGE_FILE_EXTN : myProfile.getIcon();
        file = ImageLoader.getInstance(getApplicationContext()).getFile(userPicFileName, 0);

        if (file.exists()) {
            mPicRemove.setVisibility(View.VISIBLE);
        } else {
            mPicRemove.setVisibility(View.GONE);
        }
        ViewTreeObserver vto = mPicView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                int x;
                mPicView.getViewTreeObserver().removeOnPreDrawListener(this);
                x = mPicView.getMeasuredWidth();
                mPicView.setLayoutParams(new RelativeLayout.LayoutParams(x, x));
                finalHeight = mPicView.getMeasuredHeight();
                finalWidth = mPicView.getMeasuredWidth();
                if (myProfile != null) {
                    ImageLoader.getInstance(getApplicationContext()).DisplayImage(userPicFileName,
                            mPicView, finalWidth, R.drawable.ic_person_white_24dp, true);
                }
                return true;
            }
        });

		/* below few lines to capture imageview h and w
		ViewTreeObserver vto = mPicView.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
		    public boolean onPreDraw() {
		    	mPicView.getViewTreeObserver().removeOnPreDrawListener(this);
		        finalHeight = mPicView.getMeasuredHeight();
		        finalWidth = mPicView.getMeasuredWidth();
		        Log.d(TAG, "onCreate, Height: " + finalHeight + " Width: " + finalWidth);
		        return true;
		    }
		});*/

        mProgress = findViewById(R.id.photo_progress_bar);
        mCountriesSpinner.setOnItemSelectedListener(this);
        mProgress.setVisibility(View.GONE);

        mPicRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupProfile.this, R.style.CaltxtAlertDialogTheme);
                builder.setPositiveButton(R.string.action_remove,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                                // Create a reference to the file to delete
                                StorageReference desertRef = storageRef.child("media/photos/" + userPicFileName);

                                // delete sim2 profile picture (copy)
                                if (Addressbook.getInstance(mContext).getMyProfile().getNumber2() != null
                                        && Addressbook.getInstance(mContext).getMyProfile().getNumber2().length() > 0) {

                                    userPicFileName2 = Addressbook.getInstance(getApplicationContext()).getMyProfile().getNumber2() + XMob.IMAGE_FILE_EXTN;
                                    StorageReference desertRef2 = storageRef.child("media/photos/" + userPicFileName2);
                                    desertRef2.delete();
                                }

                                // Delete the file
                                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully
                                        File file = ImageLoader.getInstance(getApplicationContext()).getFile(userPicFileName, 0);
                                        //delete old file (since same name)
                                        if (file.exists()) {
                                            file.delete();
                                            ImageLoader.getInstance(getApplicationContext()).removeCache(userPicFileName);
                                        }

                                        File file2 = null;//sim2
                                        if(userPicFileName2!=null) {
                                            file2 = ImageLoader.getInstance(getApplicationContext()).getFile(userPicFileName2, 0);
                                            if (file2.exists()) {
                                                file2.delete();
                                                ImageLoader.getInstance(getApplicationContext()).removeCache(userPicFileName2);
                                            }
                                        }

                                        mPicRemove.setVisibility(View.GONE);
                                        mPicView.setImageResource(R.drawable.ic_person_white_24dp);

                                        Log.d(TAG, " file deleted " + userPicFileName);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                        Log.d(TAG, " error deleting file " + exception);
                                    }
                                });

                            }
                        });
                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.setMessage(R.string.prompt_caltxt_profile_remove_photo);
//				builder.setIcon(R.drawable.ic_warning_white_24dp);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        mPicEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, RESULT_GALLERY);
            }
        });

        if (false == TelephonyInfo.getInstance(mContext).isDualSIM()) {
            mMobileNumber2View.setVisibility(View.GONE);
            mMobileNumber2VerifyButton.setVisibility(View.GONE);
        }
        if (isSIM1Verified(this)) {
            mMobileNumberVerifyButton.setEnabled(false);
            mMobileNumberVerifyButton.setTextColor(Color.GRAY);
            mMobileNumberVerifyButton.setText("VERIFIED");
            mMobileNumberView.setEnabled(false);// do not let user change mobile number
        }
        if (isSIM2Verified(this)) {
            mMobileNumber2VerifyButton.setEnabled(false);
            mMobileNumber2VerifyButton.setTextColor(Color.GRAY);
            mMobileNumber2VerifyButton.setText("VERIFIED");
            mMobileNumber2View.setEnabled(false);// do not let user change mobile number
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.CountryCodes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountriesSpinner.setAdapter(adapter);
        String cc = getPreference(this, getString(R.string.profile_key_mobile_country_code));//returns "India,91"
        Log.d(TAG, "onCreate, cc1 " + cc);
        if (cc.length() > 0) {
            cc = cc.split("\\(")[0].trim();//take out country name
            Log.d(TAG, "onCreate, cc2 " + cc);
        }
//            if (cc.length() == 0) {
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            cc_locale = (new Locale("", manager.getNetworkCountryIso().toUpperCase())).getDisplayCountry();
            Log.d(TAG, "onCreate, cc3 " + cc_locale);
        }
//            }
        if (cc_locale.length() == 0) {
            cc_locale = getResources().getConfiguration().locale.getDisplayCountry();
            Log.d(TAG, "onCreate, cc4 " + cc_locale);
        }
        int position = -1, sz = adapter.getCount();
        String[] g = null;
        Log.d(TAG, "onCreate, cc5 " + cc);
        for (int i = 0; i < sz; i++) {
            g = adapter.getItem(i).toString().split("\\(");
            if (g != null && g.length > 0) {
                g[0] = g[0].trim();
                Log.d(TAG, "onCreate, g[0] " + g[0]);
                if (g[0].equals(cc_locale)/* || g[1].equals(cc)*/) {
                    position = i;
                    break;
                }
            }
        }
        mCountriesSpinner.setSelection(position);

        if (isNewUser(this) || isSIMCardChanged(this)) {// this activity is used as Sign up Activity

//            findViewById(R.id.toolbar).setVisibility(View.GONE);
            getSupportActionBar().setTitle("Caltxt Signup");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
//		    getSupportActionBar().setLogo(R.drawable.ic_launcher);

            mMobileNumberVerifyButton.setVisibility(View.GONE);
            mMobileNumber2VerifyButton.setVisibility(View.GONE);

//			mPhoneStatusView.setVisibility(View.GONE);
//			((TextView) findViewById(R.id.input_status_description)).setVisibility(View.GONE);
            cardOccupationView.setVisibility(View.GONE);

//			mNameView.requestFocus();
//			getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);/* soft keyboard open*/

            mSignupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //commented 07-JAN-19, Google policy change
//                    if (PackageManager.PERMISSION_GRANTED
//                            != Caltxt.checkPermission(SignupProfile.this, Manifest.permission.SEND_SMS,
//                            Caltxt.CALTXT_PERMISSIONS_REQUEST_SEND_SMS,
//                            "Caltxt need permission to send SMS to verify phone number")) {
//
//                        return;
//                    }

                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
                    attemptSignup();
                }
            });

            if (isSIMCardChanged(this)) {
                new AlertDialog.Builder(this)
                        .setMessage("SIM card changed. Please verify your number again")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mMobileNumberView.requestFocus();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        } else {// this activity is used as Profile Activity
            tos.setVisibility(View.GONE);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Profile");

//			mOccupationView.setFocusable(false);
//			mCompanyView.setFocusable(false);

            mNameView.setFocusable(false);
            mNameView.setClickable(true);
            mNameView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(SignupProfile.this, R.style.CaltxtAlertDialogTheme);
                    builder.setTitle("Edit name");

                    // Set up the input
                    final EditText input = new EditText(SignupProfile.this);
                    int maxLength = Integer.parseInt(getResources().getString(R.string.profile_value_name_length_max));
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                    input.setFilters(FilterArray);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setText(mNameView.getText());
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mNameView.setText(input.getText().toString());
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    final AlertDialog dlg = builder.create();
                    dlg.show();

                    input.addTextChangedListener(new TextWatcher() {

                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            String str = s.toString();
                            if (str.contains("^")) {
                                mNameView.setText(str.replace("^", ""));
                                return;
                            }
                            if (str.length() < 4) {
                                dlg.getButton(AlertDialog.BUTTON1).setEnabled(false);
                            } else {
                                dlg.getButton(AlertDialog.BUTTON1).setEnabled(true);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }
                    });

                }
            });

            cardOccupationView.setVisibility(View.VISIBLE);
            mSignupButton.setVisibility(View.GONE);
//            inputPhoneDisclaimer.setVisibility(View.GONE);
            mCountriesSpinner.setVisibility(View.GONE);
            cardCCTitle.setText(R.string.profile_phone_title);
            mInputPhonenoDescription.setText(R.string.input_phone_description_profile);

//            CCWService.try2LoginCCMAsync(getApplicationContext());
        }

        shortAnimTime = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        mPicView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent activity = new Intent(SignupProfile.this, PhotoFullscreen.class);
                activity.putExtra("URL", userPicFileName);
                startActivity(activity);
            }
        });

        setScreenResolution();
        mAuth = FirebaseAuth.getInstance();
        //signInAnonymously();
    }

    private void sendVerificationCodeFirebase(final String phoneNumber) {

        //It is the verification id that will be sent to the user
//        final String mVerificationPhone = phoneNumber;

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                //Getting the code sent by SMS
                String otp = phoneAuthCredential.getSmsCode();
                boolean verificationComplete = false;
                Log.d(TAG, "onVerificationCompleted: otp "+otp);

                //sometime the code is not detected automatically
                //in this case the code will be null
                //so user has to manually enter the code
                if (otp != null) {
                    //editTextCode.setText(code);
                    //verifying the code
                    if(phoneNumber.endsWith(mMobileNumberView.getText().toString())) {

                        verificationComplete = true;
                        verificationPassSIM1 = true;
                        verifyVerificationCodeFirebase(mVerificationId1, otp);

                    } else if(phoneNumber.endsWith(mMobileNumber2View.getText().toString())) {

                        verificationComplete = true;
                        verificationPassSIM2 = true;
                        verifyVerificationCodeFirebase(mVerificationId2, otp);
                    }

                }

                if(!verificationComplete) {
                    Toast.makeText(SignupProfile.this, "Verification timed out, please try after sometime", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(SignupProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "onVerificationFailed: e "+e);

                mVerificationCountdownTimer.cancel();
                ringProgressDialog.dismiss();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String verificationId) {
                Toast.makeText(SignupProfile.this, "onCodeAutoRetrievalTimeOut", Toast.LENGTH_LONG).show();
                Log.d(TAG, "onCodeAutoRetrievalTimeOut: "+verificationId);

                mVerificationCountdownTimer.cancel();
                ringProgressDialog.dismiss();
            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                if(phoneNumber.endsWith(mMobileNumberView.getText().toString())) {

                    mVerificationId1 = s;
                    mResendToken1 = forceResendingToken;
                    Log.d(TAG, "onCodeSent: s "+s+", mVerificationId "+mVerificationId1);
                } else if(phoneNumber.endsWith(mMobileNumber2View.getText().toString())) {

                    mVerificationId2 = s;
                    mResendToken2 = forceResendingToken;
                    Log.d(TAG, "onCodeSent: s "+s+", mVerificationId "+mVerificationId2);
                } else {
                    Toast.makeText(SignupProfile.this, "Verification code error, please try after sometime", Toast.LENGTH_LONG).show();
                }
            }
        };

        final int timeoutMsecs = Integer.parseInt((getResources().getString(R.string.timeoutInMiliSecondsSignupCCW)));

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                timeoutMsecs/2,                 // Timeout duration
                TimeUnit.MILLISECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void verifyVerificationCodeFirebase(String verificationId, String otp) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        //signing the user
        signInWithFirebasePhoneAuthCredential(credential);
    }

    private void signInWithFirebasePhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignupProfile.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: isSuccessful");
                            //verification successful we will start the Caltxt activity
                            processPhoneVerificationResult(true);
                            //Intent intent = new Intent(VerifyPhoneActivity.this, ProfileActivity.class);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //startActivity(intent);

                            // if SIM2 exits and not verified, do it now!
                            if (TelephonyInfo.getInstance(mContext).isDualSIM()
                                    && !isSIM2Verified(SignupProfile.this)) {

                                String CC = Addressbook.getInstance(getApplicationContext()).getMyCountryCode();
                                sendVerificationCodeFirebase("+"+CC +mMobileNumber2View.getText().toString());

                            }

                        } else {
                            Log.d(TAG, "onComplete: isNOTSuccessful "+task.getException());

                            //verification unsuccessful.. display an error message
                            processPhoneVerificationResult(false);

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Toast toast = Toast.makeText(SignupProfile.this, message, Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
    }

    private void signInAnonymously() {
        // Sign in anonymously. Authentication is required to read or write from Firebase Storage.
        mAuth.signInAnonymously()
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "signInAnonymously:SUCCESS");
//						updateUI(authResult.getUser());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "signInAnonymously:FAILURE", exception);
//						updateUI(null);
                    }
                });
    }

    private void setScreenResolution() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void performCrop(Uri picUri) {
        try {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.CONTENT_TYPE};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int column_type = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.CONTENT_TYPE);
            Log.d(TAG, "getPath uri " + uri.toString() + ", type " + cursor.getString(column_type));
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }

        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_GALLERY:
                if (null != data) {
                    profilePictureUri = data.getData();
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String type = mime.getExtensionFromMimeType(getContentResolver().getType(profilePictureUri));
                    Log.d(TAG, "getPath uri " + profilePictureUri.toString() + ", type " + type);

                    Bitmap bitmap = ImageLoader.getInstance(getApplicationContext()).getSquareBitmapFromUriBySize(
                            (profilePictureUri),
                            screenWidth, 150000/*less than 100KB file*/,
                            R.drawable.ic_person_white_24dp_web);
//					bitmap = ((Globals)getApplication()).getImageLoader().decodeSampledBitmapFromResource(
//							getRealPathFromURI(profilePictureUri), SplashScreen.screenWidth, SplashScreen.screenWidth);

//		        	((Globals)getApplication()).getImageLoader().DisplayImage(myImageIcon,
//		    				mPicView, 0, R.drawable.ic_person_white_24dp_web);
//					bitmap = ImageLoader.scaleImage(bitmap, mPicView, SplashScreen.screenWidth);
                    Log.e(TAG, "onActivityResult, bitmap width height "
                            + bitmap.getWidth() + "," + bitmap.getHeight()
                            + " screen width height " + screenWidth + ", " + screenHeight
                            + " picView width height " + mPicView.getWidth() + ", " + mPicView.getHeight()
                            + ", url " + getRealPathFromURI(profilePictureUri));
                    if (bitmap == null) {
                        Log.e(TAG, "no bitmap found!!!");
                        return;
                    }

                    try {
                        // create image file in file cache with standard name (uname)
                        File file = ImageLoader.getInstance(getApplicationContext()).getFile(userPicFileName, 0);

                        //delete old file (since same name)
                        if (file.exists())
                            file.delete();

                        ImageLoader.getInstance(getApplicationContext()).updateMemoryCache(
                                userPicFileName, bitmap);

                        OutputStream fos = new FileOutputStream(file);
                        //copy selected image stream to file
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                        //close handles
                        fos.flush();
                        fos.close();
                        fos = null;

                        // Start MyUploadService to upload the file, so that the file is uploaded
                        // even if this Activity is killed or put in the background
                        if (isNumberVerifiedUserAdded(this)) {//existing user
                            // set the image first
//                            mPicView.setImageBitmap(bitmap);
//                            mPicView.invalidate();

                            uploadFromUri(Uri.fromFile(file));
                            Log.d(TAG, "uploadFromUri file uri " + file.getName());

                            // create sim2 profile picture (copy)
                            if (Addressbook.getInstance(mContext).getMyProfile().getNumber2() != null
                                    && Addressbook.getInstance(mContext).getMyProfile().getNumber2().length() > 0) {
                                File file1 = saveAsFile(file, Addressbook.getInstance(getApplicationContext()).getMyProfile().getNumber2() + XMob.IMAGE_FILE_EXTN);
                                if (file1 != null) {
                                    uploadFromUri(Uri.fromFile(file1));
                                    Log.d(TAG, "uploadFromUri file1 uri " + file1.getName());
                                }
                            }
                        } else {
                            ImageLoader.getInstance(getApplicationContext()).DisplayImage(userPicFileName,
                                    mPicView, finalWidth, R.drawable.ic_person_white_24dp, true);
//                            mPicView.setImageBitmap(bitmap);
//                            mPicView.invalidate();
                            // upload file after registration
                        }
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, e.toString());
                    } catch (IOException ioe) {
                        Log.e(TAG, ioe.toString());
                    }

                    PROFILE_IMG_UPDATED = true;

                }
                break;
            default:
                break;
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        CursorLoader cursorLoader = new CursorLoader(
                this,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateView();
    }

    @Override
    protected void onPause() {
		/* NOTICE : THIS METHOD SHOULD RETURN QUICK AS POSSIBLE 
		 * OTHERWISE THE UPCOMING ACTIVITY WILL SHOW LAG IN LOADING */

        persistProfile();// preserve changes if user changed profile

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // Local broadcast receiver
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive:" + intent);

            switch (intent.getAction()) {
                case DownloadService.DOWNLOAD_COMPLETED:
                    // Get number of bytes downloaded
                    long numBytes = intent.getLongExtra(DownloadService.EXTRA_BYTES_DOWNLOADED, 0);
                    Log.d(TAG, "DOWNLOAD_COMPLETED numBytes " + numBytes);

                    // Alert success
                    break;
                case DownloadService.DOWNLOAD_ERROR:
                    // Alert failure
                    Notify.toast(mMobileNumberView, mContext, "Download error", Toast.LENGTH_LONG);
                    break;
                case UploadService.UPLOAD_COMPLETED:
                    ImageLoader.getInstance(getApplicationContext()).DisplayImage(userPicFileName,
                            mPicView, finalWidth, R.drawable.ic_person_white_24dp, true);
                    break;
                case UploadService.UPLOAD_ERROR:
                    Notify.toast(mMobileNumberView, mContext, "Upload error", Toast.LENGTH_LONG);
//					onUploadResultIntent(intent);
                    break;

            }
            mProgress.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(SignupProfile.this).unregisterReceiver(mBroadcastReceiver);
        }
    };

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, DownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, UploadService.getIntentFilter());

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, UploadService.class)
                .putExtra(UploadService.EXTRA_FILE_URI, fileUri)
                .setAction(UploadService.ACTION_UPLOAD));

        mProgress.setVisibility(View.VISIBLE);
        // Show loading spinner
//        showProgressDialog(getString(R.string.progress_uploading));
    }

    @Override
    protected void onDestroy() {

        if (isNumberVerifiedUserAdded(this)) {//existing user
            if (PROFILE_IMG_UPDATED) {
                XMob ct = new XMob(Addressbook.getInstance(getApplicationContext()).getMyProfile().getName(),
                        Addressbook.getInstance(getApplicationContext()).getMyProfile().getNumber(), Addressbook.getInstance(getApplicationContext()).getMyCountryCode());

                ct.setBody(ImageLoader.getInstance(getApplicationContext()).getFileAbsolutePath(Addressbook.getInstance(getApplicationContext()).getMyProfile().getIcon()));

            }
        }
        if (mVerificationCountdownTimer != null)
            mVerificationCountdownTimer.cancel();

        super.onDestroy();
    }

    public static boolean isNewUser(Context ct) {
		/*if no profile preference found (fresh install)*/
        return getPreference(ct, ct.getString(R.string.profile_key_number_verification_status)).length() == 0/*if no profile preference found (fresh install)*/
                && getPreference(ct, ct.getString(R.string.profile_key_number2_verification_status)).length() == 0;
    }

    public static boolean isSIMCardChanged(Context ct) {
		/*SIM Changed*//*SIM Changed*/
        return getPreference(ct, ct.getString(R.string.profile_key_number_verification_status)).equals(REGISTERED_PHONE_NUMBER_SIM_CHANGED/*SIM Changed*/)
                ||
                getPreference(ct, ct.getString(R.string.profile_key_number2_verification_status)).equals(REGISTERED_PHONE_NUMBER_SIM_CHANGED/*SIM Changed*/);
    }

    public static boolean isNewUserAndNumberVerified(Context ct) {
        if (getPreference(ct, ct.getString(R.string.profile_key_number_verification_status)).equals(REGISTERED_PHONE_NUMBER_VERIFIED/*sms verification over*/)
                || getPreference(ct, ct.getString(R.string.profile_key_number2_verification_status)).equals(REGISTERED_PHONE_NUMBER_VERIFIED)) {
            return (false);
        } else {
            return getPreference(ct, ct.getString(R.string.profile_key_number_verification_status)).equals(NEWUSER_PHONE_NUMBER_VERIFIED/*sms verification over*/)
                    || getPreference(ct, ct.getString(R.string.profile_key_number2_verification_status)).equals(NEWUSER_PHONE_NUMBER_VERIFIED);
        }
    }

    public static boolean isNumberVerifiedUserAdded(Context ct) {
        return getPreference(ct, ct.getString(R.string.profile_key_number_verification_status)).equals(REGISTERED_PHONE_NUMBER_VERIFIED)
                ||
                getPreference(ct, ct.getString(R.string.profile_key_number2_verification_status)).equals(REGISTERED_PHONE_NUMBER_VERIFIED);
    }

    public static boolean isSIM1Verified(Context ct) {
        return getPreference(ct, ct.getString(R.string.profile_key_number_verification_status)).equals(REGISTERED_PHONE_NUMBER_VERIFIED)
                ||
                getPreference(ct, ct.getString(R.string.profile_key_number_verification_status)).equals(NEWUSER_PHONE_NUMBER_VERIFIED);
    }

    public static boolean isSIM2Verified(Context ct) {
        return getPreference(ct, ct.getString(R.string.profile_key_number2_verification_status)).equals(REGISTERED_PHONE_NUMBER_VERIFIED)
                ||
                getPreference(ct, ct.getString(R.string.profile_key_number2_verification_status)).equals(NEWUSER_PHONE_NUMBER_VERIFIED);
    }

    public static String getPreference(Context ct, String key) {
        return PreferenceManager.getDefaultSharedPreferences(ct).getString(key, "");
    }

    public static long getPreferenceLong(Context ct, String key) {
        return PreferenceManager.getDefaultSharedPreferences(ct).getLong(key, 0);
    }

    public static void setPreference(Context ct, String key, String value) {
        // write to persistent preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ct);
        // SharedPreferences settings = getSharedPreferences(getString(R.string.profile_name), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setPreference(Context ct, String key, int value) {
        // write to persistent preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ct);
        // SharedPreferences settings = getSharedPreferences(getString(R.string.profile_name), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void setPreferenceLong(Context ct, String key, long value) {
        // write to persistent preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ct);
        // SharedPreferences settings = getSharedPreferences(getString(R.string.profile_name), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    void populateView() {
        name_old = getPreference(this, getString(R.string.profile_key_fullname));
        mNameView.setText(name_old);

        occupation_old = getPreference(this, getString(R.string.profile_key_occupation));
        String[] ab = occupation_old.split(", ");
        if (ab != null && ab.length > 0) {
            occupation_old = ab[0];
            if (ab.length > 1)
                company_old = ab[1];
        }
        Log.e(TAG, "populateView old occupation " + occupation_old + ", " + company_old);

        mOccupationView.setText(occupation_old);
        mCompanyView.setText(company_old);

        mMobileNumberView.setText(getPreference(this, getString(R.string.profile_key_mobile)));
        if (TelephonyInfo.getInstance(mContext).isDualSIM()) {
            mMobileNumber2View.setText(getPreference(this, getString(R.string.profile_key_mobile2)));
        }
        status_old = getPreference(this, getString(R.string.profile_key_status_headline));

    }

    private Drawable resize(Drawable image, int sz) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sz, sz, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//		getMenuInflater().inflate(R.menu.activity_register, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the Signup form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual Signup attempt is made.
     */
    public void attemptSignup() {

        // populate profile attributes
        persistProfile();

        boolean cancel = false;

        cancel = checkFormData();

        if (cancel) {
            // There was an error; don't attempt Signup and focus the first
            // form field with an error.
            // focusView.requestFocus();
        } else {
            //when every field value is correct, ask user for mobile number confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(SignupProfile.this, R.style.CaltxtAlertDialogTheme);
            builder.setPositiveButton(R.string.action_continue,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//                            LayoutInflater factory = LayoutInflater.from(SignupProfile.this);
//                            final View smsVerifyDialogView = factory.inflate(R.layout.activity_sms_code, null);
//                            final AlertDialog smsDialog = new AlertDialog.Builder(SignupProfile.this).create();
//                            smsDialog.setView(smsVerifyDialogView);

                            // Show a progress spinner, and kick off a background task to
                            ringProgressDialog = ProgressDialog.show(SignupProfile.this,
                                    "Verifying mobile number..", "", true);

                            /** CountDownTimer */
                            final int totalMsecs = Integer.parseInt((getResources()
                                    .getString(R.string.timeoutInMiliSecondsSignupCCW)));
                            int callInterval = 1000;
                            mVerificationCountdownTimer = new CountDownTimer(totalMsecs, callInterval) {
                                public void onTick(long millisUntilFinished) {
                                    int secondsRemaining = (int) millisUntilFinished / 1000;
                                    int minutesRemaining = secondsRemaining / 60;
                                    secondsRemaining -= minutesRemaining * 60;
//									float fraction = millisUntilFinished / (float) totalMsecs;

                                    // progress bar is based on scale of 1 to 100;
                                    ringProgressDialog.setMessage((minutesRemaining == 0 ? ""
                                            : minutesRemaining + " min ")
                                            + secondsRemaining + " sec ");
//									mProgress.setProgress((int) (fraction * 100));
//									mSignupStatusMessageView
//											.setText((minutesRemaining == 0 ? ""
//													: minutesRemaining + " min ")
//													+ secondsRemaining + " sec ");
                                }

                                public void onFinish() {
                                    mVerificationCountdownTimer.cancel();
                                    ringProgressDialog.dismiss();
                                }
                            }.start();
                            // perform the user Signup attempt.
                            // Globals.getConnection().registerChangeListener(this);

                            // Firebase Auth based sign up
                            initiateSignUpFirebase(mMobileNumberView.getText().toString(), mMobileNumber2View.getText().toString());
                            // SMS based sign up (SMS is sent by device itself)
                            //UserSignupTask mAuthTask = new UserSignupTask(mMobileNumberView.getText().toString(),
                                    //mMobileNumber2View.getText().toString());
                            //mAuthTask.execute((Void) null);
                        }
                    });
            builder.setNegativeButton(R.string.prompt_confirm_edit,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (!isSIM1Verified(SignupProfile.this)) {
//								mMobileNumberView.setError(getString(R.string.error_invalid_number_user_cancel));
                                mMobileNumberView.requestFocus();
                            }
                            if (!isSIM2Verified(SignupProfile.this)) {
//								mMobileNumber2View.setError(getString(R.string.error_invalid_number_user_cancel));
                                mMobileNumber2View.requestFocus();
                            }
                        }
                    });

            String CC = Addressbook.getInstance(getApplicationContext()).getMyCountryCode();//country code
//			if(CC.length()==0) {
//				CC = mCountriesSpinner.getSelectedItem().toString().trim().split(",")[1];
//			}
            String fqdn = "+" + CC + mMobileNumberView.getText().toString();
            String fqdn2 = "+" + CC + mMobileNumber2View.getText().toString();

            String[] additionalArgs = {(isSIM1Verified(SignupProfile.this)
                    ? "" : fqdn + "\n\n")
                    + ((TelephonyInfo.getInstance(mContext).isDualSIM()
                    && !isSIM2Verified(SignupProfile.this))
                    ? fqdn2 : "")};

            String actionTaken = getString(R.string.prompt_caltxt_signup_mobile, (Object[]) additionalArgs);
            builder.setMessage(actionTaken).setTitle(
                    TelephonyInfo.getInstance(mContext).isDualSIM() ? R.string.prompt_caltxt_signup2 : R.string.prompt_caltxt_signup);
//			builder.setIcon(R.drawable.ic_warning_white_24dp);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private boolean initiateSignUpFirebase(String phone1, String phone2) {

        if (((Caltxt) getApplication()).hasTelephony() == false || ((Caltxt) getApplication()).hasSim() == false)//make true to skip sms verification
            return false;//make true to skip sms verification

        String CC = Addressbook.getInstance(getApplicationContext()).getMyCountryCode();
        if (!isSIM1Verified(SignupProfile.this)) {

            sendVerificationCodeFirebase("+" + CC + phone1);

        } else if (TelephonyInfo.getInstance(mContext).isDualSIM()
                    && !isSIM2Verified(SignupProfile.this)) {

            sendVerificationCodeFirebase("+"+CC +phone2);

        } else {
            return false;
        }

        return true;
    }

    /**
     * Represents an asynchronous Signup/registration task used to authenticate
     * the user.

    class UserSignupTask extends AsyncTask<Void, Void, Boolean> {
        String sim1, sim2;

        public UserSignupTask(String sim1, String sim2) {
            super();
            // do stuff
            this.sim1 = sim1;
            this.sim2 = sim2;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (((Caltxt) getApplication()).hasTelephony() == false || ((Caltxt) getApplication()).hasSim() == false)//make true to skip sms verification
                return false;//make true to skip sms verification
            // send SMS with number entered by user on this sign-up page as BODY
            // and TO

            try {

                String CC = Addressbook.getInstance(getApplicationContext()).getMyCountryCode();//country code

                if (!isSIM1Verified(SignupProfile.this)) {
                    verificationPassSIM1 = verifyNumberSMS(CC + sim1);
                }

                if (TelephonyInfo.getInstance(mContext).isDualSIM()
                        && !isSIM2Verified(SignupProfile.this)) {
                    verificationPassSIM2 = verifyNumberSMS(CC + sim2);
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return false;
            }

            return verificationPassSIM1 | verificationPassSIM2;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            processPhoneVerificationResult(success);
        }

        @Override
        protected void onCancelled() {

            ringProgressDialog.dismiss();

        }
    }*/

    void processPhoneVerificationResult(Boolean success) {

        mVerificationCountdownTimer.cancel();
        ringProgressDialog.dismiss();
        // populate profile attributes

        if (success) {
            setPreference(SignupProfile.this, getString(R.string.profile_key_mobile), mMobileNumberView.getText().toString());
            if (verificationPassSIM1) {
                setPreference(mContext, getString(R.string.profile_key_number_verification_status), NEWUSER_PHONE_NUMBER_VERIFIED);

                /* set the username and numbers SIM1, SIM2 */
                Addressbook.getInstance(getApplicationContext()).getMyProfile().setUsername(XMob.toFQMN(mMobileNumberView.getText().toString(),
                        Addressbook.getInstance(getApplicationContext()).getMyCountryCode()));
//					Addressbook.getMyProfile().setNumber2(XMob.toFQMN(mMobileNumber2View.getText().toString(),
//							CaltxtApp.getMyCountryCode()));
                Log.w(TAG, "onPostExecute, NEWUSER_PHONE_NUMBER_VERIFIED " + mMobileNumberView.getText().toString());
            }

            setPreference(SignupProfile.this, getString(R.string.profile_key_mobile2), mMobileNumber2View.getText().toString());
            if (mMobileNumber2View.getText().toString().length() > 0 && verificationPassSIM2) {
                setPreference(mContext, getString(R.string.profile_key_number2_verification_status), NEWUSER_PHONE_NUMBER_VERIFIED);

                /* set the username and numbers SIM1, SIM2 */
                if (verificationPassSIM1) {
                    //username assigned to SIM1, assign SIM2 as number2
                    Addressbook.getInstance(getApplicationContext()).getMyProfile().setNumber2(XMob.toFQMN(mMobileNumber2View.getText().toString(),
                            Addressbook.getInstance(getApplicationContext()).getMyCountryCode()));
                } else {
                    //assign SIM2 as username
                    Addressbook.getInstance(getApplicationContext()).getMyProfile().setUsername(XMob.toFQMN(mMobileNumber2View.getText().toString(),
                            Addressbook.getInstance(getApplicationContext()).getMyCountryCode()));
                }
                Log.w(TAG, "onPostExecute, NEWUSER_PHONE_NUMBER_VERIFIED " + mMobileNumber2View.getText().toString());
            }

            setPreference(mContext, getString(R.string.profile_key_status_icon), R.drawable.ic_available_white_24dp);//init in db during signup; this icon res used when mqtt connected

            //rename the temp profile picture file to actual name
            File from = ImageLoader.getInstance(getApplicationContext()).getFile(userPicFileName, 0);
            if (from.exists()) {
                File to = ImageLoader.getInstance(getApplicationContext()).getFile(Addressbook.getInstance(getApplicationContext()).getMyProfile().getIcon(), 0);
                from.renameTo(to);
                uploadFromUri(Uri.fromFile(to));

                // create sim2 profile picture (copy)
                if (Addressbook.getInstance(mContext).getMyProfile().getNumber2() != null
                        && Addressbook.getInstance(mContext).getMyProfile().getNumber2().length() > 0) {
                    File file = saveAsFile(to, Addressbook.getInstance(getApplicationContext()).getMyProfile().getNumber2() + XMob.IMAGE_FILE_EXTN);
                    if (file != null)
                        uploadFromUri(Uri.fromFile(file));
                }
            }

            // start Caltxt activity
            Intent i = new Intent(SignupProfile.this, SplashScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else {
            if (!((Caltxt) getApplication()).hasTelephony()) {
                if (((Caltxt) getApplication()).hasSim()) {
                    Notify.toast(mMobileNumberView, mContext, "This app works with telephony device only", Toast.LENGTH_LONG);
                } else {
                    Notify.toast(mMobileNumberView, mContext, "Please insert SIM card", 2000);
                }
            } else {
                if (!isSIM1Verified(SignupProfile.this)) {
                    mMobileNumberView.setError(getString(R.string.error_incorrect_number));
                    mMobileNumberView.requestFocus();
                }

                if (!isSIM2Verified(SignupProfile.this)) {
                    mMobileNumber2View.setError(getString(R.string.error_incorrect_number));
                    mMobileNumber2View.requestFocus();
                } /*else {
						if(isNewUserAndNumberVerified(SignupProfile.this)) {
							mMobileNumberView.setError(getString(R.string.error_verified_already_number));
							mMobileNumberView.requestFocus();
						}
						if(isNumber2Verified(SignupProfile.this)) {
							mMobileNumber2View.setError(getString(R.string.error_verified_already_number));
							mMobileNumber2View.requestFocus();
						}
					}*/
            }
        }
    }

    private File saveAsFile(File file, String saveAsName) {
        File saveAsFile = ImageLoader.getInstance(getApplicationContext()).getFile(saveAsName, 0);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new FileOutputStream(saveAsFile);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            try {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
                in.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            saveAsFile.delete();
            saveAsFile = null;
            e.printStackTrace();
        }

        return saveAsFile;
    }

    /*boolean verifyNumberSMS(String fqdn) throws InterruptedException {
        boolean verificationPass = false;

        Log.d(TAG, "sendSMSing, received " + semaphore + ", " + fqdn);

        sendSMS(fqdn, fqdn);

        try {
            // wait for SMS to be received/processed by
            // AllPurposeBroadcastReceiver
            synchronized (semaphore) {
                //wait half (30 sec for each verification)
                semaphore.wait(Integer.parseInt((getResources()
                        .getString(R.string.timeoutInMiliSecondsSignupCCW))) / 2);
            }

        } catch (InterruptedException e) {
            Log.e(TAG, e.getLocalizedMessage());
            throw e;

        }

        if (semaphore.length() > 0 && semaphore.equals(fqdn)) {
//			verifiedNumbers.put(fqdn, true);
            verificationPass = true;
            Log.d(TAG, "sendSMS, received " + semaphore + ", " + fqdn);
        }

        return verificationPass;
    }*/

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        final String propertyName = event.getPropertyName();

        if (propertyName.equals(Constants.mqttPublishedProperty)) {
            // registration requested successfully
        } else if (propertyName.equals(Constants.mqttDeliveredProperty)) {
            // registration done successfully
            // we are done, listerner not required
//			Globals.getConnection().removeChangeListener(this);
        }
    }

    void persistProfile() {

        if (isNewUser(this) || isNewUserAndNumberVerified(this)) {//persist mobile number anyway
            String number = mMobileNumberView.getText().toString();
            String number2 = mMobileNumber2View.getText().toString();

            //store the verified number as key_mobile
            if (number.length() > 0) {
                setPreference(this, getString(R.string.profile_key_mobile), number);
            }
            //store the other number as key_mobile2
            if (number2.length() > 0 && !number.equals(number2)) {
                setPreference(this, getString(R.string.profile_key_mobile2), number2);
            }

            if (mCountriesSpinner.getSelectedItem() != null) {
                String codee = mCountriesSpinner.getSelectedItem().toString().trim().split("\\+")[1];
                Log.d(TAG, "persistProfile., " + codee);
                codee = codee.substring(0, codee.length()-1);
                Log.d(TAG, "persistProfile.., " + codee);
                Addressbook.getInstance(getApplicationContext()).setMyCountryCode(codee);
                Log.d(TAG, "persistProfile, " + mCountriesSpinner.getSelectedItem().toString().trim());
                setPreference(this, getString(R.string.profile_key_mobile_country_code), mCountriesSpinner.getSelectedItem().toString().trim());
            }
        }

//        if (Addressbook.getInstance(getApplicationContext()).getMyProfile() != null)
//            ModelFacade.getInstance().setUname(Addressbook.getInstance(getApplicationContext()).getMyProfile().getUsername());

        String name = mNameView.getText().toString();
        if (name.length() > 0 && Addressbook.getInstance(getApplicationContext()).getMyProfile() != null)
            Addressbook.getInstance(getApplicationContext()).getMyProfile().setName(name);

        boolean status_dirty = false;
        //WRITE ONLY IF VALUE CHANGED
        if (!name_old.equals(name)) {
            setPreference(this, getString(R.string.profile_key_fullname), name);
            status_dirty = true;
        }

        String occupation = mOccupationView.getText().toString();
        String company = mCompanyView.getText().toString();
        if (!company_old.equals(company) || !occupation_old.equals(occupation)) {
            occupation = occupation.length() == 0 ? company : occupation + ", " + company;
            Log.e(TAG, "persistProfile persist " + occupation);
            setPreference(this, getString(R.string.profile_key_occupation), occupation);
            if (Addressbook.getInstance(getApplicationContext()).getMyProfile() != null)
                Addressbook.getInstance(getApplicationContext()).getMyProfile().setOccupation(occupation);
//			Addressbook.getMyProfile().setLocation();
        }

        Log.e(TAG, "persistProfile old occupation " + occupation_old + ", " + company_old);
        Log.e(TAG, "persistProfile occupation " + occupation + ", " + company);

        if (status_dirty && isNumberVerifiedUserAdded(this)) {//broadcast new status
//			RebootService.BroadcastStatus();//- commented 5/APR/16 - integrated in Adapter::getView()
            //update status on server also
            RebootService.is_status_dirty = true;
        }
    }

    boolean checkFormData() {
        // Reset errors.
        mNameView.setError(null);
        mMobileNumberView.setError(null);
        mMobileNumber2View.setError(null);

        boolean cancel = false;
        View focusView = null;
        String mName = mNameView.getText().toString();
        String mMobileNumber = mMobileNumberView.getText().toString();
        String mMobileNumber2 = mMobileNumber2View.getText().toString();

        // Check for a valid mobile number.
        if (TextUtils.isEmpty(mMobileNumber)) {
            mMobileNumberView.setError(getString(R.string.error_field_required));
            focusView = mMobileNumberView;
            cancel = true;
        } else if (mMobileNumber.length() != 10
                || !TextUtils.isDigitsOnly(mMobileNumber)) {
            mMobileNumberView.setError(getString(R.string.error_invalid_number));
            focusView = mMobileNumberView;
            cancel = true;
        } else if (mMobileNumber.startsWith("0")) {
            mMobileNumberView.setError(getString(R.string.error_invalid_number_zero_prefix));
            focusView = mMobileNumberView;
            cancel = true;
        }

        if (TelephonyInfo.getInstance(this).isDualSIM()) {
            // Check for a valid mobile number.
            if (TextUtils.isEmpty(mMobileNumber2)) {
                mMobileNumber2View.setError(getString(R.string.error_field_required));
                focusView = mMobileNumber2View;
                cancel = true;
            } else if (mMobileNumber.equals(mMobileNumber2)) {
                mMobileNumber2View.setError(getString(R.string.error_invalid_number));
                focusView = mMobileNumber2View;
                cancel = true;
            } else if (mMobileNumber2.length() != 10
                    || !TextUtils.isDigitsOnly(mMobileNumber2)) {
                mMobileNumber2View.setError(getString(R.string.error_invalid_number));
                focusView = mMobileNumber2View;
                cancel = true;
            } else if (mMobileNumber2.startsWith("0")) {
                mMobileNumber2View.setError(getString(R.string.error_invalid_number_zero_prefix));
                focusView = mMobileNumber2View;
                cancel = true;
            }
        }

        // Check for a valid name.
        if (TextUtils.isEmpty(mName)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (mName.length() < 3) {
            mNameView.setError(getString(R.string.error_name_short));
            focusView = mNameView;
            cancel = true;
        }

        String country_selected = mCountriesSpinner.getSelectedItem().toString().trim().split("\\(")[0].trim();
        Log.v(TAG, TAG + "checkFormData country_selected "+country_selected);
        if(country_selected.length() == 0 || !country_selected.equals(cc_locale)) {
//            mCountriesSpinner.setError(getString(R.string.error_field_required));
            Toast.makeText(SignupProfile.this, "Please check the country selected", Toast.LENGTH_LONG).show();
            focusView = mCountriesSpinner;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt Signup and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
        }
        return cancel;
    }

    /*
     * public boolean isAlpha(String name) { return name.matches("[a-zA-Z ]+");
     * }

    //commented 07-JAN-19, Google policy change
    private void sendSMS(String phoneNo, String sms) {

        try {
            smsContentSent = sms;//set to match with SMS received

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+" + phoneNo, null, sms, null, null);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }*/

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,
                               long id) {
//		selectedCountryCode = (String)parent.getItemAtPosition(pos).toString().trim().split(",")[0];
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
/*
	String GetCountryISDCode() {

		String CountryID = "";
		String CountryISDCode = "";

		TelephonyManager manager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		// getNetworkCountryIso
		CountryID = getResources().getConfiguration().locale.getDisplayCountry();
//		CountryID = manager.getSimCountryIso().toUpperCase();
		String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
		for (int i = 0; i < rl.length; i++) {
			String[] g = rl[i].split(",");
//			countryList.put(g[0], g[1]);
			if (g[0].trim().equals(CountryID.trim())) {
				CountryISDCode = g[1];
				break;
			}
		}
		return CountryISDCode;
	}

	String GetCountry() {

		String Country = "";

//		TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		Country = getResources().getConfiguration().locale.getDisplayCountry();
//		CountryID = manager.getSimCountryIso().toUpperCase();
		return Country;
	}*/
    public ProgressDialog launchRingDialog(String title, String message) {
        final ProgressDialog ringProgressDialog = ProgressDialog.show(SignupProfile.this,
                title, message, true);
        ringProgressDialog.setCancelable(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Here you should write your time consuming task...
                    // Let the progress ring for 10 seconds...
                    Thread.sleep(10000);
                } catch (Exception e) {

                }
                ringProgressDialog.dismiss();
            }
        }).start();
        return ringProgressDialog;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Log.v(TAG, TAG + "::onRequestPermissionsResult");

        Map<String, Integer> perms = new HashMap<String, Integer>();
        switch (requestCode) {
            //commented 07-JAN-19 Google policy change
            /*case Caltxt.CALTXT_PERMISSIONS_REQUEST_SEND_SMS:
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
                    attemptSignup();
                }
                break;*/

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
