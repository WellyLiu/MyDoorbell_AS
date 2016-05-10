package com.gocontrol.doorbell;

import com.iptnet.c2c.BaseAudioFrame;
import com.iptnet.c2c.BaseVideoFrame;
import com.iptnet.c2c.C2CCallInfo;
import com.iptnet.c2c.C2CChannel;
import com.iptnet.c2c.C2CCommand;
import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.C2CListener;
import com.iptnet.c2c.MediaFrame;
import com.iptnet.c2c.ProtocolChannel;
import com.iptnet.c2c.RTPChannel;
import com.iptnet.c2c.StringCommand;

import android.app.Activity;
import android.util.Log;

public abstract class DoorProcess {

	private static final String TAG = DoorProcess.class.getSimpleName();
	private Activity mActivity;
	private C2CHandle mC2CHandle;
	private C2CSDK mC2CListener;
	private Session mWaitSession, mCurrSession;
	private boolean mWaitIncomingState;
	private boolean mNewIncomingState;
	
	public abstract void onUnlockResponse(boolean success, int lineId, int sessionCode, int errorCode);
	public abstract void onAnswerResponse(boolean success, int lineId, int sessionCode, boolean newConnection, int errorCode);
	public abstract void onRejectResponse(int lineId, int sessionCode, int remainConnection);
	public abstract void onReceiveVideo(int lineId, BaseVideoFrame frame);
	public abstract void onReceiveAudio(int lineId, BaseAudioFrame frame);
	public abstract void onAudioPlayResponse(int lineId, boolean opened, boolean reject);
	public abstract void onIncomingState(int lineId, int sessionCode);
	public abstract void onMediaConnection(int lineId, int sessionCode);
	public abstract void onIncomingCancelled(int lineId, int sessionCode, int remainConnection);
	public abstract void onTerminated(int lineId);

	public static class OperationInvalidException extends Exception {
		private static final long serialVersionUID = 1470395554643156988L;
		public OperationInvalidException(String msg) {
			super(msg);
		}
	}
	
	public static class Session {
		
		private static final int OP_NONE = 0;
		private static final int OP_ANSWER = 1;
		private static final int OP_UNLOCK = 2;
		private static final int OP_REJECT = 3;
		private static final int OP_AUDIO_PLAY = 4;
		
		private int mLineId, mSessionCode;
		private String mPeerId;
		private boolean mRing;
		private int mOperationState;
		
		public Session(Session session) {
			mLineId = session.mLineId;
			mSessionCode = session.mSessionCode;
			mPeerId = session.mPeerId;
			mRing = session.mRing;
			mOperationState = session.mOperationState;
		}

		public Session(int lineId, String peerId) {
			mLineId = lineId;
			mPeerId = peerId;
			mRing = false;
			mSessionCode = Integer.MIN_VALUE;
		}
		
		public Session(int lineId, String peerId, int sessionCode) {
			mLineId = lineId;
			mPeerId = peerId;
			mRing = true;
			mSessionCode = sessionCode;
		}
		
		public Session(int lineId, String peerId, boolean ring, int sessionCode) {
			mLineId = lineId;
			mPeerId = peerId;
			mRing = ring;
			mSessionCode = sessionCode;
		}
		
		public int getLineId() {
			return mLineId;
		}
		
		public String getPeerId() {
			if (mPeerId == null) mPeerId = "";
			return mPeerId;
		}
		
		public boolean isRingMode() {
			return mRing;
		}
		
		public int getSessionCode() {
			return mSessionCode;
		}
	}
	
	public DoorProcess(Activity activity, C2CHandle c2c) {
		mActivity = activity;
		mC2CHandle = c2c;
	}
	
	private void processUnlock(final boolean success, final int errorCode) {
		
		final int lineId = mCurrSession.mLineId;
		final int sessionCode = mCurrSession.mSessionCode;
		
		mCurrSession.mOperationState = Session.OP_NONE;
		
		// callback to outside by main thread
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				Log.d(TAG, "[callback] unlock method");
				onUnlockResponse(success, lineId, sessionCode, errorCode);
		}});
	}
	
	private void processAnswerWithConnection(final boolean success, final boolean newConnection, final int errorCode) {
		
		final int lineId = mWaitSession.mLineId;
		final int sessionCode = mWaitSession.mSessionCode;
		
		mCurrSession = new Session(mWaitSession);
		mCurrSession.mOperationState = Session.OP_NONE;
		mWaitSession = null;
		
		// callback to outside by main thread
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				Log.d(TAG, "[callback] answer method");
				onAnswerResponse(success, lineId, sessionCode, newConnection, errorCode);
		}});
	}
	
	private void processAnswer(final boolean success, final boolean newConnection, final int errorCode) {
		
		final int lineId = mWaitSession.mLineId;
		final int sessionCode = mWaitSession.mSessionCode;
		
		if (mNewIncomingState) {
			int ret = mC2CHandle.terminateConnection(mCurrSession.mLineId);
			if (ret < 0) Log.w(TAG, "[answer] terminate current connection fail (" + ret + ")");
			mCurrSession = null;
			mNewIncomingState = false;
			mWaitIncomingState = true;
			
		} else {
			mCurrSession = new Session(mWaitSession);
			mCurrSession.mOperationState = Session.OP_NONE;
			mWaitSession = null;
		}
		
		// callback to outside by main thread
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				Log.d(TAG, "[callback] answer method");
				onAnswerResponse(success, lineId, sessionCode, newConnection, errorCode);
		}});
	}

	
	private void processReject() {
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				
				int remainConnection = 0;
				if (mNewIncomingState) {
					remainConnection = 1;
					mNewIncomingState = false;
				}
				
				Log.d(TAG, "[callback] incoming state method");
				onRejectResponse(mWaitSession.mLineId, mWaitSession.mSessionCode, remainConnection);
				mWaitSession = null;
		}});
	}
	
	private void processAudioPlay(boolean opened, Session session, boolean reject) {
		Log.d(TAG, "[callback] audio play method");
		onAudioPlayResponse(session.mLineId, opened, reject);
		session.mOperationState = Session.OP_NONE; 
	}
	
	private void processIncomingState(boolean sameConnection) {
		
		final int lineId = mWaitSession.mLineId;
		final int sessionCode = mWaitSession.mSessionCode;
		
		if (!sameConnection)
			mNewIncomingState = true;
		
		// callback to outside by main thread
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				Log.d(TAG, "[callback] incoming state method");
				onIncomingState(lineId, sessionCode);	
		}});
	}
	
	private void processIncomingCancelled() {
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				
				int remainConnection = 0;
				if (mNewIncomingState) {
					remainConnection = 1;
					mNewIncomingState = false;
				}
				
				Log.d(TAG, "[callback] incoming cancelled");
				onIncomingCancelled(mWaitSession.mLineId, mWaitSession.mSessionCode, remainConnection);
				mWaitSession = null;
		}});
	}
	
	private void processTerminated() {
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				Log.d(TAG, "[callback] terminated");
				if (mCurrSession != null) {
					onTerminated(mCurrSession.mLineId);
					mCurrSession = null;
				} else if (mWaitSession != null) {
					onTerminated(mWaitSession.mLineId);
					mWaitSession = null;
				} else {
					Log.e(TAG, "expect state on process terminated");
				}
		}});
	}
	
	private void processMediaConnection() {
		
		final int lineId = mCurrSession.mLineId;
		final int sessionCode = mCurrSession.mSessionCode;
		
		// callback to outside by main thread
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				Log.d(TAG, "[callback] media connection method");
				onMediaConnection(lineId, sessionCode);
		}});
	}
	
	/**
	 * call {@link #start()} to register the listener.<br/>
	 * call {@link #stop()} to unregister the listener.
	 */
	private class C2CSDK extends C2CListener {
		
		private Session mIncomingSession, mBackupIncomingSession;
		
		@Override
		protected void receiveVideo(MediaFrame frame) {
			BaseVideoFrame v = (BaseVideoFrame) frame;
			int lineId = v.getLine();
			if (mWaitSession != null && mCurrSession == null) {
				if (lineId == mWaitSession.mLineId)
					onReceiveVideo(lineId, v);	
			} else if (mWaitSession == null && mCurrSession != null) {
				if (lineId == mCurrSession.mLineId)
					onReceiveVideo(lineId, v);
			} else if (mWaitSession != null && mCurrSession != null) {
				if (lineId == mWaitSession.mLineId)
					onReceiveVideo(lineId, v);
			} else {
				Log.w(TAG, "[receive video] Not any session.");
			}
		}

		@Override
		protected void receiveAudio(MediaFrame frame) {
			BaseAudioFrame a = (BaseAudioFrame) frame;
			int lineId = a.getLine();

			// check length, must be greater than 256 bytes.
			int len = a.getLength();
			if (len < 256) {
				Log.w(TAG, "[c2c receive audio] audio frame less than 256 bytes, drop the frame");
				Log.w(TAG, ">> frame length = " + len + " PT = " + a.getPayloadType() + ", id = " + a.getFrameId() + ", ts = " + a.getTimestamp());
				return;
			}

			// no exist connection
			if (mWaitSession != null && mCurrSession == null) {
//				if (lineId == mWaitSession.mLineId)
//					onReceiveAudio(lineId, a);

				// MUTE
					
			// exist one connection
			} else if (mWaitSession == null && mCurrSession != null) {
				if (lineId == mCurrSession.mLineId)
					onReceiveAudio(lineId, a);
				
			// new incoming and exist one connection
			} else if (mWaitSession != null && mCurrSession != null) {
				if (lineId == mWaitSession.mLineId)
					onReceiveAudio(lineId, a);
			} else {
				Log.w(TAG, "[receive audio] Not any session.");
			}
		}
		
		@Override
		protected synchronized void receiveMessage(C2CEvent event) {

			// get line ID
			int lineId = event.getLine();
Log.e(TAG, "c2c evnet, lineId = " + lineId +", event = " + event);
			/**
			 * Incoming event
			 */
			if (C2CEvent.C2C_INCOMING_STATE == event) {
					
				// check 'CallInfo'
				if (!event.hasCallInfo()) {
					
					// reject the incoming call
					int ret = mC2CHandle.rejectCurrentRequest(lineId);
					if (ret < 0)
						Log.w(TAG, "[c2c event] reject the incoming, could not get 'CallInfo', lineId = " + lineId);
					return;
				}
				
				// parse 'CallInfo'
				C2CCallInfo info = event.getCallInfo();
				String peerId = info.getPeerId();
				int sessionCode = Integer.valueOf(info.getCustomInfo().split("=")[1].split(";")[0]);

Log.e(TAG, "[DoorProcess] C2C INCOMING STATE >> get session code ="+sessionCode);
				
				// duplication call in abort
				if (mCurrSession != null && mCurrSession.mPeerId.equals(peerId)) {
					Log.e(TAG, "peerId duplication, peerId="+peerId);
					return;
				}


				// check busy
				if (mWaitSession != null && mWaitSession.mOperationState != Session.OP_ANSWER) {
					int ret = mC2CHandle.rejectCurrentRequest(lineId);
					if (ret < 0)
						Log.w(TAG, "[c2c event] reject the incoming, have a incoming event is processing, lineId = " + lineId);
					return;
				}
				
				// answer state
				if (mWaitIncomingState && mIncomingSession == null && mWaitSession != null && mBackupIncomingSession == null) {
					mIncomingSession = new Session(lineId, peerId, sessionCode);
					mBackupIncomingSession = mIncomingSession;
					
				// new call in state
				} else if (!mWaitIncomingState && mIncomingSession == null && mWaitSession == null && mBackupIncomingSession == null) {				
					mWaitSession = new Session(lineId, peerId, sessionCode);
					mBackupIncomingSession = mWaitSession;
					
				} else {
					
					// expected state
					if (mBackupIncomingSession == null) {
					
						Log.w(TAG, "[c2c incoming state] unexpected state!");
						Log.w(TAG, ">> mWaitIncomingState = " + mWaitIncomingState);
						Log.w(TAG, ">> mIncomingSession = " + mIncomingSession);
						Log.w(TAG, ">> mWaitSession = " + mWaitSession);
						
						// reject the incoming call
						int ret = mC2CHandle.rejectCurrentRequest(lineId);
						if (ret < 0)
							Log.w(TAG, "[c2c event] reject the incoming, state fail, lineId = " + lineId);
						
					// duplicated event
					} else {
						Log.d(TAG, "[c2c event] duplicated event");
					}
				}
				
			} else if (C2CEvent.C2C_INCOMING_ERROR == event) {
			
				if (mBackupIncomingSession != null)
					mBackupIncomingSession = null;
				
				if (event.hasSubEvent())
					Log.e(TAG, "[c2c event] incoming error, lineId = " + lineId + ", sub event = " + event.getSubEvent().getCode());
				else
					Log.e(TAG, "[c2c event] incoming error, lineId = " + lineId + ", no sub event");
					
				// answer state
				if (mWaitIncomingState && mIncomingSession == null && mWaitSession != null) {
					mIncomingSession = null;

				// new call in state				
				} else if (!mWaitIncomingState && mIncomingSession == null && mWaitSession != null) {
					mWaitSession = null;
				}
				
				// send notification to report the error
				//AppUtils.reportError(mActivity, AppUtils.ERR_C2C_INCOMING_ERROR, event.getSubEvent());
				
			/**
			 * Media transmission event
			 */
			} else if (C2CEvent.C2C_P2P_MODE == event) {
				
				if (mBackupIncomingSession != null)
					mBackupIncomingSession = null;
				
				// answer state
				if (mWaitIncomingState && mIncomingSession != null && mWaitSession != null) {
					
					mCurrSession = mIncomingSession;
					mWaitSession = null;
					mIncomingSession = null;
					mWaitIncomingState = false;
					Log.d(TAG, "[answer >> media ok] media transmission event (P2P), lineId = " + lineId);
					processMediaConnection();
					
				}  else {
					Log.w(TAG, "[c2c p2p mode] unexpected state!");
					Log.w(TAG, ">> mWaitIncomingState = " + mWaitIncomingState);
					Log.w(TAG, ">> mIncomingSession = " + mIncomingSession);
					Log.w(TAG, ">> mWaitSession = " + mWaitSession);
				}
			
			} else if (C2CEvent.C2C_RELAY_MODE == event) {
				
				if (mBackupIncomingSession != null)
					mBackupIncomingSession = null;
				
				// answer state
				if (mWaitIncomingState && mIncomingSession != null) {
					
					mWaitSession.mOperationState = Session.OP_NONE;
					mCurrSession = mWaitSession;
					mWaitSession = null;
					mIncomingSession = null;
					mWaitIncomingState = false;
					Log.d(TAG, "[answer >> media ok] media transmission event (RELAY), lineId = " + lineId);
					processMediaConnection();
					
				// new call in state
				} else if (!mWaitIncomingState && mIncomingSession == null && mWaitSession != null) {
					
					Log.d(TAG, "[incoming >> media ok] media transmission event (RELAY), lineId = " + lineId);
					processIncomingState(false);									
				
				} else {
					Log.w(TAG, "[c2c relay mode] unexpected state!");
					Log.w(TAG, ">> mWaitIncomingState = " + mWaitIncomingState);
					Log.w(TAG, ">> mIncomingSession = " + mIncomingSession);
					Log.w(TAG, ">> mWaitSession = " + mWaitSession);
				}
				
				/**
				 * Packet loss event
				 */
				} else if (C2CEvent.C2C_PACKET_LOSS == event) {
					
					Log.w(TAG, "[c2c event] occur packet loss"); 
					
					/**
					 * Terminate event
					 */
					} else if (C2CEvent.C2C_CALL_TERMINATED == event) {
						
						// process reject
						if (mWaitSession != null && lineId == mWaitSession.mLineId &&
							mWaitSession.mOperationState == Session.OP_REJECT) {
							processReject();
						
						// process incoming cancel
						} else if (mWaitSession != null && lineId == mWaitSession.mLineId &&
							mWaitSession.mOperationState == Session.OP_NONE) {
							processIncomingCancelled();

						} else if (mWaitSession == null && mCurrSession != null &&
							lineId == mCurrSession.mLineId) {
							processTerminated();
						
						} else if (mWaitSession != null && lineId == mWaitSession.mLineId &&
							mWaitSession.mOperationState == Session.OP_ANSWER){
							processTerminated();
						}
					
					} else if (C2CEvent.C2C_REQ_CANCELED == event) {
						
						if (mIncomingSession != null && lineId == mIncomingSession.mLineId) {
							Log.d(TAG, "incoming cancel, lineId="+lineId);
							processTerminated();
						} else {
							Log.e(TAG, "not catch the cancel event, lineId="+lineId);
						}


			
				/**
				 * Command error
				 */
				} else if (C2CEvent.C2C_COMMAND_ERROR == event) {
					
					// get error code
					int errorCode = 0;
					if (event.hasSubEvent())
						errorCode = event.getSubEvent().getCode();
				
					// process unlock
					if (mCurrSession != null && lineId == mCurrSession.mLineId &&
						mCurrSession.mOperationState == Session.OP_UNLOCK) {
						
						processUnlock(false, errorCode);
						
					} else if (mWaitSession != null && lineId == mWaitSession.mLineId &&
						mWaitSession.mOperationState == Session.OP_ANSWER) {
						processAnswer(false, true, errorCode);
					}
				}

		}
		
		@Override
		protected synchronized void receiveCommand(C2CChannel channel, C2CCommand command) {
			
			Log.w(TAG, "=========receive command===========");
			/**
			 * process RTP channel and String command
			 */
			if (channel instanceof RTPChannel && command instanceof StringCommand) {
				RTPChannel ch = (RTPChannel) channel;
				String tag = ((StringCommand) command).getTag();
				String cmd = ((StringCommand) command).getCommand();
				
				// get line ID
				int lineId = ch.getLine();

				/**
				 * PLAYBACK
				 */
				if (tag != null && tag.equals("PLAYBACK") && mCurrSession != null && lineId == mCurrSession.mLineId &&
					mCurrSession.mOperationState == Session.OP_AUDIO_PLAY) {
					
					if (cmd != null && cmd.equals("PLAYBACK_ON")) {
						mActivity.runOnUiThread(new Runnable() {
							public void run() {
								processAudioPlay(true, mCurrSession, false);
						}});
						
					} else if (cmd != null && cmd.equals("PLAYBACK_OFF")) {
						mActivity.runOnUiThread(new Runnable() {
							public void run() {
								processAudioPlay(false, mCurrSession, false);
						}});
					
					} else if (cmd != null && cmd.equals("PLAYBACK_REJECT")) {
						mActivity.runOnUiThread(new Runnable() {
							public void run() {
								processAudioPlay(false, mCurrSession, true);
						}});	
					} else {
						Log.e(TAG, "receive PLAYBACK command not match, cmd = " + cmd);
					}

					
				} else {
					Log.w(TAG, "receive other RTP channel String command, tag = " + tag + ", cmd = " + cmd);
				}
				
				/**
				 * process Protocol channel and String command
				 */
				} else if (channel instanceof ProtocolChannel && command instanceof StringCommand) {
					ProtocolChannel ch = (ProtocolChannel) channel;
					String cmd = ((StringCommand) command).getCommand();
				
					// get line ID
					int lineId = ch.getLine();
					
					// parse command to get tag
					String[] splits = cmd.split("=");
					String tag = splits[0];

					/**
					 * UNLOCK DONE
					 */
					if (tag.equals("UNLOCK_DONE") && mCurrSession != null && lineId == mCurrSession.mLineId &&
						mCurrSession.mOperationState == Session.OP_UNLOCK) {
						processUnlock(true, 0);

					/**
					 * UNLOCK ERROR
					 */
					} else if (tag.equals("UNLOCK_ERROR") && mCurrSession != null && lineId == mCurrSession.mLineId &&
						mCurrSession.mOperationState == Session.OP_UNLOCK) {
						int errorCode = 0;
						String[] s = splits[1].split(";");
						if (s.length > 1) {
							try {
								errorCode = Integer.valueOf(s[1]);
							} catch (NumberFormatException e) {
								errorCode = Integer.MIN_VALUE;
							}
						}
						processUnlock(false, errorCode);
							
						/**
						 * ANSWER ACK
						 */
						} else if (tag.equals("ANSWER_ACK") && mWaitSession != null && lineId == mWaitSession.mLineId &&
							mWaitSession.mOperationState == Session.OP_ANSWER) {
							
		Log.e("Hikari", "answer ack, " + mNewIncomingState + " peerId=" + mWaitSession.mPeerId);
		if (mCurrSession != null) {
		Log.e("Hikari", "answer ack, " + mCurrSession.mOperationState + " peerId=" + mWaitSession.mPeerId);				
		} else {
			Log.e("Hikari", "answer ack, mcurrsession=null");
		}

							if (mNewIncomingState)
								processAnswer(true, false, 0);
							else if (mCurrSession != null && mCurrSession.mOperationState == Session.OP_NONE) {
								processAnswerWithConnection(true, false, 0);
								processMediaConnection();
							}
							
						/**
						 * ANSWER ERROR	
						 */

					} else if (tag.equals("ANSWER_ERROR") && mWaitSession != null && lineId == mWaitSession.mLineId &&
						mWaitSession.mOperationState == Session.OP_ANSWER) {
						int errorCode = 0;
						String[] s = splits[1].split(";");
						if (s.length > 1) {
							try {
								errorCode = Integer.valueOf(s[1]);
							} catch (NumberFormatException e) {
								errorCode = Integer.MIN_VALUE;
							}
						}
						processAnswer(false, false, errorCode);
						
					/**
					 * RING
					 */
					} else if (tag.equals("RING") && mCurrSession != null && lineId == mCurrSession.mLineId &&
						mCurrSession.mOperationState == Session.OP_NONE && mWaitSession == null) {
						mWaitSession = new Session(mCurrSession);
						mWaitSession.mSessionCode = Integer.valueOf(splits[1].split(";")[0]);
						processIncomingState(true);

						
					} else if (tag.equals("RING") && mCurrSession != null &&
						(mCurrSession.mOperationState == Session.OP_NONE ||
						mCurrSession.mOperationState == Session.OP_AUDIO_PLAY) && mWaitSession == null) {
						int mLineId = mCurrSession.mLineId;
						int sessionCode = Integer.valueOf(splits[1].split(";")[0]);
						String peerId = mCurrSession.mPeerId;
						mWaitSession = new Session(mLineId, peerId, sessionCode);
						Log.e(TAG, "ring command, specify case!");
						Log.e(TAG, ">> lineId="+mLineId+" peerId="+peerId+" sessionCode="+sessionCode);
						processIncomingState(true);
						
					} else {
						try{
	Log.e(TAG, "ring command ? mCurrSession=" + mCurrSession + "[!null]");
	Log.e(TAG, "mCurrSession.lineId="+mCurrSession.mLineId+"[lineId=]"+lineId);
	Log.e(TAG, "mCurrSession.state="+mCurrSession.mOperationState+"[0|4]");
	Log.e(TAG, "mWaitSession="+mWaitSession+"[null]");
						

						Log.w(TAG, "receive other Protocol channel String command, cmd = " + cmd);
						Log.w(TAG, ">> lineId = " + lineId);
						}catch(Exception e)
						{
							Log.w("tecom", "sdk error......" );
							e.printStackTrace();
						}
					}
					
				/**
				 * Byte command
				 */
				} else {
					Log.w(TAG, "receive Byte command");
				}
		}
	}
	
	public synchronized int reject() throws OperationInvalidException {
		
		// check session
		if (mWaitSession == null) {
			throw new OperationInvalidException("[reject] Not any session to wait reject.");
		} else if (mWaitSession.mOperationState != Session.OP_NONE) {
			throw new OperationInvalidException("[reject] The session is busy to other operation.");
		} else {
			mWaitSession.mOperationState = Session.OP_REJECT;
		}
		
		// reject the incoming
		int lineId = mWaitSession.mLineId;
		int ret = mC2CHandle.terminateConnection(lineId);
		if (ret < 0) mWaitSession.mOperationState = Session.OP_NONE;
		Log.d(TAG, "[reject] return = " + ret + ", lineId = "+ lineId);
		return ret;
	}
	
	/**
	 * Answer door, the asynchronous method.<br/>
	 * Need to wait the response on onAnswerResponse callback method.
	 * @return Greater than or equal to 0 is success, the negative value is fail.
	 */
	public synchronized int answer() throws OperationInvalidException {
		
		// check session
		if (mWaitSession == null) {
			throw new OperationInvalidException("[answer] Not any session to wait answer.");
		} else if (mWaitSession.mOperationState != Session.OP_NONE) {
			throw new OperationInvalidException("[answer] The sesion is busy to other operation.");
		} else {
			mWaitSession.mOperationState = Session.OP_ANSWER;
		}
		
		// send command to answer door
		int lineId = mWaitSession.mLineId;
		int sessionCode = mWaitSession.mSessionCode;
		String cmdMsg = "ANSWER=" + String.valueOf(sessionCode) + ";";
		int ret = mC2CHandle.sendCommandByProtocolViaConnection(lineId, cmdMsg);
		if (ret < 0) mWaitSession.mOperationState = Session.OP_NONE;
		Log.d(TAG, "[answer] return = " + ret + ", lineId = " + lineId + ", sessionCode = " + sessionCode);
		return ret;
	}
	
	/**
	 * Unlock door, the asynchronous method.<br/>
	 * Need to wait the response on onUnlockResponse callback method.
	 * @return Greater than or equal to 0 is success, the negative value is fail.
	 */
	public synchronized int unlock() throws OperationInvalidException {

		// check session
		if (mCurrSession == null) {
			throw new OperationInvalidException("[unlock] Not any session to unlock door.");
		} else if (mCurrSession.mOperationState != Session.OP_NONE) {
			throw new OperationInvalidException("[unlock] The session is busy to other operation.");
		} else if (mCurrSession.mSessionCode < 0) {
			throw new OperationInvalidException("[unlock] The session code is negative. (" + mCurrSession.mSessionCode + ")");
		} else {
			mCurrSession.mOperationState = Session.OP_UNLOCK;
		}
		
		// send command to unlock door
		int lineId = mCurrSession.getLineId();
		int sessionCode = mCurrSession.getSessionCode();
		String cmdMsg = "UNLOCK=" + String.valueOf(sessionCode) + ";";
		int ret = mC2CHandle.sendCommandByProtocolViaConnection(lineId, cmdMsg);
		if (ret < 0) mCurrSession.mOperationState = Session.OP_NONE;
		Log.d(TAG, "[unlock] return = " + ret + ", lineId = " + lineId + ", sessionCode = " + sessionCode);
		return ret;
	}
	
	public synchronized int openAudioPlay() throws OperationInvalidException {
		
		// check session
		if (mCurrSession == null) {
			throw new OperationInvalidException("[open audio] Not any session to unlock door.");
		} else if (mCurrSession.mOperationState != Session.OP_NONE) {
			throw new OperationInvalidException("[open audio] The session is busy to other operation.");
		} else {
			mCurrSession.mOperationState = Session.OP_AUDIO_PLAY;
		}
		
		// send command to open door audio play
		int lineId = mCurrSession.mLineId;
		int ret = mC2CHandle.sendCommandByRtp(lineId, "PLAYBACK", "1");
		if (ret < 0) mCurrSession.mOperationState = Session.OP_NONE;
		Log.d(TAG, "[open audio] return = " + ret + ", lineId = " + lineId);
		return ret;
	}
	
	public synchronized int closeAudioPlay() throws OperationInvalidException {
		
		// check session
		if (mCurrSession == null) {
			throw new OperationInvalidException("[close audio] Not any session to unlock door.");
		} else if (mCurrSession.mOperationState != Session.OP_NONE) {
			throw new OperationInvalidException("[close audio] The session is busy to other operation.");
		} else {
			mCurrSession.mOperationState = Session.OP_AUDIO_PLAY;
		}
		
		// send command to close door audio play
		int lineId = mCurrSession.mLineId;
		int ret = mC2CHandle.sendCommandByRtp(lineId, "PLAYBACK", "0");
		if (ret < 0) mCurrSession.mOperationState = Session.OP_NONE;
		Log.d(TAG, "[close audio] return = " + ret + ", lineId = " + lineId);
		return ret;
	}
	
	/**
	 * start the door business model.
	 */
	public synchronized void start(Session session) {
		
		// register C2C listener
		mC2CListener = new C2CSDK();
		mC2CHandle.addListener(mC2CListener);
		
		if (session.mRing) {
		
			// ring mode
			mWaitSession = session;
			mWaitIncomingState = true;
			Log.d(TAG, "[start] ring mode");
			
		} else {
			
			// monitor mode
			mCurrSession = session;
			Log.d(TAG, "[start] monitor mode");
		}
	}

	/**
	 * stop the door business model.<br/>
	 * terminate all media session
	 */
	public synchronized void stop() {

		mWaitSession = null;
		mCurrSession = null;
		
		// unregister C2C listener
		mC2CHandle.removeListener(mC2CListener);
		mC2CListener = null;

		// stop all media session
		mC2CHandle.stopAllMediaSession();
		
		Log.d(TAG, "[stop] release all resource");
	}
	
	public int getSessionLineId() throws OperationInvalidException {
		
		// check session
		if (mCurrSession == null) {
			throw new OperationInvalidException("[line Id] Not any session to get line ID.");
		}
		
		return mCurrSession.getLineId();
	}
	
	public String getSessionPeerId() throws OperationInvalidException {
		
		// check session
		if (mCurrSession == null) {
			throw new OperationInvalidException("[peer Id] Not any session to get line ID.");
		}
		
		return mCurrSession.getPeerId();
	}
}
