/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml;

import org.eclipse.lsp4xml.utils.StringUtils;

/**
 * PLatform information about OS and JVM.
 */
public class Platform {

	private static final String UNKNOWN_VALUE = "unknown";

	private static final OS os = new OS();

	public static final boolean isWindows = getOS().isWindows();
	public static String SLASH = isWindows ? "\\" : "/";

	/**
	 * OS information
	 */
	public static class OS {

		private final String name;

		private final String version;

		private final String arch;

		private final transient boolean isWindows;

		public OS() {
			this.name = getSystemProperty("os.name");
			this.version = getSystemProperty("os.version");
			this.arch = getSystemProperty("os.arch");
			isWindows = name != null && name.toLowerCase().indexOf("win") >= 0;
		}

		/**
		 * Returns the OS name.
		 * 
		 * @return the OS name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the OS version.
		 * 
		 * @return the OS version.
		 */
		public String getVersion() {
			return version;
		}

		/**
		 * Returns the OS arch.
		 * 
		 * @return the OS arch.
		 */
		public String getArch() {
			return arch;
		}

		public boolean isWindows() {
			return isWindows;
		}
	}

	/**
	 * JVM information
	 *
	 */
	public static class JVM {

		/**
		 * JVM memory information
		 *
		 */
		public static class Memory {

			private final long free;

			private final long total;

			private final long max;

			private Memory() {
				super();
				this.free = Runtime.getRuntime().freeMemory();
				this.total = Runtime.getRuntime().totalMemory();
				this.max = Runtime.getRuntime().maxMemory();
			}

			public long getFree() {
				return free;
			}

			public long getTotal() {
				return total;
			}

			public long getMax() {
				return max;
			}

		}

		private final String name;

		private final String runtime;

		private final String version;

		private final Memory memory;

		public JVM() {
			this.name = getSystemProperty("java.vm.name"); // ex : OpenJDK 64-Bit Server VM
			this.runtime = getSystemProperty("java.runtime.name"); // ex : OpenJDK Runtime Environment
			this.version = getSystemProperty("java.version"); // ex : 11
			this.memory = new Memory();
		}

		/**
		 * Returns the JVM name
		 * 
		 * @return the JVM name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the JVM version
		 * 
		 * @return the JVM version
		 */
		public String getVersion() {
			return version;
		}

		/**
		 * Returns the JVM runtime name.
		 * 
		 * @return the JVM runtime name.
		 */
		public String getRuntime() {
			return runtime;
		}

		public Memory getMemory() {
			return memory;
		}
	}

	/**
	 * Returns the OS information.
	 * 
	 * @return the OS information.
	 */
	public static OS getOS() {
		return os;
	}

	/**
	 * Returns the system property from the given key and "unknown" otherwise.
	 * 
	 * @param key the property system key
	 * @return the system property from the given key and "unknown" otherwise.
	 */
	private static String getSystemProperty(String key) {
		try {
			String property = System.getProperty(key);
			return StringUtils.isEmpty(property) ? UNKNOWN_VALUE : property;
		} catch (SecurityException e) {
			return UNKNOWN_VALUE;
		}
	}
}