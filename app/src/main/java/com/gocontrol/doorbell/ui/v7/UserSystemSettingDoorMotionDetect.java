/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-28 PM4:41:53
 * Project: TecomDoor
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.ODPFeature;
import com.gocontrol.doorbell.model.ODPFeatureManager;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.utils.ImageViewCheck;
import com.gocontrol.doorbell.utils.Utils;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author Administrator
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UserSystemSettingDoorMotionDetect extends PNPBaseActivity implements
		View.OnClickListener {

	private ViewGroup mMoreLayout;
	private int odpId;
	private ODPInfo doorInfo;
	private ODPFeature feature;
	private char [] mClickStatus;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		odpId = getIntent().getIntExtra("door_id", 0);
		doorInfo = ODPManager.getInstance().getOneODP(odpId);

		feature = ODPFeatureManager.getInstance()
				.getODPFeature();
		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		ImageView img = (ImageView) getActionBar().getCustomView()
				.findViewById(R.id.btn_back);
		img.setOnClickListener(this);

		TextView txtTitle = (TextView) getActionBar().getCustomView()
				.findViewById(android.R.id.title);
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(
				getString(R.string.function_14));

		txtTitle.setText(spanBuilder);

		setContentView(R.layout.system_setting_door_motion_detect);
		getWindow().setBackgroundDrawable(null) ;
		
		String tmp = feature.getMotionDetec();
		if(TextUtils.isEmpty(tmp))
			tmp = "001100000";
		mClickStatus = tmp.toCharArray();
		
		if(mClickStatus.length != 9)
		{
			Log.d(this.getClass().getSimpleName(), "data error......");
			mClickStatus = new char[]{'0', '0','0', '0','0', '0','0', '0','0'};
		}
		
		initUI();
	}

	/**
	 * 
	 */
	private void initUI() {
		// TODO Auto-generated method stub

		mMoreLayout = (ViewGroup) findViewById(R.id.layout_more); //

		final String[] categories = getResources().getStringArray(
				R.array.tecom_categories);

		final int size = categories.length; // ����ĸ���
		final int rowCount = size / 3; // ��Ҫ���ֵ�����(ÿ������)

		/**
		 * ��̬��Ӳ��ַ�����װ ���� 1.������ 2.��Դ�������� 3.�ӵڼ�����ʼ 4.����
		 */
		fillViews(mMoreLayout, categories, 0, rowCount);
	}

	private void fillViews(ViewGroup layout, String[] categories, int start,
			int end) {
		// ����һ����
		View.inflate(this, R.layout.layout_line_horizonal, layout);

		for (int i = start; i < end; i++) {

			// �ҵ����������ڸ����������ͼƬ�ļ�������
			final int firstIndex = i * 3;
			final int secondIndex = i * 3 + 1;
			final int thirdIndex = i * 3 + 2;

			final String firstCategory = categories[firstIndex];
			final String secondCategory = categories[secondIndex];
			final String thirdCategory = categories[thirdIndex];

			// ������
			final LinearLayout linearLayout = new LinearLayout(this);

			// ��һ���Ӳ���
			View.inflate(this, R.layout.layout_line_vertical, linearLayout);
			View.inflate(this, R.layout.layout_department, linearLayout);
			View.inflate(this, R.layout.layout_line_vertical, linearLayout);

			// �ڶ����Ӳ���
			View.inflate(this, R.layout.layout_department, linearLayout);
			View.inflate(this, R.layout.layout_line_vertical, linearLayout);

			// �������Ӳ���
			View.inflate(this, R.layout.layout_department, linearLayout);
			View.inflate(this, R.layout.layout_line_vertical, linearLayout);

			LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layout.addView(linearLayout, layoutParams);

			// ������һ����
			View.inflate(this, R.layout.layout_line_horizonal, layout);

			// ��������getChildAt��ָ����λ��
			final View firstView = linearLayout.getChildAt(1);
			firstView.setTag(firstCategory); // ����tag�������ں����жϵ������һ��
			firstView.setOnClickListener(this); // ���õ��
			ImageViewCheck imageView = (ImageViewCheck) firstView
					.findViewById(R.id.image_icon);
			imageView.setCheckedResource(R.drawable.eye);
			if(mClickStatus[i * 3] == '1')
			{
				imageView.performClick();
			}
			
			final View secondView = linearLayout.getChildAt(3);
			secondView.setTag(secondCategory);
			secondView.setOnClickListener(this);
			imageView = (ImageViewCheck) secondView
					.findViewById(R.id.image_icon);
			imageView.setCheckedResource(R.drawable.eye);
			if(mClickStatus[i * 3 + 1] == '1')
			{
				imageView.performClick();
			}
			
			final View thirdView = linearLayout.getChildAt(5);
			thirdView.setTag(thirdCategory);
			thirdView.setOnClickListener(this);
			imageView = (ImageViewCheck) thirdView
					.findViewById(R.id.image_icon);
			imageView.setCheckedResource(R.drawable.eye);
			if(mClickStatus[i * 3 + 2] == '1')
			{
				imageView.performClick();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
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
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
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
		
		String tmp = String.valueOf(mClickStatus);
		Log.d("tecom", tmp);
		feature.setMotionDetec(tmp);
		Utils.sendODPSetFeature(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd(), feature);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final Object tag = v.getTag(); // ͨ��֮ǰsetTag�ҵ����λ��
		if (tag != null) {
			String department = (String) tag;
			
			ImageViewCheck imageView = (ImageViewCheck) v
					.findViewById(R.id.image_icon);
			imageView.performClick();
			
			int index = Integer.valueOf(department) - 1;
			if(imageView.isChecked())
			{
				mClickStatus[index] = '1';
			}else
			{
				mClickStatus[index] = '0';
			}
		} else {

			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			default:
				break;
			}
		}
	}

}
