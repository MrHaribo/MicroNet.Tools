package micronet.tools.filesync;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import micronet.network.NetworkConstants;
import micronet.serialization.Serialization;
import micronet.tools.codegen.CodegenConstants;

public class SyncParameterCodes {
	
	
	private static Semaphore semaphore = new Semaphore(1);

	public static void contributeParameters(Set<String> contributedCodes, String sharedDir) {
		File parameterCodeFile = getParameterCodeFile(sharedDir);
		
	    try {
	        semaphore.acquire();

	        try (Scanner scanner = new Scanner(parameterCodeFile)) {
	        	String data = new String(Files.readAllBytes(parameterCodeFile.toPath()), StandardCharsets.UTF_8);
				String[] codeArray = Serialization.deserialize(data, String[].class);
				
				Set<String> existingParameterCodes = new TreeSet<String>(Arrays.asList(codeArray));

				existingParameterCodes.addAll(contributedCodes);
				codeArray = existingParameterCodes.toArray(new String[existingParameterCodes.size()]);
				data = Serialization.serializePretty(codeArray);
				
				try (PrintWriter printer = new PrintWriter(parameterCodeFile)) {
					printer.print(data);
				}
	        } catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
	        semaphore.release();
	    }
	}
	
	public static void removeParameters(Set<String> removedCodes, String sharedDir) {
		File parameterCodeFile = getParameterCodeFile(sharedDir);
		
	    try {
	        semaphore.acquire();

	        try (Scanner scanner = new Scanner(parameterCodeFile)) {
	        	String data = new String(Files.readAllBytes(parameterCodeFile.toPath()), StandardCharsets.UTF_8);
				String[] codeArray = Serialization.deserialize(data, String[].class);
				
				Set<String> existingParameterCodes = new TreeSet<String>(Arrays.asList(codeArray));

				existingParameterCodes.removeAll(removedCodes);
				codeArray = existingParameterCodes.toArray(new String[existingParameterCodes.size()]);
				data = Serialization.serializePretty(codeArray);
				
				try (PrintWriter printer = new PrintWriter(parameterCodeFile)) {
					printer.print(data);
				}
	        } catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
	        semaphore.release();
	    }
	}
	
	public static Set<String> readParameters(String sharedDir) {
		File parameterCodeFile = getParameterCodeFile(sharedDir);
		
	    try {
	        semaphore.acquire();

	        try {
	        	String data = new String(Files.readAllBytes(parameterCodeFile.toPath()), StandardCharsets.UTF_8);
				String[] codeArray = Serialization.deserialize(data, String[].class);
				
				if (codeArray == null)
					return new TreeSet<>();
				
				return new TreeSet<String>(Arrays.asList(codeArray));
	        } catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
	        semaphore.release();
	    }
	    return new TreeSet<>();
	}
	
	private static File getParameterCodeFile(String sharedDir) {
		try {
			File parameterCodeFile = new File(sharedDir + CodegenConstants.PARAMETER_CODE);
			if (!parameterCodeFile.exists()) {
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(parameterCodeFile), "utf-8"))) {
					writer.write("[\"DUMMY_CODE\"]");
				}
			}
			return parameterCodeFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
