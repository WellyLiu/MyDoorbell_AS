package com.gocontrol.doorbell.message;



import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author Administrator
 *
 */
public class MessageQueueManager {

	private static Queue<MessageType> messageQueue;
	private ProcessMessageQueueThread mThread;
	
	private static MessageQueueManager mInstance;
	private MessageQueueManager()
	{
		messageQueue = new LinkedBlockingQueue<MessageType>();
		//startProcessMessage();
	}
	
	public static MessageQueueManager getInstance()
	{
		if(mInstance == null)
			mInstance = new MessageQueueManager();
		return mInstance;
	}
	
	public void addMessage(MessageType message)
	{
		System.out.printf(this.toString() +  "addMessage. message type .\n");
		boolean ret = messageQueue.offer(message);
		if(ret == false)
		{
			System.out.println(this.toString() +  "addMessage. the queue is full.");
		}
	}
	public MessageType getOneMessage()
	{
		MessageType message = messageQueue.poll();
		if(message == null)
		{
			//System.out.println(this.toString() +  "getOneMessage.the queue is null.");
		}
		return message;
	}
	
	public void clearAllMessage()
	{
		if(messageQueue != null)
			messageQueue.clear();
	}
	
	public void startProcessMessage()
	{
		if(mThread == null)
			mThread = new ProcessMessageQueueThread();
		mThread.start();
	}
	
	public void stopProcessMessageQueueThread()
	{
		if(mThread != null){
			mThread.setStopFlag(false);
			//mThread.interrupt();
			mThread = null;
			clearAllMessage();
		}
	}
	
	public void releaseSource()
	{
		stopProcessMessageQueueThread();
		if(messageQueue != null)
			messageQueue.clear();
	}
	
	public static void main(String []args)
	{
		System.out.println("test start... ...");
		MessageQueueManager.getInstance().startProcessMessage();
		new Thread()
		{

			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				int i=0;
				while(true)
				{
					System.out.printf("add message... %d\n", i++);
					MessageQueueManager.getInstance().addMessage(new MessageType());
					try {
						sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//MessageQueueManager.getInstance().stopProcessMessageQueueThread();
			}
			
		}.start();
	}
}
