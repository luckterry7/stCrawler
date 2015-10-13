package crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class BaiduCrawler {
	
	public static HttpEntity getHtmlEntity(String url){
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse resp = null;
		try {
			resp = httpClient.execute(httpGet);
			if(resp.getStatusLine().getStatusCode() == 200){
				for(Header header : resp.getAllHeaders()){
					System.out.println(header.getName() + " : " + header.getValue());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp.getEntity();
	};
	
	
	public static void saveToLocal(HttpEntity httpEntity, String filename) {

	    try {
	      File dir = new File("D:/work");
	      if (!dir.isDirectory()) {
	        dir.mkdir();
	      }

	      File file = new File(dir.getAbsolutePath() + "/" + filename);
	      FileOutputStream fileOutputStream = new FileOutputStream(file);
	      InputStream inputStream = httpEntity.getContent();

	      if (!file.exists()) {
	        file.createNewFile();
	      }
	      byte[] bytes = new byte[1024];
	      int length = 0;
	      while ((length = inputStream.read(bytes)) > 0) {
	        fileOutputStream.write(bytes, 0, length);
	      }
	      inputStream.close();
	      fileOutputStream.close();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }

	  }
	public static void main(String[] args) {
		String url = "http://www.baidu.com";
		HttpEntity entity = new BaiduCrawler().getHtmlEntity(url);
		saveToLocal(entity, "baidu.html");
	}
}
