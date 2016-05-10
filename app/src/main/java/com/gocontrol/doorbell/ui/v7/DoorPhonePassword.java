/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 下午4:56:37
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author Administrator
 * 
 */
public class DoorPhonePassword extends Activity implements View.OnClickListener{

	private ListView mListView;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		ImageView img = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		img.setOnClickListener(this);
		
		
		setContentView(R.layout.door_phone_password);
		getWindow().setBackgroundDrawable(null) ;
		
		mListView = (ListView) this.findViewById(R.id.listView1);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		adapter.add("Front door");
		adapter.add("Rear door");
		mListView.setAdapter(adapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				popUpPwdDialog("Password", "Current Password", "New Password", "New Password Confirm");
			}

			
		});
	}

	/**
	 * @param i
	 */
	protected void popUpPwdDialog(String title, final String oneNameStr,
			final String towNameStr,final String threeNameStr) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_two_edit, null);
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
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				
				
				EditText pwd_one = (EditText) textEntryView
						.findViewById(R.id.edit_one);
				EditText pwd_two = (EditText) textEntryView
						.findViewById(R.id.edit_two);
				EditText pwd_three = (EditText) textEntryView
						.findViewById(R.id.edit_three);
				
				//处理逻辑
				//
				//
				//
				//
				//
				
				
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		});
		builder.create().show();
		
	}

	/*
	 * (non-Javadoc)
	 * 
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
		default:
			break;
		}
	}

}
