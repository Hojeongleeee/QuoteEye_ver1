import org.jsoup.nodes.Document;

public class Launch {
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		
//		Document doc;

		SearchManager sm = new SearchManager("20170118", "20170119", 10);
//		doc = sm.search("\"�ݱ⹮\"");
//		while(doc != null) {
//			doc = sm.search();
//		}

		long end = System.currentTimeMillis();
		System.out.println("����ð� : " + (end - start)/1000.0);
		
	}
}