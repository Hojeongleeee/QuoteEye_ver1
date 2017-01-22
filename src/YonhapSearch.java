import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mysql.jdbc.PreparedStatement;
import com.opencsv.CSVWriter;

public class YonhapSearch extends SearchManager{
	//쿼리에 들어가는
	int page = 1; // 초기 1페이지 (기사)
	int total = 0; //getTotal() 로 ..
	int maxPage = total/10; //10개씩 기사 출력
//	String sDate = "20160413"; //시작일 초기화
//	String eDate = "20170120"; //종료일 초기화
	String url_candidate = "query=%EB%B0%98%EA%B8%B0%EB%AC%B8"+"&";
	String url_ctype = "ctype=A"+"&"; //?
	String url_from = "from="+"20160413"+"&"; //시작날짜
	String url_to = "to="+"20170122"+"&"; //종료날짜
	String url_period = "peroid=diy"+"&";
	String url_page_no = "page_no="+page; //page incremental, 마지막Query
	
	String query = "?"+ url_candidate + url_ctype+url_from+url_to+url_period+url_page_no;
	String URL = "http://www.yonhapnews.co.kr/home09/7091000000.html"+query;	

	HttpManager hm = new HttpManager();
	/* 1. HttpManager 객체 만듬
	 * 1.1. sDate, eDate 설정!
	 * 2. Keyword를 받음
	 * 3. 총 기사건수 getTotal()함
	 * 4. maxpage 계산
	 * 5. n번째 링크를 각각 getDoc(url)
	 * 6. Doc을 파싱/분석해서 DB에 결과를 저장
	 * 7. n<10이면 5~6을 반복 (연합뉴스 한페이지당 기사 10개니까)
	 * 8. n=10이면 page증가 
	 * 9. page증가하면 5~6을 반복하고, page가 max에 도달하면 그만!
	 * 10. 끝
	 * 
	 */
	
	public YonhapSearch(String sdate, String edate, int _term) {
		super(sdate, edate, _term);//?
//		db.stmt.execute("select "); //DB에서 sdate, edate를 가져와라!
		
		parseArticle(hm.getDoc());
	}
	
	//sDate를 세팅? TODO
	public void setsDate(String _sDate){
		
	}
	
	//오버라이딩:연합뉴스기사의 파싱!
	private Boolean parseArticle(Document doc){
		/** 가져와야 할 것
		 * 1. search_news_list_title (total)
		 * 2. cts_article -> ul -> li -> a href 링크 10개 (기사URL)
		 * 3. 
		 */
		boolean success = false;
		Article art = null;
		Elements divs = doc.select("div.cts_article a"); //Element Select? TODO 불확실
		//art객체에 값을 세팅
//		art.setTitle(title); 
//		art.setDate(date);
//		art.setDescription(description);
//		art.setPublisher(publisher);
//		art.setUrl(url);
//		db.runSQL(candidate, art); //DB저장본격
		
		
		return success;//부모클래스보고 저절로만들어진
		
//		for (Element elem : divs) {
//			art = new Article();
//			/** 가져와야 할 것
//			 * 1. setTitle
//			 * 2. setDescription
//			 * 3. setDate
//			 * 4. setUrl
//			 * 5. setPublisher
//			 */
//			String candidate = super.getKeyword(); //검색어를 가져온다!..?
//			
//		} //for
	}
	
	

}


