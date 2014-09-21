package com.opcoach.gendoc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class OPCGendocActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.opcoach.gendoc"; //$NON-NLS-1$

	// The shared instance
	private static OPCGendocActivator plugin;
	
	private static Map<String, GeneratorDesc> documentGeneratorRegistry = new HashMap<String, GeneratorDesc>();
	
	/**
	 * The constructor
	 */
	public OPCGendocActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		readDocumentGenerators();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static OPCGendocActivator getDefault() {
		return plugin;
	}
	
	
	private void readDocumentGenerators()
	{
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		for (IConfigurationElement elt : reg.getConfigurationElementsFor("com.opcoach.gendoc.documentGenerator"))
		{
			Bundle b = Platform.getBundle(elt.getNamespaceIdentifier());
			
			GeneratorDesc gd = new GeneratorDesc();
			gd.setId(elt.getAttribute("id"));
			gd.setName(elt.getAttribute("name"));
			gd.setTargetClassName(elt.getAttribute("targetClass"));
			gd.setTemplateURL(b.getEntry(elt.getAttribute("template")));
			String docType = elt.getAttribute("documentType");
			if ("odt".equals(docType))
				gd.setDocType(DocumentType.ODT);
			else
				gd.setDocType(DocumentType.DOCX);
			
			documentGeneratorRegistry.put(gd.getId(), gd);

			
		}
		
	}

	public static Map<String, GeneratorDesc> getDocumentGeneratorRegistry()
	{
		return documentGeneratorRegistry;
	}


}
