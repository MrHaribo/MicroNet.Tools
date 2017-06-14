package micronet.tools.codegen;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

import micronet.tools.filesync.SyncEnumTree;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.INode;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityTemplateRootNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.model.nodes.EnumRootNode;
import micronet.tools.model.variables.CollectionDescription;
import micronet.tools.model.variables.ComponentDescription;
import micronet.tools.model.variables.EnumDescription;
import micronet.tools.model.variables.MapDescription;
import micronet.tools.model.variables.NumberDescription;
import micronet.tools.model.variables.NumberType;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;

public class ModelGenerator {
	
	private String packageName;
	private Filer filer;
	
	public ModelGenerator(String packageName, Filer filer) {
		this.packageName = packageName;
		this.filer = filer;
	}
	
	public void generateModel(String sharedDir) {
		List<EntityTemplateNode> templates = SyncTemplateTree.loadAllTemplateTypes(sharedDir);
		for (EntityTemplateNode template : templates) {
			generateModelEntity(template);
		}
		generateEnums(sharedDir);
	}
	
	private void generateEnums(String sharedDir) {
		EnumRootNode enumTree = SyncEnumTree.loadEnumTree(sharedDir);
		for (INode node : enumTree.getChildren()) {
			if (node instanceof EnumNode) {
				generateEnum((EnumNode)node);
			}
		}
	}

	private void generateEnum(EnumNode node) {
		// TODO Auto-generated method stub
		String[] entries = (String[]) node.getEnumConstants().toArray(new String[node.getEnumConstants().size()]);

		TypeSpec.Builder builder = TypeSpec.enumBuilder(node.getName()).addModifiers(Modifier.PUBLIC);
		for (String entry : entries) {
			builder.addEnumConstant(entry);
		}
		TypeSpec typeSpec = builder.build();
		JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

		try {
			JavaFileObject file = filer.createSourceFile(packageName + "." + node.getName());
			Writer writer = file.openWriter();
			javaFile.writeTo(writer);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void generateModelEntity(EntityTemplateNode templateNode) {
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
					case SET:
					case MAP:
						TypeName entryTypeName = getParametrizedEntryTypeName(variableNode.getVariabelDescription(), packageName);
						field = FieldSpec.builder(entryTypeName, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(entryTypeName, variableName);
						getter = generateGetter(entryTypeName, variableName);
						break;
					case NUMBER:
						NumberDescription numDesc = (NumberDescription) variableNode.getVariabelDescription();
						Type numberType = getPrimitiveNumberType(numDesc.getNumberType());
						field = FieldSpec.builder(numberType, variableName).addModifiers(Modifier.PRIVATE).build();
						setter = generateSetter(numberType, variableName);
						getter = generateGetter(numberType, variableName);
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
			
			JavaFileObject file = filer.createSourceFile(packageName + "." + templateNode.getName());
			Writer writer = file.openWriter();
			javaFile.writeTo(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static TypeName getParametrizedEntryTypeName(VariableDescription variableDesc, String packageName) {
		
		switch (variableDesc.getType()) {
		case COMPONENT:
			ComponentDescription entryComponentDesc = (ComponentDescription) variableDesc;
			return ClassName.get(packageName, entryComponentDesc.getComponentType());
		case ENUM:
			EnumDescription enumDesc = (EnumDescription) variableDesc;
			return ClassName.get(packageName, enumDesc.getEnumType());
		case LIST:
			CollectionDescription listDesc = (CollectionDescription) variableDesc;
			ClassName listClassName = ClassName.get(List.class);
			TypeName entryTypeName = getParametrizedEntryTypeName(listDesc.getEntryType(), packageName);
			return ParameterizedTypeName.get(listClassName, entryTypeName);
		case SET:
			CollectionDescription setDesc = (CollectionDescription) variableDesc;
			ClassName setClassName = ClassName.get(Set.class);
			TypeName setEntryTypeName = getParametrizedEntryTypeName(setDesc.getEntryType(), packageName);
			return ParameterizedTypeName.get(setClassName, setEntryTypeName);
		case MAP:
			MapDescription mapDesc = (MapDescription) variableDesc;
			ClassName mapClassName = ClassName.get(Map.class);
			TypeName mapKeyTypeName = getParametrizedEntryTypeName(mapDesc.getKeyType(), packageName);
			TypeName mapEntryTypeName = getParametrizedEntryTypeName(mapDesc.getEntryType(), packageName);
			return ParameterizedTypeName.get(mapClassName, mapKeyTypeName, mapEntryTypeName);
		case STRING:
		case NUMBER:
		case BOOLEAN:
		case CHAR:
		default:
			return getBoxingType(variableDesc);
		
		}
	}

	private static TypeName getBoxingType(VariableDescription variableDesc) {
		
		if (variableDesc.getType() == VariableType.NUMBER) {
			NumberDescription numberDesc = (NumberDescription) variableDesc;
			switch (numberDesc.getNumberType()) {
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
		} else {
			switch (variableDesc.getType()) {
			case BOOLEAN:
				return ClassName.get(Boolean.class);
			case CHAR:
				return ClassName.get(Character.class);
			case STRING:
				return ClassName.get(String.class);
			default:
				return null;
			}
		}
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
