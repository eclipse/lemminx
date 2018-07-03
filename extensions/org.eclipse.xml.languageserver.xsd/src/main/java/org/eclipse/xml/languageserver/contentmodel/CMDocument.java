package org.eclipse.xml.languageserver.contentmodel;

import org.eclipse.xml.languageserver.model.Node;

public interface CMDocument {

	CMElement findCMElement(Node element);

}
