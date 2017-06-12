package micronet.tools.codegen;

import static micronet.tools.codegen.CodegenConstants.*;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import micronet.tools.annotation.ServiceDescription;

public class AnnotationGenerator {

	private Filer filer;

	public AnnotationGenerator(Filer filer) {
		this.filer = filer;
	}

	// @Retention(RetentionPolicy.CLASS)
	// @Target(ElementType.METHOD)
	// public @interface MessageParameter {
	// public String value();
	// }

	public void generateMessageParameterAnnotation(String packageName) {
		try {

			TypeName paramClassType = ClassName.get(packageName, PARAMETER_CODE);

			TypeSpec typeSpec = TypeSpec.annotationBuilder(MESSAGE_PARAMETER)
					.addAnnotation(AnnotationSpec.builder(Retention.class)
							.addMember("value", "$T.$L", RetentionPolicy.class, RetentionPolicy.CLASS.name()).build())

					.addAnnotation(AnnotationSpec.builder(Target.class)
							.addMember("value", "$T.$L", ElementType.class, ElementType.TYPE.name()).build())

					.addMethod(MethodSpec.methodBuilder("value").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
							.returns(paramClassType).build())

					.build();

			JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

			JavaFileObject file = filer.createSourceFile(packageName + "." + MESSAGE_PARAMETER);
			Writer writer = file.openWriter();

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void generateParametersAnnotations(String packageName) {
		generateParametersAnnotation(REQUEST_PARAMETERS, packageName);
		generateParametersAnnotation(RESPONSE_PARAMETERS, packageName);
	}

	public void generateParametersAnnotation(String parametersName, String packageName) {
		try {

			TypeSpec typeSpec = TypeSpec.annotationBuilder(parametersName)
					.addAnnotation(AnnotationSpec.builder(Retention.class)
							.addMember("value", "$T.$L", RetentionPolicy.class, RetentionPolicy.CLASS.name()).build())

					.addAnnotation(AnnotationSpec.builder(Target.class)
							.addMember("value", "$T.$L", ElementType.class, ElementType.METHOD.name()).build())

					.addMethod(MethodSpec.methodBuilder("value").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
							.returns(ArrayTypeName.of(ClassName.get(packageName, MESSAGE_PARAMETER)))
							.defaultValue("$L", "{}").build())

					.build();

			JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
			JavaFileObject file = filer.createSourceFile(packageName + "." + parametersName);
			Writer writer = file.openWriter();

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
