package com.jovistar.caltxt.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Caltxt;

import java.util.HashMap;
import java.util.Map;

import static com.jovistar.caltxt.app.Caltxt.isPermissionGranted;

public class TOS extends AppCompatActivity {
    private static final String TAG = "TOS";

    AppCompatButton button_next;
    //	CheckBox tos_checkbox;
    TextView tos;
//	TOS activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tos);

//        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//	    getSupportActionBar().setLogo(R.drawable.ic_launcher);

//        getSupportActionBar().setTitle("Caltxt Terms of Service");

        button_next = findViewById(R.id.agree_button);
        tos = findViewById(R.id.terms_description);
        tos.setText(Html.fromHtml(getResources().getString(R.string.signup_terms_agreement)));
//        tos.setText(Html.fromHtml(getResources().getString(R.string.terms_of_service)));
//		tos_checkbox = (CheckBox)findViewById(R.id.checkbox_tos);
//		button_next.setEnabled(false);
//		button_next.setTextColor(Color.LTGRAY);
/*		tos_checkbox.setChecked(false);
        tos_checkbox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (tos_checkbox.isChecked()) {
					button_next.setEnabled(true);
//					button_next.setTextColor(getResources().getColor(R.color.buttontextwhitebackground));
				}else {
					button_next.setEnabled(false);
//					button_next.setTextColor(Color.LTGRAY);
				}
			}
		});
*/
        button_next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (PackageManager.PERMISSION_GRANTED != requestPermissionReadPhoneState()) {

                            return;
                        }

                        if (PackageManager.PERMISSION_GRANTED != requestPermissionReadContacts()) {

                            return;
                        }

                        if (PackageManager.PERMISSION_GRANTED != requestPermissionReadLocationFine()) {

                            return;
                        }

                        if (PackageManager.PERMISSION_GRANTED != requestPermissionReadLocation()) {

                            return;
                        }

                        Intent i = new Intent(getBaseContext(), SignupProfile.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                });
    }

    int requestPermissionReadPhoneState() {

        if (PackageManager.PERMISSION_GRANTED
                != Caltxt.checkPermission(TOS.this, Manifest.permission.READ_PHONE_STATE,
                Caltxt.CALTXT_PERMISSIONS_REQUEST_READ_PHONE_STATE,
                getResources().getString(R.string.permission_description_read_phone_state))) {

            return PackageManager.PERMISSION_DENIED;
        } else {
            return PackageManager.PERMISSION_GRANTED;
        }
    }

    int requestPermissionReadContacts() {

        if (PackageManager.PERMISSION_GRANTED
                != Caltxt.checkPermission(TOS.this, Manifest.permission.READ_CONTACTS,
                Caltxt.CALTXT_PERMISSIONS_REQUEST_READ_CONTACTS,
                getResources().getString(R.string.permission_description_read_contacts))) {

            return PackageManager.PERMISSION_DENIED;
        } else {
            return PackageManager.PERMISSION_GRANTED;
        }
    }

    int requestPermissionReadLocation() {

        if (PackageManager.PERMISSION_GRANTED
                != Caltxt.checkPermission(TOS.this, Manifest.permission.ACCESS_COARSE_LOCATION,
                Caltxt.CALTXT_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION,
                getResources().getString(R.string.permission_description_read_location))) {

            return PackageManager.PERMISSION_DENIED;
        } else {
            return PackageManager.PERMISSION_GRANTED;
        }
    }

    int requestPermissionReadLocationFine() {

        if (PackageManager.PERMISSION_GRANTED
                != Caltxt.checkPermission(TOS.this, Manifest.permission.ACCESS_FINE_LOCATION,
                Caltxt.CALTXT_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION,
                getResources().getString(R.string.permission_description_read_location))) {

            return PackageManager.PERMISSION_DENIED;
        } else {
            return PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    protected void onPause() {
        Log.v(TAG, TAG + "::onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, TAG + "::onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
/*		if (tos_checkbox.isChecked()) {
			button_next.setEnabled(true);
//			button_next.setTextColor(getResources().getColor(R.color.buttontextwhitebackground));
		}else {
			button_next.setEnabled(false);
//			button_next.setTextColor(Color.LTGRAY);
		}*/
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case Caltxt.CALTXT_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
            case Caltxt.CALTXT_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
            case Caltxt.CALTXT_PERMISSIONS_REQUEST_READ_CONTACTS:
            case Caltxt.CALTXT_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                for (int i = 0; i < permissions.length; i++) {
                    Log.v(TAG, TAG + "::onRequestPermissionsResult "+permissions[i] +" grant "+grantResults[i]);
                }

                if(isPermissionGranted(TOS.this, Manifest.permission.READ_PHONE_STATE)) {

                    if(isPermissionGranted(TOS.this, Manifest.permission.READ_CONTACTS)) {

                        if(isPermissionGranted(TOS.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            Log.v(TAG, TAG + "::onRequestPermissionsResult Permission Granted");

                            // Permission Granted
                            Intent i = new Intent(getBaseContext(), SignupProfile.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        } else {

                            if (PackageManager.PERMISSION_GRANTED != requestPermissionReadLocationFine()) {

                                return;
                            }
                        }
                    } else {

                        if (PackageManager.PERMISSION_GRANTED != requestPermissionReadContacts()) {

                            return;
                        }
                    }
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
