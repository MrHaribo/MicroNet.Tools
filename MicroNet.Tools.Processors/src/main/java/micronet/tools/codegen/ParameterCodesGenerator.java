package micronet.tools.codegen;

import static micronet.tools.codegen.CodegenConstants.PARAMETER_CODE;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import micronet.tools.filesync.SyncParameterCodes;

public class ParameterCodesGenerator {
	private Filer filer;

	public ParameterCodesGenerator(Filer filer) {
		this.filer = filer;
	}

	public void generateParameterCodeEnum(String packageName, String sharedDir) {

		TypeSpec.Builder builder = TypeSpec.classBuilder(PARAMETER_CODE).addModifiers(Modifier.PUBLIC);
		Set<String> params = SyncParameterCodes.getAllParameterCodes(sharedDir);
		
		for (String entry : params) {
			builder.addField(FieldSpec.builder(String.class, entry)
			    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
			    .initializer("$S", entry)
			    .build());
		}
		
		TypeSpec typeSpec = builder.build();
		JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

		try {
			JavaFileObject file = filer.createSourceFile(packageName + "." + PARAMETER_CODE);
			Writer writer = file.openWriter();

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
