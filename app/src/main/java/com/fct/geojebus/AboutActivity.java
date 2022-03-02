package com.fct.geojebus;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final TextView tutorial = (TextView) findViewById(R.id.tutorial_tv);
        tutorial.setText(Html.fromHtml("<a href=\"http://www.geojebus.kr/tutorial\">이용방법</a>"));
        tutorial.setMovementMethod(LinkMovementMethod.getInstance());
        final TextView tou = (TextView) findViewById(R.id.tou_tv);
        tou.setText(Html.fromHtml("<a href=\"http://www.geojebus.kr/toa\">이용약관</a>"));
        tou.setMovementMethod(LinkMovementMethod.getInstance());
        final TextView pp = (TextView) findViewById(R.id.pp_tv);
        pp.setText(Html.fromHtml("<a href=\"http://www.geojebus.kr/privacy\">개인정보 취급 방침</a>"));
        pp.setMovementMethod(LinkMovementMethod.getInstance());
        final TextView osl = (TextView) findViewById(R.id.osl_tv);
        osl.setText(Html.fromHtml("<a href=\"http://www.geojebus.kr/oslicense\">Open Source License</a>"));
        osl.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
