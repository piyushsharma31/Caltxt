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

                        //ommented 23SEP2022, call it after signup
                        /*if (PackageManager.PERMISSION_GRANTED
                                != Caltxt.checkPermission(TOS.this, "android.permission.READ_PHONE_STATE",
                                Caltxt.CALTXT_PERMISSIONS_REQUEST_READ_PHONE_STATE,
                                "Caltxt need permission to manage your phone calls")) {

                            return;
                        }*/

						/*
						if(PackageManager.PERMISSION_GRANTED
								!= CaltxtApp.checkPermission(this, "android.permission.INTERNET",
										CaltxtApp.CALTXT_PERMISSIONS_REQUEST_ACCESS_INTERNET)) {

							return;
						}
				*/
//						if (tos_checkbox.isChecked()) {
                        Intent i = new Intent(getBaseContext(), SignupProfile.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
//						}
                    }
                });
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

        Log.v(TAG, TAG + "::onRequestPermissionsResult");
        Map<String, Integer> perms = new HashMap<String, Integer>();
        switch (requestCode) {
            case Caltxt.CALTXT_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Intent i = new Intent(getBaseContext(), SignupProfile.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else {
                    // Permission Denied
                }
                finish();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
