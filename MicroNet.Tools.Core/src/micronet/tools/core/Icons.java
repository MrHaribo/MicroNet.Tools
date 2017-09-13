package micronet.tools.core;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class Icons {
	public static final ImageDescriptor IMG_MICRONET = getImageDescriptor("micronet_icon.png");
	public static final ImageDescriptor IMG_ADD = getImageDescriptor("add.png");
	public static final ImageDescriptor IMG_REMOVE = getImageDescriptor("remove.png");
	public static final ImageDescriptor IMG_CHECK = getImageDescriptor("check.png");
	public static final ImageDescriptor IMG_QUESTION = getImageDescriptor("question.png");
	public static final ImageDescriptor IMG_REFRESH = getImageDescriptor("refresh.png");
	public static final ImageDescriptor IMG_LETTERBOX = getImageDescriptor("letterbox.png");
	public static final ImageDescriptor IMG_SCRIPT = getImageDescriptor("js.png");
	
	public static final ImageDescriptor IMG_PARAM = getImageDescriptor("param.png");
	public static final ImageDescriptor IMG_PARAM_GREY = getImageDescriptor("param_grey.png");
	
	public static final ImageDescriptor IMG_CHECKED = getImageDescriptor("checked.png");
	public static final ImageDescriptor IMG_UNCHECKED = getImageDescriptor("unchecked.png");

	public static final ImageDescriptor IMG_DEBUG = getImageDescriptor("debug.png");
	public static final ImageDescriptor IMG_RUN = getImageDescriptor("run.png");

	public static final ImageDescriptor IMG_LAUNCH_GROUP = getImageDescriptor("launch_group.png");
	public static final ImageDescriptor IMG_NATIVE_JAVA = getImageDescriptor("native_java.png");
	public static final ImageDescriptor IMG_DOCKER = getImageDescriptor("docker.png");
	public static final ImageDescriptor IMG_MAVEN = getImageDescriptor("maven.png");
	public static final ImageDescriptor IMG_COUCHBASE = getImageDescriptor("couchbase.png");
	public static final ImageDescriptor IMG_ACTIVEMQ = getImageDescriptor("activemq.png");
	public static final ImageDescriptor IMG_POSTGRESQL = getImageDescriptor("postgreesql.png");
	
	public static final ImageDescriptor IMG_CONST = getImageDescriptor("const.png");
	public static final ImageDescriptor IMG_PREFAB = getImageDescriptor("prefab.png");
	public static final ImageDescriptor IMG_VARIABLE = getImageDescriptor("variable.png");
	public static final ImageDescriptor IMG_TEMPLATE = getImageDescriptor("template.png");
	public static final ImageDescriptor IMG_ENUM = getImageDescriptor("enum.png");
	public static final ImageDescriptor IMG_BUILD = getImageDescriptor("build.png");
	
	public static final ImageDescriptor IMG_TERMINAL = getImageDescriptor("terminal.gif");
	public static final ImageDescriptor IMG_PROPERTIES = getImageDescriptor("properties.gif");
	
	public static ImageDescriptor getImageDescriptor(String file) {
		Bundle bundle = FrameworkUtil.getBundle(Icons.class);
		URL url = FileLocator.find(bundle, new org.eclipse.core.runtime.Path("icons/" + file), null);
		return ImageDescriptor.createFromURL(url);
	}
}
