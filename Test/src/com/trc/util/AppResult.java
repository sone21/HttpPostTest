package com.trc.util;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;

public class AppResult {
	
	public static final String APPCODE = "appcode";
	public static final String DATABUFFER = "databuffer";
	public static final String RESULT = "result";

	private String appcode;
	private String databuffer;
	private Object result;
	
	public AppResult(){
	}
	
	public AppResult(String appcode, String databuffer, Object result){
		this.appcode = appcode;
		this.databuffer = databuffer;
		this.result = result;
	}
	
	public String returnJson(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("appcode", this.appcode);
		jsonObject.put("databuffer", this.databuffer);
		jsonObject.put("result", this.result);
		return jsonObject.toString();
	}
	
	public static boolean resultValide(String result){
		if(StringUtils.isEmpty(result)){
			return false;
		}else{
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject = JSONObject.fromObject(result);
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}
	
	
	public static AppResult getAppResult(String result){
		JSONObject jsonObject = JSONObject.fromObject(result);
		AppResult appResult = new AppResult(jsonObject.getString("appcode"),
				jsonObject.getString("databuffer"),
				jsonObject.getString("result"));
		return appResult;
	}
	
	
	public String getAppcode() {
		return appcode;
	}
	public void setAppcode(String appcode) {
		this.appcode = appcode;
	}
	public String getDatabuffer() {
		return databuffer;
	}
	public void setDatabuffer(String databuffer) {
		this.databuffer = databuffer;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	
	
	
}
