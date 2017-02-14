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
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
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
/** SearchManager 클래스
 * - Search(keyword) 후보자 각각을 키워드로 하여 본격파싱
 * - Search() 후보자에 대한 본격파싱 continue?
 * - Search(...)안에 각각의 언론사 파싱을 하는 클래스 객체를 생성
 * - nextCandidate() 키워드를 다른 사람으로 바꿈?
 * - Array! 검색어 대상을 배열로 해놓으면 안되나!
 * 
 * 
 * @author 이호정
 *
 */
public class SearchManager {
	private ArrayList<String> candidates = new ArrayList<>(); //후보자리스트!검색에활용될것
	
	private String url = "http://search.daum.net/search?w=news&cluster=n&req=tab&period=u&DA=STC"; // 검색
	// URL
	private SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
	// 날짜 포맷, 수정 금지
	final private int NUM = 50; // 기사 표시 개수
	final private int SORT = 3; // 정렬방법, 1이 최신, 3이 정확도
	private boolean done = false;

	private String tempUrl;
	private HttpGet http;
	private HttpClient httpClient;
	private HttpResponse response;
	private HttpEntity entity;

	private BufferedReader br;
	private StringBuffer sb;

	private String query; // 검색어
	private String keyword; // 저장시 키워드
	private Date startDate; // 최초 검색일
	private Date endDate; // 최종 검색일
	private int term; // 검색 날짜 텀

	private Date prevSdate;
	// 이전 검색 시작일, 뒤에 시간값 000000 필요
	private Date prevEdate;
	// 이전 검색 종료일, 뒤에 시간값 235959 필요

	private Date curSdate; // 현재 검색 시작일
	private Date curEdate; // 현재 검색 종료일

	private String stime = "000000";
	private String etime = "235959";

	private int page; // 현재 페이지 p
	private int total; // 검색 건수
	private int maxPage; // 최대 페이지

	private ArrayList<Article> list; // 기사 csv 저장용 리스트
	
	DBManager db = new DBManager(); //DBManager 생성

/**
 * 생성자
 * - ?
 * -
 * @param sdate 필요? (DB에서 가져와야함)
 * @param edate 필요? (DB에서 가져와야함)
 * @param _term 필요? (뭘까 !!!)
 */
	public SearchManager(String sdate, String edate) {
		try {
			list = new ArrayList<Article>();
//			startDate = sd.parse(sdate); // 최초일
//			endDate = sd.parse(edate); // 최종일
//			term = _term; // 검색 텀 지정
//			url = url + "&n=" + NUM; // 페이지당 표시개수

			//검색을 할 keyword들 미리 세팅
			candidates.add("반기문");
//			candidates.add("문재인");
//			candidates.add("박원순");
//			candidates.add("이재명");
//			candidates.add("안철수");
//			candidates.add("유승민");
//			candidates.add("안희정");
//			candidates.add("손학규");
//			candidates.add("검색 쿼리 활용,,,");
			
			//후보자들을 search에 쏙쏙
			for (String candidate : candidates){
				search(candidate);	
			}
			
			/**
			 * 언론사별 클래스
			 * - 시작일/종료일은 모든 언론사가 공유하는 int라고 하자 (Search 최초 1번 DB에서 불러와서 재활용)
			 * - http 매니저 코딩은 따로 클래스를 만들고, 언론사마다 http객체 1개씩!
			 * - page, total, maxpage 계산은 각자 언론사가 알아서!
			 * - dbm은 부모클래스에서 상속받아서 사용
			 * - Article 객체는 각 언론사 클래스에서 진행
			 * - DB저장은 각 언론사 클래스에서 진행
			 * 
			 */
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 검색어를 받아서 검색 시작, search()는 두번째부터
	 * 검색결과를 그때그때 DB에 저장! 1건마다 -> runSQL(String);
	 * 인물별 검색결과 저장? DB 스키마 필요
	 * 
	 * Search(){
	 *  setsdate();
	 *  setedate();
	 * 	Yonhap(반기문).parseArticle();
	 *  ... 
	 *  Joseon(반기문).parseArticle();
	 *  nextCandidate(); //다음후보자세팅
	 * }
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	@SuppressWarnings("finally")
	public boolean search(String _keyword) {

		//임시로 startDate, endDate 대신 sDate, eDate
//		String sDate = setsDate();
//		String eDate = seteDate();
		String sDate = "20170120";
		String eDate = "20170123";
		
		//언론사별로 크롤 (링크,크롤,파싱,DB저장 전 과정)
		try {
			YonhapSearch yh = new YonhapSearch(sDate, eDate, _keyword);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
//		JoongangSearch jn = new JoongangSearch(sdate, edate, _keyword);
// 		...		

//뭐지,,,
//		try {
//			query = URLEncoder.encode(_keyword, "UTF-8");
//			tempUrl = url + "&q=" + query;
//
//			curSdate = startDate;
//			curEdate = addTerm(curSdate);
//
//			if (curEdate.after(endDate) && !done) {
//				curEdate = endDate;
//				done = true;
//			}
//			tempUrl = tempUrl + "&sd=" + sd.format(curSdate) + stime + "&ed=" + sd.format(curEdate) + etime;
//			System.out.println("시작일 : " + curSdate + " 종료일: " + curEdate);
//			System.out.println("시작일 포맷 : " + sd.format(curSdate) + " 종료일 포맷 : " + sd.format(curEdate));
//			System.out.println(tempUrl);
//			
//			
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} finally {
//			return doc;
//		}
		
		return true;
	} //


	private String seteDate() {
		// TODO Auto-generated method stub
		return null;
	}

/* 여기있던 getTotal 삭제함 (언론사별로 달라서 옮겼음) */
private String setsDate() {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * ?
	 */
	public void saveAsCSV() {
		String fileName = "DAUM-" + keyword + ".csv";
		FileOutputStream fos;
		OutputStreamWriter osw;
		CSVWriter csv = null;
		try {
			fos = new FileOutputStream(fileName, true);
			osw = new OutputStreamWriter(fos, "euc-kr");
			csv = new CSVWriter(osw, ',', '"');

			for (Article art : list) {
				csv.writeNext(new String[] { String.valueOf(art.getTitle()), String.valueOf(art.getDate()),
						String.valueOf(art.getPublisher()), String.valueOf(art.getDescription()),
						String.valueOf(art.getUrl()) });
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

	}

	/**
	 * search 별표다섯개
	 * @return doc?
	 */
	@SuppressWarnings("finally")
//	public Document search() {
//		String result = "";
//		Document doc = null;
//		boolean renew = false;//?
//		String tempUrl2;
//		// System.out.println("done = " + done);
//		// 저장용 키워드
//		try {
//			// 마지막 페이지 도달 여부 확인
//			if (page >= maxPage) {
//				System.out.println("Page done");
//				saveAsCSV();
//				if (!done) {
//					changeDate();
//					renew = true;
//				} else if (done) {
//					doc = null;
//					return null;
//				}
//				// total 갱신
//			} else {
//				// 마지막 페이지가 아닌 경우, 페이지 증가 후 검색
//				page++;
//			}
//			if (renew)
//				tempUrl2 = tempUrl;
//			else
//				tempUrl2 = tempUrl + "&p=" + page; // 페이지
//			// Http 요청
//			System.out.println(tempUrl2);
//			http = new HttpGet(tempUrl2);
//			httpClient = HttpClientBuilder.create().build();
//			response = httpClient.execute(http);
//			entity = response.getEntity();
//			ContentType content = ContentType.getOrDefault(entity);
//			Charset charset = content.getCharset();
//			br = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
//			StringBuffer sb = new StringBuffer();
//			String line = "";
//			while ((line = br.readLine()) != null) {
//				sb.append(line + "\n");
//			}
//			result = sb.toString();
//			doc = Jsoup.parse(result);
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//
//			if (renew) {
//				total = getTotal(doc);
//				setPage();
//				page = 1;
//			}
//			// saveAsHTML(result);
//			if (!done)
//				parseArticle(doc);
//
//			return doc;
//		}
//	}
//
//	/**
//	 * ?
//	 */
//	private void changeDate() {
//		System.out.println("changeDate() called.");
//		prevSdate = curSdate;
//		prevEdate = curEdate;
//
//		curSdate = addTerm(prevEdate, 1);
//		curEdate = addTerm(curSdate);
//		System.out.println("시작일 : " + curSdate + " 종료일: " + curEdate);
//		System.out.println("시작일 포맷 : " + sd.format(curSdate) + " 종료일 포맷 : " + sd.format(curEdate));
//
//		if (curEdate.after(endDate)) {
//			curEdate = endDate;
//			done = true;
//		}
//		tempUrl = url + "&q=" + query;
//		tempUrl = tempUrl + "&sd=" + sd.format(curSdate) + stime + "&ed=" + sd.format(curEdate) + etime;
//	}
//
//	/**
//	 * ?
//	 */
//	private void setPage() {
//		maxPage = total / NUM;
//
//		if (total % NUM != 0) {
//			maxPage++;
//		}
//		if (maxPage > 80) {
//			maxPage = 80;
//		}
//		System.out.println("maxPage = " + maxPage);
//	}
//
//	/**
//	 * ?
//	 * @param _date
//	 * @return
//	 */
//	private Date addTerm(Date _date) {
//		Calendar cal = new GregorianCalendar();
//		cal.setTime(_date);
//		cal.add(Calendar.DAY_OF_YEAR, term);
//
//		return cal.getTime();
//	}
//
//	/**
//	 * ?
//	 * @param _date
//	 * @param _term
//	 * @return
//	 */
//	private Date addTerm(Date _date, int _term) {
//		Calendar cal = new GregorianCalendar();
//		cal.setTime(_date);
//		cal.add(Calendar.DAY_OF_YEAR, _term);
//		return cal.getTime();
//	}
//	
//	
///**
// * ?
// * @param contents
// * @return
// */
//	private Boolean saveAsHTML(String contents) {
//		@Deprecated
//		// html 저장 미사용
//		boolean success = false;
//		String fileName;
//		try {
//			fileName = "DAUM-" + keyword + "-" + sd.format(curSdate) + "-" +
//					sd.format(curEdate) + "-" + page + "-"
//					+ total + ".html";
//			FileOutputStream fos = new FileOutputStream(fileName);
//			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
//			BufferedWriter out = new BufferedWriter(osw);
//			out.write(contents);
//			out.close();
//			success = true;
//			System.out.println("[SAVE] :: " + fileName + " saved.");
//			return success;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return success;
//	}
//
//	public String getKeyword (){
//		return keyword;
//	}
/**
 * parseArticle
 * - 시작/끝날짜를 주면, 그 기간의 기사를 모두 크롤하여 DB에 저장까지 수행
 * - 각 언론사의 클래스에서 오버라이딩 구현
 * - 언론사별 html분석 제각각, 구현되는부분ㄴ
 * 
 * @param doc = dom객체를 param 받아 파싱
 * @return success여부
 * 
 * 
 */
	private Boolean parseArticle(Document doc) {
		boolean success = false;
		Article art = null;
		Elements divs = doc.select("div.cont_inner");
		// System.out.println("size of div = " + divs.size());
		/*
		 * Article Parsing 하는 곳
		 * HTML에서 직접 Elem을 가져옴
		 * art객체에 저장됨 --> 여기서 DB에 저장하는 과정 필요
		 */

		//다음API
		for (Element elem : divs) {
			art = new Article();
			String candidate = keyword;
			art.setTitle(elem.select("div.wrap_tit a").get(0).text()); 			// 제목 추출
			art.setDescription(elem.select("p.f_eb").get(0).text()); 			// 내용 추출
			art.setDate(elem.select("span.f_nb").get(0).text().substring(0, 10)); 			// 날짜 추출
			art.setUrl(elem.select("div.wrap_tit a").get(0).attr("abs:href")); 			// 링크 추출
			art.setPublisher(elem.select("span.f_nb").get(0).text().substring(10).replaceAll("[|]", "").replaceAll("\uB2E4\uC74C\uB274\uC2A4", "").trim()); // 언론사 추출
			list.add(art); //list에 추가
			db.runSQL(candidate, art); //article을 통째로 넘겨 DB 저장

//			System.out.println("제목\t"+list.get(list.size()-1).getTitle());
//			System.out.println("날짜\t"+list.get(list.size()-1).getDate());
//			System.out.println("링크\t"+list.get(list.size()-1).getUrl());
//			System.out.println("내용\t"+list.get(list.size()-1).getDescription());
//			System.out.println("언론사\t"+list.get(list.size()-1).getPublisher());
//			System.out.println();
			
		}

		return success;
	}
}

// getTotal 등의 메소드에 로깅용 출력 추가 [getTotal()] : ~
// finally는 return 전에 수행됨