package micronet.tools.annotation.codegen;

import static micronet.tools.annotation.codegen.CodegenConstants.SERVICE_IMPL_CLASSNAME;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import micronet.annotation.MessageListener;
import micronet.network.Context;
import micronet.network.IPeer;
import micronet.network.Request;
import micronet.network.factory.PeerFactory;
import micronet.tools.annotation.ServiceDescription;

public class ServiceImplGenerator {

	private Filer filer;
	private Messager messager;

	public ServiceImplGenerator(Filer filer, Messager messager) {
		this.filer = filer;
		this.messager = messager;
	}
	
	public void generateServiceImplementation(ServiceDescription serviceDescription) {
		
		String serviceName = serviceDescription.getService().getSimpleName().toString();
		String serviceUri = serviceDescription.getURI();
		
		String peerVariableName = "peer";
		String contextVariableName = "context";
		String serviceVariableName = "service";
		String requestVariableName = "request";
		
		TypeName serviceTypeName = ClassName.get(serviceDescription.getPackage(), serviceDescription.getName());
		
		CodeBlock.Builder startCodeBuilder = CodeBlock.builder()
				.addStatement("$T.out.println($S)", System.class, serviceName + " started...");
		for (Element method : serviceDescription.getStartMethods()) {
			startCodeBuilder.addStatement("$N.$N($N)", serviceVariableName, method.getSimpleName(), contextVariableName);
		}
		
		CodeBlock.Builder stopCodeBuilder = CodeBlock.builder()
				.addStatement("$T.out.println($S)", System.class, serviceName + " stopped...");
		for (Element method : serviceDescription.getStopMethods()) {
			stopCodeBuilder.addStatement("$N.$N($N)", serviceVariableName, method.getSimpleName(), contextVariableName);
		}
		
		
		CodeBlock.Builder listerCodeBuilder = CodeBlock.builder()
				.addStatement("$T.out.println($S)", System.class, "Registering message listeners...");
		for (Element method : serviceDescription.getMessageListeners()) {
			MessageListener annotation = method.getAnnotation(MessageListener.class);
			
			//REF: peer.listen("/register", (Request request) -> service.onRegister(context, request));
			listerCodeBuilder.addStatement("$N.listen($S, ($T $N) -> $N.$N($N, $N))", 
					peerVariableName, annotation.uri(), Request.class, requestVariableName, serviceVariableName, method.getSimpleName(), contextVariableName, requestVariableName);
		}
		
		CodeBlock startCode = startCodeBuilder.build();
		CodeBlock stopCode = stopCodeBuilder.build();
		CodeBlock listenerCode = listerCodeBuilder.build();
		
		TypeSpec stopThread = TypeSpec.anonymousClassBuilder("")
			    .addSuperinterface(Thread.class)
			    .addMethod(MethodSpec.methodBuilder("run")
			        .addAnnotation(Override.class)
			        .addModifiers(Modifier.PUBLIC)
			        .addCode(stopCode)
			        .returns(void.class)
			        .build())
			    .build();
		
		CodeBlock tryBlock = CodeBlock.builder()
				.beginControlFlow("try")
				.addStatement("$T.out.println($S)", System.class,  "Starting " + serviceName + "...")
				.add("\n")
				
				.addStatement("$T $N = $T.createPeer()", IPeer.class, peerVariableName, PeerFactory.class)
				.addStatement("$T $N = new $T($N, $S)", Context.class, contextVariableName, Context.class, peerVariableName, serviceUri)
				.addStatement("$T $N = new $T()", serviceTypeName, serviceVariableName, serviceTypeName)
				.add("\n")
				
				.add(listenerCode)
				.add("\n")
				
				.add(startCode)
				.add("\n")
				
				.addStatement("$T.getRuntime().addShutdownHook($L)", Runtime.class, stopThread)
				
				.endControlFlow()
				
				.beginControlFlow("catch ($T e)", Exception.class)
				.addStatement("$T.err.println($S)", System.class, "Could not start " + serviceName + "...")
				.addStatement("e.printStackTrace()")
				.endControlFlow()
				.build();
		
		MethodSpec main = MethodSpec.methodBuilder("main")
			    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
			    .returns(void.class)
			    .addParameter(String[].class, "args")
			    .addCode(tryBlock)
			    .build();

		TypeSpec serviceImplType = TypeSpec.classBuilder(SERVICE_IMPL_CLASSNAME)
		    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
		    .addMethod(main)
		    .build();

		JavaFile javaFile = JavaFile.builder("", serviceImplType).build();
		System.out.println(javaFile);
		try {
			JavaFileObject file = filer.createSourceFile(SERVICE_IMPL_CLASSNAME, serviceDescription.getService());
			Writer writer = file.openWriter();
			javaFile.writeTo(writer);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
