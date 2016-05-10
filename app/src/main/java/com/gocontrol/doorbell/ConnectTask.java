package com.gocontrol.doorbell;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gocontrol.doorbell.ConnectTask.Result;
import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.C2CListener;
import com.iptnet.c2c.C2CSubEvent;

public class ConnectTask extends C2CListener implements Callable<Result> {

	public static final int CONNECT_TIME_OUT = 30; // time unit is second
	
	private int mLineId = Integer.MIN_VALUE;
	private Result mResult;
	
	public static enum Result {
		// success
		CONNECTED_P2P,
		CONNECTED_RELAY,
		// fail
		FAIL_NETWORK,
		FAIL_TIMEOUT,
		FAIL_CALLING,
		FAIL_UNAUTHORIZED,
		FAIL_SRV_NO_RSP,
		FAIL_PEER_OFFLINE,
		FAIL_PEER_BUSY,
		FAIL_PEER_NO_RSP;

		private int mLineId = Integer.MIN_VALUE;
		
		Result setLineId(int lineId) {
			mLineId = lineId;
			return this;
		}
		
		public int getLineId() {
			return mLineId;
		}
	}
	
	public synchronized Result startConnection(String peerId, String account, String password, String tag) {
		
		// register C2C listener
		C2CHandle c2c = C2CHandle.getInstance();
		c2c.addListener(this);

		// start connect to peer
		mLineId = c2c.startConnection(peerId, account, password, tag);
		if (mLineId < 0) {
			return Result.FAIL_NETWORK.setLineId(mLineId);
		}
			
		// start task to connect to peer
		FutureTask<Result> task = new FutureTask<ConnectTask.Result>(this);
		new Thread(task).start();
		try {
			mResult = task.get(CONNECT_TIME_OUT, TimeUnit.SECONDS).setLineId(mLineId);
			
		} catch (InterruptedException e) {
			mResult = Result.FAIL_CALLING;
			
		} catch (ExecutionException e) {
			mResult = Result.FAIL_CALLING;
			
		} catch (TimeoutException e) {
			mResult = Result.FAIL_TIMEOUT;
		}
		
		// unregister C2C listener
		c2c.removeListener(this);

		return mResult;
	}
	
	@Override
	protected void receiveMessage(C2CEvent event) {
		
		// get response event
		switch (event) {
			case C2C_OUTGOING_ERROR:
				C2CSubEvent sub = event.getSubEvent();
				switch (sub) {
					case C2C_UNAUTHORIZED:	mResult = Result.FAIL_UNAUTHORIZED;	break;
					case C2C_SRV_NO_RESP:	mResult = Result.FAIL_SRV_NO_RSP;	break;
					default:				mResult = Result.FAIL_CALLING;
				}
				break;		
			case C2C_P2P_MODE:		mResult = Result.CONNECTED_P2P;		break;
			case C2C_RELAY_MODE:	mResult = Result.CONNECTED_RELAY;	break;
			case C2C_RECV_BUSY:		mResult = Result.FAIL_PEER_BUSY;	break;
			case C2C_RECV_404:		mResult = Result.FAIL_PEER_OFFLINE;	break;
			case C2C_NOANSWER:		mResult = Result.FAIL_PEER_NO_RSP;	break;
			default:
		}
	}
	
	@Override
	public Result call() throws Exception {
		while (mResult == null) {
			
			// wait responsse result
			Thread.sleep(100);
		}
		return mResult;
	}	
}
