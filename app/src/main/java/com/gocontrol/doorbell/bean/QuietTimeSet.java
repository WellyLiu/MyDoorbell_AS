package com.gocontrol.doorbell.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import com.gocontrol.doorbell.AppApplication;

import android.util.Log;

/** 
* @ClassName: QuietTimeSet 
* @Description: store the data of the quiet hours and write to the file.
*  @author: Vincent Luo
* @email: wlluo@tecomtech.com
* @date: 2015-12-16 下午1:48:25 
*  
*/ 

public class QuietTimeSet implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TAG = QuietTimeSet.class.getSimpleName();
	private static final boolean DEFAULT_STATUS = true;
	private static QuietTimeSet mInstance;
	private ArrayList<QuietTime> qtList = null;
	
	private QuietTimeSet()
	{
		QuietTimeSet bj = (QuietTimeSet)readObjectFromFile();
		if(bj != null){
			this.qtList = bj.qtList;
		}else{
			qtList = new ArrayList<QuietTimeSet.QuietTime>();
		}
	}
	public void writeObjectToFile(Object obj)
    {
        File file =new File(AppApplication.getInstance().getExternalFilesDir(null),"QuietTimeSet.dat");
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            ObjectOutputStream objOut=new ObjectOutputStream(out);
            objOut.writeObject(obj);
            objOut.flush();
            objOut.close();
            Log.d(TAG,"Store Quiet Time Set success!");
        } catch (IOException e) {
        	Log.d(TAG,"Store Quiet Time Set Failed!");
            e.printStackTrace();
        }
    }
	public boolean nowIsQuietTime(){
		boolean ret = false;
		Calendar cal = Calendar.getInstance();// current date
		int hour = cal.get(Calendar.HOUR_OF_DAY);// get the hour
		int minute = cal.get(Calendar.MINUTE);// get the minute.
		int minuteOfDay = hour*60 + minute;
		for(int i = 0;i < qtList.size();i++){
			QuietTime qt = qtList.get(i);
			if(qt.isSelected){
				int targetStartMinOfDay = qt.startTimeHour*60+qt.startTimeMin;
				int targetEndMinOfDay = qt.endTimeHour*60 + qt.endTimeMin;
				if((targetStartMinOfDay <= targetEndMinOfDay && minuteOfDay >= targetStartMinOfDay && minuteOfDay <= targetEndMinOfDay)
						|| (targetStartMinOfDay > targetEndMinOfDay && (minuteOfDay >= targetStartMinOfDay || minuteOfDay <= targetEndMinOfDay))){
					Log.d(TAG,String.format("%02d:%02d is between %02d:%02d ~ %02d:%02d", hour,minute,qt.startTimeHour,qt.startTimeMin,qt.endTimeHour,qt.endTimeMin));
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
	private Object readObjectFromFile()
    {
        Object temp=null;
        File file =new File(AppApplication.getInstance().getExternalFilesDir(null),"QuietTimeSet.dat");
        
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            ObjectInputStream objIn=new ObjectInputStream(in);
            temp=objIn.readObject();
            objIn.close();
            Log.d(TAG,"Read Quiet Time Set success!");
        } catch (IOException e) {
        	Log.d(TAG,"Read Quiet Time Set failed!");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return temp;
    }
	public synchronized static QuietTimeSet getInstance()
	{
		if(mInstance == null)
			mInstance = new QuietTimeSet();
		
		return mInstance;
	}
	public synchronized void addQuietTime(int startTimeHour,int startTimeMin,int endTimeHour,int endTimeMin){
		QuietTime qt = new QuietTime();
		qt.index = qtList.size();
		qt.startTimeHour = startTimeHour;
		qt.endTimeHour = endTimeHour;
		qt.startTimeMin = startTimeMin;
		qt.endTimeMin = endTimeMin;
		qt.isSelected = DEFAULT_STATUS;
		qtList.add(qt);
		//writeObjectToFile(qtList);
	}
	public synchronized void delQuietTime(int index) throws IndexOutOfBoundsException{
		try{
			qtList.remove(index);
		}catch(IndexOutOfBoundsException e){
			throw e;
		}
		//writeObjectToFile(qtList);
	}
	public synchronized void setQuietTimeChecked(int index,boolean isChecked) throws IndexOutOfBoundsException{
		if(index < 0 || index >= qtList.size()){
			throw new IndexOutOfBoundsException();
		}	
		QuietTime qt = qtList.get(index);
		qt.isSelected = isChecked;
		//writeObjectToFile(qtList);
	}
	public synchronized void modifyQuietTime(int index,int startTimeHour,int startTimeMin,int endTimeHour,int endTimeMin) throws IndexOutOfBoundsException{
		if(index < 0 || index >= qtList.size()){
			throw new IndexOutOfBoundsException();
		}	
		QuietTime qt = qtList.get(index);
		qt.startTimeHour = startTimeHour;
		qt.endTimeHour = endTimeHour;
		qt.startTimeMin = startTimeMin;
		qt.endTimeMin = endTimeMin;
		//writeObjectToFile(qtList);
	}
	public synchronized QuietTime getQuietTime(int index) throws IndexOutOfBoundsException{
		if(index < 0 || index >= qtList.size()){
			throw new IndexOutOfBoundsException();
		}	
		return qtList.get(index);
	}
	public synchronized int getQuitTimeCount(){
		return qtList.size();
	}
	public class QuietTime implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int index;
		public int startTimeHour;
		public int startTimeMin;
		public int endTimeHour;
		public int endTimeMin;
		public boolean isSelected;
	}
}
