package micronet.tools.annotation;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;

import micronet.annotation.MessageService;

public class ServiceDescription {
	Element service;

	Comparator<Element> elementComperator = new Comparator<Element>() {
		public int compare(Element u1, Element u2) {
			return u1.getSimpleName().toString().compareTo(u2.getSimpleName().toString());
		}
	};

	Set<Element> messageListeners = new TreeSet<Element>(elementComperator);
	Set<Element> startMethods = new TreeSet<Element>(elementComperator);
	Set<Element> stopMethods = new TreeSet<Element>(elementComperator);

	public Element getService() {
		return service;
	}

	public void setService(Element service) {
		this.service = service;
	}

	public Set<? extends Element> getMessageListeners() {
		return messageListeners;
	}

	public void setMessageListeners(Set<? extends Element> messageListeners) {
		this.messageListeners.clear();
		this.messageListeners.addAll(messageListeners);
	}
	
	public void addMessageListeners(Set<? extends Element> messageListeners) {
		this.messageListeners.addAll(messageListeners);
	}

	public Set<? extends Element> getStartMethods() {
		return startMethods;
	}

	public void setStartMethods(Set<? extends Element> startMethods) {
		this.startMethods.clear();
		this.startMethods.addAll(startMethods);
	}

	public Set<? extends Element> getStopMethods() {
		return stopMethods;
	}

	public void setStopMethods(Set<? extends Element> stopMethods) {
		this.stopMethods.clear();
		this.stopMethods.addAll(stopMethods);
	}

	public String getName() {
		if (service == null)
			return null;
		return service.getSimpleName().toString();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getTypename() {
		return getPackage() + "." + getName();
	}
	
	public String getPackage() {
		if (service == null || service.getEnclosingElement() == null)
			return null;
		
		if (service.getEnclosingElement() instanceof PackageElement) {
			return ((PackageElement)service.getEnclosingElement()).getQualifiedName().toString();
		}
		return service.getEnclosingElement().getSimpleName().toString();
	}

	public String getURI() {
		if (service == null)
			return null;
		return service.getAnnotation(MessageService.class).uri();
	}
	
	public String getDescription() {
		if (service == null)
			return null;
		try {
			return service.getAnnotation(MessageService.class).desc();
		} catch (UndeclaredThrowableException e) {
			if (!(e.getUndeclaredThrowable() instanceof NoSuchMethodException))
				e.getUndeclaredThrowable().printStackTrace();
		}
		return null;
	}
}