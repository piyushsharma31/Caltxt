package com.jovistar.caltxt.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnCloseListener;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.firebase.client.DatabaseFirebase;
import com.jovistar.caltxt.images.ImageLoader;
import com.jovistar.caltxt.mqtt.client.ConnectionMqtt;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.data.ConnectivityBroadcastReceiver;
import com.jovistar.caltxt.network.voice.CaltxtHandler;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Blockbook;
import com.jovistar.caltxt.phone.Logbook;
import com.jovistar.caltxt.phone.Searchbook;
import com.jovistar.commons.bo.XMob;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;

//import android.support.v4.app.widget.FloatingActionButton;
//import android.support.design.widget.NavigationView;

public class CaltxtPager extends AppCompatActivity implements PropertyChangeListener {
    private static final String TAG = "CaltxtPager";

    //	BroadcastReceiver mRegistrationBroadcastReceiver = null;
//	final int CALTXT_STATUS_RET_CODE = 45;
//	EditTextCaltxtStatus mPhoneStatusView;
    FloatingActionButton fab;
    ActionBar mActionBar;
    Toolbar mToolBar;
    TextView head_label, subject_label;
    ImageView head_image;
    androidx.appcompat.widget.AppCompatImageButton profile_edit;
    static int lastOpenFragment;
//	static boolean statusEditActionBarEnabled = false;

    private CaltxtFragment contactsFragment, callsFragment, blockedFragment, searchFragment;
    private ActivityCaltxtPagerAdapter mActivityCaltxtPagerAdapter;
    private ViewPager mViewPager;
    PagerSlidingTabStrip mTabs = null;
    AsyncTask<Void, Void, Void> getbookTask = null;
    private DrawerLayout mDrawerLayout;

    static String alert_to, alert_text, alert_title, alert_url;
    private static final int UPDATE_SETTING = 4;
    public static final int RESULT_GALLERY = 9;
    public static final int INSERT_CONTACT = 11;
    // search filter
    String mCurFilter;
    private CaltxtPager caltxtpager = this;
    public static boolean OP_GETALL_SVC_CALTXT_USER_COMPLETE = true;//if SVC_CALTXT_USER, OP_GETALL is initiated and completed or not

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.caltxt_pager);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        fab = findViewById(R.id.fab);

        //reset missed calls count
        Notify.auto_responded_call_count = 0;
        Notify.dnd_rejected_call_count = 0;
        Notify.blocked_call_count = 0;
        Notify.missed_call_count = 0;
        Notify.missed_call_response_count = 0;
        Notify.missed_text_count = 0;
//		updateNotifications();

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mActivityCaltxtPagerAdapter = new ActivityCaltxtPagerAdapter(
                getSupportFragmentManager());

        mViewPager = findViewById(R.id.caltxtpager);
        mViewPager.setAdapter(mActivityCaltxtPagerAdapter);

        if (getIntent().getExtras() != null) {
            String intentType = getIntent().getExtras().getString("fragment");
            if (intentType != null) {
                if (intentType.equals(Constants.FRAGMENT_BLOCKED)) {
                    mViewPager.setCurrentItem(lastOpenFragment = 3);
                } else if (intentType.equals(Constants.FRAGMENT_CALLS)) {
                    mViewPager.setCurrentItem(lastOpenFragment = 0);
                } else if (intentType.equals(Constants.FRAGMENT_CONTACTS)) {
                    mViewPager.setCurrentItem(lastOpenFragment = 1);
                } else if (intentType.equals(Constants.FRAGMENT_SEARCH)) {
                    mViewPager.setCurrentItem(lastOpenFragment = 2);
                }
            }
        } else {
            if (Logbook.get(getApplicationContext()).getCount() == 0) {
                mViewPager.setCurrentItem(lastOpenFragment = 1);//land in Contacts page
            } else {
                mViewPager.setCurrentItem(lastOpenFragment);//land in last open page
            }
        }

        // commented 14112019, do not need addressbook write permission. all edits/add/delete contacts is not allowed Caltxt
/*        if (!Caltxt.isPermissionGranted(this, "android.permission.WRITE_CONTACTS")) {
            if (PackageManager.PERMISSION_GRANTED !=
                    Caltxt.checkPermission(this, "android.permission.WRITE_CONTACTS",
                            Caltxt.CALTXT_PERMISSIONS_REQUEST_WRITE_CONTACTS,
                            "Caltxt need permission to read your contacts to build contact list. It will never upload your contacts" +
                                    " to remote server")) {
                return;
            }
        }
*/
        String first = SignupProfile.getPreference(this, getString(R.string.property_app_run_first_time));

        if (first.length() == 0) {
            SignupProfile.setPreference(this, getString(R.string.property_app_run_first_time),
                    "false");

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CaltxtAlertDialogTheme);
            builder.setPositiveButton(R.string.welcome_page_tour_button_text,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent it = new Intent(caltxtpager, TourPager.class);
                            startActivity(it);
                        }
                    });
            builder.setNegativeButton(R.string.welcome_page_skip,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.setMessage(R.string.welcome_page_subject).setTitle(R.string.welcome_page_title);
//			builder.setIcon(R.drawable.ic_warning_white_24dp);
            AlertDialog dialog = builder.create();
            dialog.show();

            // get the Caltxt users here again (RebootService also GET it).
            // again because during signup, the GET is done with partial contacts
            // Contacts still being loaded
/*			XPbk reg = new XPbk();
            reg.unm = Addressbook.getInstance(getApplicationContext()).getMyProfile().getUsername();
			reg.myPhStus = (Addressbook.getInstance(getApplicationContext()).getUnregisteredContacts());
			ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_CALTXT_USER,
					ModelFacade.getInstance().OP_GET, reg, caltxtpager);*/
        }

        mTabs = findViewById(R.id.caltxtpager_title_tabs);
        mTabs.setIndicatorColorResource(R.color.white);
        mTabs.setDividerColor(0);
//		mTabs.setUnderlineColorResource(R.color.white);
//		mTabs.setBackgroundResource(R.color.caltxt_color_statusbar);
//		mTabs.setShouldExpand(true);
        mTabs.setViewPager(mViewPager);
        mTabs.setTextColor(Color.WHITE);
//		mTabs.setIndicatorColor(Color.WHITE);
//		mTabs.setDividerColorResource(R.color.caltxt_color_statusbar);
//		mTabs.setIndicatorHeight(8);
//		mTabs.setUnderlineHeight(10);
//		mTabs.setVisibility(View.GONE);
//		mTabs.setFitsSystemWindows(true);
//		mTabs.setHorizontalScrollBarEnabled(false);

        //your other customizations related to tab strip...blahblah
        // Set last open tab selected
        LinearLayout mTabsLinearLayout = ((LinearLayout) mTabs.getChildAt(0));
        for (int i = 0; i < mTabsLinearLayout.getChildCount(); i++) {
            TextView tv = (TextView) mTabsLinearLayout.getChildAt(i);
            if (i == lastOpenFragment) {
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setTextColor(Color.GRAY);
            }
        }

        mTabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                // Determine whether the user is swiping forwards or backwards through the ViewPager
            }

            @Override
            public void onPageSelected(int position) {
                // When swiping between pages, select the corresponding tab.
                LinearLayout mTabsLinearLayout = ((LinearLayout) mTabs.getChildAt(0));
                for (int i = 0; i < mTabsLinearLayout.getChildCount(); i++) {
                    TextView tv = (TextView) mTabsLinearLayout.getChildAt(i);
                    if (i == position) {
                        tv.setTextColor(Color.WHITE);
                    } else {
                        tv.setTextColor(Color.GRAY);
                    }
                }

                if (getCurrentFragment() != null)
                    getCurrentFragment().resetEmptyView();

                switch (position) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        if (OP_GETALL_SVC_CALTXT_USER_COMPLETE &&
                                Searchbook.get(getApplicationContext()).getCount() == 0 && ConnectivityBroadcastReceiver.haveNetworkConnection()/*RebootService.getConnection(getApplicationContext())Connection.get().isConnected()*/) {

                            /*XMob user = new XMob();
                            user.setHeadline("");
                            user.setNumber(String.valueOf(Searchbook.get(getApplicationContext()).getCount()));
                            user.setUsername(XMob.toFQMN(user.getNumber(), Addressbook.getInstance(getApplicationContext()).getMyCountryCode()));
                            user.setName(String.valueOf(Searchbook.DISCOVER_COUNT_AT_A_TIME));
                            ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_CALTXT_USER,
                                    ModelFacade.getInstance().OP_GETALL, user, caltxtpager);*/
                            OP_GETALL_SVC_CALTXT_USER_COMPLETE = false;
                            if (searchFragment != null) {
                                // no need to show - firebase query is quick
//							searchFragment.mProgress.setVisibility(View.VISIBLE);
                            }
                            DatabaseFirebase.getNextRegisteredContacts(null);
                        }
                        break;
                    case 3:
                        break;
                    default:
                        break;
                }
//			    caltxtpager.supportInvalidateOptionsMenu();
//				getCurrentFragment().adapter.notifyDataSetInvalidated();
                lastOpenFragment = position;
            }
        });

        mToolBar = findViewById(R.id.toolbar);
//	    mToolBar.setNavigationIcon(R.drawable.ic_launcher);
/*	    mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CaltxtPager.this,"Navigation",Toast.LENGTH_SHORT).show();
            }
        });*/
        setSupportActionBar(mToolBar);
        mActionBar = getSupportActionBar();
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
//	    mActionBar.setHomeAsUpIndicator(Addressbook.get().getMyStatusIconResourceSquare());
        mActionBar.setDisplayHomeAsUpEnabled(true);
//	    mActionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu);
        //	    mActionBar.setDisplayHomeAsUpEnabled(true);
//	    mActionBar.setHomeButtonEnabled(true);
//	    mActionBar.show();
//	    mActionBar.setDisplayShowHomeEnabled(true);
//	    mActionBar.setIcon(R.drawable.ic_launcher);
//		mActionBar.setDisplayShowCustomEnabled(true);
//	    mActionBar.setDisplayUseLogoEnabled(true);
//	    mActionBar.setDisplayShowTitleEnabled(true);
//	    mActionBar.setLogo(R.drawable.ic_launcher);
//	    mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE
//	    		|ActionBar.DISPLAY_SHOW_HOME);

        Connection.get().registerChangeListener(this);
        updateActionBarTitle();
/*
		int abTitleId = getResources().getIdentifier("action_bar_title", "id", "android");
		if(abTitleId==0){
			abTitleId = (R.id.action_bar_title);//for v7 appcompat
		}
		findViewById(abTitleId).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				statusEditActionBarEnabled = true;
//				Log.i(TAG, "statusEditActionBarEnabled TRUE");
//				mActionBar.setDisplayShowTitleEnabled(false);
//				mActionBar.setDisplayShowHomeEnabled(false);
//			    caltxtpager.supportInvalidateOptionsMenu();
		        final String[] listStatus = new String[] { XMob.STRING_STATUS_DND, XMob.STRING_STATUS_AVAILABLE, XMob.STRING_STATUS_AWAY, XMob.STRING_STATUS_BUSY};
		        final ListPopupWindow lpw = new ListPopupWindow(caltxtpager);
		        ProfileStatusAdapter adapter = new ProfileStatusAdapter(caltxtpager, listStatus);
		        lpw.setAdapter(adapter);
//		        lpw.setHeight(500);
		        lpw.setAnchorView(
//		        		caltxtpager.getWindow().getDecorView()
		        		getCurrentFragment().getView()
		        		);
		        lpw.setModal(true);
//		        ColorDrawable cd = new ColorDrawable(R.color.white);
//		        cd.setAlpha(255);
//		        lpw.setBackgroundDrawable(cd);
		        lpw.setOnDismissListener(new PopupWindow.OnDismissListener(){

					@Override
					public void onDismiss() {
				    	lpw.dismiss();
					}

		        });
		        lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				        String status = listStatus[position];
				        int left_icon_resource = R.drawable.available;

						if (status.equals(XMob.STRING_STATUS_DND)) {
							left_icon_resource = ( R.drawable.dnd);
						} else if (status.equals(XMob.STRING_STATUS_AVAILABLE)) {
							left_icon_resource = ( R.drawable.available);
						} else if (status.equals(XMob.STRING_STATUS_AWAY)) {
							left_icon_resource = ( R.drawable.away);
						} else if (status.equals(XMob.STRING_STATUS_BUSY)) {
							left_icon_resource = ( R.drawable.busy);
						}

				        Addressbook.get().setMyStatusFromIconResource(left_icon_resource);
				        SignupProfile.setPreference(caltxtpager,
				        		getString(R.string.profile_key_status_icon), 
				        		left_icon_resource);

						XRes res = new XRes();
						res.op = -1;
						res.svc = -1;
						res.status = new XReqSts();
						res.status.cd = CCMException.SUCCCESS;

						if(status.length()>0) {
							Addressbook.getMyProfile().setHeadline(status);
							SignupProfile.setPreference(Globals.getCustomAppContext(),
									getString(R.string.profile_key_status_headline),
									status);
							RebootService.is_status_dirty = true;
							//update status on server also
							ModelFacade.getInstance().fxAsyncServiceRequest(
									ModelFacade.getInstance().SVC_CALTXT_USER,
									ModelFacade.getInstance().OP_SET, Addressbook.getMyProfile(), 
									caltxtpager);
//							RebootService.BroadcastStatus();//- commented 5/APR/16 - integrated in Adapter::getView()
						}

			    		updateActionBarTitle();
						lpw.dismiss();
					}
		        });
		        lpw.show();
			}
		});*/

        /***
         *** login to CCM so that Caltxt user list can be refreshed and
         *** user pictures can be retrieved
         ***/
        /*if (CCWService.isCCMLoggedIn() == false) {
            Log.i(TAG, "onCreate TRYING TO LOGIN...");
            XUsr user = new XUsr();
            user.id = Addressbook.getInstance(getApplicationContext()).getMyProfile().getUsername();
            user.pwd = Addressbook.getInstance(getApplicationContext()).getIMEI();
            user.cmnt = Addressbook.getInstance(getApplicationContext()).getIMEI();
            ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_USER,
                    ModelFacade.getInstance().OP_LOGIN, user, this);
        }*/

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
		    	/*Snackbar.make(findViewById(R.id.activity_caltxtpager), "I'm a Snackbar", Snackbar.LENGTH_LONG).setAction("Action", new View.OnClickListener() {
	                @Override
	                public void onClick(View v) {
	                    Toast.makeText(CaltxtPager.this, "Snackbar Action", Toast.LENGTH_LONG).show();
	                }
	            }).show();*/
//		        final Intent intent = new Intent(CaltxtPager.this, CaltxtStatusList.class);
//		    	startActivityForResult(intent, CALTXT_STATUS_RET_CODE);

//				Intent it = new Intent(CaltxtPager.this, ActivityCaltxtStatusPicker.class);
                Intent it = new Intent(CaltxtPager.this, CaltxtStatusPager.class);
                startActivity(it);
//		    	openStatusDlg();
            }
        });

        if (PackageManager.PERMISSION_GRANTED
                != Caltxt.checkPermission(this, "android.permission.ACCESS_COARSE_LOCATION",
                Caltxt.CALTXT_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION,
                "Caltxt needs permission to use nearby WiFi hotspots to tag your status")) {
//			finish();
            return;
        }

//		notifyDataSetChangedFragments();
    }

    AlertDialog statusDlg;
    TypedArray imgs;

    //	public void openStatusDlg() {
//		Intent it = new Intent(caltxtpager, CaltxtStatusPicker.class);
//		startActivity(it);
//	}
	/*
	public void openStatusDlg() {

		ArrayList<CaltxtStatus> statusList = new ArrayList<CaltxtStatus>();
        String[] statusnames = getResources().getStringArray(R.array.preference_status_list_titles);
//        String[] statuscodes = getResources().getStringArray(R.array.preference_status_list_values);
        imgs = getResources().obtainTypedArray(R.array.status_icons);
        for(int i = 0; i < statusnames.length; i++){
            statusList.add(new CaltxtStatus(statusnames[i], imgs.getResourceId(i, -1)));
        }*/
/*
		ListAdapter adapter = new CaltxtStatusListAdapter(caltxtpager, Addressbook.get().getStatusList());
		final AlertDialog.Builder builder = new AlertDialog.Builder(caltxtpager, R.style.CaltxtAlertDialogTheme);
		builder.setTitle("Change status");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
		        Log.d(TAG, "onItemClick my status "+ Addressbook.getMyProfile().getStatus());
                Toast.makeText(CaltxtPager.this, "Item Selected: " + arg1, Toast.LENGTH_SHORT).show();
                imgs.recycle();
			}
			
		});

		statusDlg = builder.create();
		statusDlg.show();
		Intent it = new Intent(CaltxtPager.this, ActivityCaltxtStatusPicker.class);
		startActivity(it);
	}*/
	/*
	public void closeStatusDlg() {
		if(statusDlg!=null) {
			statusDlg.dismiss();
		}
		if(imgs!=null) {
			imgs.recycle();
		}
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = listStatus[position];
        mPhoneStatusView.setText(item);
        int left_icon_resource = Constants.icon_notify_available;

		if (item.equals(XMob.STRING_STATUS_DND)) {
			left_icon_resource = ( Constants.icon_notify_dnd);
		} else if (item.equals(XMob.STRING_STATUS_AVAILABLE)) {
			left_icon_resource = ( Constants.icon_notify_available);
		} else if (item.equals(XMob.STRING_STATUS_AWAY)) {
			left_icon_resource = ( Constants.icon_notify_away);
		} else if (item.equals(XMob.STRING_STATUS_BUSY)) {
			left_icon_resource = ( Constants.icon_notify_busy);
		}
		Drawable d1 = this.getResources().getDrawable(left_icon_resource);
		d1 = resize(this, d1, 48);
		Drawable d2 = this.getResources().getDrawable(R.drawable.ic_action_done);
        mPhoneStatusView.setCompoundDrawablesWithIntrinsicBounds( d1, null, d2, null);
//        mPhoneStatusView.setCompoundDrawablesWithIntrinsicBounds( left_icon_resource, 0, R.drawable.numberpicker_down_normal_holo_light, 0);

        Addressbook.get().setMyStatusFromIconResource(left_icon_resource);
//        int status = Addressbook.get().getStatusFromIconResource(left_icon_resource);
        SignupProfile.setPreference(this, getString(R.string.profile_key_status_icon), left_icon_resource);
//		Addressbook.getMyProfile().setStatus(status);
        Log.d(TAG, "onItemClick my status "+ Addressbook.getMyProfile().getStatus());

        lpw.dismiss();
    }
*/
    public static Drawable resize(Context context, Drawable image, int sz) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sz, sz, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);
    }

    private CaltxtFragment getCurrentFragment() {
        return mActivityCaltxtPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
//		return currentFragment;
    }

//	public void setCurrentFragment(CaltxtFragment f) {
//		currentFragment = f;
//	}

    public static void updateNotifications(Context context) {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
        XQrp qrp = Persistence.getInstance(context).getQuickAutoResponse();

        if ((qrp != null && qrp.getAutoResponseEndTime() > Calendar.getInstance().getTimeInMillis())) {
            String when = DateUtils.getRelativeDateTimeString(context, qrp.getAutoResponseEndTime(),
                    DateUtils.DAY_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
//					if(RebootService.getConnection().isConnected()) {
            Notify.notify_caltxt_autoresponse_enabled(context, qrp.getQuickResponseValue(), when);
//					} else {
//				    	Notify.notify_caltxt_autoresponse_set_disconnected(qrp.getQuickResponseValue(), when);
//					}
        } else if (Addressbook.getInstance(context).getMyProfile().isDND()) {
            Notify.notify_caltxt_call_dnd_enabled(context, Calendar.getInstance().getTimeInMillis());
        } else {
//	        		Notify.notify_caltxt_status_change(Addressbook.getMyProfile().getHeadline(),
//	        				"", "", Addressbook.get().getMyStatusIconResource());
        }

//			}
//		});
    }

    private void updateActionBarTitle() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /* reset the Action Bar title and Icon */
//				mActionBar.setHomeButtonEnabled(true);
//	    		mActionBar.setDisplayShowTitleEnabled(true);
//	    		mActionBar.setDisplayShowHomeEnabled(true);
                mActionBar.setTitle(/*Html.fromHtml("<b>" + */Addressbook.getInstance(getApplicationContext()).getMyProfile().getName()/* + "</b>")*/);
                if (ConnectionMqtt.getConnection(getApplicationContext()) == null || !Connection.get().isConnected()) {
                    mActionBar.setSubtitle(XMob.STRING_STATUS_OFFLINE);
                } else {
                    mActionBar.setSubtitle(/*Html.fromHtml("<small>" + */
                            Addressbook.getInstance(getApplicationContext()).getMyProfile().getPlace().trim().length() == 0 ?
                                    Addressbook.getInstance(getApplicationContext()).getMyProfile().getHeadline() :
                                    Addressbook.getInstance(getApplicationContext()).getMyProfile().getHeadline()
                                            + ", " + Addressbook.getInstance(getApplicationContext()).getMyProfile().getPlace()
//							Addressbook.getMyProfile().getHeadline()
                            /* + "</small>")*/);
                }
//				mActionBar.setHomeAsUpIndicator(Addressbook.get().getMyStatusIconResourceSquare());
                mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
                fab.setImageResource(Addressbook.getInstance(getApplicationContext()).getContactStatusIconResource(Addressbook.getInstance(getApplicationContext()).getMyProfile()));
//				fab.setImageResource(R.drawable.ic_person_white_24dp);
                fab.setBackgroundTintList(ColorStateList.valueOf(Addressbook.getInstance(getApplicationContext()).getMyStatusColor()));
//				mToolBar.setNavigationIcon(Addressbook.get().getMyStatusIconResourceSquare());

                initNavigationDrawer();
            }
        });
    }

    public void initNavigationDrawer() {

        NavigationView navigationView = findViewById(R.id.navigation_view);
        View header = navigationView.getHeaderView(0);
        head_label = header.findViewById(R.id.header_label);
        head_label.setText(Addressbook.getInstance(getApplicationContext()).getMyProfile().getName());
        subject_label = header.findViewById(R.id.subject_label);
        subject_label.setText(Addressbook.getInstance(getApplicationContext()).getMyProfile().getNumber());
        head_image = header.findViewById(R.id.header_image);
        ImageLoader.getInstance(getApplicationContext()).DisplayImage(Addressbook.getInstance(getApplicationContext()).getMyProfile().getIcon(),
                head_image, 140, R.drawable.ic_person_white_24dp, true);
        head_image.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDrawerLayout.closeDrawers();
                Intent it = new Intent(CaltxtPager.this, SignupProfile.class);
                startActivityForResult(it, UPDATE_SETTING);
            }

        });
        profile_edit = header.findViewById(R.id.profile_edit);
        profile_edit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDrawerLayout.closeDrawers();
                Intent it = new Intent(CaltxtPager.this, SignupProfile.class);
                startActivityForResult(it, UPDATE_SETTING);
            }
        });
//	    navigationView.setItemIconTintList(null);//remove icon grey color
        final Menu menu = navigationView.getMenu();
//		if(Addressbook.getMyProfile().getUsername().equals("919953693002")
//				|| XMob.toFQMN(Addressbook.getMyProfile().getNumber2(), CaltxtApp.getMyCountryCode()).equals("919953693002")) {
        if (Addressbook.getInstance(getApplicationContext()).isItMe("919953693002")) {
            menu.findItem(R.id.pager_action_sendalert).setVisible(true);
        } else {
            menu.findItem(R.id.pager_action_sendalert).setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
//		            menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.pager_action_faq:
                        Intent it = new Intent(CaltxtPager.this, FAQ.class);
                        startActivity(it);
                        break;
		    			/*case R.id.pager_action_profile:
		    				it = new Intent(CaltxtPager.this, SignupProfile.class);
		    				startActivityForResult(it, UPDATE_SETTING);
		    				break;*/
                    /* 04-JAN-19, commented due to Google policy change*/
                    case R.id.pager_action_settings:
                        Log.d(TAG, "setNavigationItemSelectedListener, action_settings");
                        it = new Intent(CaltxtPager.this, Settings.class);
                        startActivityForResult(it, UPDATE_SETTING);
                        break;
                    case R.id.pager_action_tour:
                        it = new Intent(CaltxtPager.this, TourPager.class);
                        startActivity(it);
                        break;
                    case R.id.pager_action_share:
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.service_sms_invite));
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Share using"));
                        return true;
                    case R.id.pager_action_about:
                        it = new Intent(CaltxtPager.this, About.class);
                        startActivity(it);
                        return true;
		    	/*		case R.id.pager_action_blocklist:
		    				it = new Intent(this, ActivityBlockbook.class);
		    				startActivity(it);
		    				return true;*/
                    case R.id.pager_action_sendalert:
                        if (/*RebootService.getConnection(getApplicationContext())*/Connection.get().isConnected() == false) {
                            Notify.toast(mDrawerLayout, CaltxtPager.this, getString(R.string.no_internet_message), Toast.LENGTH_LONG);
                            return false;
                        }
                        AlertDialog.Builder alert = new AlertDialog.Builder(CaltxtPager.this, R.style.CaltxtAlertDialogTheme);
                        alert.setTitle("Send alert");
//		    				alert.setMessage("Send alert message");
//		    				final EditTextCaltxt fromNameEditText = new EditTextCaltxt(this);fromNameEditText.setText(alert_fromname);fromNameEditText.setHint("Type sender name");
//		    				final EditText fromNumEditText = new EditText(this);fromNumEditText.setText(alert_fromnum);fromNumEditText.setHint("Type sender number");
//		    				fromNumEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                        final EditText toEditText = new EditText(CaltxtPager.this);
                        toEditText.setText(alert_to);
                        toEditText.setHint("Type receiver number");
                        toEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                        final EditTextCaltxt alertEditText = new EditTextCaltxt(CaltxtPager.this);
                        alertEditText.setText(alert_text);
                        alertEditText.setHint("Type message");
                        alertEditText.setSingleLine(false);
                        final EditTextCaltxt titleEditText = new EditTextCaltxt(CaltxtPager.this);
                        titleEditText.setText(alert_title);
                        titleEditText.setHint("Type title");
                        titleEditText.setSingleLine(false);
                        final EditTextCaltxt urlEditText = new EditTextCaltxt(CaltxtPager.this);
                        urlEditText.setText(alert_url);
                        urlEditText.setHint("Type url");
                        LinearLayout layout = new LinearLayout(CaltxtPager.this);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(48, 0, 48, 0);
                        toEditText.setLayoutParams(lp);
                        toEditText.setGravity(android.view.Gravity.TOP|android.view.Gravity.LEFT);

                        InputFilter[] filterArray = new InputFilter[1];
                        filterArray[0] = new InputFilter.LengthFilter(12);
                        titleEditText.setFilters(filterArray);

                        filterArray[0] = new InputFilter.LengthFilter(20);
                        alertEditText.setFilters(filterArray);

                        filterArray[0] = new InputFilter.LengthFilter(20);
                        urlEditText.setFilters(filterArray);

                        filterArray[0] = new InputFilter.LengthFilter(20);
                        toEditText.setFilters(filterArray);

//		    			    layout.addView(fromNameEditText);
//		    			    layout.addView(fromNumEditText);
                        layout.addView(titleEditText, lp);
                        layout.addView(alertEditText, lp);
                        layout.addView(urlEditText, lp);
                        layout.addView(toEditText, lp);

                        alert.setPositiveButton(R.string.action_send,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* soft keyboard HIDE*/
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(alertEditText.getWindowToken(), 0);

                                        if (alertEditText.getText().length() == 0) {
                                            return;
                                        }

//		    							if (toEditText.getText().toString().equals(Addressbook.getMyProfile().getUsername())) {
                                        if (Addressbook.getInstance(getApplicationContext()).isItMe(toEditText.getText().toString())) {
                                            Notify.toast(mDrawerLayout, caltxtpager, caltxtpager.getString(R.string.alert_cannotsend_self), Toast.LENGTH_LONG);
                                            return;
                                        }

                                        CaltxtHandler.get(CaltxtPager.this).publishAlert(
                                                alert_to = toEditText.getText().toString(),
                                                alert_text = alertEditText.getText().toString(),
                                                alert_title = titleEditText.getText().toString(),
                                                alert_url = urlEditText.getText().toString());
                                    }
                                });

                        alert.setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        /* soft keyboard HIDE*/
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(alertEditText.getWindowToken(), 0);
//		    							getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);/* soft keyboard HIDDEN*/
                                    }
                                });

//		    				if (alertEditText.getParent() != null)
//		    					((ViewGroup) alertEditText.getParent()).removeAllViews();

                        alert.setView(layout);
                        alert.show();//.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                        return true;

                    case R.id.pager_action_sendfeedback:
                        if (/*RebootService.getConnection(getApplicationContext())*/Connection.get().isConnected() == false) {
                            Notify.toast(mDrawerLayout, CaltxtPager.this, getString(R.string.no_internet_message), Toast.LENGTH_LONG);
                            return false;
                        }
                        /*AlertDialog.Builder */
                        alert = new AlertDialog.Builder(CaltxtPager.this, R.style.CaltxtAlertDialogTheme);
                        alert.setTitle("Your feedback");
                        alert.setMessage("Send us your suggestion, feedback or report an issue");
                        final EditText feedbackEditText = new EditText(CaltxtPager.this);
                        feedbackEditText.setHint("Type your feedback");
                        filterArray = new InputFilter[1];
                        filterArray[0] = new InputFilter.LengthFilter(150);
                        feedbackEditText.setFilters(filterArray);

                        layout = new LinearLayout(CaltxtPager.this);
                        layout.setOrientation(LinearLayout.VERTICAL);
//                        final LayoutParams lparam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT); // Width , height
                        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(48, 0, 48, 0);
                        feedbackEditText.setLayoutParams(lp);
                        feedbackEditText.setGravity(android.view.Gravity.TOP|android.view.Gravity.LEFT);
                        layout.addView(feedbackEditText, lp);
//		    				feedbackEditText.setFocusableInTouchMode(true);
//		    				feedbackEditText.requestFocus();
//		    				getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);/* soft keyboard open*/
//		    				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//		    				imm.showSoftInput(feedbackEditText, 0);//does not work

                        alert.setPositiveButton(R.string.action_send,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* soft keyboard HIDE*/
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(feedbackEditText.getWindowToken(), 0);
//		    							getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//		    							String value = feedbackEditText.getText().toString();
                                        if (feedbackEditText.getText().length() == 0) {
                                            return;
                                        }
                                        Log.i(TAG, "feedback message:" + feedbackEditText.getText());
                                        DatabaseFirebase.sendCaltxtFeedback(feedbackEditText.getText().toString());
//                                        XMsg msg = new XMsg();
                                        // msg.bdy = feedbackTextBox.getString();
//                                        msg.frm = ModelFacade.getInstance()
//                                                .getThisUsername();
//                                        msg.sub = feedbackEditText.getText().toString();
//                                        msg.to.add("support");
//                                        msg.unred = 1;
//                                        ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_FEEDBACK_CALTXT,
//                                                ModelFacade.getInstance().OP_SEND, msg, CaltxtPager.this);
                                    }
                                });

                        alert.setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        /* soft keyboard HIDE*/
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(feedbackEditText.getWindowToken(), 0);
//		    							getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);/* soft keyboard HIDDEN*/
                                    }
                                });

//                        if (feedbackEditText.getParent() != null)
//                            ((ViewGroup) feedbackEditText.getParent()).removeAllViews();

                        alert.setView(layout);
                        alert.show();//.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                        return true;
                }

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 27-JAN-17: for situations when app was left open and screen went idle / doze mode
        // connection is broken when in doze or idle mode, so reconnect
        // commented 26-FEB-17, MqttService should take care of it!
/*		if(ConnectivityBroadcastReceiver.haveNetworkConnection(getApplicationContext())
			&& !RebootService.getConnection(getApplicationContext()).isConnectedOrConnecting()) {

			startService(new Intent(CaltxtPager.this, RebootService.class).putExtra("caller", "RebootReceiver"));
//			Intent broadcastIntent = new Intent("com.jovistar.caltxt.message.RestartService");
//			sendBroadcast(broadcastIntent);
		}
*/
        updateActionBarTitle();
    }

    @Override
    protected void onPause() {
//		LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//		if(connection!=null)
//			connection.removeChangeListener(changeListener);
        Connection.get().removeChangeListener(this);
        Log.i(TAG, "CaltxtPager::onDestroy");
        super.onDestroy();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            doSearch(intent.getStringExtra(SearchManager.QUERY));
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void doSearch(String query) {
        filter(query);
    }

    private void filter(String filterpattern) {
        getCurrentFragment().filter(filterpattern);
        getCurrentFragment().notifyDataSetChanged();
    }

    private void notifyDataSetChangedFragments() {
        runOnUiThread(new Runnable() {
            public void run() {
                if (callsFragment != null) {
                    callsFragment.notifyDataSetChanged();
                }
                if (contactsFragment != null) {
                    contactsFragment.notifyDataSetChanged();
                }
                if (blockedFragment != null) {
                    blockedFragment.notifyDataSetChanged();
                }
                if (searchFragment != null) {
                    searchFragment.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.caltxt_menu_toolbar, menu);
//		getMenuInflater().inflate(R.menu.caltxt_log_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.pager_action_search);
        final SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(searchItem);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                String newFilter = !TextUtils.isEmpty(newText) ? newText
                        : null;
                // Don't do anything if the filter hasn't actually changed.
                // Prevents restarting the loader when restoring state.
                if (mCurFilter == null && newFilter == null) {
                    return true;
                }
                if (mCurFilter != null && mCurFilter.equals(newFilter)) {
                    return true;
                }
                mCurFilter = newFilter;
                filter(mCurFilter);
                if (mViewPager.getCurrentItem() == 2) {
                    if (newFilter != null && newFilter.length() > 3 && OP_GETALL_SVC_CALTXT_USER_COMPLETE
                            && ConnectivityBroadcastReceiver.haveNetworkConnection()/*RebootService.getConnection(getApplicationContext())Connection.get().isConnected()*/) {

//                        XMob user = new XMob();
//                        user.setHeadline(newFilter);
//                        user.setName(String.valueOf(Searchbook.DISCOVER_COUNT_AT_A_TIME));
//                        user.setNumber(String.valueOf(/*Searchbook.get().getCount()*/0)/*,
//								CaltxtApp.getMyCountryCode()*/);
//                        user.setUsername(XMob.toFQMN(user.getNumber(), Addressbook.getInstance(getApplicationContext()).getMyCountryCode()));
//                        ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_CALTXT_USER,
//                                ModelFacade.getInstance().OP_GETALL, user, caltxtpager);
                        OP_GETALL_SVC_CALTXT_USER_COMPLETE = false;
                        if (searchFragment != null) {
                            // no need to show - firebase query is quick
//							searchFragment.mProgress.setVisibility(View.VISIBLE);
                        }
                        DatabaseFirebase.getMatchingContactsByValue(newFilter);
                    }
                } else {
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return true;
            }
        });
        searchView.setOnCloseListener(new OnCloseListener() {
            @Override
            public boolean onClose() {
                mCurFilter = null;
                filter(mCurFilter);
                searchView.setQuery("", false);

                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i(TAG, "onPrepareOptionsMenu statusEditActionBarEnabled "
//				+statusEditActionBarEnabled
                + ", getCurrentItem " + mViewPager.getCurrentItem());

		/*if(statusEditActionBarEnabled) {
	    	menu.findItem(R.id.pager_action_dialer_pad).setVisible(false);
	    	menu.findItem(R.id.pager_action_add_person).setVisible(false);
	    	menu.findItem(R.id.pager_action_refresh).setVisible(false);
	    	menu.findItem(R.id.pager_action_clearblock).setVisible(false);
	    	menu.findItem(R.id.pager_action_search).setVisible(false);
		} else {*/
//			menu.findItem(R.id.pager_action_dialer_pad).setVisible(false);

        if (mViewPager.getCurrentItem() == 0) {//calls
//		    	menu.findItem(R.id.pager_action_dialer_pad).setVisible(true);
            menu.findItem(R.id.pager_action_add_person).setVisible(false);
            menu.findItem(R.id.pager_action_refresh).setVisible(false);
            menu.findItem(R.id.pager_action_clearblock).setVisible(false);
        } else if (mViewPager.getCurrentItem() == 1) {//contacts
            //commented 14112019, do not allow contact addressbook edit actions
//            menu.findItem(R.id.pager_action_add_person).setVisible(true);
            menu.findItem(R.id.pager_action_refresh).setVisible(true);
            menu.findItem(R.id.pager_action_clearblock).setVisible(false);
        } else if (mViewPager.getCurrentItem() == 2) {//search
            menu.findItem(R.id.pager_action_clearblock).setVisible(false);
            menu.findItem(R.id.pager_action_add_person).setVisible(false);
            menu.findItem(R.id.pager_action_refresh).setVisible(false);
        } else if (mViewPager.getCurrentItem() == 3) {//blocked
            menu.findItem(R.id.pager_action_clearblock).setVisible(true);
            menu.findItem(R.id.pager_action_add_person).setVisible(true);
            menu.findItem(R.id.pager_action_refresh).setVisible(false);
        }
//		}
        //commented 14112019, do not allow addressbook changes from caltxt (user uses phone addressbook)
        //uncommented 21112019 to allow adding number series to block. E.g. 91140, 91140321 etc.
//        menu.findItem(R.id.pager_action_add_person).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.pager_action_refresh:
//				if(ConnectivityBroadcastReceiver.haveNetworkConnection(this)) {
//			        startService(new Intent(ActivityPhonebook.this, RebootService.class).putExtra("caller", "LoadAddressbook"));
//					Addressbook.get().updateStatusAllOffline();
//					Addressbook.get().clear();
//				Persistence.getInstance(this).clearXMOB();//commented, no need to clear the database
//					if(RebootService.isCCMLoggedIn()) {
//						((Globals)getApplication()).getImageLoader().clearCache();//clear image cache to fetch updates (if any) from server
//					} else {
//						RebootService.getConnection().try2LoginCCM();
//					}
//						syncView();
                if (getbookTask == null || getbookTask.getStatus() == AsyncTask.Status.FINISHED) {
                    getbookTask = new GetPhoneAddressbook(this).execute();
                } else {
                    Notify.toast(mDrawerLayout, this, "Already refreshing contacts", Toast.LENGTH_LONG);
                }
//				} else {
//					Notify.toast(this, getString(R.string.no_internet_message), Toast.LENGTH_SHORT);
//				}
                return true;
            case R.id.pager_action_search:// called in devices with OS < 30; later version
                // show SearchView in action-bar
//				onSearchRequested();//calling this will invoke Search Dialog in API 10 device
                return true;
            case R.id.pager_action_clearblock: {
                if (blockedFragment.getCount() == 0)
                    return true;
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CaltxtAlertDialogTheme);
                builder.setPositiveButton(R.string.action_clear,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Blockbook.getInstance(getApplicationContext()).clear();
                                blockedFragment.clear();
                                Connection.get().addAction(Constants.contactUnblockedProperty, null, null);
                            }
                        });
                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.setMessage(R.string.prompt_caltxt_block_delete_msg).setTitle(R.string.prompt_caltxt_block_delete);
//				builder.setIcon(R.drawable.ic_warning_white_24dp);
                AlertDialog dialog = builder.create();
                dialog.show();

                return true; }
            case R.id.pager_action_dialer_pad:
                /** Creating an intent with the dialer's action name  */
                /** Since the intent is created with activity's action name, the intent is an implicit intent */
                Intent intent = new Intent("android.intent.action.DIAL");

                /** Starting the Dialer activity */
                startActivity(intent);
                return true;
            case R.id.pager_action_add_person: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CaltxtAlertDialogTheme);
                builder.setTitle("Add number to blocked list");

                // Set up the input
                final EditTextCaltxt input = new EditTextCaltxt(this);
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(12);
                input.setFilters(filterArray);

                LinearLayout container = new LinearLayout(this);
                container.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(48, 0, 0, 0);
                input.setLayoutParams(lp);
                input.setGravity(android.view.Gravity.TOP|android.view.Gravity.LEFT);
                input.setLines(1);
                input.setHint("Type number with country code");
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setMaxLines(1);
                container.addView(input, lp);

                builder.setView(container);

                // Set up the buttons
                builder.setPositiveButton(R.string.action_block, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().length() == 0) {
                            Notify.toast(mViewPager, getApplicationContext(), "Phone number is blank", Toast.LENGTH_LONG);
                            return;
                        }
                        //get country code
                        String number_to_block = input.getText().toString();
                        String number_country = "";
                        String number_country_code = "";
                        String country = "";
                        String country_code = "";
                        String[] countries = getResources().getStringArray( R.array.CountryCodes );
                        for(int i=0; i<countries.length; i++) {
                            country = countries[i].split("\\(")[0].trim();//take out country name
                            country_code = countries[i].split("\\(")[1].trim();
                            country_code = country_code.substring(1, country_code.length()-1);
                            if(number_to_block.startsWith(country_code)) {
                                number_country_code = country_code;
                                if(number_country.length()>0) {
                                    number_country = number_country +", " + country;
                                } else {
                                    number_country = country;
                                }
//                                break;
                            }
                        }
                        Log.i(TAG, "onOptionsItemSelected action_block country " + country+", code "+number_country_code+", country "+number_country+", number "+number_to_block);
                        String slogan = Addressbook.getInstance(getApplicationContext()).getContactNameFromPhoneAddressbook(number_to_block);
                        if(slogan == null) {
                            if(number_country.length()==0) {
                                if(number_to_block.length() < 10) {
                                    slogan = "All numbers starting "+number_to_block;
                                } else {
                                    slogan = number_to_block;
                                }
                            } else {
                                slogan = "All "+number_country + " numbers" +
                                        (number_to_block.length() == number_country_code.length()
                                                ?"":" starting "+number_to_block.substring(number_country_code.length()));
                            }
                        }
                        Log.i(TAG, "onOptionsItemSelected slogan " + slogan);
                        Blockbook.getInstance(getApplicationContext()).add(number_to_block, slogan);
                        Connection.get().addAction(Constants.contactBlockedProperty, null, null);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show(); }

                //commented 21112019, do not allow editing phone address book. use this menu to add number series to block
/*                Intent it = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(it, INSERT_CONTACT);*/
                return true;
	/*		case R.id.pager_action_clearlog:
				if (caltxtpager.size() == 0)
					return true;
				AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CaltxtAlertDialogTheme);
				builder.setPositiveButton(R.string.action_clear,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Logbook.get().clear();
								log_adapter.notifyDataSetChanged();
							}
						});
				builder.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				builder.setMessage(R.string.prompt_caltxt_log_clear_msg).setTitle(R.string.prompt_caltxt_log_clear);
//				builder.setIcon(R.drawable.ic_warning_white_24dp);
				AlertDialog dialog = builder.create();
				dialog.show();

				return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
        // Handle presses on the action bar items
		/*switch (item.getItemId()) {
		case android.R.id.home:
			Intent it = new Intent(caltxtpager, QuickResponseEdit.class);
			startActivity(it);
			return true;
		case R.id.pager_action_settings:
			// Launching Preferences Screen
			it = new Intent(this,
					Settings.class);
			startActivityForResult(it, UPDATE_SETTING);
			return true;
		}*/
    }

    /*
        @Override
        public boolean onSearchRequested() {
            return super.onSearchRequested();
        }
    */

    public static int convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int dp = (int) (px / (metrics.densityDpi / 160f));
        return dp;
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
		/*case (CALTXT_STATUS_RET_CODE):
	        if(resultCode == Activity.RESULT_OK){
	            String statusString = data.getStringExtra(CaltxtStatusList.RESULT_STATUSNAME);
	            if(statusString.equals("Automatic Response")) {
	    			Intent it = new Intent(caltxtpager, QuickResponseEdit.class);
	    			startActivity(it);
	            } else {
	            	changeStatus(statusString);
	            }
//                Toast.makeText(CaltxtPager.this, "Snackbar Action "+statusString, Toast.LENGTH_LONG).show();
	        }
			break;*/
            case (UPDATE_SETTING):
                updateActionBarTitle();
                break;
		/*case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);

				if (c.moveToFirst()) {
					// other data is available for the Contact. I have decided
					// to only get the name of the Contact.
					final String name = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
					String id = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String hasPhone = c
							.getString(c
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					String number = "";

					if (hasPhone.equalsIgnoreCase("1")) {
						Cursor phones = getContentResolver()
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = " + id, null, null);
						phones.moveToFirst();
						final CharSequence[] al = new CharSequence[phones
								.getCount()];
						int i = 0;
						while (phones.isAfterLast() == false) {
							int type = phones.getInt(phones
									.getColumnIndexOrThrow(Phone.TYPE));
							number = phones.getString(phones
									.getColumnIndex("data1"));
							al[i++] = (number);
							phones.moveToNext();
						}
						if (al.length > 1) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									this, R.style.CaltxtAlertDialogTheme);
							builder.setTitle(name).setItems(al,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											promptContextCall(name,
													al[which].toString());
										}
									});
							AlertDialog ad = builder.create();
							ad.show();
						} else if (al.length == 1) {
							promptContextCall(name, al[0].toString());
						}

					}
				}
			}*/
            case (INSERT_CONTACT):
                Log.i(TAG, "onActivityResult ENTRY1 code " + resultCode);
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "onActivityResult " + number);
                        while (c.isAfterLast() == false) {
                            // TODO Whatever you want to do with the selected contact name.
                            c.moveToNext();
                        }
                    }
                    c.close();
                }
                break;
            default:
                break;
        }
    }

    class GetPhoneAddressbook extends AsyncTask<Void, Void, Void> {
        CaltxtPager activity;

        public GetPhoneAddressbook(CaltxtPager activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {//called before doInBackground on UI thread
            super.onPreExecute();
            if (contactsFragment != null) {
                contactsFragment.mProgress.setVisibility(View.VISIBLE);
            }
/*			View progressStatusView = findViewById(R.id.progress_status);
			TextView textView = (TextView) findViewById(R.id.progress_status_textview);
			SignupProfile.showProgress(true, phonebookgui.getDTOView(), progressStatusView, textView, "");*/
        }

        @Override
        protected Void doInBackground(Void... args) {//invoked on the background thread immediately after onPreExecute()
//			Collection<XMob> adb = Addressbook.get().readPhoneAddressBook();
            Addressbook.getInstance(getApplicationContext()).syncWithPhoneAddressbook();
//			Logbook.get().updateNames(Addressbook.getInstance(getApplicationContext()).getContacts());
//            XPbk reg = null;

//			if(adb.size()>0) {
            if (ConnectivityBroadcastReceiver.haveNetworkConnection()) {
                ImageLoader.getInstance(getApplicationContext()).clearCache();//clear image cache to fetch updates (if any) from server
                Addressbook.getInstance(getApplicationContext()).updateStatusAllUnregistered();//reset all contacts as unregistered
                //get the latest set of caltxt users
//                reg = new XPbk();
//                reg.unm = Addressbook.getInstance(getApplicationContext()).getMyProfile().getUsername();
//                reg.myPhStus = /*new ArrayList<XMob>(adb);//*/(Addressbook.getInstance(getApplicationContext()).getUnregisteredContacts());
//                ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_CALTXT_USER,
//                        ModelFacade.getInstance().OP_GET, reg, caltxtpager);
                DatabaseFirebase.checkAndSyncAddressbook();

                Log.i(TAG, "doInBackground OP_GET..SVC_CALTXT_USER");
                //so that the address book is sync with phone book
//				Addressbook.get().syncAddressbookAndDB(new ArrayList<XMob>(adb));
//			}
//                return reg.myPhStus;
            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Notify.toast(mDrawerLayout, getApplicationContext(), getString(R.string.no_internet_message), 3000);
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {//invoked on the UI thread after the background computation finishes
            if (contactsFragment != null) {
                contactsFragment.mProgress.setVisibility(View.GONE);
                contactsFragment.notifyDataSetChanged();//reflect change on the view
            }
//			syncView();
/*
			View progressStatusView = findViewById(R.id.progress_status);
			TextView textView = (TextView) findViewById(R.id.progress_status_textview);
			SignupProfile.showProgress(false, phonebookgui.getDTOView(), progressStatusView, textView, "");
*/
//			if(v.size()==0)
//				Notify.toast(Globals.getCustomAppContext(), "Contact list is up to date", Toast.LENGTH_LONG);
//			getbookTask = null;
            super.onPostExecute(v);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.caltxt_log_menu_context, menu);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        final String propertyName = event.getPropertyName();
        final Object newValue = event.getNewValue() == null ? "" : event.getNewValue();
        Log.d(TAG, "propertyChange " + propertyName + ", " + event.getNewValue());
        // getNewValue() will always return a IDTObject in string form.
        // XMob will start with 3^ or 4^
        // XCtx will start with 1^
/*
		runOnUiThread(new Runnable() {
			public void run() {

				if(XCtx.isXCtx(newValue.toString())) {
					// Caltxt update received
					XCtx ctx = new XCtx();
					ctx.init(newValue.toString());

					int count = getCurrentFragment().getCount();

					for(int i=0; i<count; i++) {
						IDTObject obj = getCurrentFragment().getItem(i);
						if(obj.getCName().equals("XCtx")) {
							// Caltxt log activity is current
							XCtx tctx = (XCtx) obj;

							if(tctx.getPersistenceId()==ctx.getPersistenceId()) {
								// found matching Caltxt log
								getCurrentFragment().notifyItemChanged(i);
								Log.i(TAG, "propertyChange "+propertyName+", pid "+tctx.getPersistenceId()+", object "+obj);

								// only one XCtx object with persistence id exists always
								break;
							}
						} else {
							break;
						}
					}

				} else if(XMob.isXMob(newValue.toString())) {
					// Contact update received
					XMob mob = new XMob();
					mob.init(newValue.toString());

					if(false==Addressbook.getInstance(getApplicationContext()).isItMe(mob.getUsername())) {
						int count = getCurrentFragment().getCount();

						for(int i=0; i<count; i++) {
							IDTObject obj = getCurrentFragment().getItem(i);
							if(obj.getCName().equals("XMob")) {
								// Contact activity is currently open
								if(((XMob)obj).getUsername()==mob.getUsername()) {
									// update matching contact
									getCurrentFragment().notifyItemChanged(i);
									Log.i(TAG, "propertyChange "+propertyName+", object "+obj);
								}
							} else if(obj.getCName().equals("XCtx")) {
								// Caltxt log activity is current
								XCtx ctx = (XCtx) obj;
								if(ctx.getUsernameCaller().equals(mob.getUsername()) || ctx.getNumberCallee().equals(mob.getUsername())) {
									// update matching contact
									getCurrentFragment().notifyItemChanged(i);
									Log.i(TAG, "propertyChange "+propertyName+", object "+obj);
								}
							} else {
								break;
							}
						}
					}
				}
			}
		});
*/
        if (propertyName.equals(Constants.messageArrivedProperty)
                || propertyName.equals(Constants.messageUpdateProperty)
                || propertyName.equals(Constants.mqttPublishingProperty)
                || propertyName.equals(Constants.mqttPublishedProperty)
                || propertyName.equals(Constants.mqttDeliveredProperty)
                || propertyName.equals(Constants.firebasePublishingProperty)
                || propertyName.equals(Constants.firebasePublishedProperty)
                || propertyName.equals(Constants.firebaseDeliveredProperty)
                || propertyName.equals(Constants.smsPublishedProperty)
                || propertyName.equals(Constants.smsDeliveredProperty/*SMS*/)
                || propertyName.equals(Constants.callIncomingEndProperty)
                || propertyName.equals(Constants.callIncomingMissedProperty)
                || propertyName.equals(TelephonyManager.EXTRA_STATE_IDLE)
                || propertyName.equals(Constants.callOutgoingEndProperty)) {

            runOnUiThread(new Runnable() {
                public void run() {
                    updateActionBarTitle();
                }
            });
            notifyDataSetChangedFragments();
        } else if (propertyName.equals(Constants.contactBlockedProperty)
                || propertyName.equals(Constants.contactUnblockedProperty)
                || propertyName.equals(Constants.contactDeleteProperty)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mTabs.notifyDataSetChanged();
                }
            });
            notifyDataSetChangedFragments();
        } else if (propertyName.equals(Constants.myStatusChangeProperty)) {
			/*final String status = (String) event.getNewValue();

			if(status.length()>0) {
				Addressbook.getMyProfile().setHeadline(status);
				SignupProfile.setPreference(CaltxtApp.getCustomAppContext(),
						getString(R.string.profile_key_status_headline),
						status);
				RebootService.is_status_dirty = true;
				//update status on server also
				ModelFacade.getInstance().fxAsyncServiceRequest(
						ModelFacade.getInstance().SVC_CALTXT_USER,
						ModelFacade.getInstance().OP_SET, Addressbook.getMyProfile(), 
						caltxtpager);
			}*/
            if (Addressbook.getInstance(getApplicationContext()).getMyProfile().isDND()) {
                Notify.toast(mViewPager, getApplicationContext(),
                        "Incoming calls will be blocked", Toast.LENGTH_LONG);
            } else if (Addressbook.getInstance(getApplicationContext()).getMyProfile().isAutoResponding()) {
                Notify.toast(mViewPager, getApplicationContext(),
                        "Automatic response will be sent to calling contacts", Toast.LENGTH_LONG);
            } else {
                Log.d(TAG, "propertyChange, Status changed");
                if (ConnectionMqtt.getConnection(this) != null && /*ConnectionMqtt.getConnection(this)*/Connection.get().isConnected()) {
                    Log.d(TAG, "propertyChange, Status changed to");
                    Notify.toast(mViewPager, getApplicationContext(), "Status changed to " +
                                    (Addressbook.getInstance(this).getMyProfile().getPlace().length() == 0 ?
                                            Addressbook.getInstance(this).getMyProfile().getHeadline() :
                                            Addressbook.getInstance(this).getMyProfile().getHeadline())
                                    + (Addressbook.getInstance(this).getMyProfile().getPlace().length() == 0 ?
                                    "" : (", " + Addressbook.getInstance(this).getMyProfile().getPlace())),
                            Toast.LENGTH_LONG);
                }
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    updateActionBarTitle();
                }
            });
        } else if (propertyName.equals(Constants.logDeletedProperty)) {
            filter(mCurFilter);
        } else if (propertyName.equals(Constants.usersSearchResultProperty)
                || propertyName.equals(Constants.usersMatchResultProperty)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (newValue != null) {
                        Log.w(TAG, "handleCallback usersSearchResultProperty " + newValue);
                        XMob mob = Addressbook.getInstance(getApplicationContext()).get(newValue);
                        if (mob == null) {
                            mob = new XMob((String) newValue, (String) newValue, Addressbook.getMyCountryCode());
                        }

                        if (!mob.getUsername().equals(Addressbook.getMyProfile().getUsername())) {
                            Searchbook.get(getApplicationContext()).prepend(mob);
                            //broadcast your status and get others!!
                            searchFragment.adapter.notifyDataSetChanged();
                        }
                    }
                    searchFragment.mProgress.setVisibility(View.GONE);
                }
            });
        } else if (propertyName.equals(Constants.contactNameUpdatedProperty)
                || (propertyName.equals(Constants.contactPhotoChangeProperty))) {
        } else if (propertyName.equals(Constants.contactNameAddProperty)) {
//			runOnUiThread(new Runnable() {
//				public void run() {
            ArrayList<XMob> mobs = (ArrayList<XMob>) event.getNewValue();
            Addressbook.getInstance(getApplicationContext()).syncAddressbookAndDB(mobs);
            //refresh to find all caltxt users from contacts
//            XPbk reg = new XPbk();
//            reg.unm = Addressbook.getInstance(getApplicationContext()).getMyProfile().getUsername();
//            reg.myPhStus = mobs;
//            ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_CALTXT_USER,
//                    ModelFacade.getInstance().OP_GET, reg, caltxtpager);

//				}
//			});
        } else if (propertyName.equals(Constants.ConnectionStatusProperty)) {
//			notifyDataSetChangedFragments();
            runOnUiThread(new Runnable() {
                public void run() {
                    updateActionBarTitle();
                }
            });
            updateNotifications(getApplicationContext());
        }

        // commented 03-2018, due to flickering view
//        notifyDataSetChangedFragments();
    }

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    public class ActivityCaltxtPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<CaltxtFragment> registeredFragments = new SparseArray<CaltxtFragment>();

        public ActivityCaltxtPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CaltxtFragment getItem(int id) {
            CaltxtFragment fragment = new CaltxtFragment();
            Bundle args = new Bundle();
            args.putInt(CaltxtFragment.ARG_OBJECT, id + 1);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return Constants.FRAGMENT_CALLS;
            } else if (position == 1) {
                return Constants.FRAGMENT_CONTACTS;
            } else if (position == 2) {
                return Constants.FRAGMENT_SEARCH;
            } else if (position == 3) {
                if (Blockbook.getInstance(getApplicationContext()).getCount() > 0) {
                    return Constants.FRAGMENT_BLOCKED + " (" + Blockbook.getInstance(getApplicationContext()).getCount() + ")";
                } else {
                    return Constants.FRAGMENT_BLOCKED;
                }
            }
            return "HELO";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            CaltxtFragment fragment = (CaltxtFragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            if (position == 0) {
                callsFragment = fragment;
            } else if (position == 1) {
                contactsFragment = fragment;
            } else if (position == 2) {
                searchFragment = fragment;
            } else if (position == 3) {
                blockedFragment = fragment;
            }
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public CaltxtFragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
/*
	CaltxtFragment getCallsFragment() {
		CaltxtFragment fragment = null;
		Log.i(TAG, "getCallsFragment NULL?"+(callsFragment==null));

		if(callsFragment==null) {
			Bundle args = new Bundle();

			fragment = callsFragment = new CaltxtFragment();
			args.putInt(CaltxtFragment.ARG_OBJECT, 1);
			callsFragment.setArguments(args);
		} else {
			fragment = callsFragment;
		}
		return fragment;
	}

	CaltxtFragment getContactsFragment() {
		CaltxtFragment fragment = null;
		Log.i(TAG, "getContactsFragment NULL?"+(contactsFragment==null));

		if(contactsFragment==null) {
			Bundle args = new Bundle();
			fragment = contactsFragment = new CaltxtFragment();
			args.putInt(CaltxtFragment.ARG_OBJECT, 2);
			contactsFragment.setArguments(args);
		} else {
			fragment = contactsFragment;
		}
		return fragment;
	}

	CaltxtFragment getBlockedFragment() {
		CaltxtFragment fragment = null;
		Log.i(TAG, "getBlockedFragment NULL?"+(blockedFragment==null));

		if(blockedFragment==null) {
			Bundle args = new Bundle();
			fragment = blockedFragment = new CaltxtFragment();
			args.putInt(CaltxtFragment.ARG_OBJECT, 3);
			blockedFragment.setArguments(args);
		} else {
			fragment = blockedFragment;
		}
		return fragment;
	}*/
/*
	@Override
	public boolean onNavigationItemSelected(int arg0, long arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	int mLastFirstVisibleItem;
	boolean mIsScrollingUp = false;
	private static int preLast;

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		final ListView lw = getCurrentFragment().getListView();
    	final int currentFirstVisibleItem = lw.getFirstVisiblePosition();

		if(scrollState == 0) {
		}

        if (view.getId() == lw.getId()) {
        	if (currentFirstVisibleItem > mLastFirstVisibleItem) {
        		mIsScrollingUp = false;
//                mActionBar.hide();
        	} else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
        		mIsScrollingUp = true;
//                mActionBar.show();
        	}

        	mLastFirstVisibleItem = currentFirstVisibleItem;
        }
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		switch(view.getId()) {
			case android.R.id.list:

			// Make your calculation stuff here. You have all your
			// needed info from the parameters of this function.

			// Sample calculation to determine if the last
			// item is fully visible.
			if(visibleItemCount==0 || firstVisibleItem==0)
				break;
			final int lastItem = firstVisibleItem + visibleItemCount;
			if(lastItem == totalItemCount) {
				if(preLast!=lastItem){ //to avoid multiple calls for last item
		    		int count = Searchbook.get().getCount();
					Log.d(TAG, "firstVisibleItem "+firstVisibleItem+", visibleItemCount "
					+visibleItemCount+", lastItem "+lastItem
					+", preLast "+preLast +", view.getId() "+view.getId());

					preLast = lastItem;

			    	if(mViewPager.getCurrentItem()==3 && OP_GETALL_SVC_CALTXT_USER_COMPLETE && 
			    			RebootService.getConnection().isConnected()) {

						XMob user = new XMob();
						user.setHeadline(mCurFilter==null?"":mCurFilter);
						if(Searchbook.search_result_contain_self) {
							count = count + 1;
						}
						user.setNumber(String.valueOf(count), 
								Globals.getMyCountryCode());
						user.setUsername(XMob.toFQMN(user.getNumber(), CaltxtApp.getMyCountryCode()));
						user.setName(String.valueOf(Searchbook.DISCOVER_COUNT_AT_A_TIME));
						ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_CALTXT_USER, 
			            		ModelFacade.getInstance().OP_GETALL, user, caltxtpager);
			    		OP_GETALL_SVC_CALTXT_USER_COMPLETE = false;

			    		if(searchFragment!=null) {
							searchFragment.mProgress.setVisibility(View.VISIBLE);
						}
			    	}
				}
			}
		}
	}*/
}
