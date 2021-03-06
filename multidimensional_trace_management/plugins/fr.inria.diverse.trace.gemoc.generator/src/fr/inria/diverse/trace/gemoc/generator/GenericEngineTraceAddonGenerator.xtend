package fr.inria.diverse.trace.gemoc.generator

import ecorext.Ecorext
import fr.inria.diverse.trace.commons.CodeGenUtil
import fr.inria.diverse.trace.commons.ManifestUtil
import fr.inria.diverse.trace.commons.PluginXMLHelper
import fr.inria.diverse.trace.plugin.generator.GenericTracePluginGenerator
import java.util.List
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.emf.ecore.EPackage
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.ui.PlatformUI
import org.eclipse.xtend.lib.annotations.Accessors
import org.jdom2.Element
import org.jdom2.filter.ElementFilter

class GenericEngineTraceAddonGenerator {

	// Inputs
	private val EPackage abstractSyntax // URI
	private val Ecorext executionEcorExt // URI
	private val String pluginName

	// Transient
	private var String packageQN
	private var String className
	private var String traceManagerClassName

	// Output
	@Accessors(PUBLIC_GETTER, PROTECTED_SETTER)
	IProject project

	new(org.eclipse.emf.ecore.EPackage abstractSyntax, ecorext.Ecorext executionEcorExt, String pluginName) {
		this.abstractSyntax = abstractSyntax
		this.executionEcorExt = executionEcorExt
		this.pluginName = pluginName
	}

	public def void generateCompleteAddon() {
		PlatformUI.workbench.activeWorkbenchWindow.run(false, true, [ m |
			generateCompleteAddon(m)
		])
	}

	public def void generateCompleteAddon(IProgressMonitor m) {

		// Generate trace plugin
		val GenericTracePluginGenerator a = new GenericTracePluginGenerator(abstractSyntax, executionEcorExt,
			pluginName, true)
		a.generate(m)

		// Retrieving some info from the plugin generation
		packageQN = a.packageQN
		className = a.languageName.replaceAll(" ", "").toFirstUpper + "EngineAddon"
		traceManagerClassName = a.traceManagerClassName

		// Generate trace engine addon class (same package as the trace manager)
		val String prettyCode = CodeGenUtil.formatJavaCode(generateAddonClassCode())
		val IPackageFragment fragment = a.packageFragment
		fragment.createCompilationUnit(className + ".java", prettyCode, true, m)

		// Add dependency to plugin containing AbstractTraceAddon
		ManifestUtil.addToPluginManifest(a.project, m, "fr.inria.diverse.trace.gemoc")
		ManifestUtil.addToPluginManifest(a.project, m, "org.gemoc.gemoc_language_workbench.api")
		ManifestUtil.addToPluginManifest(a.project, m, "org.gemoc.execution.engine.trace.model")
		ManifestUtil.addToPluginManifest(a.project, m, "org.gemoc.sequential_addons.multidimensional.timeline")
		ManifestUtil.addToPluginManifest(a.project, m, "fr.obeo.timeline")

		// Add extension point (taken from GemocLanguageDesignerBuilder)
		this.project = a.project
		val IFile pluginfile = project.getFile(PluginXMLHelper.PLUGIN_FILENAME);
		PluginXMLHelper.createEmptyTemplateFile(pluginfile, false);
		val PluginXMLHelper helper = new PluginXMLHelper();
		helper.loadDocument(pluginfile);
		val Element extensionPoint = helper.getOrCreateExtensionPoint(
			"org.gemoc.gemoc_language_workbench.engine_addon");
		updateDefinitionAttributeInExtensionPoint(extensionPoint, "Class", packageQN + "." + className);
		updateDefinitionAttributeInExtensionPoint(extensionPoint, "Default", "false");
		updateDefinitionAttributeInExtensionPoint(extensionPoint, "id", pluginName);
		updateDefinitionAttributeInExtensionPoint(extensionPoint, "Name", a.languageName + " Addon");
		helper.saveDocument(pluginfile);
	}

	private def static Element updateDefinitionAttributeInExtensionPoint(Element extensionPoint, String atributeName,
		String value) {
		var Element result;
		val String defName = "Addon"
		val List<Element> elements = extensionPoint.getContent(new ElementFilter(defName));
		if (elements.size() == 0) {

			// create extension point
			result = new Element(defName);
			extensionPoint.addContent(result);
		} else {
			result = elements.get(0);
		}
		result.setAttribute(atributeName, value);
		return result;
	}

	private def String generateAddonClassCode() {
		return '''package «packageQN»;

import fr.inria.diverse.trace.api.ITraceManager;
import fr.inria.diverse.trace.gemoc.traceaddon.AbstractTraceAddon;

import org.eclipse.emf.ecore.resource.Resource;

public class «className» extends AbstractTraceAddon {

	@Override
	public ITraceManager constructTraceManager(Resource exeModel, Resource traceResource) {
		return new «traceManagerClassName»(exeModel,traceResource);
	}

}'''
	}

}
