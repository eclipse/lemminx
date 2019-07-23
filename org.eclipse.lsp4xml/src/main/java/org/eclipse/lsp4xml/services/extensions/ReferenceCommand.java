package org.eclipse.lsp4xml.services.extensions;

import java.util.Arrays;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.client.ClientCommands;

public class ReferenceCommand extends Command {

	private transient int nbReferences = 1;

	public ReferenceCommand(String uri, Position position, boolean supportedByClient) {
		super(getTitle(1), supportedByClient ? ClientCommands.SHOW_REFERENCES : "");
		super.setArguments(Arrays.asList(uri, position));
	}

	public void increment() {
		nbReferences++;
		super.setTitle(getTitle(nbReferences));
	}

	private static String getTitle(int nbReferences) {
		if (nbReferences == 1) {
			return nbReferences + " reference";
		}
		return nbReferences + " references";
	}

}
