package com.jovistar.caltxt.activity;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.jovistar.caltxt.app.Constants;

//import androidx.core.app.FragmentManager;
//import androidx.core.app.FragmentStatePagerAdapter;

public class CaltxtStatusFragmentAdapter extends FragmentStatePagerAdapter {

    SparseArray<CaltxtStatusFragment> registeredFragments = new SparseArray<CaltxtStatusFragment>();
    private CaltxtStatusFragment mFragment;

    public CaltxtStatusFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CaltxtStatusFragment getItem(int id) {
        CaltxtStatusFragment fragment = new CaltxtStatusFragment();
        Bundle args = new Bundle();
        args.putInt(CaltxtStatusFragment.ARG_OBJECT, id + 1);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return Constants.FRAGMENT_STATUS;
        } else if (position == 1) {
            return Constants.FRAGMENT_PLACES;
        }
        return "HELO";
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        CaltxtStatusFragment fragment = (CaltxtStatusFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        mFragment = fragment;

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public CaltxtStatusFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
