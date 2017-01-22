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
	//������ ����
	int page = 1; // �ʱ� 1������ (���)
	int total = 0; //getTotal() �� ..
	int maxPage = total/10; //10���� ��� ���
//	String sDate = "20160413"; //������ �ʱ�ȭ
//	String eDate = "20170120"; //������ �ʱ�ȭ
	String url_candidate = "query=%EB%B0%98%EA%B8%B0%EB%AC%B8"+"&";
	String url_ctype = "ctype=A"+"&"; //?
	String url_from = "from="+"20160413"+"&"; //���۳�¥
	String url_to = "to="+"20170122"+"&"; //���ᳯ¥
	String url_period = "peroid=diy"+"&";
	String url_page_no = "page_no="+page; //page incremental, ������Query
	
	String query = "?"+ url_candidate + url_ctype+url_from+url_to+url_period+url_page_no;
	String URL = "http://www.yonhapnews.co.kr/home09/7091000000.html"+query;	

	HttpManager hm = new HttpManager();
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
	
	public YonhapSearch(String sdate, String edate, int _term) {
		super(sdate, edate, _term);//?
//		db.stmt.execute("select "); //DB���� sdate, edate�� �����Ͷ�!
		
		parseArticle(hm.getDoc());
	}
	
	//sDate�� ����? TODO
	public void setsDate(String _sDate){
		
	}
	
	//�������̵�:���մ�������� �Ľ�!
	private Boolean parseArticle(Document doc){
		/** �����;� �� ��
		 * 1. search_news_list_title (total)
		 * 2. cts_article -> ul -> li -> a href ��ũ 10�� (���URL)
		 * 3. 
		 */
		boolean success = false;
		Article art = null;
		Elements divs = doc.select("div.cts_article a"); //Element Select? TODO ��Ȯ��
		//art��ü�� ���� ����
//		art.setTitle(title); 
//		art.setDate(date);
//		art.setDescription(description);
//		art.setPublisher(publisher);
//		art.setUrl(url);
//		db.runSQL(candidate, art); //DB���庻��
		
		
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
	
	

}


