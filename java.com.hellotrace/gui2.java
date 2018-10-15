//package com.blyang;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.Window;
//import android.widget.ImageView;
//import android.widget.TabHost.OnTabChangeListener;
//import android.widget.TabHost.TabSpec;
//import android.widget.TextView;
//
//
//public class gui2 extends Activity implements OnTabChangeListener {
//
//    private FragmentTabHost tabHost;
//    private String[] tabText = { "TRACK", "MOMENTS", "ME:)"};
//   // private int[] imageRes = new int[] { R.drawable.tab_chat, R.drawable.tab_friends, R.drawable.tab_contacts, R.drawable.tab_setting };
//    private Class[] fragments = new Class[] { GUIuse.class, moment.class, me.class};
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.gui2main);
//
//        tabHost = (FragmentTabHost) super.findViewById(R.id.tabhost);
//        tabHost.setup(this, getSupportFragmentManager(), R.id.flContainer);
//        tabHost.getTabWidget().setDividerDrawable(null);
//        tabHost.setOnTabChangedListener(this);
//        initTab();
//    }
//
//    private void initTab() {
//        for (int i = 0; i < tabText.length; i++) {
//
//            View view = LayoutInflater.from(this).inflate(R.layout.item_tab, null);
//            ((TextView) view.findViewById(R.id.tv)).setText(tabText[i]);
//           // ((ImageView) view.findViewById(R.id.iv)).setImageResource(imageRes[i]);
//
//            TabSpec tabSpec = tabHost.newTabSpec(tabText[i]).setIndicator(view);
//            tabHost.addTab(tabSpec, fragments[i], null);
//            tabHost.setTag(i);
//        }
//    }
//
//
//    //自动把getCurrentTabView下的所有子View的selected状态设为true. 牛逼!
//    @Override
//    public void onTabChanged(String tabId) {
//        //首次打开自动会调用一下  首次自动输出tabId : 聊天
//        Log.e("yao", "tabId : " + tabId);
////		TabWidget tabWidget = tabHost.getTabWidget(); //获取整个底部Tab的布局, 可以通过tabWidget.getChildCount和tabWidget.getChildAt来获取某个子View
////		int pos = tabHost.getCurrentTab(); //获取当前tab的位置
////		View view = tabHost.getCurrentTabView(); //获取当前tab的view
//    }
//
