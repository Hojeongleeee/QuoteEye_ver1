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
/** SearchManager Ŭ����
 * - Search(keyword) �ĺ��� ������ Ű����� �Ͽ� �����Ľ�
 * - Search() �ĺ��ڿ� ���� �����Ľ� continue?
 * - Search(...)�ȿ� ������ ��л� �Ľ��� �ϴ� Ŭ���� ��ü�� ����
 * - nextCandidate() Ű���带 �ٸ� ������� �ٲ�?
 * - Array! �˻��� ����� �迭�� �س����� �ȵǳ�!
 * 
 * 
 * @author ��ȣ��
 *
 */
public class SearchManager {
	private ArrayList<String> candidates = new ArrayList<>(); //�ĺ��ڸ���Ʈ!�˻���Ȱ��ɰ�
	
	private String url = "http://search.daum.net/search?w=news&cluster=n&req=tab&period=u&DA=STC"; // �˻�
	// URL
	private SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
	// ��¥ ����, ���� ����
	final private int NUM = 50; // ��� ǥ�� ����
	final private int SORT = 3; // ���Ĺ��, 1�� �ֽ�, 3�� ��Ȯ��
	private boolean done = false;

	private String tempUrl;
	private HttpGet http;
	private HttpClient httpClient;
	private HttpResponse response;
	private HttpEntity entity;

	private BufferedReader br;
	private StringBuffer sb;

	private String query; // �˻���
	private String keyword; // ����� Ű����
	private Date startDate; // ���� �˻���
	private Date endDate; // ���� �˻���
	private int term; // �˻� ��¥ ��

	private Date prevSdate;
	// ���� �˻� ������, �ڿ� �ð��� 000000 �ʿ�
	private Date prevEdate;
	// ���� �˻� ������, �ڿ� �ð��� 235959 �ʿ�

	private Date curSdate; // ���� �˻� ������
	private Date curEdate; // ���� �˻� ������

	private String stime = "000000";
	private String etime = "235959";

	private int page; // ���� ������ p
	private int total; // �˻� �Ǽ�
	private int maxPage; // �ִ� ������

	private ArrayList<Article> list; // ��� csv ����� ����Ʈ
	
	DBManager db = new DBManager(); //DBManager ����

/**
 * ������
 * - ?
 * -
 * @param sdate �ʿ�? (DB���� �����;���)
 * @param edate �ʿ�? (DB���� �����;���)
 * @param _term �ʿ�? (���� !!!)
 */
	public SearchManager(String sdate, String edate) {
		try {
			list = new ArrayList<Article>();
//			startDate = sd.parse(sdate); // ������
//			endDate = sd.parse(edate); // ������
//			term = _term; // �˻� �� ����
//			url = url + "&n=" + NUM; // �������� ǥ�ð���

			//�˻��� �� keyword�� �̸� ����
			candidates.add("�ݱ⹮");
//			candidates.add("������");
//			candidates.add("�ڿ���");
//			candidates.add("�����");
//			candidates.add("��ö��");
//			candidates.add("���¹�");
//			candidates.add("������");
//			candidates.add("���б�");
//			candidates.add("�˻� ���� Ȱ��,,,");
			
			//�ĺ��ڵ��� search�� ���
			for (String candidate : candidates){
				search(candidate);	
			}
			
			/**
			 * ��л纰 Ŭ����
			 * - ������/�������� ��� ��л簡 �����ϴ� int��� ���� (Search ���� 1�� DB���� �ҷ��ͼ� ��Ȱ��)
			 * - http �Ŵ��� �ڵ��� ���� Ŭ������ �����, ��л縶�� http��ü 1����!
			 * - page, total, maxpage ����� ���� ��л簡 �˾Ƽ�!
			 * - dbm�� �θ�Ŭ�������� ��ӹ޾Ƽ� ���
			 * - Article ��ü�� �� ��л� Ŭ�������� ����
			 * - DB������ �� ��л� Ŭ�������� ����
			 * 
			 */
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �˻�� �޾Ƽ� �˻� ����, search()�� �ι�°����
	 * �˻������ �׶��׶� DB�� ����! 1�Ǹ��� -> runSQL(String);
	 * �ι��� �˻���� ����? DB ��Ű�� �ʿ�
	 * 
	 * Search(){
	 *  setsdate();
	 *  setedate();
	 * 	Yonhap(�ݱ⹮).parseArticle();
	 *  ... 
	 *  Joseon(�ݱ⹮).parseArticle();
	 *  nextCandidate(); //�����ĺ��ڼ���
	 * }
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	@SuppressWarnings("finally")
	public boolean search(String _keyword) {

		//�ӽ÷� startDate, endDate ��� sDate, eDate
//		String sDate = setsDate();
//		String eDate = seteDate();
		String sDate = "20170120";
		String eDate = "20170123";
		
		//��л纰�� ũ�� (��ũ,ũ��,�Ľ�,DB���� �� ����)
		try {
			YonhapSearch yh = new YonhapSearch(sDate, eDate, _keyword);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
//		JoongangSearch jn = new JoongangSearch(sdate, edate, _keyword);
// 		...		

//����,,,
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
//			System.out.println("������ : " + curSdate + " ������: " + curEdate);
//			System.out.println("������ ���� : " + sd.format(curSdate) + " ������ ���� : " + sd.format(curEdate));
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

/* �����ִ� getTotal ������ (��л纰�� �޶� �Ű���) */
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
	 * search ��ǥ�ټ���
	 * @return doc?
	 */
	@SuppressWarnings("finally")
//	public Document search() {
//		String result = "";
//		Document doc = null;
//		boolean renew = false;//?
//		String tempUrl2;
//		// System.out.println("done = " + done);
//		// ����� Ű����
//		try {
//			// ������ ������ ���� ���� Ȯ��
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
//				// total ����
//			} else {
//				// ������ �������� �ƴ� ���, ������ ���� �� �˻�
//				page++;
//			}
//			if (renew)
//				tempUrl2 = tempUrl;
//			else
//				tempUrl2 = tempUrl + "&p=" + page; // ������
//			// Http ��û
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
//		System.out.println("������ : " + curSdate + " ������: " + curEdate);
//		System.out.println("������ ���� : " + sd.format(curSdate) + " ������ ���� : " + sd.format(curEdate));
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
//		// html ���� �̻��
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
 * - ����/����¥�� �ָ�, �� �Ⱓ�� ��縦 ��� ũ���Ͽ� DB�� ������� ����
 * - �� ��л��� Ŭ�������� �������̵� ����
 * - ��л纰 html�м� ������, �����ǴºκФ�
 * 
 * @param doc = dom��ü�� param �޾� �Ľ�
 * @return success����
 * 
 * 
 */
	private Boolean parseArticle(Document doc) {
		boolean success = false;
		Article art = null;
		Elements divs = doc.select("div.cont_inner");
		// System.out.println("size of div = " + divs.size());
		/*
		 * Article Parsing �ϴ� ��
		 * HTML���� ���� Elem�� ������
		 * art��ü�� ����� --> ���⼭ DB�� �����ϴ� ���� �ʿ�
		 */

		//����API
		for (Element elem : divs) {
			art = new Article();
			String candidate = keyword;
			art.setTitle(elem.select("div.wrap_tit a").get(0).text()); 			// ���� ����
			art.setDescription(elem.select("p.f_eb").get(0).text()); 			// ���� ����
			art.setDate(elem.select("span.f_nb").get(0).text().substring(0, 10)); 			// ��¥ ����
			art.setUrl(elem.select("div.wrap_tit a").get(0).attr("abs:href")); 			// ��ũ ����
			art.setPublisher(elem.select("span.f_nb").get(0).text().substring(10).replaceAll("[|]", "").replaceAll("\uB2E4\uC74C\uB274\uC2A4", "").trim()); // ��л� ����
			list.add(art); //list�� �߰�
			db.runSQL(candidate, art); //article�� ��°�� �Ѱ� DB ����

//			System.out.println("����\t"+list.get(list.size()-1).getTitle());
//			System.out.println("��¥\t"+list.get(list.size()-1).getDate());
//			System.out.println("��ũ\t"+list.get(list.size()-1).getUrl());
//			System.out.println("����\t"+list.get(list.size()-1).getDescription());
//			System.out.println("��л�\t"+list.get(list.size()-1).getPublisher());
//			System.out.println();
			
		}

		return success;
	}
}

// getTotal ���� �޼ҵ忡 �α�� ��� �߰� [getTotal()] : ~
// finally�� return ���� �����