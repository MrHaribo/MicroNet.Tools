package micronet.tools.annotation;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.DocumentationTool.Location;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import micronet.annotation.MessageListener;
import micronet.tools.api.ServiceAPI;
import micronet.tools.contribution.ModelContribution;
import micronet.tools.contribution.ParameterCodeParser;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.filesync.SyncServiceAPI;

public class ServiceAnnotationProcessor extends AbstractProcessor {

	private Elements elementUtils;
	private Types typeUtils;
	private ServiceAnnotationProcessorContext context;
	private String sharedDir;
	private ServiceProject serviceProject;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		elementUtils = processingEnv.getElementUtils();
		typeUtils = processingEnv.getTypeUtils();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		sharedDir = workspaceRoot.getLocation().toOSString() + "/shared/";

		String projectName = findProjectName(processingEnv.getFiler());
		serviceProject = ModelProvider.INSTANCE.getServiceProject(projectName);

		String packageName = null;
		if (serviceProject != null) {
			String contributedSharedDir = serviceProject.getContributedSharedDir();
			if (serviceProject.isSharedDirContributionEnabled() && contributedSharedDir != null) {
				ModelContribution.contributeSharedDir(contributedSharedDir, sharedDir);
				ModelProvider.INSTANCE.notifyModelChangedListeners();
				serviceProject.setSharedDirContributionEnabled(false);
			}

			packageName = serviceProject.getPackageName();
		}

		if (packageName == null)
			return;

		context = new ServiceAnnotationProcessorContext(processingEnv, packageName, sharedDir);

		String generateModelArg = processingEnv.getOptions().get("generate_model");
		if (generateModelArg != null && generateModelArg.equals("true")) {
			System.out.println("generate_model=true -> Generate global code");
			context.generateGlobalCode();
		}
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		if (context == null)
			return null;
		return context.getSupportedAnnotationTypes();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		if (context == null)
			return null;
		return context.getSupportedSourceVersion();
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (context == null)
			return false;
		
		context.process(roundEnv);
		


		if (context.getServiceDescription() != null) {
			
			ServiceAPI api = SyncServiceAPI.generateAPIDescription(context.getServiceDescription(), elementUtils, sharedDir);
			
			try {
				String fileName = context.getServiceDescription().getName() + ".java";
				String srcPath = "src/main/java";
				String packagePath = context.getServiceDescription().getPackage().replace(".", "/");
				File codeFile = new File(serviceProject.getProject().getLocation().append(srcPath).append(packagePath).append(fileName).toString());
				String data = new String(Files.readAllBytes(codeFile.toPath()));
				
				if (context.getServiceDescription().getName().equals("PlayerService")) {
					ParameterCodeParser parser = new ParameterCodeParser(context.getServiceDescription(), data);
					api = parser.parseParameterCodes(api);
					SyncServiceAPI.saveServiceAPI(context.getServiceDescription(), api, sharedDir);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		return true;
	}
	
	

	private String findProjectName(Filer filer) {
		try {
			FileObject resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "");
			File file = new File(resource.toUri().getPath().replaceAll(resource.getName(), ""));
			return file.getName();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
