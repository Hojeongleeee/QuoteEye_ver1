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
 * HttpManager 클래스
 * - getDoc(url): url에 해당하는 doc반환
 * - getDoc(): 현재객체의 doc 반환
 * 용도
 * - url에해당하는 doc을 활용하여 각 언론사 파서 클래스에서 Elem을 추출하도록 함
 * - 각 언론사 클래스에서 재사용됨!
 * 
 * @author 이호정
 * 수정날짜: 2017-01-23
 */

public class HttpManager {
	private String tempUrl; //접속할Url
	private HttpGet http;
	private HttpClient httpClient;
	private HttpResponse response;
	private HttpEntity entity;

	private BufferedReader br;
	private StringBuffer sb;

	String result = ""; //doc = Jsoup.parse(result);
	Document doc = null; //DOM 객체

	HttpManager(){
		//NuLL~~~~~~~~~~
	}
	HttpManager(String _tempUrl){
		tempUrl=_tempUrl;
		/** 할일
		 * 
		 */
	}//constructor
	
/** getDoc
 * Url을 업데이트한 후 doc에 Jsoup.parse결과저장
 * @param _tempUrl
 * @return doc
 */
	public Document getDoc(String _tempUrl){
		tempUrl = _tempUrl;
		try{
			// Http 요청해서 doc에 저장까지
			http = new HttpGet(tempUrl); //tempUrl 접속
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
			doc = Jsoup.parse(result); //doc에 tempUrl의 DOM저장
		} catch (IOException e) {
			e.printStackTrace();
		} finally { //SearchManager에있는것 그대로 복붙하는ㅂ ㅏ람에 필요없는내용 주석처리함
			//setPage();//maxPage <= total / NUM (최대 페이지 수)
			//page = 1;
			//prevSdate = curSdate; // 필요없음
			//prevEdate = curEdate;
			//saveAsHTML(result);
			//parseArticle(doc);
			return doc;
		} //finally
	} //setUrl
	
	
	/** getDoc()
	 * param 없을때에도 doc을 get할수있는 메소드 (오버로딩)
	 * @return doc
	 */
	public Document getDoc(){
		return doc;
	}
}
