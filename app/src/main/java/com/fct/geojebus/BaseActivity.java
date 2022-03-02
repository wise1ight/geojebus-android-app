package com.fct.geojebus;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.fct.geojebus.fragment.FavoriteFragment;
import com.fct.geojebus.fragment.HomeFragment;
import com.fct.geojebus.fragment.RouteSearchFragment;
import com.fct.geojebus.fragment.StopSearchFragment;

/**
 * 거제버스 안드로이드 앱
 * <p/>
 * BaseActivity.java
 * 앱 기본 UI 정의
 */

public class BaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private static final String NAV_ITEM_ID = "navItemId";
    private final Handler mDrawerActionHandler = new Handler();
    private long backKeyPressedTime = 0;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mNavItemId;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null == savedInstanceState) {
            mNavItemId = R.id.drawer_item_home;
        } else {
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().findItem(mNavItemId).setChecked(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open,
                R.string.close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        navigate(mNavItemId);
    }

    private void navigate(final int itemId) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (itemId) {
            case R.id.drawer_item_home:
                fragmentTransaction.replace(R.id.content, new HomeFragment());
                fragmentTransaction.commit();
                mNavItemId = itemId;
                break;
            case R.id.drawer_item_stop:
                fragmentTransaction.replace(R.id.content, new StopSearchFragment());
                fragmentTransaction.commit();
                mNavItemId = itemId;
                break;
            case R.id.drawer_item_route:
                fragmentTransaction.replace(R.id.content, new RouteSearchFragment());
                fragmentTransaction.commit();
                mNavItemId = itemId;
                break;
            case R.id.drawer_item_favorite:
                fragmentTransaction.replace(R.id.content, new FavoriteFragment());
                fragmentTransaction.commit();
                mNavItemId = itemId;
                break;
            case R.id.drawer_item_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.drawer_item_info:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        menuItem.setChecked(true);

        mDrawerLayout.closeDrawer(GravityCompat.START);
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mNavItemId != menuItem.getItemId())
                    navigate(menuItem.getItemId());
            }
        }, DRAWER_CLOSE_DELAY_MS);
        return true;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.support.v7.appcompat.R.id.home) {
            return mDrawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(this,
                        getString(R.string.exit_msg), Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, mNavItemId);
    }
}