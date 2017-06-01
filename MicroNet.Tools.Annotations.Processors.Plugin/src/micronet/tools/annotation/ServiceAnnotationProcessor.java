package micronet.tools.annotation;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import micronet.serialization.Serialization;
import micronet.tools.annotation.ServiceAnnotationProcessorContext.ProcessingState;
import micronet.tools.annotation.api.ListenerAPI;
import micronet.tools.annotation.api.ParameterAPI;
import micronet.tools.annotation.api.ServiceAPI;
import micronet.tools.annotation.codegen.CodegenConstants;
import micronet.tools.annotation.codegen.ServiceAPIGenerator;
import micronet.tools.annotation.filesync.SyncParameterCodes;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;

public class ServiceAnnotationProcessor extends AbstractProcessor implements Observer {

	private Elements elementUtils;

	ServiceAnnotationProcessorContext context;
	private String sharedDir;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		elementUtils = processingEnv.getElementUtils();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		sharedDir = workspaceRoot.getLocation().toOSString() + "/shared/";

		context = new ServiceAnnotationProcessorContext(processingEnv, sharedDir);
		context.addObserver(this);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return context.getSupportedAnnotationTypes();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return context.getSupportedSourceVersion();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		return context.process(roundEnv);
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Annotation Processing ended");

		ServiceDescription serviceDescription = ((ServiceAnnotationProcessorContext) o).getServiceDescription();
		IProject project = findProject(serviceDescription);
		ServiceProject serviceProject = ModelProvider.INSTANCE.getServiceProject(project.getName());
		System.out.println("generated: " + serviceProject.getPath().toOSString());

		switch ((ProcessingState) arg) {
		case SERVICE_FOUND:
			Set<String> contributedParameters = serviceProject.getRequiredParameters();
			SyncParameterCodes.contributeParameters(contributedParameters, sharedDir);
			break;
		case PROCESSING_COMPLETE:
			ServiceAPIGenerator apiGenerator = new ServiceAPIGenerator(elementUtils);
			ServiceAPI apiDescription = apiGenerator.generateAPIDescription(serviceDescription, sharedDir);

			Set<String> requiredParameters = getRequiredParameters(apiDescription);
			serviceProject.setRequiredParameters(requiredParameters);
			break;
		}
	}

	private void contributeParameters(ServiceProject serviceProject) {
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

	private void writeFileChannel(FileChannel channel, String data) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
		channel.write(buffer, 0);
		channel.close();
	}

	private String readFileChannel(FileChannel channel) throws IOException {
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

	// private boolean updateParameters(ServiceProject serviceProject) {
	//
	// Scanner scanner = null;
	// FileWriter fileWriter = null;
	//
	//
	//
	// try {
	// File parameterCodeFile = new File(sharedDir +
	// CodegenConstants.PARAMETER_CODE);
	// scanner = new Scanner(parameterCodeFile);
	// String data = scanner.useDelimiter("\\A").next();
	// String[] codeArray = Serialization.deserialize(data, String[].class);
	//
	// Set<String> existingParameterCodes = new
	// HashSet<String>(Arrays.asList(codeArray));
	// Set<String> projectParameterCodes =
	// serviceProject.getRequiredParameters();
	//
	// existingParameterCodes.addAll(projectParameterCodes);
	// codeArray = existingParameterCodes.toArray(new
	// String[existingParameterCodes.size()]);
	// data = Serialization.serialize(codeArray);
	//
	//
	// fileWriter = new FileWriter(parameterCodeFile, false); // true to append
	// fileWriter.write(data);
	// fileWriter.close();
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// scanner.close(); // Put this call in a finally block
	// fileWriter.close();
	// }
	// }

	private Set<String> getRequiredParameters(ServiceAPI apiDescription) {
		Set<String> requiredParameters = new HashSet<String>();
		for (ListenerAPI listener : apiDescription.getListeners()) {
			for (ParameterAPI parameter : listener.getRequestParameters()) {
				requiredParameters.add(parameter.getType());
			}
			for (ParameterAPI parameter : listener.getResponseParameters()) {
				requiredParameters.add(parameter.getType());
			}
		}
		return requiredParameters;
	}

	private IProject findProject(ServiceDescription serviceDescription) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			if (!project.isOpen())
				continue;

			try {
				if (!project.hasNature(JavaCore.NATURE_ID))
					continue;
				IJavaProject javaProject = JavaCore.create(project.getProject());

				if (javaProject.findType(serviceDescription.getTypename()) != null) {
					return project;
				}

			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
