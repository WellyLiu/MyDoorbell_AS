/**
 * 
 */
package com.gocontrol.doorbell.model;

import java.util.HashSet;

/**
 * @author Welly Liu E-mail: wliu@tecomtech.com
 * @version create time�?2013-1-8 下午2:40:04
 * class declaration.
 * interfaces and static parameters.
 */

public class IProxSensor {

	public static boolean proxSensorNear = false;
	//Proximity Sensor
	public interface IProxSensorListener{
		public void onProximitySensorChanged(boolean near);
	}
	private static HashSet<IProxSensorListener> mProxSensorListener = new HashSet<IProxSensorListener>();
	public static void addProxSensorListener(IProxSensorListener one)
	{
		mProxSensorListener.add(one);
	}
	public static void delProxSensorListener(IProxSensorListener one)
	{
		mProxSensorListener.remove(one);
	}
	public static HashSet<IProxSensorListener> getProxSensorListener()
	{
		return mProxSensorListener;
	}
}
