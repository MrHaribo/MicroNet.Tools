package micronet.tools.annotation.codegen;

import static micronet.tools.annotation.codegen.CodegenConstants.*;

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

	public void generateMessageParameterAnnotation(ServiceDescription serviceDescription) {
		try {

			TypeName paramClassType = ClassName.get(serviceDescription.getPackage(), PARAMETER_CODE);

			TypeSpec typeSpec = TypeSpec.annotationBuilder(MESSAGE_PARAMETER)
					.addAnnotation(AnnotationSpec.builder(Retention.class)
							.addMember("value", "$T.$L", RetentionPolicy.class, RetentionPolicy.CLASS.name()).build())

					.addAnnotation(AnnotationSpec.builder(Target.class)
							.addMember("value", "$T.$L", ElementType.class, ElementType.TYPE.name()).build())

					.addMethod(MethodSpec.methodBuilder("value").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
							.returns(paramClassType).build())

					.build();

			JavaFile javaFile = JavaFile.builder(serviceDescription.getPackage(), typeSpec).build();

			JavaFileObject file = filer.createSourceFile(serviceDescription.getPackage() + "." + MESSAGE_PARAMETER);
			Writer writer = file.openWriter();

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void generateParametersAnnotations(ServiceDescription serviceDescription) {
		generateParametersAnnotation(REQUEST_PARAMETERS, serviceDescription);
		generateParametersAnnotation(RESPONSE_PARAMETERS, serviceDescription);
	}

	public void generateParametersAnnotation(String parametersName, ServiceDescription serviceDescription) {
		try {

			TypeSpec typeSpec = TypeSpec.annotationBuilder(parametersName)
					.addAnnotation(AnnotationSpec.builder(Retention.class)
							.addMember("value", "$T.$L", RetentionPolicy.class, RetentionPolicy.CLASS.name()).build())

					.addAnnotation(AnnotationSpec.builder(Target.class)
							.addMember("value", "$T.$L", ElementType.class, ElementType.METHOD.name()).build())

					.addMethod(MethodSpec.methodBuilder("value").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
							.returns(ArrayTypeName.of(ClassName.get(serviceDescription.getPackage(), MESSAGE_PARAMETER)))
							.defaultValue("$L", "{}").build())

					.build();

			JavaFile javaFile = JavaFile.builder(serviceDescription.getPackage(), typeSpec).build();
			JavaFileObject file = filer.createSourceFile(serviceDescription.getPackage() + "." + parametersName);
			Writer writer = file.openWriter();

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
