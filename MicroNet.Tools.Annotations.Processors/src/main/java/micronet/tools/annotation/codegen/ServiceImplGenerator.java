package micronet.tools.annotation.codegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import micronet.annotation.MessageListener;
import micronet.tools.annotation.ServiceAnnotationProcessorContext;
import micronet.tools.annotation.ServiceDescription;

public class ServiceImplGenerator {

	private Filer filer;
	private Messager messager;

	public ServiceImplGenerator(Filer filer, Messager messager) {
		this.filer = filer;
		this.messager = messager;
	}

	public void generateServiceImplementation(ServiceDescription description) {
		log(description.getService(), "Generating Service implementation: " + description.getService().getSimpleName());

		String additionalImports = "import " + description.getService().toString() + ";\n\n";
		String serviceClassName = "ServiceImpl";

		String startCode = generateStartCode(description);
		String stopCode = generateStopCode(description);
		String listenerCode = generateListenerCode(description);

		try {
			String dir = "/";
			System.out.println("Checking Dir: " + dir);
			Enumeration<URL> en = ServiceAnnotationProcessorContext.class.getClassLoader().getResources(dir);
			while (en.hasMoreElements()) {
				// prints only file1 from engine.jar
				// (actually it's in classes directory because I run it from my
				// IDE)
				System.out.println("Ress:" + en.nextElement());
			}
		} catch (IOException e1) {
			System.out.println("");
			e1.printStackTrace();
		}

		System.out.println("Res: " + this.getClass().getResource("/ServiceTemplate.txt").toString());
		InputStream resourceAsStream = this.getClass().getResourceAsStream("/ServiceTemplate.txt");

		BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));

		try {

			JavaFileObject file = filer.createSourceFile(serviceClassName, description.getService());
			Writer writer = file.openWriter();

			String line = reader.readLine();
			while (line != null) {

				line = line.replaceAll(Pattern.quote("${additional_imports}"), additionalImports);
				line = line.replaceAll(Pattern.quote("${service_class}"), serviceClassName);
				line = line.replaceAll(Pattern.quote("${service_name}"), description.getName());
				line = line.replaceAll(Pattern.quote("${service_uri}"), description.getURI());
				line = line.replaceAll(Pattern.quote("${service_variable}"), description.getServiceVariable());
				line = line.replaceAll(Pattern.quote("${peer_variable}"), description.getPeerVariable());
				line = line.replaceAll(Pattern.quote("${on_start}"), startCode);
				line = line.replaceAll(Pattern.quote("${on_stop}"), stopCode);
				line = line.replaceAll(Pattern.quote("${register_listeners}"), listenerCode);

				writer.append(line + "\n");
				line = reader.readLine();
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String generateListenerCode(ServiceDescription description) {

		StringBuilder code = new StringBuilder();

		for (Element method : description.getMessageListeners()) {
			MessageListener annotation = method.getAnnotation(MessageListener.class);
			code.append(description.getPeerVariable() + ".listen(\"" + annotation.uri() + "\", (Request request) -> ");
			code.append(description.getServiceVariable() + "." + method.getSimpleName() + "(context, request));\n");
		}

		return code.toString();
	}

	private String generateStopCode(ServiceDescription description) {
		StringBuilder code = new StringBuilder();
		code.append("System.out.println(\"" + description.getName() + " stopped...\");\n");

		for (Element method : description.getStopMethods()) {
			code.append(description.getServiceVariable() + "." + method.getSimpleName() + "(context);\n");
		}

		return code.toString();
	}

	private String generateStartCode(ServiceDescription description) {

		StringBuilder code = new StringBuilder();
		code.append("System.out.println(\"" + description.getName() + " started...\");\n");

		for (Element method : description.getStartMethods()) {
			code.append(description.getServiceVariable() + "." + method.getSimpleName() + "(context);\n");
		}

		return code.toString();
	}

	private void log(Element e, String msg) {
		messager.printMessage(Kind.NOTE, msg);
	}

}
