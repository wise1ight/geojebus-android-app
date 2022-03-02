package com.fct.geojebus;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import com.fct.geojebus.adapter.PassThroughRouteAdapter;
import com.fct.geojebus.adapter.StopArriveAdapter;
import com.fct.geojebus.database.BusDB;
import com.fct.geojebus.database.FavoriteDB;
import com.fct.geojebus.database.RecentDB;
import com.fct.geojebus.model.PassThroughRoute;
import com.fct.geojebus.model.StopArrive;
import com.fct.geojebus.ui.AdapterWrapper;
import com.fct.geojebus.ui.DividerAdapter;
import com.fct.geojebus.ui.HeaderViewAdapter;
import com.fct.geojebus.util.HttpJSONQuery;
import com.fct.geojebus.util.HttpJSONQueryCallback;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.AppActionInfoBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class StopDetailActivity extends DetailBaseActivity {
    ArrayList<StopArrive> lcs = new ArrayList<>();
    StopArriveAdapter realTimeAdapter;
    private HttpJSONQuery aq;
    private BusDB mDbHelper;
    private FavoriteDB mFavoriteDb;
    private RecentDB mRecentDb;
    private int detailItemID;
    private HashMap<String, String> dataRow = new HashMap<>();

    AdapterWrapper createAdapter() {
        AdapterWrapper wrapper = new AdapterWrapper();

        HeaderViewAdapter headerViewAdapter = new HeaderViewAdapter();
        View headerView = new View(StopDetailActivity.this);
        headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mFlexibleSpaceImageHeight));
        headerView.setMinimumHeight(mFlexibleSpaceImageHeight);
        headerView.setClickable(true);
        headerViewAdapter.addHeaderView(0, headerView);
        wrapper.wrapAdapter(headerViewAdapter);

        realTimeAdapter = new StopArriveAdapter(this, lcs);
        wrapper.wrapAdapter(realTimeAdapter)
                .sectionHeader("실시간 도착 정보", "#e0e0e0");

        DividerAdapter dividerAdapter = new DividerAdapter();
        wrapper.wrapAdapter(dividerAdapter);

        // 경유 노선 출력
        mDbHelper = BusDB.getInstance(this);
        Cursor cursor = mDbHelper.queryViaRouteList(dataRow.get("st_num"), dataRow.get("st_country"));
        cursor.moveToFirst();

        ArrayList<PassThroughRoute> ViaRoute = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            PassThroughRoute item = new PassThroughRoute();
            item.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            item.setName(cursor.getString(cursor.getColumnIndex("rt_name")));
            item.setExtra(cursor.getString(cursor.getColumnIndex("rt_name_extra")));
            Cursor cursora = mDbHelper.selectBusRouteAdditional(cursor.getString(cursor.getColumnIndex("rt_country")), cursor.getString(cursor.getColumnIndex("rt_num")));
            cursora.moveToFirst();
            item.setType(cursora.getString(cursora.getColumnIndex("rt_type")));
            ViaRoute.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        PassThroughRouteAdapter passRouteAdapter = new PassThroughRouteAdapter(this, ViaRoute);
        wrapper.wrapAdapter(passRouteAdapter)
                .sectionHeader("경유 노선", "#00000000");

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
        Cursor cursor = mDbHelper.selectBusStop(detailItemID);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            dataRow.put(cursor.getColumnName(i), cursor.getString(i));
        }
        cursor.close();

        mRecentDb = RecentDB.getInstance(this);
        mRecentDb.insertRecent(dataRow.get("st_name"), "stop", Integer.parseInt(dataRow.get("st_country")), Integer.parseInt(dataRow.get("st_num")));

        AdapterWrapper wrapper = createAdapter();
        mRecyclerView.setAdapter(wrapper);

        ((TextView) findViewById(R.id.header_title)).setText(dataRow.get("st_name"));
        ((TextView) findViewById(R.id.header_subtitle)).setText(dataRow.get("st_ars"));
        Cursor cCursor = mDbHelper.selectCountry(dataRow.get("st_country"));
        cCursor.moveToFirst();
        byte[] decodedString = Base64.decode(cCursor.getString(cCursor.getColumnIndex("ct_image")), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        ((ImageView) findViewById(R.id.header_icon)).setImageBitmap(decodedByte);
        cCursor.close();

        // 지도 설정
        if (mGoogleMap == null) {
            mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            mGoogleMap.clear();

            if (mGoogleMap != null) {
                final LatLng StopLatLng = new LatLng(Double.parseDouble(dataRow.get("st_lat")),
                        Double.parseDouble(dataRow.get("st_lng")));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(StopLatLng, 17);
                MarkerOptions marker = new MarkerOptions().position(StopLatLng);

                mGoogleMap.moveCamera(cameraUpdate);
                mGoogleMap.animateCamera(cameraUpdate);
                mGoogleMap.addMarker(marker);
                mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
            }
        }

        final FloatingActionButton actionKakao = (FloatingActionButton) findViewById(R.id.fab_action_kakao);
        actionKakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    KakaoLink kakaoLink = KakaoLink.getKakaoLink(StopDetailActivity.this);
                    KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

                    KakaoTalkLinkMessageBuilder contents = kakaoTalkLinkMessageBuilder
                            .addText("[" + getString(R.string.app_name) + "]\n"
                                    + "'" + dataRow.get("st_name") + "' 정류장 정보를 지금 확인해보세요.")
                            .addAppButton("앱으로 이동",
                                    new AppActionBuilder()
                                            .addActionInfo(AppActionInfoBuilder
                                                    .createAndroidActionInfoBuilder()
                                                    .setExecuteParam("kakao_type=stop&kakao_country=" + dataRow.get("st_country") + "&kakao_num=" + dataRow.get("st_num"))
                                                    .setMarketParam("referrer=kakaotalklink")
                                                    .build())
                                            .addActionInfo(AppActionInfoBuilder.createiOSActionInfoBuilder()
                                                    .setExecuteParam("kakao_type=stop&kakao_country=" + dataRow.get("st_country") + "&kakao_num=" + dataRow.get("st_num"))
                                                    .setMarketParam("referrer=kakaotalklink")
                                                    .build())
                                            .build());
                    kakaoLink.sendMessage(contents, StopDetailActivity.this);
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
                data.put("num", dataRow.get("st_num"));
                data.put("country", dataRow.get("st_country"));
                final String dataString = data.toString();
                if (!mFavoriteDb
                        .isRegisterFavorite("stop", dataString)) {
                    AlertDialog.Builder favorite = new AlertDialog.Builder(
                            StopDetailActivity.this);
                    favorite.setTitle("즐겨찾기 추가");
                    favorite.setMessage("즐겨찾기에 추가될 항목의 이름을 입력해 주세요.");

                    final EditText favoriteName = new EditText(StopDetailActivity.this);
                    favoriteName.setText(dataRow.get("st_name"));
                    favoriteName.setSingleLine(true);
                    favorite.setView(favoriteName);
                    favorite.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mFavoriteDb.insertFavorite("stop", favoriteName.getEditableText().toString(), dataString)) {
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
                    if (mFavoriteDb.deleteFavorite("stop", dataString)) {
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
                        StopDetailActivity.this);
                shortcut.setTitle("바로가기 추가");
                shortcut.setMessage("런처홈에 생성될 바로가기의 이름을 입력해 주세요.");

                final EditText shortcutName = new EditText(StopDetailActivity.this);
                shortcutName.setText(dataRow.get("st_name") + " 정류장");
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
                                shortcutIntent.putExtra("shortcut_type", "stop");
                                shortcutIntent.putExtra("shortcut_country", dataRow.get("st_country"));
                                shortcutIntent.putExtra("shortcut_num", dataRow.get("st_num"));

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
                Intent intent = new Intent(StopDetailActivity.this, WebViewActivity.class);
                intent.putExtra("mode", "report");
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stop_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_roadview:
                intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("mode", "roadview");
                intent.putExtra("id", detailItemID);
                startActivity(intent);
                return true;
            case R.id.action_map:
                intent = new Intent(this, LargeMapActivity.class);
                intent.putExtra("mode", "stop");
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

        //버스정보 country에따라 파싱
        aq.ajax(getString(R.string.geojebus_api_url) + "/businfo/bis.php?platform=android&app_version=2.1.0&country=" + dataRow.get("st_country") + "&type=stop&bis_code=" + dataRow.get("st_num"), new HttpJSONQueryCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject json, int status) {

                try {
                    if (json.getString("request").equals("success")) {
                        lcs.clear();

                        for (int i = 0; i < json.getJSONArray("data").length(); i++) {
                            JSONObject obj = json.getJSONArray("data").getJSONObject(i);
                            StopArrive cse = new StopArrive();
                            cse.setRouteCountry(obj.getString("route_country"));
                            cse.setRouteName(obj.getString("route_name"));
                            cse.setWaitTime(obj.getInt("wait_time"));
                            cse.setPosition(obj.getString("position"));
                            cse.setPositionNum(obj.getInt("position_num"));
                            cse.setVehicleNum(obj.getString("vehicle_num"));
                            cse.setRouteBis(obj.getString("route_bis"));
                            lcs.add(cse);
                        }

                        Snackbar.make(findViewById(R.id.coordinator), "버스 도착 정보가 갱신되었습니다", Snackbar.LENGTH_SHORT)
                                .show();
                        realTimeAdapter.notifyDataSetChanged();
                    } else {
                        lcs.clear();
                        realTimeAdapter.notifyDataSetChanged();
                        Toast.makeText(StopDetailActivity.this, "버스정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    lcs.clear();
                    realTimeAdapter.notifyDataSetChanged();
                    Toast.makeText(StopDetailActivity.this, "버스정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
                }

                refreshLayout.setRefreshing(false);
            }
        });
    }
}
