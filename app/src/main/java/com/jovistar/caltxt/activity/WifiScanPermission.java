package com.jovistar.caltxt.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.jovistar.caltxt.notification.Notify;

/**
 * Created by jovika on 3/6/2017.
 */

public class WifiScanPermission extends Activity {
    private static final String TAG = "WifiScanPermission";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Notify.playNotification(this);
        getWindow().addFlags(
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | /*14-SEP-16, WHY*/
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, 100);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "User enabled Scan always available", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "User did not enable Scan always available", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
