package com.armedia.alfresco.extension.mock;

import java.util.Collection;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceServiceMemoryImpl;

public class DummyNamespaceService extends NamespaceServiceMemoryImpl {

	public DummyNamespaceService() {
	}

	@Override
	public void unregisterNamespace(String prefix) {
		super.unregisterNamespace(prefix);
	}

	@Override
	public String getNamespaceURI(String prefix) throws NamespaceException {
		String uri = super.getNamespaceURI(prefix);
		if (uri == null) {
			uri = String.format("{%s}", prefix);
			registerNamespace(prefix, uri);
		}
		return uri;
	}

	@Override
	public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException {
		return super.getPrefixes(namespaceURI);
	}

}