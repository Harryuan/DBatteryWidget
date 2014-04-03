package com.android.dualbattery;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;
import android.os.BatteryManager;
import android.os.Handler;//added by huiyang.tang
import android.app.ActivityManager;
import java.util.ArrayList;
import java.util.List;

//yuanhl 
//import android.content.pm.PackageManager;
  
//DBatteryWidgetprovider
public class DualBatteryWidget extends AppWidgetProvider{

	private final static String TAG= "DBatteryWidgetprovider";
	private final static boolean log = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		///super.onReceive(context, intent);
		
		if (log)Log.d(TAG,"SFProfilesWidget...on receive");
		 
		Log.d(TAG,"DualBatteryWidget onReceive");
		
		String action = intent.getAction();
	       
        Log.d(TAG,"action = "+ action);
		//返回桌面时，刷新一下，避免在Settings/APP内部被stop service。
		if(action.equals(Intent.ACTION_MAIN)){
		    
		    Log.d(TAG," onReceive start ACTION_MAIN");
		    context.startService(new Intent(context,  WidgetUpdateService.class)); 
		}
		else{
		    super.onReceive(context, intent);
		}
	}      
  
	@Override
	public void onEnabled(Context context){
		Log.d(TAG,"DualBatteryWidget onEnabled");
		//super.onEnabled(context);
		//context.registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));  
		context.startService(new Intent(context,  WidgetUpdateService.class)); 
		//如果主页上有该widget实例，说明启动了AppWidgetProvider组件，
		//则使其可以接收广播消息ACTION_BATTERY_CHANGED。
		//PackageManager pm = context.getPackageManager();
		//pm.setComponentEnabledSetting(new ComponentName("com.android.dualbattery",".DualBatteryWidget"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

	}

// 当桌面上这个widget的所有实体都被删除后，停止配套服务   
  @Override  
  public void onDisabled(Context context) {   
      
      Log.d(TAG,"DualBatteryWidget onDisabled");
      context.stopService(new Intent(context, WidgetUpdateService.class));   
      //context.unregisterReceiver(mReceiver);
   	//如果主页上删除完了widget实例，则使其禁止接收广播消息ACTION_BATTERY_CHANGED。
    //PackageManager pm = context.getPackageManager();
    //pm.setComponentEnabledSetting(new ComponentName("com.android.dualbattery",".DualBatteryWidget"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

  }   

  /*public BroadcastReceiver mReceiver = new BroadcastReceiver(){
      
      @Override  
      public void onReceive(Context context, Intent intent) {
          
          long id = Thread.currentThread().getId();
          Log.d(TAG,"------->mReceiver thread id = "+id);
          
          String action = intent.getAction();
          Log.d(TAG,"action = "+ action);
          
          context.startService(new Intent(context,  WidgetUpdateService.class)); 
          
      }
  };*/
  
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
	    Log.d(TAG, "onupdate starts");  
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.d(TAG, "on update");
		
		context.startService(new Intent(context,  WidgetUpdateService.class));   

	}
	
}
