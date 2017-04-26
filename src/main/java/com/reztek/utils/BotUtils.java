package com.reztek.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.reztek.secret.GlobalDefs;

public abstract class BotUtils {
	public static String getPaddingForLen(String toPad, int desiredLen) {
		String padding = "";
		if (toPad == null) return padding;
		for (int y = 0; y < (desiredLen - toPad.length()); ++y) {
			padding += ' ';
		}
		return padding;
	}
	
	public static String getJSONString(String sURL, HashMap<String, String> props) {
		String retObj = null;
		
		try {
			URL url = new URL(sURL);
			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.addRequestProperty("User-Agent", "Mozilla/4.76"); 
			urlConnection.setRequestMethod("GET");
			urlConnection.setUseCaches(false);
			if (props != null) {
				Set<String> keys = props.keySet();
				for (String key : keys) {
					urlConnection.setRequestProperty(key, props.get(key));
				}
			}
			
			InputStream is = urlConnection.getInputStream();
			String enc = urlConnection.getContentEncoding() == null ? "UTF-8" : urlConnection.getContentEncoding();
			String jsonReq = IOUtils.toString(is,enc);
			
			retObj = jsonReq;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return retObj;
	}
	
	public static String getVersion() {
		return GlobalDefs.BOT_VERSION + (GlobalDefs.BOT_DEV ? "-devel" : "");
	}
	
	public static class Tuple<T> {
		public Tuple(T first, T second) {
			_first = first;
			_second = second;
		}
		public Tuple() {
			
		}
		private T _first;
		private T _second;
		public void setFirst(T first) {
			_first = first;
		}
		public void setSecond(T second) {
			_second = second;
		}
		public T getFirst() {
			return _first;
		}
		public T getSecond() {
			return _second;
		}
	}
	
	public static class JsonConverter {
		Class<?> _requestclass;
		String jsonFileName;

		public JsonConverter(String jsonFileName, Class<?> requestObject){
		    this.jsonFileName = jsonFileName;
		    _requestclass = requestObject;
		}

		public JSONObject getJsonObject(){
		    //Create input stream
		    InputStream inputStreamObject = getRequestclass().getResourceAsStream(jsonFileName);
	
		   try {
		       BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStreamObject, "UTF-8"));
		       StringBuilder responseStrBuilder = new StringBuilder();
	
		       String inputStr;
		       while ((inputStr = streamReader.readLine()) != null)
		           responseStrBuilder.append(inputStr);
	
		       JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());
		       return jsonObject;
	
		   } catch (IOException e) {
		       e.printStackTrace();
		   } catch (JSONException e) {
		       e.printStackTrace();
		   }
	
		    //if something went wrong, return null
		    return null;
		}
	
		private Class<?> getRequestclass(){
		    return _requestclass;
		}
	}
}
