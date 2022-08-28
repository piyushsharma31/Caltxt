package com.jovistar.caltxt.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Blockbook;
import com.jovistar.caltxt.phone.Logbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

public abstract class DTOListUI extends AppCompatActivity {

    private static final String TAG = "DTOListUI";

    //	public static boolean isInForeground=false;
    protected DTOListAdapter log_adapter;
    protected ListView listview;
    private ActionMode mActionMode;

    //progress bar
//	View mProgressStatusView;
    protected ProgressBar mProgress;
//	private TextView mProgressMessageView;
//	int shortAnimTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dtolist);

        listview = findViewById(R.id.dtolist);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView tv = findViewById(R.id.empty_view);
        if (getComponentName().getShortClassName().contains("ActivityPhonebook")) {
            tv.setText(R.string.empty_view_pb);
            log_adapter = new DTOListAdapter(this, Addressbook.getInstance(getApplicationContext()).getList());
            listview.setAdapter(log_adapter);
            log_adapter.notifyDataSetChanged();
        } else if (getComponentName().getShortClassName().contains("ActivityCaltxtLog")) {
            log_adapter = new DTOListAdapter(this, Logbook.get(getApplicationContext()).getList());
            listview.setAdapter(log_adapter);
            log_adapter.notifyDataSetChanged();
            tv.setText(R.string.empty_view_log);
        } else if (getComponentName().getShortClassName().contains("ActivityBlockbook")) {
            log_adapter = new DTOListAdapter(this, Blockbook.getInstance(getApplicationContext()).getList());
            listview.setAdapter(log_adapter);
            log_adapter.notifyDataSetChanged();
            tv.setText(R.string.empty_view_block);
        } else if (getComponentName().getShortClassName().contains("FAQ")) {
            tv.setText(""/*R.string.no_internet_message*/);
        }
        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (mActionMode == null) {
                    /*no items selected, so perform item click actions
                     * like moving to next activity */
                    rowOnClickAction(log_adapter.getItem(position));
                } else {
                    onListItemSelect(position);
                }
            }
        });
        listview.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent,
                                           View view, int position, long id) {
                //allow multiple select on log view only
                if (log_adapter.getItem(position).getCName().equals("XCtx")) {
                    onListItemSelect(position);
                }
                return true;
            }
        });
        listview.setEmptyView(tv);
		/*dtoList = (ArrayList<IDTObject>) getIntent().getSerializableExtra("list");
		//		if (log_adapter == null)*/
//			log_adapter = new DTOListAdapter(this, dtoList);
//		listview.setAdapter(log_adapter);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        registerForContextMenu(listview);
        listview.setTextFilterEnabled(true);
//		listview.setOnItemClickListener(this);

        //progress bar
//		shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//		mProgressStatusView = findViewById(R.id.progress_status);
//		mProgressMessageView = (TextView) findViewById(R.id.progress_status_textview);
        mProgress = findViewById(R.id.progress_bar);
        mProgress.setVisibility(View.GONE);
//		mProgress.setIndeterminate(true);
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.caltxt_log_menu_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.action_delete_item:
                    AlertDialog.Builder builder = new AlertDialog.Builder(DTOListUI.this, R.style.CaltxtAlertDialogTheme);
                    builder.setPositiveButton(R.string.action_delete,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // retrieve selected items and delete them out
                                    SparseBooleanArray selected = log_adapter.getSelectedIds();
                                    for (int i = (selected.size() - 1); i >= 0; i--) {
                                        if (selected.valueAt(i)) {
                                            IDTObject selectedItem = log_adapter.getItem(selected.keyAt(i));
                                            Logbook.get(getApplicationContext()).remove((XCtx) selectedItem);
                                            log_adapter.notifyDataSetChanged();
                                        }
                                    }
                                    mode.finish(); // Action picked, so close the CAB
                                }
                            });
                    builder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    if (log_adapter.getSelectedCount() == 1) {
                        builder.setMessage(R.string.prompt_caltxt_log_delete_selected).setTitle(R.string.prompt_caltxt_log_delete);
                    } else {
                        builder.setMessage(R.string.prompt_caltxt_log_delete_selected_items).setTitle(R.string.prompt_caltxt_log_delete_items);
                    }
//    			builder.setIcon(R.drawable.ic_warning_white_24dp);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                case R.id.action_block:
                    builder = new AlertDialog.Builder(DTOListUI.this, R.style.CaltxtAlertDialogTheme);
                    builder.setPositiveButton(R.string.action_block,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // retrieve selected items and delete them out
                                    SparseBooleanArray selected = log_adapter.getSelectedIds();
                                    for (int i = (selected.size() - 1); i >= 0; i--) {
                                        if (selected.valueAt(i)) {
                                            IDTObject selectedItem = log_adapter.getItem(selected.keyAt(i));
                                            if (selectedItem.getCName().equals("XCtx")) {
                                                XCtx ctx = (XCtx) selectedItem;
//	    			            			if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                                                if (Addressbook.getInstance(getApplicationContext()).isItMe(ctx.getNumberCallee())) {
                                                    Blockbook.getInstance(getApplicationContext()).add(ctx.getUsernameCaller(), ctx.getNameCaller());
                                                } else {
                                                    Blockbook.getInstance(getApplicationContext()).add(ctx.getNumberCallee(), ctx.getNameCallee());
                                                }
                                            } else if (selectedItem.getCName().equals("XMob")) {
                                                XMob mob = (XMob) selectedItem;
                                                Blockbook.getInstance(getApplicationContext()).add(mob.getUsername(), mob.getName());
                                            }
                                        }
                                    }
                                    Connection.get().addAction(Constants.contactBlockedProperty, null, null);
//		                        log_adapter.notifyDataSetChanged();
                                    mode.finish(); // Action picked, so close the CAB
                                }
                            });
                    builder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    if (log_adapter.getSelectedCount() == 1) {
                        builder.setMessage(R.string.prompt_caltxt_log_block_selected).setTitle(R.string.prompt_caltxt_log_block);
                    } else {
                        builder.setMessage(R.string.prompt_caltxt_log_block_selected_items).setTitle(R.string.prompt_caltxt_log_block_items);
                    }
//    			builder.setIcon(R.drawable.ic_warning_white_24dp);
                    dialog = builder.create();
                    dialog.show();

                    return true;
                case R.id.action_selectall:
                    if (log_adapter.getSelectedCount() == log_adapter.getCount()) {
                    } else {
                        log_adapter.removeSelection();
                    }
                    for (int i = 0; i < log_adapter.getCount(); i++) {
                        onListItemSelect(i);
                    }
                    log_adapter.notifyDataSetChanged();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
            log_adapter.removeSelection();
            mActionMode = null;
        }
    }

    private void onListItemSelect(int position) {
        log_adapter.toggleSelection(position);
        boolean hasCheckedItems = log_adapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null) {
            mActionMode.setTitle(log_adapter.getSelectedCount() + " selected");
        }
    }

    @Override
    protected void onResume() {
//		isInForeground=true;
        log_adapter.notifyDataSetChanged();
//		log_adapter.notifyDataSetInvalidated();
        super.onResume();
//		Log.d(TAG, "onResume, isInForeground TRUE");
    }

    @Override
    protected void onDestroy() {
//		isInForeground=false;
        super.onDestroy();
//		Log.d(TAG, "onResume, isInForeground FALSE");
    }

    @Override
    protected void onPause() {
        super.onPause();
//		isInForeground=false;
//		Log.d(TAG, "onResume, isInForeground FALSE");
    }

    public void setEmptyView(View emptyView) {
        listview.setEmptyView(emptyView);
    }

    private void rowOnClickAction(IDTObject obj) {
        Intent profile = new Intent(this, Profile.class);
        XMob mob = null;
        if (obj.getCName().equals("XCtx")) {
            String uname = null;
            XMob nmob = new XMob();

            XCtx ri = (XCtx) obj;//(XCtx) getItem(Integer.parseInt((v.getTag()).toString()));
//			if (ri.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
            if (Addressbook.getInstance(getApplicationContext()).isItMe(ri.getUsernameCaller())) {
                uname = ri.getNumberCallee();
                nmob.setName(ri.getNameCallee());
                nmob.setNumber(ri.getNumberCallee()/*, CaltxtApp.getMyCountryCode()*/);
            } else {
                uname = ri.getUsernameCaller();
                nmob.setName(ri.getNameCaller());
                nmob.setNumber(ri.getUsernameCaller()/*, CaltxtApp.getMyCountryCode()*/);
            }
            nmob.setUsername(XMob.toFQMN(nmob.getNumber(), Addressbook.getInstance(getApplicationContext()).getMyCountryCode()));
            mob = Addressbook.getInstance(getApplicationContext()).getRegistered(uname);
            if (mob == null) {
                mob = nmob;
            }
//			mob.setHeadline(Globals.getContactHeadline(mob.getUsername()));
            profile.putExtra("IDTOBJECT", mob);
            startActivity(profile);
        } else if (obj.getCName().equals("XMob")) {
            mob = (XMob) obj;//(XMob) getItem(Integer.parseInt((v.getTag()).toString()));
//			mob = (XMob)rowitem;
            profile.putExtra("IDTOBJECT", mob);
            startActivity(profile);
        }
    }

    /*
    public void removeItem(IDTObject obj) {
//		IDTObject o = log_adapter.getItem(location);
//		IDTObject o = getItem(location);
        log_adapter.remove(obj);
        log_adapter.notifyDataSetChanged();
    }

    public void clear() {
        log_adapter.clear();
        log_adapter.notifyDataSetChanged();
    }

    public void notifyChange() {
        log_adapter.notifyDataSetChanged();
    }
*/
    public void filter(String filterpattern) {
        log_adapter.getFilter().filter(filterpattern);
        log_adapter.notifyDataSetChanged();
    }

    public View getDTOView() {
        return listview;
    }

    public int size() {
        return listview.getCount();
//		return log_adapter.getCount();
    }

    public IDTObject getItem(int location) {
        return (IDTObject) listview.getItemAtPosition(location);
//		return log_adapter.getItem(location);
    }
/*
	public void prependDTObject(IDTObject o) {
		log_adapter.add(o);
		log_adapter.notifyDataSetChanged();
	}
*/

    /**
     * Shows the progress UI and hides the Signup form.
     *
     * @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2) public void showProgress1(final boolean show, final View viewToHide, String msg) {
     * // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
     * // for very easy animations. If available, use these APIs to fade-in
     * // the progress spinner.
     * mProgressMessageView.setText(msg);
     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
     * <p>
     * mProgressStatusView.setVisibility(View.VISIBLE);
     * mProgressStatusView.animate().setDuration(shortAnimTime)
     * .alpha(show ? 1 : 0)
     * .setListener(new AnimatorListenerAdapter() {
     * @Override public void onAnimationEnd(Animator animation) {
     * mProgressStatusView.setVisibility(show ? View.VISIBLE
     * : View.GONE);
     * }
     * });
     * <p>
     * viewToHide.setVisibility(View.VISIBLE);
     * viewToHide.animate().setDuration(shortAnimTime)
     * .alpha(show ? 0 : 1)
     * .setListener(new AnimatorListenerAdapter() {
     * @Override public void onAnimationEnd(Animator animation) {
     * viewToHide.setVisibility(show ? View.GONE
     * : View.VISIBLE);
     * }
     * });
     * } else {
     * // The ViewPropertyAnimator APIs are not available, so simply show
     * // and hide the relevant UI components.
     * mProgressStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
     * viewToHide.setVisibility(show ? View.GONE : View.VISIBLE);
     * }
     * }
     * <p>
     * public void updateProgress(int u, String s) {
     * mProgress.setProgress(u);
     * mProgressMessageView.setText(s);
     * }
     */
}
