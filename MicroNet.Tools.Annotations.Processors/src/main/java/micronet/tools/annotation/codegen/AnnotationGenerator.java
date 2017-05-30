package micronet.tools.annotation.codegen;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import micronet.tools.annotation.ServiceDescription;

public class AnnotationGenerator {

	private Filer filer;

	public AnnotationGenerator(Filer filer) {
		this.filer = filer;
	}

	public void generate(ServiceDescription description, String workspacePath) {
		try {
			TypeSpec typeSpec = TypeSpec
				  .annotationBuilder("SomeClass")
				  .addAnnotation(RetentionPolicy.class)
				  .addAnnotation(ElementType.class)
				  .build();

			JavaFile javaFile = JavaFile.builder(description.getPackage(), typeSpec).build();

			JavaFileObject file = filer.createSourceFile(description.getPackage() + ".ParameterCode",
					description.getService());
			Writer writer = file.openWriter();

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
