package com.jovistar.commons.util;

import android.util.Log;

public class Logr {
	public static int level = Log.VERBOSE;

	public static void setLevel(int l) {
		level = l;
	}

	public static void v(String cat, String msg) {
		if (com.jovistar.commons.BuildConfig.DEBUG && level<=Log.VERBOSE) {
			Log.v(cat, msg);
		}
	}

	public static void d(String cat, String msg) {
		if (com.jovistar.commons.BuildConfig.DEBUG && level<=Log.DEBUG) {
			Log.d(cat, msg);
		}
	}

	public static void i(String cat, String msg) {
		if (com.jovistar.commons.BuildConfig.DEBUG && level<=Log.INFO) {
			Log.i(cat, msg);
		}
	}

	public static void e(String cat, String msg) {
		if (com.jovistar.commons.BuildConfig.DEBUG && level<=Log.ERROR) {
			Log.e(cat, msg);
		}
	}

	public static void w(String cat, String msg) {
		if (com.jovistar.commons.BuildConfig.DEBUG && level<=Log.WARN) {
			Log.w(cat, msg);
		}
	}
}
