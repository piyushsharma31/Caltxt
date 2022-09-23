package com.jovistar.caltxt.bo;

import android.content.Context;
import android.text.format.DateUtils;

import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

public class XRul implements IDTObject, Serializable {
    private static final String TAG = "XRul";
    private static final long serialVersionUID = 11L;

    protected long pid = -1;//persistence id
    boolean enabled = false;/*disabled, true-enabled*/
    boolean alwaysAsk = true;
    long tod;

    String event = ""/*IF condition "place", "time" */;
    String eventFrom = ""/*status "919953693002", "android" */;
    String eventValue = ""/*status "at Work", "12:00am" */;
    long eventTimestamp/*status "at Work" received at 12 noon */;

    String action = "";/*THEN action "text", "call", "mute phone", "unmute phone"*/
    String actionFor = "";/*send or call "919953693002" */
    long actionWhen = -1;/*send(t) or call(ed) at 12 noon for timed rules. For place rules: 0:moved to, 1:moved from*/
    String actionValue = "";/*send(t) or call(ed) "I am home" */

    String description = "";
    public static final String RULES_REPEAT_DAILY = "DAILY";
    public static final String RULES_REPEAT_HOURLY = "HOURLY";
    public static final String RULES_REPEAT_ONCE = "ONCE";

    public static final String RULES_EVENT_TYPE_STATUS = "place";
    public static final String RULES_EVENT_TYPE_TIME = "time";
    public static final String RULES_ACTION_TYPE_TEXT = "text";
    public static final String RULES_ACTION_TYPE_CALL = "call";
    public static final String RULES_ACTION_TYPE_MUTE_PHONE = "mute";
    public static final String RULES_ACTION_TYPE_UNMUTE_PHONE = "unmute";

    String repeat = RULES_REPEAT_DAILY;
//	Context mContext;

//	public XRul(Context context) {
//		mContext = context;
//	}

    public void setActionRepeat(String r) {
        repeat = r;
        if (actionWhen <= Calendar.getInstance().getTimeInMillis()) {
            if (repeat.equals(RULES_REPEAT_DAILY)) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(actionWhen);
                Calendar newcal = Calendar.getInstance();
                newcal.set(Calendar.HOUR, cal.get(Calendar.HOUR));
                newcal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
                newcal.set(Calendar.SECOND, cal.get(Calendar.SECOND));
                actionWhen = newcal.getTimeInMillis() + 24 * 60 * 60 * 1000;
            } else if (repeat.equals(RULES_REPEAT_HOURLY)) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(actionWhen);
                Calendar newcal = Calendar.getInstance();
                newcal.set(Calendar.HOUR, cal.get(Calendar.HOUR));
                newcal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
                newcal.set(Calendar.SECOND, cal.get(Calendar.SECOND));
                actionWhen = newcal.getTimeInMillis() + 60 * 60 * 1000;
            }
        }
    }

    public String getActionRepeat() {
        return repeat;
    }

    @Override
    public String searchString() {
        return description;
    }

    @Override
    public long getPersistenceId() {
        return pid;
    }

    @Override
    public void setPersistenceId(long id) {
        pid = id;
    }

    @Override
    public String getHeader() {
        return null;
    }

    @Override
    public void setHeader(String s) {
    }

    @Override
    public int getHeaderIconResource() {
        return 0;
    }

    @Override
    public int getHeaderBackground() {
        return 0;
    }

    @Override
    public int getHeaderFontColor() {
        return 0;
    }

    @Override
    public String getSubject() {
        if (RULES_EVENT_TYPE_STATUS == event) {
            description = "IF status " + (eventValue);

            if (action.equals(RULES_ACTION_TYPE_TEXT)) {
                description += " THEN SEND TEXT to" + actionValue;
            } else if (action.equals(RULES_ACTION_TYPE_CALL)) {
                description += " THEN CALL " + actionValue;
            } else if (action.equals(RULES_ACTION_TYPE_MUTE_PHONE)) {
                description += " THEN MUTE RINGER";// + actionValue;
            } else if (action.equals(RULES_ACTION_TYPE_UNMUTE_PHONE)) {
                description += " THEN UNMUTE RINGER";// + actionValue;
            }
        }
        return description;
    }

    public String getAction() {
        return action;
    }

    public String getActionValue() {
        return actionValue;
    }

    public String getActionFor() {
        return actionFor;
    }

    public long getActionWhen() {
        return actionWhen;
    }

    public void setAction(String act) {
        action = act;
    }

    public void setActionValue(String val) {
        actionValue = val;
    }

    public void setActionFor(String to) {
        actionFor = to;
    }

    public void setActionWhen(long when) {
        actionWhen = when;
    }

    public String getEvent() {
        return event;
    }

    public String getEventValue() {
        return eventValue;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public String getEventFrom() {
        return eventFrom;
    }

    public void setEvent(String evt) {
        event = evt;
    }

    public void setEventValue(String val) {
        eventValue = val;
    }

    public void setEventFrom(String from) {
        eventFrom = from;
    }

    public void setEventTimestamp(long ts) {
        eventTimestamp = ts;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAlwaysAsk() {
        return alwaysAsk;
    }

    public void setAlwaysAsk(boolean val) {
        alwaysAsk = val;
    }

    public void setEnabled(boolean val) {
        enabled = val;
    }

    public long getToD() {
        return tod;
    }

    public void setToD(long t) {
        tod = t;
    }

    @Override
    public void setSubject(String s) {
        description = s;
    }

    @Override
    public int getSubjectIconResource() {
        return 0;
    }

    @Override
    public int getSubjectBackground() {
        return 0;
    }

    @Override
    public int getSubjectFontColor() {
        return 0;
    }

    @Override
    public String getBody() {
        return null;
    }

    @Override
    public void setBody(String s) {
    }

    @Override
    public int getBodyIconResource() {
        return 0;
    }

    @Override
    public int getBodyBackground() {
        return 0;
    }

    @Override
    public int getBodyFontColor() {
        return 0;
    }

    @Override
    public String getFooter() {
        return null;
    }

    @Override
    public void setFooter(String s) {

    }

    @Override
    public int getFooterIconResource() {
        return 0;
    }

    @Override
    public int getFooterBackground() {
        return 0;
    }

    @Override
    public int getFooterFontColor() {
        return 0;
    }

    @Override
    public void populateFields(HashMap<String, Object> table) {
    }

    @Override
    public HashMap<String, Object> extractFields() {
        return null;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public int getIconResource() {
        return 0;
    }

    @Override
    public String getCName() {
        return "XRul";
    }

    @Override
    public Object getKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isComplete() {
//		Log.v(TAG, "XRul::isComplete, actionFor "+actionFor
//				+", action "+action
//				+", actionValue "+actionValue);
        return !(action.length() == 0
                || (actionFor.length() == 0 && !(action.equals(RULES_ACTION_TYPE_MUTE_PHONE)
                || action.equals(RULES_ACTION_TYPE_UNMUTE_PHONE)))
                || (actionValue.length() == 0 && action.equals(RULES_ACTION_TYPE_TEXT)));
    }

    @Override
    public String toString() {
        if (isComplete() == false)
            return "<i>Incomplete trigger</i>";

        String when = Long.toString(actionWhen);
//		DateUtils.getRelativeDateTimeString(context, actionWhen,
//				DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
        String actionTypeStr = "<b><font color=\"#2e7d32\">" + action + "</font></b>";
        String actionForStr = actionFor;
//		" <b>" +Addressbook.getInstance(context).getName(actionFor) + "</b>, +" + actionFor;
        String actionValueStr = (actionValue.length() > 0 ? " (<i>" + actionValue + "</i>)" : "");
        String conditionStr = null;

        if (action.equals(RULES_ACTION_TYPE_MUTE_PHONE)
                || action.equals(RULES_ACTION_TYPE_UNMUTE_PHONE)) {
            actionForStr = " <b>this phone</b>";
        }

        if (event.equals(RULES_EVENT_TYPE_TIME)) {
            conditionStr = (actionWhen == 0 ? "" : " " + when);
        } else {
            conditionStr = (" <b><font color=\"#1e88e5\">when</font></b> you "
                    + (actionWhen == 0 ? "arrive " : (actionWhen == 1 ? "leave " : " ")) /*CONDITION*/
                    + (actionWhen == 1 ? eventValue.substring(eventValue.lastIndexOf(" ") + 1) : eventValue) + "");
//					+ (actionWhen==1?eventValue.substring(3):eventValue) + "");
        }

        return actionTypeStr + /*ACTION TYPE - text, call, mute, unmute*/
                actionForStr + /*ACTION FOR - 919953693002, this device (for mute)*/
                actionValueStr + /*ACTION VALUE - text message (if any), blank otherwise*/
                conditionStr /*CONDITION - when TIME, PLACE change*/;

    }

    public String toString(Context context) {
//		if(isComplete()==false)
//			return "<i>Incomplete trigger</i>";
/*
        SimpleDateFormat format = null;
		String when = null;
		if(repeat.equals(ActivityRuleWizard.RULES_REPEAT_DAILY)) {
			format = new SimpleDateFormat("hh:mm a");
			when = "Daily "+format.format(actionWhen);
		} else {
			format = new SimpleDateFormat("hh:mm a, MMM dd,yyyy");
			when = format.format(actionWhen);
		}
*/
        String when = DateUtils.getRelativeDateTimeString(context, actionWhen,
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
        String actionTypeStr = "<font color=\"#2e7d32\">" + action + "</font>";
        String actionForStr = " <font color=\"#2e7d32\">" + Addressbook.getInstance(context).getName(actionFor) + "</font>, +" + XMob.toFQMN(actionFor,
                Addressbook.getInstance(context).getMyCountryCode());
        String actionValueStr = (actionValue.length() > 0 ? " (<i>" + actionValue + "</i>)" : "");
        String conditionStr = null;

        if (action.equals(RULES_ACTION_TYPE_MUTE_PHONE)
                || action.equals(RULES_ACTION_TYPE_UNMUTE_PHONE)) {
            actionForStr = " this phone";
//			actionForStr = " <b>this phone</b>";
            actionValueStr = "";
        }

        if (event.equals(RULES_EVENT_TYPE_TIME)) {
            conditionStr = "<font color=\"#1e88e5\">"
                    + (actionWhen == 0 ? "" : " " + when)
                    + "</font>";
        } else {
            conditionStr = (" when you <font color=\"#1e88e5\">"
                    + (actionWhen == 0 ? "arrive " : (actionWhen == 1 ? "leave " : " ")) /*CONDITION*/
                    + (actionWhen == 1 ? eventValue.substring(eventValue.lastIndexOf(" ") + 1) : eventValue) + "")
//					+ (actionWhen==1?eventValue.substring(3):eventValue) + "")
                    + "</font>";
        }

        return actionTypeStr + /*ACTION TYPE - text, call, mute, unmute*/
                actionForStr + /*ACTION FOR - 919953693002, this device (for mute)*/
                actionValueStr + /*ACTION VALUE - text message (if any), blank otherwise*/
                conditionStr +/*CONDITION - when TIME, PLACE change*/
                (isComplete() ? "" : " (<i>Incomplete trigger</i>)");
    }

}
