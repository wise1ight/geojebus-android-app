package com.fct.geojebus;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fct.geojebus.adapter.RouteDetailAdapter;
import com.fct.geojebus.adapter.RouteStopListAdapter;
import com.fct.geojebus.database.BusDB;
import com.fct.geojebus.database.FavoriteDB;
import com.fct.geojebus.database.RecentDB;
import com.fct.geojebus.model.RouteBusItem;
import com.fct.geojebus.model.RouteDetailItem;
import com.fct.geojebus.model.RouteStopItem;
import com.fct.geojebus.ui.AdapterWrapper;
import com.fct.geojebus.ui.DividerAdapter;
import com.fct.geojebus.ui.HeaderViewAdapter;
import com.fct.geojebus.util.HttpJSONQuery;
import com.fct.geojebus.util.HttpJSONQueryCallback;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.AppActionInfoBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RouteDetailActivity extends DetailBaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    String rt_depart_time;
    String rt_stop_gap;
    private HttpJSONQuery aq;
    private BusDB mDbHelper;
    private FavoriteDB mFavoriteDb;
    private RecentDB mRecentDb;
    private int detailItemID;
    private String rt_num;
    private String rt_country;
    private List<RouteDetailItem> mDetailArray;
    private List<RouteStopItem> mStopArray;
    private RouteStopListAdapter realTimeAdapter;
    private ArrayList<String> mStopList = new ArrayList<String>();

    AdapterWrapper createAdapter() {
        AdapterWrapper wrapper = new AdapterWrapper();
        //어댑터 내용이들어간다
        //노선 정보, 경유 정류장 목록

        HeaderViewAdapter headerViewAdapter = new HeaderViewAdapter();
        View headerView = new View(RouteDetailActivity.this);
        headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mFlexibleSpaceImageHeight));
        headerView.setMinimumHeight(mFlexibleSpaceImageHeight);
        headerView.setClickable(true);
        headerViewAdapter.addHeaderView(0, headerView);
        wrapper.wrapAdapter(headerViewAdapter);

        wrapper.wrapAdapter(new RouteDetailAdapter(mDetailArray))
                .sectionHeader("노선 정보", "#00000000");

        wrapper.wrapAdapter(new DividerAdapter());

        realTimeAdapter = new RouteStopListAdapter(RouteDetailActivity.this, mStopArray);
        wrapper.wrapAdapter(realTimeAdapter)
                .sectionHeader("경유 정류장", "#00000000");

        return wrapper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 파라미터를 받아오는 부분
        Intent intent = getIntent();
        detailItemID = intent.getIntExtra("_id", 0);

        if (detailItemID == 0) {
            //에러코드 출력
            finish();
            return;
        }

        aq = new HttpJSONQuery();

        mFavoriteDb = FavoriteDB.getInstance(this);

        mDbHelper = BusDB.getInstance(this);
        final Cursor cursor = mDbHelper.selectBusRoute(detailItemID);
        cursor.moveToFirst();
        rt_num = cursor.getString(cursor.getColumnIndex("rt_num"));
        rt_country = cursor.getString(cursor.getColumnIndex("rt_country"));
        final Cursor cursora = mDbHelper.selectBusRouteAdditional(rt_country, rt_num);
        cursora.moveToFirst();
        rt_depart_time = cursora.getString(cursora.getColumnIndex("rt_depart_time"));
        rt_stop_gap = cursora.getString(cursora.getColumnIndex("rt_stop_gap"));

        mRecentDb = RecentDB.getInstance(this);
        mRecentDb.insertRecent(cursor.getString(cursor.getColumnIndex("rt_name")) + " (" + cursor.getString(cursor.getColumnIndex("rt_name_extra")) + ")", "route", Integer.parseInt(rt_country), Integer.parseInt(rt_num));

        mDetailArray = new ArrayList<>();

        //노선 종류
        RouteDetailItem item = new RouteDetailItem();
        item.setIcon(R.drawable.ic_bus_grey600_24dp);
        Cursor tCursor = mDbHelper.selectType(cursora.getString(cursora.getColumnIndex("rt_type")));
        tCursor.moveToFirst();
        item.setSummary(tCursor.getString(tCursor.getColumnIndex("ty_name")));
        mDetailArray.add(item);

        //노선 배차간격
        String interval = cursora.getString(cursora.getColumnIndex("rt_interval"));
        if (!interval.equals("")) {
            item = new RouteDetailItem();
            item.setIcon(R.drawable.ic_timer_grey600_24dp);
            item.setSummary(interval + "분 간격");
            mDetailArray.add(item);
        }

        String[] depart = cursora.getString(cursora.getColumnIndex("rt_depart_time")).split(",");
        SimpleDateFormat input = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("HH시 mm분");
        if (depart.length > 1) {
            try {
                item = new RouteDetailItem();
                item.setIcon(R.drawable.ic_clock_grey600_24dp);
                Date firstBus = input.parse(depart[0]);
                Date lastBus = input.parse(depart[depart.length - 1]);
                item.setSummary("첫차 " + output.format(firstBus)
                        + "\n막차 " + output.format(lastBus));
                mDetailArray.add(item);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (depart.length == 1) {
            try {
                item = new RouteDetailItem();
                item.setIcon(R.drawable.ic_clock_grey600_24dp);
                Date timeBus = input.parse(depart[0]);
                item.setSummary(output.format(timeBus) + "에만 운행");
                mDetailArray.add(item);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String[] lowfloor = cursora.getString(cursora.getColumnIndex("rt_lowfloor_time")).split(",");
        if (Arrays.asList(lowfloor).contains("1")) {
            item = new RouteDetailItem();
            item.setIcon(R.drawable.ic_alert_circle_grey600_24dp);
            item.setSummary("저상버스 운행");
            mDetailArray.add(item);
        }

        String[] stopList = cursor.getString(cursor.getColumnIndex("rt_stop_list")).split(",");
        mStopArray = new ArrayList<>();

        for (int i = 0; i < stopList.length; i++) {
            RouteStopItem stopItem = new RouteStopItem();
            Cursor sCursor = mDbHelper.StopBisQuery(stopList[i], cursor.getString(cursor.getColumnIndex("rt_country")));
            sCursor.moveToFirst();
            stopItem.setStopName((i + 1) + ". " + sCursor.getString(sCursor.getColumnIndex("st_name")));
            //stopGap을 위해서
            mStopList.add(sCursor.getString(sCursor.getColumnIndex("st_name")));

            stopItem.setStopArs(sCursor.getString(sCursor.getColumnIndex("st_ars")));
            stopItem.setStopId(sCursor.getInt(sCursor.getColumnIndex("_id")));
            stopItem.setStopNum(sCursor.getInt(sCursor.getColumnIndex("st_num")));
            mStopArray.add(stopItem);
        }

        AdapterWrapper wrapper = createAdapter();
        mRecyclerView.setAdapter(wrapper);

        ((TextView) findViewById(R.id.header_title)).setText(cursor.getString(cursor.getColumnIndex("rt_name")));
        ((TextView) findViewById(R.id.header_subtitle)).setText(cursor.getString(cursor.getColumnIndex("rt_name_extra")));
        Cursor cCursor = mDbHelper.selectCountry(cursor.getString(cursor.getColumnIndex("rt_country")));
        cCursor.moveToFirst();
        byte[] decodedString = Base64.decode(cCursor.getString(cCursor.getColumnIndex("ct_image")), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        ((ImageView) findViewById(R.id.header_icon)).setImageBitmap(decodedByte);

        initMap(cursor);

        final FloatingActionButton actionKakao = (FloatingActionButton) findViewById(R.id.fab_action_kakao);
        actionKakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //카카오링크 API 변경됨
                    KakaoLink kakaoLink = KakaoLink.getKakaoLink(RouteDetailActivity.this);
                    KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

                    KakaoTalkLinkMessageBuilder contents = kakaoTalkLinkMessageBuilder
                            .addText("[" + getString(R.string.app_name) + "]\n"
                                    + "'" + cursor.getString(cursor.getColumnIndex("rt_name")) + "'번 노선 정보를 지금 확인해보세요.")
                            .addAppButton("앱으로 이동",
                                    new AppActionBuilder()
                                            .addActionInfo(AppActionInfoBuilder
                                                    .createAndroidActionInfoBuilder()
                                                    .setExecuteParam("kakao_type=route&kakao_country=" + cursor.getString(cursor.getColumnIndex("rt_country")) + "&kakao_num=" + cursor.getString(cursor.getColumnIndex("rt_num")))
                                                    .setMarketParam("referrer=kakaotalklink")
                                                    .build())
                                            .addActionInfo(AppActionInfoBuilder.createiOSActionInfoBuilder()
                                                    .setExecuteParam("kakao_type=route&kakao_country=" + cursor.getString(cursor.getColumnIndex("rt_country")) + "&kakao_num=" + cursor.getString(cursor.getColumnIndex("rt_num")))
                                                    .setMarketParam("referrer=kakaotalklink")
                                                    .build())
                                            .build());
                    kakaoLink.sendMessage(contents, RouteDetailActivity.this);
                } catch (KakaoParameterException e) {
                    e.printStackTrace();
                }
            }
        });

        final FloatingActionButton actionFavorite = (FloatingActionButton) findViewById(R.id.fab_action_favorite);
        actionFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> data = new HashMap<>();
                data.put("num", rt_num);
                data.put("country", rt_country);
                final String dataString = data.toString();
                if (!mFavoriteDb
                        .isRegisterFavorite("route", dataString)) {
                    AlertDialog.Builder favorite = new AlertDialog.Builder(
                            RouteDetailActivity.this);
                    favorite.setTitle("즐겨찾기 추가");
                    favorite.setMessage("즐겨찾기에 추가될 항목의 이름을 입력해 주세요.");

                    final EditText favoriteName = new EditText(RouteDetailActivity.this);
                    favoriteName.setText(cursor.getString(cursor.getColumnIndex("rt_name")) + "번");
                    favoriteName.setSingleLine(true);
                    favorite.setView(favoriteName);
                    favorite.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mFavoriteDb.insertFavorite("route", favoriteName.getEditableText().toString(), dataString)) {
                                        Toast.makeText(getApplicationContext(), "즐겨찾기가 추가되었습니다",
                                                Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(getApplicationContext(), "즐겨찾기 추가에 실패하였습니다",
                                                Toast.LENGTH_SHORT).show();
                                }
                            });
                    favorite.setNegativeButton("취소", null);
                    favorite.show();
                } else {
                    if (mFavoriteDb.deleteFavorite("route", dataString)) {
                        Toast.makeText(getApplicationContext(), "즐겨찾기가 삭제되었습니다",
                                Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), "즐겨찾기 삭제에 실패하였습니다",
                                Toast.LENGTH_SHORT).show();
                }
            }
        });

        final FloatingActionButton actionShortcut = (FloatingActionButton) findViewById(R.id.fab_action_shortcut);
        actionShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder shortcut = new AlertDialog.Builder(
                        RouteDetailActivity.this);
                shortcut.setTitle("바로가기 추가");
                shortcut.setMessage("런처홈에 생성될 바로가기의 이름을 입력해 주세요.");

                final EditText shortcutName = new EditText(RouteDetailActivity.this);
                shortcutName.setText(cursor.getString(cursor.getColumnIndex("rt_name")) + "번 노선");
                shortcutName.setSingleLine(true);
                shortcut.setView(shortcutName);

                shortcut.setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent shortcutIntent = new Intent(
                                        getApplicationContext(),
                                        SplashActivity.class);
                                shortcutIntent
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                shortcutIntent
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                shortcutIntent.putExtra("shortcut_type", "route");
                                shortcutIntent.putExtra("shortcut_country", cursor.getString(cursor.getColumnIndex("rt_country")));
                                shortcutIntent.putExtra("shortcut_num", cursor.getString(cursor.getColumnIndex("rt_num")));

                                Intent addIntent = new Intent();
                                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                                        shortcutIntent);
                                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                                        shortcutName.getEditableText().toString());
                                addIntent.putExtra(
                                        Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                        Intent.ShortcutIconResource.fromContext(
                                                getApplicationContext(),
                                                R.mipmap.ic_launcher));
                                addIntent
                                        .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                                getApplicationContext().sendBroadcast(addIntent);
                            }
                        });
                shortcut.setNegativeButton("취소", null);
                shortcut.show();
            }
        });

        final FloatingActionButton actionReport = (FloatingActionButton) findViewById(R.id.fab_action_report);
        actionReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RouteDetailActivity.this, WebViewActivity.class);
                intent.putExtra("mode", "report");
                startActivity(intent);
            }
        });
    }

    private void initMap(Cursor c) {
        // 지도 설정
        if (mGoogleMap == null) {
            mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            mGoogleMap.clear();

            if (mGoogleMap != null) {
                mDbHelper = BusDB.getInstance(RouteDetailActivity.this);

                String[] ViaStopArray = c.getString(c.getColumnIndex("rt_stop_list"))
                        .split(",");
                LatLng beforeLatlng = null;
                LatLng currentLatlng = null;
                double latsum = 0;
                double lngsum = 0;

                for (int i = 0; i < ViaStopArray.length; i++) {
                    //지역코드 넣자
                    Cursor cursor = mDbHelper.StopBisQuery(ViaStopArray[i], c.getString(c.getColumnIndex("rt_country")));
                    cursor.moveToFirst();
                    double latitude = Double.parseDouble(cursor.getString(cursor
                            .getColumnIndex("st_lat")));
                    double longitude = Double.parseDouble(cursor.getString(cursor
                            .getColumnIndex("st_lng")));
                    latsum = latsum + latitude;
                    lngsum = lngsum + longitude;

                    currentLatlng = new LatLng(latitude, longitude);

                    if (beforeLatlng != null) {
                        mGoogleMap.addPolyline(new PolylineOptions()
                                .add(beforeLatlng, currentLatlng).geodesic(true)
                                .width(5).color(Color.parseColor("#009b77")));
                    }

                    beforeLatlng = currentLatlng;
                }

                CameraPosition INIT = new CameraPosition.Builder()
                        .target(new LatLng(latsum / ViaStopArray.length, lngsum
                                / ViaStopArray.length)).zoom(10F).bearing(0F) // orientation
                        .tilt(0F).build();
                mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(INIT));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_timetable:
                if (rt_depart_time.equals("")) {
                    Toast.makeText(RouteDetailActivity.this, "해당 노선은 시간표 기능을 지원하지 않습니다.", Toast.LENGTH_LONG).show();
                    ;
                } else {
                    intent = new Intent(this, TimeTableActivity.class);
                    intent.putExtra("rt_depart_time", rt_depart_time);
                    intent.putExtra("rt_stop_gap", rt_stop_gap);
                    intent.putExtra("stop_list", mStopList);
                    startActivity(intent);
                }
                return true;
            case R.id.action_map:
                intent = new Intent(this, LargeMapActivity.class);
                intent.putExtra("mode", "route");
                intent.putExtra("id", detailItemID);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        refreshLayout.setRefreshing(true);
        Cursor cursor = mDbHelper.selectBusRoute(detailItemID);
        cursor.moveToFirst();

        aq.ajax(getString(R.string.geojebus_api_url) + "/businfo/bis.php?platform=android&app_version=2.1.0&country=" + cursor.getString(cursor
                .getColumnIndex("rt_country")) + "&type=route&bis_code=" + cursor.getString(cursor
                .getColumnIndex("rt_num")), new HttpJSONQueryCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject json, int status) {

                List<RouteBusItem> mArray = new ArrayList<>();

                try {
                    if (json.getString("request").equals("success")) {
                        for (int i = 0; i < json.getJSONArray("data").length(); i++) {
                            JSONObject obj = json.getJSONArray("data").getJSONObject(i);
                            RouteBusItem item = new RouteBusItem();
                            item.setStopBis(obj.getInt("re_st_num"));
                            item.setVehicleNum(obj.getString("re_vc_plate").substring(2, 6));
                            mArray.add(item);
                        }

                        if (json.getJSONArray("data").length() > 0)
                            Snackbar.make(findViewById(R.id.coordinator), json.getJSONArray("data").length() + "대의 버스가 운행 중 입니다", Snackbar.LENGTH_SHORT)
                                    .show();
                        else
                            Snackbar.make(findViewById(R.id.coordinator), "운행중인 버스가 없습니다", Snackbar.LENGTH_SHORT)
                                    .show();
                    } else {
                        realTimeAdapter.sendBusData(mArray);
                        Toast.makeText(RouteDetailActivity.this, "버스정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    realTimeAdapter.sendBusData(mArray);
                    Toast.makeText(RouteDetailActivity.this, "버스정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
                }

                realTimeAdapter.sendBusData(mArray);

                refreshLayout.setRefreshing(false);
            }
        });
    }
}
