import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
/**
 * HttpManager Ŭ����
 * - getDoc(url): url�� �ش��ϴ� doc��ȯ
 * - getDoc(): ���簴ü�� doc ��ȯ
 * �뵵
 * - url���ش��ϴ� doc�� Ȱ���Ͽ� �� ��л� �ļ� Ŭ�������� Elem�� �����ϵ��� ��
 * - �� ��л� Ŭ�������� �����!
 * 
 * @author ��ȣ��
 * ������¥: 2017-01-23
 */

public class HttpManager {
	private String tempUrl; //������Url
	private HttpGet http;
	private HttpClient httpClient;
	private HttpResponse response;
	private HttpEntity entity;

	private BufferedReader br;
	private StringBuffer sb;

	String result = ""; //doc = Jsoup.parse(result);
	Document doc = null; //DOM ��ü

	HttpManager(){
		//NuLL~~~~~~~~~~
	}
	HttpManager(String _tempUrl){
		tempUrl=_tempUrl;
		/** ����
		 * 
		 */
	}//constructor
	
/** getDoc
 * Url�� ������Ʈ�� �� doc�� Jsoup.parse�������
 * @param _tempUrl
 * @return doc
 */
	public Document getDoc(String _tempUrl){
		tempUrl = _tempUrl;
		try{
			// Http ��û�ؼ� doc�� �������
			http = new HttpGet(tempUrl); //tempUrl ����
			httpClient = HttpClientBuilder.create().build();
			response = httpClient.execute(http);
			entity = response.getEntity();
			ContentType content = ContentType.getOrDefault(entity);
			Charset charset = content.getCharset();
			br = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			result = sb.toString();
			doc = Jsoup.parse(result); //doc�� tempUrl�� DOM����
		} catch (IOException e) {
			e.printStackTrace();
		} finally { //SearchManager���ִ°� �״�� �����ϴ¤� ������ �ʿ���³��� �ּ�ó����
			//setPage();//maxPage <= total / NUM (�ִ� ������ ��)
			//page = 1;
			//prevSdate = curSdate; // �ʿ����
			//prevEdate = curEdate;
			//saveAsHTML(result);
			//parseArticle(doc);
			return doc;
		} //finally
	} //setUrl
	
	
	/** getDoc()
	 * param ���������� doc�� get�Ҽ��ִ� �޼ҵ� (�����ε�)
	 * @return doc
	 */
	public Document getDoc(){
		return doc;
	}
}
