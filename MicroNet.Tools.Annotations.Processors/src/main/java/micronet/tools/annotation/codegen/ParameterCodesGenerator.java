package micronet.tools.annotation.codegen;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import micronet.serialization.Serialization;
import micronet.tools.annotation.ServiceDescription;

public class ParameterCodesGenerator {
	private Filer filer;

	public ParameterCodesGenerator(Filer filer) {
		this.filer = filer;
	}

	public void generateParameterCodeEnum(ServiceDescription description, String workspacePath) {

		try {
			Scanner scanner = new Scanner(new File(workspacePath + "/shared/ParameterCodes"));
			String text = scanner.useDelimiter("\\A").next();
			scanner.close(); // Put this call in a finally block
			String[] entries = Serialization.deserialize(text, String[].class);
			
			
			TypeSpec.Builder builder = TypeSpec.enumBuilder("ParameterCode").addModifiers(Modifier.PUBLIC);
			for (String entry : entries) {
			    builder.addEnumConstant(entry);
			}
			TypeSpec typeSpec = builder.build();
			JavaFile javaFile = JavaFile.builder(description.getPackage(), typeSpec).build();
			
			JavaFileObject file = filer.createSourceFile(description.getPackage() + ".ParameterCode", description.getService());
			Writer writer = file.openWriter();
			
			javaFile.writeTo(writer);
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
