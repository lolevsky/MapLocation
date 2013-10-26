package com.iapplize.maplocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.iapplize.maplocation.http.ConnectionInterface;

@SuppressWarnings("serial")
public class GetCheckIns extends HttpServlet implements ConnectionInterface {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = req.getReader();
			while ((line = reader.readLine()) != null) {
				jb.append(line);
			}
		} catch (Exception e) {
		}

		JSONObject jsonObject = null;

		try {
			jsonObject = new JSONObject(jb.toString());
		} catch (Exception e) {

		}

		Query q = new Query("users");

		Date d = new Date(((new Date()).getTime()) - 172800000);
		Filter dateFilter = new FilterPredicate(PAR_DATE,
				FilterOperator.GREATER_THAN_OR_EQUAL, d);

		if (jsonObject != null) {

			String userId = jsonObject.optString(PAR_USERID, "null");

			if (!userId.equals("null")) {

				Filter userFilter = new FilterPredicate(PAR_USERID,
						FilterOperator.EQUAL, userId);

				Filter dateAndRegIdFilter = CompositeFilterOperator.and(
						dateFilter, userFilter);

				q.setFilter(dateAndRegIdFilter);
			}else{
				q.setFilter(dateFilter);
			}
		} else {
			q.setFilter(dateFilter);
		}

		q.addSort(PAR_DATE, Query.SortDirection.DESCENDING);

		PreparedQuery pq = datastore.prepare(q);

		JSONArray jsonArray = null;

		try {

			jsonArray = new JSONArray();

			for (Entity result : pq.asIterable(FetchOptions.Builder.withLimit(
					50).offset(0))) {

				JSONObject jsonObj = new JSONObject();

				jsonObj.put(PAR_USERNAME,
						(String) result.getProperty(PAR_USERNAME));
				jsonObj.put(PAR_USERLASTNAME,
						(String) result.getProperty(PAR_USERLASTNAME));
				jsonObj.put(PAR_USERIMAGEURL,
						(String) result.getProperty(PAR_USERIMAGEURL));
				jsonObj.put(PAR_LAT, (String) result.getProperty(PAR_LAT));
				jsonObj.put(PAR_LON, (String) result.getProperty(PAR_LON));

				jsonArray.put(jsonObj);

			}

			JSONObject jo = new JSONObject();

			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");

			if (jsonArray != null) {
				jo.put(PAR_DATA, jsonArray);
			}

			resp.getWriter().write(jo.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
