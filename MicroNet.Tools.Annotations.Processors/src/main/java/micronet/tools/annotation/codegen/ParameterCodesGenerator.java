package micronet.tools.annotation.codegen;

import static micronet.tools.annotation.codegen.CodegenConstants.PARAMETER_CODE;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import micronet.serialization.Serialization;

public class ParameterCodesGenerator {
	private Filer filer;

	public ParameterCodesGenerator(Filer filer) {
		this.filer = filer;
	}

	public void generateParameterCodeEnum(String packageName, String sharedDir) {

		try {
			Set<String> params = readParameters(sharedDir);
			String[] entries = (String[]) params.toArray(new String[params.size()]);

			TypeSpec.Builder builder = TypeSpec.enumBuilder(PARAMETER_CODE).addModifiers(Modifier.PUBLIC);
			for (String entry : entries) {
			    builder.addEnumConstant(entry);
			}
			TypeSpec typeSpec = builder.build();
			JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
			
			JavaFileObject file = filer.createSourceFile(packageName + "." + PARAMETER_CODE);
			Writer writer = file.openWriter();
			
			javaFile.writeTo(writer);
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Set<String> readParameters(String sharedDir) {
		
		File parameterCodeFile = new File(sharedDir + CodegenConstants.PARAMETER_CODE);
		try (RandomAccessFile file = new RandomAccessFile(parameterCodeFile, "rw")) {
			file.getChannel().lock();
			try {
				String data = readFileChannel(file.getChannel());
				String[] codeArray = Serialization.deserialize(data, String[].class);

				return new TreeSet<String>(Arrays.asList(codeArray));
			} catch (Exception e) {
				System.out.println("Parse parameterCode File Error: " + e.getMessage());
			}
		} catch (IOException e) {
			System.out.println("I/O Error: " + e.getMessage());
		}
		return new TreeSet<String>();
	}
	
	private static String readFileChannel(FileChannel channel) throws IOException {
		StringBuilder dataString = new StringBuilder();
		ByteBuffer buffer = ByteBuffer.allocate(20);
		int noOfBytesRead = channel.read(buffer);

		while (noOfBytesRead != -1) {
			buffer.flip();
			while (buffer.hasRemaining()) {
				dataString.append((char) buffer.get());
			}
			buffer.clear();
			noOfBytesRead = channel.read(buffer);
		}
		return dataString.toString();
	}
}
