package architecture.community.web.spring.controller.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
}
