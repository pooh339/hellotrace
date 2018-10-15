package com.blyang;

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
	
	
    int gatherInterval = 3;  //λ�òɼ����� (s)
    int packInterval = 10;  //������� (s)
    String entityName = null;  // entity��ʶ
    long serviceId = 204976;// ӥ�۷���ID
    int traceType = 2;  //�켣��������
    private static OnStartTraceListener startTraceListener = null;  //�����켣���������
    
	private static MapView mapView = null;
	private static BaiduMap baiduMap = null;
	private static OnEntityListener entityListener = null;
	private RefreshThread refreshThread = null;  //ˢ�µ�ͼ�߳��Ի�ȡʵʱ��
	private static MapStatusUpdate msUpdate = null;
	private static BitmapDescriptor realtimeBitmap;  //ͼ��
	private static OverlayOptions overlay;  //������
	private static List<LatLng> pointList = new ArrayList<LatLng>();  //��λ��ļ���
	private static PolylineOptions polyline = null;  //·�߸�����
	
	
    private Trace trace;  // ʵ�����켣����
    private LBSTraceClient client;  // ʵ�����켣����ͻ���

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SDKInitializer.initialize(getApplicationContext());
		
		setContentView(R.layout.activity_main);
		
		init();
		
		initOnEntityListener();
		
		initOnStartTraceListener();
		
		
		
		client.startTrace(trace, startTraceListener);  // �����켣����
	}

	/**
	 * ��ʼ����������
	 */
	 private void init() {
		 
		 mapView = (MapView) findViewById(R.id.mapView);
		 baiduMap = mapView.getMap();
		 mapView.showZoomControls(false);
		 
		 entityName = getImei(getApplicationContext());  //�ֻ�Imeiֵ�Ļ�ȡ�������䵱ʵ����
		 
         client = new LBSTraceClient(getApplicationContext());  //ʵ�����켣����ͻ���
         
         trace = new Trace(getApplicationContext(), serviceId, entityName, traceType);  //ʵ�����켣����
         
         client.setInterval(gatherInterval, packInterval);  //����λ�òɼ��ʹ������
	 }
	 
	 
	 /**
	  * ��ʼ������ʵ��״̬������
	  */
	 private void initOnEntityListener(){
		 
		 //ʵ��״̬������
		 entityListener = new OnEntityListener(){

			@Override
			public void onRequestFailedCallback(String arg0) {
				Looper.prepare();
				Toast.makeText(
						getApplicationContext(), 
						"entity����ʧ�ܵĻص��ӿ���Ϣ��"+arg0, 
						Toast.LENGTH_SHORT)
						.show();
				Looper.loop();
			}
			
			@Override
			public void onQueryEntityListCallback(String arg0) {
				/**
				 * ��ѯʵ�弯�ϻص���������ʱ����ʵʱ�켣����
				 */
				showRealtimeTrack(arg0);  
			}
			
		 };
	 }
	 
	 
	 
	/** ׷�ٿ�ʼ */
	private void initOnStartTraceListener() {
		
		// ʵ���������켣����ص��ӿ�
		startTraceListener = new OnStartTraceListener() {
			// �����켣����ص��ӿڣ�arg0 : ��Ϣ���룬arg1 : ��Ϣ���ݣ�����鿴��ο���
			@Override
			public void onTraceCallback(int arg0, String arg1) {
				Log.i("TAG", "onTraceCallback=" + arg1);
				if(arg0 == 0 || arg0 == 10006){
					startRefreshThread(true);
				}
			}

			// �켣�������ͽӿڣ����ڽ��շ����������Ϣ��arg0 : ��Ϣ���ͣ�arg1 : ��Ϣ���ݣ�����鿴��ο���
			@Override
			public void onTracePushCallback(byte arg0, String arg1) {
				Log.i("TAG", "onTracePushCallback=" + arg1);
			}
		};
		
		

	}
	
	
	/**
	 * �켣ˢ���߳�
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
					System.out.println("�߳�����ʧ��");
				}
			}
			
		}
	}
	 
	/**
	 * ��ѯʵʱ��·
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
	 * չʾʵʱ��·ͼ
	 * @param realtimeTrack
	 */
	protected void showRealtimeTrack(String realtimeTrack){
		
		if(refreshThread == null || !refreshThread.refresh){
			return;
		}
		
		//������JSON��ʽ��ȡ
		RealtimeTrackData realtimeTrackData = GsonService.parseJson(realtimeTrack, RealtimeTrackData.class);
		
		if(realtimeTrackData != null && realtimeTrackData.getStatus() ==0){
			
			LatLng latLng = realtimeTrackData.getRealtimePoint();
			
			if(latLng != null){
				pointList.add(latLng);
				drawRealtimePoint(latLng);
			}
			else{
				Toast.makeText(getApplicationContext(), "��ǰ�޹켣��", Toast.LENGTH_LONG).show();
			}
			
		}
		
	}
	
	/**
	 * ����ʵʱ��·��
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
	 * ����ˢ���߳�
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
	 * ��ȡ�ֻ���Imei�룬��Ϊʵ�����ı��ֵ
	 * @param context
	 * @return
	 */
	
	private String getImei(Context context){
		String mImei = "NULL";
        try {
            mImei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            System.out.println("��ȡIMEI��ʧ��");
            mImei = "NULL";
        }
        return mImei;
	}
	
}


