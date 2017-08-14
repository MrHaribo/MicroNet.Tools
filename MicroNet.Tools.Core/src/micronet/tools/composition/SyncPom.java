package micronet.tools.composition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

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
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
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
			Bundle bundle = Platform.getBundle("MicroNet.Tools.Core");
			InputStream stream = FileLocator.openStream(bundle, new Path("resources/reference-pom.xml"), false);
	
			byte[] buffer = new byte[stream.available()];
			stream.read(buffer);
	
			OutputStream outStream = new FileOutputStream(pom);
			outStream.write(buffer);
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateMetadataInApplicationPom() {
		ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "MicroNet.Tools.Preferences");
		String groupID = preferenceStore.getString(PreferenceConstants.APP_GROUP_ID);
		String artifactID = preferenceStore.getString(PreferenceConstants.APP_ARTIFACT_ID);
		String version = preferenceStore.getString(PreferenceConstants.APP_VERSION);
		updateMetadataInApplicationPom(groupID, artifactID, version);
	}
	
	public static void updateMetadataInApplicationPom(String groupID, String artifactID, String version) {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String pomFilepath = myWorkspaceRoot.getLocation().append("pom.xml").toOSString();
		File pom = new File(pomFilepath);
		if (!pom.exists())
			createApplicationPom();
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(pomFilepath);
			
			Node groupIDNode = doc.getElementsByTagName("groupId").item(0);
			Text groupIDNameNode = doc.createTextNode(groupID);
			groupIDNode.replaceChild(groupIDNameNode, groupIDNode.getFirstChild());

			Node artifactIdNode = doc.getElementsByTagName("artifactId").item(0);
			Text artifactIdNameNode = doc.createTextNode(artifactID);
			artifactIdNode.replaceChild(artifactIdNameNode, artifactIdNode.getFirstChild());
			
			Node versionNode = doc.getElementsByTagName("version").item(0);
			Text versionNameNode = doc.createTextNode(version);
			versionNode.replaceChild(versionNameNode, versionNode.getFirstChild());
			
			Node nameNode = doc.getElementsByTagName("name").item(0);
			Text nameNameNode = doc.createTextNode(artifactID);
			nameNode.replaceChild(nameNameNode, nameNode.getFirstChild());
			
			writeToPom(pomFilepath, doc);
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
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

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
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
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
}
