package org.gemoc.execution.engine.ui;

import java.util.function.Supplier;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gemoc.execution.engine.debug.ui.semanticsopener.OpenSemanticsHandler;
import org.gemoc.gemoc_language_workbench.api.core.IBasicExecutionEngine;
//import org.gemoc.gemoc_language_workbench.extensions.sirius.services.AbstractGemocDebuggerServices;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.gemoc.execution.engine.ui"; //$NON-NLS-1$

	
	// The shared instance
	private static Activator plugin;
	
	private OpenSemanticsHandler handler;
	
	private Supplier<IBasicExecutionEngine> engineSupplier;
	
	private Supplier<String> bundleSymbolicNameSupplier;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	//	AbstractGemocDebuggerServices.LISTENER.uninstall();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static void warn(String msg, Throwable e){
		Activator.getDefault().getLog().log(new Status(Status.WARNING, PLUGIN_ID,
                Status.OK, 
                msg, 
                e));
	}
	public static void error(String msg, Throwable e){
		Activator.getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID,
                Status.OK, 
                msg, 
                e));
	}
	
	public void setHandler(OpenSemanticsHandler handler) {
		this.handler = handler;
		if(engineSupplier != null && bundleSymbolicNameSupplier != null) {
			this.handler.setEngine(engineSupplier.get());
			this.handler.setBundleSymbolicName(bundleSymbolicNameSupplier.get());
		}
	}
	
	public void setHandlerFieldSuppliers(Supplier<IBasicExecutionEngine> engineSupplier, Supplier<String> bundleSymbolicNameSupplier) {
		this.engineSupplier = engineSupplier;
		this.bundleSymbolicNameSupplier = bundleSymbolicNameSupplier;
	}

	public OpenSemanticsHandler getHandler() {
		return handler;
	}
	
}
