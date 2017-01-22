import org.jsoup.nodes.Document;

public class Launch {
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		
//		Document doc;

		SearchManager sm = new SearchManager("20170118", "20170119", 10);
//		doc = sm.search("\"반기문\"");
//		while(doc != null) {
//			doc = sm.search();
//		}

		long end = System.currentTimeMillis();
		System.out.println("실행시간 : " + (end - start)/1000.0);
		
	}
}