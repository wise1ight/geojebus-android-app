package com.fct.geojebus.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.fct.geojebus.R;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;

public abstract class SearchFragment extends ListFragment implements SearchBox.SearchListener, View.OnTouchListener {
    public SearchBox mSearch;
    private RelativeLayout mSemiBgLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null)
            view.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));

        mSearch = (SearchBox) getActivity().findViewById(R.id.searchbox);
        mSearch.enableVoiceRecognition(this);
        mSearch.setSearchString("");
        mSearch.setSearchListener(this);

        mSemiBgLayout = (RelativeLayout) getActivity().findViewById(R.id.semi_background);
        mSemiBgLayout.setOnTouchListener(this);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE
                && resultCode == AppCompatActivity.RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mSearch.setSearchString(matches.get(0));
            mSearch.hideCircularly(getActivity());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSearchOpened() {
        mSemiBgLayout.setEnabled(true);

        Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        mSemiBgLayout.startAnimation(in);

        mSemiBgLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSearchClosed() {
        mSemiBgLayout.setEnabled(false);

        closeSearch();

        Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        mSemiBgLayout.startAnimation(out);

        mSemiBgLayout.setVisibility(View.GONE);
    }

    @Override
    public void onSearchTermChanged(String s) {
        if (getActivity() != null) {
            Search(s);
            getActivity().setTitle(s);
        }
    }

    @Override
    public void onSearch(String searchTerm) {
        Search(searchTerm);
        getActivity().setTitle(searchTerm);
    }

    @Override
    public void onResultClick(SearchResult searchResult) {

    }

    @Override
    public void onSearchCleared() {
        Search("");
    }

    protected abstract void Search(String query);

    protected void closeSearch() {
        mSearch.hideCircularly(getActivity());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && mSearch.getSearchOpen()) {
            mSearch.toggleSearch();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN && !mSearch.getSearchOpen()) {
            Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            mSemiBgLayout.startAnimation(out);

            mSemiBgLayout.setVisibility(View.GONE);
            return true;
        }
        return false;
    }
}