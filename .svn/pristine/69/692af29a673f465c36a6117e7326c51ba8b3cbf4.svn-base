package se.lth.cs.palcom.browsergui.dnd;

import se.lth.cs.palcom.discovery.proxy.Resource;
import se.lth.cs.palcom.discovery.proxy.ResourceListener;

public class DNDResourceWrapper<T> implements Resource {
	private T resource;

	public DNDResourceWrapper(T resource) {
		super();
		this.resource = resource;
	}

	public boolean isAvailable() {
		return false;
	}

	public void addListener(ResourceListener listener) {}
	public void removeListener(ResourceListener listener) {}

	public T getResource() {
		return resource;
	}
}
