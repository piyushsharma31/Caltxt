package com.jovistar.caltxt.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.bo.XRul;
import com.jovistar.caltxt.network.voice.CallManager;
import com.jovistar.caltxt.network.voice.CaltxtHandler;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.service.RuleAlarmReceiver;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class IFTTT extends AppCompatActivity {
    private static final String TAG = "IFTTT";

//	static HashMap<Long/*pid*/, Timer> timers = new HashMap<Long, Timer>();

    public ActionMode mActionMode;
    IFTTT thisActivity;
    protected static IFTTTListAdapter ifttt_adapter;
    protected ListView listview;
    TextView tv;
    public static final int RULE_EDIT = 12;
    //	public static boolean RELOAD_RULES = false;
    public int editingXRulIndex = 0;//index (in adapter) of rule which is now editing in Wizard

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ifttt_list);

//		if(rules==null || RELOAD_RULES) {
//			rules = Persistence.getInstance(CaltxtApp.getCustomAppContext()).getAllXRUL();
//		}

        listview = findViewById(R.id.iftttlist);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
//	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//	    getSupportActionBar().setDisplayShowHomeEnabled(true);

        ifttt_adapter = new IFTTTListAdapter(this, /*getRulesList()*/
                Persistence.getInstance(this).getAllXRUL());
        listview.setAdapter(ifttt_adapter);
        ifttt_adapter.notifyDataSetChanged();

        tv = findViewById(R.id.empty_view);
        if (ifttt_adapter.isEmpty() == false) {
            tv.setVisibility(View.GONE);
        }

//		listview.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1,
//					int position, long arg3) {
//			}
//    });
        thisActivity = this;
    }

    @Override
    protected void onResume() {
        ifttt_adapter.notifyDataSetChanged();
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
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.caltxt_ifttt_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_rule_add:
                XRul rul = new XRul();
                long pid = Persistence.getInstance(this).insertXRUL(rul);
                rul.setPersistenceId(pid);

                ifttt_adapter.insert(rul, 0);

                if (ifttt_adapter.isEmpty() == false) {
                    tv.setVisibility(View.GONE);
                }

                Intent it = new Intent(this, IFTTTRuleWizard.class);
                it.putExtra("XRul", rul);
                it.putExtra("ACTION", "IF");
                editingXRulIndex = 0;
                startActivityForResult(it, IFTTT.RULE_EDIT);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (RULE_EDIT):

                XRul rul = ifttt_adapter.getItem(editingXRulIndex);
                XRul newrul = Persistence.getInstance(this).getXRul(rul.getPersistenceId());
                Log.v(TAG, "onActivityResult rul pid " + rul.getPersistenceId() + ", index " + editingXRulIndex);

                ifttt_adapter.remove(editingXRulIndex);
                ifttt_adapter.insert(newrul, editingXRulIndex);

                if (true == rul.getEvent().equals(XRul.RULES_EVENT_TYPE_TIME))
                    resetTimedActions(getApplicationContext(), newrul);

                ifttt_adapter.notifyDataSetChanged();
                Log.v(TAG, "onActivityResult rul pid CHANGE COMPLETE " + newrul.getPersistenceId() + ", index " + editingXRulIndex);
                break;
            default:
                break;
        }
    }

    public synchronized static void placeChangeCallback(final Context context, final String status,
                                                        final long statusToFrom/*0:status changed to 'status';	1:status changed from 'status'*/) {
        Log.v(TAG, "Rule placeChangeCallback " + (statusToFrom == 0 ? "moved to " : "left ") + status);
        new Thread(new Runnable() {

            @Override
            public synchronized void run() {
                ArrayList<XRul> rules = Persistence.getInstance(context).getAllXRUL();
                Log.v(TAG, "Rule placeChangeCallback rules count " + rules.size());
                for (Iterator<XRul> it = rules.iterator(); it.hasNext(); ) {
                    final XRul rul = it.next();
                    if (rul.getEventValue().equals(status) && rul.isEnabled() && rul.getActionWhen() == statusToFrom) {
                        Log.v(TAG, "Rule placeChangeCallback rul " + rul.toString());
                        triggerAction(context, rul);
                    }
                }
            }

        }).start();
    }

    public static synchronized void triggerAction(Context context, XRul rule) {
        if (rule == null) {
            Log.w(TAG, TAG + "::triggerAction, no rule");
            return;
        }

        Log.v(TAG, TAG + "::triggerAction, rule " + rule.toString());

        if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_TEXT)) {
            //send text
            if (rule.isAlwaysAsk()) {

                Intent it = new Intent(context, ToastRuleAction.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                it.putExtra("IDTOBJECT", rule);
                context.startActivity(it);

            } else {
                //initiate text
                XMob callee = Addressbook.getInstance(context).getRegistered(rule.getActionFor());
                if (callee == null) {
                    callee = new XMob();
                    callee.setUsername(rule.getActionFor());
                }
                CaltxtHandler.get(context).publishTriggerAlert(callee.getUsername(),
                        rule.getActionValue(), "", "");
//				CaltxtHandler.get(context).initiateMessage((XMob) callee, rule.getActionValue(),
//						XCtx.PRIORITY_NORMAL);
                Notify.notify_caltxt_trigger_text_sent(context, callee.getName(), rule.getActionValue(), "", Calendar.getInstance().getTimeInMillis());
            }
        } else if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_UNMUTE_PHONE)) {
            if (rule.isAlwaysAsk()) {

                Intent it = new Intent(context, ToastRuleAction.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                it.putExtra("IDTOBJECT", rule);
                context.startActivity(it);

            } else {
                CallManager.normalModeRinger(context);
            }
        } else if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_MUTE_PHONE)) {
            if (rule.isAlwaysAsk()) {

                Intent it = new Intent(context, ToastRuleAction.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                it.putExtra("IDTOBJECT", rule);
                context.startActivity(it);

            } else {
                CallManager.vibrateModeRinger(context);
            }
        } else if (rule.getAction().equals(XRul.RULES_ACTION_TYPE_CALL)) {
            //call
//			if(rule.isAlwaysAsk()) {

            Intent it = new Intent(context, ToastRuleAction.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            it.putExtra("IDTOBJECT", rule);
            context.startActivity(it);
//			} else {
            //initiate call
//			}
        }

        //if repeating, schedule next run
        if (rule.getEvent().equals(XRul.RULES_EVENT_TYPE_TIME)) {
            if (rule.getActionRepeat().equals(XRul.RULES_REPEAT_DAILY)) {
                rule.setActionWhen(rule.getActionWhen() + 24 * 60 * 60 * 1000/*next day*/);
                Persistence.getInstance(context).update(rule);
            } else if (rule.getActionRepeat().equals(XRul.RULES_REPEAT_HOURLY)) {
                rule.setActionWhen(rule.getActionWhen() + 60 * 60 * 1000/*next hour*/);
                Persistence.getInstance(context).update(rule);
            }

            resetTimedActions(context, rule);
        }
    }

    //call to initialize all timers
    public static void listenForTimedActions(final Context context) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                ArrayList<XRul> rules = Persistence.getInstance(context).getAllXRUL();

                for (Iterator<XRul> it = rules.iterator(); it.hasNext(); ) {
                    final XRul rul = it.next();
                    if (rul.getEvent().equals(XRul.RULES_EVENT_TYPE_TIME) && rul.isEnabled()) {

                        //cancel old timer, if any
                        RuleAlarmReceiver.cancel(context, rul.getPersistenceId());
						/*Timer tmr = timers.get(rul.getPersistenceId());
						if(tmr!=null)
							tmr.cancel();*/

                        //if time is in past, remove timer
                        if (rul.getActionWhen() <= Calendar.getInstance().getTimeInMillis()) {
//					    	timers.remove(rul.getPersistenceId());
                            continue;
                        }
                        //create new timer, and add
                        RuleAlarmReceiver.SetAlarm(context, rul);
						/*Timer timer = new Timer();
				    	timer.schedule(new TimerTask() {

							@Override
							public void run() {
								triggerAction(rul);						
							}
				    	}, new Date(rul.getActionWhen()));*/
//				    	timers.put(rul.getPersistenceId(), timer);
                    }
                }
            }
        }).start();
    }

    //call to reset rule timer
    public static void resetTimedActions(Context context, final XRul rul) {

        if (false == rul.getEvent().equals(XRul.RULES_EVENT_TYPE_TIME))
            return;

//		Log.v(TAG, "Rule resetTimedActions when & now "+ new Date(rul.getActionWhen()).toLocaleString()
//				+" &" +new Date(Calendar.getInstance().getTimeInMillis()).toLocaleString());

        //cancel old timer, if any
//		AlarmReceiver.cancelAlarm(rul);
		/*Timer tmr = timers.get(rul.getPersistenceId());
		if(tmr!=null) {
			tmr.cancel();
			timers.remove(rul.getPersistenceId());
		}*/

        //create new timer, and add
        if (true == rul.isEnabled() && rul.getActionWhen() > Calendar.getInstance().getTimeInMillis()) {
            RuleAlarmReceiver.SetAlarm(context, rul);
			/*Timer timer = new Timer();
	    	timer.schedule(new TimerTask() {

				@Override
				public void run() {
					triggerAction(rul);
				}
	    	}, new Date(rul.getActionWhen()));*/
//	    	timers.put(rul.getPersistenceId(), timer);
        }

        if (ifttt_adapter != null)
            ifttt_adapter.refresh();
//    	ifttt_adapter.notifyDataSetChanged();

        Log.v(TAG, "Rule resetTimedActions " + rul.toString());
    }

    //delete rule timer if any
    public void deleteTimedActions(final XRul rul) {

        if (false == rul.getEvent().equals(XRul.RULES_EVENT_TYPE_TIME)
            /*|| false==rul.isEnabled()*/)
            return;

        Log.v(TAG, "Rule deleteTimedActions when & now " + new Date(rul.getActionWhen()).toLocaleString()
                + " &" + new Date(Calendar.getInstance().getTimeInMillis()).toLocaleString());

        //cancel old timer, if any
        RuleAlarmReceiver.cancel(getApplicationContext(), rul.getPersistenceId());
		/*Timer tmr = timers.get(rul.getPersistenceId());
		if(tmr!=null) {
			tmr.cancel();
			timers.remove(rul.getPersistenceId());
		}*/
    }

    protected void onListItemSelect(int position) {
        ifttt_adapter.toggleSelection(position);
        boolean hasCheckedItems = ifttt_adapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null) {
            mActionMode.setTitle(ifttt_adapter.getSelectedCount() + " selected");
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.caltxt_ifttt_menu_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

            /* to avoid executing below code when selection is not in current view
             * for e.g. user selects items in log view and swipes to contacts view
             * or blocked view and tries to do operation on log item selection */
            if (this == null) {
                return false;
            }

            switch (item.getItemId()) {
                case R.id.ifttt_action_delete_item:
                    AlertDialog.Builder builder = new AlertDialog.Builder(IFTTT.this, R.style.CaltxtAlertDialogTheme);
                    builder.setPositiveButton(R.string.action_delete,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // retrieve selected items and delete them out
                                    SparseBooleanArray selected = ifttt_adapter.getSelectedIds();
                                    for (int i = (selected.size() - 1); i >= 0; i--) {
                                        if (selected.valueAt(i)) {
                                            XRul rul = ifttt_adapter.getItem(selected.keyAt(i));
                                            Persistence.getInstance(IFTTT.this).deleteXRul(rul.getPersistenceId());
                                            ifttt_adapter.remove(selected.keyAt(i));
                                            deleteTimedActions(rul);
                                        }
                                    }
                                    ifttt_adapter.notifyDataSetChanged();
                                    mode.finish(); // Action picked, so close the CAB
                                }
                            });
                    builder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    if (ifttt_adapter.getSelectedCount() == 1) {
                        builder.setMessage(R.string.prompt_caltxt_ifttt_delete_selected).setTitle(R.string.prompt_caltxt_ifttt_delete);
                    } else {
                        builder.setMessage(R.string.prompt_caltxt_ifttt_delete_selected_items).setTitle(R.string.prompt_caltxt_ifttt_delete_items);
                    }
//    			builder.setIcon(R.drawable.ic_warning_white_24dp);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                case R.id.ifttt_action_selectall:
                    if (ifttt_adapter.getSelectedCount() == ifttt_adapter.getItemCount()) {
                    } else {
                        ifttt_adapter.removeSelection();
                    }
                    for (int i = 0; i < ifttt_adapter.getItemCount(); i++) {
                        onListItemSelect(i);
                    }
                    ifttt_adapter.notifyDataSetChanged();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
            ifttt_adapter.removeSelection();
            mActionMode = null;
        }
    }
}
