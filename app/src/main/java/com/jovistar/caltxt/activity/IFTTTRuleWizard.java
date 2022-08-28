package com.jovistar.caltxt.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.bo.XRul;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.XMob;
import com.jovistar.commons.util.Logr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class IFTTTRuleWizard extends AppCompatActivity {
    private static final String TAG = "IFTTTRuleWizard";

    int currentStep = 0;
    ListView listview = null;
    CaltxtStatusListAdapter adapter = null;
    TextView ruleDescription;
    Toolbar tb;
    EditText actionValue;
    static AppCompatButton pickTime;
    static AppCompatButton pickDate, pickOccurance;
    CheckBox checkbox_alwaysask;
    ArrayList<CaltxtStatus> choices = new ArrayList<CaltxtStatus>();

    static XRul theRule;
    String wizardStart = "";
//	Date actionWhen = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        theRule = (XRul) getIntent().getExtras().getSerializable("XRul");
        wizardStart = getIntent().getExtras().getString("ACTION");

//		actionWhen.setTime(theRule.getActionWhen());

        setContentView(R.layout.ifttt_rule_wizard);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        tb = findViewById(R.id.toolbar);
        tb.setTitle(R.string.create_rule);

        findViewById(R.id.divider1).setVisibility(View.GONE);
        findViewById(R.id.divider2).setVisibility(View.GONE);
        findViewById(R.id.divider3).setVisibility(View.GONE);
        checkbox_alwaysask = findViewById(R.id.checkbox_alwaysask);
        if (theRule.isAlwaysAsk())
            checkbox_alwaysask.setChecked(true);
        else
            checkbox_alwaysask.setChecked(false);

        checkbox_alwaysask.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                theRule.setAlwaysAsk(checked);

                if (checked) {
//    				builder.setMessage(R.string.prompt_alwaysask_description);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(IFTTTRuleWizard.this, R.style.CaltxtAlertDialogTheme);
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    Log.d(TAG, "onCheckedChanged " + checked);
                    //    			builder.setIcon(R.drawable.ic_warning_white_24dp);
                    builder.setMessage(R.string.prompt_neverask_description);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                Persistence.getInstance(IFTTTRuleWizard.this).update(theRule);
            }

        });
        pickOccurance = findViewById(R.id.rule_action_recurrance);
        pickOccurance.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Drawable d1 = getResources().getDrawable(R.drawable.ic_repeat_one_white_24dp);

                if (pickOccurance.getText().toString().equals(XRul.RULES_REPEAT_ONCE)) {
                    pickOccurance.setText(XRul.RULES_REPEAT_HOURLY);
                    theRule.setActionRepeat(XRul.RULES_REPEAT_HOURLY);
                    d1 = getResources().getDrawable(R.drawable.ic_repeat_white_24dp);
                } else if (pickOccurance.getText().toString().equals(XRul.RULES_REPEAT_HOURLY)) {
                    /*pickOccurance.setText(XRul.RULES_REPEAT_MINUTES);
                    theRule.setActionRepeat(XRul.RULES_REPEAT_MINUTES);
                    d1 = getResources().getDrawable(R.drawable.ic_repeat_white_24dp);
                } else if (pickOccurance.getText().toString().equals(XRul.RULES_REPEAT_MINUTES)) {*/
                    pickOccurance.setText(XRul.RULES_REPEAT_DAILY);
                    theRule.setActionRepeat(XRul.RULES_REPEAT_DAILY);
                    d1 = getResources().getDrawable(R.drawable.ic_repeat_white_24dp);
                } else if (pickOccurance.getText().toString().equals(XRul.RULES_REPEAT_DAILY)) {
                    pickOccurance.setText(XRul.RULES_REPEAT_ONCE);
                    theRule.setActionRepeat(XRul.RULES_REPEAT_ONCE);
                    d1 = getResources().getDrawable(R.drawable.ic_repeat_one_white_24dp);
                }

                Drawable wrappedDrawable = DrawableCompat.wrap(d1);
                wrappedDrawable = wrappedDrawable.mutate();
                DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
                d1.invalidateSelf();
                pickOccurance.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);

                Persistence.getInstance(IFTTTRuleWizard.this).update(theRule);
                Log.d(TAG, "pickOccurance onclick " + theRule.getActionRepeat());
            }

        });
        Drawable d1 = getResources().getDrawable(R.drawable.ic_repeat_one_white_24dp);
        pickOccurance.setText(XRul.RULES_REPEAT_ONCE);
        if (theRule.getActionRepeat().equals(XRul.RULES_REPEAT_HOURLY)) {
            d1 = getResources().getDrawable(R.drawable.ic_repeat_white_24dp);
            pickOccurance.setText(XRul.RULES_REPEAT_HOURLY);
        } else if (theRule.getActionRepeat().equals(XRul.RULES_REPEAT_DAILY)) {
            d1 = getResources().getDrawable(R.drawable.ic_repeat_white_24dp);
            pickOccurance.setText(XRul.RULES_REPEAT_DAILY);
        }
        Drawable wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        pickOccurance.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);

        pickTime = findViewById(R.id.rule_action_time);
        pickTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
            }

        });
        d1 = this.getResources().getDrawable(R.drawable.ic_access_time_white_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        pickTime.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        if (theRule.getActionWhen() > 0)
            pickTime.setText(sdf.format(new Date(theRule.getActionWhen())));
        else
            pickTime.setText(R.string.pick_time);

        pickDate = findViewById(R.id.rule_action_date);
        pickDate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }

        });
        d1 = this.getResources().getDrawable(R.drawable.ic_date_range_white_24dp);
        wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.grey));
        d1.invalidateSelf();
        pickDate.setCompoundDrawablesWithIntrinsicBounds(d1, null, null, null);
        sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
        if (theRule.getActionWhen() > 0)
            pickDate.setText(sdf.format(new Date(theRule.getActionWhen())));
        else
            pickDate.setText(R.string.pick_date);
        Log.d(TAG, "onCreate getActionWhen " + theRule.getActionWhen());

        listview = findViewById(R.id.rulewizard_list);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        actionValue = findViewById(R.id.rulewizard_actionvalue);
        ruleDescription = findViewById(R.id.rule_description);

        adapter = new CaltxtStatusListAdapter(this, fillEventTypeChoices());
        listview.setAdapter(adapter);

        if (wizardStart.equals("IF")) {
        } else {
        }

        moveToNextStep();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (theRule.isComplete() == false && theRule.isEnabled()) {
            theRule.setEnabled(false);
        }
        Persistence.getInstance(this).update(theRule);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.caltxt_rules_wizard, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentStep >= 4) {
            MenuItem mi = menu.findItem(R.id.previous_step);
            mi.setVisible(true);
            mi.setIcon(R.drawable.ic_check_white_24dp);
            mi.setTitle("Finish");
            mi.setTitleCondensed("Finish");
        } else if (currentStep == 2) {
            MenuItem mi = menu.findItem(R.id.previous_step);
            if (theRule.getEvent().equals(XRul.RULES_EVENT_TYPE_TIME)) {
                menu.findItem(R.id.previous_step).setVisible(true);
                mi.setIcon(R.drawable.ic_arrow_forward_white_24dp);
                mi.setTitle("Next");
                mi.setTitleCondensed("Next");
            } else {
                menu.findItem(R.id.previous_step).setVisible(false);
            }
        } else if (currentStep <= 1) {
            menu.findItem(R.id.previous_step).setVisible(false);
        } else {
            MenuItem mi = menu.findItem(R.id.previous_step);
            mi.setVisible(true);
            mi.setIcon(R.drawable.ic_arrow_back_white_24dp);
            mi.setTitle("Previous");
            mi.setTitleCondensed("Previous");
        }

        Log.d(TAG, "onPrepareOptionsMenu " + currentStep);
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.previous_step:

                if (currentStep >= 4) {
                    if (theRule.getAction().equals(XRul.RULES_ACTION_TYPE_TEXT)) {
                        if (actionValue.getText().toString().trim().length() == 0) {
                            Notify.toast(actionValue, getBaseContext(), "Action message is empty", Toast.LENGTH_LONG);
                            return true;
                        }
                        theRule.setActionValue(actionValue.getText().toString());
                    } else {
                        actionValue.setText("");
                        theRule.setActionValue("");
                    }
                    Log.d(TAG, "onOptionsItemSelected actionValue " + actionValue.getText().toString());
                    Persistence.getInstance(this).update(theRule);
                    finish();
                } else if (currentStep == 2) {
                    if (theRule.getEvent().equals(XRul.RULES_EVENT_TYPE_TIME)) {
                        if (theRule.getActionWhen() == 0) {
                            Notify.toast(pickTime, getBaseContext(), "Please select date and time", Toast.LENGTH_LONG);
//					} else if(theRule.getActionWhen()<=(Calendar.getInstance().getTimeInMillis()+(15 * 60 * 1000))) {
//						Notify.toast(pickTime, getBaseContext(), "Time must be atleast 15 minutes later from now", Toast.LENGTH_LONG);
                        } else {
                            moveToNextStep();
                        }
                    }
                } else if (currentStep == 0) {
                    finish();
                } else {
                    currentStep--;
                    currentStep--;
                    moveToNextStep();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void refreshView() {

        ruleDescription.setText(Html.fromHtml(theRule.toString(getApplicationContext())));

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        if (theRule.getActionWhen() > 0) {
            pickTime.setText(sdf.format(theRule.getActionWhen()));
        }

        sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
        if (theRule.getActionWhen() > 0)
            pickDate.setText(sdf.format(theRule.getActionWhen()));

        if (theRule.getActionValue().length() == 0 && theRule.getEvent().equals(XRul.RULES_EVENT_TYPE_STATUS)) {
            if (theRule.getActionWhen() == 0 && theRule.getEventValue().length() > 0) {
                actionValue.setText(getString(R.string.rule_arrived)
                        + theRule.getEventValue());
            } else if (theRule.getActionWhen() == 1 && theRule.getEventValue().length() > 0) {
                actionValue.setText(getString(R.string.rule_left)
                        + theRule.getEventValue().substring(theRule.getEventValue().lastIndexOf(" ") + 1));
            }
        } else {
            actionValue.setText(theRule.getActionValue());
        }

        //update database, and memory
        Persistence.getInstance(this).update(theRule);
//		IFTTT.getRules().put(theRule.getPersistenceId(), theRule);

        Logr.d(TAG, "refreshView " + theRule.toString(getApplicationContext()));
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            if (theRule.getActionWhen() > Calendar.getInstance().getTimeInMillis())
                c.setTimeInMillis(theRule.getActionWhen());
            else
                c.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            Log.d(TAG, "onCreateDialog " + hour + ":" + minute);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            Date actionWhen = null;
            if (theRule.getActionWhen() > 0)
                actionWhen = new Date(theRule.getActionWhen());
            else
                actionWhen = new Date();
            actionWhen.setHours(hourOfDay);
            actionWhen.setMinutes(minute);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
            pickTime.setText(sdf.format(actionWhen));

            theRule.setActionWhen(actionWhen.getTime());
            Log.d(TAG, "onTimeSet actionWhen " + actionWhen.getTime() + ", " + Calendar.getInstance().getTimeInMillis());
        }
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            if (theRule.getActionWhen() > Calendar.getInstance().getTimeInMillis())
                c.setTimeInMillis(theRule.getActionWhen());
            else
                c.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            Log.d(TAG, "onCreateDialog " + year + ", " + month + ", " + day);
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Date actionWhen = null;
            if (theRule.getActionWhen() > 0)
                actionWhen = new Date(theRule.getActionWhen());
            else
                actionWhen = new Date();
            actionWhen.setDate(day);
            actionWhen.setMonth(month);
            actionWhen.setYear(year - 1900);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
//			SimpleDateFormat sdf = SimpleDateFormat("EEE, MMM d, yyyy");
            pickDate.setText(sdf.format(actionWhen));

            theRule.setActionWhen(actionWhen.getTime());
            Log.d(TAG, "onDateSet actionWhen " + actionWhen.getTime() + ", " + Calendar.getInstance().getTimeInMillis());
        }
    }

    ArrayList<CaltxtStatus> fillEventTypeChoices() {
        choices.clear();

        choices.add(new CaltxtStatus("Place", "Trigger action when place changes", R.drawable.ic_place_white_24dp));
        choices.add(new CaltxtStatus("Time", "Trigger action at given time", R.drawable.ic_timer_white_24dp));

        return choices;
    }

    ArrayList<CaltxtStatus> fillPlaceTypeChoices() {
        choices.clear();

        choices.add(new CaltxtStatus("Arrive at a place", "Move to a given place", R.drawable.ic_place_white_24dp));
        choices.add(new CaltxtStatus("Leave a place", "Move from a given place", R.drawable.ic_place_white_24dp));

        return choices;
    }

    ArrayList<CaltxtStatus> fillActionChoices() {
        choices.clear();

        choices.add(new CaltxtStatus("Send text", "Send a text message", R.drawable.ic_message_out_white_24dp));
        choices.add(new CaltxtStatus("Call", "Place a call", R.drawable.ic_call_white_24dp));
        choices.add(new CaltxtStatus("Mute ringer", "Mute this phone", R.drawable.ic_vibration_white_24dp));
        choices.add(new CaltxtStatus("Unmute ringer", "Unmute this phone", R.drawable.ic_volume_up_white_24dp));

        return choices;
    }

    ArrayList<CaltxtStatus> fillContactsChoices() {
        choices.clear();

        ArrayList<XMob> ab = Addressbook.getInstance(getApplicationContext()).getContacts();
        for (int i = 0; i < ab.size(); i++) {
            choices.add(new CaltxtStatus(ab.get(i).getName(), ab.get(i).getUsername(), R.drawable.ic_person_white_24dp));
        }

        return choices;
    }

    void chosePlace() {
        adapter.clear();
        List<CaltxtStatus> al = Addressbook.getInstance(getApplicationContext()).getPlaceList();
        for (int i = 0; i < al.size(); i++) {
            if (al.get(i).getStatusName().equals("Forget")) {
                continue;
            }
            if (theRule.getActionWhen() == 1) {
                al.get(i).setStatusName("leave " + al.get(i).getStatusCode().substring(al.get(i).getStatusCode().lastIndexOf(" ") + 1));
            } else {
                al.get(i).setStatusName("arrive at " + al.get(i).getStatusCode().substring(al.get(i).getStatusCode().lastIndexOf(" ") + 1));
            }

            adapter.add(al.get(i));
        }

        adapter.removeSelection();
        adapter.notifyDataSetChanged();

        pickTime.setVisibility(View.GONE);
        pickDate.setVisibility(View.GONE);
        pickOccurance.setVisibility(View.GONE);
        actionValue.setVisibility(View.GONE);
//		ruleDescription.setVisibility(View.GONE);
        checkbox_alwaysask.setVisibility(View.GONE);
        findViewById(R.id.divider1).setVisibility(View.GONE);
        findViewById(R.id.divider2).setVisibility(View.GONE);
        findViewById(R.id.divider3).setVisibility(View.GONE);

        listview.setVisibility(View.VISIBLE);
        if (theRule.getEventValue().length() > 0) {
            int sel = adapter.getItemPosition(theRule.getEventValue());
            listview.setSelection(sel);
            adapter.toggleSelection(sel);
        }
        tb.setTitle(R.string.select_place);
        refreshView();
    }

    void choseActionTime() {

        actionValue.setVisibility(View.GONE);
        listview.setVisibility(View.GONE);
//		ruleDescription.setVisibility(View.GONE);
        checkbox_alwaysask.setVisibility(View.GONE);

        pickTime.setVisibility(View.VISIBLE);
        pickDate.setVisibility(View.VISIBLE);
        pickOccurance.setVisibility(View.VISIBLE);
        findViewById(R.id.divider1).setVisibility(View.VISIBLE);
        findViewById(R.id.divider2).setVisibility(View.VISIBLE);
        findViewById(R.id.divider3).setVisibility(View.VISIBLE);

        tb.setTitle("Set action time");
        refreshView();
    }

    void choseActionType() {
        fillActionChoices();
        adapter.removeSelection();
        adapter.notifyDataSetChanged();

        findViewById(R.id.divider1).setVisibility(View.GONE);
        findViewById(R.id.divider2).setVisibility(View.GONE);
        findViewById(R.id.divider3).setVisibility(View.GONE);
        pickTime.setVisibility(View.GONE);
        pickDate.setVisibility(View.GONE);
        pickOccurance.setVisibility(View.GONE);
        actionValue.setVisibility(View.GONE);

        checkbox_alwaysask.setVisibility(View.VISIBLE);

        listview.setVisibility(View.VISIBLE);
        if (theRule.getAction().length() > 0) {
            int sel = -1;
            if (theRule.getAction().equals(XRul.RULES_ACTION_TYPE_TEXT)) {
                sel = adapter.getItemPosition("Send a text message");
            } else if (theRule.getAction().equals(XRul.RULES_ACTION_TYPE_CALL)) {
                sel = adapter.getItemPosition("Place a call");
            } else if (theRule.getAction().equals(XRul.RULES_ACTION_TYPE_MUTE_PHONE)) {
                sel = adapter.getItemPosition("Mute this phone");
            } else if (theRule.getAction().equals(XRul.RULES_ACTION_TYPE_UNMUTE_PHONE)) {
                sel = adapter.getItemPosition("Unmute this phone");
            }

            if (sel >= 0) {
                listview.setSelection(sel);
                adapter.toggleSelection(sel);
            }
        }
//		ruleDescription.setVisibility(View.GONE);

        tb.setTitle("Select action");
        refreshView();
    }

    void choseActionTo() {
        pickTime.setVisibility(View.GONE);
        pickDate.setVisibility(View.GONE);
        pickOccurance.setVisibility(View.GONE);
        actionValue.setVisibility(View.GONE);
//		ruleDescription.setVisibility(View.GONE);
        checkbox_alwaysask.setVisibility(View.GONE);
        findViewById(R.id.divider1).setVisibility(View.GONE);
        findViewById(R.id.divider2).setVisibility(View.GONE);
        findViewById(R.id.divider3).setVisibility(View.GONE);

        fillContactsChoices();
        adapter.removeSelection();
        adapter.notifyDataSetChanged();

        listview.setVisibility(View.VISIBLE);
        if (theRule.getActionFor().length() > 0) {
            int sel = adapter.getItemPosition(theRule.getActionFor());
            listview.setSelection(sel);
            adapter.toggleSelection(sel);
        }

        tb.setTitle("Select action receiver");
        refreshView();
    }

    void choseEventType() {

        fillEventTypeChoices();
        adapter.removeSelection();
        adapter.notifyDataSetChanged();

        pickTime.setVisibility(View.GONE);
        pickDate.setVisibility(View.GONE);
        pickOccurance.setVisibility(View.GONE);
        actionValue.setVisibility(View.GONE);
//		ruleDescription.setVisibility(View.GONE);
        checkbox_alwaysask.setVisibility(View.GONE);
        findViewById(R.id.divider1).setVisibility(View.GONE);
        findViewById(R.id.divider2).setVisibility(View.GONE);
        findViewById(R.id.divider3).setVisibility(View.GONE);

        listview.setVisibility(View.VISIBLE);
        if (theRule.getEvent().length() > 0) {
            int sel = adapter.getItemPosition(theRule.getEvent());
            listview.setSelection(sel);
            adapter.toggleSelection(sel);
        }

        tb.setTitle("Select event type");
        refreshView();
    }

    void chosePlaceEventType() {

        fillPlaceTypeChoices();
        adapter.removeSelection();
        adapter.notifyDataSetChanged();

        pickTime.setVisibility(View.GONE);
        pickDate.setVisibility(View.GONE);
        pickOccurance.setVisibility(View.GONE);
        actionValue.setVisibility(View.GONE);
//		ruleDescription.setVisibility(View.GONE);
        checkbox_alwaysask.setVisibility(View.GONE);
        findViewById(R.id.divider1).setVisibility(View.GONE);
        findViewById(R.id.divider2).setVisibility(View.GONE);
        findViewById(R.id.divider3).setVisibility(View.GONE);

        listview.setVisibility(View.VISIBLE);
        if (theRule.getEvent().length() > 0) {
            int sel = adapter.getItemPosition(theRule.getEvent());
            listview.setSelection(sel);
            adapter.toggleSelection(sel);
        }

        tb.setTitle("Select place event type");
        refreshView();
    }

    void choseActionValue() {
        choices.clear();

        listview.setVisibility(View.GONE);
        pickTime.setVisibility(View.GONE);
        pickDate.setVisibility(View.GONE);
        pickOccurance.setVisibility(View.GONE);
//		ruleDescription.setVisibility(View.GONE);
        checkbox_alwaysask.setVisibility(View.GONE);
        findViewById(R.id.divider1).setVisibility(View.GONE);
        findViewById(R.id.divider2).setVisibility(View.GONE);
        findViewById(R.id.divider3).setVisibility(View.GONE);

        actionValue.setVisibility(View.VISIBLE);

        tb.setTitle("Type action message");
        refreshView();
    }

    public void setListValue(CaltxtStatus c) {
        refreshView();
        Log.d(TAG, "setListValue:" + c.getStatusName());

        if (c.getStatusName().equals("Place")) {
            theRule.setEvent(XRul.RULES_EVENT_TYPE_STATUS);
        } else if (c.getStatusName().equals("Time")) {
            theRule.setEvent(XRul.RULES_EVENT_TYPE_TIME);
        } else if (c.getStatusName().equals("Send text")) {
            theRule.setAction(XRul.RULES_ACTION_TYPE_TEXT);
        } else if (c.getStatusName().equals("Call")) {
            theRule.setAction(XRul.RULES_ACTION_TYPE_CALL);
        } else if (c.getStatusName().equals("Mute ringer")) {
            theRule.setAction(XRul.RULES_ACTION_TYPE_MUTE_PHONE);
        } else if (c.getStatusName().equals("Unmute ringer")) {
            theRule.setAction(XRul.RULES_ACTION_TYPE_UNMUTE_PHONE);
        } else if (c.getStatusName().startsWith("Leave a place")) {
            theRule.setActionWhen(1);
        } else if (c.getStatusName().startsWith("Arrive at a place")) {
            theRule.setActionWhen(0);
        } else if (c.getStatusCode().startsWith("at ")) {
            //chosen a place
            theRule.setEventValue(c.getStatusCode());
        } else {
            //chosen a contact
            theRule.setActionFor(c.getStatusCode());
            if (theRule.getAction().equals(XRul.RULES_ACTION_TYPE_TEXT)) {
                theRule.setActionValue(actionValue.getText().toString());
            } else {
                actionValue.setText("");
                theRule.setActionValue("");
            }
        }

        refreshView();
        moveToNextStep();
    }

    void moveToNextStep() {

        refreshView();
        Log.d(TAG, "moveToNextStep " + currentStep);

        if (currentStep == 0) {
            if (theRule.getEvent().length() == 0) {
                choseEventType();
            } else {
                if (wizardStart.equals("IF")) {
                    tb.setTitle(R.string.edit_rule_event);
                    moveToStep1();
                } else if (wizardStart.equals("THEN")) {
                    tb.setTitle("Select action");
                    moveToStep2();
                }
            }
        } else if (currentStep == 1) {
            moveToStep1();
        } else if (currentStep == 2) {
            moveToStep2();
        } else if (currentStep == 3) {
            moveToStep3();
        } else if (currentStep == 4) {
            moveToStep4();
        }

        currentStep++;
        supportInvalidateOptionsMenu();
    }

    void moveToStep1() {
        if (theRule.getEvent().equals(XRul.RULES_EVENT_TYPE_TIME)) {
            choseActionTime();
        } else if (theRule.getEvent().equals(XRul.RULES_EVENT_TYPE_STATUS)) {
            if (theRule.getActionWhen() < 0) {
                chosePlaceEventType();
                currentStep--;
                return;//skip incrementing currentStep
            } else {
                chosePlace();
            }
        }
        currentStep = 1;
    }

    void moveToStep2() {
        choseActionType();
        currentStep = 2;
    }

    void moveToStep3() {
        if (theRule.getAction().equals(XRul.RULES_ACTION_TYPE_MUTE_PHONE)
                || theRule.getAction().equals(XRul.RULES_ACTION_TYPE_UNMUTE_PHONE)) {
            finish();
            return;
        }
        choseActionTo();
        currentStep = 3;
    }

    void moveToStep4() {
        if (theRule.getAction().equals(XRul.RULES_ACTION_TYPE_TEXT)) {
            choseActionValue();
        } else if (theRule.getAction().equals(XRul.RULES_ACTION_TYPE_CALL)) {
            finish();
        }
        currentStep = 4;
    }

}
