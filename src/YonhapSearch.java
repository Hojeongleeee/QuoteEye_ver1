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

public class YonhapSearch {
	//쿼리에 들어가는
	int page = 1; // 초기 1페이지 (기사)
	int total = 0; //getTotal() 로 ..
	int maxPage = total/10; //10개씩 기사 출력
//	String sDate = "20160413"; //시작일 초기화
//	String eDate = "20170120"; //종료일 초기화
	String url_candidate = "query=";
	String url_ctype = "ctype=A"+"&"; //?
	String url_from = "from="; //시작날짜
	String url_to = "to="+"20170122"+"&"; //종료날짜
	String url_period = "period=diy"+"&";
	String url_page_no = "page_no="+page; //page incremental, 마지막Query

	String URL = "";
	
	//HttpManager hm = new HttpManager();
	//HTTP요청을 위한 멤ㅁ버변수들
	private String tempUrl; //접속할Url
	private HttpGet http;
	private HttpClient httpClient;
	private HttpResponse response;
	private HttpEntity entity;

	private BufferedReader br;
	private StringBuffer sb;

	String result = ""; //doc = Jsoup.parse(result);	
	
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
	
	public YonhapSearch(String sDate, String eDate, String keyword) throws UnsupportedEncodingException {

		//매개변수로 받은 키워드로 최종 URL을 완성하는 부분
		url_candidate = URLEncoder.encode(keyword, "UTF-8");
		this.URL = "http://www.yonhapnews.co.kr/home09/7091000000.html?query="+url_candidate+"&ctype=A&from="+sDate+"&to="+eDate+"&period=diy&page_no="+page;
		System.out.println(URL);
		
		//검색페이지 및 목록에서 URL 긁어오는 함수
		ArrayList <String> URLlist = setURLlist(URL); //중요!!!
		
		//URL마다 접속하여 parse 및 DB 저장
//		for ( String temp_url : URLlist ){
//			parseArticle(hm.getDoc(temp_url));
//		}
	}
	
	
	//오버라이딩:연합뉴스기사의 파싱!
	@SuppressWarnings("null")
	private Boolean parseArticle(Document doc){
		/** 가져와야 할 것
		 * 1. search_news_list_title (total)
		 * 2. cts_article -> ul -> li -> a href 링크 10개 (기사URL)
		 * 3. 
		 */
		boolean success = false;
		Article art = null;
//		String divs = doc.select("div.cts_article a").toString(); //Element Select? TODO 불확실
		String title = doc.select("").toString();
		String date = doc.select("").toString();
		String description = doc.select("").toString();
		String publisher = doc.select("").toString();
		String url = this.URL;
				
		//art객체에 값을 세팅
		art.setTitle(title); 
		art.setDate(date);
		art.setDescription(description);
		art.setPublisher(publisher);
		art.setUrl(url);
//		db.runSQL(candidate, art); //DB저장본격

		//Test용 로깅
		System.out.println("title\t"+art.getTitle());
		System.out.println("date\t"+art.getDate());
		System.out.println("description\t"+art.getDescription());
		System.out.println("publisher\t"+art.getPublisher());
		System.out.println("url\t"+art.getUrl());
		
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
	
	
	/**
	 * 
	 * @param URL 
	 * @return
	 */
	//url만 긁어서 리스트에? 저장
	//Http요청까지 전부 한 메소드에 담은 부분
	private ArrayList<String> setURLlist(String URL){
		//
		Document doc = null;
		
		
		//http요청 및 doc에 parse결과 저장
		try{
			// Http 요청해서 doc에 저장까지
			http = new HttpGet(URL); //tempUrl 접속
			httpClient = HttpClientBuilder.create().build();
			response = httpClient.execute(http);
			entity = response.getEntity();
			ContentType content = ContentType.getOrDefault(entity);
			Charset charset = content.getCharset();
			charset = content.getCharset();
			br = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			result = sb.toString();
			doc = Jsoup.parse(result); //doc에 tempUrl의 DOM저장
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//검색 리스트 페이지로 doc 설정
		//HttpManager temp_hm = new HttpManager(URL);
		
		
		//1페이지부터 max페이지까지 페이지당 10개 URL을 연속으로 쭈룩 저장함!
		ArrayList<String> URLlist = new ArrayList<>(); 
	
		//전체기사건수 및 페이지수 저장(totalText, total, maxPage) span.total text (2개만)
		//System.out.println(doc.toString());
		String totalText = doc.select("#article_list").select(".total").toString(); //알수없음
		System.out.println("totalText:"+totalText);
		Elements linklist = doc.select("#article_list h3 > a");
		System.out.println("linklist.toString():"+linklist.toString());
		//linklist.add(doc);
		//System.out.println(linklist.get(0).toString());
		//System.out.println(linklist.get(1).toString());
		//System.out.println(linklist.get(2).toString());

		//임시링크
		URLlist.add("http://www.yonhapnews.co.kr/bulletin/2017/01/29/0200000000AKR20170129035600004.HTML?from=search");
//		this.maxPage = Integer.getInteger(totalText)/10; //페이지당 10개
//		URLlist.add(doc.select("div.cts_arclst li a [href]").get(0).toString());
		
		//로깅
	
		System.out.println("total\t"+total);
		for ( String URLitr : URLlist)
			System.out.println("link\t"+ URLitr);

		return URLlist;
	}
	
	/**
	 * URL리스트 뽑을 때 사용: max>page일 경우 page 1 증가
	 * @return success
	 */
	private boolean incPage(){
		if (maxPage>page){
			page++;
			this.url_page_no = "page_no="+this.page; //page incremental, 마지막Query
			setURL(url_candidate,url_from,url_to,page); //URL 세팅
		} else {
			return false; //maxpage 도달(page증가 X)
		}

		return true; //page증가 완료
	}
	
	/**
	 * URL리스트 뽑을 때 사용: 접속가능한 최종 URL 완성시키는 메소드
	 * @param url_candidate
	 * @param from
	 * @param to
	 * @param page
	 * @return URL
	 */
	private String setURL(String url_candidate, String from, String to, int page){
		this.url_candidate = "query=%EB%B0%98%EA%B8%B0%EB%AC%B8"+"&";
		this.url_ctype = "ctype=A"+"&"; //?
		this.url_from = "from="+from+"&"; //시작날짜
		this.url_to = "to="+to+"&"; //종료날짜
		this.url_period = "period=diy"+"&";
		this.url_page_no = "page_no="+page; //page incremental, 마지막Query
		
		return URL;
	}

}


