package micronet.tools.codegen;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
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
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import micronet.script.ScriptExecutor;
import micronet.tools.filesync.SyncEnumTree;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.INode;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityTemplateRootNode;
import micronet.tools.model.nodes.EntityVariableConstNode;
import micronet.tools.model.nodes.EntityVariableDynamicNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.model.nodes.EnumRootNode;
import micronet.tools.model.nodes.ModelNode;
import micronet.tools.model.nodes.PrefabVariableNode;
import micronet.tools.model.variables.CollectionDescription;
import micronet.tools.model.variables.ComponentDescription;
import micronet.tools.model.variables.EnumDescription;
import micronet.tools.model.variables.GeometryDescription;
import micronet.tools.model.variables.MapDescription;
import micronet.tools.model.variables.NumberDescription;
import micronet.tools.model.variables.NumberType;
import micronet.tools.model.variables.ScriptDescription;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;
import micronet.type.Vector2;
import micronet.type.Vector3;

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
			List<FieldSpec> ctorArgs = new ArrayList<>();
			
			for (INode node : templateNode.getChildren()) {
				
				if (node instanceof EntityVariableNode) {
					
					EntityVariableNode variableNode = (EntityVariableNode) node;
					
					if (variableNode instanceof EntityVariableConstNode) {
						FieldSpec field = generateConstField((EntityVariableConstNode)variableNode);
						if (field == null)
							continue;
						fields.add(field);
					} else if (variableNode instanceof EntityVariableDynamicNode) {
						
						EntityVariableDynamicNode dynamicVariable = (EntityVariableDynamicNode)node;
						
						if (variableNode.getVariabelDescription().getType() == VariableType.SCRIPT) {
							ScriptDescription scriptDesc = (ScriptDescription) variableNode.getVariabelDescription();
							MethodSpec scriptMethod = generateScriptMethod(variableNode, scriptDesc);
							methods.add(scriptMethod);
							continue;
						} else {
							FieldSpec field = generateField(variableNode);
							MethodSpec setter = generateSetter(field.type, variableNode.getName());
							MethodSpec getter = generateGetter(field.type, variableNode.getName());
							methods.add(setter);
							methods.add(getter);
							
							if (dynamicVariable.isCtorArg())
								ctorArgs.add(field);
							fields.add(field);
						}
					}
				}
			}

			TypeSpec entityClass = generateModelType(templateNode, methods, fields, ctorArgs);
			JavaFile javaFile = JavaFile.builder(packageName, entityClass).build();
			JavaFileObject file = filer.createSourceFile(packageName + "." + templateNode.getName());
			Writer writer = file.openWriter();
			javaFile.writeTo(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private MethodSpec generateScriptMethod(EntityVariableNode variableNode, ScriptDescription scriptDesc) {
		
		StringBuilder parametersCalls = new StringBuilder();
		for (String memberArg : scriptDesc.getMemberArgs()) {

			for (INode variable : ((ModelNode)variableNode.getParent()).getChildren()) {
				if (variable.getName().equals(memberArg)) {
					EntityVariableNode variableMirror = (EntityVariableNode) variable;
					if (variableMirror instanceof EntityVariableConstNode) {
						parametersCalls.append(",");
						parametersCalls.append(memberArg);
					} else if (variableMirror.getVariabelDescription().getType() == VariableType.SCRIPT) {
						parametersCalls.append(",");
						parametersCalls.append(memberArg);
						parametersCalls.append("()");
					} else {
						parametersCalls.append(",");
						parametersCalls.append(getGetterName(memberArg));
						parametersCalls.append("()");
					}
				}
			}
		}
		
		StringBuilder externalParams = new StringBuilder();
		List<ParameterSpec> externalParametersArray = new ArrayList<>();
		for (Map.Entry<String, VariableDescription> externalArg : scriptDesc.getExternalArgs().entrySet()) {
			ParameterSpec argTypeSpec;
			if (externalArg.getValue().getType() == VariableType.NUMBER) {
				Type argType = getPrimitiveNumberType(((NumberDescription)externalArg.getValue()).getNumberType());
				argTypeSpec = ParameterSpec.builder(argType, externalArg.getKey()).build();
			} else {
				TypeName argType = getBoxingType(externalArg.getValue());
				argTypeSpec = ParameterSpec.builder(argType, externalArg.getKey()).build();
			}
			externalParametersArray.add(argTypeSpec);
			
			externalParams.append(",");
			externalParams.append(externalArg.getKey());
		}
		
		MethodSpec scriptMethod = MethodSpec.methodBuilder(variableNode.getName())
			    .addModifiers(Modifier.PUBLIC)
			    .addParameters(externalParametersArray)
			    .returns(Object.class)
			    .addStatement("return $T.INSTANCE.invokeFunction($S$L$L)", ScriptExecutor.class, scriptDesc.getScriptName(), parametersCalls, externalParams)
			    .build();
		return scriptMethod;
	}

	private TypeSpec generateModelType(EntityTemplateNode templateNode, List<MethodSpec> methods,
			List<FieldSpec> fields, List<FieldSpec> ctorArgs) {
		TypeSpec.Builder entityBuilder = TypeSpec.classBuilder(templateNode.getName())
		    .addModifiers(Modifier.PUBLIC)
		    .addMethods(methods)
		    .addFields(fields);
		
		INode parentNode = templateNode.getParent();
		List<FieldSpec> parentCtorArgs = new ArrayList<>();
		while (parentNode != null) {
			List<FieldSpec> tempList = new ArrayList<>();
			for (INode node : ((ModelNode)parentNode).getChildren()) {
				if (node instanceof EntityVariableDynamicNode) {
					EntityVariableDynamicNode potentialCtorArg = (EntityVariableDynamicNode)node;
					if (potentialCtorArg.isCtorArg()) {
						FieldSpec argSpec = generateField(potentialCtorArg);
						tempList.add(argSpec);
					}
				}
			}
			Collections.reverse(tempList);
			parentCtorArgs.addAll(tempList);
			parentNode = parentNode.getParent();
		}
		
		if (ctorArgs.size() > 0 || parentCtorArgs.size() > 0) {
			MethodSpec.Builder ctorBuilder = MethodSpec.constructorBuilder()
					.addModifiers(Modifier.PUBLIC);
			
			if (parentCtorArgs.size() > 0) {
				
				Collections.reverse(parentCtorArgs);
				StringBuilder superCtorArgs = new StringBuilder();
				for (FieldSpec parentCtorArg : parentCtorArgs) {
					superCtorArgs.append(parentCtorArg.name);
					superCtorArgs.append(", ");
				}
				
				ctorBuilder.addStatement("super($L)", superCtorArgs.substring(0, superCtorArgs.toString().lastIndexOf(",")));
			}
			
			for (FieldSpec parentCtorArg : parentCtorArgs) {
				ctorBuilder.addParameter(parentCtorArg.type, parentCtorArg.name);
			}

			for (FieldSpec ctorArg : ctorArgs) {
				ctorBuilder.addParameter(ctorArg.type, ctorArg.name);
				ctorBuilder.addStatement("this.$N = $N", ctorArg.name, ctorArg.name);
			}
			
			entityBuilder.addMethod(ctorBuilder.build());
		}

		if (templateNode.getParent() != null) {
			if (templateNode.getParent() instanceof EntityTemplateNode && !(templateNode.getParent() instanceof EntityTemplateRootNode)) {
				TypeName superTypename = ClassName.get(packageName, templateNode.getParent().getName());
				entityBuilder.superclass(superTypename);
			}
		}

		TypeSpec entityClass = entityBuilder.build();
		return entityClass;
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
		case SCRIPT:
		case GEOMETRY:
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
		} else if(variableDesc.getType() == VariableType.GEOMETRY) {
			GeometryDescription geometryDesc = (GeometryDescription) variableDesc;
			switch (geometryDesc.getGeometryType()) {
			case VECTOR2:
				return ClassName.get(Vector2.class);
			case VECTOR3:
				return ClassName.get(Vector3.class);
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
			case SCRIPT:
				return ClassName.get(Object.class);
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
	
	private FieldSpec generateConstField(EntityVariableConstNode variableNode) {
		String variableName = variableNode.getName();
		
		PrefabVariableNode prefabNode = (PrefabVariableNode) variableNode.getChildren().get(0);
		
		
		switch (variableNode.getVariabelDescription().getType()) {
		case BOOLEAN:
			return FieldSpec.builder(boolean.class, variableName)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
				.initializer("$L", prefabNode.getVariableValue()).build();
		case CHAR:
			return FieldSpec.builder(char.class, variableName)
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer("'$L'", prefabNode.getVariableValue()).build();
		case ENUM:
			EnumDescription enumDesc = (EnumDescription) variableNode.getVariabelDescription();
			TypeName enumTypeName = ClassName.get(packageName, enumDesc.getEnumType());
			return FieldSpec.builder(enumTypeName, variableName)
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer("$T.$L", enumTypeName, prefabNode.getVariableValue()).build();
		case COMPONENT:
		case LIST:
		case SET:
		case MAP:
			return null;
		case NUMBER:
			NumberDescription numDesc = (NumberDescription) variableNode.getVariabelDescription();
			Type numberType = getPrimitiveNumberType(numDesc.getNumberType());
			FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(numberType, variableName)
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
			if (numDesc.getNumberType() == NumberType.FLOAT) {
				fieldSpecBuilder.initializer("$Lf", prefabNode.getVariableValue());
			} else if (numDesc.getNumberType() == NumberType.LONG) {
				fieldSpecBuilder.initializer("$LL", prefabNode.getVariableValue());
			} else {
				fieldSpecBuilder.initializer("$L", prefabNode.getVariableValue());
			}
			return fieldSpecBuilder.build();
		case STRING:
			return FieldSpec.builder(String.class, variableName)
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer("$S", prefabNode.getVariableValue()).build();
		case GEOMETRY:
			return FieldSpec.builder(getBoxingType(variableNode.getVariabelDescription()), variableName)
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer("new $T()", getBoxingType(variableNode.getVariabelDescription()), prefabNode.getVariableValue()).build();
		case SCRIPT:
		default:
			return null;
		}
	}
	
	private FieldSpec generateField(EntityVariableNode variableNode) {
		String variableName = variableNode.getName();
		switch (variableNode.getVariabelDescription().getType()) {
		case BOOLEAN:
			return FieldSpec.builder(boolean.class, variableName).addModifiers(Modifier.PRIVATE).build();
		case CHAR:
			return FieldSpec.builder(char.class, variableName).addModifiers(Modifier.PRIVATE).build();
		case COMPONENT:
			ComponentDescription componentDesc = (ComponentDescription) variableNode.getVariabelDescription();
			TypeName componentClassName = ClassName.get(packageName, componentDesc.getComponentType());
			return FieldSpec.builder(componentClassName, variableName).addModifiers(Modifier.PRIVATE).build();
		case ENUM:
			EnumDescription enumDesc = (EnumDescription) variableNode.getVariabelDescription();
			TypeName enumTypeName = ClassName.get(packageName, enumDesc.getEnumType());
			return FieldSpec.builder(enumTypeName, variableName).addModifiers(Modifier.PRIVATE).build();
		case LIST:
		case SET:
		case MAP:
			TypeName entryTypeName = getParametrizedEntryTypeName(variableNode.getVariabelDescription(), packageName);
			return FieldSpec.builder(entryTypeName, variableName).addModifiers(Modifier.PRIVATE).build();
		case NUMBER:
			NumberDescription numDesc = (NumberDescription) variableNode.getVariabelDescription();
			Type numberType = getPrimitiveNumberType(numDesc.getNumberType());
			return FieldSpec.builder(numberType, variableName).addModifiers(Modifier.PRIVATE).build();
		case STRING:
			return FieldSpec.builder(String.class, variableName).addModifiers(Modifier.PRIVATE).build();
		case GEOMETRY:
			return FieldSpec.builder(getBoxingType(variableNode.getVariabelDescription()), variableName).addModifiers(Modifier.PRIVATE).build();
		case SCRIPT:
		default:
			return null;
		}
	}

	private MethodSpec generateSetter(TypeName typeName, String variableName) {
		return MethodSpec.methodBuilder(getSetterName(variableName))
			    .addModifiers(Modifier.PUBLIC)
			    .returns(void.class)
			    .addParameter(typeName, variableName)
			    .addStatement("this." + variableName + "=" + variableName)
			    .build();
	}
	
	private MethodSpec generateGetter(TypeName typeName, String variableName) {
		return MethodSpec.methodBuilder(getGetterName(variableName))
			    .addModifiers(Modifier.PUBLIC)
			    .returns(typeName)
			    .addStatement("return " + variableName)
			    .build();
	}
	
	private String getSetterName(String variableName) {
		String nameFirstUpper = variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
		return "set" + nameFirstUpper;
	}
	
	private String getGetterName(String variableName) {
		String nameFirstUpper = variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
		return "get" + nameFirstUpper;	
	}
}
