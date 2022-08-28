package com.jovistar.caltxt.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import androidx.appcompat.widget.ListPopupWindow;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.service.RebootService;
import com.jovistar.commons.bo.XMob;
import com.jovistar.commons.bo.XReqSts;
import com.jovistar.commons.bo.XRes;
import com.jovistar.commons.exception.CCMException;
import com.jovistar.commons.ui.IDisplayObject;

public class EditTextCaltxtStatus extends EditTextCaltxt implements android.widget.AdapterView.OnItemClickListener,
        PopupWindow.OnDismissListener {
    private static final String TAG = "EditTextCaltxtStatus";

    Context mContext;
    IDisplayObject activity;

    ListPopupWindow lpw;
    String[] listStatus;

    public void setCallback(IDisplayObject activity) {
        this.activity = activity;
    }

    public void setAnchorView(View view) {
        lpw.setAnchorView(view);
    }

    public EditTextCaltxtStatus(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setText(SignupProfile.getPreference(context, context.getString(R.string.profile_key_status_headline)));

        int left_icon_resource = Addressbook.getInstance(getContext()).getContactStatusIconResource(Addressbook.getInstance(getContext()).getMyProfile());
        Drawable d1 = context.getResources().getDrawable(left_icon_resource);
        d1 = CaltxtPager.resize(context, d1, 48);
        Drawable d2 = context.getResources().getDrawable(R.drawable.ic_done_white_24dp);
        setCompoundDrawablePadding(10);
        setCompoundDrawablesWithIntrinsicBounds(d1, null, d2, null);

        listStatus = new String[]{XMob.STRING_STATUS_DND, XMob.STRING_STATUS_AVAILABLE, XMob.STRING_STATUS_AWAY, XMob.STRING_STATUS_BUSY};
        lpw = new ListPopupWindow(context);
        ProfileStatusAdapter adapter = new ProfileStatusAdapter(context, listStatus);
        lpw.setAdapter(adapter);
//        lpw.setAnchorView(this);
        lpw.setModal(true);
//        ColorDrawable cd = new ColorDrawable(R.color.white);
//        cd.setAlpha(255);
//        lpw.setBackgroundDrawable(cd);
        lpw.setOnItemClickListener(this);
        lpw.setOnDismissListener(this);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
//		        final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
//		        final int DRAWABLE_BOTTOM = 3;

                // Check if touch point is in the area of the right button
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (getRight()
                            - getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        updateStatus();
                        lpw.dismiss();

                        return true;
                    } else if (event.getRawX() <= (getLeft()
                            + getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                        lpw.show();
                        return true;
                    }
                }
                return false;
            }
        });
        Log.i(TAG, "mPhoneStatusView setCustomView");
    }

    void updateStatus() {
        XRes res = new XRes();
        res.op = -1;
        res.svc = -1;
        res.status = new XReqSts();
        res.status.cd = CCMException.SUCCCESS;
        try {
            activity.callback(res);
        } catch (CCMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (getText().toString().length() > 0) {
            Addressbook.getInstance(getContext()).getMyProfile().setHeadline(getText().toString());
            SignupProfile.setPreference(getContext(),
                    mContext.getString(R.string.profile_key_status_headline),
                    getText().toString());
            RebootService.is_status_dirty = true;
            //update status on server also
//            ModelFacade.getInstance().fxAsyncServiceRequest(
//                    ModelFacade.getInstance().SVC_CALTXT_USER,
//                    ModelFacade.getInstance().OP_SET, Addressbook.getInstance(getContext()).getMyProfile(),
//                    activity);
//			RebootService.BroadcastStatus();//- commented 5/APR/16 - integrated in Adapter::getView()
        }
    }

    public void show() {
        lpw.show();
//        lpw.setAnchorView(this);
    }

    public EditTextCaltxtStatus(Context context) {
        super(context);
        Log.i(TAG, "mPhoneStatusView EditTextCaltxtStatus");
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        String item = listStatus[position];
        setText(item);
        int left_icon_resource = R.drawable.ic_available_white_24dp;
        Addressbook.getInstance(getContext()).getMyProfile().setStatusAvailable();

        if (item.equals(XMob.STRING_STATUS_DND)) {
            left_icon_resource = R.drawable.ic_donotdisturb_white_24dp;
            Addressbook.getInstance(getContext()).getMyProfile().setStatusDND();
        } else if (item.equals(XMob.STRING_STATUS_AVAILABLE)) {
            left_icon_resource = R.drawable.ic_available_white_24dp;
            Addressbook.getInstance(getContext()).getMyProfile().setStatusAvailable();
        } else if (item.equals(XMob.STRING_STATUS_AWAY)) {
            left_icon_resource = R.drawable.ic_away_white_24dp;
            Addressbook.getInstance(getContext()).getMyProfile().setStatusAway();
        } else if (item.equals(XMob.STRING_STATUS_BUSY)) {
            left_icon_resource = R.drawable.ic_busy_white_24dp;
            Addressbook.getInstance(getContext()).getMyProfile().setStatusBusy();
        }
        Drawable d1 = this.getResources().getDrawable(left_icon_resource);
        d1 = CaltxtPager.resize(mContext, d1, 48);
        Drawable d2 = this.getResources().getDrawable(R.drawable.ic_done_white_24dp);
        setCompoundDrawablesWithIntrinsicBounds(d1, null, d2, null);
//        setCompoundDrawablesWithIntrinsicBounds( left_icon_resource, 0, R.drawable.numberpicker_down_normal_holo_light, 0);

//        Addressbook.get().setMyStatusFromIconResource(left_icon_resource);
//        int status = Addressbook.get().getStatusFromIconResource(left_icon_resource);
        SignupProfile.setPreference(mContext, mContext.getString(R.string.profile_key_status_icon), left_icon_resource);
//		Addressbook.getMyProfile().setStatus(status);

        updateStatus();
        lpw.dismiss();
    }

    @Override
    public void onDismiss() {
        updateStatus();
        lpw.dismiss();
    }
}
