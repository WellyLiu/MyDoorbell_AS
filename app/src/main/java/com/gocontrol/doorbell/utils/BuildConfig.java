/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-5 AM11:51:51
 * Project: Dnake_IP5809N_3in1
 * PackageName: com.m2000.rs485.log
 */
package com.gocontrol.doorbell.utils;

/**
 * @author Administrator
 *
 */
public class BuildConfig {

	public static final Boolean DEBUG =  Boolean.parseBoolean("true");
	
	public static final int MAX_ODP_NUM = 2;
	
	public static boolean DETEC_ODP_STATUS_ENABLE = true;
	
	
	public static final String Account_Local_Default = "b6648f3521513d85c7e6c5628ce5c4f2a6ed8303";
	public static final String Password_Local_Default = "7db035213e2ae7f27fda7aa24b3a6e19b6311cca";
	public static final String Account_ODP_Local_Default = "247d8b566fbcaac6d99d530f5eb0547a28a0ca6e";
	public static final String Password_ODP_Local_Default = "fecfd93b61669304f213ce56bad77cedaca6a008";
	
	/*
	public static final String Account_Local_Default = "admin";
	public static final String Password_Local_Default = "1234";
	public static final String Account_ODP_Local_Default = "root";
	public static final String Password_ODP_Local_Default = "admin";
	*/
	/*
	public static final String Account_Local_Default = "b6648f3521513d85";
	public static final String Password_Local_Default = "7db035213e2ae7f2";
	public static final String Account_ODP_Local_Default = "247d8b566fbcaac";
	public static final String Password_ODP_Local_Default = "fecfd93b61669304" ;
	*/	
	public static String Name_Local_Default = "";
	
	public static Boolean MODIFY_PCM_DATA = true;
	
	public static String nortekUri = "http://www.gocontrol.com/mydoorbell.php";
	
	public static String []  timeZoneMsg = {
		
		"Midway Island, Samoa",
		"Midway Island, Samoa",
		"Hawaii",
		"Alaska",
		"Pacific Time (US &amp; Canada)",
		"Mountain Time (US &amp; Canada)",
		"Central Time (US &amp; Canada)",
		"Eastern Time (US &amp; Canada)",
		"Atlantic Time (Canada)",
		"Newfoundland",
		"Brasilia",
		"Mid-Atlantic",
		"Azores, Cape Verde Is",
		"Greenwich Mean Time, London",
		"Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna",
		"Jerusalem, Athens, Istanbul, Minsk",
		"Baghdad, Kuwait, Riyadh",
		"Tehran",
		"(Abu Dhabi, Muscat",
		"Kabul- the capital of Afghanistan",
		"Ekaterinburg",
		"Bombay, Calcutta, Madras, New Delhi",
		"Katmandu-the capital of Nepal",
		"Almaty, Dhaka",
		"Yangon-the capital of Myanmar",
		"Bangkok, Hanoi, Jakarta, ...)",
		"Beijing, Hong Kong, Singapore, Taipei",
		"Osaka, Tokyo, Seoul",
		"Adelaide",
		"Canberra, Melbourne, Sydney",
		"Magadan, Solomon Is, New Caledonia",
		"Auckland, Wellington",
		"Nukualofa-the capital of Tonga"


	};
	
	public static String []  timeZoneData = {
		"GMT-12:00",
		"GMT-11:00",
		"GMT-10:00",
		"GMT-09:00",
		"GMT-08:00",
		"GMT-07:00",
		"GMT-06:00",
		"GMT-05:00",
		"GMT-04:00",
		"GMT-03:30",
		"GMT-03:00",
		"GMT-02:00",
		"GMT-01:00",
		"GMT",
		"GMT+01:00",
		"GMT+02:00",
		"GMT+03:00",
		"GMT+03:30",
		"GMT+04:00",
		"GMT+04:30",
		"GMT+05:00",
		"GMT+05:30",
		"GMT+05:45",
		"GMT+06:00",
		"GMT+06:30",
		"GMT+07:00",
		"GMT+08:00",
		"GMT+09:00",
		"GMT+09:30",
		"GMT+10:00",
		"GMT+11:00",
		"GMT+12:00",
		"GMT+13:00"

	};
}
