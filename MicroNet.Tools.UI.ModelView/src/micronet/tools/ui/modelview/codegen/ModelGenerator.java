package micronet.tools.ui.modelview.codegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityVariableNode;
import micronet.tools.ui.modelview.variables.NumberDescription;
import micronet.tools.ui.modelview.variables.NumberType;

public class ModelGenerator {
	
	private static String packageName = "SomeGame.WorldService";
	private static String outDirName = "D:\\Workspace\\runtime-EclipseApplication\\WorldService\\target\\generated-sources\\annotations\\SomeGame\\WorldService\\";
	
	public static void generateModelEntity(EntityTemplateNode templateNode) {
		try {
			
			List<MethodSpec> methods = new ArrayList<>();
			List<FieldSpec> fields = new ArrayList<>();
			
			
			for (INode node : templateNode.getChildren()) {
				
				MethodSpec setter = null;
				MethodSpec getter = null;
				FieldSpec field = null;
				
				String variableName = node.getName();
				String nameFirstUpper = variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
				
				if (node instanceof EntityVariableNode) {
					EntityVariableNode variableNode = (EntityVariableNode) node;
					
					switch (variableNode.getVariabelDescription().getType()) {
					case BOOLEAN:
						field = FieldSpec.builder(boolean.class, variableName).build();
						setter = generateSetter(boolean.class, variableName);
						getter = generateGetter(boolean.class, variableName);
						break;
					case CHAR:
						field = FieldSpec.builder(boolean.class, variableName).build();
						setter = generateSetter(boolean.class, variableName);
						getter = generateGetter(boolean.class, variableName);
						break;
					case COMPONENT:
						break;
					case ENUM:
						break;
					case LIST:
						break;
					case MAP:
						break;
					case NUMBER:
						NumberDescription numDesc = (NumberDescription) variableNode.getVariabelDescription();
						Type numberType = getNumberType(numDesc.getNumberType());
						field = FieldSpec.builder(numberType, variableName).build();
						setter = generateSetter(numberType, variableName);
						getter = generateGetter(numberType, variableName);
						break;
					case REF:
						break;
					case SET:
						break;
					case STRING:
						field = FieldSpec.builder(String.class, variableName).build();
						setter = generateSetter(String.class, variableName);
						getter = generateGetter(String.class, variableName);
						break;
					default:
						break;
						
					}
				}
				
				methods.add(setter);
				methods.add(getter);
				fields.add(field);
			}

			TypeSpec entityClass = TypeSpec.classBuilder(templateNode.getName())
			    .addModifiers(Modifier.PUBLIC)
			    .addMethods(methods)
			    .addFields(fields)
			    .build();

			JavaFile javaFile = JavaFile.builder(packageName, entityClass).build();
			
			File outDir = new File(outDirName);
			Writer writer = new FileWriter(outDir + "/" + templateNode.getName() + ".java");

			javaFile.writeTo(writer);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Type getNumberType(NumberType numberType) {
		switch(numberType){
		case BYTE:
			return byte.class;
		case DOUBLE:
			return double.class;
		case FLOAT:
			return float.class;
		case INT:
			return int.class;
		case LONG:
			return long.class;
		case SHORT:
			return short.class;
		default:
			return Object.class;
		}
	}

	private static MethodSpec generateSetter(Type type, String variableName) {
		String nameFirstUpper = variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
		return MethodSpec.methodBuilder("set" + nameFirstUpper)
			    .addModifiers(Modifier.PUBLIC)
			    .returns(void.class)
			    .addParameter(type, variableName)
			    .addStatement("this." + variableName + "=" + variableName)
			    .build();
	}
	private static MethodSpec generateGetter(Type type, String variableName) {
		String nameFirstUpper = variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
		return MethodSpec.methodBuilder("get" + nameFirstUpper)
			    .addModifiers(Modifier.PUBLIC)
			    .returns(type)
			    .addStatement("return " + variableName)
			    .build();
	}
	
}
