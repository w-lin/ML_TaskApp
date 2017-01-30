package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import entity.Task;
import java.util.logging.Level;
import java.util.logging.Logger;



public class JsonSchemaUtil {

    public static final String JSON_V4_SCHEMA_IDENTIFIER = "http://json-schema.org/draft-04/schema#";
    public static final String JSON_SCHEMA_IDENTIFIER_ELEMENT = "$schema";
    private static final Logger LOGGER = Logger.getLogger(JsonSchemaUtil.class.getName());

    public static boolean isValid (Object entity, String schemaLocation) throws Exception {
        ProcessingReport report = null;
        try {
            JsonNode schemaNode = JsonLoader.fromResource(schemaLocation);
            JsonSchema jsonSchema = getSchemaNode(schemaNode);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.convertValue(entity, JsonNode.class);
            report = jsonSchema.validate(node);
        }
        catch (Exception internal)
        {
            LOGGER.log(Level.SEVERE, internal.getMessage());
            throw new Exception("Error in processing json schema validation");
        }
        if(report!=null) {
            if (report.isSuccess())
                return true;
            throw new Exception(report.toString());
        }
        throw new Exception("Error in processing json schema validation");
    }
    private static JsonSchema getSchemaNode(JsonNode jsonNode) throws ProcessingException
    {
        JsonNode schemaIdentifier = jsonNode.get(JSON_SCHEMA_IDENTIFIER_ELEMENT);
        if (null == schemaIdentifier){
            ((ObjectNode) jsonNode).put(JSON_SCHEMA_IDENTIFIER_ELEMENT, JSON_V4_SCHEMA_IDENTIFIER);
        }
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        return factory.getJsonSchema(jsonNode);
    }

    public static void main(String s []) throws Exception {
        Task ta = new Task();
        ta.setUser("wenghui.lin@gmail.com");
        ta.setPriority(9);
        ta.setDescription("Ak");
        ta.setCompleted("2016-07-06T12:22:46-04:00");
        System.out.println(new JsonSchemaUtil().isValid(ta, "/jsonschema/task.schema.json"));
    }

}
