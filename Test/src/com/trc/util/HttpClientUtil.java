package com.trc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.trc.exception.BizException;

public class HttpClientUtil {

	public HttpClientUtil(){
		PropertyConfigurator.configure("D:/GitRepository/Test/src/log4j.properties");
	}
	
	private static Log log = LogFactory.getLog(HttpClientUtil.class);

	/**
	 * httpClient客户端post方式请求
	 * 
	 * @Title: postMethod
	 * @Description:
	 * @param @param url 接口地址
	 * @param @param paramMap 参数： 如：[{"aaa", "bbb"},{"data":{"test":"123"}}]
	 *        表示2个参数aaa=bbb和data={"test":"123"}
	 * @param @return
	 * @return String
	 * @throws
	 */
	public String postMethod(String url, Map<String, String> paramMap) {
		Date dateStart = new Date();
		long start = System.nanoTime();
		log.warn("http调用开始时间:" + DateUtils.dateToNormalFullString(dateStart)
				+ ">>>>>>>>>>>>>>>>>");
		if (StringUtils.isEmpty(url)) {
			throw new BizException("http接口调用地址参数url不能为空");
		}
		if (null == paramMap) {
			throw new BizException("http接口调用参数paramMap不能为空");
		}
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setContentCharset("UTF-8");
		PostMethod postMethod = new PostMethod(url);
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			NameValuePair nameValuePair = new NameValuePair(entry.getKey(),
					entry.getValue());
			paramList.add(nameValuePair);
		}
		NameValuePair[] data = paramList.toArray(new NameValuePair[paramList
				.size()]);
		postMethod.setRequestBody(data);
		JSONObject paramObj = JSONObject.fromObject(paramMap);
		String responseMsg = "";
		try {
			log.warn("Http接口POST调用请求：" + url + "，调用参数：" + paramObj.toString());
			httpClient.executeMethod(postMethod);
			responseMsg = postMethod.getResponseBodyAsString().trim();
			log.warn("Http接口调用返回结果：" + responseMsg);
		} catch (HttpException e) {
			log.warn("Http接口调用异常：" + e);
		} catch (IOException e) {
			log.warn("Http接口调用IO异常：" + e);
		} finally {
			postMethod.releaseConnection();
			httpClient.getHttpConnectionManager().closeIdleConnections(0);
		}
		Date dateEnd = new Date();
		long end = System.nanoTime();
		log.warn("http调用返回结果:" + responseMsg);
		log.warn("http调用结束时间:" + DateUtils.dateToNormalFullString(dateEnd)
				+ ", 耗时" + DateUtils.getMilliSecondBetween(start, end)
				+ "毫秒<<<<<<<<<<<<<<<<");
		return responseMsg;
	}

	/**
	 * httpClient客户端post方式请求
	 * 
	 * @Title: postMethod
	 * @Description:
	 * @param @param url 接口地址
	 * @param @param paramMap 参数： 如：[{"aaa", "bbb"},{"data":{"test":"123"}}]
	 *        表示2个参数aaa=bbb和data={"test":"123"}
	 * @param @return
	 * @return String
	 * @throws
	 */
	public AppResult getMethod(String url, Map<String, String> paramMap) {
		if (StringUtils.isEmpty(url)) {
			throw new BizException("http接口调用地址参数url不能为空");
		}
		if (null == paramMap) {
			throw new BizException("http接口调用参数paramMap不能为空");
		}
		Date dateStart = new Date();
		long start = System.nanoTime();
		log.warn("http调用开始时间:" + DateUtils.dateToNormalFullString(dateStart)
				+ ">>>>>>>>>>>>>>>>>");
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setContentCharset("UTF-8");
		StringBuilder stringBuilder = new StringBuilder();
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			stringBuilder.append(entry.getKey());
			stringBuilder.append("=");
			if(null != entry.getValue())
				stringBuilder.append(String.valueOf(entry.getValue()));
			else{
				stringBuilder.append("");
			}
			stringBuilder.append("&");
		}
		String paramStr = stringBuilder.toString();
		if (paramStr.length() > 0) {
			paramStr = paramStr.substring(0, paramStr.length() - 1);
		}
		String invokeUrl = url + "?" + paramStr;
		GetMethod getMethod = new GetMethod(invokeUrl);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		BufferedReader br = null;
		String responseMsg = "";
		try {
			JSONObject paramObj = JSONObject.fromObject(paramMap);
			log.warn("Http接口GET调用请求url：" + invokeUrl + "请求参数："
					+ paramObj.toString());
			httpClient.executeMethod(getMethod);
			InputStream ins = getMethod.getResponseBodyAsStream();
			br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
			StringBuffer sbf = new StringBuffer();
			while ((responseMsg = br.readLine()) != null) {
				sbf.append(responseMsg);
			}
			responseMsg = sbf.toString();
			log.warn("Http接口调用返回结果：" + responseMsg);
		} catch (HttpException e) {
			log.warn("Http接口调用异常：" + e);
		} catch (IOException e) {
			log.warn("Http接口调用IO异常：" + e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				log.warn("关闭Http返回结果流异常", e);
			}
			// 6.释放连接
			getMethod.releaseConnection();
			httpClient.getHttpConnectionManager().closeIdleConnections(0);
		}
		Date dateEnd = new Date();
		long end = System.nanoTime();
		log.warn("http调用返回结果:" + responseMsg);
		log.warn("http调用结束时间:" + DateUtils.dateToNormalFullString(dateEnd)
				+ ", 耗时" + DateUtils.getMilliSecondBetween(start, end)
				+ "毫秒<<<<<<<<<<<<<<<<");
		return (AppResult)JSONObject.toBean(JSONObject.fromObject(responseMsg), AppResult.class);
	}

	public static void main(String[] args) {
		HttpClientUtil clientUtil = new HttpClientUtil();

		Map<String , String> params =new HashMap();

		//请求时间戳是年月日时分秒不加分隔符20161108115220

		Date time=new Date();
		SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");//验证里面需要的时间格式
		String date = format.format(time);

		format=new SimpleDateFormat("yyyy-MM-ddHH:mm:ss"); //_request_time需要的时间格式
		String date1=format.format(time);
		System.out.println();
		params.put("_request_time",date);
		System.out.println(date);

		//货主用100，sku:12030410005508    XS0000000354
		String data="{\n" +
				"    \"header\": [\n" +
				"        {\n" +
				"            \"order_no\": \"XS0000000354\",\n" +
				"            \"order_type\": \"F1\",\n" +
				"            \"order_time\": \"2016.10.25 17:22:20\",\n" +
				"            \"expected_shipment_time1\": \"\",\n" +
				"            \"customer_id\": \"100\",\n" +
				"            \"soreference2\": \"HL201611011715\",\n" +
				"            \"soreference3\": \"\",\n" +
				"            \"soreference5\": \"221046263197\",\n" +
				"            \"delivery_no\": \"221046263197\",\n" +
				"            \"consignee_id\": \"XN\",\n" +
				"            \"consignee_name\": \"RECEIVER\",\n" +
				"            \"c_country\": \"\",\n" +
				"            \"c_province\": \"湖北\",\n" +
				"            \"c_city\": \"武汉\",\n" +
				"            \"c_tel1\": \"18062099618\",\n" +
				"            \"c_tel2\": \"\",\n" +
				"            \"c_zip\": \"313100\",\n" +
				"            \"c_mail\": \"\",\n" +
				"            \"c_address1\": \"芳草二路观澜高尔夫公馆4号楼1801\",\n" +
				"            \"c_address2\": \"经济技术开发区\",\n" +
				"            \"c_address3\": \"大头笔\",\n" +
				"            \"userdefine4\": \"\",\n" +
				"            \"userdefine5\": \"\",\n" +
				"            \"invoice_print_flag\": \"\",\n" +
				"            \"notes\": \"2345678\",\n" +
				"            \"h_edi_01\": \"\",\n" +
				"            \"h_edi_02\": \"800\",\n" +
				"            \"h_edi_03\": \"\",\n" +
				"            \"h_edi_04\": \"\",\n" +
				"            \"h_edi_05\": \"\",\n" +
				"            \"h_edi_06\": \"\",\n" +
				"            \"h_edi_07\": \"\",\n" +
				"            \"h_edi_08\": \"\",\n" +
				"            \"h_edi_09\": \"\",\n" +
				"            \"h_edi_10\": \"0\",\n" +
				"            \"warehouse_id\": \"330156K002\",\n" +
				"            \"routecode\": \"\",\n" +
				"            \"stop\": \"\",\n" +
				"            \"carriermail\": \"\",\n" +
				"            \"carrierfax\": \"\",\n" +
				"            \"channel\": \"\",\n" +
				"            \"carrier_id\": \"STO\",\n" +
				"            \"carrier_name\": \"\",\n" +
				"            \"detailsItem\": [\n" +
				"                {\n" +
				"                    \"line_no\": \"1\",\n" +
				"                    \"customer_id\": \"100\",\n" +
				"                    \"sku\": \"11010110001085\",\n" +
				"                    \"lotatt01\": \"\",\n" +
				"                    \"lotatt02\": \"\",\n" +
				"                    \"lotatt03\": \"\",\n" +
				"                    \"lotatt08\": \"\",\n" +
				"                    \"lotatt12\": \"\",\n" +
				"                    \"qty_ordered\": \"2\",\n" +
				"                    \"userdefine6\": \"\",\n" +
				"                    \"notes\": \"轻拿轻放\",\n" +
				"                    \"price\": \"200\"\n" +
				"                }\n" +
				"            ]\n" +
				"        }\n" +
				"    ]\n" +
				"}\n";
		//2016-07-12 11:35:41
		/*String data="{\n" +
				"      \"customer_id\": \"100\",\n" +
				"      \"sku_group6\": \"\",\n" +
				"      \"time\": \"\",\n" +
				"      \"page_size\": \"\",\n" +
				"      \"page_no\": \"\"\n" +
				"    }";*/
		System.out.println(data);
		params.put("data",data);

		params.put("cmd","putSOData");

		params.put("_api_key","123456");

		params.put("_api_username","API-USER-ERP");


		// String timeTemp=time.getTime()+"";
		///res/zjmierp/wms/gateway + _api_key的值+_api_username的值+时间戳

		String sign= DigestUtils.md5Hex("/res/zjmierp/wms/gateway"+"API-USER-ERP"+"123456"+date);
		System.out.println("/res/zjmierp/wms/gateway"+"API-USER-ERP"+"123456"+date);
		System.out.println(sign);
		params.put("_sign",sign);
		//String url, Map<String, String> params,String encode

		String jsonData=null;
		String result =null;
		try {//"http://apitest.zjmiec.cn/api/res/zjmierp/wms/gateway", params, 1000
			result = clientUtil.postMethod("http://apitest.zjmiec.cn/api/res/zjmierp/wms/gateway",params);
			System.out.println(result);

		}catch (Exception e){
			e.getMessage();
		}
		for (String key:params.keySet()) {
			System.out.println("key="+key+"::"+params.get(key));
		}
	}
}
