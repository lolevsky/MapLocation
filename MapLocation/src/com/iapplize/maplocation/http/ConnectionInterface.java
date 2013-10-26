package com.iapplize.maplocation.http;

public interface ConnectionInterface {

	static final String BASE_URL = "https://maplocationexample.appspot.com/";

	final String CALL_CHECKIN = "checkin";
	final String CALL_GETCHECKINS = "getcheckins";
	
	final String PAR_USERNAME = "userName";
	final String PAR_USERLASTNAME = "userLastName";
	final String PAR_USERID = "userID";
	final String PAR_USERIMAGEURL = "userImageUrl";
	final String PAR_LAT = "Lat";
	final String PAR_LON = "Lon";
	
	final String PAR_DATA = "data";
}
