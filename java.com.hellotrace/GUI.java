package com.blyang;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class GUI extends Activity implements OnClickListener {

    private List<View> views = new ArrayList<View>();
    private ViewPager viewPager;
    private LinearLayout llFriends, llContacts, llSettings;
    private TextView tvFriends, tvContacts, tvSettings, tvCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gui);

        initView();

        initData();
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);


        llFriends = (LinearLayout) findViewById(R.id.llFriends);
        llContacts = (LinearLayout) findViewById(R.id.llContacts);
        llSettings = (LinearLayout) findViewById(R.id.llSettings);


        llFriends.setOnClickListener(this);
        llContacts.setOnClickListener(this);
        llSettings.setOnClickListener(this);



        tvFriends = (TextView) findViewById(R.id.tvFriends);
        tvContacts = (TextView) findViewById(R.id.tvContacts);
        tvSettings = (TextView) findViewById(R.id.tvSettings);


        tvFriends.setSelected(true);
        tvCurrent = tvFriends;

        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                changeTab(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    private void initData() {
        LayoutInflater mInflater = LayoutInflater.from(this);

        View tab01 = mInflater.inflate(R.layout.activity_guiuse, null);
        View tab02 = mInflater.inflate(R.layout.activity_moment2, null);
        View tab03 = mInflater.inflate(R.layout.activity_me, null);
        views.add(tab01);
        views.add(tab02);
        views.add(tab03);


        BottomAdapter adapter;
        adapter = new BottomAdapter(views);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        changeTab(v.getId());
    }

    private void changeTab(int id) {

     //   tvCurrent.setSelected(false);
        switch (id) {
            case R.id.llFriends:
                viewPager.setCurrentItem(0);
                Intent intent = new Intent(GUI.this,GUIuse.class);
                startActivity(intent);
            case 0:

                tvFriends.setSelected(true);
                tvCurrent = tvFriends;

                break;
            case R.id.llContacts:
                viewPager.setCurrentItem(1);
            case 1:

                tvContacts.setSelected(true);
                tvCurrent = tvContacts;
                Intent intent2 = new Intent(GUI.this,moment2.class);
                startActivity(intent2);
                break;
            case R.id.llSettings:
                viewPager.setCurrentItem(2);
            case 2:

                tvSettings.setSelected(true);
                tvCurrent = tvSettings;
                Intent intent3 = new Intent(GUI.this,me.class);
                startActivity(intent3);
                break;
            default:
                break;
        }
    }

}
