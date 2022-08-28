package com.jovistar.caltxt.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Caltxt;

public class About extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        ProgressBar pb = findViewById(R.id.progress_bar);
        pb.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.textsplash2)).setText(getVersionInfo());
    }

    //get the current version number and name
    public static String getVersionInfo() {
        String versionName = "";
        int versionCode = -1;
        try {
            PackageInfo packageInfo = Caltxt.getCustomAppContext().getPackageManager().getPackageInfo(Caltxt.getCustomAppContext().getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        TextView textViewVersionInfo = (TextView) findViewById(R.id.textview_version_info);
//        textViewVersionInfo.setText(String.format("Version name = %s \nVersion code = %d", versionName, versionCode));
        return String.format("Version %s.%d", versionName, versionCode);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
