package architecture.community.model.json;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import architecture.ee.exception.RuntimeError;

public class JsonDateDeserializer extends JsonDeserializer<Date> {
 
    private static final ISO8601DateFormat formatter = new ISO8601DateFormat();

    @Override
    public Date deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext)
	    throws IOException, JsonProcessingException {

	String date = jsonparser.getText();

	try {
	    return formatter.parse(date);
	} catch (ParseException e) {
	    throw new RuntimeError(e);
	}

    }

}
