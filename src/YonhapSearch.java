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
	//������ ����
	int page = 1; // �ʱ� 1������ (���)
	int total = 0; //getTotal() �� ..
	int maxPage = total/10; //10���� ��� ���
//	String sDate = "20160413"; //������ �ʱ�ȭ
//	String eDate = "20170120"; //������ �ʱ�ȭ
	String url_candidate = "query=";
	String url_ctype = "ctype=A"+"&"; //?
	String url_from = "from="; //���۳�¥
	String url_to = "to="+"20170122"+"&"; //���ᳯ¥
	String url_period = "period=diy"+"&";
	String url_page_no = "page_no="+page; //page incremental, ������Query

	String URL = "";
	
	//HttpManager hm = new HttpManager();
	//HTTP��û�� ���� �⤱��������
	private String tempUrl; //������Url
	private HttpGet http;
	private HttpClient httpClient;
	private HttpResponse response;
	private HttpEntity entity;

	private BufferedReader br;
	private StringBuffer sb;

	String result = ""; //doc = Jsoup.parse(result);	
	
	/* 1. HttpManager ��ü ����
	 * 1.1. sDate, eDate ����!
	 * 2. Keyword�� ����
	 * 3. �� ���Ǽ� getTotal()��
	 * 4. maxpage ���
	 * 5. n��° ��ũ�� ���� getDoc(url)
	 * 6. Doc�� �Ľ�/�м��ؼ� DB�� ����� ����
	 * 7. n<10�̸� 5~6�� �ݺ� (���մ��� ���������� ��� 10���ϱ�)
	 * 8. n=10�̸� page���� 
	 * 9. page�����ϸ� 5~6�� �ݺ��ϰ�, page�� max�� �����ϸ� �׸�!
	 * 10. ��
	 * 
	 */
	
	public YonhapSearch(String sDate, String eDate, String keyword) throws UnsupportedEncodingException {

		//�Ű������� ���� Ű����� ���� URL�� �ϼ��ϴ� �κ�
		url_candidate = URLEncoder.encode(keyword, "UTF-8");
		this.URL = "http://www.yonhapnews.co.kr/home09/7091000000.html?query="+url_candidate+"&ctype=A&from="+sDate+"&to="+eDate+"&period=diy&page_no="+page;
		System.out.println(URL);
		
		//�˻������� �� ��Ͽ��� URL �ܾ���� �Լ�
		ArrayList <String> URLlist = setURLlist(URL); //�߿�!!!
		
		//URL���� �����Ͽ� parse �� DB ����
//		for ( String temp_url : URLlist ){
//			parseArticle(hm.getDoc(temp_url));
//		}
	}
	
	
	//�������̵�:���մ�������� �Ľ�!
	@SuppressWarnings("null")
	private Boolean parseArticle(Document doc){
		/** �����;� �� ��
		 * 1. search_news_list_title (total)
		 * 2. cts_article -> ul -> li -> a href ��ũ 10�� (���URL)
		 * 3. 
		 */
		boolean success = false;
		Article art = null;
//		String divs = doc.select("div.cts_article a").toString(); //Element Select? TODO ��Ȯ��
		String title = doc.select("").toString();
		String date = doc.select("").toString();
		String description = doc.select("").toString();
		String publisher = doc.select("").toString();
		String url = this.URL;
				
		//art��ü�� ���� ����
		art.setTitle(title); 
		art.setDate(date);
		art.setDescription(description);
		art.setPublisher(publisher);
		art.setUrl(url);
//		db.runSQL(candidate, art); //DB���庻��

		//Test�� �α�
		System.out.println("title\t"+art.getTitle());
		System.out.println("date\t"+art.getDate());
		System.out.println("description\t"+art.getDescription());
		System.out.println("publisher\t"+art.getPublisher());
		System.out.println("url\t"+art.getUrl());
		
		return success;//�θ�Ŭ�������� �����θ������
		
//		for (Element elem : divs) {
//			art = new Article();
//			/** �����;� �� ��
//			 * 1. setTitle
//			 * 2. setDescription
//			 * 3. setDate
//			 * 4. setUrl
//			 * 5. setPublisher
//			 */
//			String candidate = super.getKeyword(); //�˻�� �����´�!..?
//			
//		} //for
	}
	
	
	/**
	 * 
	 * @param URL 
	 * @return
	 */
	//url�� �ܾ ����Ʈ��? ����
	//Http��û���� ���� �� �޼ҵ忡 ���� �κ�
	private ArrayList<String> setURLlist(String URL){
		//
		Document doc = null;
		
		
		//http��û �� doc�� parse��� ����
		try{
			// Http ��û�ؼ� doc�� �������
			http = new HttpGet(URL); //tempUrl ����
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
			doc = Jsoup.parse(result); //doc�� tempUrl�� DOM����
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//�˻� ����Ʈ �������� doc ����
		//HttpManager temp_hm = new HttpManager(URL);
		
		
		//1���������� max���������� �������� 10�� URL�� �������� �޷� ������!
		ArrayList<String> URLlist = new ArrayList<>(); 
	
		//��ü���Ǽ� �� �������� ����(totalText, total, maxPage) span.total text (2����)
		//System.out.println(doc.toString());
		String totalText = doc.select("#article_list").select(".total").toString(); //�˼�����
		System.out.println("totalText:"+totalText);
		Elements linklist = doc.select("#article_list h3 > a");
		System.out.println("linklist.toString():"+linklist.toString());
		//linklist.add(doc);
		//System.out.println(linklist.get(0).toString());
		//System.out.println(linklist.get(1).toString());
		//System.out.println(linklist.get(2).toString());

		//�ӽø�ũ
		URLlist.add("http://www.yonhapnews.co.kr/bulletin/2017/01/29/0200000000AKR20170129035600004.HTML?from=search");
//		this.maxPage = Integer.getInteger(totalText)/10; //�������� 10��
//		URLlist.add(doc.select("div.cts_arclst li a [href]").get(0).toString());
		
		//�α�
	
		System.out.println("total\t"+total);
		for ( String URLitr : URLlist)
			System.out.println("link\t"+ URLitr);

		return URLlist;
	}
	
	/**
	 * URL����Ʈ ���� �� ���: max>page�� ��� page 1 ����
	 * @return success
	 */
	private boolean incPage(){
		if (maxPage>page){
			page++;
			this.url_page_no = "page_no="+this.page; //page incremental, ������Query
			setURL(url_candidate,url_from,url_to,page); //URL ����
		} else {
			return false; //maxpage ����(page���� X)
		}

		return true; //page���� �Ϸ�
	}
	
	/**
	 * URL����Ʈ ���� �� ���: ���Ӱ����� ���� URL �ϼ���Ű�� �޼ҵ�
	 * @param url_candidate
	 * @param from
	 * @param to
	 * @param page
	 * @return URL
	 */
	private String setURL(String url_candidate, String from, String to, int page){
		this.url_candidate = "query=%EB%B0%98%EA%B8%B0%EB%AC%B8"+"&";
		this.url_ctype = "ctype=A"+"&"; //?
		this.url_from = "from="+from+"&"; //���۳�¥
		this.url_to = "to="+to+"&"; //���ᳯ¥
		this.url_period = "period=diy"+"&";
		this.url_page_no = "page_no="+page; //page incremental, ������Query
		
		return URL;
	}

}


