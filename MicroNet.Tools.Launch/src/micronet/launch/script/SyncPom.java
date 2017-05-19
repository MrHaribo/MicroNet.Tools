package micronet.launch.script;

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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public final class SyncPom {
	private SyncPom() {
	}

	public static List<String> getServicesFromGamePom() {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String pomFilepath = myWorkspaceRoot.getLocation().append("pom.xml").toOSString();
		File pom = new File(pomFilepath);

		if (!pom.exists())
			return new ArrayList<>();
		try {
			List<String> result = new ArrayList<>();
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(pomFilepath);

			NodeList list = doc.getElementsByTagName("module");
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				String serviceName = node.getTextContent();
				result.add(serviceName);
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

	public static void updateGamePom(List<IProject> projects) {
		try {

			IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			String pomFilepath = myWorkspaceRoot.getLocation().append("pom.xml").toOSString();
			File pom = new File(pomFilepath);

			if (!pom.exists()) {
				Bundle bundle = Platform.getBundle("MicroNet.Tools.Launch");
				InputStream stream = FileLocator.openStream(bundle, new Path("resources/reference-pom.xml"), false);

				byte[] buffer = new byte[stream.available()];
				stream.read(buffer);

				OutputStream outStream = new FileOutputStream(pom);
				outStream.write(buffer);
				outStream.close();
			}

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(pomFilepath);

			Element newModulesNode = doc.createElement("modules");

			for (IProject project : projects) {
				Element nameNode = doc.createElement("module");
				newModulesNode.appendChild(nameNode);
				Text moduleNameNode = doc.createTextNode(project.getName());
				nameNode.appendChild(moduleNameNode);
			}

			Node project = doc.getFirstChild();
			Node oldModulesNode = doc.getElementsByTagName("modules").item(0);
			project.replaceChild(newModulesNode, oldModulesNode);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(pomFilepath));
			transformer.transform(source, result);

			System.out.println("Pom update Done");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
	}
}
