package micronet.tools.annotation;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import micronet.serialization.Serialization;

public class CodeGenTest {
	public static void main(String[] args) throws IOException {
		new CodeGenTest().generate();
	}

	public void generate() throws IOException {
		Properties props = new Properties();
		URL url = this.getClass().getClassLoader().getResource("velocity.properties");
		props.load(url.openStream());

		VelocityEngine ve = new VelocityEngine(props);
		ve.init();

		VelocityContext context = new VelocityContext();

		Scanner scanner = new Scanner( new File("D:\\Workspace\\runtime-EclipseApplication\\shared_api\\ParameterCodes") );
		String text = scanner.useDelimiter("\\A").next();
		scanner.close(); // Put this call in a finally block
		
		String[] entries = Serialization.deserialize(text, String[].class);
		
		context.put("name", new String("Velocity"));
		context.put("entries", entries);

		Template template = null;

		try {
			URL resource = this.getClass().getResource("/ParameterCodeTemplate.vm");
			template = ve.getTemplate("ParameterCodeTemplate.vm");
		} catch (ResourceNotFoundException rnfe) {
			// couldn't find the template
		} catch (ParseErrorException pee) {
			// syntax error: problem parsing the template
		} catch (MethodInvocationException mie) {
			// something invoked in the template
			// threw an exception
		} catch (Exception e) {
		}

		StringWriter sw = new StringWriter();

		template.merge(context, sw);

		System.out.println(sw);
	}
}
