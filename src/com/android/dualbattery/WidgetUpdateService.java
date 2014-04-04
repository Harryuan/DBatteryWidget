package com.android.dualbattery;

import android.app.Service;   
import android.appwidget.AppWidgetManager;   
import android.content.BroadcastReceiver;   
import android.content.ComponentName;   
import android.content.Context;   
import android.content.Intent;   
import android.content.IntentFilter;   
import android.os.BatteryManager;   
import android.os.IBinder;   
import android.widget.RemoteViews;   
import android.app.PendingIntent;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
 
import android.util.Log;

  
//用以根据电池电量更新而刷新桌面Widget的服务   
public class WidgetUpdateService extends Service {   
	private static final String TAG = "WidgetUpdateService";
 
  @Override  
  public IBinder onBind(Intent intent) {   
    return null;   
  }   
	int mbgImgId = R.drawable.bg;
	int mBattery1ImgId = R.drawable.battery1;
	int mBattery2ImgId = R.drawable.battery2;
  
	int[] batt_use_icon = {R.drawable.battery_use_0, R.drawable.battery_use_15, R.drawable.battery_use_28, R.drawable.battery_use_43, 
				R.drawable.battery_use_57, R.drawable.battery_use_71, R.drawable.battery_use_85, R.drawable.battery_use_100};
	int[] batt_not_use_icon = {R.drawable.battery_aux_0, R.drawable.battery_aux_15, R.drawable.battery_aux_28, R.drawable.battery_aux_43, 
				R.drawable.battery_aux_57, R.drawable.battery_aux_71, R.drawable.battery_aux_85, R.drawable.battery_aux_100};
	int batt_in_charge = R.drawable.batt_in_charge;

	private int getBattLevelIndex(int level)
	{	
		int index = 0;
		if(level < 4)	index = 0;
		else if (level < 16) index = 1;
		else if (level < 28) index = 2;
		else if (level < 43) index = 3;
		else if (level < 57) index = 4;
		else if (level < 71) index = 5;
		else if (level < 100) index = 6;
		else if (level >= 100) index = 7;
		return index;
		
	}

	private  String getAuxBattPercentage(Intent batteryChangedIntent) {
        int level = batteryChangedIntent.getIntExtra(BatteryManager.EXTRA_AUX_BATT_LEVEL, 0);
        int scale = batteryChangedIntent.getIntExtra("scale", 100);
        return String.valueOf(level * 100 / scale) + "%";
	}
	private  String getMainBattPercentage(Intent batteryChangedIntent) {
        int level = batteryChangedIntent.getIntExtra(BatteryManager.EXTRA_MAIN_BATT_LEVEL, 0);
        int scale = batteryChangedIntent.getIntExtra("scale", 100);
        return String.valueOf(level * 100 / scale) + "%";
	} 
	private boolean flagService = false ; 
	
	//added huiyang.tang
	public class BatteryBean {
		private int current_batt = -1; 
		private boolean battAuxLevelFlag = false;
		private boolean battMainLevelFlag = false;
		private String auxBattPer = "";
		private String mainBattPer = "";
		
		private int battAuxLevel;
		private int battMainLevel;
		private int status;
		private int battAuxIndex;
		private int battMainIndex;
		private int battAuxVoltage;
		private int battMainVoltage;

		private int battAuxVoltages;
		private int battMainVoltages;
	}
	
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        
        Log.d(TAG, "onStartCommand");
        long id = Thread.currentThread().getId();
        Log.d(TAG,"------->onStartCommand tid="+id);
        
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        
        new ServiceThread().start();
        
        return START_STICKY;
    }    

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        unregisterReceiver(mReceiver);
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
  
  public class ServiceThread extends Thread {  
      public void run() {
    	  while(true){
    	        long id = Thread.currentThread().getId();
    	        Log.d(TAG,"------->Thread thread id = "+id);
    	    	if(flagService){
                    Log.d(TAG,"ServiceThread flashWidget");    	    	    
    	        	flashWidget(mBatteryBean);
    	        	flagService = false; 
    	            Log.d(TAG,"ServiceThread flagService=true");
    	        	break ;
    	    	}
    	    	try{
    	    		this.sleep(500);
    	    	}catch(Exception e){
    	    		e.printStackTrace();
    	    	}
    	    	 Log.d(TAG,"ServiceThread returned");
    	    }
      }  
  }  
  

/*
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
	// TODO Auto-generated method stub		
	super.onStartCommand(intent, flags, startId);
	 return START_REDELIVER_INTENT;
}	
*/ 
  
 
  
	public boolean ifChangeData(BatteryBean mBatteryBean,BatteryBean mOldBatteryBean) {
		if (null == mOldBatteryBean) {
			return false;
		}

		return (mOldBatteryBean.current_batt == mBatteryBean.current_batt)
				&& (mOldBatteryBean.battAuxIndex == mBatteryBean.battAuxIndex)
				&& (mOldBatteryBean.battMainIndex == mBatteryBean.battMainIndex)
				&& (mOldBatteryBean.status == mBatteryBean.status)
				&& (mOldBatteryBean.battAuxLevelFlag == mBatteryBean.battAuxLevelFlag)
				&& (mOldBatteryBean.battMainLevelFlag == mBatteryBean.battMainLevelFlag)
				&& (mOldBatteryBean.auxBattPer.equals(mBatteryBean.auxBattPer))
				&& (mOldBatteryBean.mainBattPer.equals(mBatteryBean.mainBattPer));
	}
 
  private Context mContext = null; 
  private BatteryBean mOldBatteryBean = null;
  private BatteryBean mBatteryBean = null;
  private PendingIntent pIntent = null ;
  
  private BroadcastReceiver mReceiver = new BroadcastReceiver() {   
      @Override  
      public void onReceive(Context context, Intent intent) { 
    	    mContext = context;
			Log.d(TAG,"WidgetUpdateService onReceive");
			
			if(null == mBatteryBean ){
				mBatteryBean = new BatteryBean();
			}
			if(null == mOldBatteryBean ){
				mOldBatteryBean = new BatteryBean();
			}
			
			mBatteryBean.battAuxLevel = intent.getIntExtra(BatteryManager.EXTRA_AUX_BATT_LEVEL, 0);
			mBatteryBean.battMainLevel = intent.getIntExtra(BatteryManager.EXTRA_MAIN_BATT_LEVEL, 0);
			mBatteryBean.status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
	                    BatteryManager.BATTERY_STATUS_UNKNOWN);
			mBatteryBean.battAuxIndex = getBattLevelIndex(mBatteryBean.battAuxLevel);
			mBatteryBean.battMainIndex = getBattLevelIndex(mBatteryBean.battMainLevel); 
	   
			mBatteryBean.battAuxVoltage = intent.getIntExtra(BatteryManager.EXTRA_AUX_BATT_VOLTAGE, 0);
			mBatteryBean.battMainVoltage = intent.getIntExtra(BatteryManager.EXTRA_MAIN_BATT_VOLTAGE, 0);
	
			if((mBatteryBean.battMainLevel == 0)&&(mBatteryBean.battMainVoltage>3000)){ 
				mBatteryBean.battMainVoltage=Integer.parseInt(getChargerInfo("cat /sys/class/power_supply/battery/mbatt_InstatVolt"));
			}
			if((mBatteryBean.battAuxLevel == 0)&&(mBatteryBean.battAuxVoltage>3000)){ 
				mBatteryBean.battAuxVoltage=Integer.parseInt(getChargerInfo("cat /sys/class/power_supply/battery/abatt_InstatVolt")); 
			}
	
			mBatteryBean.current_batt = intent.getIntExtra(BatteryManager.EXTRA_CURRENT_BATT, 0);
	 
		  	 
		  	if (mBatteryBean.battMainLevel == 0 && mBatteryBean.battMainVoltage < 3000){
		  		mBatteryBean.battMainLevelFlag=true;
		  	}else{
		  		mBatteryBean.battMainLevelFlag=false;
		  	}
		  	 
		  	if (mBatteryBean.battAuxLevel == 0 && mBatteryBean.battAuxVoltage < 3000){
		  		mBatteryBean.battAuxLevelFlag=true;
		  	}else{
		  		mBatteryBean.battAuxLevelFlag=false;
		  	}
		  	
		  	mBatteryBean.auxBattPer = getAuxBattPercentage(intent);
		  	mBatteryBean.mainBattPer = getMainBattPercentage(intent); 

		  	boolean changeFlag = ifChangeData(mBatteryBean,mOldBatteryBean);
		  	dataBackup(mBatteryBean,mOldBatteryBean);
		  	
		  	Log.d(TAG,"changeFlag="+changeFlag);

    	    flagService = true;
		  	if(changeFlag){
					return ; 
		  	}
            Log.d(TAG,"mReceiver flashWidget"); 
		  	flashWidget(mBatteryBean); 
      }   
    };   

    
    public void dataBackup(BatteryBean mBatteryBean,BatteryBean mOldBatteryBean){
    	mOldBatteryBean.current_batt = mBatteryBean.current_batt;
		mOldBatteryBean.battAuxIndex = mBatteryBean.battAuxIndex;
		mOldBatteryBean.battMainIndex = mBatteryBean.battMainIndex;
		mOldBatteryBean.status = mBatteryBean.status;
		mOldBatteryBean.battAuxLevelFlag = mBatteryBean.battAuxLevelFlag;
		mOldBatteryBean.battMainLevelFlag = mBatteryBean.battMainLevelFlag;
		mOldBatteryBean.auxBattPer=mBatteryBean.auxBattPer;
		mOldBatteryBean.mainBattPer=mBatteryBean.mainBattPer;
    }
    
    public void flashWidget(BatteryBean mBatteryBean){

		if(null == pIntent){ 
			Intent powerUsageIntent = new Intent();
			powerUsageIntent.setAction(Intent.ACTION_POWER_USAGE_SUMMARY);
			pIntent = PendingIntent.getActivity(mContext, 0, powerUsageIntent, 0);
		}
    	
    	AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext); 
		RemoteViews rViews = new RemoteViews(mContext.getPackageName(),R.layout.dual_battery_widget);   
		rViews.setOnClickPendingIntent(R.id.dual_battery_widget, pIntent);
		rViews.setImageViewResource(R.id.bg_icon, mbgImgId);

	
		if (mBatteryBean.current_batt == 0)// use the main battery
		{
			if (mBatteryBean.battAuxLevel == 0 && mBatteryBean.battAuxVoltage < 3000) {
				rViews.setViewVisibility(R.id.aux_battery_icon, View.GONE);
				rViews.setTextViewText(R.id.aux_battery_percent, null);
			} else {
				rViews.setViewVisibility(R.id.aux_battery_icon,View.VISIBLE);
				rViews.setImageViewResource(R.id.aux_battery_icon,batt_not_use_icon[mBatteryBean.battAuxIndex]);
				rViews.setViewVisibility(R.id.aux_charge_icon, View.GONE);

				rViews.setTextViewText(R.id.aux_battery_percent, mBatteryBean.auxBattPer);
			}

			rViews.setViewVisibility(R.id.main_battery_icon, View.VISIBLE);
			rViews.setImageViewResource(R.id.main_battery_icon,
					batt_use_icon[mBatteryBean.battMainIndex]);
			// 若正在充电
			if (mBatteryBean.status == BatteryManager.BATTERY_STATUS_CHARGING) {
				rViews.setImageViewResource(R.id.main_charge_icon,
						batt_in_charge);
				rViews.setViewVisibility(R.id.main_charge_icon,
						View.VISIBLE);
				rViews.setViewVisibility(R.id.aux_charge_icon, View.GONE);
			} else {
				rViews.setViewVisibility(R.id.main_charge_icon, View.GONE);
			}
			rViews.setTextViewText(R.id.main_battery_percent, mBatteryBean.mainBattPer);
		} else// use the aux battery
		{
			rViews.setViewVisibility(R.id.aux_battery_icon, View.VISIBLE);
			rViews.setImageViewResource(R.id.aux_battery_icon,
					batt_use_icon[mBatteryBean.battAuxIndex]);
			// 若正在充电
			if (mBatteryBean.status == BatteryManager.BATTERY_STATUS_CHARGING) {
				rViews.setImageViewResource(R.id.aux_charge_icon,
						batt_in_charge);
				rViews.setViewVisibility(R.id.aux_charge_icon, View.VISIBLE);
				rViews.setViewVisibility(R.id.main_charge_icon, View.GONE);
			} else {
				rViews.setViewVisibility(R.id.aux_charge_icon, View.GONE);
			}
			rViews.setTextViewText(R.id.aux_battery_percent, mBatteryBean.auxBattPer);

			if (mBatteryBean.battMainLevel == 0 && mBatteryBean.battMainVoltage < 3000) {
				rViews.setViewVisibility(R.id.main_charge_icon, View.GONE);
				rViews.setViewVisibility(R.id.main_battery_icon, View.GONE);
				rViews.setTextViewText(R.id.main_battery_percent, null);
			} else {
				rViews.setViewVisibility(R.id.main_battery_icon,
						View.VISIBLE);
				rViews.setImageViewResource(R.id.main_battery_icon,
						batt_not_use_icon[mBatteryBean.battMainIndex]);
				rViews.setViewVisibility(R.id.main_charge_icon, View.GONE);
				rViews.setTextViewText(R.id.main_battery_percent,
						mBatteryBean.mainBattPer);
			}
		}
		
		if(null == mComponentName){
			mComponentName = new ComponentName(mContext,DualBatteryWidget.class);
		}

		widgetManager.updateAppWidget(mComponentName, rViews);   
    }
       
    private ComponentName mComponentName = null ;

 public static String ERROR = "ERROR";
 private static StringBuilder sb = new StringBuilder("");
 private static boolean debugflag = true;
 /**
  * get the message after execute
  * 
  * @return
  */
 public static String getOutput() {
  return sb.toString();
 }
 

/**
  * help to execute the command in shell
  * 
  * @param commamd
  * @return 0 is ture and -1 is fail
  * @throws IOException
  */
	public int execCommand(String[] command) throws IOException {
		Log.v(TAG, command[0] + command[1] + command[2]); // return2

		Runtime runtime = Runtime.getRuntime();
		Process proc  = runtime.exec(command);
		 
		InputStream inputstream = proc.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader); 

		sb.delete(0, sb.length());
		try {
			if (proc.waitFor() != 0) {
				Log.i(TAG, "exit value = " + proc.exitValue());
				sb.append(ERROR);
				return -1;
			} else {
				String line;
				line = bufferedreader.readLine();
				if (line != null) {
					sb.append(line);
				} else {
					return 0;
				}
				while (true) {
					line = bufferedreader.readLine();
					if (line == null) {
						break;
					} else {
						sb.append('\n');
						sb.append(line);
					}
				}
				return 0;
			}
		} catch (InterruptedException e) {
			Log.i(TAG, "exe fail " + e.toString());
			sb.append(ERROR);
			return -1;
		}
	}

	private String getChargerInfo(String location) {
		int ret;
		String[] cmd = { "/system/bin/sh", "-c", location };

		try {
			ret = execCommand(cmd);
			if (ret != 0) {
				Log.i(TAG, "exec  error");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "error";

		}
		return getOutput();

	}


} 


