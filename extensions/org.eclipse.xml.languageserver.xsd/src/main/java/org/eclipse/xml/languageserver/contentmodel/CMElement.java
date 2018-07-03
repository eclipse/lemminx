package org.eclipse.xml.languageserver.contentmodel;

import java.util.Collection;

public interface CMElement {

	String getName();

	Collection<CMElement> getElements();

	CMElement findCMElement(String tag, String namespace);

	String getDocumentation();
}
