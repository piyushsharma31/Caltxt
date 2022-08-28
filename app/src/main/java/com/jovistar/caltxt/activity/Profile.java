package com.jovistar.caltxt.activity;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.images.ImageLoader;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Blockbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.ArrayList;

public class Profile extends AppCompatActivity implements PropertyChangeListener {
    private static final String TAG = "Profile";

    private final int ADD_CONTACT = 5, UPDATE_CONTACT = 6;
    int ContactID = -1;//contact ID in phone address book
    ArrayList<String> numbers;
    // UI references.
    private TextView mCardTitle;
    //	private TextView mMobileNumberView;
    private TextView mPhoneStatusView;
    private ImageView mPhoneStatusIcon;
    private TextView mLastSeen;
    private ImageView mPicView, /*mIconRight, */
            mExpandedPicView;
    private AppCompatButton mButtonCall, mButtonCall2, mButtonBlock, mButtonDelete;

    //	Context mContext = this;
    int finalHeight = 0, finalWidth = 0;
    XMob mob = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mob = (XMob) getIntent().getSerializableExtra("IDTOBJECT");
        Log.i(TAG, "onCreate " + mob);

        Connection.get().registerChangeListener(this);

        setContentView(R.layout.profile);
        mButtonBlock = findViewById(R.id.button_block);
        mButtonBlock.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                blockContact(arg0);
            }

        });

        mButtonDelete = findViewById(R.id.button_delete);
        mButtonDelete.setVisibility(View.GONE);//hidden 07112019 dont allow edit on phone addressbook
        if (Addressbook.getInstance(getApplicationContext()).isContact(mob.getUsername())) {
            mButtonDelete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    deleteContact(arg0);
                }

            });
        } else {
            mButtonDelete.setVisibility(View.GONE);
            findViewById(R.id.divider2).setVisibility(View.GONE);
        }

        if (mob.getOccupation() != null && mob.getOccupation().length() > 0) {
            TextView mOccupation = findViewById(R.id.occupation);
            findViewById(R.id.card_occupation_view).setVisibility(View.VISIBLE);
            mOccupation.setText(mob.getOccupation());
        } else {
            findViewById(R.id.card_occupation_view).setVisibility(View.GONE);
        }

        if (Blockbook.getInstance(getApplicationContext()).get(mob.getUsername()) != null) {
            Drawable d1 = getResources().getDrawable(R.drawable.ic_remove_circle_outline_black_24dp);
            Drawable wrappedDrawable = DrawableCompat.wrap(d1);
            wrappedDrawable = wrappedDrawable.mutate();
            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
            d1.invalidateSelf();
            mButtonBlock.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);
            mButtonBlock.setText(R.string.action_unblock_contact);
            mButtonBlock.setTextColor(Color.RED);
        } else {
            Drawable d1 = this.getResources().getDrawable(R.drawable.ic_block_black_24dp);
            Drawable wrappedDrawable = DrawableCompat.wrap(d1);
            wrappedDrawable = wrappedDrawable.mutate();
            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
            d1.invalidateSelf();
            mButtonBlock.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);
            mButtonBlock.setText(R.string.action_block_contact);
            mButtonBlock.setTextColor(getResources().getColor(R.color.grey));
        }
        mButtonCall = findViewById(R.id.button_caltxt);
        mButtonCall2 = findViewById(R.id.button_caltxt2);
        if (mob.getNumber2().length() == 0) {
            mButtonCall2.setVisibility(View.GONE);
            findViewById(R.id.divider2).setVisibility(View.GONE);
        }
        if (mob.getNumber().length() == 0) {
            mButtonCall.setVisibility(View.GONE);
            findViewById(R.id.divider1).setVisibility(View.GONE);
        }

        Drawable d1 = this.getResources().getDrawable(R.drawable.ic_delete_black_24dp);
        Drawable wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mButtonDelete.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);
        //		mButtonCall.setCompoundDrawablesWithIntrinsicBounds(Addressbook.get().getContactStatusIconResource(mob), 0, 0, 0);

//		if(Addressbook.get().isCaltxtContact(mob.getUsername())) {
        d1 = this.getResources().getDrawable(R.drawable.ic_call_black_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        mButtonCall.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);
        mButtonCall2.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);
//		}
        mButtonCall.setText(getResources().getString(R.string.action_call) + " " + mob.getNumber());
        mButtonCall.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "onClick " + mob);
                openCaltxtInputActivity(mob.getNumber());
            }

        });

        mButtonCall2.setText(getResources().getString(R.string.action_call) + " +" + mob.getNumber2());
        mButtonCall2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "onClick " + mob);
                openCaltxtInputActivity(mob.getNumber2());
            }

        });

        mCardTitle = findViewById(R.id.card_title);
//		mMobileNumberView = (TextView) findViewById(R.id.profile_phone_number);
//		mMobileNumberView.setVisibility(View.GONE);//HIDE IT

        mPhoneStatusView = findViewById(R.id.profile_status);
        mPhoneStatusIcon = findViewById(R.id.status_icon);
        mPhoneStatusIcon.setBackgroundResource(Addressbook.getInstance(getApplicationContext()).geStatusBackgroundResource(mob));
        mPhoneStatusIcon.setImageResource(Addressbook.getInstance(getApplicationContext()).getContactStatusIconResource(mob));
//		mPhoneStatusIcon.setImageDrawable(Addressbook.get().getContactStatusIconResource(mob));
        if (mob.isRegistered() == false) {
            mPhoneStatusIcon.setVisibility(View.GONE);
        }
        mLastSeen = findViewById(R.id.profile_lastseen);
        mPicView = findViewById(R.id.profile_image);
        ViewTreeObserver vto = mPicView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                int x;
                mPicView.getViewTreeObserver().removeOnPreDrawListener(this);
                x = mPicView.getMeasuredWidth();
                mPicView.setLayoutParams(new RelativeLayout.LayoutParams(x, x));
//                mPicView.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(x,x));
                finalHeight = mPicView.getMeasuredHeight();
                finalWidth = mPicView.getMeasuredWidth();
                ImageLoader.getInstance(getBaseContext()).DisplayImageReload(mob.getIcon(), mPicView,
                        finalWidth, R.drawable.ic_person_white_24dp, true);
                return true;
            }
        });
        /*
        mExpandedPicView = (ImageView) findViewById(R.id.expanded_image);
		mExpandedPicView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Hide the thumbnail and show the zoomed-in view. When the animation
			    // begins, it will position the zoomed-in view in the place of the
			    // thumbnail.
				mExpandedPicView.setVisibility(View.GONE);

				mPicView.setVisibility(View.VISIBLE);
				mNameView.setVisibility(View.VISIBLE);
//				mMobileNumberView.setVisibility(View.VISIBLE);
				showStatus();
				showLastSeen();
				mButtonCall.setVisibility(View.VISIBLE);
				mButtonBlock.setVisibility(View.VISIBLE);
//				mIconRight.setVisibility(View.VISIBLE);
			}
		});
		mPicView.setOnClickListener(new OnClickListener() {//OPEN THIS COMMENT TO EXPAND PROFILE PIC ON CLICK

			@Override
			public void onClick(View arg0) {
				// Hide the thumbnail and show the zoomed-in view. When the animation
			    // begins, it will position the zoomed-in view in the place of the
			    // thumbnail.
				mPicView.setVisibility(View.GONE);
				mNameView.setVisibility(View.GONE);
				mMobileNumberView.setVisibility(View.GONE);
				mPhoneStatusView.setVisibility(View.GONE);
				mLastSeen.setVisibility(View.GONE);
				mIconRight.setVisibility(View.GONE);
				mButtonCall.setVisibility(View.GONE);
				mButtonBlock.setVisibility(View.GONE);

				((Globals) getApplication()).getImageLoader().DisplayImage(mob.getIcon(), mExpandedPicView, 
						finalWidth, Constants.icon_social_person);
				mExpandedPicView.setVisibility(View.VISIBLE);
			}
		});*/

//		mIconRight = (ImageView) findViewById(R.id.profile_right_icon);
//		mIconRight.setVisibility(View.GONE);//HIDE IT, since we are using buttons now

//		mNameView.setText(mob.getName().equals(mob.getUsername())?"Anonymous":mob.getName());
//        mCardTitle.setText(mob.getHeader());
//		mMobileNumberView.setText(mob.getNumber());
//		mMobileNumberView.setText(mob.getSubject());

        showStatus();
        showLastSeen();

/*		mIconRight.setImageResource(Addressbook.get().getContactStatusIconResource(mob));
		mIconRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent caltxtInput = new Intent(Profile.this, CaltxtInputActivity.class);
				caltxtInput.putExtra("IDTOBJECT", mob);
				caltxtInput.putExtra("VIEW", Constants.INPUT_VIEW);
				startActivity(caltxtInput);
			}

		});
*/
        Toolbar toolbar = findViewById(R.id.toolbar);
//		toolbar.getBackground().setAlpha(0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(mob.getName());

//	    final String EXTRA_IMAGE = "Profile:profile_image";
//	    ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), EXTRA_IMAGE);
//	    CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//        collapsingToolbarLayout.setTitle(mob.getName());
//        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.holo_blue_bright));

        //	    setSupportActionBar((Toolbar) findViewById(R.id.actionbartoolbar));
        mPicView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent activity = new Intent(Profile.this, PhotoFullscreen.class);
                activity.putExtra("URL", mob.getIcon());
                startActivity(activity);
            }
        });

    }

    void showStatus() {
        if (mob.getBody().trim().length() > 0) {
//			mPhoneStatusView.setText(mob.getSubject());
            mPhoneStatusView.setText(mob.getBody());
            mPhoneStatusView.setVisibility(View.VISIBLE);
            mPhoneStatusIcon.setVisibility(View.VISIBLE);
        } else {
            mPhoneStatusView.setVisibility(View.GONE);
            mPhoneStatusIcon.setVisibility(View.GONE);
//			mPhoneStatusView.setText(getResources().getString(R.string.profile_value_status_default));
        }
    }

    void showLastSeen() {
//		if(mob.getStatus()==XMob.STATUS_OFFLINE && mob.getModified()>0) {
        if (mob.isOffline() && mob.getModified() > 0) {
            DateFormat df = DateFormat.getDateTimeInstance();
            mLastSeen.setText(getResources().getString(R.string.last_seen) +
                    DateUtils.getRelativeDateTimeString(this, mob.getModified(),
                            DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString());
//			mLastSeen.setText("Last seen "+df.format(new Date(mob.getModified())));
            mLastSeen.setVisibility(View.VISIBLE);
        } else {
            mLastSeen.setVisibility(View.GONE);
//			mLastSeen.setText("Last seen "+getResources().getString(R.string.profile_value_lastseen_default));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
		/* NOTICE : THIS METHOD SHOULD RETURN QUICK AS POSSIBLE 
		 * OTHERWISE THE UPCOMING ACTIVITY WILL SHOW LAG IN LOADING */

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Connection.get().removeChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.caltxt_profile_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
//	    ContactID = Addressbook.get().getContactIDFromPhoneAddressbook(mob.getNumber());
        if (Addressbook.getInstance(getApplicationContext()).isContact(mob.getUsername())) {
//	    if(ContactID>0) {
            menu.findItem(R.id.action_addressbook_add).setVisible(false);
//            menu.findItem(R.id.action_addressbook_update).setVisible(true);commented 07112019, do not allow contact addressbook edit actions
            menu.findItem(R.id.action_addressbook_update).setVisible(false);
        } else {
//            menu.findItem(R.id.action_addressbook_update).setVisible(true);commented 07112019, do not allow contact addressbook edit actions
            menu.findItem(R.id.action_addressbook_add).setVisible(false);
//            menu.findItem(R.id.action_addressbook_add).setVisible(true);
            menu.findItem(R.id.action_addressbook_update).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_addressbook_add:
                addAsContactConfirmed(mob.getName(), mob.getNumber());
                return true;
            case R.id.action_addressbook_update:
                updateContactConfirmed(mob.getName(), mob.getNumber());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when the user touches the caltxt call button
     */
    public void openCaltxtInputActivity(String number) {
        // Do something in response to button click
        Intent caltxtInput = new Intent(Profile.this, CaltxtInputActivity.class);
        XMob nmob = Addressbook.getInstance(this).getRegistered(number);
//		if(mob.getNumber2().endsWith(number)) {
        if (nmob == null) {
            nmob = new XMob();
//				nmob.init(mob.toString());
//				nmob.setUsername(mob.getUsername());
            nmob.setUsername(XMob.toFQMN(number, Addressbook.getMyCountryCode()));
            // swap only the number1 and number2. let username be the same for both, to fetch the profile picture
//				nmob.setName(mob.getName());
//				nmob.setNumber(mob.getNumber2());
//				nmob.setNumber2(mob.getNumber());
        }
        Log.d(TAG, "sendCaltxtCall nmob " + nmob);
        caltxtInput.putExtra("IDTOBJECT", nmob);
//		} else {
//			caltxtInput.putExtra("IDTOBJECT", mob);
//            Log.d(TAG, "sendCaltxtCall mob "+mob);
//		}
//		caltxtInput.putExtra("VIEW", Constants.INPUT_VIEW);
        startActivity(caltxtInput);
    }
/*
	public void inviteContact(View view) {
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);         
		sendIntent.setData(Uri.parse("sms:+"+mob.getNumber()));
		sendIntent.putExtra("sms_body", 
				getString(R.string.service_sms_invite));
		startActivity(sendIntent);
	}
*/

    /**
     * Called when the user touches the block contact button
     */
    public void blockContact(View view) {
        // if not in block book, add, else remove
        XMob bmob = (XMob) Blockbook.getInstance(getApplicationContext()).get(mob.getUsername());
        if (bmob == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CaltxtAlertDialogTheme);
            builder.setPositiveButton(R.string.action_block,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Blockbook.getInstance(getApplicationContext()).add(mob.getUsername(), mob.getName());

                            Drawable d1 = getResources().getDrawable(R.drawable.ic_remove_circle_outline_black_24dp);
                            Drawable wrappedDrawable = DrawableCompat.wrap(d1);
                            wrappedDrawable = wrappedDrawable.mutate();
                            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
                            d1.invalidateSelf();
                            mButtonBlock.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);
                            mButtonBlock.setText(R.string.action_unblock_contact);
                            mButtonBlock.setTextColor(Color.RED);
                            Connection.get().addAction(Constants.contactBlockedProperty, mob.getUsername(), null);
                        }
                    });
            builder.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            String[] additionalArgs = {mob.getName()};
            String actionTaken = getString(R.string.prompt_caltxt_block_contact, (Object[]) additionalArgs);
            builder.setMessage(R.string.prompt_caltxt_block_contact_msg).setTitle(actionTaken);
//			builder.setIcon(R.drawable.ic_warning_white_24dp);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Blockbook.getInstance(getApplicationContext()).remove(mob.getUsername());

            Drawable d1 = getResources().getDrawable(R.drawable.ic_block_black_24dp);
            Drawable wrappedDrawable = DrawableCompat.wrap(d1);
            wrappedDrawable = wrappedDrawable.mutate();
            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
            d1.invalidateSelf();
            mButtonBlock.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);
            mButtonBlock.setText(R.string.action_block_contact);
            mButtonBlock.setTextColor(getResources().getColor(R.color.grey));
            Connection.get().addAction(Constants.contactUnblockedProperty, mob.getUsername(), null);
        }
    }

    /**
     * Called when the user touches the delete contact button
     */
    public void deleteContact(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CaltxtAlertDialogTheme);
        builder.setPositiveButton(R.string.action_delete,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Addressbook.getInstance(getApplicationContext()).remove(mob.getUsername());
                        Connection.get().addAction(Constants.contactDeleteProperty, mob.getUsername(), null);
                        finish();
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        String[] additionalArgs = {mob.getName()};
        String actionTaken = getString(R.string.prompt_caltxt_delete_contact, (Object[]) additionalArgs);
        builder/*.setMessage(R.string.prompt_caltxt_delete_contact_msg)*/.setTitle(actionTaken);
//			builder.setIcon(R.drawable.ic_warning_white_24dp);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void addAsContactConfirmed(final String name, final String mobile) {

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, mobile);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        startActivityForResult(intent, ADD_CONTACT);
    }

    void updateContactConfirmed(final String name, final String number) {

        Intent intent = new Intent(Intent.ACTION_EDIT);

        ContactID = Addressbook.getInstance(getApplicationContext()).getContactIDFromPhoneAddressbook(number);
        numbers = Addressbook.getInstance(getApplicationContext()).getContactNumberFromPhoneAddressbook(ContactID);

        Log.i(TAG, "updateContactConfirmed ContactID," + ContactID + ", numbers, " + numbers);
        intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID));
        intent.putExtra("finishActivityOnSaveCompleted", true);
        startActivityForResult(intent, UPDATE_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (ADD_CONTACT):
//			if (resultCode == RESULT_OK) {
                //name now in phone Address book, pick it!
                String name = Addressbook.getInstance(getApplicationContext()).getContactNameFromPhoneAddressbook(mob.getUsername());
                if (name == null)
                    break;
                mob.setName(name);
                Addressbook.getInstance(getApplicationContext()).prepend(mob);
                Log.i(TAG, "onActivityResult name," + mob.toString());

                ArrayList<XMob> mobs = new ArrayList<XMob>();
                mobs.add(mob);
//				Addressbook.getInstance(getApplicationContext()).syncAddressbookAndDB(mobs);
//				Logbook.get().updateNames(mobs);//NOT REQUIRED, XCtx picks name from Addressbook
                Connection.get().addAction(Constants.contactNameAddProperty, mob, mobs);
                finish();
//			} else if(resultCode == RESULT_CANCELED) {
//			}
                break;
            case (UPDATE_CONTACT):
                if (resultCode == RESULT_OK) {
                    //name now in phone Address book, pick it!
                    name = Addressbook.getInstance(getApplicationContext()).getContactNameFromPhoneAddressbook(ContactID);
                    Log.i(TAG, "updateContactConfirmed name," + name);
                    if (name == null) {
//					break;
                    }

                    boolean existing = false;
                    ArrayList<XMob> newvalues = new ArrayList<XMob>();
                    ArrayList<String> values = Addressbook.getInstance(getApplicationContext()).getContactNumberFromPhoneAddressbook(ContactID);
                    for (int i = 0; i < values.size(); i++) {
                        String newnumber = values.get(i);
//					String newnumber = XMob.toFQMN(values.get(i), Addressbook.getInstance(getApplicationContext()).getMyCountryCode());
                        XMob mb = new XMob(name, newnumber, Addressbook.getInstance(getApplicationContext()).getMyCountryCode());
                        existing = false;
                        for (int j = 0; j < numbers.size(); j++) {
                            String oldnumber = numbers.get(j);
//						String oldnumber = XMob.toFQMN(numbers.get(j), Addressbook.getInstance(getApplicationContext()).getMyCountryCode());

                            if (oldnumber.equals(newnumber)) {
                                existing = true;
                                XMob m = Addressbook.getInstance(getApplicationContext()).getContact(XMob.toFQMN(oldnumber, Addressbook.getInstance(getApplicationContext()).getMyCountryCode()));
                                if (m != null) {
                                    m.setName(name);
                                    Addressbook.getInstance(getApplicationContext()).update(m);
                                }
                                numbers.remove(j);
                                break;
                            }
                        }
                        if (existing == false) {
                            Addressbook.getInstance(getApplicationContext()).prepend(mb);
                            newvalues.add(mb);
                        }
                    }

                    //remove remaining numbers not found in new list
                    for (int j = 0; j < numbers.size(); j++) {
                        Addressbook.getInstance(getApplicationContext()).remove(XMob.toFQMN(numbers.get(j), Addressbook.getInstance(getApplicationContext()).getMyCountryCode()));
                    }
//				mob.setNumber(name, Globals.getMyCountryCode());
//				mob.setUsername(XMob.toFQMN(mob.getNumber(), CaltxtApp.getMyCountryCode()));

//				mobs.add(mob);
//				Addressbook.get().syncAddressbookAndDB(mobs);
//				Addressbook.get().remove(mob);
                    Connection.get().addAction(Constants.contactNameUpdatedProperty, null, newvalues);

                    if (newvalues.size() > 0) {
//					Logbook.get().updateNames(newvalues);//NOT REQUIRED, XCtx picks name from Addressbook
                        Connection.get().addAction(Constants.contactNameAddProperty, mob, newvalues);
                    }
                    finish();
                } else if (resultCode == RESULT_CANCELED) {
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        final String propertyName = event.getPropertyName();

        if (propertyName.equals(Constants.messageArrivedProperty)) {
//			mIconRight.setImageResource(Addressbook.get().getContactStatusIconResource(mob));
            IDTObject p = (IDTObject) event.getNewValue();
            if (p.getCName().equals("XMob")) {
                if (((XMob) p).getUsername().equals(mob.getUsername())) {
                    mPhoneStatusIcon.setBackgroundResource(Addressbook.getInstance(getApplicationContext()).geStatusBackgroundResource((XMob) p));
                    mPhoneStatusIcon.setImageResource(Addressbook.getInstance(getApplicationContext()).getContactStatusIconResource((XMob) p));
//					mPhoneStatusIcon.setImageDrawable(Addressbook.get().getContactStatusIconResource((XMob)p));
                    mPhoneStatusView.setText(((XMob) p).getHeadline());
                    Log.i(TAG, "propertyChange mob " + mob);
                }
            }
        }
    }
}
