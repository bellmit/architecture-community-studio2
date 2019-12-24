package architecture.community.web.spring.controller.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import architecture.community.model.Property;

public class Utils {

	public static List<Property> toList(Map<String, String> properties) {
		
		List<Property> list = new ArrayList<Property>();
		for (String key : properties.keySet()) {
		    String value = properties.get(key);
		    list.add(new Property(key, value));
		} 
		return list;
	} 
	
	/**
	 * extract data-image-link attr  value as List in img tag.
	 * @param content
	 * @return
	 */
	public static List<String> getImageLinksFromHtml(String content){ 
		Document doc = Jsoup.parse(content);
		Elements elements = doc.select("img");
		List<String> list = new ArrayList<String>();
		for (Element element : elements ) {
			Map<String, String> data = element.dataset();
			if( element.hasAttr("data-image-link") ) {					
				list.add(element.attr("data-image-link"));
			} 
		}
		return list;
	}

}
