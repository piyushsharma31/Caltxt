package com.jovistar.caltxt.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.firebase.client.DatabaseFirebase;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.data.ConnectivityBroadcastReceiver;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Blockbook;
import com.jovistar.caltxt.phone.Logbook;
import com.jovistar.caltxt.phone.Searchbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

public class CaltxtFragment extends Fragment {
    private static final String TAG = "CaltxtFragment";
    public static final String ARG_OBJECT = "object";

    protected RecyclerAdapter adapter;
    RecyclerView recyclerView = null;
    //	protected DTOListAdapter adapter;
    protected ProgressBar mProgress;
    //	protected ListView listview;
    public ActionMode mActionMode;
    int viewType = 0;
    TextView tv = null;
    CaltxtPager pager;

    //for scrolling
    boolean loading = true;
    int pastVisiblesItems;
    int visibleItemCount;
    int totalItemCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        int i = args.getInt(ARG_OBJECT);
        View view = inflater.inflate(R.layout.dtolistpager, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
//        recyclerView.setHasFixedSize(false);

        mProgress = view.findViewById(R.id.progress_bar_pager);
        mProgress.setVisibility(View.GONE);
        tv = view.findViewById(R.id.empty);
        viewType = i;

        if (viewType == 1) {
            if (Logbook.get(getContext()).getList().size() != Persistence.getInstance(getContext()).getCountXCTX()) {
                Logbook.get(getContext()).load();
            }
            adapter = new RecyclerAdapter(this, Logbook.get(getContext()).getList());
//			adapter = new DTOListAdapter(getActivity(), Logbook.get().getList());
        } else if (viewType == 2) {
            // load in RebootService; load here as well so that its recovered in case fragment is rebuilt (w/o calling RebootService)
            if (Addressbook.getInstance(getContext()).getList().size() != Persistence.getInstance(getContext()).getCountXMOB()) {
                Addressbook.getInstance(this.getContext()).load();
            }
            adapter = new RecyclerAdapter(this, Addressbook.getInstance(this.getContext()).getList());
//			adapter = new DTOListAdapter(getActivity(), Addressbook.get().getList());
        } else if (viewType == 3) {
            adapter = new RecyclerAdapter(this, Searchbook.get(getContext()).getList());
//			adapter = new DTOListAdapter(getActivity(), Searchbook.get().getList());
        } else if (viewType == 4) {
            adapter = new RecyclerAdapter(this, Blockbook.getInstance(getContext()).getList());
//			adapter = new DTOListAdapter(getActivity(), Blockbook.get().getList());
        }
        if (adapter != null) {
            recyclerView.setAdapter(adapter);
//			setListAdapter(adapter);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        pager = (CaltxtPager) getActivity();

        Log.i(TAG, "onCreateView" + viewType);
        return view;
    }

    public int getType() {
        return viewType;
    }

    public void resetEmptyView() {
        if (adapter.getItemCount() > 0) {
            tv.setText("");
            tv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (viewType == 1) {
//			adapter = callsAdapter;
//			adapter.notifyDataSetChanged();
            tv.setText(R.string.empty_view_log);
        } else if (viewType == 2) {
            tv.setText(R.string.empty_view_pb);
//			adapter = contactsAdapter;
//			adapter.notifyDataSetChanged();
        } else if (viewType == 3) {
            tv.setText(""/*"There is no one here!"*//*R.string.no_internet_message*/);
        } else if (viewType == 4) {
//			adapter = blockedAdapter;
//			adapter.notifyDataSetChanged();
            tv.setText(R.string.empty_view_block);
        }
        if (adapter.getItemCount() > 0) {
            tv.setVisibility(View.GONE);
        }
//		getListView().setEmptyView(tv);
//        recyclerView.setOnClickListener(l)
/*
        getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				if (mActionMode == null) {
					rowOnClickAction(adapter.getItem(position));
		        } else {
		        	onListItemSelect(position);
		        }
			}
		});
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

	        public boolean onItemLongClick(AdapterView<?> parent,
	                View view, int position, long id) {
	        	//allow multiple select on log view only
	    		if (adapter.getItem(position).getCName().equals("XCtx")) {
	    			onListItemSelect(position);
	    		}
	            return true;
	        }
		});
*/
	/*dtoList = (ArrayList<IDTObject>) getIntent().getSerializableExtra("list");
	//		if (adapter == null)*/
//		adapter = new DTOListAdapter(this, dtoList);
//	listview.setAdapter(adapter);
        registerForContextMenu(recyclerView);
		/*recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.i(TAG, "TRACK addOnItemTouchListener::onClick");
                //If ActionMode not null select item
				if (mActionMode == null) {
					rowOnClickAction(adapter.getItem(position));
		        } else {
		        	onListItemSelect(position);
		        }
            }
 
            @Override
            public void onLongClick(View view, int position) {
                Log.i(TAG, "TRACK addOnItemTouchListener::onLongClick");
	        	//allow multiple select on log view only
	    		if (adapter.getItem(position).getCName().equals("XCtx")) {
	    			onListItemSelect(position);
	    		}
            }
        }));*/
/*		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		registerForContextMenu(getListView());
		getListView().setTextFilterEnabled(true);*/
        if (viewType == 3) {
            final int preLast;
            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);

            int firstVisible = mLayoutManager.findFirstVisibleItemPosition();
            int lastVisible = mLayoutManager.findLastCompletelyVisibleItemPosition();
            totalItemCount = mLayoutManager.getItemCount();
            if ((firstVisible == 0 || firstVisible == -1) && lastVisible + 1 == totalItemCount
                    && CaltxtPager.OP_GETALL_SVC_CALTXT_USER_COMPLETE &&
                    ConnectivityBroadcastReceiver.haveNetworkConnection()/*RebootService.getConnection(getContext())Connection.get().isConnected()*/) {
/*
                XMob user = new XMob();
                user.setHeadline(pager.mCurFilter == null ? "" : pager.mCurFilter);
                int cnt = Searchbook.get(getContext()).getCount();
                if (Searchbook.search_result_contain_self) {
                    cnt = cnt + 1;
                }
                user.setNumber(String.valueOf(cnt));
                user.setUsername(XMob.toFQMN(user.getNumber(), Addressbook.getInstance(getContext()).getMyCountryCode()));
                user.setName(String.valueOf(Searchbook.DISCOVER_COUNT_AT_A_TIME));
                ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_CALTXT_USER,
                        ModelFacade.getInstance().OP_GETALL, user, pager);*/
                CaltxtPager.OP_GETALL_SVC_CALTXT_USER_COMPLETE = false;

                mProgress.setVisibility(View.VISIBLE);
//				IDTObject object = adapter.getItem(cnt-1);
//				Log.d(TAG, "onScrolled, last object "+object);
                DatabaseFirebase.getNextRegisteredContacts(null);
            }

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) //check for scroll down
                    {
                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
//					final int lastItem = pastVisiblesItems + visibleItemCount;
                        Log.v(TAG, "visibleItemCount " + visibleItemCount + ", totalItemCount " + totalItemCount
                                + ", pastVisiblesItems " + pastVisiblesItems);

                        if (loading) {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                loading = false;
                                Log.v(TAG, "Last Item Wow !");

                                int count = Searchbook.get(getContext()).getCount();

//							preLast = lastItem;

                                if (CaltxtPager.OP_GETALL_SVC_CALTXT_USER_COMPLETE &&
                                        ConnectivityBroadcastReceiver.haveNetworkConnection()/*RebootService.getConnection(getContext())Connection.get().isConnected()*/) {

                                    if (Searchbook.search_result_contain_self) {
                                        count = count + 1;
                                    }
                                    /*XMob user = new XMob();
                                    user.setHeadline(pager.mCurFilter == null ? "" : pager.mCurFilter);
                                    user.setNumber(String.valueOf(count));
                                    user.setUsername(XMob.toFQMN(user.getNumber(), Addressbook.getInstance(getContext()).getMyCountryCode()));
                                    user.setName(String.valueOf(Searchbook.DISCOVER_COUNT_AT_A_TIME));
                                    ModelFacade.getInstance().fxAsyncServiceRequest(ModelFacade.getInstance().SVC_CALTXT_USER,
                                            ModelFacade.getInstance().OP_GETALL, user, pager);*/
                                    CaltxtPager.OP_GETALL_SVC_CALTXT_USER_COMPLETE = false;

                                    mProgress.setVisibility(View.VISIBLE);
                                    IDTObject object = adapter.getItem(0);
                                    Log.d(TAG, "onScrolled, last object " + object);
                                    DatabaseFirebase.getNextRegisteredContacts(((XMob) object).getUsername());
                                }
                            }
                        }
                    }
                }
            });
        }
        //	listview.setOnItemClickListener(this);

        //progress bar
//	shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//	mProgressStatusView = findViewById(R.id.progress_status);
//	mProgressMessageView = (TextView) findViewById(R.id.progress_status_textview);
//	mProgress = (ProgressBar) findViewById(R.id.progress_bar);
//	mProgress.setVisibility(View.GONE);
//	mProgress.setIndeterminate(true);
//		ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.Planets, android.R.layout.simple_list_item_1);
//		getListView().setOnItemClickListener(this);
//		((CaltxtPager)getActivity()).setCurrentFragment(this);
        Log.i(TAG, "onActivityCreated" + viewType + ", size " + adapter.getItemCount());
    }

    /*
        public interface ClickListener {

            void onClick(View view, int position);

            void onLongClick(View view, int position);
        }

        public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

            private GestureDetector gestureDetector;
            private ClickListener clickListener;

            public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
                this.clickListener = clickListener;
                gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        Log.i(TAG, "TRACK RecyclerTouchListener::onSingleTapUp");
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        Log.i(TAG, "TRACK RecyclerTouchListener::onLongPress");
                        View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (child != null && clickListener != null) {
                            clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                        }
                    }
                });
            }

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                Log.i(TAG, "TRACK RecyclerTouchListener::onInterceptTouchEvent");

                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                    clickListener.onClick(child, rv.getChildPosition(child));
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                Log.i(TAG, "TRACK RecyclerTouchListener::onTouchEvent");
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        }
    */
    public int getCount() {
        return adapter.getItemCount();
    }

    public void clear() {
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    protected XMob getContactFromXCtx(XCtx ctx) {
        String uname = null;
        XMob nmob = new XMob();

        XCtx ri = ctx;//(XCtx) getItem(Integer.parseInt((v.getTag()).toString()));
//		if (ri.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
        if (Addressbook.getInstance(getContext()).isItMe(ri.getUsernameCaller())) {
            uname = ri.getNumberCallee();
            nmob.setName(ri.getNameCallee());
            nmob.setNumber(ri.getNumberCallee()/*, CaltxtApp.getMyCountryCode()*/);
            nmob.setUsername(XMob.toFQMN(ri.getNumberCallee(), Addressbook.getMyCountryCode()));
        } else {
            uname = ri.getUsernameCaller();
            nmob.setName(ri.getNameCaller());
            nmob.setNumber(ri.getUsernameCaller()/*, CaltxtApp.getMyCountryCode()*/);
            nmob.setUsername(XMob.toFQMN(ri.getUsernameCaller(), Addressbook.getMyCountryCode()));
        }
        XMob mob = Addressbook.getInstance(getContext()).getRegistered(uname);
        if (mob == null) {
            mob = nmob;
        }

        return mob;
    }

    protected void rowOnClickActionView(IDTObject obj) {
        XMob mob = null;
        if (obj.getCName().equals("XCtx")) {
            mob = getContactFromXCtx((XCtx) obj);
            if(mob.getUsername().length()==0 || mob.getUsername().equals(Addressbook.getInstance(this.getContext()).getMyProfile().getUsername())) {
                // do not open caltxt input screen if this item is this user itself
                Log.i(TAG, "rowOnClickActionOpen: do not open profile screen if this item is this user itself");
                return;
            }

            //open contact profile
            Intent profile = new Intent(getActivity(), Profile.class);
            profile.putExtra("IDTOBJECT", mob);
            startActivity(profile);
        } else if (obj.getCName().equals("XMob")) {
            mob = (XMob) obj;//(XMob) getItem(Integer.parseInt((v.getTag()).toString()));
            Intent profile = new Intent(getActivity(), Profile.class);
            profile.putExtra("IDTOBJECT", mob);
            startActivity(profile);
        }
    }

    protected void rowOnClickActionOpen(IDTObject obj) {
        XMob mob = null;
        if (obj.getCName().equals("XCtx")) {
            mob = getContactFromXCtx((XCtx) obj);
            if(mob.getUsername().length()==0 || mob.getUsername().equals(Addressbook.getInstance(this.getContext()).getMyProfile().getUsername())) {
                // do not open caltxt input screen if this item is this user itself
                Log.i(TAG, "rowOnClickActionOpen: do not open caltxt input screen if this item is this user itself");
                return;
            }

            //make a call
            Intent caltxtInput = new Intent(getActivity(), CaltxtInputActivity.class);
            caltxtInput.putExtra("IDTOBJECT", mob);
//			caltxtInput.putExtra("VIEW", Constants.INPUT_VIEW);
            startActivity(caltxtInput);
        } else if (obj.getCName().equals("XMob")) {
            mob = (XMob) obj;//(XMob) getItem(Integer.parseInt((v.getTag()).toString()));
//			mob = (XMob)rowitem;
            Intent profile = new Intent(getActivity(), Profile.class);
            profile.putExtra("IDTOBJECT", mob);
            startActivity(profile);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void onListItemSelect(int position) {
        adapter.toggleSelection(position);
        boolean hasCheckedItems = adapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = ((CaltxtPager) getActivity()).startSupportActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null) {
            mActionMode.setTitle(adapter.getSelectedCount() + " selected");
        }
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

            /* to avoid executing below code when selection is not in current view
             * for e.g. user selects items in log view and swipes to contacts view
             * or blocked view and tries to do operation on log item selection */
            if (getActivity() == null) {
                return false;
            }

            switch (item.getItemId()) {
                case R.id.action_delete_item:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CaltxtAlertDialogTheme);
                    builder.setPositiveButton(R.string.action_delete,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // retrieve selected items and delete them out
                                    SparseBooleanArray selected = adapter.getSelectedIds();
                                    int itemIndex = 0;
                                    for (int i = (selected.size() - 1); i >= 0; i--) {
                                        if (selected.valueAt(i)) {
                                            itemIndex = selected.keyAt(i);
                                            IDTObject selectedItem = adapter.getItem(itemIndex);
                                            Logbook.get(getContext()).remove((XCtx) selectedItem);

                                            // 23-JUL-17 added below line
                                            // 03-JAN-19 commented these lines, giving index out of bound on multiple deletes
//                                            adapter.dtoListOriginal.remove(itemIndex);
//                                            adapter.dtoListFiltered.remove(itemIndex);
//										recyclerView.removeViewAt(itemIndex);
                                            adapter.notifyItemRemoved(itemIndex);
                                            adapter.notifyItemRangeChanged(itemIndex, adapter.getItemCount());
                                            Log.i(TAG, "delete row " + itemIndex);
                                        }
                                    }
//		                        adapter.notifyDataSetChanged();
                                    // 23-JUL-17 commented below line (addAction)
//    							RebootService.getConnection(getContext()).addAction(Constants.logDeletedProperty, null, null);
                                    mode.finish(); // Action picked, so close the CAB
                                }
                            });
                    builder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    if (adapter.getSelectedCount() == 1) {
                        builder.setMessage(R.string.prompt_caltxt_log_delete_selected).setTitle(R.string.prompt_caltxt_log_delete);
                    } else {
                        builder.setMessage(R.string.prompt_caltxt_log_delete_selected_items).setTitle(R.string.prompt_caltxt_log_delete_items);
                    }
//    			builder.setIcon(R.drawable.ic_warning_white_24dp);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                case R.id.action_block:
                    builder = new AlertDialog.Builder(getActivity(), R.style.CaltxtAlertDialogTheme);
                    builder.setPositiveButton(R.string.action_block,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // retrieve selected items and delete them out
                                    SparseBooleanArray selected = adapter.getSelectedIds();
                                    for (int i = (selected.size() - 1); i >= 0; i--) {
                                        if (selected.valueAt(i)) {
                                            IDTObject selectedItem = adapter.getItem(selected.keyAt(i));
                                            if (selectedItem.getCName().equals("XCtx")) {
                                                XCtx ctx = (XCtx) selectedItem;
//	    			            			if (ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
                                                if (Addressbook.getInstance(getContext()).isItMe(ctx.getNumberCallee())) {
                                                    Blockbook.getInstance(getContext()).add(ctx.getUsernameCaller(), ctx.getNameCaller());
                                                } else {
                                                    Blockbook.getInstance(getContext()).add(ctx.getNumberCallee(), ctx.getNameCallee());
                                                }
                                            } else if (selectedItem.getCName().equals("XMob")) {
                                                XMob mob = (XMob) selectedItem;
                                                Blockbook.getInstance(getContext()).add(mob.getUsername(), mob.getName());
                                            }
                                        }
                                    }
                                    Connection.get().addAction(Constants.contactBlockedProperty, null, null);
//		                        adapter.notifyDataSetChanged();
                                    mode.finish(); // Action picked, so close the CAB
                                }
                            });
                    builder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    if (adapter.getSelectedCount() == 1) {
                        builder.setMessage(R.string.prompt_caltxt_log_block_selected).setTitle(R.string.prompt_caltxt_log_block);
                    } else {
                        builder.setMessage(R.string.prompt_caltxt_log_block_selected_items).setTitle(R.string.prompt_caltxt_log_block_items);
                    }
//    			builder.setIcon(R.drawable.ic_warning_white_24dp);
                    dialog = builder.create();
                    dialog.show();

                    return true;
                case R.id.action_selectall:
                    if (adapter.getSelectedCount() == adapter.getItemCount()) {
                    } else {
                        adapter.removeSelection();
                    }
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        onListItemSelect(i);
                    }
                    adapter.notifyDataSetChanged();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
            adapter.removeSelection();
            mActionMode = null;
        }
    }

    public void notifyDataSetChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            if (tv != null && adapter.getItemCount() > 0) {
                tv.setText("");
            }
        }
    }

    public void notifyItemChanged(int position) {
        if (adapter != null) {
            adapter.notifyItemChanged(position);
        }
    }

    public void filter(String filterpattern) {
        if (adapter != null) {
            adapter.getFilter().filter(filterpattern);
            adapter.notifyDataSetChanged();
        }
    }

    public IDTObject getItem(int location) {
//		return (IDTObject)getListView().getItemAtPosition(location);
        return adapter.getItem(location);
    }
}
