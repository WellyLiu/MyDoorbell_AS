package com.gocontrol.doorbell;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.iptnet.c2c.BaseAudioFrame;

import android.os.Process;

public abstract class AudioJitterBuffer {

	private final static int MAX_QUEUE_SIZE = 255;
	
	private int mQueueTimeInMsec;
	
	private int lastAddQueueTime;
	private int lastTakeQueueTime;
	private volatile int totalQueueTime;
	private boolean putting;
	private boolean enabled;
	
	private ExecutorService services = Executors.newSingleThreadExecutor();
	private ArrayBlockingQueue<BaseAudioFrame> queue = new ArrayBlockingQueue<BaseAudioFrame>(MAX_QUEUE_SIZE);
	
	public abstract void onBufferOut(BaseAudioFrame frame, int queue, int totalQueueTime);
	
	private class TakeQueue implements Runnable {
		public void run() {
			
			Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
			
			while (!services.isShutdown()) {
				try {
					if (putting) {
						if (totalQueueTime <= mQueueTimeInMsec) {						
							// not need drop						
							BaseAudioFrame frame = queue.take();
							
							// process time
							int diff = decreaseTime(frame.getTimestamp());
							int sleep = diff;
							if (sleep <= 0)
								sleep = 0;
	 
							Thread.sleep(sleep);
							int queueSize = queue.size();
							onBufferOut(frame, queueSize, totalQueueTime);
								
						} else {
							BaseAudioFrame frame = queue.take();
							decreaseTime(frame.getTimestamp());
						}
						
						if (queue.size() == 0) {
							putting = false;
						}
						
					} else {
						try { Thread.sleep(10); }
						catch (InterruptedException e) {}
					}
										
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	public AudioJitterBuffer() {
		start();
	}
	
	public void start() {
		services.execute(new TakeQueue());
	}
	
	public void stop() {
		services.shutdown();
		queue.clear();		
	}
	
	public void setQueueTime(int timeInMsec) {
		mQueueTimeInMsec = timeInMsec;
	}
	
	public void enabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean putBuffer(BaseAudioFrame frame) {
		if (frame == null) return false;
		
		if (!enabled) {
			onBufferOut(frame, 0, 0);
			return true;
		}
		
		// process time
		increaseTime(frame.getTimestamp());
		boolean putOk = queue.offer(frame);
		if (totalQueueTime >= mQueueTimeInMsec) {
			putting = true;
		}
		return putOk;
	}
	
	private int increaseTime(int timestamp) {
		if (lastAddQueueTime == 0 || lastAddQueueTime > timestamp)
			lastAddQueueTime = timestamp;
		int diff = timestamp - lastAddQueueTime;
		totalQueueTime += diff;
		lastAddQueueTime = timestamp;
		return diff;
	}
	
	private int decreaseTime(int timestamp) {
		if (lastTakeQueueTime == 0 || lastTakeQueueTime > timestamp)
			lastTakeQueueTime = timestamp;
		int diff = timestamp - lastTakeQueueTime;
		totalQueueTime -= diff;
		lastTakeQueueTime = timestamp;
		return diff;
	}
}
