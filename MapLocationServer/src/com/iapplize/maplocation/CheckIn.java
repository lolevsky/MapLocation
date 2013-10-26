package com.iapplize.maplocation;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.iapplize.maplocation.http.ConnectionInterface;

@SuppressWarnings("serial")
public class CheckIn extends HttpServlet implements ConnectionInterface{
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String userName = req.getParameter(PAR_USERNAME);
		String userLastName = req.getParameter(PAR_USERLASTNAME);
		String userID = req.getParameter(PAR_USERID);
		String userImageUrl = req.getParameter(PAR_USERIMAGEURL);
		String Lat = req.getParameter(PAR_LAT);
		String Lon = req.getParameter(PAR_LON);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Date d = new Date();
		
		Entity userData = new Entity("users", d.getTime());
		
		userData.setProperty(PAR_DATE, d);
		userData.setProperty(PAR_USERID, userID);
		userData.setProperty(PAR_USERNAME, userName);
		userData.setProperty(PAR_USERLASTNAME, userLastName);
		userData.setProperty(PAR_USERIMAGEURL, userImageUrl);
		userData.setProperty(PAR_LAT, Lat);
		userData.setProperty(PAR_LON, Lon);
		
		datastore.put(userData);
		
	}
	
}
