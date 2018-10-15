package com.hellotrace;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.Trace;
import com.blyang.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	
    int gatherInterval = 3;  
    int packInterval = 10;  
    String entityName = null;  
    long serviceId = 204976;// 鹰眼服务ID
    int traceType = 2;  
    private static OnStartTraceListener startTraceListener = null;  //开启轨迹服务监听器
    
	private static MapView mapView = null;
	private static BaiduMap baiduMap = null;
	private static OnEntityListener entityListener = null;
	private RefreshThread refreshThread = null;  
	private static MapStatusUpdate msUpdate = null;
	private static BitmapDescriptor realtimeBitmap;  
	private static OverlayOptions overlay; 
	private static List<LatLng> pointList = new ArrayList<LatLng>();  
	private static PolylineOptions polyline = null;  
	
	
    private Trace trace;  // 实例化轨迹服务
    private LBSTraceClient client;  // 实例化轨迹服务客户端

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SDKInitializer.initialize(getApplicationContext());
		
		setContentView(R.layout.activity_main);
		
		init();
		
		initOnEntityListener();
		
		initOnStartTraceListener();
		
		
		
		client.startTrace(trace, startTraceListener);  // 开启轨迹服务
	}

	/**
	 * 初始化各个参数
	 */
	 private void init() {
		 
		 mapView = (MapView) findViewById(R.id.mapView);
		 baiduMap = mapView.getMap();
		 mapView.showZoomControls(false);
		 
		 entityName = getImei(getApplicationContext());  //手机Imei值的获取，用来充当实体名
		 
         client = new LBSTraceClient(getApplicationContext());  //实例化轨迹服务客户端
         
         trace = new Trace(getApplicationContext(), serviceId, entityName, traceType);  //实例化轨迹服务
         
         client.setInterval(gatherInterval, packInterval);  //设置位置采集和打包周期
	 }
	 
	 
	 /**
	  * 初始化设置实体状态监听器
	  */
	 private void initOnEntityListener(){
		 
		 //实体状态监听器
		 entityListener = new OnEntityListener(){

			@Override
			public void onRequestFailedCallback(String arg0) {
				Looper.prepare();
				Toast.makeText(
						getApplicationContext(), 
						"entity请求失败的回调接口信息："+arg0, 
						Toast.LENGTH_SHORT)
						.show();
				Looper.loop();
			}
			
			@Override
			public void onQueryEntityListCallback(String arg0) {
				/**
				 * 查询实体集合回调函数，此时调用实时轨迹方法
				 */
				showRealtimeTrack(arg0);  
			}
			
		 };
	 }
	 
	 
	 
	/** 追踪开始 */
	private void initOnStartTraceListener() {
		
		// 实例化开启轨迹服务回调接口
		startTraceListener = new OnStartTraceListener() {
			// 开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
			@Override
			public void onTraceCallback(int arg0, String arg1) {
				Log.i("TAG", "onTraceCallback=" + arg1);
				if(arg0 == 0 || arg0 == 10006){
					startRefreshThread(true);
				}
			}

			// 轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
			@Override
			public void onTracePushCallback(byte arg0, String arg1) {
				Log.i("TAG", "onTracePushCallback=" + arg1);
			}
		};
		
		

	}
	
	
	/**
	 * 轨迹刷新线程
	 * @author BLYang
	 */
	private class RefreshThread extends Thread{
		 
		protected boolean refresh = true;  
		
		public void run(){
			
			while(refresh){
				queryRealtimeTrack();
				try{
					Thread.sleep(packInterval * 1000);
				}catch(InterruptedException e){
					System.out.println("线程休眠失败");
				}
			}
			
		}
	}
	 
	/**
	 * 查询实时线路
	 */
	private void queryRealtimeTrack(){
		
		String entityName = this.entityName;
		String columnKey = "";
		int returnType = 0;
		int activeTime = 0;
		int pageSize = 10;
		int pageIndex = 1;
		
		this.client.queryEntityList(
				serviceId, 
				entityName, 
				columnKey, 
				returnType,
				activeTime, 
				pageSize, 
				pageIndex, 
				entityListener
				);
		
	}
	
	
	/**
	 * 展示实时线路图
	 * @param realtimeTrack
	 */
	protected void showRealtimeTrack(String realtimeTrack){
		
		if(refreshThread == null || !refreshThread.refresh){
			return;
		}
		
		//数据以JSON形式存取
		RealtimeTrackData realtimeTrackData = GsonService.parseJson(realtimeTrack, RealtimeTrackData.class);
		
		if(realtimeTrackData != null && realtimeTrackData.getStatus() ==0){
			
			LatLng latLng = realtimeTrackData.getRealtimePoint();
			
			if(latLng != null){
				pointList.add(latLng);
				drawRealtimePoint(latLng);
			}
			else{
				Toast.makeText(getApplicationContext(), "当前无轨迹点", Toast.LENGTH_LONG).show();
			}
			
		}
		
	}
	
	/**
	 * 画出实时线路点
	 * @param point
	 */
	private void drawRealtimePoint(LatLng point){
		
		baiduMap.clear();
		MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(18).build();
		msUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
		realtimeBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
		overlay = new MarkerOptions().position(point)
				.icon(realtimeBitmap).zIndex(9).draggable(true);
		
		if(pointList.size() >= 2  && pointList.size() <= 1000){
			polyline = new PolylineOptions().width(10).color(Color.RED).points(pointList);
		}
		
		addMarker();
		
	}
	
	
	private void addMarker(){
		
		if(msUpdate != null){
			baiduMap.setMapStatus(msUpdate);
		}
		
		if(polyline != null){
			baiduMap.addOverlay(polyline);
		}
		
		if(overlay != null){
			baiduMap.addOverlay(overlay);
		}
		
		
	}
	
	
	/**
	 * 启动刷新线程
	 * @param isStart
	 */
	private void startRefreshThread(boolean isStart){
		
		if(refreshThread == null){
			refreshThread = new RefreshThread();
		}
		
		refreshThread.refresh = isStart;
		
		if(isStart){
			if(!refreshThread.isAlive()){
				refreshThread.start();
			}
		}
		else{
			refreshThread = null;
		}
		
		
	}
	
	
	/**
	 * 获取手机的Imei码，作为实体对象的标记值
	 * @param context
	 * @return
	 */
	
	private String getImei(Context context){
		String mImei = "NULL";
        try {
            mImei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            System.out.println("获取IMEI码失败");
            mImei = "NULL";
        }
        return mImei;
	}
	
}


