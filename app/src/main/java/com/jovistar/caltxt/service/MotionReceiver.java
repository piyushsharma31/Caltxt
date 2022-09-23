package com.jovistar.caltxt.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.util.Log;

/**
 * Created by jovika on 3/6/2017.
 */

public class MotionReceiver {
    private static final String TAG = "MotionReceiver";

    static boolean RECEIVER_REGISTERED = false;
    // 02-2018, default value changed to true; so that scan always run on first install
    // or fresh app run
    static boolean DEVICE_MOVED_SIGNIFICANTLY = true;
    static MotionReceiver mr = null;

    private MotionReceiver() {
    }

    public static MotionReceiver getInstance() {
        if (mr == null) {
            mr = new MotionReceiver();
        }
        return mr;
    }

    public boolean isMotionSensorAvailable(final Context context) {
        SensorManager mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        return mSensor != null;
    }

    public void registerMotionReceiver(final Context context) {
        if (RECEIVER_REGISTERED /*|| !MOTION_SENSOR_AVAILABLE*/)
            return;

        Sensor mSensor;
        TriggerEventListener mTriggerEventListener = null;

        SensorManager mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        if (mSensor == null) {
            Log.d(TAG, "registerMotionReceiver RECEIVER_REGISTERED " + RECEIVER_REGISTERED);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mTriggerEventListener = new TriggerEventListener() {
                @Override
                public void onTrigger(TriggerEvent event) {
                    // Do work
                    DEVICE_MOVED_SIGNIFICANTLY = true;
                    RECEIVER_REGISTERED = false;// it disables itself
                    MotionReceiver.getInstance().registerMotionReceiver(context.getApplicationContext());
                }
            };
            mSensorManager.requestTriggerSensor(mTriggerEventListener, mSensor);
            RECEIVER_REGISTERED = true;
            Log.d(TAG, "registerMotionReceiver RECEIVER_REGISTERED " + RECEIVER_REGISTERED);
        }
    }

    public boolean hasMoved(final Context context) {
        if (isMotionSensorAvailable(context) == true) {
            // if sensor available then return actual status=DEVICE_MOVED_SIGNIFICANTLY
            return DEVICE_MOVED_SIGNIFICANTLY;
        }
        return true;
    }

    public void resetMoved(final Context context) {
        if (isMotionSensorAvailable(context) == true) {
            // if sensor available then reset DEVICE_MOVED_SIGNIFICANTLY
            DEVICE_MOVED_SIGNIFICANTLY = false;
        }
    }
}
