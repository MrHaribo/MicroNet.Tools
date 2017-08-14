package micronet.tools.yaml.utility;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlUtility {

    public static <T> T readYaml(File file, Class<T> clazz) {
        try {
        	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            T fileObject = mapper.readValue(file, clazz);
            //System.out.println(ReflectionToStringBuilder.toString(fileObject, ToStringStyle.MULTI_LINE_STYLE));
            return fileObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
	public static void writeYaml(File file, Object object) {
        try {
        	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.setSerializationInclusion(Include.NON_EMPTY);
            mapper.writeValue(file, object);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
    
}
