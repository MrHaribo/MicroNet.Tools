package micronet.tools.composition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import micronet.tools.console.Console;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.PreferenceConstants;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class SyncPom {
	private SyncPom() {
	}

	public static boolean isServiceInApplicationPom(ServiceProject project) {
		return getServicesFromApplicationPom().contains(project.getName());
	}
	
	public static List<String> getServicesFromApplicationPom() {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String pomFilepath = myWorkspaceRoot.getLocation().append("pom.xml").toOSString();
		File pom = new File(pomFilepath);
		if (!pom.exists()) {
			createApplicationPom();
			return new ArrayList<>();
		}
		
		try {
			List<String> result = new ArrayList<>();
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(pomFilepath);

			NodeList list = doc.getElementsByTagName("module");
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				String servicePath = node.getTextContent();
				result.add(new File(servicePath).getName());
			}
			return result;
		} catch (Exception e) {
			Console.println("Error getting Services from Application POM");
			Console.printStackTrace(e);
		}
		return new ArrayList<>();
	}
	
	public static void createApplicationPom() {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String pomFilepath = myWorkspaceRoot.getLocation().append("pom.xml").toOSString();
		File pom = new File(pomFilepath);
		
		if (pom.exists())
			return;
		
		try {
			Bundle bundle = Platform.getBundle("com.github.mrharibo.micronet.tools.core");
			InputStream stream = FileLocator.openStream(bundle, new Path("resources/reference-pom.xml"), false);
	
			byte[] buffer = new byte[stream.available()];
			stream.read(buffer);
	
			OutputStream outStream = new FileOutputStream(pom);
			outStream.write(buffer);
			outStream.close();
			
			String groupID = getMetadataFromApplicationPom(PreferenceConstants.PREF_APP_GROUP_ID);
			String artifactID = getMetadataFromApplicationPom(PreferenceConstants.PREF_APP_ARTIFACT_ID);
			String version = getMetadataFromApplicationPom(PreferenceConstants.PREF_APP_VERSION);
			
			IPreferenceStore preferenceStore = ModelProvider.INSTANCE.getPreferenceStore();
			preferenceStore.setValue(PreferenceConstants.PREF_APP_GROUP_ID, groupID);
			preferenceStore.setValue(PreferenceConstants.PREF_APP_ARTIFACT_ID, artifactID);
			preferenceStore.setValue(PreferenceConstants.PREF_APP_VERSION, version);
			preferenceStore.setDefault(PreferenceConstants.PREF_APP_GROUP_ID, groupID);
			preferenceStore.setDefault(PreferenceConstants.PREF_APP_ARTIFACT_ID, artifactID);
			preferenceStore.setDefault(PreferenceConstants.PREF_APP_VERSION, version);
			
		} catch (Exception e) {
			Console.println("Error Creating Application POM");
			Console.printStackTrace(e);
		}
	}
	
	public static String getMetadataFromApplicationPom(String key) {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String pomFilepath = myWorkspaceRoot.getLocation().append("pom.xml").toOSString();
		File pom = new File(pomFilepath);
		if (!pom.exists()) {
			return null;
		}
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(pomFilepath);

			Node groupIDNode = doc.getElementsByTagName(key).item(0);
			return groupIDNode.getFirstChild().getTextContent();

		}  catch (Exception e) {
			Console.println("Error getting Metadata from Application POM");
			Console.printStackTrace(e);
		}
		return null;
	}
	
	public static void updateMetadataInApplicationPom(String key, String value) {
		if (key == null || value == null || key.equals("") || value.equals(""))
			return;
		
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String pomFilepath = myWorkspaceRoot.getLocation().append("pom.xml").toOSString();
		File pom = new File(pomFilepath);
		if (!pom.exists())
			createApplicationPom();
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(pomFilepath);
			
			Node node = doc.getElementsByTagName(key).item(0);
			Text nameNode = doc.createTextNode(value);
			node.replaceChild(nameNode, node.getFirstChild());
			
			writeToPom(pomFilepath, doc);
		} catch (Exception e) {
			Console.println("Error updating Metadata in Application POM");
			Console.printStackTrace(e);
		}
	}

	public static void updateServicesInApplicationPom(List<ServiceProject> projects) {
		try {

			IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			String pomFilepath = myWorkspaceRoot.getLocation().append("pom.xml").toOSString();
			File pom = new File(pomFilepath);
			if (!pom.exists())
				createApplicationPom();

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(pomFilepath);

			Element newModulesNode = doc.createElement("modules");

			for (ServiceProject project : projects) {
				if (!project.hasNature(Nature.MAVEN))
					continue;
				
				Element nameNode = doc.createElement("module");
				newModulesNode.appendChild(nameNode);
				Text moduleNameNode = doc.createTextNode(project.getRelativePath());
				nameNode.appendChild(moduleNameNode);
			}

			Node project = doc.getFirstChild();
			Node oldModulesNode = doc.getElementsByTagName("modules").item(0);
			project.replaceChild(newModulesNode, oldModulesNode);

			writeToPom(pomFilepath, doc);

			System.out.println("Pom update Done");

		} catch (Exception e) {
			Console.println("Error updating Services in Application POM");
			Console.printStackTrace(e);
		}
	}

	private static void writeToPom(String pomFilepath, Document doc) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(pomFilepath));
			transformer.transform(source, result);
		} catch (Exception e) {
			Console.println("Error writing POM file");
			Console.printStackTrace(e);
		}
	}
}
