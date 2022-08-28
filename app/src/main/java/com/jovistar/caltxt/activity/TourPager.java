package com.jovistar.caltxt.activity;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jovistar.caltxt.R;

public class TourPager extends AppCompatActivity {
    private static final String TAG = "ActivityTour";
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    ActivityTourPagerAdapter mActivityTourPagerAdapter;
    ViewPager mViewPager;
    ImageView mdot2, mdot1, mdot3, mdot4, mdot5, mdot6, mdot7;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tour);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Quick Tour");
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mActivityTourPagerAdapter = new ActivityTourPagerAdapter(
                getSupportFragmentManager());
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mActivityTourPagerAdapter);
        mdot1 = findViewById(R.id.dot1);
        mdot1.setImageResource(R.drawable.circle_teal);
        mdot2 = findViewById(R.id.dot2);
        mdot2.setImageResource(R.drawable.circle_grey);
        mdot3 = findViewById(R.id.dot3);
        mdot3.setImageResource(R.drawable.circle_grey);
        mdot4 = findViewById(R.id.dot4);
        mdot4.setImageResource(R.drawable.circle_grey);
        mdot5 = findViewById(R.id.dot5);
        mdot5.setImageResource(R.drawable.circle_grey);
        mdot6 = findViewById(R.id.dot6);
        mdot6.setImageResource(R.drawable.circle_grey);
        mdot7 = findViewById(R.id.dot7);
        mdot7.setImageResource(R.drawable.circle_grey);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                // Determine whether the user is swiping forwards or backwards through the ViewPager
            }

            @Override
            public void onPageSelected(int position) {
                // When swiping between pages, select the
                // corresponding tab.
//				getSupportActionBar().setSelectedNavigationItem(position);
                switch (position) {
                    case 0:
                        mdot1.setImageResource(R.drawable.circle_teal);
                        mdot2.setImageResource(R.drawable.circle_grey);
                        mdot3.setImageResource(R.drawable.circle_grey);
                        mdot4.setImageResource(R.drawable.circle_grey);
                        mdot5.setImageResource(R.drawable.circle_grey);
                        mdot6.setImageResource(R.drawable.circle_grey);
                        mdot7.setImageResource(R.drawable.circle_grey);
                        break;
                    case 1:
                        mdot1.setImageResource(R.drawable.circle_grey);
                        mdot2.setImageResource(R.drawable.circle_teal);
                        mdot3.setImageResource(R.drawable.circle_grey);
                        mdot4.setImageResource(R.drawable.circle_grey);
                        mdot5.setImageResource(R.drawable.circle_grey);
                        mdot6.setImageResource(R.drawable.circle_grey);
                        mdot7.setImageResource(R.drawable.circle_grey);
                        break;
                    case 2:
                        mdot1.setImageResource(R.drawable.circle_grey);
                        mdot2.setImageResource(R.drawable.circle_grey);
                        mdot3.setImageResource(R.drawable.circle_teal);
                        mdot4.setImageResource(R.drawable.circle_grey);
                        mdot5.setImageResource(R.drawable.circle_grey);
                        mdot6.setImageResource(R.drawable.circle_grey);
                        mdot7.setImageResource(R.drawable.circle_grey);
                        break;
                    case 3:
                        mdot1.setImageResource(R.drawable.circle_grey);
                        mdot2.setImageResource(R.drawable.circle_grey);
                        mdot3.setImageResource(R.drawable.circle_grey);
                        mdot4.setImageResource(R.drawable.circle_teal);
                        mdot5.setImageResource(R.drawable.circle_grey);
                        mdot6.setImageResource(R.drawable.circle_grey);
                        mdot7.setImageResource(R.drawable.circle_grey);
                        break;
                    case 4:
                        mdot1.setImageResource(R.drawable.circle_grey);
                        mdot2.setImageResource(R.drawable.circle_grey);
                        mdot3.setImageResource(R.drawable.circle_grey);
                        mdot4.setImageResource(R.drawable.circle_grey);
                        mdot5.setImageResource(R.drawable.circle_teal);
                        mdot6.setImageResource(R.drawable.circle_grey);
                        mdot7.setImageResource(R.drawable.circle_grey);
                        break;
                    case 5:
                        mdot1.setImageResource(R.drawable.circle_grey);
                        mdot2.setImageResource(R.drawable.circle_grey);
                        mdot3.setImageResource(R.drawable.circle_grey);
                        mdot4.setImageResource(R.drawable.circle_grey);
                        mdot5.setImageResource(R.drawable.circle_grey);
                        mdot6.setImageResource(R.drawable.circle_teal);
                        mdot7.setImageResource(R.drawable.circle_grey);
                        break;
                    case 6:
                        mdot1.setImageResource(R.drawable.circle_grey);
                        mdot2.setImageResource(R.drawable.circle_grey);
                        mdot3.setImageResource(R.drawable.circle_grey);
                        mdot4.setImageResource(R.drawable.circle_grey);
                        mdot5.setImageResource(R.drawable.circle_grey);
                        mdot6.setImageResource(R.drawable.circle_grey);
                        mdot7.setImageResource(R.drawable.circle_teal);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    public class ActivityTourPagerAdapter extends FragmentStatePagerAdapter {
        public ActivityTourPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new TourFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(TourFragment.ARG_OBJECT, i + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }
}
