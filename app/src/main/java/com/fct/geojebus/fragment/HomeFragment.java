/**
 * 안드로이드 거제버스 어플리케이션
 * <p/>
 * HomeFragment.java
 * 홈 화면 출력 담당
 * <p/>
 * Copyright(C) 2013 FCT. All Rights Reserved.
 */

package com.fct.geojebus.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fct.geojebus.R;
import com.fct.geojebus.adapter.HomeFragmentAdapter;
import com.fct.geojebus.ui.SlidingTabLayout;

public class HomeFragment extends Fragment {

    ViewPager pager;
    HomeFragmentAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"최근조회", "버스요금", "운수업체", "공지사항"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);

        getActivity().setTitle("거제버스");

        adapter = new HomeFragmentAdapter(getActivity().getSupportFragmentManager(), Titles, Titles.length);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.white);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        return view;
    }
}