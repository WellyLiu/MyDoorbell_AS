package com.gocontrol.doorbell.message;
/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-24 PM1:47:22
 * Project: Cloud_Phone_Demo
 * PackageName: java.tecom.door.network
 */


import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.util.Log;

/**
 * @author Administrator
 * 
 */
public class DataConversion {
	private static final String TAG = "DataConversion";

	public static byte[] StringToUTF8Byte(String chi) {
		byte[] ret = null;
		try {
			ret = chi.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.lang.NullPointerException ex) {
			ex.printStackTrace();
			return new byte[1];
		}
		return ret;
	}

	public static String UTF8ByteToString(byte[] b, int length) {
		String ret = new String(b);
		return ret;
	}
	
	
	public static byte[] getNewMac() {
		
		String mac = getLocalMacAddressFromIp();
		if (null == mac || "".equals(mac)) {
			System.out.println("getNewMac error  :: mac address is null");
			return null;
		}
		// End
		String num[];
		if(mac.contains(".")){
			num = mac.split("\\.");
		}else{
			num = mac.split("\\:");
		}
		String newMac = "";
		for (int i = 0; i < num.length; i++) {
			newMac = newMac + num[i];
		}
		System.out.println("+++++++++++++++newMac++++++++++++++++++     "
				+ newMac);
		byte bNewMac[] = new byte[6];
		byte aNewMac[];
		aNewMac = DataConversion.StringToUTF8Byte(newMac);
		bNewMac[0] = twoByteToMac(aNewMac, 0);
		bNewMac[1] = twoByteToMac(aNewMac, 2);
		bNewMac[2] = twoByteToMac(aNewMac, 4);
		bNewMac[3] = twoByteToMac(aNewMac, 6);
		bNewMac[4] = twoByteToMac(aNewMac, 8);
		bNewMac[5] = twoByteToMac(aNewMac, 10);
				
		return bNewMac;
	}

	private static byte twoByteToMac(byte[] sourceData, int start) {// 从byte到short类型
		byte value;
		byte temp1, temp2;
		value = 0;
		if (sourceData == null) {
			System.out.println("twoByteToMac ourceData is null!");
		} else {
			temp1 = ascTobyte(sourceData[start]);
			temp1 = (byte) (temp1 << 4);
			temp2 = ascTobyte(sourceData[start + 1]);
			value = (byte) (temp1 + temp2);
		}

		return value;
	}

	public static byte ascTobyte(byte asc) {
		byte byteVal = 0;
		if (asc >= '0' && asc <= '9')
			byteVal = (byte) (asc - '0');
		else if (asc >= 'a' && asc <= 'z')
			byteVal = (byte) (asc - 'a' + 10);
		else if (asc >= 'A' && asc <= 'Z')
			byteVal = (byte) (asc - 'A' + 10);
		return byteVal;
	}

	public static byte[] getMacAddressFromIp() {		
		byte[] mac = null;
		try {
			
			NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress
					.getByName(getLocalIpAddress()));
			mac = ne.getHardwareAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac;
	}
	
	public static String getLocalMacAddressFromIp() {
		String mac_s = "";
		try {
			byte[] mac;
			NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress
					.getByName(getLocalIpAddress()));
			mac = ne.getHardwareAddress();
			mac_s = byte2hex(mac);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac_s;
	}

	public static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer(b.length);
		String stmp = "";
		int len = b.length;
		for (int n = 0; n < len; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1) {
				hs = hs.append("0").append(stmp);
			} else {
				hs = hs.append(stmp);
			}
		}
		return String.valueOf(hs);
	}
	
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			System.out.println("WifiPreference IpAddress \n" +  ex.toString());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** convert the IPV4 string(for example, 192.168.1.23)to the byte array.*/
	public static byte[] ipv4Address2BinaryArray(String ipAdd) {
		try {
			byte[] binIP = new byte[4];
			String[] strs = ipAdd.split("\\.");
			for (int i = 0; i < strs.length; i++) {
				binIP[i] = (byte) Integer.parseInt(strs[i]);
			}
			return binIP;
		} catch (NumberFormatException e) {
			Log.d("DataConversion", "NumberFormatException,Invalid int.");
			return new byte[] { 0x00, 0x00, 0x00, 0x00 };
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[] { 0x00, 0x00, 0x00, 0x00 };
		}
	}

	/** convert the byte array to the IPV4 string(for example, 192.168.1.23) */
	public static String binaryArray2Ipv4Address(byte[] addr) {
		String ip = "";
		for (int i = 0; i < addr.length; i++) {
			ip += (addr[i] & 0xFF) + ".";
		}
		return ip.substring(0, ip.length() - 1);
	}
	
	 /** 
     *   convert the int data to the byte array(4 bytes) low byte first ; high byte last 。 bytesToInt() is another function .
     * @param value 
     *            the int value that to be converted.
     * @return byte array.
     */  
	public static byte[] intToBytes( int value ) 
	{ 
		byte[] src = new byte[4];
		src[3] =  (byte) ((value>>24) & 0xFF);
		src[2] =  (byte) ((value>>16) & 0xFF);
		src[1] =  (byte) ((value>>8) & 0xFF);  
		src[0] =  (byte) (value & 0xFF);				
		return src; 
	}
	 /** 
     * convert the int data to the byte array(4 bytes) high byte first ; low byte last 。 bytesToInt2() is another function .
     */  
	public static byte[] intToBytes2(int value) 
	{ 
		byte[] src = new byte[4];
		src[0] = (byte) ((value>>24) & 0xFF);
		src[1] = (byte) ((value>>16)& 0xFF);
		src[2] = (byte) ((value>>8)&0xFF);  
		src[3] = (byte) (value & 0xFF);		
		return src;
	}
	
	 /** 
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *  
     * @param src 
     *            byte数组 
     * @param offset 
     *            从数组的第offset位开始 
     * @return int数值 
     */  
	public static int bytesToInt(byte[] src, int offset) {
		int value;	
		value = (int) ((src[offset] & 0xFF) 
				| ((src[offset+1] & 0xFF)<<8) 
				| ((src[offset+2] & 0xFF)<<16) 
				| ((src[offset+3] & 0xFF)<<24));
		return value;
	}
	
	 /** 
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
	public static int bytesToInt2(byte[] src, int offset) {
		int value;	
		value = (int) ( ((src[offset] & 0xFF)<<24)
				|((src[offset+1] & 0xFF)<<16)
				|((src[offset+2] & 0xFF)<<8)
				|(src[offset+3] & 0xFF));
		return value;
	}
	
	/** 
     * 通过byte数组取到short 
     *  (低位在前，高位在后)
     * @param b 
     * @param index 
     *            第几位开始取 
     * @return 
     */  
    public static short getShort(byte[] b, int index) {  
        return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));  
    } 
    /** 
     * 通过byte数组取到short 
     *  (低位在后，高位在前)
     * @param b 
     * @param index 
     *            第几位开始取 
     * @return 
     */  
    public static short getShort2(byte[] b, int index) {  
        return (short) (((b[index + 0] << 8) | b[index + 1] & 0xff));  
    }
    
    /**
     * 将指定byte数组以16进制的形式打印到控制台
     * 
     * @param hint
     *            String
     * @param b
     *            byte[]
     * @return void
     */
    public static void printHexString(String hint, byte[] b)
    {
        System.out.print(hint);
        for (int i = 0; i < b.length; i++)
        {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase() + " ");
        }
        System.out.println("");
    }
    
    /*
     * 将byte数组 按照 某个字节进行划分为几个部分，返回对应几个部分的String
     * 两步， 1， 划分 2 转为String
     * @param src byte数组
     * @param split 用来划分的字节值
     * @return String 数组
     */
    public static String [] spitAndTransfomBytes( byte[] src, byte split)
    {
    	String [] ret = null;
    	if(src == null)
    		return ret;
    	
    	ArrayList<String> list = new ArrayList<String>();
    	int start = 0, end = 0 ;
    	printHexString("Payload data:", src);
    	if( src[0] == 0x0a )
    	{
    		System.out.printf("spitAndTransfomBytes process.. error: the fistr letter is 0x0a ");
    		return null;
    	}
    	for(int i=0; i<src.length; i++)
		{
			if( src[i] == split)
			{			
				end = i;
				byte tmp[] = new byte[end - start];
				System.arraycopy(src, start, tmp, 0, tmp.length);
				String str = UTF8ByteToString(tmp, tmp.length);
				list.add(str);		
				
				start = i + 1;				
			}
		}
		int size=list.size();  
        ret = (String[])list.toArray(new String[size]);  
        
    	return ret;
    }

	/**
	 * @param macAddress
	 * @return
	 */
	public static String binaryArray2mac(byte[] macAddress) {
		// TODO Auto-generated method stub
		StringBuffer mac = new StringBuffer();
		for (int i = 0; i < macAddress.length; i++)
        {
            String hex = Integer.toHexString(macAddress[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            mac.append(hex);
        }
		return mac.toString();
	}
}