package com.gocontrol.doorbell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.DoorProcess.OperationInvalidException;
import com.gocontrol.doorbell.datasource.DataHelper;
import com.gocontrol.doorbell.model.IProxSensor;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.model.IProxSensor.IProxSensorListener;
import com.gocontrol.doorbell.ui.v7.ImageTextButton;
import com.gocontrol.doorbell.utils.Utils;
import com.gocontrol.doorbell.utils.WebRtcAecManager;
import com.iptnet.android.audio.AudioStreamCapture;
import com.iptnet.android.audio.AudioStreamPlay;
import com.iptnet.c2c.BaseAudioFrame;
import com.iptnet.c2c.BaseVideoFrame;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.PayloadType;
import com.iptnet.codec.AudioCallback;
import com.iptnet.codec.AudioCodec;
import com.iptnet.codec.VideoCallback;
import com.iptnet.codec.VideoCodec;
import com.iptnet.ntilmpeg.mp4.extras.MP4AudioInfo;
import com.iptnet.ntilmpeg.mp4.extras.MP4Context;
import com.iptnet.ntilmpeg.mp4.extras.MP4Param;
import com.iptnet.ntilmpeg.mp4.extras.MP4VideoInfo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ViewerActivity extends Activity implements IProxSensorListener{

	private static final String TAG = ViewerActivity.class.getSimpleName();
	private Context mContext;
	private Handler mHandler;
	private Runnable runnable;
	private int timeCount = 0;  
	
	private ProgressBar mProgressBar;
	
	private RingEvent mRemoteRingEvent;
	private DoorHandle mDoorHandle;
	private TextView txtTitle;
	private boolean bellRing;
	private LinearLayout mViewerFunction;
	private boolean isSpeakerOn = true;
	private String peerID = "";
	private String doorName = "Doorbell";
	
	private MediaPlayer mMediaPlayer=null;	
	
	private boolean useWebRTCAec;
	private static final int DATA_LEN_PER_FRAME = 240;
	private static final int AEC_BUFFER_LEN = 8000; //500ms
	private boolean isAecCreated;
	private ByteCircleQueue aecBuffer;
	private byte[] bufferMicData;
	private long micDataCount;
	
	private TextView recordTxt;
	
	private int mSoftwareAec;
	
	private class DoorHandle extends DoorProcess {

		public DoorHandle(Activity activity, C2CHandle c2c) {
			super(activity, c2c);
		}

		@Override
		public void onUnlockResponse(boolean success, int lineId, int sessionCode, int errorCode) {
			
			// resume the button
			findViewById(R.id.button_unlock_door).setEnabled(true);
			((ToggleButton) findViewById(R.id.button_unlock_door)).setChecked(false);
			
			// unlock fail
			if (!success) {
				Toast.makeText(ViewerActivity.this, "Unlock command error (" + errorCode + ")", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onAnswerResponse(boolean success, int lineId, int sessionCode, boolean newConnection, int errorCode) {

			Log.d("Tecom", "1 onAnswerResponse line: " + lineId);
			// answer fail
			if (!success) {
				Toast.makeText(ViewerActivity.this, "Answer command error (" + errorCode + ")", Toast.LENGTH_SHORT).show();
			}
			
			Log.d("Tecom", "2 onAnswerResponse line: " + lineId);
		}

		@Override
		public void onRejectResponse(int lineId, int sessionCode, int remainConnection) {

			Log.d("Tecom", "1 onRejectResponse line: " + lineId);
			// only one connection, finish the activity
			if (remainConnection == 0) {
				Log.d(TAG,"onRejectResponse remainConnection == 0");
				if(mMediaPlayer != null){
					mMediaPlayer.release();
					mMediaPlayer = null;
				}
				if(bellRing){
					DataHelper.getInstance().AddUserInfoData("" + DataHelper.getInstance(ViewerActivity.this).getAllDataItems().getCount(), doorName, 
							peerID, AppUtils.createFileTimestamp(), 0);
				}else{
					DataHelper.getInstance().AddUserInfoData("" + DataHelper.getInstance(ViewerActivity.this).getAllDataItems().getCount(), doorName, 
							peerID, AppUtils.createFileTimestamp(), 1);
				}
				finish();
			
			// continue monitor door
			} else {
				openMonitorPanel();
			}
		}
		
		@Override
		public void onAudioPlayResponse(int lineId, boolean opened, boolean reject) {
			
			// resume the button
			findViewById(R.id.button_audio_send).setEnabled(true);
			
			// show reject message
			if (reject) {
				//Toast.makeText(ViewerActivity.this, "Playback Reject", Toast.LENGTH_SHORT).show();
				//((ToggleButton) findViewById(R.id.button_audio_send)).setChecked(false);
			}
		}


		@Override
		public void onIncomingState(int lineId, int sessionCode) {

			Log.d("Tecom", "1 onIncomingState line: " + lineId);
			// stop video recording
			ImageTextButton btn = (ImageTextButton) findViewById(R.id.button_video_recording);
			if (btn.isChecked()) {
				btn.setTag("");
				btn.performClick();
			}
			
			openRingPanel();
		}

		@Override
		public void onIncomingCancelled(int lineId, int sessionCode, int remainConnection) {
			
			Log.d("Tecom", "1 onIncomingCancelled line: " + lineId);
			// only one connection, finish the activity
			if (remainConnection == 0) {
				Log.d(TAG,"onIncomingCancelled remainConnection == 0");
				//Toast.makeText(ViewerActivity.this, "Terminated", Toast.LENGTH_SHORT).show();
				if(mMediaPlayer != null){
					mMediaPlayer.release();
					mMediaPlayer = null;
				}
				if(bellRing){
					DataHelper.getInstance().AddUserInfoData("" + DataHelper.getInstance(ViewerActivity.this).getAllDataItems().getCount(), doorName, 
							peerID, AppUtils.createFileTimestamp(), 0);
					ODPInfo one = ODPManager.getInstance().getOneODP(peerID);
					int num = one.getOdpMissedCallNum();
					num++;
					one.setOdpMissedCallNum(num);
					Log.d("tst", "the odp :" + peerID + "  " + "number:" + num);
				}else{
					DataHelper.getInstance().AddUserInfoData("" + DataHelper.getInstance(ViewerActivity.this).getAllDataItems().getCount(), doorName, 
							peerID, AppUtils.createFileTimestamp(), 1);
				}
				//add by tecom
				//mDoorHandle.stop();
				//end
				finish();
				
			// continue monitor door
			} else {
				openMonitorPanel();
			}
		}
		
		@Override
		public void onMediaConnection(int lineId, int sessionCode) {

			Log.d("Tecom", "1 onMediaConnection line: " + lineId);
			// unlock button
			//findViewById(R.id.ringing_answer).setEnabled(true);
			//findViewById(R.id.ringing_cancel).setEnabled(true);
			
			openMonitorPanel();
			
			//unlockRecordButton();

			
		}
		
		@Override
		public void onReceiveVideo(int lineId, BaseVideoFrame frame) {
			
			Log.v(TAG, "receive video, lineId = " + lineId + " frame len = " + frame.getLength());
			// get frame type
			PayloadType type = frame.getPayloadType();
			
			// check video codec is supported
			if (type != PayloadType.H264) {
				Log.e(TAG, "not supported video codec (" + type + ")");
				return;
			}
			
			// check video codec is initial
			if (mVideoCodec == null) {
				Log.w(TAG, "video codec is not initial");
				return;
			}
			
			// decode video frame
			byte[] buf = frame.getData();
			int len = frame.getLength();
			int payloadType = type.getCode();
			int frameType = frame.getFrameType();
			int timestamp = frame.getTimestamp();
			int frameId = frame.getFrameId();
			int fps = frame.getFPS();
			mVideoCodec.decode(buf, len, payloadType, frameType, timestamp, frameId, fps);
			
			// get video info for video recording
			if (mMP4VideoInfo == null && mMP4VideoWidth != 0 && mMP4VideoHeight != 0) {
				mMP4VideoInfo = new MP4VideoInfo(MP4Param.VCODEC_H264, mMP4VideoWidth, mMP4VideoHeight, fps);
			}
			
			// write frame to storage
			if (mMP4Context != null && mCodecInfoDetectTask.detectOk) {
				if (frameId == C2CHandle.FRAME_TYPE_P)
					mMP4Context.writeH264(buf, len, timestamp, false);
				else
					mMP4Context.writeH264(buf, len, timestamp, true);
			}
			
			runOnUiThread(new Runnable()
			{

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mProgressBar.setVisibility(View.GONE);
				}
				
			});
		}

		@Override
		public void onReceiveAudio(int lineId, BaseAudioFrame frame) {
			
			// check audio codec is initial
			if (mAudioCodec == null) {
				Log.w(TAG, "audio codec is not initial");
				return;
			}

			// decode audio frame
			byte[] buf = frame.getData();
			int len = frame.getLength();
			int payloadType = frame.getPayloadType().getCode();
			int timestamp = frame.getTimestamp();
			int frameId = frame.getFrameId();
			
			// get frame type
			PayloadType type = frame.getPayloadType();
						
			// process audio
			if (type == PayloadType.PCM_8K || type == PayloadType.PCM_11K || type == PayloadType.PCM_16K) {
				int sampleRate = 8000;
				if (mAudioPlay == null) {	
					/*Blocked by Tecom. Tecom always use 8k(sample rate 8000)
					if (type == PayloadType.PCM_11K)		sampleRate = 11025;
					else if (type == PayloadType.PCM_16K)	sampleRate = 16000;
					*/
					/*
					mAudioPlay = AudioStreamPlay.create(
						AudioStreamPlay.TYPE_SPEAKER, sampleRate, AUDIO_PLAY_CHAN, mAudioCapture.getAudioSessionId());
						*/
					mAudioPlay = AudioStreamPlay.create(
							AudioStreamPlay.TYPE_HANDSET, sampleRate, AUDIO_PLAY_CHAN);
					///
					mAudioPlay.play();
				}

				// push audio frame
				mAudioJitter.putBuffer(frame);
				
				// get audio info for video recording
				if (mMP4AudioInfo == null) {
					mMP4AudioInfo = new MP4AudioInfo(
						MP4Param.ACODEC_PCM, sampleRate,
						MP4AudioInfo.SAMPLE_SIZE_16_BITS,
						MP4AudioInfo.CHANNEL_MONO);
				}
				
				// write PCM frame to storage
				if (mMP4Context != null && mCodecInfoDetectTask.detectOk) {
					// [2016/04/25] change saving remote audio stream way
//					mMP4Context.writeLocalAudio(buf, len, timestamp, MP4Param.ACODEC_PCM);
					mMP4Context.writeAudio(buf, len, timestamp);
				}
				
			} else if (type == PayloadType.AAC) {
				mAudioCodec.decode(buf, len, payloadType, timestamp, frameId);

				// write AAC frame to storage [2016/04/25]
				if (mMP4Context != null && mCodecInfoDetectTask.detectOk) {
					mMP4Context.writeAdtsAAC(buf, len, timestamp);
				}
				
			} else {
				Log.e(TAG, "not supported audio codec (" + type + ")");
				return;
			}	

		}
		

		@Override
		public void onTerminated(int lineId) {
			Log.d(TAG, "terminated, finish activity");
			finish();
			//Toast.makeText(getApplicationContext(), "Terminated", Toast.LENGTH_SHORT).show();
		}

	}
	// display view
	private FrameLayout mContainer;
	private SurfaceView mDisplay;
	private SurfaceHolder mDisplayHolder;	
	private Rect mDisplayRect;
	
	// audio
	private final int AUDIO_SAMPLE_RATE = 8000;
	private final int AUDIO_PLAY_CHAN = AudioStreamPlay.CHANNEL_MONO;
	private final int AUDIO_CAPTURE_CHAN = AudioStreamCapture.CHANNEL_MONO;
	private AudioStreamPlay mAudioPlay;
	private AudioStreamCapture mAudioCapture;
	private AudioCaptureCallback mAudioSendProcess;
	private AudioManager mAudioManager;
	private JitterBuffer mAudioJitter;
	private int mAudioPlayGain;

	
	// C2C library
	private C2CHandle mC2CHandle;
	
	// codec
	private VideoCodec mVideoCodec;
	private AudioCodec mAudioCodec;
	private VideoDecoderCallback mVideoDecoderCallback;
	private AudioDecoderCallback mAudioDecoderCallback;
	
	// video recording
	private MP4Context mMP4Context;
	private MP4VideoInfo mMP4VideoInfo;
	private MP4AudioInfo mMP4AudioInfo;
	private int mMP4VideoWidth, mMP4VideoHeight;
	private File mMP4RecordFile;
	private CodecInfoDetectTask mCodecInfoDetectTask;

	protected int snapshotNumber = 1;
	
	private class JitterBuffer extends AudioJitterBuffer {

		@Override
		public void onBufferOut(BaseAudioFrame frame, int queue, int totalQueueTime) {
			try{
				//Log.i("webrtc_aec", "play voice data len = " + frame.getLength());
				
				if(useWebRTCAec == true && isAecCreated == true){
					for(int i=0; i<frame.getLength(); i++){
						aecBuffer.insert(frame.getData()[i]);
					}
					//Log.i("webrtc_aec", "Aec buffer length = " + aecBuffer.getSize() );
				}
				
				mAudioPlay.push(frame.getData(), 0, frame.getLength());
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//FileOutputStream fout1 ;
	//FileOutputStream fout2 ;
	private class AudioCaptureCallback implements AudioStreamCapture.Callback {

		@Override
		public void onAudioDataCapture(byte[] data, int length, int timestamp, int sequence, int remain) {
			
			// check reference is exist
			if (mC2CHandle == null) {
				Log.w(TAG, "c2c handle is not initial");
				return;
			}
			
			if(com.gocontrol.doorbell.utils.BuildConfig.MODIFY_PCM_DATA)
			{
				int degree = SystemConfigManager.getInstance().getMicVol();
				Utils.processMicVol(data, length, degree);
				
			}
					
			if (mMP4AudioInfo != null) {
				// send audio to remote
				int sampleRate = mMP4AudioInfo.getSampleRate();
				int code = PayloadType.PCM_8K.getCode();
				if (sampleRate == 11025)		code = PayloadType.PCM_11K.getCode();
				else if (sampleRate == 16000)	code = PayloadType.PCM_16K.getCode();

				if((isAecCreated == false) && (useWebRTCAec == true)){
					isAecCreated = true;
					// option:PJMEDIA_ECHO_USE_NOISE_SUPPRESSOR 0x80;
					// PJMEDIA_ECHO_AGGRESSIVENESS_CONSERVATIVE 0x100, aec_config.echoMode = 0,
					// PJMEDIA_ECHO_AGGRESSIVENESS_MODERATE 0x200, aec_config.echoMode = 2,
					// PJMEDIA_ECHO_AGGRESSIVENESS_AGGRESSIVE 0x300, aec_config.echoMode = 4,
					// range is 0 to 4, default is 3. 0 is conservative, 4 is aggressive.  
					// tail delay is hard to define, but it is extremely important.
					
					int ecOptions = 0x00;
					
					bufferMicData = new byte[DATA_LEN_PER_FRAME*2];
					
					WebRtcAecManager.getInstance().createWebRtcAec(sampleRate, 1, length, 30, ecOptions);
				}
				
				try {
					int lineId = mDoorHandle.getSessionLineId();
					
					if(useWebRTCAec == true){
						
						System.arraycopy(data, 0, bufferMicData, (int) (DATA_LEN_PER_FRAME*(micDataCount%2)), length);
						micDataCount++;
						
						if(micDataCount%2 == 0){
							//Log.i("webrtc_aec", "echo cancellation in process.");
							
							byte[]  bFarEndData = new byte[DATA_LEN_PER_FRAME*2];
							short[] sFarEndData = new short[DATA_LEN_PER_FRAME];
							
							byte[]  bOutputData = new byte[DATA_LEN_PER_FRAME*2];
							short[] sOutputData = new short[DATA_LEN_PER_FRAME];
						
							if(aecBuffer.getSize() >= DATA_LEN_PER_FRAME*2){
								for(int k=0; k<DATA_LEN_PER_FRAME*2; k++){
									bFarEndData[k] = aecBuffer.peekFront();
									aecBuffer.remove();
								}
							}else{
								Arrays.fill(bFarEndData, (byte) 0x00);
								Log.w("webrtc_aec", "not enough data in Aec buffer, use default data." );
							}
							
							Utils.preProcessVoiceAecData(bFarEndData, 0, DATA_LEN_PER_FRAME*2, sFarEndData);
							
							Utils.preProcessVoiceAecData(bufferMicData, 0, DATA_LEN_PER_FRAME*2, sOutputData);
							
							WebRtcAecManager.getInstance().startWebRtcAec(sOutputData, sFarEndData);
							
							Utils.postProcessVoiceAecData(sOutputData, DATA_LEN_PER_FRAME, bOutputData, 0);

							mC2CHandle.audioPutInData(lineId, bOutputData, DATA_LEN_PER_FRAME*2, timestamp, sequence, 64, code);
							
							// [2016/04/25] remove the local audio stream saving
//							if (mMP4Context != null)
//								mMP4Context.writeRemoteAudio(data, length, timestamp, MP4Param.ACODEC_PCM);					

						}

					}else{
						mC2CHandle.audioPutInData(lineId, data, length, timestamp, sequence, 64, code);
						
						if (mMP4Context != null)
							mMP4Context.writeRemoteAudio(data, length, timestamp, MP4Param.ACODEC_PCM);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					Log.w(TAG, "could not to get line ID");
				}
			}
		}
	}
	
	private class VideoDecoderCallback implements VideoCallback {

		private final Bitmap.Config BITMAP_CONFIG = Config.ARGB_8888;
		private Bitmap mDisplayBitmap;
		private int mDisplayBitmapWidth, mDisplayBitmapHeight;
		
		public Bitmap getScreenBitmap() {
			if (mDisplayBitmap != null)
				return mDisplayBitmap.copy(BITMAP_CONFIG, true);
			return null;
		}
		
		@Override
		public void onVideoDecoded(byte[] buf, int len, int width, int height, int requestInitial) {
			
			
//			Log.e("Hikari", "video decoded , len = " + len + " w (" + width + ") h (" + height + ") r (" + requestInitial + ")");
			
			// check draw screen size
			if (mDisplayRect == null) {
				Log.w(TAG, "rect is not initial");
				return;
			}
			
			// initialize Bitmap object
			if (mDisplayBitmap == null) {
				mDisplayBitmapWidth = width;
				mDisplayBitmapHeight = height;
				mDisplayBitmap = Bitmap.createBitmap(width, height, BITMAP_CONFIG);

			// recreate Bitmap when width or height are changed
			} else if (mDisplayBitmapWidth != width || mDisplayBitmapHeight != height) {
				mDisplayBitmap.recycle();
				mDisplayBitmapWidth = width;
				mDisplayBitmapHeight = height;
				mDisplayBitmap = Bitmap.createBitmap(width, height, BITMAP_CONFIG);
			}
			
			// raw buffer copy to Bitmap object
			mDisplayBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(buf, 0, len));
			
			// draw Bitmap
			Canvas canvas = mDisplayHolder.lockCanvas();
			if (canvas == null) {
				Log.w(TAG, "lock canvas fail");
				return;
			}
			android.graphics.Matrix mx = new Matrix();
			RectF rfs = new RectF(0, 0, width, height);
			RectF rfd = new RectF(0, 0, mDisplay.getWidth(),
					mDisplay.getHeight());
			mx.setRectToRect(rfs, rfd, Matrix.ScaleToFit.CENTER);
			canvas.drawBitmap(mDisplayBitmap, mx, null);
			// canvas.drawBitmap(mDisplayBitmap, null, mDisplayRect, null);
			mDisplayHolder.unlockCanvasAndPost(canvas);
			
			// get width and height for video recording
			mMP4VideoWidth = width;
			mMP4VideoHeight = height;		
			
			//auto take snapshot when incoming call. added by welly
			new AutoTakeShot().execute();
		}
	}

	private class AutoTakeShot extends AsyncTask<Void, Void, Void>
	{

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			if (snapshotNumber == 1 && bellRing == true) {
				Log.d("Tecom", "autoTakeShotWhenIncoming... ");
				while (snapshotNumber <= 4) {
					try {
						Log.d("Tecom",
								"autoTakeShotWhenIncoming... snapshotNumber:"
										+ snapshotNumber);
						takeSnapshot(mVideoDecoderCallback,
								mRemoteRingEvent.getPeerId());
						Thread.sleep(150);
					} catch (Exception e) {
						e.printStackTrace();
					}

					snapshotNumber++;
				}
			}
			System.gc();
			return null;
		}
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onCancelled(java.lang.Object)
		 */
		@Override
		protected void onCancelled(Void result) {
			// TODO Auto-generated method stub
			super.onCancelled(result);
		}
				
	}
	
	private class AudioDecoderCallback implements AudioCallback {

		@Override
		public void onAudioDecoded(short[] buf, int len, int timestamp, int sr, int ch_num) {
			
			if (mAudioPlay == null) {		
				int channel = AudioStreamPlay.CHANNEL_MONO;
				if (ch_num == 2) channel = AudioStreamPlay.CHANNEL_STEREO;
				/*
				mAudioPlay = AudioStreamPlay.create(
					AudioStreamPlay.TYPE_SPEAKER, sr, channel, mAudioCapture.getAudioSessionId());
					*/
				mAudioPlay = AudioStreamPlay.create(
						AudioStreamPlay.TYPE_HANDSET, sr, channel, mAudioCapture.getAudioSessionId());
				////
				
				mAudioPlay.play();
				
				// get audio info for video recording
				if (mMP4AudioInfo == null) {
					mMP4AudioInfo = new MP4AudioInfo(
							MP4Param.ACODEC_AAC, sr,
						MP4AudioInfo.SAMPLE_SIZE_16_BITS,
						ch_num);
				}
			}				
			mAudioPlay.push(buf, 0, len);
		}
	}
	
	private class CodecInfoDetectTask extends Thread {
		
		boolean detectOk = false;
		
		@Override
		public void run() {
			
			MP4Context context = MP4Context.getInstance();
			context.encoderInit(); // default record one file on same time
			mMP4Context = context;
			detectOk = false;
			while (!interrupted()) {

				// sleep 1 second
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
				
				// setting parameter
				if (mMP4VideoInfo != null && mMP4AudioInfo != null) {
					context.setVideoParam(mMP4VideoInfo);
					context.setAudioParam(mMP4AudioInfo);
					detectOk = true;
					interrupt();
				}
			}
			
			if (detectOk) {
				context.createFile(mMP4RecordFile.toString(), mMP4VideoInfo, mMP4AudioInfo);
				Log.d(TAG, "codec information detect ok");
			} else
				Log.d(TAG, "codec information detect end");
		}
	}
	
	private class ByteCircleQueue {
		private int itemCount;// 队中元素个数
		private int front = 0;// 队头
		private int rear = 0;// 队尾
		private final byte[] queueArray;
		private int maxSize = 0;

		public ByteCircleQueue(int caption) {
	        this.maxSize = caption;  
	        this.itemCount = 0;  
	        this.front = 0;  
	        this.rear = 0;  
	        this.queueArray = new byte[caption];  
	    }  
	  
	    public void insert(byte j) {  
	        this.queueArray[this.rear++] = j;  
	        this.itemCount++;
	        if (this.rear == this.maxSize) {  
	            this.rear = 0;  
	        }
	    }  
	  
	    public byte remove() {  
	        byte temp = this.queueArray[this.front++];  
	        if (this.front == this.maxSize) {  
	            this.front = 0;  
	        }  
	        this.itemCount--;
	        return temp;  
	    }  
	  
	    public byte peekFront() {  
	        return this.queueArray[this.front];  
	    }
	    
	    public int getSize(){
	    	return (rear-front+maxSize)%maxSize;
	    }
	}  
	
	/** 
	* @Title: showRingUI 
	* @Description: ��ʾ����״̬��UI����
	* @param:  null
	* @return: void 
	* @throws 
	*/
	private void openRingPanel(){
		mViewerFunction.setVisibility(View.INVISIBLE);
		findViewById(R.id.imgBtn_snapshot).setVisibility(View.VISIBLE);
		try{
			findViewById(R.id.ring_id).setVisibility(View.VISIBLE);
			findViewById(R.id.ring_answer_id).setVisibility(View.GONE);	
		}catch(Exception e)
		{
			LinearLayout mViewerRinging = (LinearLayout)this.findViewById(R.id.viewer_ringing);
			mViewerRinging.setVisibility(View.VISIBLE);
			mViewerRinging.findViewById(R.id.view_left).setVisibility(View.INVISIBLE);			
			mViewerRinging.findViewById(R.id.ringing_answer).setVisibility(View.VISIBLE);
			TextView tmp = (TextView) mViewerRinging.findViewById(R.id.ringing_answer_txt);
			if(tmp  != null)
				tmp.setVisibility(View.VISIBLE);
		}
	}

	private void openMonitorPanel(){		
		mViewerFunction.setVisibility(View.VISIBLE);
		findViewById(R.id.imgBtn_snapshot).setVisibility(View.INVISIBLE);
		try{
			findViewById(R.id.ring_id).setVisibility(View.GONE);
			findViewById(R.id.ring_answer_id).setVisibility(View.VISIBLE);	
		}catch(Exception e)
		{
			LinearLayout mViewerRinging = (LinearLayout)this.findViewById(R.id.viewer_ringing);
			mViewerRinging.setVisibility(View.VISIBLE);
			mViewerRinging.findViewById(R.id.view_left).setVisibility(View.GONE);			
			mViewerRinging.findViewById(R.id.ringing_answer).setVisibility(View.GONE);
			Button mbutton =  (Button)(mViewerRinging.findViewById(R.id.ringing_cancel));
			mbutton.setText(R.string.end);
			TextView tmp = (TextView) mViewerRinging.findViewById(R.id.ringing_answer_txt);
			if(tmp  != null)
				tmp.setVisibility(View.GONE);
		}
		//lockRecordButton();

	}
	
	private void lockRecordButton() {
		ImageTextButton v = (ImageTextButton) findViewById(R.id.button_video_recording);
		v.setEnabled(false);
	}
	
	private void unlockRecordButton() {
		ImageTextButton v = (ImageTextButton) findViewById(R.id.button_video_recording);
		v.setEnabled(true);
	}

	
	public void onButtonByAnswer(View view) {

		bellRing = false;
		// lock button
		view.setEnabled(false);
		//findViewById(R.id.ringing_cancel).setEnabled(false);
		
		// answer to door
		try {
			int ret = mDoorHandle.answer();
			if (ret < 0) Toast.makeText(this,getString(R.string.accept) + getString(R.string.failed)+"(" + ret + ")", Toast.LENGTH_SHORT).show();
			openMonitorPanel();
			/*incoming �����SMPĬ�����農������Monitor���農��.
			����Call UIĬ��Speaker��Answer������SPecҪ��Handfreeģʽ
			*/					
			ImageTextButton v = (ImageTextButton) findViewById(R.id.button_audio_send);
			v.setImgResource(R.drawable.odp_mc_btn_v7);
			v.setChecked(false);
			isMuteOn = true;
			mAudioCapture.start();	
			/*
			new Handler().postDelayed(new Runnable(){    
			    public void run() {    
			    //execute the task   
			    	// start audio recording
				mAudioCapture.start();		
				ImageTextButton v = (ImageTextButton) findViewById(R.id.button_audio_send);
				v.setChecked(false);
			    }    
			 }, 1500);   
*/
			
			//end
		} catch (OperationInvalidException e) {
			Toast.makeText(this, getString(R.string.failed_reason_network), Toast.LENGTH_SHORT).show();
		}
		
		if(mMediaPlayer != null){
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		
	}

	public void onButtonByCancel(View view) {
		Log.d("tecom", "onButtonByCancel ...... 1");
		if(bellRing){
			try {
				Log.d("tecom", "onButtonByCancel ...... 2");
				int ret = mDoorHandle.reject();
				if (ret < 0) Toast.makeText(this, getString(R.string.cancel) + getString(R.string.failed)+"(" + ret + ")", Toast.LENGTH_SHORT).show();
			} catch (OperationInvalidException e) {
				Toast.makeText(this,  getString(R.string.failed_reason_network), Toast.LENGTH_SHORT).show();
			}
		}else{
			DataHelper.getInstance().AddUserInfoData("" + DataHelper.getInstance(ViewerActivity.this).getAllDataItems().getCount(), doorName, 
					peerID, AppUtils.createFileTimestamp(), 1);
			Log.d("tecom", "onButtonByCancel ...... 4");
			mDoorHandle.stop();
			finish();
			Log.d("tecom", "onButtonByCancel ...... 5");
		}
		Log.d("tecom", "onButtonByCancel ...... 6");
	}
	
	public void onButtonByRecording(View view) {
		ImageTextButton v = (ImageTextButton) view;
		String doorId;
		
		if (v.isChecked()) {
			// get index
			int index = Integer.MIN_VALUE;
			try {
				v.setBackgroundResource(R.color.btn_bg_press_color);
				String peerId = mDoorHandle.getSessionPeerId();
				doorId = peerId;
				try {
					String tmp[] = Utils.splitString(doorId, "@");
					doorId = tmp[0];
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					doorId = peerId;
				}
				index = Door.getIndexById(this, peerId);
				if (index < 0) {
					Toast.makeText(this, "Could not to get door index, not to do recording", Toast.LENGTH_SHORT).show();
					v.setChecked(false);
					v.setBackgroundResource(R.color.btn_bg_color);
					return;
				}				
			} catch (OperationInvalidException e) {
				Toast.makeText(this, "Could not to get peer ID, not to do recording", Toast.LENGTH_SHORT).show();
				v.setChecked(false);
				v.setBackgroundResource(R.color.btn_bg_color);
				return;
			}
			
			// get file path			
			//String path = "door" + String.valueOf(index) + File.separator + "clips";
			String path = doorId + File.separator + "clips";
			String fileName = AppUtils.createFileTimestamp() + ".mp4";
			mMP4RecordFile = new File(AppUtils.getExternalFileDir(ViewerActivity.this, path), fileName);
			
			// start video recording		
			if (mCodecInfoDetectTask == null) {
				mCodecInfoDetectTask = new CodecInfoDetectTask();
				mCodecInfoDetectTask.start();
			}
			
			//update the record time textview.
			if(recordTxt != null)
			{
				recordTxt.setText(mContext.getString(R.string.nortek_record_time) + Utils.secToTime(0));
				recordTxt.setVisibility(View.VISIBLE);
				mHandler.postDelayed(runnable, 1000);  
			}
		} else {
			
			v.setBackgroundResource(R.color.btn_bg_color);
			// stop video recording
			MP4Context context = mMP4Context;
			mMP4Context = null;
			context.writeDone();
			context.encoderRelease();
			
			// add the file to storage index
			String fileName = mMP4RecordFile.toString();
			AppUtils.mediaScan(ViewerActivity.this, mMP4RecordFile);
			mMP4RecordFile = null;
			
			if (mCodecInfoDetectTask != null) {
				mCodecInfoDetectTask.interrupt();
				mCodecInfoDetectTask = null;
			}
			
			if (v.getTag() == null) {
				// show dialog
				new AlertDialog.Builder(this)
					.setMessage(getString(R.string.save_to) + fileName.toString())
					.setCancelable(false)
					.setPositiveButton(getString(R.string.ok), null)
					.show();
			} else {
				
				v.setTag(null);
				
				// show toast
				Toast.makeText(this, getString(R.string.save_to) + fileName.toString(), Toast.LENGTH_SHORT).show();
			}
			//update the record time textview.
			if(recordTxt != null)
			{
				recordTxt.setVisibility(View.INVISIBLE);
				mHandler.removeCallbacks(runnable);
				mHandler.removeCallbacksAndMessages(null);
				timeCount = 0;
			}
		}
	}
	public void onButtonBySnapshot1(View view) {
		final ImageButton v = (ImageButton) view;
			v.setEnabled(false);
			
			String peerId;
			try {
				peerId = mDoorHandle.getSessionPeerId();
			} catch (OperationInvalidException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				if(mRemoteRingEvent != null)
					peerId = mRemoteRingEvent.getPeerId();
				else
					peerId = "";
				Log.d("tst", "onButtonBySnapshot1:" + peerId);
			}
			final String doorId = peerId;
			// start thread to save
			new Thread(new Runnable() {
				public void run() {
			
					String tmpDoorId = doorId;
					// get screen bitmap
					Bitmap bitmap = mVideoDecoderCallback.getScreenBitmap();
					if (bitmap == null) {
						v.post(new Runnable() {
							public void run() {
								Toast.makeText(ViewerActivity.this, "Snapshot fail", Toast.LENGTH_SHORT).show();
								v.setEnabled(true);
						}});
						return;
					}
					
					// get file path
					try {
						String tmp[] = Utils.splitString(doorId, "@");
						tmpDoorId = tmp[0];
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						tmpDoorId = doorId;
					}
					String path = tmpDoorId + File.separator + "snapshots";
					String fileName = AppUtils.createFileTimestamp() + ".jpg";
					final File file = new File(AppUtils.getExternalFileDir(ViewerActivity.this, path), fileName);
					boolean saveOk = false;
					try {
						
						// saving file to storage
						saveOk = bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(file));
						
						// add the file to storage index
						AppUtils.mediaScan(ViewerActivity.this, file);
						
					} catch (FileNotFoundException e) {
						saveOk = false;
					} finally {
						v.setTag(saveOk);
					}
					
					v.post(new Runnable() {
						public void run() {
							
							// change button status
							v.setEnabled(true);
							//v.setChecked(false);
							
							// get save status
							boolean saveOk = (Boolean) v.getTag();
							String msg = "";
							if (saveOk) {
								msg = getString(R.string.save_to) + file.toString();
							} else {
								msg = getString(R.string.save_failed);
							}
							
							// show dialog
							new AlertDialog.Builder(ViewerActivity.this)
								.setMessage(msg)
								.setCancelable(false)
								.setPositiveButton(getString(R.string.ok), null)
								.show();
					}});
					
			}}).start();
	}
	
	public void onButtonBySnapshot(View view) {
		final ImageTextButton v = (ImageTextButton) view;
		
		String peerId;
		try {
			peerId = mDoorHandle.getSessionPeerId();
		} catch (OperationInvalidException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if(mRemoteRingEvent != null)
				peerId = mRemoteRingEvent.getPeerId();
			else
				peerId = "";
			Log.d("tst", "onButtonBySnapshot:" + peerId);
		}
		final String doorId = peerId;
		
		if (v.isChecked()) {
			v.setEnabled(false);
			
			// start thread to save
			new Thread(new Runnable() {
				public void run() {
			
					String tmpDoorId = doorId;
					// get screen bitmap
					Bitmap bitmap = mVideoDecoderCallback.getScreenBitmap();
					if (bitmap == null) {
						v.post(new Runnable() {
							public void run() {
								Toast.makeText(ViewerActivity.this, "Snapshot fail", Toast.LENGTH_SHORT).show();
								v.setEnabled(true);
								v.setChecked(false);
						}});
						return;
					}
					
					// get file path
					try {
						String tmp[] = Utils.splitString(doorId, "@");
						tmpDoorId = tmp[0];
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						tmpDoorId = doorId;
					}
					String path = tmpDoorId + File.separator + "snapshots";
					String fileName = AppUtils.createFileTimestamp() + ".jpg";
					final File file = new File(AppUtils.getExternalFileDir(ViewerActivity.this, path), fileName);
					boolean saveOk = false;
					try {
						
						// saving file to storage
						saveOk = bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(file));
						
						// add the file to storage index
						AppUtils.mediaScan(ViewerActivity.this, file);
						
					} catch (FileNotFoundException e) {
						saveOk = false;
					} finally {
						v.setTag(saveOk);
					}
					
					v.post(new Runnable() {
						public void run() {
							
							// change button status
							v.setEnabled(true);
							v.setChecked(false);
							
							// get save status
							boolean saveOk = (Boolean) v.getTag();
							String msg = "";
							String title = "";
							if (saveOk) {
								title = getString(R.string.nortek_save_successful);
								msg = getString(R.string.save_to) + file.toString();
							} else {
								title = getString(R.string.save_failed);
								msg = getString(R.string.nortek_save_failed);
							}
							
							// show dialog
							new AlertDialog.Builder(ViewerActivity.this)
								.setTitle(title)
								.setMessage(msg)
								.setCancelable(false)
								.setPositiveButton(getString(R.string.ok), null)
								.show();
					}});
					
			}}).start();
		}
	}
	
	public void onButtonByAudioMute(View view) {
		ImageTextButton v = (ImageTextButton) view;
		
		//speaker �� handset �л�
		if (v.isChecked()) {
			v.setImgResource(R.drawable.odp_mute_btn_normal_v7);
			v.setText(getString(R.string.audio_play_mute));
			isSpeakerOn = true;
			mAudioManager.setSpeakerphoneOn(true);

		} else {
			isSpeakerOn = false;
			v.setImgResource(R.drawable.odp_mute_btn_v7);
			v.setText(getString(R.string.audio_play_handset));
			mAudioManager.setSpeakerphoneOn(false);

		}
	}
	
	public void onButtonByAudioSend(View view) {
		ImageTextButton v = (ImageTextButton) view;
		if (!v.isChecked()) {				
			v.setImgResource(R.drawable.odp_mc_btn_v7);
			mAudioCapture.start();		
			isMuteOn = true;
		} else {				
			v.setImgResource(R.drawable.odp_mc_btn_normal_v7);
			mAudioCapture.stop();
			isMuteOn = true;
			isAecCreated = false;
		}
	}
	
	public void onButtonByUnlockDoor(View view) {
		ToggleButton v = (ToggleButton) view;
		if (v.isChecked()) {
			v.setEnabled(false);
			try {
				int ret = mDoorHandle.unlock();
				if (ret < 0) {
					Toast.makeText(this, "Unlock Fail (" + ret + ")", Toast.LENGTH_SHORT).show();
				}
				
			} catch (OperationInvalidException e) {
				Toast.makeText(this, "Unlock Invalid", Toast.LENGTH_SHORT).show();
				v.setEnabled(true);
				v.setChecked(false);	
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {

			// terminate all connection
			mDoorHandle.stop();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		
		Log.d("Tecom", "Mic degree:" + SystemConfigManager.getInstance().getMicVol());		
		Log.d(TAG, "viewer activity onCreate called!!");

		// initialize view
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// get parameter from Intent
		RingEvent mRingEvent = getIntent().getParcelableExtra("event");
		mRemoteRingEvent = mRingEvent;
		DoorProcess.Session session;
		if (mRingEvent == null) {
			
			// monitor mode
			Intent intent = getIntent();
			Door door = (Door) intent.getSerializableExtra("door");
			int lineId = intent.getIntExtra("line.id", Integer.MIN_VALUE);
			int sessionCode = intent.getIntExtra("session.code", Integer.MIN_VALUE);
			session = new DoorProcess.Session(lineId, door.getId(), false, sessionCode);
			
			//added by tecom. store the peerID;
			peerID = door.getId();
			//doorName = door.getODPName();
			ODPInfo one = ODPManager.getInstance().getOneODP(peerID);
			if(one != null)
				doorName = one.getOdpName();
			//openMonitorPanel();
			Log.d(TAG, "monitor mode, lineId = " + lineId + " sessionCode = " + sessionCode);
					
		} else {
			bellRing = true;
			// ring mode
			Door door = Door.read(this, mRingEvent.getPeerId());
			int lineId = mRingEvent.getLineId();
			int sessionCode = mRingEvent.getSessionCode();
			
			Log.d(TAG, "ring mode, cur lineId = " + lineId + " sessionCode = " + sessionCode);
			
			session = new DoorProcess.Session(lineId, door.getId(), sessionCode);
			
			//added by tecom. store the peerID;
			peerID = door.getId();
			//doorName = door.getODPName();
			ODPInfo one = ODPManager.getInstance().getOneODP(peerID);
			if(one != null)
				doorName = one.getOdpName();
			//openRingPanel();
		}
		
		// create and start door handle
		mDoorHandle = new DoorHandle(this, C2CHandle.getInstance());
		mDoorHandle.start(session);

		initUI();
		if (mRingEvent != null) {
			int current_ring_index = getSharedPreferences("ring", 0).getInt(
					"current_ring_index", 0);
			Log.d(TAG, "current_ring_index = " + current_ring_index);
			
			if( current_ring_index == 0)
				mMediaPlayer = MediaPlayer.create(this,
					R.raw.ringtone_60seconds_1);
			if( current_ring_index == 1)
				mMediaPlayer = MediaPlayer.create(this,
					R.raw.ringtone_60seconds_2);
			if( current_ring_index == 2)
				mMediaPlayer = MediaPlayer.create(this,
					R.raw.ringtone_60seconds_3);
			if(mMediaPlayer != null)
			if (!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
				mMediaPlayer.setLooping(true);
			}

		}
		// initialize audio
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		mAudioManager.setSpeakerphoneOn(true);

		try {
			mAudioCapture = AudioStreamCapture.create(
					AudioStreamCapture.SOURCE_VOIP, AUDIO_SAMPLE_RATE,
					AUDIO_CAPTURE_CHAN, AudioStreamCapture.FRAME_SIZE_240);

			mAudioSendProcess = new AudioCaptureCallback();
			mAudioCapture.setRecordListener(mAudioSendProcess);
			mAudioJitter = new JitterBuffer();
			mAudioJitter.enabled(true);
			mAudioJitter.setQueueTime(300);
			mAudioJitter.start();

			// check audio record HW
			if (mAudioCapture.getState() != AudioStreamCapture.STATE_INITIALIZED) {
				mAudioCapture.release();
				Toast.makeText(this, "Audio recording HW initial fail",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
		
		
		getSFData();

		// Initial hardware AEC
		AppUtils.enableEchoCancellation(mAudioCapture.getAudioSessionId());
		AppUtils.enableHTCEC(mAudioManager);

		if(mSoftwareAec == 1){
			useWebRTCAec = true;
			aecBuffer = new ByteCircleQueue(AEC_BUFFER_LEN);
			Log.i("webrtc_aec", "Choose to use webRTC AEC.");
		}else{
			useWebRTCAec = false; 
			Log.i("webrtc_aec", "Choose not to use webRTC AEC.");
		}
		
		isAecCreated = false;
		micDataCount = 0;
	
		// register C2C listener
		mC2CHandle = C2CHandle.getInstance();
				
		// initialize codec
		mVideoCodec = VideoCodec.getInstance();
		mVideoCodec.initial();
		mVideoDecoderCallback = new VideoDecoderCallback();
		mVideoCodec.setVideoCallback(mVideoDecoderCallback);
		mAudioCodec = AudioCodec.getInstacne();
		mAudioCodec.initial();
		mAudioDecoderCallback = new AudioDecoderCallback();
		mAudioCodec.setAudioCallback(mAudioDecoderCallback);	
			
		((AppApplication) getApplication()).setManualCall(false, null);		
		/*for auto test.
		if(bellRing)
		{
			//for auto test.
			new Handler().postDelayed(new Runnable()
			{

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Button t = (Button) findViewById(R.id.ringing_answer);
					
					if(t != null)
						t.performClick();
				}
				
			}, ((new Random().nextInt())%6 + 1)  * 1000);
		}*/

		mHandler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				timeCount++;
				if(recordTxt != null)
					recordTxt.setText(mContext.getString(R.string.nortek_record_time) + Utils.secToTime(timeCount));
				mHandler.postDelayed(this, 1000);
			}
		};
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();		
		
		mHandler.removeCallbacksAndMessages(null);
		
		// stop door handle
		mDoorHandle.stop();
		
		// check video recording
		if (mMP4Context != null) {
			findViewById(R.id.button_video_recording).performClick();
		}
		// check close all connection
		mC2CHandle.stopAllMediaSession();
		RingEventProcess.getInstance().connectionTerminated();
		
		// disable AEC
		AppUtils.disableEchoCancellation();
	
		// unitialize audio
		//mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
		if (mAudioPlay != null) {
			if (mAudioPlay.getState() == AudioStreamPlay.STATE_INITIALIZED)
				mAudioPlay.release();
		}
		mAudioCapture.setRecordListener(null);
		mAudioSendProcess = null;
		mAudioCapture.release();
		mAudioJitter.stop();
		
		// uninitialize codec
		if (mVideoCodec != null) {
			mVideoCodec.setVideoCallback(null);
			mVideoDecoderCallback = null;
			mVideoCodec.release();
			mVideoCodec = null;
			mAudioCodec.setAudioCallback(null);
			mAudioDecoderCallback = null;
			mAudioCodec.release();
			mAudioCodec = null;
		}
	
		((AppApplication) getApplication()).setManualCall(false, null);
		if(mMediaPlayer != null){
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		
		snapshotNumber = 1;
		
		System.gc();
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if(IProxSensor.proxSensorNear)
		{
			onProximitySensorChanged(true);
		}
		else
		{
			onProximitySensorChanged(false);
		}
	}

	/* (non-Javadoc)
	 * @see com.tecom.door.model.IProxSensor.IProxSensorListener#onProximitySensorChanged(boolean)
	 */
	@Override
	public void onProximitySensorChanged(boolean near) {
		// TODO Auto-generated method stub
		Utils.onProxSensorChangeView(ViewerActivity.this, near, false);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		IProxSensor.addProxSensorListener(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		IProxSensor.delProxSensorListener(this);
	}
	private void initUI(){
		// initialize view
		final ActionBar actionBar = getActionBar();  
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  
        actionBar.setCustomView(R.layout.actionbar_v7);
		txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);		
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(doorName);			
		txtTitle.setText(spanBuilder);
		
		ImageView btnBack = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mDoorHandle.stop();
			if(bellRing){
				DataHelper.getInstance().AddUserInfoData("" + DataHelper.getInstance(ViewerActivity.this).getAllDataItems().getCount(), doorName, 
						peerID, AppUtils.createFileTimestamp(), 0);
			}else{
				DataHelper.getInstance().AddUserInfoData("" + DataHelper.getInstance(ViewerActivity.this).getAllDataItems().getCount(), doorName, 
						peerID, AppUtils.createFileTimestamp(), 1);
			}
			finish();
		}
		});
		setContentView(R.layout.viewer_v7);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mViewerFunction = (LinearLayout) findViewById(R.id.viewer_function);		
		mContainer = (FrameLayout) findViewById(R.id.container);
		LayoutParams params = (LayoutParams) mContainer.getLayoutParams();
		mDisplay = (SurfaceView)mContainer.findViewById(R.id.surface);
		mDisplayHolder = mDisplay.getHolder();
		
		
		((ImageTextButton)findViewById(R.id.button_video_recording)).setChecked(isCaptureVideo);
		if(isCaptureVideo)
		{
			((ImageTextButton)findViewById(R.id.button_video_recording)).setBackgroundResource(R.color.btn_bg_press_color);
		}else
		{
			((ImageTextButton)findViewById(R.id.button_video_recording)).setBackgroundResource(R.color.btn_bg_color);
		}
		if (isMuteOn) {				
			((ImageTextButton)findViewById(R.id.button_audio_send)).setImgResource(R.drawable.odp_mc_btn_v7);
			((ImageTextButton)findViewById(R.id.button_audio_send)).setChecked(false);			
		} else {				
			((ImageTextButton)findViewById(R.id.button_audio_send)).setImgResource(R.drawable.odp_mc_btn_normal_v7);
			((ImageTextButton)findViewById(R.id.button_audio_send)).setChecked(true);			
		}
		if(isSpeakerOn)
		{
			ImageTextButton v = (ImageTextButton)findViewById(R.id.button_mute);
			if(v != null){
				v.setChecked(true);
				v.setImgResource(R.drawable.odp_mute_btn_normal_v7);
				v.setText(getString(R.string.audio_play_mute));
			}
		}else{
			ImageTextButton v = (ImageTextButton)findViewById(R.id.button_mute);
			if(v != null){
				v.setChecked(false);
				v.setImgResource(R.drawable.odp_mute_btn_v7);
				v.setText(getString(R.string.audio_play_handset));
			}
		}
		
		
		// get display rect
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d(TAG, "landspace widthPixels = " + outMetrics.widthPixels);
			Log.d(TAG, "landspace heightPixels = " + outMetrics.heightPixels);
			int width = outMetrics.widthPixels*6/7;
			int height = width * 9 / 16; // default 16:9 ratio
			mDisplayRect = new Rect(0, 0, width, height);
			//below is unused anymore
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.d(TAG, "portrait");
			int width = outMetrics.widthPixels;
			int height = width * 9 / 16; // default 16:9 ratio
			mDisplayRect = new Rect(0, 0, width, height);
			params.width = width;
			params.height = height;
		}
		if (bellRing) {
			openRingPanel();
		}else{
			openMonitorPanel();
		}
		
		mProgressBar = (ProgressBar) this.findViewById(R.id.processBar);
		recordTxt = (TextView)this.findViewById(R.id.record_time);
		
		if(isCaptureVideo)
		{
			recordTxt.setVisibility(View.VISIBLE);
		}else
		{
			recordTxt.setVisibility(View.INVISIBLE);
		}
	}
	boolean isCaptureVideo = false, isMuteOn = false;
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "onConfigurationChanged");
		isCaptureVideo = ((ImageTextButton)findViewById(R.id.button_video_recording)).isChecked();
		Log.d(TAG, "onConfigurationChanged isCaptureVideo = " + isCaptureVideo);
		isMuteOn = !((ImageTextButton)findViewById(R.id.button_audio_send)).isChecked();
		Log.d(TAG, "onConfigurationChanged isMuteOn = " + isMuteOn);
		Log.d(TAG, "onConfigurationChanged isSpeakerOn = " + isSpeakerOn);
		initUI();
	}
	
	/*
	 * �Ե�ǰ��Ƶ��ͼ
	 */
	public void takeSnapshot( VideoDecoderCallback videoDecoderCallback, String mDoorID)
	{
		if(videoDecoderCallback == null)
		{
			Log.d("Tecom", "takeSnapshot ... videoDecoderCallback null ...");
			return;
		}
		// get screen bitmap
		Bitmap bitmap = videoDecoderCallback.getScreenBitmap();
		if (bitmap == null) {
			Log.d("Tecom", "takeSnapshot ... bitmap null ...");
			return;
		}
		String tmpDoorId;
		try {
			String tmp[] = Utils.splitString(mDoorID, "@");
			tmpDoorId = tmp[0];
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			tmpDoorId = mDoorID;
		}
		// get file path
		String path = tmpDoorId + File.separator + "snapshots";
		String fileName = AppUtils.createFileTimestamp() + ".jpg";
		final File file = new File(AppUtils.getExternalFileDir(ViewerActivity.this, path), fileName);
		
		try {
			
			// saving file to storage
			bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(file));
			
			// add the file to storage index
			AppUtils.mediaScan(ViewerActivity.this, file);
			
		} catch (FileNotFoundException e) {
			Log.d("Tecom", "takeSnapshot ...failed ...");
		} finally {
			if(bitmap != null && !bitmap.isRecycled())
			{
				bitmap = null;
			}
		}
		
	}
	
	private void getSFData(){
		SharedPreferences pref = getSharedPreferences("cloud2door_aec", MODE_PRIVATE);
		mSoftwareAec = pref.getInt("software_aec", 0);
	}
}
