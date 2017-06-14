package micronet.tools.codegen;

import static micronet.tools.codegen.CodegenConstants.PARAMETER_CODE;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import micronet.tools.filesync.SyncParameterCodes;

public class ParameterCodesGenerator {
	private Filer filer;

	public ParameterCodesGenerator(Filer filer) {
		this.filer = filer;
	}

	public void generateParameterCodeEnum(String packageName, String sharedDir) {

		Set<String> params = SyncParameterCodes.readParameters(sharedDir);
		String[] entries = (String[]) params.toArray(new String[params.size()]);

		TypeSpec.Builder builder = TypeSpec.enumBuilder(PARAMETER_CODE).addModifiers(Modifier.PUBLIC);
		for (String entry : entries) {
			builder.addEnumConstant(entry);
		}
		TypeSpec typeSpec = builder.build();
		JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

		try {
			JavaFileObject file = filer.createSourceFile(packageName + "." + PARAMETER_CODE);
			Writer writer = file.openWriter();

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
