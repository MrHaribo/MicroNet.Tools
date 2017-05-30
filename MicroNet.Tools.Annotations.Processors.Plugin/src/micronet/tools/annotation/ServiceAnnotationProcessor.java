package micronet.tools.annotation;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import micronet.annotation.MessageListener;
import micronet.annotation.MessageService;
import micronet.annotation.OnStart;
import micronet.annotation.OnStop;
import micronet.tools.annotation.codegen.ParameterCodesGenerator;
import micronet.tools.annotation.codegen.ServiceAPIGenerator;
import micronet.tools.annotation.codegen.ServiceImplGenerator;

public class ServiceAnnotationProcessor extends AbstractProcessor {

	private static final String PARAMETER_CODE_CLASSNAME = "ParameterCode";
	private static final String MESSAGE_PARAMETER_CLASSNAME = "MessageParameter";
	private static final String REQUEST_PARAMETERS_CLASSNAME = "RequestParameters";
	private static final String RESPONSE_PARAMETERS_CLASSNAME = "ResponseParameters";

	ServiceAnnotationProcessorContext context;

	private Filer filer;
	private Types typeUtils;
	private Elements elementUtils;
	private Messager messager;

	private String workspacePath;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		filer = processingEnv.getFiler();
		typeUtils = processingEnv.getTypeUtils();
		elementUtils = processingEnv.getElementUtils();
		messager = processingEnv.getMessager();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		workspacePath = workspaceRoot.getLocation().toOSString();

		context = new ServiceAnnotationProcessorContext(processingEnv, workspacePath);

	}

	// @Retention(RetentionPolicy.CLASS)
	// @Target(ElementType.METHOD)
	// public @interface MessageParameter {
	// public String value();
	// }

	public void generateMessageParameterAnnotation(String packageName, String workspacePath) {
		try {

			TypeName paramClassType = ClassName.get(packageName, PARAMETER_CODE_CLASSNAME);

			TypeSpec typeSpec = TypeSpec.annotationBuilder(MESSAGE_PARAMETER_CLASSNAME)
					.addAnnotation(AnnotationSpec.builder(Retention.class)
							.addMember("value", "$T.$L", RetentionPolicy.class, RetentionPolicy.CLASS.name()).build())

					.addAnnotation(AnnotationSpec.builder(Target.class)
							.addMember("value", "$T.$L", ElementType.class, ElementType.TYPE.name()).build())

					.addMethod(MethodSpec.methodBuilder("value").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
							.returns(paramClassType).build())

					.build();

			JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

			JavaFileObject file = filer.createSourceFile(packageName + "." + MESSAGE_PARAMETER_CLASSNAME);
			Writer writer = file.openWriter();

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void generateParametersAnnotation(String parametersName, String packageName, String workspacePath) {
		try {

			TypeSpec typeSpec = TypeSpec.annotationBuilder(parametersName)
					.addAnnotation(AnnotationSpec.builder(Retention.class)
							.addMember("value", "$T.$L", RetentionPolicy.class, RetentionPolicy.CLASS.name()).build())

					.addAnnotation(AnnotationSpec.builder(Target.class)
							.addMember("value", "$T.$L", ElementType.class, ElementType.METHOD.name()).build())

					.addMethod(MethodSpec.methodBuilder("value").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
							.returns(ArrayTypeName.of(ClassName.get(packageName, "MessageParameter")))
							.defaultValue("$L", "{}").build())

					.build();

			JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

			JavaFileObject file = filer.createSourceFile(packageName + "." + parametersName);
			Writer writer = file.openWriter();

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		try {

			ServiceDescription description = null;
			for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(MessageService.class)) {

				// Check if a class has been annotated with @Factory
				if (annotatedElement.getKind() != ElementKind.CLASS) {
					System.out.println("Only classes can be annotated withMessageService");
					return true; // Exit processing
				}

				description = new ServiceDescription();

				description.setMessageListeners(roundEnv.getElementsAnnotatedWith(MessageListener.class));
				description.setStartMethods(roundEnv.getElementsAnnotatedWith(OnStart.class));
				description.setStopMethods(roundEnv.getElementsAnnotatedWith(OnStop.class));

				description.setService(annotatedElement);

				ParameterCodesGenerator parameterCodesGenerator = new ParameterCodesGenerator(filer);
				parameterCodesGenerator.generateParameterCodeEnum(description, workspacePath);

				generateMessageParameterAnnotation(description.getPackage(), workspacePath);
				generateParametersAnnotation(REQUEST_PARAMETERS_CLASSNAME, description.getPackage(), workspacePath);
				generateParametersAnnotation(RESPONSE_PARAMETERS_CLASSNAME, description.getPackage(), workspacePath);

				List<String> requestParameters = readParameterList(REQUEST_PARAMETERS_CLASSNAME, roundEnv, description);
				List<String> responseParameters = readParameterList(RESPONSE_PARAMETERS_CLASSNAME, roundEnv, description);

				ServiceImplGenerator implGenerator = new ServiceImplGenerator(filer, messager);
				implGenerator.generateServiceImplementation(description);

				ServiceAPIGenerator apiGenerator = new ServiceAPIGenerator(typeUtils);
				apiGenerator.generateAPIDescription(description, workspacePath);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private List<String> readParameterList(String parameterType, RoundEnvironment roundEnv, ServiceDescription description) {
		
		List<String> parameterList = new ArrayList<>();
		
		TypeElement parameterTypeElement = elementUtils.getTypeElement(description.getPackage() + "." + parameterType);
		Set<? extends Element> elementsWithMessageParameters = roundEnv.getElementsAnnotatedWith(parameterTypeElement);

		for (Element elem : elementsWithMessageParameters) {
			List<? extends AnnotationMirror> allElementAnnotationMirrors = elem.getAnnotationMirrors();

			for (AnnotationMirror annotationMirror : allElementAnnotationMirrors) {

				if (!annotationMirror.getAnnotationType().toString().contains(parameterType))
					continue;

				AnnotationValue value = getFieldFromAnnotationMirror(annotationMirror, "value");
				List<AnnotationValue> paramList = (List<AnnotationValue>) value.getValue();

				for (AnnotationValue param : paramList) {

					AnnotationMirror paramMiror = (AnnotationMirror) param.getValue();
					AnnotationValue paramValue = getFieldFromAnnotationMirror(paramMiror, "value");

					System.out.println(paramValue.getValue());
					parameterList.add(paramValue.getValue().toString());
				}
			}
		}
		return parameterList;
	}

	private AnnotationValue getFieldFromAnnotationMirror(AnnotationMirror mirror, String fieldName) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
			if (entry.getKey().getSimpleName().toString().equals(fieldName)) {
				return entry.getValue();
			}
		}
		return null;
	}
}
