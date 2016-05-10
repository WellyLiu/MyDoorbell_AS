package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageTextButton extends LinearLayout {
    
    private static final String TAG = "ImageTextButton";
	private ImageView imgView;  
    private TextView  textView;
    private boolean isChecked = false;
    public ImageTextButton(Context context) {
        super(context,null);
    }
    
    public ImageTextButton(Context context,AttributeSet attributeSet) {
        super(context, attributeSet);
        
        LayoutInflater.from(context).inflate(R.layout.image_text_button, this,true);
        
        
        TypedArray array = context.obtainStyledAttributes(
        		attributeSet, R.styleable.ImageTextButton);
        ColorStateList textColor = array.getColorStateList(R.styleable.ImageTextButton_textColor); 
        float textSize = array.getDimension(R.styleable.ImageTextButton_textSize, -1);
        CharSequence text = array.getText(R.styleable.ImageTextButton_text);
        Drawable drawable = array.getDrawable(R.styleable.ImageTextButton_src);
        this.imgView = (ImageView)findViewById(R.id.imgview);
        this.textView = (TextView)findViewById(R.id.textview);
        if(!isInEditMode()){
        	if(textSize!=-1){
        		textView.setTextSize(textSize);
        	}
	        if(textColor != null){
	        	textView.setTextColor(textColor);
	        }
	        if(text != null){
	        	textView.setText(text);
	        }
	        if(drawable != null){
	        	imgView.setImageDrawable(drawable);
	        }
        }
        
        this.setClickable(true);
        this.setFocusable(true);
    }
    
    public void setImgResource(int resourceID) {
        this.imgView.setImageResource(resourceID);
    }
    
    public void setText(String text) {
        this.textView.setText(text);
    }
    
    public void setTextColor(int color) {
        this.textView.setTextColor(color);
    }
    
    public void setTextSize(float size) {
        this.textView.setTextSize(size);
    }
    public boolean isChecked(){
    	return this.isChecked;
    }
    public void setChecked(boolean isChecked){
    	this.isChecked = isChecked;
    	Log.d(TAG,"setChecked isChecked = " + isChecked);
    	if(isChecked){
			//setBackgroundResource(R.color.btn_bg_press_color);
		}else{
			//setBackgroundResource(R.color.btn_bg_color);
		}
    }

	@Override
	public boolean performClick() {
		// TODO Auto-generated method stub
		Log.d(TAG,"performClick");
		setChecked(!isChecked);
		return super.performClick();
	}
    
}