package crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import bean.Stock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class XueqiuCrawler {

	public static void main(String[] args) {
		String loginUrl = "http://xueqiu.com/user/login";
		String stockUrl = "http://xueqiu.com/cubes/rebalancing/history.json?cube_symbol=ZH010389&count=20&page=1";
		
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		
		//1,读取相关参数
		//data.ini文件内容：雪球账号#雪球密码#发件邮箱#邮箱账号#收件人
		//暂时支持qq邮箱为发件邮箱
		String[] fileds = loadData("./data.ini");
		String xueqiuUser = fileds[0];
		String xueqiuPwd = fileds[1];
		String from_email_user = fileds[2];
		String from_email_pwd = fileds[3];
		String to_email = fileds[4];
		//2，模拟用户登陆
		login(httpclient,loginUrl,xueqiuUser,xueqiuPwd);
		
		//3，得到原始数据
		HttpEntity entity = getHtmlEntity(httpclient,stockUrl);
		String body = null;
		try {
			body = EntityUtils.toString(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("body:" + body);
		
		//4，抽取需要数据
		List results = parseJson(body);
		
		List stocks = new ArrayList<Stock>();
		for(Object obj :results){
			Map stockMap = JSON.parseObject(obj.toString(),Map.class);
			String name = (String) stockMap.get("stock_name");
			String symbol = (String) stockMap.get("stock_symbol");
			BigDecimal price = (BigDecimal) stockMap.get("price");
			BigDecimal prev_weight = (BigDecimal) stockMap.get("prev_weight");
			BigDecimal target_weight = (BigDecimal) stockMap.get("target_weight");
			Long updateTime = (Long) stockMap.get("updated_at");
			Stock stock = new Stock();
			stock.setName(name);
			stock.setSymbol(symbol);
			stock.setPrice(price);
			stock.setPrev_weight(prev_weight);
			stock.setTarget_weight(target_weight);
			stock.setUpdateTime(formatTimeStamp(new Timestamp(updateTime)));
			stocks.add(stock);
		}
		
		//5，发送邮件
		try {
			sendEmail(from_email_user,from_email_pwd,to_email,stocks);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private static String[] loadData(String path) {
		 BufferedReader bf = null;
		 String []fields = null;
		try {
			bf = new BufferedReader(new FileReader(new File(path)));
			String data = bf.readLine();
			fields = data.split("#");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fields;
	}


	/**
	 * 发送邮件
	 * @param from_addr
	 * @param password
	 * @param to_addr
	 * @param stocks
	 * @throws MessagingException
	 */
	private static void sendEmail(String from_addr, String password,
			String to_addr, List stocks) throws MessagingException {
		final Properties properties = new Properties();
		
		// 表示SMTP发送邮件，需要进行身份验证
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.host", "smtp.qq.com");
		
		// 发件人的账号
		properties.put("mail.user", from_addr);
        // 访问SMTP服务时需要提供的密码
		properties.put("mail.password", password);
		
		 // 构建授权信息，用于进行SMTP进行身份验证
		Authenticator authenticator = new Authenticator(){
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				String userName = properties.getProperty("mail.user");
                String password = properties.getProperty("mail.password");
				return new PasswordAuthentication(userName, password);
			}
		};
		
		// 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(properties, authenticator);
        
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress form = new InternetAddress(
        		properties.getProperty("mail.user"));
        message.setFrom(form);
        
     // 设置收件人
        InternetAddress to = new InternetAddress(to_addr);
        message.setRecipient(RecipientType.TO, to);
        
     // 设置邮件标题
        message.setSubject("java 测试");
     // 设置邮件的内容体
        message.setContent(stocks.toString(),"text/html;charset=UTF-8");
        
     // 发送邮件
        Transport.send(message);
        System.out.println("发送邮件完毕");
	}

	private static String formatTimeStamp(Timestamp timestamp){
		String tsStr = "";   
	    DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    tsStr = sdf.format(timestamp);
		return tsStr;
	}
	
	
	
	private static List parseJson(String body) {
		JSONObject obj = JSON.parseObject(body);
		Object list = obj.get("list");
		JSONArray jsonList = JSON.parseArray(list.toString());
		JSONObject histories = (JSONObject) jsonList.get(0);
		Object jsonSockList = histories.get("rebalancing_histories");
		List<Object> Socklist = JSON.parseArray(jsonSockList.toString());
		
		return Socklist;
	}

	/**
	 * 
	 * @param httpclient
	 * @param stockUrl
	 * @return
	 */
	private static HttpEntity getHtmlEntity(CloseableHttpClient httpclient,
			String stockUrl) {
		HttpGet httpGet = new HttpGet(stockUrl);
		HttpResponse resp = null;
		try {
			resp = httpclient.execute(httpGet);
			if(resp.getStatusLine().getStatusCode() == 400){
				for(Header header : resp.getAllHeaders()){
					System.out.println(header.getName() + " : " + header.getValue());
				}
			}else{
				System.out.println("statusCode " + resp.getStatusLine().getStatusCode());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp.getEntity();
	}

	/**
	 * 
	 * @param httpClient
	 * @param loginUrl
	 * @param username
	 * @param password
	 */
	private static void login(CloseableHttpClient httpClient, String loginUrl,String username,String password) {

		try {
			HttpPost post = new HttpPost(loginUrl);
			post.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
			
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();  
			nvps.add(new BasicNameValuePair("username", username));  
			nvps.add(new BasicNameValuePair("password", password));  
			post.setEntity(new UrlEncodedFormEntity(nvps,"utf-8")); 
			
			HttpResponse loginResponse = httpClient.execute(post);
			System.out.println("statusCode :" + loginResponse.getStatusLine().getStatusCode());
			if (loginResponse.getStatusLine().getStatusCode() == 302) {  
			    String locationUrl=loginResponse.getLastHeader("Location").getValue();  
			    HttpEntity locationEntity = getHtmlEntity(httpClient,locationUrl);//跳转到重定向的url  
//			    System.out.println("locationEntity" + EntityUtils.toString(locationEntity));
			   }  
			post.releaseConnection();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}
