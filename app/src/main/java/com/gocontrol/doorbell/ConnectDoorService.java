package com.gocontrol.doorbell;

import java.util.Random;

import com.iptnet.c2c.C2CChannel;
import com.iptnet.c2c.C2CCommand;
import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.C2CListener;
import com.iptnet.c2c.C2CSubEvent;
import com.iptnet.c2c.ProtocolChannel;
import com.iptnet.c2c.StringCommand;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.util.Log;

public class ConnectDoorService extends IntentService {

	private static final String TAG = ConnectDoorService.class.getSimpleName();

	private abstract class C2C extends C2CListener {
	
		// connect step
		public static final int REASON_SEND_FAIL = -101;
		public static final int REASON_SESSION_CODE_FAIL = -102;
		public static final int REASON_FORMAT_FAIL = -103;
		public static final int REASON_TIMEOUT = -104;

		// response success
		public static final int REASON_INVALID_SESSION_CODE = -204;
		public static final int REASON_APP_UID_NOT_FOUND = -205;
		public static final int REASON_DOOR_STATE_FAIL = -206;
		public static final int REASON_UNKNOWN_DOOR_FAIL = -207;
		
		// command error
		public static final int REASON_CMD_ERR_UNKNOWN = -308;
		public static final int REASON_CMD_ERR_UNAUTHORIZED = -309;
		public static final int REASON_CMD_ERR_BUSY = -310;
		public static final int REASON_CMD_ERR_OFFLINE = -311;
		public static final int REASON_CMD_ERR_NO_RSP = -312;
		public static final int REASON_CMD_ERR_FORBIDDEN = -313;
		
		private int mLineId = Integer.MIN_VALUE;
		private int mSessionCode = Integer.MIN_VALUE;
		
		public abstract void onSuccess(int sessionCode);
		public abstract void onFail(int reason);
		
		public void sendCallbackCommand(String id, String acc, String pwd, int sessionCode) {
		
			// register listener
			C2CHandle c2c = C2CHandle.getInstance();
			c2c.addListener(this);
			
			// get command text
			mSessionCode = sessionCode;
			String command = "CALLBACK=" + String.valueOf(sessionCode) + ";";
		
			// send command
			int ret = mLineId = c2c.sendCommandByProtocol(id, acc, pwd, command);
			if (ret < 0) {
				Log.w(TAG, "send command to door fail, return (" + mLineId + ")");
				Log.v(TAG, ">> id = " + id + ", acc = " + acc + ", pwd = " + pwd);
				Log.v(TAG, ">> command = " + command);
				onFail(REASON_SEND_FAIL);
				
				// command end
				sendCommandEnd();
				
			} else {
				Log.d(TAG, "send command to door success, return (" + ret + ")");
			}
		}
		
		private void sendCommandEnd() {
			
			// resume state
			mLineId = mSessionCode = Integer.MIN_VALUE;
			C2CHandle.getInstance().removeListener(this);
		}
		
		@Override
		protected void receiveCommand(C2CChannel channel, C2CCommand command) {
			if (channel instanceof ProtocolChannel && command instanceof StringCommand) {
				ProtocolChannel pCh = (ProtocolChannel) channel;
				String cmdText = ((StringCommand) command).getCommand();
				PROCESS: {
					
					// check lineId
					if (mLineId != pCh.getLine()) {
						Log.w(TAG, "lineId no match, target = " + mLineId + ", current = " + pCh.getLine());
						break PROCESS;
					}
					
					// processing success response
					String[] splits = cmdText.split("=");
					if (splits.length > 1) {
						String tag = splits[0];
						String cmdParam = splits[1];
						if (tag.equals("CALLBACK_ACK")) {
							try {
								String[] params = cmdParam.split(";");
								String code = params[0];
								int sessionCode = Integer.valueOf(code);
								if (sessionCode ==  mSessionCode) {
									Log.d(TAG, "response command success, ack sessionCode = " + mSessionCode);
									onSuccess(mSessionCode);
									
								} else {
									Log.w(TAG, "response session code not match, target = " + mSessionCode + ", current = " + sessionCode);
									onFail(REASON_SESSION_CODE_FAIL);
								}
								
							} catch (NumberFormatException e) {
								Log.w(TAG, "response command not match, command = " + cmdText);
								onFail(REASON_FORMAT_FAIL);
							}
							
						} else if (tag.equals("CALLBACK_ERROR")) {
							try {
								String[] params = cmdParam.split(";");
								if (params.length > 1) {
									int sessionCode = Integer.valueOf(params[0]);
									int errorCode = Integer.valueOf(params[1]);
									if (sessionCode == mSessionCode) {
										Log.d(TAG, "response command success, sessionCode = " + mSessionCode + ", errorCode = " + errorCode);
										switch (errorCode) {
											case 400:	onFail(REASON_INVALID_SESSION_CODE);	break;
											case 404:	onFail(REASON_APP_UID_NOT_FOUND);		break;
											case 486:	onFail(REASON_DOOR_STATE_FAIL);			break;
											default:	onFail(REASON_UNKNOWN_DOOR_FAIL);		break;
										}
										
									} else {
										Log.w(TAG, "response session code not match, target = " + mSessionCode + ", current = " + sessionCode);
										onFail(REASON_SESSION_CODE_FAIL);
									}
									
								} else {
									Log.w(TAG, "response command not match, command = " + cmdText);
									onFail(REASON_FORMAT_FAIL);	
								}
								
							} catch (NumberFormatException e) {
								Log.w(TAG, "response command not match, command = " + cmdText);
								onFail(REASON_FORMAT_FAIL);
							}
							
						} else {
							Log.w(TAG, "response command not match, command = " + cmdText);
							onFail(REASON_FORMAT_FAIL);
						}
					
					// command no match
					} else {
						Log.w(TAG, "response command no match, command = " + cmdText);
						onFail(REASON_FORMAT_FAIL);
					}
				}
				
				// command end
				sendCommandEnd();
			}
		}

		@Override
		protected void receiveMessage(C2CEvent event) {

			if (event.getLine() == mLineId && event == C2CEvent.C2C_COMMAND_ERROR) {
				
				if (event.hasSubEvent()) {
					C2CSubEvent sub = event.getSubEvent();
					switch (sub) {
						case C2C_UNAUTHORIZED:
							Log.w(TAG, "response command error, unauthorized");
							onFail(REASON_CMD_ERR_UNAUTHORIZED);
							break;
							
						case C2C_REMOTE_BUSY:
							Log.w(TAG, "response command error, remote busy");
							onFail(REASON_CMD_ERR_BUSY);
							break;
							
						case C2C_REMOTE_UNREACHED:
							Log.w(TAG, "response command error, remote offline");
							onFail(REASON_CMD_ERR_OFFLINE);
							break;
						
						case C2C_REMOTE_NO_RESP:
							Log.w(TAG, "response command error, remote no response");
							onFail(REASON_CMD_ERR_NO_RSP);
							break;
							
						case C2C_FORBIDDEN:
							Log.w(TAG, "response command error, forbidden");
							onFail(REASON_CMD_ERR_FORBIDDEN);
							break;
							
						default:
							Log.w(TAG, "response command error, unknown sub event (" + sub.getCode() + ")");
							onFail(REASON_CMD_ERR_UNKNOWN);
					}
					
				} else {
					Log.w(TAG, "response command error, no sub event");
					onFail(REASON_CMD_ERR_UNKNOWN);
				}
				
				// command end
				sendCommandEnd();
			}
		}
	};
	
	private void sendNotificationToShowFail(int errorCode, String text) {
		Notification.Builder build = new Notification.Builder(this);
		build.setSmallIcon(android.R.drawable.ic_delete);
		build.setAutoCancel(true);
		build.setOngoing(false);
		build.setContentTitle("Connect Door Fail (" + String.valueOf(errorCode) + ")");
		build.setContentText(text);
		
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(
			ConnectDoorService.class.getSimpleName(),
			new Random().nextInt(), build.build());
			
	}
	
	public ConnectDoorService() {
		super(ConnectDoorService.class.getSimpleName());
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {

		final RingEvent event = intent.getParcelableExtra("ring.event");
		
		// event is null, send notification to show fail message.
		if (event == null) {
			Log.e(TAG, "could not to get RingEvent, operation is stoped");
			return;
		}
		
		// wait register done
		int timeoutTimes = 0;
		while (true) {
			boolean regOk = C2CHandle.getInstance().isRegistrationDone();
			if (regOk) {
				break;
			} else {
				try {
					Log.d(TAG, "wait register done ...");
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
			}
			timeoutTimes++;
			if (timeoutTimes > 300) {
				// send notification to show fail
				sendNotificationToShowFail(C2C.REASON_TIMEOUT, "Timeout, not send 'CALLBACK' command");
				return;
			}
		}
		Log.d(TAG, "start send 'CALLBACK' command");

		// send command to door
		Door door = Door.read(this, event.getPeerId());
		String id = door.getId();
		String acc = door.getAccount();
		String pwd = door.getPassword();
		int sessionCode = event.getSessionCode();		
		new C2C() {
			
			// response success
			public void onSuccess(int sessionCode) {
				// need not to implement
				Log.d(TAG, "response command success, sessionCode = " + sessionCode);
			}
			
			// response fail
			public void onFail(int reason) {
				
				String text = "";
				
				switch (reason) {
				// connect step
				case REASON_SEND_FAIL:				text = "Send command fail";		break;
				case REASON_SESSION_CODE_FAIL:		text = "Session code fail";		break;
				case REASON_FORMAT_FAIL:			text = "Command format fail";	break;
				
				// response success
				case REASON_INVALID_SESSION_CODE:	text = "Invalid session code (400)";	break;
				case REASON_APP_UID_NOT_FOUND:		text = "APP UID not found (404)";	break;
				case REASON_DOOR_STATE_FAIL:		text = "Door state fail (486)";	break;
				case REASON_UNKNOWN_DOOR_FAIL:		text = "Unknown door fail";	break;
				
				// command error
				case REASON_CMD_ERR_UNKNOWN:		text = "Command error";	break;
				case REASON_CMD_ERR_UNAUTHORIZED:	text = "Unauthorized";	break;
				case REASON_CMD_ERR_BUSY:			text = "Door busy";			break;
				case REASON_CMD_ERR_OFFLINE:		text = "Door offline";		break;
				case REASON_CMD_ERR_NO_RSP:			text = "Door no response";	break;
				case REASON_CMD_ERR_FORBIDDEN:		text = "Forbidden";			break;
			}

				
				// send notification to show fail
				//sendNotificationToShowFail(reason, text);
			}
			
		}.sendCallbackCommand(id, acc, pwd, sessionCode);
	}
}
