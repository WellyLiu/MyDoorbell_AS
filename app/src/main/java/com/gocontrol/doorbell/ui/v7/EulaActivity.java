/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-3-22 AM8:58:42
 * Project: NortekDoorBell
 * PackageName: com.gocontrol.doorbell.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;


import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.ui.ObservableWebView;
import com.gocontrol.doorbell.ui.ObservableWebView.OnScrollChangedCallback;
import com.gocontrol.doorbell.utils.SharedPrefsUtil;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author welly
 * 
 */
public class EulaActivity extends Activity implements OnClickListener {

	static Point size;
	static float density;

	private ObservableWebView mEulaTxt;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		int appLoginStatus = SystemConfigManager.getInstance().isAppAutoLogin();
		boolean acceptEula = SharedPrefsUtil.getValue(this, SharedPrefsUtil.HAS_ACCEPT_EULA, false);
		Log.d("tecom", "appLoginStatus:" + appLoginStatus);
		Log.d("tecom", "acceptEula:" + String.valueOf(acceptEula));
		if (appLoginStatus != 3 || acceptEula) {
			startActivity(new Intent(this, SplashActivity.class));
			finish();
		}
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);

		getActionBar().getCustomView().findViewById(R.id.btn_back)
				.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						finish();
					}
					
				});

		TextView txtTitle = (TextView) getActionBar().getCustomView()
				.findViewById(android.R.id.title);
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(
				getString(R.string.nortek_terms_service));
		txtTitle.setText(spanBuilder);

		setContentView(R.layout.eula_activity);
		getWindow().setBackgroundDrawable(null);
		
		final Button accept = (Button) this.findViewById(R.id.accept);
		accept.setOnClickListener(this);
		Button decline = (Button) this.findViewById(R.id.decline);
		decline.setOnClickListener(this);
		
		mEulaTxt = (ObservableWebView) this.findViewById(R.id.eula_txt);		
		mEulaTxt.loadUrl("file:///android_asset/eula.htm");
		mEulaTxt.setOnScrollChangedCallback(new OnScrollChangedCallback() {

            @Override
            public void onScroll(int l, int t) {
                  int tek = (int) Math.floor(mEulaTxt.getContentHeight() * mEulaTxt.getScale());
                  Log.d("tst", "tek:" + tek + " mEulaTxt.getHeight():" + mEulaTxt.getHeight() + "  mEulaTxt.getScrollY():" + mEulaTxt.getScrollY());
                  if(tek - mEulaTxt.getScrollY() <= ( mEulaTxt.getHeight() + 50)){
                     Toast.makeText(EulaActivity.this, "End", Toast.LENGTH_SHORT).show();
                  	 if(accept != null){
                  		accept.setBackgroundResource(R.drawable.button_shape_style_pnp_1);
                  		accept.setEnabled(true);
                  	 }
                  }
            }
        });
		
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.accept:
			SystemConfigManager.getInstance().setAppAutoLogin(0);
			SystemConfigManager.getInstance().saveAppAutoLogin(this);
			startActivity(new Intent(this, SplashActivity.class));
			SharedPrefsUtil.putValue(this, SharedPrefsUtil.HAS_ACCEPT_EULA, true);
			finish();
			break;
		case R.id.decline:
			finish();
			break;
		}
	}

}
