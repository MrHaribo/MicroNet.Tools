package micronet.tools.ui.modelview.codegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.ModelConstants;
import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityTemplateRootNode;
import micronet.tools.ui.modelview.nodes.EntityVariableNode;
import micronet.tools.ui.modelview.variables.CollectionDescription;
import micronet.tools.ui.modelview.variables.ComponentDescription;
import micronet.tools.ui.modelview.variables.EnumDescription;
import micronet.tools.ui.modelview.variables.MapDescription;
import micronet.tools.ui.modelview.variables.NumberDescription;
import micronet.tools.ui.modelview.variables.NumberType;
import micronet.tools.ui.modelview.variables.VariableType;

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
				
				if (node instanceof EntityVariableNode) {
					EntityVariableNode variableNode = (EntityVariableNode) node;
					
					switch (variableNode.getVariabelDescription().getType()) {
					case BOOLEAN:
						field = FieldSpec.builder(boolean.class, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(boolean.class, variableName);
						getter = generateGetter(boolean.class, variableName);
						break;
					case CHAR:
						field = FieldSpec.builder(boolean.class, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(boolean.class, variableName);
						getter = generateGetter(boolean.class, variableName);
						break;
					case COMPONENT:
						ComponentDescription componentDesc = (ComponentDescription) variableNode.getVariabelDescription();
						TypeName componentClassName = ClassName.get(packageName, componentDesc.getComponentType());
						field = FieldSpec.builder(componentClassName, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(componentClassName, variableName);
						getter = generateGetter(componentClassName, variableName);
						break;
					case ENUM:
						EnumDescription enumDesc = (EnumDescription) variableNode.getVariabelDescription();
						TypeName enumTypeName = ClassName.get(packageName, enumDesc.getEnumType());
						field = FieldSpec.builder(enumTypeName, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(enumTypeName, variableName);
						getter = generateGetter(enumTypeName, variableName);
						break;
					case LIST:
						CollectionDescription listDesc = (CollectionDescription) variableNode.getVariabelDescription();
						ClassName listClassName = ClassName.get(List.class);
						TypeName entryTypeName = null;
						if (ModelConstants.isTemplateCollection(listDesc)) {
							entryTypeName = ClassName.get(packageName, listDesc.getEntryType());
						} else {
							entryTypeName = getPrimitiveCollectionType(listDesc);
						}
						TypeName parametrizedListTypeName = ParameterizedTypeName.get(listClassName, entryTypeName);
						field = FieldSpec.builder(parametrizedListTypeName, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(parametrizedListTypeName, variableName);
						getter = generateGetter(parametrizedListTypeName, variableName);
						break;
					case MAP:
						MapDescription mapDesc = (MapDescription) variableNode.getVariabelDescription();
						ClassName mapClassName = ClassName.get(Map.class);
						TypeName mapKeyTypeName = getPrimitiveTypeFromName(mapDesc.getKeyType());
						
						TypeName mapEntryTypeName =  null;
						if (ModelConstants.isTemplateCollection(mapDesc)) {
							mapEntryTypeName = ClassName.get(packageName, mapDesc.getEntryType());
						} else {
							mapEntryTypeName = getPrimitiveCollectionType(mapDesc);
						}
						TypeName parametrizedMapTypeName = ParameterizedTypeName.get(mapClassName, mapKeyTypeName, mapEntryTypeName);
						field = FieldSpec.builder(parametrizedMapTypeName, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(parametrizedMapTypeName, variableName);
						getter = generateGetter(parametrizedMapTypeName, variableName);
						break;
					case NUMBER:
						NumberDescription numDesc = (NumberDescription) variableNode.getVariabelDescription();
						Type numberType = getPrimitiveNumberType(numDesc.getNumberType());
						field = FieldSpec.builder(numberType, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(numberType, variableName);
						getter = generateGetter(numberType, variableName);
						break;
					case REF:
						field = FieldSpec.builder(Object.class, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(Object.class, variableName);
						getter = generateGetter(Object.class, variableName);
						break;
					case SET:
						CollectionDescription setDesc = (CollectionDescription) variableNode.getVariabelDescription();
						ClassName setClassName = ClassName.get(Set.class);
						TypeName setEntryTypeName = getPrimitiveCollectionType(setDesc);

						TypeName parametrizedSetTypeName = ParameterizedTypeName.get(setClassName, setEntryTypeName);
						field = FieldSpec.builder(parametrizedSetTypeName, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(parametrizedSetTypeName, variableName);
						getter = generateGetter(parametrizedSetTypeName, variableName);
						break;
					case STRING:
						field = FieldSpec.builder(String.class, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(String.class, variableName);
						getter = generateGetter(String.class, variableName);
						break;
					default:
						break;
						
					}
				}
				
				if (field != null) {
					methods.add(setter);
					methods.add(getter);
					fields.add(field);
				}

			}

			Builder entityBuilder = TypeSpec.classBuilder(templateNode.getName())
			    .addModifiers(Modifier.PUBLIC)
			    .addMethods(methods)
			    .addFields(fields);
			
			if (templateNode.getParent() != null) {
				if (templateNode.getParent() instanceof EntityTemplateNode && !(templateNode.getParent() instanceof EntityTemplateRootNode)) {
					TypeName superTypename = ClassName.get(packageName, templateNode.getParent().getName());
					entityBuilder.superclass(superTypename);
				}
			}

			TypeSpec entityClass = entityBuilder.build();
			
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
	
	private static TypeName getPrimitiveTypeFromName(String typeName) { 
		return getBoxingType(typeName);
	}
	
	private static TypeName getPrimitiveCollectionType(CollectionDescription desc) {
		return getBoxingType(desc.getEntryType());
	}
	
	private static TypeName getBoxingType(String typeName) {
		
		NumberType numberType = ModelConstants.getNumberEntryTypeFromName(typeName);
		
		if (numberType != null) {
			switch (numberType) {
			case BYTE:
				return ClassName.get(Byte.class);
			case DOUBLE:
				return ClassName.get(Double.class);
			case FLOAT:
				return ClassName.get(Float.class);
			case INT:
				return ClassName.get(Integer.class);
			case LONG:
				return ClassName.get(Long.class);
			case SHORT:
				return ClassName.get(Short.class);
			default:
				return null;
			}
		}
		
		VariableType variableType = ModelConstants.getVariableEntryTypeFromName(typeName);
		if (variableType != null) {
			switch (variableType) {
			case BOOLEAN:
				return ClassName.get(Boolean.class);
			case CHAR:
				return ClassName.get(Character.class);
			case REF:
				return ClassName.get(Object.class);
			case STRING:
				return ClassName.get(String.class);
			default:
				return null;
			}
		}
		return null;
	}

	private static Type getPrimitiveNumberType(NumberType numberType) {
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

	private static MethodSpec generateSetter(TypeName typeName, String variableName) {
		String nameFirstUpper = variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
		return MethodSpec.methodBuilder("set" + nameFirstUpper)
			    .addModifiers(Modifier.PUBLIC)
			    .returns(void.class)
			    .addParameter(typeName, variableName)
			    .addStatement("this." + variableName + "=" + variableName)
			    .build();
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
	private static MethodSpec generateGetter(TypeName typeName, String variableName) {
		String nameFirstUpper = variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
		return MethodSpec.methodBuilder("get" + nameFirstUpper)
			    .addModifiers(Modifier.PUBLIC)
			    .returns(typeName)
			    .addStatement("return " + variableName)
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
