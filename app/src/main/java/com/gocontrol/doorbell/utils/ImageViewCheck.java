/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-29 下午12:16:12
 * Project: Layout
 * PackageName: app.ui
 */
package com.gocontrol.doorbell.utils;

import com.gocontrol.doorbell.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Administrator
 *
 */
public class ImageViewCheck extends ImageView{

	private boolean isChecked;
	/**
	 * @return the isChecked
	 */
	public boolean isChecked() {
		return isChecked;
	}

	private int mCheckedResourceId;
	/**
	 * @param context
	 */
	public ImageViewCheck(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ImageViewCheck(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ImageViewCheck(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.view.View#performClick()
	 */
	@Override
	public boolean performClick() {
		// TODO Auto-generated method stub
		setChecked(!isChecked);
		return super.performClick();
	}

	/**
	 * @param b
	 */
	private void setChecked(boolean b) {
		// TODO Auto-generated method stub
		if(b){
			this.setBackgroundResource(R.color.white);
			if(mCheckedResourceId != 0)
				this.setImageResource(mCheckedResourceId);
		}else
		{
			this.setBackgroundResource(R.drawable.eye_bg_gray);
			this.setImageDrawable(null);
		}
		isChecked = !isChecked;
	}

	public void setCheckedResource( int resourceID)
	{
		mCheckedResourceId = resourceID;
	}
}
