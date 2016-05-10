package com.gocontrol.doorbell.message;



import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

/**
 * @author Administrator
 *
 */
public class DataQueueManager {

	private static Queue<MessageType> dataQueue;
	
	private static DataQueueManager mInstance;
	
	ProcessDataQueueThread mThread;
	
	private DataQueueManager()
	{
		dataQueue = new LinkedBlockingQueue<MessageType>();
		//startDataProcessMessage();
	}
	
	public static DataQueueManager getInstance()
	{
		if(mInstance == null)
			mInstance = new DataQueueManager();
		return mInstance;
	}
	
	public void addData(MessageType message)
	{
		System.out.printf("add one data. \n");
		boolean ret = dataQueue.offer(message);
		if(ret == false)
		{
			System.out.println(this.toString() +  " addData. the queue is full.");
		}
	}
	public MessageType getOneData()
	{
		MessageType message = dataQueue.poll();
		if(message == null)
		{
			//System.out.println(this.toString() +  "getOneData.the queue is null.");
		}
		return message;
	}
	
	
	public void clearAllData()
	{
		if(dataQueue != null)
			dataQueue.clear();
	}
	
	public void startDataProcessMessage()
	{
		
		if(mThread == null){
			
			mThread = new ProcessDataQueueThread();
		}
		mThread.start();
		
	}
	
	public void stopDataMessageQueueThread()
	{
		if(mThread != null){
			mThread.setStopFlag(false);
			//mThread.interrupt();
			mThread = null;
			clearAllData();
		}
	}
	
	public void releaseSource()
	{
		stopDataMessageQueueThread();
		dataQueue.clear();
	}
	

	/**
	 * @param rec
	 */
	public void receiveData(byte[] rec) {
		// TODO Auto-generated method stub
		//判断数据合法性
		if(rec.length < 38){
			System.out.println(this.toString() + " == received data length error.");
			return;
		}
		byte [] len = new byte[]{rec[34],rec[35],rec[36], rec[37]};
		DataConversion.printHexString("received payload length:", len);
		int payloadLength = DataConversion.bytesToInt2(len, 0);
		System.out.println(payloadLength);
		System.out.println(rec.length);
		if( (payloadLength + 38) != rec.length)
		{
			System.out.println(this.toString() + " == received data length error 2.");
			return;
		}
		//构建ReceivedMessageType
		ReceivedMessageType one = new ReceivedMessageType(rec);

		//加入处理队列
		addData(one);
	}
	
	public static void main(String []args){
		DataQueueManager.getInstance().startDataProcessMessage();
		new Thread()
		{

			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				int i=0;
				while(i<1000){
					DataQueueManager.getInstance().addData(new MessageType());
					try {
						sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}.start();
	}

}
