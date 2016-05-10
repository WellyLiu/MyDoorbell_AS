/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-7 AM9:23:08
 * Project: TecomDoor
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author Administrator
 *
 */
public class DoorPhoneAddPassword extends PNPBaseActivity implements View.OnClickListener{

	private Button mHasPwd, mNoPwd;
	
	/* (non-Javadoc)
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
		
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);	
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.door_setup));
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.doorphone_add_password);
		getWindow().setBackgroundDrawable(null);
		
		mHasPwd = (Button)this.findViewById(R.id.door_phone_add_pwd);
		mNoPwd = (Button)this.findViewById(R.id.door_phone_add_pwd_no);
		mHasPwd.setOnClickListener(this);
		mNoPwd.setOnClickListener(this);
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
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
		case R.id.door_phone_add_pwd:
			startActivity(new Intent(this, DoorPhonePasswordInput.class));
			break;
		case R.id.door_phone_add_pwd_no:
			startActivity(new Intent(this, DoorPhoneAddByOther.class));
			break;
		default:
			break;
		}
	}
}