/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-9 上午10:43:07
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.utils
 */
package com.gocontrol.doorbell.utils;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.TimerTask;

import com.gocontrol.doorbell.Door;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.ODPFeature;
import com.gocontrol.doorbell.bean.ODPTimer;
import com.gocontrol.doorbell.bean.ReceivedC2CEvent;
import com.gocontrol.doorbell.bean.QuietTimeSet.QuietTime;
import com.gocontrol.doorbell.message.DataConversion;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.RequestMessageType;
import com.gocontrol.doorbell.model.ODPFeatureManager;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.model.ODPTimerMannager;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.iptnet.c2c.C2CHandle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author Administrator
 *
 */
public class Utils {

	public static String ODP_System_Pwd;
	
	/////////////////////////////
	
	public static int[] readAudioConfig(Context context)
	{
		SharedPreferences prefs = context.getSharedPreferences("c2c_tecom.reg", Context.MODE_PRIVATE);
		int[] params = new int[1];		
		params[0] = prefs.getInt("sys_mic_vol", 3); //
		return params;
	}
	
	public static boolean saveAudioConfig(Context context, int micVol)
	{
		SharedPreferences prefs = context.getSharedPreferences("c2c_tecom.reg", Context.MODE_PRIVATE);
		return prefs.edit()				
				.putInt("sys_mic_vol", micVol)				
				.commit();
	}
	
	public static int readAppAutoLogin(Context context)
	{
		SharedPreferences prefs = context.getSharedPreferences("c2c_tecom.reg", Context.MODE_PRIVATE);
		int params = prefs.getInt("sys_app_login", 3); // auto login tag.default 3, not accept user licence.
		return params;
	}
	
	public static boolean saveAppAutoLogin(Context context, int value)
	{
		SharedPreferences prefs = context.getSharedPreferences("c2c_tecom.reg", Context.MODE_PRIVATE);
		return prefs.edit()
				.putInt("sys_app_login", value)
				.commit();
	}
	
	public static String[] readC2CLoginParams(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("c2c_tecom.reg", Context.MODE_PRIVATE);
		String[] params = new String[4];
		params[0] = prefs.getString("c2c.acc", "");
		params[1] = prefs.getString("c2c.pwd", "");
		params[2] = prefs.getString("c2c.srv", "");
		params[3] = prefs.getString("c2c.name", ""); // local name added by Tecom
		return params;
	}
	
	public static boolean saveC2CLoginParams(Context context, String account, String password, String server,
			String name) {
		if (account == null) account = "";
		if (password == null) password = "";
		if (server == null) server = "";
		if (name == null) name = "";
		SharedPreferences prefs = context.getSharedPreferences("c2c_tecom.reg", Context.MODE_PRIVATE);
		return prefs.edit()
			.putString("c2c.acc", account)
			.putString("c2c.pwd", password)
			.putString("c2c.srv", server)
			.putString("c2c.name", name)
			.commit();
	}
	
	public static boolean saveC2CLoginName(Context context, String name) {
		
		if (name == null) name = "";
		SharedPreferences prefs = context.getSharedPreferences("c2c_tecom.reg", Context.MODE_PRIVATE);
		return prefs.edit()
			.putString("c2c.name", name)
			.commit();
	}
	
	/*
	 * 根据距离感应，置灰或亮View
	 * @speakerOn. add this for speaker state. if the speaker is on, don't change the view.
	 */
	public static void onProxSensorChangeView(Activity activity, boolean near, boolean speakerOn)
	{
		//if the speaker is on, don't do it.
		if(speakerOn)
			return;
		final Window window = activity.getWindow();
		WindowManager.LayoutParams lAttrs = activity.getWindow()
				.getAttributes();
		/*
		View view = ((ViewGroup) window.getDecorView().findViewById(
				android.R.id.content)).getChildAt(0);
				*/
		View view = ((ViewGroup) window.getDecorView().findViewById(
				android.R.id.content)).getRootView();
		if (near) {
			lAttrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			view.setVisibility(View.INVISIBLE);
		} else {
			lAttrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			view.setVisibility(View.VISIBLE);
		}
		window.setAttributes(lAttrs);
	}
	
	/*
	 * 根据距离感应，置灰或亮View
	 * @speakerOn. add this for speaker state. if the speaker is on, don't change the view.
	 */
	public static void onProxSensorSceenOnOff(Activity activity, boolean near, boolean speakerOn)
	{
		//if the speaker is on, don't do it.
		if(speakerOn)
			return;
		
		WindowManager.LayoutParams params = activity.getWindow().getAttributes();
		if (near) {
			//params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
	        //params.screenBrightness = 0;
			params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
	        activity.getWindow().setAttributes(params);
		} else {
			//params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
		    //params.screenBrightness = -1f;
			params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
		    activity.getWindow().setAttributes(params);
		}
		
	}
	
	public static String[] splitString(String src, String split) throws Exception
	{
		if(src == null)
			return null;
		else
		{
			return src.split(split);
		}
	}
	/**
	 * 距离感应 ture 达到感应距离 否则
	 * 
	 * @param event
	 * @return
	 */
	public static boolean isProximitySensorNearby(final SensorEvent event) {
		float threshold = 4.001f; // <= 4 cm is near

		final float distanceInCm = event.values[0];
		final float maxDistance = event.sensor.getMaximumRange();

		if (maxDistance <= threshold) {
			// Case binary 0/1 and short sensors
			threshold = maxDistance;
		}

		return distanceInCm < threshold;
	}
	
	/*
	 * 获取连接wifi的ssid
	 * 
	 */
	public static String getConnectWifiSsid(Context mContxt){ 
        WifiManager wifiManager = (WifiManager) mContxt.getSystemService(mContxt.WIFI_SERVICE); 
        WifiInfo wifiInfo = wifiManager.getConnectionInfo(); 
        if(wifiInfo == null)
        	return null;
        LogUtils.LOGD(mContxt, "wifiInfo " +  wifiInfo.toString()); 
        LogUtils.LOGD(mContxt, "SSID " + wifiInfo.getSSID()); 
        return wifiInfo.getSSID(); 
	}
	

	/**
	 * @param mODPInitMacPwd
	 * @return
	 */
	public static String getInitPwdFromMac(String mODPInitMacPwd) {
		// TODO Auto-generated method stub
		
		String tmp = "";
		if(mODPInitMacPwd == null)
			return tmp;
		
		try{
			tmp = mODPInitMacPwd.substring(mODPInitMacPwd.length() - 8, mODPInitMacPwd.length());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return tmp;	
		
	}
	
	
	/*
	 * 从Prefence中读取信息数据，初始化User的账号信�?
	 * 
	 */
	public static void initiaUserAccount(Context mContxt)
	{
		String[] params = Utils.readC2CLoginParams(mContxt);
		Log.d("Tecom", "initiaUserAccount.... user:" + params[1] + "  pwd:" + params[2]);
		LocalUserInfo.getInstance().updateUserInfo(params[0], params[1], BuildConfig.Account_Local_Default, 
				BuildConfig.Password_Local_Default, params[3], params[2]);
	}

	/**
	 * @param appApplication
	 */
	public static void initiaODPInfo(Context mContxt) {
		// TODO Auto-generated method stub
		for(int i=0; i<BuildConfig.MAX_ODP_NUM; i++){
			Door door = Door.read(mContxt, i);
			if (!door.getId().isEmpty()){
				ODPInfo one = new ODPInfo();
				one.setOdpAccount(door.getId());
				one.setOdpIndex(door.getIndex());
				one.setOdpLocalAccount(door.getAccount());
				one.setOdpLocalPwd(door.getPassword());
				one.setOdpName(door.getODPName());
				one.setOnLine(false); //default true;
				//add default name.
				if(TextUtils.isEmpty(door.getODPName()))
				{
					if(door.getIndex() == 0)
						one.setOdpName("Front Door");
					else
						if(door.getIndex() == 1)
							one.setOdpName("Back Door");
				}
				ODPManager.getInstance().addOneODP(one);
			}
		}
	}

	/**
	 * @param index
	 * @param odpAccount
	 * @param odpLocalAcc
	 * @param odpLocalPwd
	 * @param string
	 */
	public static void addODPToList(int index, String odpAccount,
			String odpLocalAcc, String odpLocalPwd, String name) {
		// TODO Auto-generated method stub
		ODPInfo one = new ODPInfo();
		one.setOdpAccount(odpAccount);
		one.setOdpIndex(index);
		one.setOdpLocalAccount(odpLocalAcc);
		one.setOdpLocalPwd(odpLocalPwd);
		one.setOdpName(name);
		one.setOnLine(false); //default true;
		//add default name.
		if(TextUtils.isEmpty(name))
		{
			if(index == 0)
				one.setOdpName("Front Door");
			else
				if(index == 1)
					one.setOdpName("Back Door");
		}
		ODPManager.getInstance().addOneODP(one);
	}
	
	/*
	 * detect odp status.
	 * send private protocol to ODP.
	 */
	public static void detectODPStatus()
	{
		if(ODPManager.getInstance().getODPNum()<=0)
			return;
		Log.d("Tecom", " ODP number:" + ODPManager.getInstance().getODPNum() );
		for(ODPInfo one : ODPManager.getInstance().getODPList())
		{
			if(one != null)
			{
				String doorAcc = one.getOdpAccount();
				if( !TextUtils.isEmpty(doorAcc))
				{
					String doorLocalAcc = one.getOdpLocalAccount();
					String doorLocalPwd = one.getOdpLocalPwd();
					sendODPDetectProtocol(doorAcc, doorLocalAcc, doorLocalPwd);
				}
			}
		}
	}
	
	/*
	 * �?$修改后的SMP账号，还原为原始的Email以显�?
	 */
	public static String processOrignalLoginAccount(String account) {
		boolean needChange = account.contains("$");
		if (needChange) {
			String[] strs = account.split("@");
			String acc = strs[0];
			
			acc = acc.replace("$", "@") ;
			Log.d("Tecom", "transform the c2c login account to " + acc);
			return acc;
		} else {
			return account;
		}
	}
	
	
	// a integer to xx:xx:xx
    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
    
	
	////////////////////////////////////////
	
	/**
	 * @param DoorAcc2
	 * @param DoorLocalAcc2
	 * @param DoorLocalPwd2
	 */
	private static void sendODPDetectProtocol(final String DoorAcc2,
			String DoorLocalAcc2, String DoorLocalPwd2) {
		// TODO Auto-generated method stub
		System.out.printf("sendODPDetectProtocol = %s, %s, %s", DoorAcc2 , DoorLocalAcc2, DoorLocalPwd2);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) 0x0801, null); 
		byte [] data = sendOneMsg.getByteArrayFromMessageHead();
		C2CHandle.getInstance().sendCommandByProtocol(DoorAcc2, DoorLocalAcc2, DoorLocalPwd2, 0x27, data , data.length);
	
		ODPTimer one = new ODPTimer(DoorAcc2);
		one.schedule(
				new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub						
						ArrayList<ODPInfo>  list = ODPManager.getInstance().getODPList();
						for( int i=0; i< ODPManager.getInstance().getODPNum(); i++)
						{
							ODPInfo one = list.get(i);
							if(one == null)
									return ;
							if(one != null)
							{
								Log.d("Tecom", "set " + DoorAcc2 + " off-line" );
								one.setOnLine(false);
								one.setOdpState(2);
							}
						}	
						ODPTimerMannager.getInstance().removeODPTimer(DoorAcc2);
					}
				}, 8000
				);
		ODPTimerMannager.getInstance().addODPTimer(one);
	}

	/**
	 * @param event
	 */
	public static synchronized int updateODPListStauts(ReceivedC2CEvent event) {
		// TODO Auto-generated method stub
		String peerID = event.getPeerId();
		Log.d("updateODPListStauts", peerID );
		ODPTimerMannager.getInstance().removeODPTimer(peerID);
		
		ArrayList<ODPInfo>  list = ODPManager.getInstance().getODPList();
		for( int i=0; i< ODPManager.getInstance().getODPNum(); i++)
		{
			ODPInfo one = list.get(i);
			if(one == null)
					return 0;
			String doorID = one.getOdpAccount();
			if(peerID.equalsIgnoreCase(doorID))
			{
				Log.d("updateODPListStauts", peerID +" door on-line");		
				one.setOnLine(true);
				if(event.getMsg().getEventType() == MessageDataDefine.SMP_ASK_ODP_REG_STATUS_ACK)
				{					
					/*
					//1: online  2：offline  3：ODP没有此账户相关信�?
					byte data = event.getMsg().getPayloadByte();
					Log.d("Tecom", "Get SMP_ASK_ODP_REG_STATUS_ACK... data:" + data );
					one.setOdpState(data);
					*/
					byte data[] = event.getMsg().getPayloadByteArray();					
					DataConversion.printHexString("Get SMP_ASK_ODP_REG_STATUS_ACK... data:", data);
					byte status = data[0];
					byte byteLen = data[1];
					int nameLen = byteLen;
					int actulLen = data.length - 2;
					if(nameLen != actulLen)
					{
						Log.d("updateODPListStauts" , "ODP name actual len:" + actulLen + " nameLen:" + nameLen);
					}
					
					byte tmp[] = new byte[actulLen];
					System.arraycopy(data, 2, tmp, 0, tmp.length);
					String str = DataConversion.UTF8ByteToString(tmp, tmp.length);
					
					one.setOdpState(status);
					one.setOdpName(str);
				}
				
					
			}
		}
		return 1;
		
	}

	/**
	 * reset all ODP status to false.
	 */
	public static void resetODPStatusFalse() {
		// TODO Auto-generated method stub
		ArrayList<ODPInfo>  list = ODPManager.getInstance().getODPList();
		for( int i=0; i< ODPManager.getInstance().getODPNum(); i++)
		{
			ODPInfo one = list.get(i);
			if(one == null)
					return;
			one.setOnLine(false);
		}
	}
	
	/*
	 * 对收到ODP的私有协议字段进行取�?
	 */
	public static String getEqualString(String string) {
		// TODO Auto-generated method stub
		String[] strs = string.split("=");
		return strs[1];
	}
	
	public static int getWhichDoorIndex(Context mContext)
	{
		for(int i=0; i<BuildConfig.MAX_ODP_NUM; i++){
			Door door = Door.read(mContext, i);
		
			if (door.getId().isEmpty()){
				return i;
			}
		}
		//如果outdoor已满，则直接覆盖�?后一个Outdoor的配对文�?
		return BuildConfig.MAX_ODP_NUM - 1;
	}
	
	
	/**
	 * @param appApplication
	 */
	public static void initiaAppSystemConfig(Context mContext) {
		// TODO Auto-generated method stub
		int autoLogin = readAppAutoLogin(mContext);
		SystemConfigManager.getInstance().setAppAutoLogin(autoLogin);
		
		int [] audio = readAudioConfig(mContext);		
		SystemConfigManager.getInstance().setMicVol(audio[0]);		
	}

	/**
	 * @param str
	 */
	public static void updateODPFeature(String[] str) {
		// TODO Auto-generated method stub
		try {
			ODPFeature feature = ODPFeatureManager.getInstance()
					.getODPFeature();
			
			String mic = Utils.getEqualString(str[0]);
			feature.setmMicroVol(Integer.parseInt(mic));
			String speaker = Utils.getEqualString(str[1]);
			feature.setmSpeakerVol(Integer.parseInt(speaker));
			
			String resolution = Utils.getEqualString(str[2]); // 1,2,3
			feature.setmResolution(resolution);
			String frameRate = Utils.getEqualString(str[3]); // 0,1,2,3,4,5,6,7
			feature.setmFrameRate(Integer.parseInt(frameRate));

			String brightNess = Utils.getEqualString(str[4]); // 0--255
			feature.setBrightness(Integer.parseInt(brightNess));
			String contras = Utils.getEqualString(str[5]); // 0--255
			feature.setContrast(Integer.parseInt(contras));
			String hue = Utils.getEqualString(str[6]); // 0--255
			feature.setHue(Integer.parseInt(hue));
			String saturation = Utils.getEqualString(str[7]); // 0--255
			feature.setSaturation(Integer.parseInt(saturation));
			String sharp = Utils.getEqualString(str[8]); // 0--255
			feature.setSharpness(Integer.parseInt(sharp));

			String flip = Utils.getEqualString(str[9]); // 0 , 1
			feature.setFlip(Integer.parseInt(flip) == 1 ? true : false);
			String mirror = Utils.getEqualString(str[10]); // 0 , 1
			feature.setMirror(Integer.parseInt(mirror) == 1 ? true : false);
			String motionDetec = Utils.getEqualString(str[11]); // 0 , 1
			feature.setMotionDetec(motionDetec);
			String pir = Utils.getEqualString(str[12]); // 0 , 1
			feature.setPIR(Integer.parseInt(pir));
			
			String target = Utils.getEqualString(str[13]); // 0--255
			feature.setTargetY(Integer.parseInt(target));
			
			String timezone = Utils.getEqualString(str[14]); // string gmt
			feature.setTimeZone(timezone);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param buf
	 * @param degree
	 */
	public static void processSpeakerVol(short[] buf, int len, int degree) {
		// TODO Auto-generated method stub
		switch(degree)
		{
		case 1:
			for (int i=0; i<len; i++)
			{
			        buf[i] = (short) (buf[i] >> 2);
			}
			break;
		case 2:
			for (int i=0; i<len; i++)
			{
			        buf[i] = (short) (buf[i] >> 1);
			}
			break;
		case 3:
			break;
		case 4:
			for (int i = 0; i < len; i++) {
				if (buf[i] > 16383)
					buf[i] = 16383;

				if (buf[i] < -16383)
					buf[i] = -16383;

				buf[i] = (short) (buf[i] << 1);
				
			}
			break;
		case 5:
			for (int i = 0; i < len; i++) {
				if (buf[i] > 8191)
					buf[i] = 8191;

				if (buf[i] < -8191)
					buf[i] = -8191;

				buf[i] = (short) (buf[i] << 2);
				
			}
			break;
		default:
			break;
		}
		
	}

	/**
	 * @param buf
	 * @param degree
	 */
	public static void processMicVol(byte[] buf, int len, int degree) {
		// TODO Auto-generated method stub
		short temp;
		short tmp1, tmp2;
		switch (degree) {
		case 1:

			
			for (int i=0; i<len; i+=2)
			{
				tmp1 = (short) (buf[i + 1] & 0x00ff);
				tmp2 =  (short) (buf[i + 0] & 0x00ff);
				temp =  (short) ((tmp1 << 8) | tmp2 );
				temp = (short) (temp >> 2);  
				
		        buf[i + 1] = (byte) (temp >> 8); 
		        buf[i + 0] = (byte) (temp >> 0);
			}
			break;
		case 2:

			
				for (int i=0; i<len; i+=2)
				{
					tmp1 = (short) (buf[i + 1] & 0x00ff);
					tmp2 =  (short) (buf[i + 0] & 0x00ff);
					temp =  (short) ((tmp1 << 8) | tmp2 );
					temp = (short) (temp >> 1);  
					
			        buf[i + 1] = (byte) (temp >> 8); 
			        buf[i + 0] = (byte) (temp >> 0);
				}

			
			break;
		case 3:

			break;
		case 4:

			

				for (int i=0; i<len; i+=2)
				{
					tmp1 = (short) (buf[i + 1] & 0x00ff);
					tmp2 =  (short) (buf[i + 0] & 0x00ff);
					temp =  (short) ((tmp1 << 8) | tmp2 );
					temp = (short) (temp << 1);  
					
			        buf[i + 1] = (byte) (temp >> 8); 
			        buf[i + 0] = (byte) (temp >> 0);
				}

			
			break;
		case 5:

			for (int i=0; i<len; i+=2)
			{
				tmp1 = (short) (buf[i + 1] & 0x00ff);
				tmp2 =  (short) (buf[i + 0] & 0x00ff);
				temp =  (short) ((tmp1 << 8) | tmp2 );
				temp = (short) (temp << 2);  
				
		        buf[i + 1] = (byte) (temp >> 8); 
		        buf[i + 0] = (byte) (temp >> 0);
			}
			break;
		default:
			break;
		}

	}
	
	/**
	 * transform voice data from byte to short, which may decline voice quality.
	 */
	public static void preProcessVoiceAecData(byte[] buf, int offset, int byteLen, short[] processedData){
		short tmp1, tmp2;
		for (int i=0,j=0; i<byteLen; i+=2,j++)
		{
			tmp1 = (short) (buf[i + 1] & 0x00ff);
			tmp2 =  (short) (buf[i + 0] & 0x00ff);
			processedData[j] =  (short) ((tmp1 << 8) | tmp2 );
		}
	}
	
	/**
	 * transform voice data from short to byte, which may decline voice quality.
	 */
	public static void postProcessVoiceAecData(short[] buf, int shortLen, byte[] processedData, int offset){
        for (int i=0,j=0; i<shortLen*2; i+=2,j++)
        {
	        processedData[i + 1] = (byte)(buf[j] >> 8);
	        processedData[i + 0] = (byte)(buf[j] >> 0);
        }
	}

	/**
	 * @param mODPLoginAcc
	 * @return
	 */
	public static boolean isInODPAccountList(String mODPLoginAcc) {
		// TODO Auto-generated method stub
		if(!TextUtils.isEmpty(mODPLoginAcc))
		{
			for(ODPInfo odp : ODPManager.getInstance().getODPList())
			{
				if(odp.getOdpAccount().equalsIgnoreCase(mODPLoginAcc))
					return true;
			}
		}
		return false;
	}
	
	/////////////////////////////////////////////////////////////////////////////
	/*
	 * send command to the ODP via NTUT Server
	 */
	
	//SMP_GET_ODP_SYS_PARAMETER (0x0501) 请求ODP的feature
	
	public static void sendODPRequestFeature(final String DoorAcc2,
			String DoorLocalAcc2, String DoorLocalPwd2) {
		// TODO Auto-generated method stub
		System.out.printf("sendODPRequestFeature = %s, %s, %s", DoorAcc2 , DoorLocalAcc2, DoorLocalPwd2);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) 0x0501, null); 
		byte [] data = sendOneMsg.getByteArrayFromMessageHead();
		C2CHandle.getInstance().sendCommandByProtocol(DoorAcc2, DoorLocalAcc2, DoorLocalPwd2, 0x27, data , data.length);
	}
	
	//SMP_SET_ODP_SYS_PARAMETER (0x0503) 设置ODP feature
	public static void sendODPSetFeature(final String DoorAcc2,
			String DoorLocalAcc2, String DoorLocalPwd2, ODPFeature one) {
		// TODO Auto-generated method stub
		System.out.printf("\nsendODPRequestFeature = %s, %s, %s\n", DoorAcc2 , DoorLocalAcc2, DoorLocalPwd2);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		String 	RX_gain = "RX_gain=" + String.valueOf(one.getmSpeakerVol())  ;     //1~9
		String 	TX_gain = "TX_gain=" + String.valueOf(one.getmMicroVol()) ;       //1~9
	    String 	V_Resolution = "V_Resolution=" + one.getmResolution()	;	// (1~3)    1:360x240, 2:720x480, 3:1280x720         
	    String 	V_FrameRate = "V_FrameRate=" + String.valueOf(one.getmFrameRate());   // (0~7)   0: 1, 1: 3, 2: 5, 3: 10, 4: 12, 5: 15, 6: 20, 7: 30
	    String	V_Brightness = "V_Brightness=" + String.valueOf(one.getBrightness() )  ;   //0~255   亮度
	    String	V_Contras = "V_Contras=" + String.valueOf(one.getContrast() )   ;     //0~255		對比�?
	    String	V_Hue = "V_Hue=" + String.valueOf(one.getHue() )     ;        //0~255		色相
	    String	V_Saturation = "V_Saturation=" +  String.valueOf(one.getSaturation() )    ;   //0~255	飽和�?
	    String	V_Sharpness = "V_Sharpness=" +  String.valueOf(one.getSharpness() )    ;   //0~255	銳利�?
	    String	V_Flip = "V_Flip=" + String.valueOf(one.isFlip()?1:0) ;   ;    	//(0~1)  0: disable 1: enable
	    String	V_Mirror = "V_Mirror=" + String.valueOf(one.isMirror()?1:0)  ;     	//(0~1)  0: disable 1: enable
	    String	Detection= "V_Motion Detection=" + one.getMotionDetec() ;  //(0~1)  0: disable 1: enable 共有九个字符，即九宫格每个格子的设定，默认�?�为: 000000000
	    String	V_PIR = "V_PIR=" + String.valueOf(one.getPIR())   ;			//(0~1)  0: disable 1: enable
		String  V_AEtargetY= "V_AEtargetY="  + String.valueOf(one.getTargetY());     //0 ~ 255 		
		String  Time_Zone = "Time_Zone="  + String.valueOf(one.getTimeZone());     //0 ~ 255 		
		
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) 0x0503, new String[]{
			RX_gain,TX_gain,V_Resolution,V_FrameRate,
			V_Brightness,V_Contras,V_Hue,V_Saturation,
			V_Sharpness,V_Flip,V_Mirror,Detection,V_PIR,
			V_AEtargetY, Time_Zone
		}); 
		byte [] data = sendOneMsg.getByteArrayFromMessage();
		C2CHandle.getInstance().sendCommandByProtocol(DoorAcc2, DoorLocalAcc2, DoorLocalPwd2, 0x14, data , data.length);
	}
	
	/**SMP_GET_ODP_SMP_ACCOUNT (0x0401)
	 * @param doorLocalAcc
	 * @param doorLocalPwd
	 */
	public static void sendODPRequestSMPAccountStatus(String doorAcc, String doorLocalAcc,
			String doorLocalPwd) {
		// TODO Auto-generated method stub
		System.out.printf("\nsendODPRequestSMPAccountStatus = %s, %s, %s\n", doorAcc , 
				doorLocalAcc, doorLocalPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) 0x0401, new String[]{"ODP_local_account=" + doorLocalAcc,
				"ODP_local_password=" + doorLocalPwd}); 
		byte [] data = sendOneMsg.getByteArrayFromMessage();
		DataConversion.printHexString("Send Data info:", data);
		C2CHandle.getInstance().sendCommandByProtocol(doorAcc, doorLocalAcc, doorLocalPwd, 0x14, data , data.length);
	}
	
	/**SMP_REMOVE_ODP_SMP_ACCOUNT (0x0403)
	 * @param doorLocalAcc
	 * @param doorLocalPwd
	 */
	public static void sendODPRemoveOneSMPAccount(String doorAcc, String doorLocalAcc,
			String doorLocalPwd, String smpAccount, String odpSystemPwd) {
		// TODO Auto-generated method stub
		System.out.printf("\nsendODPRequestSMPAccountStatus = %s, %s, %s, %s, %s\n", doorAcc ,
				doorLocalAcc, doorLocalPwd, smpAccount, odpSystemPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) 0x0403, new String[]{"SMP_account=" + smpAccount, "ODP_system_password=" + odpSystemPwd}); 
		byte [] data = sendOneMsg.getByteArrayFromMessage();
		C2CHandle.getInstance().sendCommandByProtocol(doorAcc, doorLocalAcc, doorLocalPwd, 0x14, data , data.length);
	}
	
	/**
	 * SMP_GET_ODP_VERSION (0x0601)
	 */
	public static void sendRequestODPVersion(String doorAcc, String doorLocalAcc,
			String doorLocalPwd) {
		// TODO Auto-generated method stub
		System.out.printf("sendRequestODPVersion = %s, %s, %s\n", doorAcc , doorLocalAcc, doorLocalPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) 0x0601, null); 
		byte [] data = sendOneMsg.getByteArrayFromMessageHead();
		C2CHandle.getInstance().sendCommandByProtocol(doorAcc, doorLocalAcc, doorLocalPwd, 0x27, data , data.length);
	}
	
	
	/**
	 * SMP_GET_ODP_SYSLOG (0x0707)
	 */
	public static void sendRequestODPSysLog(String doorAcc, String doorLocalAcc,
			String doorLocalPwd) {
		// TODO Auto-generated method stub
		System.out.printf("sendRequestODPSysLog = %s, %s, %s\n", doorAcc , doorLocalAcc, doorLocalPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage(MessageDataDefine.SMP_GET_ODP_SYSLOG, null); 
		byte [] data = sendOneMsg.getByteArrayFromMessageHead();
		C2CHandle.getInstance().sendCommandByProtocol(doorAcc, doorLocalAcc, doorLocalPwd, 0x27, data , data.length);
	}
	
	/**
	 * SMP_SET_ODP_SYSLOG (0x0705)
	 */
	public static void sendSetODPSysLog(String doorAcc, String doorLocalAcc,
			String doorLocalPwd, String server, String port, String status) {
		// TODO Auto-generated method stub
		System.out.printf("sendSetODPSysLog = %s, %s, %s, %s\n", doorAcc , doorLocalAcc, doorLocalPwd,status);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage(MessageDataDefine.SMP_SET_ODP_SYSLOG, new String[]{
				"SYSLOG=" + status, "SYSLOG_server=" + server, "SYSLOG_port=" + port}); 
		byte [] data = sendOneMsg.getByteArrayFromMessage();
		
		C2CHandle.getInstance().sendCommandByProtocol(doorAcc, doorLocalAcc, doorLocalPwd, 0x27, data , data.length);
	}
	

	/**SMP_TO_ODP_VERSION_CHECK (0x0603)
	 * @param odpAccount
	 * @param odpLocalAccount
	 * @param odpLocalPwd
	 */
	public static void sendRequestCheckODPVersion(String odpAccount,
			String odpLocalAccount, String odpLocalPwd) {
		// TODO Auto-generated method stub
		System.out.printf("sendRequestCheckODPVersion = %s, %s, %s\n", odpAccount , odpLocalAccount, odpLocalPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) 0x0603, null); 
		byte [] data = sendOneMsg.getByteArrayFromMessageHead();
		C2CHandle.getInstance().sendCommandByProtocol(odpAccount, odpLocalAccount, odpLocalPwd, 0x27, data , data.length);
	}

	/**SMP_TO_ODP_UPDATE_VERSION (0x0605)
	 * @param odpAccount
	 * @param odpLocalAccount
	 * @param odpLocalPwd
	 */
	public static void sendUpdateODP(String odpAccount, String odpLocalAccount,
			String odpLocalPwd) {
		// TODO Auto-generated method stub
		System.out.printf("sendUpdateODP = %s, %s, %s\n", odpAccount , odpLocalAccount, odpLocalPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) 0x0605, null); 
		byte [] data = sendOneMsg.getByteArrayFromMessageHead();
		C2CHandle.getInstance().sendCommandByProtocol(odpAccount, odpLocalAccount, odpLocalPwd, 0x27, data , data.length);
	}

	/**
	 * @param odpAccount
	 * @param odpLocalAccount
	 * @param odpLocalPwd
	 */
	public static void sendRemoveSmpSelf(String odpAccount,
			String odpLocalAccount, String odpLocalPwd, String smpAccount, String odpSystemPwd) {
		// TODO Auto-generated method stub
		sendODPRemoveOneSMPAccount(odpAccount, odpLocalAccount,
				odpLocalPwd, smpAccount, odpSystemPwd);
	}

	/** query quiet time information from ODP.
	 * @param odpAccount
	 * @param odpLocalAccount
	 * @param odpLocalPwd
	 */
	public static void sendQueryQuietimeInfo(String odpAccount,
			String odpLocalAccount, String odpLocalPwd) {
		// TODO Auto-generated method stub
		System.out.printf("sendQueryQuietimeInfo = %s, %s, %s\n", odpAccount , odpLocalAccount, odpLocalPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) MessageDataDefine.SMP_GET_ODP_QUIET_TIME, null); 
		byte [] data = sendOneMsg.getByteArrayFromMessageHead();
		C2CHandle.getInstance().sendCommandByProtocol(odpAccount, odpLocalAccount, odpLocalPwd, 0x27, data , data.length);
	}

	/**
	 * @param odpAccount
	 * @param odpLocalAccount
	 * @param odpLocalPwd
	 */
	public static void sendSetQuietTime(String odpAccount,
			String odpLocalAccount, String odpLocalPwd, ArrayList<QuietTime>dataSet) throws Exception{
		// TODO Auto-generated method stub
		System.out.printf("\nsendSetQuietTime = %s, %s, %s\n", odpAccount ,
				odpLocalAccount, odpLocalPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) MessageDataDefine.SMP_SET_ODP_QUIET_TIME, new String[]
				{"Enable_time1=" + (dataSet.get(0).isSelected?1:0) , //(0~1)  0: disable 1: enable
				"Start_hour1=" +  dataSet.get(0).startTimeHour  ,//(0~23) 
				"Start_minute1=" + dataSet.get(0).startTimeMin  , //(0~59) 
				"End_hour1=" + dataSet.get(0).endTimeHour ,   //(0~23) 
				"End_minute1=" + dataSet.get(0).endTimeMin ,   //(0~59) 
				"Enable_time2=" + (dataSet.get(1).isSelected?1:0) ,   //(0~1)  0: disable 1: enable
				"Start_hour2=" +  dataSet.get(1).startTimeHour,   //(0~23) 
				"Start_minute2=" + dataSet.get(1).startTimeMin ,   //(0~59) 
				"End_hour2=" + dataSet.get(1).endTimeHour ,   //(0~23) 
				"End_minute2=" + dataSet.get(1).endTimeMin ,   //(0~59) 
				"Enable_time3=" + (dataSet.get(2).isSelected?1:0) ,   //(0~1)  0: disable 1: enable
				"Start_hour3=" + dataSet.get(2).startTimeHour ,   //(0~23) 
				"Start_minute3=" + dataSet.get(2).startTimeMin ,   //(0~59) 
				"End_hour3=" + dataSet.get(2).endTimeHour ,  //(0~23) 
				"End_minute3=" + dataSet.get(2).endTimeMin ,   //(0~59) 
				"Enable_time4=" + (dataSet.get(3).isSelected?1:0) ,   //(0~1)  0: disable 1: enable
				"Start_hour4=" + dataSet.get(3).startTimeHour ,   //(0~23) 
				"Start_minute4=" + dataSet.get(3).startTimeMin ,   //(0~59) 
				"End_hour4=" + dataSet.get(3).endTimeHour ,   //(0~23) 
				"End_minute4=" + dataSet.get(3).endTimeMin ,   //(0~59) 
				"Enable_time5=" + (dataSet.get(4).isSelected?1:0) ,   //(0~1)  0: disable 1: enable
				"Start_hour5=" + dataSet.get(4).startTimeHour  ,   //(0~23) 
				"Start_minute5=" + dataSet.get(4).startTimeMin ,   //(0~59) 
				"End_hour5=" + dataSet.get(4).endTimeHour ,   //(0~23) 
				"End_minute5=" + dataSet.get(4).endTimeMin   //(0~59)); " 
				});
		byte [] data = sendOneMsg.getByteArrayFromMessage();
		C2CHandle.getInstance().sendCommandByProtocol(odpAccount, odpLocalAccount, odpLocalPwd, 0x27, data , data.length);

	}

	/**
	 * @param private protocol.
	 * @param doorPeerId
	 * @param odpLocalAccount
	 * @param odpLocalPwd
	 */
	public static void sendRequestODPInfoFromSMP(short QueryMotionPPCmd,
			String doorPeerId, String odpLocalAccount, String odpLocalPwd) {
		// TODO Auto-generated method stub
		System.out.printf("sendRequestODPInfoFromSMP : cmd = %x, %s, %s, %s\n", QueryMotionPPCmd, doorPeerId , odpLocalAccount, odpLocalPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
	
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.updateMessage((short) QueryMotionPPCmd, null); 
		byte [] data = sendOneMsg.getByteArrayFromMessageHead();
		C2CHandle.getInstance().sendCommandByProtocol(doorPeerId, odpLocalAccount, odpLocalPwd, 0x27, data , data.length);
	}

	
	

	
	//end. send command to the ODP via NTUT Server
	/////////////////////////////////////////////////////////////////

	
	/**
	 * @param tmp
	 */
	public static int getWhichTimeZone(String tmp) {
		// TODO Auto-generated method stub
		try{
			for(int i=0; i < BuildConfig.timeZoneData.length; i++)
			{
				if( BuildConfig.timeZoneData[i].equalsIgnoreCase(tmp))
					return i;
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			return 25; //default GMT + 8;
		}
		
		return 25;//default GMT + 8;
	}

	/**
	 * @param arg1
	 * @return
	 */
	public static String getTimeZoneStr(int arg1) {
		// TODO Auto-generated method stub
		try{
			return BuildConfig.timeZoneData[arg1];
		}catch(Exception e)
		{
			e.printStackTrace();
			return BuildConfig.timeZoneData[25]; //default GMT + 8;
		}
		
		
	}

	/**
	 * @return
	 */
	public static String getSMPCurrentTimeZone() {
		// TODO Auto-generated method stub
		TimeZone tz =  TimeZone.getDefault();
		return tz.getDisplayName(false, TimeZone.SHORT);
	}

	/*
	 * 是否全为数字
	 */
	public static boolean isDigit(String strNum) {  
	    return strNum.matches("[0-9]{1,}");  
	} 
	
	/*
	 * 是否全为数字
	 */
	public static boolean isCharacter(String strNum) {  
	    return strNum.matches("[a-zA-Z]+");  
	}

	/*
	 * 是否含有一个小写字母
	 */
	public static boolean isContainOneLower(String str)
	{
		boolean ret = false;
		
		if(TextUtils.isEmpty(str))
		{
			return ret;
		}else
		{
			int len  = str.length();
			for( int i=0 ;i < len; i++)
			{
				char ch = str.charAt(i);
				if(Character.isLowerCase(ch))
				{
					ret = true;
				}
			}
		}
		return ret;
	}
	/*
	 * 是否含有一个大写字母
	 */
	public static boolean isContainOneUpper(String str)
	{
		boolean ret = false;
		
		if(TextUtils.isEmpty(str))
		{
			return ret;
		}else
		{
			int len  = str.length();
			for( int i=0 ;i < len; i++)
			{
				char ch = str.charAt(i);
				if(Character.isUpperCase(ch))
				{
					ret = true;
				}
			}
		}
		return ret;
	}
	/**
	 * @param string
	 * @param i
	 * @return
	 */
	public static String getSubString(String string, int i) {
		// TODO Auto-generated method stub
		String str = "";
		if(TextUtils.isEmpty(string))
			return str;
		
		int len = string.length();
		if( len <= i)
			return string;
		else{
			String tmp = string.substring(0, i);
			tmp = tmp.concat("...");
			return tmp;
		}
		
	}

	/**
	 * @param appUtilsService
	 */
	public static void initiaODPStatus(Context mContext) {
		// TODO Auto-generated method stub
		ODPManager.getInstance().resetODPStatus();
	} 
}
