package com.fct.geojebus.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fct.geojebus.fragment.CompanyFragment;
import com.fct.geojebus.fragment.FareFragment;
import com.fct.geojebus.fragment.NoticeFragment;
import com.fct.geojebus.fragment.RecentFragment;

public class HomeFragmentAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public HomeFragmentAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return new RecentFragment();
        } else if (position == 1) {
            return new FareFragment();
        } else if (position == 2) {
            return new CompanyFragment();
        } else {
            return new NoticeFragment();
        }

    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}