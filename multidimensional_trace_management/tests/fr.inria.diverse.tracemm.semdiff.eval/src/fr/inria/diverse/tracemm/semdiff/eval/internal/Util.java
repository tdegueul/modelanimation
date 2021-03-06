package fr.inria.diverse.tracemm.semdiff.eval.internal;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.modelexecution.xmof.vm.util.EMFUtil;

public abstract class Util {

	public static Resource loadResource(ResourceSet resourceSet, String filePath) {
		URI uri = null;
		if (filePath.contains("platform:/plugin/")) {
			uri = EMFUtil.createPlatformPluginURI(filePath.replaceAll(
					"platform:/plugin", ""));
		} else if (filePath.contains("platform:/resource/")) {
			uri = EMFUtil.createPlatformPluginURI(filePath.replaceAll(
					"platform:/resource", ""));
		} else {
			uri = EMFUtil.createFileURI(filePath);
		}
		return EMFUtil.loadResource(resourceSet, uri);
	}

}
