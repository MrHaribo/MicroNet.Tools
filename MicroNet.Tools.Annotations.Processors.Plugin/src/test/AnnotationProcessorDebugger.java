package test;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import micronet.tools.annotation.ServiceAnnotationProcessor;

public class AnnotationProcessorDebugger {
	public static void main(String[] args) throws Exception {
		runAnnoationProcessor();
	}
	
	public static void runAnnoationProcessor() throws Exception {
		String source = "D:\\Workspace\\runtime-EclipseApplication\\WorldService";
		
		System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.8.0_73\\jre");

		Iterable<JavaFileObject> files = getSourceFiles(source);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		CompilationTask task = compiler.getTask(new PrintWriter(System.out), null, null, null, null, files);
		task.setProcessors(Arrays.asList(new ServiceAnnotationProcessor()));

		task.call();
	}

	private static Iterable<JavaFileObject> getSourceFiles(String p_path) throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);

		files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(p_path)));

		Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);
		return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
	}
}
