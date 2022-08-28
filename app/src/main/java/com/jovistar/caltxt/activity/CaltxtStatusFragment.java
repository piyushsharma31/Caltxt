package com.jovistar.caltxt.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.phone.Addressbook;

public class CaltxtStatusFragment extends Fragment {
    private static final String TAG = "CaltxtStatusFragment";
    public static final String ARG_OBJECT = "object";

    protected CaltxtStatusRecyclerAdapter adapter;
    RecyclerView recyclerView = null;
    CaltxtStatusPager pager;

    //for scrolling
    boolean loading = true;
    int pastVisiblesItems;
    int visibleItemCount;
    int totalItemCount;
    int viewType = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        viewType = args.getInt(ARG_OBJECT);
        pager = (CaltxtStatusPager) getActivity();

        View view = inflater.inflate(R.layout.status_grid, container, false);
        recyclerView = view.findViewById(R.id.status_recyclerview);
//        recyclerView.setHasFixedSize(true);

//		view.findViewById(R.id.toolbar).setVisibility(View.GONE);

        if (viewType == 1) {
            adapter = new CaltxtStatusRecyclerAdapter(this.getActivity(), Addressbook.getInstance(getContext()).getStatusList());
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else if (viewType == 2) {
            adapter = new CaltxtStatusRecyclerAdapter(this.getActivity(), Addressbook.getInstance(getContext()).getPlaceList());
            recyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), 4));
        }

        if (adapter != null) {
            recyclerView.setAdapter(adapter);
        }

//		recyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), 4));
//		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Log.i(TAG, "onCreateView" + viewType);
        return view;
    }

    public int getType() {
        return viewType;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public int getCount() {
        return adapter.getItemCount();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
