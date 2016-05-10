/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 AM10:04:53
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import java.util.ArrayList;
import java.util.List;

import com.iptnet.android.web.eac.EACContext;
import com.iptnet.android.web.eac.LoginResult;
import com.iptnet.android.web.eac.ModifyResult;
import com.iptnet.android.web.eac.EACContext.EACException;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.MResult;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.ui.v7.UserSystemSettingsDoor.DIALOG_ITEM;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.Utils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Administrator
 *
 */
public class UserAccountManager extends PNPBaseActivity implements View.OnClickListener{

	private final static String TAG = "UserAccountManager.class";
	private List<String> list = new ArrayList<String>();
	public List<String> getListData(){  
        for(ODPInfo one : ODPManager.getInstance().getODPList())
        {
        	list.add(one.getOdpName());
        }
        return list;  
    }  
	
	private Context mContext;
	private TextView mUserName;
	private LinearLayout mRename, mPwd;
	
	
	private ListView listView;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		ImageView img = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		img.setOnClickListener(this);
		
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		txtTitle.setText(R.string.account_manager_title);
		
		setContentView(R.layout.account_manager);
		getWindow().setBackgroundDrawable(null) ;
		
		mRename = (LinearLayout)this.findViewById(R.id.rename);
		mRename.setOnClickListener(this);
		mPwd = (LinearLayout)this.findViewById(R.id.password);
		mPwd.setOnClickListener(this);
		
		
		mUserName = (TextView)this.findViewById(R.id.user_name);
		mUserName.setText(LocalUserInfo.getInstance().getLocalName());
		
		listView = (ListView) findViewById(R.id.odp_list);  
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.simple_txt_item, getListData());    
	    listView.setAdapter(adapter);  
	    listView.setOnItemClickListener(new OnItemClickListener()
	    {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				startActivity(new Intent(mContext, AllAccountShow.class).putExtra("door_id", position));
				
			}
	    	
	    });
	      	
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		
		case R.id.btn_back:
			finish();
			break;
		case R.id.rename:
			popUpRenameDialog(getString(R.string.door_rename), getString(R.string.door_name));
			break;
		case R.id.password:
			popUpPwdDialog(getString(R.string.door_pwd), getString(R.string.door_cur_pwd), getString(R.string.door_new_pwd), getString(R.string.door_confirm_pwd));
			break;
		
		default:
			break;
		}
	}

	/**
	 * @param speaker
	 * @return
	 */
	private int getShowWhichIndex(DIALOG_ITEM speaker) {
		// TODO Auto-generated method stub
		int ret = 0;
		switch(speaker)
		{	
		case MICRO:	
			int mVol = SystemConfigManager.getInstance().getMicVol();
			ret = mVol -1;
			break;
		default:
			break;
		}
		return ret;
	}
	
	/**
	 * @param i
	 */
	protected void popUpRenameDialog(String title, final String editName) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_one_eidit, null);

		TextView userName = (TextView) textEntryView
				.findViewById(R.id.edit_name); 
		userName.setText(editName);
		final EditText name = (EditText) textEntryView
				.findViewById(R.id.edit);
		name.setHint(LocalUserInfo.getInstance().getLocalName());		
		
		builder.setTitle(title);
		builder.setView(textEntryView);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {			
				String newName = name.getEditableText().toString();
				Log.d("tecom", "new name: " + newName);
				//�����߼�
				//store to the entity.				
				mUserName.setText(newName);				
				LocalUserInfo.getInstance().setLocalName(newName);				
				Utils.saveC2CLoginName(mContext,newName);								
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		});
		builder.create().show();
		
	}
	
	/**
	 * @param i
	 */
	protected void popUpPwdDialog(String title, final String oneNameStr,
			final String towNameStr,final String threeNameStr) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_three_edit, null);
		TextView oneName = (TextView) textEntryView
				.findViewById(R.id.name_one);
		oneName.setText( oneNameStr );
		TextView twoName = (TextView) textEntryView
				.findViewById(R.id.name_two);
		twoName.setText(towNameStr);
		TextView threeName = (TextView) textEntryView
				.findViewById(R.id.name_three);
		threeName.setText(threeNameStr);
		
		builder.setTitle(title);
		builder.setView(textEntryView);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				
				
				EditText pwd_one = (EditText) textEntryView
						.findViewById(R.id.edit_one);
				EditText pwd_two = (EditText) textEntryView
						.findViewById(R.id.edit_two);
				EditText pwd_three = (EditText) textEntryView
						.findViewById(R.id.edit_three);
				

				// check parameter
				String oldPwd = pwd_one.getText().toString();
				String newPwd = pwd_two.getText().toString();
				String confirmPwd = pwd_three.getText().toString();
				if (oldPwd.isEmpty()) {
					Toast.makeText(mContext, mContext.getString(R.string.password_1), Toast.LENGTH_SHORT).show();
					return ;
				}else if( !oldPwd.equalsIgnoreCase(LocalUserInfo.getInstance().getC2cPassword()) )
				{
					Toast.makeText(mContext, mContext.getString(R.string.password_2), Toast.LENGTH_SHORT).show();
					return ;
				}
				
				if (newPwd.isEmpty()) {
					Toast.makeText(mContext, mContext.getString(R.string.password_3), Toast.LENGTH_SHORT).show();
					return ;
				}
				if (confirmPwd.isEmpty()) {
					Toast.makeText(mContext, mContext.getString(R.string.password_4), Toast.LENGTH_SHORT).show();
					return;
				}
				
				if( !newPwd.equalsIgnoreCase(confirmPwd) )
				{
					Toast.makeText(mContext, mContext.getString(R.string.password_5) , Toast.LENGTH_SHORT).show();
					return;
				}
				
				doWebLoginAndChangePwd(oldPwd, newPwd);
				
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		});
		builder.create().show();
		
	}

	private RequestTask mRequestTask;
	private String domain, account, newPwd,oldPwd,name;
	/**
	 * @param oldPwd
	 * @param newPwd
	 */
	protected void doWebLoginAndChangePwd(String oldPwd, String newPwd) {
		// TODO Auto-generated method stub
		mRequestTask = new RequestTask();
		mRequestTask.execute(newPwd);
	}
	
	private class RequestTask extends AsyncTask<String, Void, MResult> implements OnCancelListener {

		private ProgressDialog mProgress;
		
		@Override
		protected void onPreExecute() {
			mProgress = new ProgressDialog(mContext);
			mProgress.setTitle(null);
			mProgress.setMessage("Changing ...");
			mProgress.setOnCancelListener(this);
			mProgress.setCancelable(true);
			mProgress.show();
		}
		
		@Override
		protected MResult doInBackground(String... params) {

			// get sign up data
			domain = mContext.getString(R.string.def_reg_domain);
			account = LocalUserInfo.getInstance().getC2cAccount();
			newPwd = params[0];
			oldPwd = LocalUserInfo.getInstance().getC2cPassword();
			name = LocalUserInfo.getInstance().getLocalName();
			MResult mResult = null;
			try {
				/////////login in the web server
				String url = getString(R.string.web_eac_url);
				String apiKey = getString(R.string.web_eac_api_key);
				EACContext server = new EACContext(url, apiKey);
				LoginResult result = server.login(domain, account, oldPwd);
				if (result == null) {						
					return mResult;
				}else
				{
					int state = result.getState();
					if (state == 200) {

						String token = result.getLoginToken();
						//change the password...
						server.showDebugMessage(true);
						ModifyResult modifyResult = server.modifyProfile(domain, account, oldPwd, newPwd, name, token);
						mResult = new MResult(modifyResult.getState(), modifyResult.getLoginToken());
						return mResult;
					} else {
						
						mResult = new MResult(result.getState(), result.getLoginToken());
						return mResult;
					}
				}
				
			} catch (EACException e) {
				Log.w("Tecom", e.getMessage());
				return null;
			}
		}
		
		protected void onPostExecute(MResult result) {

			mProgress.dismiss();
			
			// occur EACException
			if (result == null) {				
				Toast.makeText(mContext, R.string.ntut_tip_4, Toast.LENGTH_SHORT).show();
			
			// get response
			} else {
				int state = result.getState();
				if (state == 200) {
					Toast.makeText(mContext, R.string.ntut_tip_3, Toast.LENGTH_SHORT).show();
					
					// save configuration to shared preference
					AppUtils.saveC2CLoginParams(mContext, account, newPwd, domain);
					//save configuration the Tecom preference.
					
					if(TextUtils.isEmpty(name))
						name = BuildConfig.Name_Local_Default;
					Utils.saveC2CLoginParams(mContext, account, newPwd, domain, name);
					
					Log.d(TAG, account + " " + newPwd + "  " + domain + " " + name);
					//store to the object.					
					LocalUserInfo.getInstance().setC2cPassword(newPwd);
					
					startActivity(new Intent(mContext, UserLoginUI.class));
					finish();
				} else {
					String msg = getString(R.string.ntut_tip_5);
					msg += result.getDescription() /*+ "(" + state + ")"*/;
					Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();	
				}
			}
			
			mRequestTask = null;
		}
		
		@Override
		protected void onCancelled() {
			mProgress.dismiss();
			mRequestTask = null;
			Toast.makeText(mContext, R.string.ntut_tip_6, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			cancel(true);
		}
	}
}