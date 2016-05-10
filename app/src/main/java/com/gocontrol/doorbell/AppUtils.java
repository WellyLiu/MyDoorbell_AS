package com.gocontrol.doorbell;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.iptnet.c2c.C2CSubEvent;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.audiofx.AcousticEchoCanceler;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppUtils {
	
	private static final String TAG = AppUtils.class.getSimpleName();
	private static AcousticEchoCanceler mAEC;
	public static final String APP_NAME = "MyDoorbell";
	public static String SD_PATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath();
	public static final int ERR_C2C_INCOMING_ERROR	= 0xF0001;
	public static final int ERR_C2C_PACKET_LOSS		= 0xF0002;
	
	public static String[] readWebLoginParams(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("web", Context.MODE_PRIVATE);
		String[] params = new String[4];
		params[0] = prefs.getString("web.domain", "");
		params[1] = prefs.getString("web.acc", "");
		params[2] = prefs.getString("web.pwd", "");
		params[3] = prefs.getString("web.token", "");
		return params;
	}
	
	public static boolean saveWebLoginParams(Context context, String domain, String account, String password, String token) {
		if (domain == null) domain = "";
		if (account == null) account = "";
		if (password == null) password = "";
		SharedPreferences prefs = context.getSharedPreferences("web", Context.MODE_PRIVATE);
		return prefs.edit()
			.putString("web.domain", domain)
			.putString("web.acc", account)
			.putString("web.pwd", password)
			.putString("web.token", token)
			.commit();
	}
	
	public static String[] readC2CLoginParams(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("c2c.reg", Context.MODE_PRIVATE);
		String[] params = new String[3];
		params[0] = prefs.getString("c2c.acc", "");
		params[1] = prefs.getString("c2c.pwd", "");
		params[2] = prefs.getString("c2c.srv", "");
		return params;
	}
	
	public static boolean saveC2CLoginParams(Context context, String account, String password, String server) {
		if (account == null) account = "";
		if (password == null) password = "";
		if (server == null) server = "";
		SharedPreferences prefs = context.getSharedPreferences("c2c.reg", Context.MODE_PRIVATE);
		return prefs.edit()
			.putString("c2c.acc", account)
			.putString("c2c.pwd", password)
			.putString("c2c.srv", server)
			.commit();
	}
	
	public static String readGcmToken(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("gcm", Context.MODE_PRIVATE);
		return prefs.getString("token", "");
	}
	
	public static boolean saveGcmToken(Context context, String token) {
		SharedPreferences prefs = context.getSharedPreferences("gcm", Context.MODE_PRIVATE);
		if (token == null) token = "";
		return prefs.edit().putString("token", token).commit();
	}
	
	public static boolean isEchoCancellationEnabled() {
		if (mAEC != null) {
			try {
				return mAEC.getEnabled();
			} catch (IllegalStateException e) {
				Log.e(TAG, "get echo cancellation enable exception, msg = " + e.getMessage());
			}
		}
		return false;
	}
	
	public static void enableHTCEC(AudioManager am)
	{
		if (android.os.Build.BRAND.equals("HTC")
				|| android.os.Build.BRAND.startsWith("HTC")
				|| android.os.Build.BRAND.startsWith("htc")) {

			Log.d(TAG, "In HTC HW ACE");

			if (am != null) {
				am.setParameters("HTCHWAEC=ON");// ON->hw AEC is ON,
															// OFF->hw AEC is
															// OFF

				// set mode to MODE_IN_COMMUNICATION
				am.setMode(AudioManager.MODE_IN_COMMUNICATION);

			}

		}
	}
	
	public static boolean enableEchoCancellation(int audioSessionId) {
		// process echo cancellation
		boolean isAvailable = AcousticEchoCanceler.isAvailable();
		Log.d(TAG, "Is the device's echo cancallation available ? (" + isAvailable + ")");
		if (isAvailable) {
			mAEC = AcousticEchoCanceler.create(audioSessionId);
			if (mAEC == null) {
				Log.w(TAG, "the device is not implement the echo cancellation");

			} else {
				try {
					int ret = mAEC.setEnabled(true);
					if (ret == AcousticEchoCanceler.SUCCESS) {
						Log.d(TAG, "enable echo cancellation success");
						return true;
						
					} else {
						Log.e(TAG, "enable echo cancellation error (" + ret + ")");
					}
				} catch (IllegalStateException e) {
					Log.e(TAG, "enable echo cancellation exception, msg = " + e.getMessage());
				}
			}
		}
		return false;
	}
	
	public static void disableEchoCancellation() {
		// process echo cancellation
		if (mAEC != null) {
			try {
				int ret = mAEC.setEnabled(false);
				if (ret == AcousticEchoCanceler.SUCCESS) {
					Log.d(TAG, "disable the echo cancellation success");
				} else {
					Log.w(TAG, "disable the echo cancellation error (" + ret + ")");
				}
			} catch (IllegalStateException e) {
				Log.e(TAG, "disable echo cancellation exception, msg = " + e.getMessage());
			} finally {
				mAEC.release();
				mAEC = null;
				Log.d(TAG, "echo calcellation was released");
			}
		} else {
			Log.w(TAG, "echo cancellation was not enabled");
		}
	}
	
	public static void mediaScan(Context context, File file) {
		String ext = MimeTypeMap.getFileExtensionFromUrl((Uri.fromFile(file).toString()));
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		MediaScannerConnection.scanFile(
			context, new String[] {file.toString()}, new String[] {mimeType}, null);
	}
	
	public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
		ActivityManager mgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo serviceInfo : mgr.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceInfo.service.getClassName().equals(serviceClass.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public static File getExternalFileDir(Context context, String... dirLevel) {
		File root = new File(SD_PATH + "/" + APP_NAME);
		for (int i=0; i<dirLevel.length; i++) {
			root = new File(root, dirLevel[i]);
			if (!root.exists())	root.mkdirs();
		}
		return root;
	}
	
	public static String createFileTimestamp() {
		
		// time
		SimpleDateFormat format
			= new SimpleDateFormat("MM-dd-yyyy HH.mm.ss a", Locale.getDefault());
		Date date = new Date(System.currentTimeMillis());
		
		// create file name
		return format.format(date);
	}
	
	public static String createFileTime() {
		
		// time
		SimpleDateFormat format
			= new SimpleDateFormat("MM-dd-yyyy HH.mm.ss", Locale.getDefault());
		Date date = new Date(System.currentTimeMillis());
		
		// create file name
		return format.format(date);
	}
	
	public static String createFileDay() {
		
		// time
		SimpleDateFormat format
			= new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
		Date date = new Date(System.currentTimeMillis());
		
		// create file name
		return format.format(date);
	}
	
	
	public static void reportError(Context context, int error, Object... obj) {
		
		if (ERR_C2C_INCOMING_ERROR == error) {
			if (obj[0] != null) {
				
				int errCode = 0;
				if (obj[0] instanceof C2CSubEvent) {
					C2CSubEvent subEvent = (C2CSubEvent) obj[0];
					errCode = subEvent.getCode();
				}

				((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
					.notify(555042612,
					new Notification.Builder(context)
						.setSmallIcon(android.R.drawable.ic_dialog_alert)
						.setContentTitle("C2C INCOMING ERROR (" + errCode + ")")
						.build());
				
			} else
				Log.e(TAG, "report error message fail, 'ERR_C2C_INCOMING_ERROR'");
			
		} else if (ERR_C2C_PACKET_LOSS == error) {
			if (obj[0] != null && obj[0] instanceof Integer) {
				
				((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(555042613,
				new Notification.Builder(context)
					.setSmallIcon(android.R.drawable.ic_dialog_alert)
					.setContentTitle("C2C PACKET LOSS")
					.setContentText("Line ID: " + String.valueOf((Integer) obj[0]))
					.build());
				
			} else
				Log.e(TAG, "report error message fail, 'ERR_C2C_PACKET_LOSS'");
		}
	}
	public static String processWebLoginAccount(String account, String server) {
		boolean needChange = account.contains("@");
		if (needChange) {
			account = account.replace("@", "$") + "@" + server;
			Log.d(TAG, "transform the account to " + account);
			return account;
		} else {
			return account;
		}
	}
}
