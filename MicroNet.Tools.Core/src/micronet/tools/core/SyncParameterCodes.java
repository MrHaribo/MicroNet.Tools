package micronet.tools.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import micronet.serialization.Serialization;
import micronet.tools.annotation.codegen.CodegenConstants;

public class SyncParameterCodes {
	public static void contributeParameters(ServiceProject serviceProject) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String sharedDir = workspaceRoot.getLocation().toOSString() + "/shared/";
		
		File parameterCodeFile = new File(sharedDir + CodegenConstants.PARAMETER_CODE);
		try (RandomAccessFile file = new RandomAccessFile(parameterCodeFile, "rw")) {
			file.getChannel().lock();
			try {
				String data = readFileChannel(file.getChannel());
				String[] codeArray = Serialization.deserialize(data, String[].class);

				Set<String> existingParameterCodes = new TreeSet<String>(Arrays.asList(codeArray));
				Set<String> projectParameterCodes = serviceProject.getRequiredParameters();

				existingParameterCodes.addAll(projectParameterCodes);
				codeArray = existingParameterCodes.toArray(new String[existingParameterCodes.size()]);
				data = Serialization.serializePretty(codeArray);

				file.setLength(data.length());
				writeFileChannel(file.getChannel(), data);
			} catch (Exception e) {
				System.out.println("Parse parameterCode File Error: " + e.getMessage());
			}
		} catch (IOException e) {
			System.out.println("I/O Error: " + e.getMessage());
		}
	}
	
	public static Set<String> readParameters() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String sharedDir = workspaceRoot.getLocation().toOSString() + "/shared/";
		
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
	
	private static void writeFileChannel(FileChannel channel, String data) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
		channel.write(buffer, 0);
		channel.close();
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
