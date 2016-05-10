package com.gocontrol.doorbell.ui.v7;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.gocontrol.doorbell.AppApplication;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.LogUtils;
import com.gocontrol.doorbell.utils.Utils;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MediaActivity extends Activity implements OnItemClickListener,ScanMediaFileListener {

	private static final String TAG = MediaActivity.class.getSimpleName();
	private static final int threadCount = Runtime.getRuntime().availableProcessors();
	private String index;
	private int type;
	private List<String> mFileNameList = new ArrayList<String>();
	//private ArrayAdapter<String> mAdapter;
	private MediaInfoListAdapter imgAdapter;
	private TextView txtTitle;
	private List<Map<String, Object>> list;
	private ListView listView;
	private Button delAll;
	private TextView time,duration;
	private GetThumbnailTask getTask = null;
	private ThreadPoolExecutor threadPool;
	private DrawerLayout mDrawerLayout = null;
	private ListView mLvRightMenu;
	
	private Context mContext;
	private String doorName;
	
	private ProgressDialog proDialog;
	private static Handler mHandler;
	
	private final static int START_LOG_OUT = 1000;
	private final static int LOG_OUT_DONE = 1001;
	private final static int LOG_OUT_ERROR = 1002;
	private final static int POP_UP_SURE_LOG_OUT = 1003;
	private final static int LOG_OUT_TIME_OUT = 1004;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if( !EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().register(this);
		
		// get index from previous actvitiy
		Intent intent = getIntent();
		index = intent.getStringExtra("index");
		type = intent.getIntExtra("type", 0);
		doorName = intent.getStringExtra("door_name");
		
		Log.d("Vincent","type = " + type);
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
		ActionBar.LayoutParams.MATCH_PARENT,
		ActionBar.LayoutParams.MATCH_PARENT,
		Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar_v7, null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		ImageView btnBack = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
		});
		ImageView btnMenu = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_menu);
		btnMenu.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mDrawerLayout != null)
			{
				if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
					mDrawerLayout.closeDrawers();
				else
					mDrawerLayout.openDrawer(Gravity.RIGHT);
			}
		}
		});
		btnMenu.setVisibility(View.VISIBLE);
		
		imgAdapter = new MediaInfoListAdapter(this,getData());
		//Log.d("tst", "====" + imgAdapter.getCount());
		
		imgAdapter.registerDataSetObserver(new DataSetObserver()
		{

			/* (non-Javadoc)
			 * @see android.database.DataSetObserver#onChanged()
			 */
			@Override
			public void onChanged() {
				// TODO Auto-generated method stub
				super.onChanged();
				if(imgAdapter.isEmpty())
				{
					updateUIWhenNoItem();
				}
			}

			/* (non-Javadoc)
			 * @see android.database.DataSetObserver#onInvalidated()
			 */
			@Override
			public void onInvalidated() {
				// TODO Auto-generated method stub
				super.onInvalidated();
			}
			
			
		});
		setContentView(R.layout.mediainfo);
		getWindow().setBackgroundDrawable(null) ;
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLvRightMenu = (ListView) findViewById(R.id.right_drawer);   
	
		setUpDrawer();
		listView = (ListView)findViewById(R.id.list);
		listView.setAdapter(imgAdapter);
		listView.setOnItemClickListener(this);
		
		time = (TextView)findViewById(R.id.txt_time);
		duration = (TextView)findViewById(R.id.txt_duration);
		
		if(imgAdapter.isEmpty())
		{			
			updateUIWhenNoItem();
		}
		
		delAll = (Button)findViewById(R.id.btn_del_all);
		delAll.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				dialog();
			}
		});
		if(type == 0){
			txtTitle.setText(getString(R.string.dlg_select_pictures));
			//time.setVisibility(View.INVISIBLE);
			//duration.setVisibility(View.INVISIBLE);
		}else{
			txtTitle.setText(getString(R.string.dlg_select_clips));
		}
		threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount); 
		//threadPool =(ThreadPoolExecutor) Executors.newCachedThreadPool();
		Log.d(TAG,"getCorePoolSize = " + threadPool.getCorePoolSize());
		Log.d(TAG,"getMaximumPoolSize = " + threadPool.getMaximumPoolSize());
		getTask =  new GetThumbnailTask();
		getTask.execute("");
		
		mHandler = new Handler()
		{

			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what)
				{
				case START_LOG_OUT:
					SystemConfigManager.getInstance().setLogoutState(1);
					int ret = C2CHandle.getInstance().resetAllNotification();
					//Log.d("tst", "logout==============ret:" + ret);
					if(ret < 0)
					{
						SystemConfigManager.getInstance().setLogoutState(0);
						mHandler.sendEmptyMessage(LOG_OUT_ERROR);
					}else{
						proDialog = android.app.ProgressDialog.show(mContext, mContext.getString(R.string.ntut_tip_11),
								getString(R.string.tecom_precess_content));
						proDialog.setCancelable(true);
						mHandler.sendEmptyMessageDelayed(LOG_OUT_TIME_OUT, 10*1000);
					}
					break;
				case LOG_OUT_DONE:
					if(proDialog != null)
						proDialog.dismiss();
					
					SystemConfigManager.getInstance().setAppAutoLogin(2);
					SystemConfigManager.getInstance().saveAppAutoLogin(mContext);
					//Toast.makeText(mContext, mContext.getString(R.string.log_out_suc), Toast.LENGTH_SHORT).show();
					//startActivity(new Intent(mContext, UserLoginUI.class));
					
					finish();
					break;
				case LOG_OUT_ERROR:
					if(proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, mContext.getString(R.string.log_out_error), Toast.LENGTH_SHORT).show();
					break;
				case POP_UP_SURE_LOG_OUT:
					popupTipDialog();
					break;
				case LOG_OUT_TIME_OUT :
					if(proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, mContext.getString(R.string.ntut_tip_12), Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
		};
		
	}
	

	/**
	 * @param imgAdapter2
	 */
	private void updateUIWhenNoItem() {		// TODO Auto-generated method stub
		
		TextView tv = (TextView) findViewById(R.id.no_log_tip);
		if (tv != null) {
			
			tv.setVisibility(View.VISIBLE);
			tv.setText(R.string.nortek_pic_no);
			if (type == 1) {
				
				tv.setText(R.string.nortek_clips_no);
			}
			
			Button tmp = (Button) findViewById(R.id.btn_del_all);
			if (tmp != null) {
				tmp.setVisibility(View.INVISIBLE);
			}
		}

	}


	protected void popupTipDialog() {
    	AlertDialog.Builder builder = new Builder(mContext);
    	builder.setTitle(R.string.system_logout);
    	builder.setMessage(R.string.sure_to_delete_logout);
    	builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(START_LOG_OUT);
			}
    		
    	});
    	builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    	builder.create().show();
	}
	
	private void setUpDrawer()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mLvRightMenu.addHeaderView(inflater.inflate(R.layout.header_just_username, mLvRightMenu, false));
        mLvRightMenu.setAdapter(new MenuItemAdapter(this));
        
        TextView userAcc = (TextView) mLvRightMenu.findViewById(R.id.id_username);
        TextView showAcc = (TextView) mLvRightMenu.findViewById(R.id.id_showname);
        if(userAcc != null)
        	userAcc.setText(LocalUserInfo.getInstance().getC2cAccount());
        if(showAcc != null)
        	showAcc.setText(LocalUserInfo.getInstance().getLocalName());
    }
	 void shutdownAndAwaitTermination(ExecutorService pool) {
		 Log.d("Vincent","shutdownAndAwaitTermination11");
		 pool.shutdown(); // Disable new tasks from being submitted    
		 try {     
			 // Wait a while for existing tasks to terminate     
			 if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				 Log.d("Vincent","shutdownAndAwaitTermination22");
				 pool.shutdownNow(); // Cancel currently executing tasks       
				 // Wait a while for tasks to respond to being cancelled       
				 if (!pool.awaitTermination(60, TimeUnit.SECONDS))           
					 Log.e("Vincent","Pool did not terminate");          
				}
		 }catch (InterruptedException ie) {     
			 Log.e("Vincent","shutdownAndAwaitTermination33");
					 // (Re-)Cancel if current thread also interrupted   
					 pool.shutdownNow();      
					 // Preserve interrupt status      
					 Thread.currentThread().interrupt();    
		 }
	 }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(threadPool != null){
			threadPool.shutdown();
		}
		if(getTask != null){
			getTask.stop();
			getTask.cancel(true);
		}
		if(threadPool != null){
			threadPool.shutdownNow();
		}
		EventBus.getDefault().unregister(this);
	}


	public void onEvent(Object event){
		
		if( event instanceof C2CEvent)
		{
			Log.d("Tecom", ((C2CEvent) event).getInfo());
			if(C2CEvent.C2C_SETUP_ERROR == event){
				mHandler.removeMessages(LOG_OUT_TIME_OUT);
				mHandler.sendEmptyMessage(LOG_OUT_ERROR);
			}else if(C2CEvent.C2C_SETUP_DONE == event)
			{
				if(SystemConfigManager.getInstance().getLogoutState() == 1){
					mHandler.removeMessages(LOG_OUT_TIME_OUT);
					mHandler.sendEmptyMessage(LOG_OUT_DONE);
				}
				
				
			}
		}
		
	}

	/**
	 * 删除某个文件夹下的所有文件夹和文件
	 * @param delpath
	 * @return
	 */
	public  boolean deleteFile(String delpath) {
		File file = new File(delpath);
		if (!file.isDirectory()) {
			Log.d(TAG,"delete is file");
			file.delete();
		}else if (file.isDirectory()) {
			Log.d(TAG,"delete is directory");
			File[] fileList = file.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				File delfile = fileList[i];
				if (!delfile.isDirectory()) {
					delfile.delete();
				} else if (delfile.isDirectory()) {
					deleteFile(fileList[i].getPath());
				}
			}
			file.delete();
		}
		return true;
	}
	private List<Map<String, Object>> getData() {
		// get file list from storage
		String door = index;
		try {
			String tmp[] = Utils.splitString(door, "@");
			door = tmp[0];
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			door = index;
		}
		String type = "snapshots";
		if (this.type == 1) type = "clips";
		
		Log.d("Vincent","path = " + door + File.separator + type);
		File dir = new File(AppUtils.SD_PATH + "/" + AppUtils.APP_NAME, door + File.separator + type);
		if (dir.exists()) {
			Log.d("Vincent","dir.exists()");
			
			// get directory
			mFileNameList.clear();
			String[] files = dir.list();
			String[] revFiles = new String[files.length];
			for(int i = 0;i < files.length;i++){
				revFiles[i] = files[files.length - i - 1];
			}
			mFileNameList.addAll(Arrays.asList(revFiles));
			
		} else {
			Log.d("Vincent","!dir.exists()");
			
			// directory is not existing
			dir.mkdirs();
			AppUtils.mediaScan(this, dir);
		}
        list = new ArrayList<Map<String, Object>>();
        for(String name:mFileNameList){
        	Map<String, Object> map = new HashMap<String, Object>();
        	Log.d("Vincent","name = " + name);
	        map.put(MediaInfoListAdapter.KEY_TIME, name.substring(0, name.length() - 4));//4 == ".mp4".length()/".jpg".length()
	        map.put(MediaInfoListAdapter.KEY_DOOR_NAME, doorName);
	        map.put(MediaInfoListAdapter.KEY_PATH, dir.getAbsolutePath() + File.separator + name);
	        Log.d("tecom", "========" + dir.getAbsolutePath() + File.separator + name);
	        list.add(map);
        }
        return list;
        }  
	@Override
	protected void onResume() {
		super.onResume();
		
		// update list
		//mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		String duration = (String)list.get(position).get(MediaInfoListAdapter.KEY_DURATION);
		if("00:00".equalsIgnoreCase(duration))
		{
			Toast.makeText(this, getString(R.string.open_file_fail_1), Toast.LENGTH_SHORT).show();
			return;
		}
		// get file
		String fileName = mFileNameList.get(position);
		String door = index;
		try {
			String tmp[] = Utils.splitString(door, "@");
			door = tmp[0];
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			door = index;
		}
		String type = "snapshots";
		if (this.type == 1) type = "clips";
		//File file = new File(getExternalFilesDir(null), door + File.separator + type + File.separator + fileName);
		File file = new File(AppUtils.SD_PATH + "/" + AppUtils.APP_NAME, door + File.separator + type +  File.separator + fileName);
		// get mime type
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
			MimeTypeMap.getFileExtensionFromUrl((Uri.fromFile(file).toString())));
		
		// start correspond activity
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), mimeType);
			Log.d("Tecom", Uri.fromFile(file).toString());
			startActivity(intent);

		} catch (ActivityNotFoundException e) {
			
			// not start activity
			Toast.makeText(this, getString(R.string.open_file_fail_1), Toast.LENGTH_SHORT).show();
		}
	}
	/**
	 * 根据指定的图像路径和大小来获取缩略图
	 * 此方法有两点好处：
	 *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
	 *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
	 *        用这个工具生成的图像不会被拉伸。
	 * @param imagePath 图像的路径
	 * @param width 指定输出图像的宽度
	 * @param height 指定输出图像的高度
	 * @return 生成的缩略图
	 */
	private Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// ��ȡ���ͼƬ�Ŀ�͸ߣ�ע��˴���bitmapΪnull
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // ��Ϊ false
		// �������ű�
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
		be = beWidth;
		} else {
		be = beHeight;
		}
		if (be <= 0) {
		be = 1;
		}
		options.inSampleSize = be;
		// ���¶���ͼƬ����ȡ���ź��bitmap��ע�����Ҫ��options.inJustDecodeBounds ��Ϊ false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// ����ThumbnailUtils����������ͼ������Ҫָ��Ҫ�����ĸ�Bitmap����
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
		ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	return bitmap;
	}


	/**
	* ��ȡ��Ƶ������ͼ
	* ��ͨ��ThumbnailUtils������һ����Ƶ������ͼ��Ȼ��������ThumbnailUtils������ָ����С������ͼ��
	* �����Ҫ������ͼ�Ŀ�͸߶�С��MICRO_KIND��������Ҫʹ��MICRO_KIND��Ϊkind��ֵ���������ʡ�ڴ档
	* @param videoPath ��Ƶ��·��
	* @param width ָ�������Ƶ����ͼ�Ŀ��
	* @param height ָ�������Ƶ����ͼ�ĸ߶ȶ�
	* @param kind ����MediaStore.Images.Thumbnails���еĳ���MINI_KIND��MICRO_KIND��
	*            ���У�MINI_KIND: 512 x 384��MICRO_KIND: 96 x 96
	* @return ָ����С����Ƶ����ͼ
	*/
	private Bitmap getVideoThumbnail(String videoPath, int width, int height,
	int kind) {
		Bitmap bitmap = null;
		// ��ȡ��Ƶ������ͼ
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		System.out.println("w"+bitmap.getWidth());
		System.out.println("h"+bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
		ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	 public class GetThumbnailTask extends AsyncTask<String, Integer, String>{
	        private static final String TAG = "GetThumbnailTask";
	        private boolean isStoped = false;
	        public void stop(){
	        	isStoped = true;
	        }
	        /**
	         * onPreExecute(), �÷�������ִ��ʵ�ʵĺ�̨����ǰ��UI �̵߳��á�
	         * �����ڸ÷�������һЩ׼�����������ڽ�������ʾһ��������������һЩ�ؼ���ʵ����
	         */
	        @Override
	        protected void onPreExecute() {
	            // TODO Auto-generated method stub
	        }

	        /**
	         * ��doInBackground ִ����ɺ�onPostExecute ��������UI �̵߳��ã�
	         * ��̨�ļ�������ͨ���÷������ݵ�UI �̣߳������ڽ�����չʾ���û�
	         */
	        @Override
	        protected void onPostExecute(String result) {
	            // TODO Auto-generated method stub
	        }
	        
	        @Override
			protected void onProgressUpdate(Integer... values) {
				// TODO Auto-generated method stub
				super.onProgressUpdate(values);
	        	//imgAdapter.notifyDataSetChanged();
			}

			/**
	         * ����onPreExecute ����ִ�к�����ִ�У��÷��������ں�̨�߳��С����ｫ��Ҫ
	         * ����ִ����Щ�ܺ�ʱ�ĺ�̨������
	         */
	        @Override
	        protected String doInBackground(String... params) {
	        	 for(int i = 0;i < mFileNameList.size() && !isStoped;i++){
	        		// Map<String, Object> map = list.get(i);
	        		 String door = index;
	        		 try {
	        				String tmp[] = Utils.splitString(door, "@");
	        				door = tmp[0];
	        			} catch (Exception e1) {
	        				// TODO Auto-generated catch block
	        				e1.printStackTrace();
	        				door = index;
	        			}
	        		String type1 = "snapshots";
	        		 if(type == 0){
	        			ScanMediaFileThread scanThread = new ScanMediaFileThread(new File(AppUtils.SD_PATH + "/" + AppUtils.APP_NAME/*getExternalFilesDir(null)*/, door + File.separator + type1 + File.separator + mFileNameList.get(i)).getAbsolutePath(), i,MediaInfoListAdapter.MEDIA_TYPE_PICTURE, MediaActivity.this);  
	        		         threadPool.submit(scanThread);  
	        		       // new WeakReference<Future<?>>(request);
	        			// map.put(MediaInfoListAdapter.KEY_THUMBNALL, getImageThumbnail(new File(getExternalFilesDir(null), door + File.separator + type1 + File.separator + mFileNameList.get(i)).getAbsolutePath(),100,56));
	        		 }else{
	        			 type1 = "clips";
	        			/* File clipFile = new File(getExternalFilesDir(null), door + File.separator + type1 + File.separator + mFileNameList.get(i));
	        			 map.put(MediaInfoListAdapter.KEY_THUMBNALL, ThumbnailUtils.createVideoThumbnail(clipFile.getAbsolutePath(),Thumbnails.MINI_KIND));
	        			 MediaMetadataRetriever mmr = new MediaMetadataRetriever();
	        			 mmr.setDataSource(clipFile.getAbsolutePath());
	        			 int durationMs = 0;
	        			 try{
	        				 durationMs = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
	        			 }catch(NumberFormatException  e){
	        				 e.printStackTrace();
	        			 }
	        			 int second = durationMs/1000;
	        			 map.put(MediaInfoListAdapter.KEY_DURATION, String.format("%02d", second/60) + ":" + String.format("%02d", second%60));*/
	        			 ScanMediaFileThread scanThread = new ScanMediaFileThread(new File(AppUtils.SD_PATH + "/" + AppUtils.APP_NAME/*getExternalFilesDir(null)*/, door + File.separator + type1 + File.separator + mFileNameList.get(i)).getAbsolutePath(), i,MediaInfoListAdapter.MEDIA_TYPE_CLIP, MediaActivity.this);  
	        			 threadPool.submit(scanThread);  
	        		       // new WeakReference<Future<?>>(request);
	        		 }
	        		 Log.d("Vincent","publishProgress");
	     	       //publishProgress(0);
	             }
	            return null;
	        } 
	    }

	@Override
	public void onThumbnallGeted(int position, Bitmap thumbnall) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onThumbnallGeted " + position);
		 Map<String, Object> map = list.get(position);
		 map.put(MediaInfoListAdapter.KEY_THUMBNALL,thumbnall);
		 listView.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				imgAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onDurationGeted(int position, int ms) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onDurationGeted " + position);
		 Map<String, Object> map = list.get(position);
		 int second = ms/1000;
		 if(second >= 0){
			 map.put(MediaInfoListAdapter.KEY_DURATION, String.format("%02d", second/60) + ":" + String.format("%02d", second%60));
			 listView.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					imgAdapter.notifyDataSetChanged();
				}
			});
		 }
	}
	@Override
	public void onFinished(int position) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onFinished " + position);
	}
	private class ScanMediaFileThread implements Runnable{
		private String filePath;
		private int position;
		private int type;
		private ScanMediaFileListener scanMediaFileListener;  
		public ScanMediaFileThread(String filePath, int position,int type,
				ScanMediaFileListener scanMediaFileListener) {  
	        this.filePath = filePath;  
	        this.position = position;
	        this.type = type;
	        this.scanMediaFileListener = scanMediaFileListener;  
	    }  
		private boolean prepare(){
			boolean ret = true;
			File file = new File(filePath);
			if(!file.exists()){
				ret = false;
			}
			return ret;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(prepare()){
				if(this.type == MediaInfoListAdapter.MEDIA_TYPE_CLIP){
	    			 MediaMetadataRetriever retriever = new MediaMetadataRetriever();
	    			 Bitmap bitmap = null;  
	    			 try {
	    		            retriever.setDataSource(filePath);
	    		            bitmap = retriever.getFrameAtTime(-1);
	    		            scanMediaFileListener.onThumbnallGeted(position, bitmap);
	    		            int durationMs = 0;
		   	    			 try{
		   	    				 durationMs = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
		   	    			 }catch(NumberFormatException  e){
		   	    				 e.printStackTrace();
		   	    			 }
		   	    			 scanMediaFileListener.onDurationGeted(position, durationMs);
	    		        } catch (IllegalArgumentException ex) {
	    		            // Assume this is a corrupt video file
	    		        } catch (RuntimeException ex) {
	    		            // Assume this is a corrupt video file.
	    		        } finally {
	    		            try {
	    		                retriever.release();
	    		            } catch (RuntimeException ex) {
	    		                // Ignore failures while cleaning up.
	    		            }
	    		        }
	    			 
				}else{
					scanMediaFileListener.onThumbnallGeted(position,  getImageThumbnail(filePath,100,56));
				}
				scanMediaFileListener.onFinished(position);
			}else{
				scanMediaFileListener.onFinished(position);
			}
		}
		
	}
	private static class LvMenuItem
    {
        public LvMenuItem(int icon, String name)
        {
            this.icon = icon;
            this.name = name;

            if (icon == NO_ICON && TextUtils.isEmpty(name))
            {
                type = TYPE_SEPARATOR;
            } else if (icon == NO_ICON)
            {
                type = TYPE_NO_ICON;
            } else
            {
                type = TYPE_NORMAL;
            }

            if (type != TYPE_SEPARATOR && TextUtils.isEmpty(name))
            {
                throw new IllegalArgumentException("you need set a name for a non-SEPARATOR item");
            }

            LogUtils.LOGD(this, type + "");


        }

        public LvMenuItem(String name)
        {
            this(NO_ICON, name);
        }

        public LvMenuItem()
        {
            this(null);
        }

        private static final int NO_ICON = 0;
        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_NO_ICON = 1;
        public static final int TYPE_SEPARATOR = 2;

        int type;
        String name;
        int icon;

    }

	 private static class MenuItemAdapter extends BaseAdapter
	    {
	        private final int mIconSize;
	        private LayoutInflater mInflater;
	        private Context mContext;

	        public MenuItemAdapter(Context context)
	        {
	            mInflater = LayoutInflater.from(context);
	            mContext = context;

	            mIconSize = 65;
	        }

	        private List<LvMenuItem> mItems = new ArrayList<LvMenuItem>(
	                Arrays.asList(
	                        new LvMenuItem(R.drawable.menu_system_settings, AppApplication.getInstance().getString(R.string.system_settings)),
	                        new LvMenuItem(R.drawable.menu_account_management, AppApplication.getInstance().getString(R.string.account_management)),
	                        new LvMenuItem(R.drawable.menu_about_device, AppApplication.getInstance().getString(R.string.about_devices)),
	                        new LvMenuItem(R.drawable.where_to_buy, AppApplication.getInstance().getString(R.string.nortek_where_to_buy)),
	                        new LvMenuItem(R.drawable.feedback, AppApplication.getInstance().getString(R.string.nortek_feedback)),
	                        new LvMenuItem(R.drawable.menu_system_logout, AppApplication.getInstance().getString(R.string.system_logout)) 
	                        //new LvMenuItem(),
	                        //new LvMenuItem("Sub Items"),
	                        //new LvMenuItem(R.drawable.ic_dashboard, "Sub Item 1"),
	                        //new LvMenuItem(R.drawable.ic_forum, "Sub Item 2")
	                ));


	        @Override
	        public int getCount()
	        {
	            return mItems.size();
	        }


	        @Override
	        public Object getItem(int position)
	        {
	            return mItems.get(position);
	        }


	        @Override
	        public long getItemId(int position)
	        {
	            return position;
	        }

	        @Override
	        public int getViewTypeCount()
	        {
	            return 3;
	        }

	        @Override
	        public int getItemViewType(int position)
	        {
	            return mItems.get(position).type;
	        }

	        @Override
	        public View getView(int position, View convertView, ViewGroup parent)
	        {
	            LvMenuItem item = mItems.get(position);
	            switch (item.type)
	            {
	                case LvMenuItem.TYPE_NORMAL:
	                    if (convertView == null)
	                    {
	                        convertView = mInflater.inflate(R.layout.design_drawer_item, parent,
	                                false);
	                    }
	                    TextView itemView = (TextView) convertView;
	                    itemView.setText(item.name);
	                   
	                    Drawable icon = mContext.getResources().getDrawable(item.icon);
	                    setIconColor(icon);
	                    if (icon != null)
	                    {
	                        icon.setBounds(20, 0, mIconSize + 20, mIconSize );
	                        TextViewCompat.setCompoundDrawablesRelative(itemView, icon, null, null, null);
	                    }

	                    break;
	                case LvMenuItem.TYPE_NO_ICON:
	                    if (convertView == null)
	                    {
	                        convertView = mInflater.inflate(R.layout.design_drawer_item_subheader,
	                                parent, false);
	                    }
	                    TextView subHeader = (TextView) convertView;
	                    subHeader.setText(item.name);
	                    break;
	                case LvMenuItem.TYPE_SEPARATOR:
	                    if (convertView == null)
	                    {
	                        convertView = mInflater.inflate(R.layout.design_drawer_item_separator,
	                                parent, false);
	                    }
	                    break;
	            }
	            
	            final int itemIndex = position;
	            convertView.setOnClickListener(new OnClickListener()
	            {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
						//just test code. you must control your own business process.
						//((Activity) mContext).finish();
						switch(itemIndex)
						{
						case 0:
							mContext.startActivity(new Intent(mContext, UserSystemSettings.class));
							break;
						case 1:
							mContext.startActivity(new Intent(mContext, UserAccountManager.class));
							break;
						
						case 2:
							mContext.startActivity(new Intent(mContext, UserAboutDevice.class));
							break;
						
						case 3: //logout			
							Uri uri = Uri.parse(BuildConfig.nortekUri);  
				            Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
				            mContext.startActivity(intent);  
							break;
						case 4: //feedback					
							LogUtils.SendLogFileToEmail(mContext);
							///xxx;
							break;
						case 5: //logout					
							
							mHandler.sendEmptyMessage(POP_UP_SURE_LOG_OUT);
							break;
						default:
							break;
						}
						
					}            	
	            });
	            return convertView;
	        }

	        public void setIconColor(Drawable icon)
	        {
	            //int textColorSecondary = android.R.attr.textColorSecondary;
	        	int textColorSecondary = R.color.gray;
	            TypedValue value = new TypedValue();
	            if (!mContext.getTheme().resolveAttribute(textColorSecondary, value, true))
	            {
	                return;
	            }
	            int baseColor = mContext.getResources().getColor(value.resourceId);
	            icon.setColorFilter(baseColor, PorterDuff.Mode.MULTIPLY);
	        }
	    }

	 protected void dialog() {
	    	AlertDialog.Builder builder = new Builder(mContext);
	    	builder.setMessage(R.string.sure_to_delete);
	    	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
	    	{
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							String door = index;
							try {
								String tmp[] = Utils.splitString(door, "@");
								door = tmp[0];
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								door = index;
							}
							String type = "snapshots";
							if (MediaActivity.this.type == 1) type = "clips";							
							File dir = new File(AppUtils.SD_PATH + "/" + AppUtils.APP_NAME, door + File.separator + type);
							
							deleteFile(dir.getAbsolutePath());
							list.clear();
							delAll.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									imgAdapter.notifyDataSetChanged();									
								}
							});
						}
					}).start();
				}
	    		
	    	});
	    	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
	    		
	    	});
	    	builder.create().show();
		}
}
