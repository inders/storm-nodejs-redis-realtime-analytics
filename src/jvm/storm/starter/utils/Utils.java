package storm.starter.utils;

import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;

public class Utils {

	public static final void StringToList(String message, List<String> list) {
		if(message == null) {
			return;
		}
		
		synchronized (list) {
			list.clear();
			String[] domains = message.split(",");
			if(domains != null) {
				list.addAll(Arrays.asList(domains));
			}
		}
	}
	
	public static String html2text(String html) {
    return Jsoup.parse(html).text();
	}
	
	public static void main(String[] args) {
	   System.out.println(html2text("crap <title> hurrah </title>"));
	}
}
