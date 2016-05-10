package com.gocontrol.doorbell.utils;

import java.io.File;
import java.io.IOException;

import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.ViewerActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This class is copied from the project https://github.com/google/iosched
public class LogUtils {
    private static final String LOG_PREFIX = "jb_digipass_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
         * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static String makeLogTag(Object obj){ return  obj.getClass().getSimpleName();}
    public static void LOGD(final String tag, String message) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfig.DEBUG  || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }

    public static void LOGD(final Object obj, String message) {
        LOGD(makeLogTag(obj), message);
    }

    public static void LOGD(final String tag, String message, Throwable cause) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message, cause);
        }
    }

    public static void LOGD(final Object obj, String message, Throwable cause) {
        LOGD(makeLogTag(obj), message,cause);
    }

    public static void LOGV(final String tag, String message) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, message);
        }
    }

    public static void LOGV(final Object obj, String message) {
        LOGV(makeLogTag(obj), message);
    }

    public static void LOGV(final String tag, String message, Throwable cause) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, message, cause);
        }
    }

    public static void LOGV(final Object obj, String message, Throwable cause) {
        LOGV(makeLogTag(obj), message, cause);
    }

    public static void LOGI(final String tag, String message) {
        Log.i(tag, message);
    }

    public static void LOGI(final Object obj, String message) {
        LOGI(makeLogTag(obj), message);
    }
    public static void LOGI(final String tag, String message, Throwable cause) {
        Log.i(tag, message, cause);
    }

    public static void LOGI(final Object obj, String message, Throwable cause) {
        LOGI(makeLogTag(obj), message, cause);
    }

    public static void LOGW(final String tag, String message) {
        Log.w(tag, message);
    }

    public static void LOGW(final Object obj, String message) {
        LOGW(makeLogTag(obj), message);
    }

    public static void LOGW(final String tag, String message, Throwable cause) {
        Log.w(tag, message, cause);
    }

    public static void LOGW(final Object obj, String message, Throwable cause) {
        LOGW(makeLogTag(obj), message, cause);
    }

    public static void LOGE(final String tag, String message) {
        Log.e(tag, message);
    }

    public static void LOGE(final Object obj, String message) {
        LOGE(makeLogTag(obj), message);
    }

    public static void LOGE(final String tag, String message, Throwable cause) {
        Log.e(tag, message, cause);
    }

    public static void LOGE(final Object obj, String message, Throwable cause) {
        LOGE(makeLogTag(obj), message, cause);
    }

    private LogUtils() {
    }
    
    private static File outputFile;
    public static void StartLogCatToFile(Context mContext)
    {
    	 // save logcat in file
    	String path = "logs";
		String fileName = AppUtils.createFileDay() + ".txt";
		Log.d("tecom", "start log  0");
		outputFile = new File(AppUtils.getExternalFileDir(mContext, path), fileName);
		Log.d("tecom", "outputFile:" + outputFile.getAbsolutePath());
		
        try {
        	Log.d("tecom", "start log  2");
            Runtime.getRuntime().exec(
                    "logcat -v time -f " + outputFile.getAbsolutePath());            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //Log.d("tst", "start log  4");
        }
    }
    public static void DeleteLogFile()
    {
    	if(outputFile != null)
    	{
    		File parentFolder = outputFile.getParentFile();
    		if(parentFolder.exists())
    		{
    			File files[] = parentFolder.listFiles(); 
    			for (int i = 0; i < files.length; i++) { 
    				Boolean t = files[i].delete();
    	    		if(t)
    	    		{
    	    			Log.d("tecom", i + " delete log file ok.");
    	    		}else
    	    		{
    	    			Log.d("tecom", i +" delete log file failed.");
    	    		}
    			}
    		}else
    		{
    			Log.d("tst", "parentFolder not exit...");
    		}
    		
    	}else
    	{
    		Log.d("tecom", "DeleteLogFile() outputFile null.");
    	}
    }
    public static void SendLogFileToEmail(Context mContext)
    {
    	 //send file using email
    	 Intent emailIntent = new Intent(Intent.ACTION_SEND);
    	 // Set type to "email"
    	 emailIntent.setType("vnd.android.cursor.dir/email");
    	 String to[] = {"mydoorbell.support@nortek.com"};
    	 emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
    	 // the attachment
    	 Log.d("tst", outputFile.getAbsolutePath());
    	 emailIntent .putExtra(Intent.EXTRA_STREAM,  Uri.fromFile(outputFile));
    	 // the mail subject
    	 emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject:" + mContext.getString(R.string.app_name) + mContext.getString(R.string.app_version));
    	 mContext.startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }
}
