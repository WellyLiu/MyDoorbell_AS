/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-13 AM11:03:07
 * Project: TecomDoor
 * PackageName: com.tecom.door.bean
 */
package com.gocontrol.doorbell.bean;

/**
 * @author Administrator
 *
 */
public class ODPFeature {

	private int mSpeakerVol;
	private int mMicroVol;
	private String mResolution;
	private int mFrameRate;
	private boolean flip;
	private boolean mirror;
	private String motionDetec; //0: disable 1: enable default: 000000000
	private int PIR;
	
	private String timeZone;
	
	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}
	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	private int brightness;
	private int contrast;
	private int hue;
	private int saturation;
	private int sharpness;
	private int targetY;
	
	
	/**
	 * @return the mSpeakerVol
	 */
	public int getmSpeakerVol() {
		return mSpeakerVol;
	}
	/**
	 * @return the mMicroVol
	 */
	public int getmMicroVol() {
		return mMicroVol;
	}
	/**
	 * @return the mResolution
	 */
	public String getmResolution() {
		return mResolution;
	}
	/**
	 * @return the mFrameRate
	 */
	public int getmFrameRate() {
		return mFrameRate;
	}
	/**
	 * @return the flip
	 */
	public boolean isFlip() {
		return flip;
	}
	/**
	 * @return the mirror
	 */
	public boolean isMirror() {
		return mirror;
	}
	/**
	 * @return the motionDetec
	 */
	public String getMotionDetec() {
		return motionDetec;
	}
	/**
	 * @return the pIR
	 */
	public int getPIR() {
		return PIR;
	}
	/**
	 * @return the brightness
	 */
	public int getBrightness() {
		return brightness;
	}
	/**
	 * @return the contrast
	 */
	public int getContrast() {
		return contrast;
	}
	/**
	 * @return the hue
	 */
	public int getHue() {
		return hue;
	}
	/**
	 * @return the saturation
	 */
	public int getSaturation() {
		return saturation;
	}
	/**
	 * @return the sharpness
	 */
	public int getSharpness() {
		return sharpness;
	}
	/**
	 * @return the targetY
	 */
	public int getTargetY() {
		return targetY;
	}
	/**
	 * @param mSpeakerVol the mSpeakerVol to set
	 */
	public void setmSpeakerVol(int mSpeakerVol) {
		this.mSpeakerVol = mSpeakerVol;
	}
	/**
	 * @param mMicroVol the mMicroVol to set
	 */
	public void setmMicroVol(int mMicroVol) {
		this.mMicroVol = mMicroVol;
	}
	/**
	 * @param mResolution the mResolution to set
	 */
	public void setmResolution(String mResolution) {
		this.mResolution = mResolution;
	}
	/**
	 * @param mFrameRate the mFrameRate to set
	 */
	public void setmFrameRate(int mFrameRate) {
		this.mFrameRate = mFrameRate;
	}
	/**
	 * @param flip the flip to set
	 */
	public void setFlip(boolean flip) {
		this.flip = flip;
	}
	/**
	 * @param mirror the mirror to set
	 */
	public void setMirror(boolean mirror) {
		this.mirror = mirror;
	}
	/**
	 * @param motionDetec the motionDetec to set
	 */
	public void setMotionDetec(String motionDetec) {
		this.motionDetec = motionDetec;
	}
	/**
	 * @param pIR the pIR to set
	 */
	public void setPIR(int pIR) {
		PIR = pIR;
	}
	/**
	 * @param brightness the brightness to set
	 */
	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}
	/**
	 * @param contrast the contrast to set
	 */
	public void setContrast(int contrast) {
		this.contrast = contrast;
	}
	/**
	 * @param hue the hue to set
	 */
	public void setHue(int hue) {
		this.hue = hue;
	}
	/**
	 * @param saturation the saturation to set
	 */
	public void setSaturation(int saturation) {
		this.saturation = saturation;
	}
	/**
	 * @param sharpness the sharpness to set
	 */
	public void setSharpness(int sharpness) {
		this.sharpness = sharpness;
	}
	/**
	 * @param targetY the targetY to set
	 */
	public void setTargetY(int targetY) {
		this.targetY = targetY;
	}
	
	
}
