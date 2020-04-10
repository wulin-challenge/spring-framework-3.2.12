package cn.wulin.security.oauth2.test.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http util to send data
 * 
 * @author wubo
 */
public class HttpClientUtil {
	private static Logger logger = LoggerFactory.getLogger("");
	private static final Integer TIMEOUT = 500000;
	
	public static String get(String url, Map<String, String> params, Map<String, String> headers){
		url += getGetParmas(url, params);
		
		HttpGet httpGet = null;
		CloseableHttpClient httpClient = null;
		try{
			// httpGet config
			httpGet = new HttpGet(url);
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).build();
			httpGet.setConfig(requestConfig);

			// headers
			if (headers!=null && headers.size()>0) {
				for (Map.Entry<String, String> headerItem: headers.entrySet()) {
					httpGet.setHeader(headerItem.getKey(), headerItem.getValue());
				}
			}

			// httpClient = HttpClients.createDefault();	// default retry 3 times
			// httpClient = HttpClients.custom().setRetryHandler(new DefaultHttpRequestRetryHandler(3, true)).build();
			httpClient = HttpClients.custom().disableAutomaticRetries().build();
			
			// parse response
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (null != entity) {
				if (response.getStatusLine().getStatusCode() == 200) {
					String responseMsg = EntityUtils.toString(entity, "UTF-8");
					EntityUtils.consume(entity);
					return responseMsg;
				}
				EntityUtils.consume(entity);
			}
			logger.info("http statusCode error, statusCode:" + response.getStatusLine().getStatusCode());
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			/*StringWriter out = new StringWriter();
			e.printStackTrace(new PrintWriter(out));
			callback.setMsg(out.toString());*/
			return e.getMessage();
		} finally{
			if (httpGet!=null) {
				httpGet.releaseConnection();
			}
			if (httpClient!=null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String post(String url, Map<String, String> params, Map<String, String> headers){
		HttpPost httpPost = null;
		CloseableHttpClient httpClient = null;
		try{
			// httpPost config
			httpPost = new HttpPost(url);
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> formParams = new ArrayList<NameValuePair>();
				for(Map.Entry<String,String> entry : params.entrySet()){
					formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
			}
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).build();
			httpPost.setConfig(requestConfig);

			// headers
			if (headers!=null && headers.size()>0) {
				for (Map.Entry<String, String> headerItem: headers.entrySet()) {
					httpPost.setHeader(headerItem.getKey(), headerItem.getValue());
				}
			}

			// httpClient = HttpClients.createDefault();	// default retry 3 times
			// httpClient = HttpClients.custom().setRetryHandler(new DefaultHttpRequestRetryHandler(3, true)).build();
			httpClient = HttpClients.custom().disableAutomaticRetries().build();
			
			// parse response
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (null != entity) {
				if (response.getStatusLine().getStatusCode() == 200) {
					String responseMsg = EntityUtils.toString(entity, "UTF-8");
					EntityUtils.consume(entity);
					return responseMsg;
				}
				EntityUtils.consume(entity);
			}
			logger.info("http statusCode error, statusCode:" + response.getStatusLine().getStatusCode());
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			/*StringWriter out = new StringWriter();
			e.printStackTrace(new PrintWriter(out));
			callback.setMsg(out.toString());*/
			return e.getMessage();
		} finally{
			if (httpPost!=null) {
				httpPost.releaseConnection();
			}
			if (httpClient!=null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
     * 得到get参数
     * @param params
     * @return
     */
    private static String getGetParmas(String httpUrl,Map<String,String> params){
    	if(params == null || params.isEmpty()){
    		return "";
    	}
    	
    	StringBuffer paramsBuffer = new StringBuffer();
    	Set<Entry<String, String>> entrySet = params.entrySet();
    	boolean isFirst = true;
    	for (Entry<String, String> entry : entrySet) {
    		if(isFirst){
    			isFirst = false;
    			if(httpUrl.contains("?")){
    				paramsBuffer.append("&"+entry.getKey()+"="+entry.getValue());
    			}else{
    				paramsBuffer.append("?"+entry.getKey()+"="+entry.getValue());
    			}
    		}else{
    			paramsBuffer.append("&"+entry.getKey()+"="+entry.getValue());
    		}
		}
    	return paramsBuffer.toString();
    }

}
