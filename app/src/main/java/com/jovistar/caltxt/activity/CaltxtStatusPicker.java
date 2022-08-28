package com.jovistar.caltxt.activity;

import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.phone.Addressbook;

public class CaltxtStatusPicker extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private CaltxtStatusRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    TypedArray imgs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_grid);
        mRecyclerView = findViewById(R.id.status_recyclerview);

//		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
//		getSupportActionBar().setTitle("Change status");
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(this, 4);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new CaltxtStatusRecyclerAdapter(
                this.getApplicationContext(),
                Addressbook.getInstance(getApplicationContext()).getPlaceList());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//		imgs.recycle();
        super.onDestroy();
    }

}
