package org.gemoc.executionengine.java.sequential_modeling_workbench.ui.launcher.tabs;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.gemoc.commons.eclipse.emf.URIHelper;
import org.gemoc.commons.eclipse.ui.dialogs.SelectAnyIFileDialog;
import org.gemoc.execution.engine.ui.commons.RunConfiguration;
import org.gemoc.executionengine.java.api.extensions.languages.SequentialLanguageDefinitionExtension;
import org.gemoc.executionengine.java.api.extensions.languages.SequentialLanguageDefinitionExtensionPoint;
import org.gemoc.executionengine.java.engine.PlainK3ExecutionEngine;
import org.gemoc.executionengine.java.sequential_modeling_workbench.ui.Activator;
import org.gemoc.executionengine.java.sequential_xdsml.SequentialLanguageDefinition;
import org.gemoc.executionframework.ui.dialogs.SelectAIRDIFileDialog;
import org.gemoc.executionframework.ui.dialogs.SelectAnyConcreteEClassDialog;
import org.gemoc.executionframework.ui.dialogs.SelectAnyEObjectDialog;
import org.gemoc.executionframework.ui.dialogs.SelectMainMethodDialog;
import org.gemoc.executionframework.ui.utils.ENamedElementQualifiedNameLabelProvider;
import org.gemoc.gemoc_language_workbench.extensions.sirius.modelloader.DefaultModelLoader;
import org.osgi.framework.Bundle;

import fr.obeo.dsl.debug.ide.launch.AbstractDSLLaunchConfigurationDelegate;
import fr.obeo.dsl.debug.ide.sirius.ui.launch.AbstractDSLLaunchConfigurationDelegateUI;

public class LaunchConfigurationMainTab extends LaunchConfigurationTab {

	protected Composite _parent;

	protected Text _modelLocationText;
	protected Text _siriusRepresentationLocationText;
	protected Button _animateButton;
	protected Text _delayText;
	protected Combo _languageCombo;
	protected Text _melangeQueryText;
	protected Button _animationFirstBreak;

	protected Group _k3Area;
	protected Text _entryPointModelElementText;
	protected Text _entryPointMethodText;
	
	protected Combo _melangeCombo;
	protected Combo _modelTypeCombo;

	protected Text modelofexecutionglml_LocationText;

	public int GRID_DEFAULT_WIDTH = 200;
	
	@Override
	public void createControl(Composite parent) {
		_parent = parent;
		Composite area = new Composite(parent, SWT.NULL);
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		area.setLayout(gl);
		area.layout();
		setControl(area);

		Group modelArea = createGroup(area, "Model:");
		createModelLayout(modelArea, null);

		Group languageArea = createGroup(area, "Language:");
		createLanguageLayout(languageArea, null);

		Group debugArea = createGroup(area, "Animation:");
		createAnimationLayout(debugArea, null);

		_k3Area = createGroup(area, "Pure K3 execution:");
		createK3Layout(_k3Area, null);

	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RunConfiguration.LAUNCH_DELAY, 1000);
		configuration.setAttribute(RunConfiguration.LAUNCH_SELECTED_DECIDER,
				RunConfiguration.DECIDER_ASKUSER_STEP_BY_STEP);
		configuration.setAttribute(RunConfiguration.LAUNCH_MODEL_ENTRY_POINT, "");
		configuration.setAttribute(RunConfiguration.LAUNCH_METHOD_ENTRY_POINT, "");
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			RunConfiguration runConfiguration = new RunConfiguration(
					configuration);
			_modelLocationText.setText(URIHelper
					.removePlatformScheme(runConfiguration
							.getExecutedModelURI()));

			if (runConfiguration.getAnimatorURI() != null)
				_siriusRepresentationLocationText
						.setText(URIHelper
								.removePlatformScheme(runConfiguration
										.getAnimatorURI()));
			else
				_siriusRepresentationLocationText.setText("");
			_delayText.setText(Integer.toString(runConfiguration
					.getAnimationDelay()));
			_languageCombo.setText(runConfiguration.getLanguageName());
			_melangeQueryText.setText(runConfiguration.getMelangeQuery());
			_animationFirstBreak.setSelection(runConfiguration.getBreakStart());

			//TODO: find aspect target
			_entryPointModelElementText.setText(runConfiguration.getModelEntryPoint());
			_entryPointMethodText.setText(runConfiguration.getExecutionEntryPoint());

		} catch (CoreException e) {
			Activator.error(e.getMessage(), e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				AbstractDSLLaunchConfigurationDelegate.RESOURCE_URI,
				this._modelLocationText.getText());
		configuration.setAttribute(
				AbstractDSLLaunchConfigurationDelegateUI.SIRIUS_RESOURCE_URI,
				this._siriusRepresentationLocationText.getText());
		configuration.setAttribute(RunConfiguration.LAUNCH_DELAY,
				Integer.parseInt(_delayText.getText()));
		configuration.setAttribute(RunConfiguration.LAUNCH_SELECTED_LANGUAGE,
				this._languageCombo.getText());
		configuration.setAttribute(RunConfiguration.LAUNCH_MELANGE_QUERY,
				this._melangeQueryText.getText());
		configuration.setAttribute(RunConfiguration.LAUNCH_MODEL_ENTRY_POINT,
				_entryPointModelElementText.getText());
		configuration.setAttribute(RunConfiguration.LAUNCH_METHOD_ENTRY_POINT,
				_entryPointMethodText.getText());
		configuration.setAttribute(RunConfiguration.LAUNCH_BREAK_START,
				_animationFirstBreak.getSelection());
	}

	@Override
	public String getName() {
		return "Main";
	}

	// -----------------------------------

	/**
	 * Basic modify listener that can be reused if there is no more precise need
	 */
	private ModifyListener fBasicModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent arg0) {
			updateLaunchConfigurationDialog();
		}
	};

	// -----------------------------------

	/***
	 * Create the Field where user enters model to execute
	 * 
	 * @param parent
	 * @param font
	 * @return
	 */
	public Composite createModelLayout(Composite parent, Font font) {
		createTextLabelLayout(parent, "Model to execute");
		// Model location text
		_modelLocationText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		_modelLocationText.setLayoutData(createStandardLayout());
		_modelLocationText.setFont(font);
		_modelLocationText.addModifyListener(fBasicModifyListener);
		Button modelLocationButton = createPushButton(parent, "Browse", null);
		modelLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				// handleModelLocationButtonSelected();
				// TODO launch the appropriate selector

				SelectAnyIFileDialog dialog = new SelectAnyIFileDialog();
				if (dialog.open() == Dialog.OK) {
					String modelPath = ((IResource) dialog.getResult()[0])
							.getFullPath().toPortableString();
					_modelLocationText.setText(modelPath);
					updateLaunchConfigurationDialog();
				}
			}
		});
		return parent;
	}

	private Composite createAnimationLayout(Composite parent, Font font) {
		createTextLabelLayout(parent, "Animator");

		_siriusRepresentationLocationText = new Text(parent, SWT.SINGLE
				| SWT.BORDER);
		_siriusRepresentationLocationText.setLayoutData(createStandardLayout());
		_siriusRepresentationLocationText.setFont(font);
		_siriusRepresentationLocationText
				.addModifyListener(fBasicModifyListener);
		Button siriusRepresentationLocationButton = createPushButton(parent,
				"Browse", null);
		siriusRepresentationLocationButton
				.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						// handleModelLocationButtonSelected();
						// TODO launch the appropriate selector

						SelectAIRDIFileDialog dialog = new SelectAIRDIFileDialog();
						if (dialog.open() == Dialog.OK) {
							String modelPath = ((IResource) dialog.getResult()[0])
									.getFullPath().toPortableString();
							_siriusRepresentationLocationText
									.setText(modelPath);
							updateLaunchConfigurationDialog();
						}
					}
				});

		createTextLabelLayout(parent, "Delay");
		_delayText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		_delayText.setLayoutData(createStandardLayout());
		_delayText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		createTextLabelLayout(parent, "(in milliseconds)");

		new Label(parent, SWT.NONE).setText("");
		_animationFirstBreak = new Button(parent, SWT.CHECK);
		_animationFirstBreak.setText("Break at start");
		_animationFirstBreak.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				updateLaunchConfigurationDialog();
			}
		}

		);

		return parent;
	}

	private GridData createStandardLayout() {
		return new GridData(SWT.FILL, SWT.CENTER, true, false);
	}

	/***
	 * Create the Field where user enters the language used to execute
	 * 
	 * @param parent
	 * @param font
	 * @return
	 */
	public Composite createLanguageLayout(Composite parent, Font font) {
		createTextLabelLayout(parent, "xDSML");
		_languageCombo = new Combo(parent, SWT.NONE);
		_languageCombo.setLayoutData(createStandardLayout());

		ArrayList<String> xdsmlNames = new ArrayList<String>();
		IConfigurationElement[] confElements = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						SequentialLanguageDefinitionExtensionPoint.GEMOC_SEQUENTIAL_LANGUAGE_EXTENSION_POINT);
		for (int i = 0; i < confElements.length; i++) {
			xdsmlNames.add(confElements[i].getAttribute("name"));
		}
		if (confElements.length == 0) {
			xdsmlNames.add("<No xdml available>");
		}
		String[] empty = {};
		_languageCombo.setItems(xdsmlNames.toArray(empty));
		_languageCombo.addModifyListener(fBasicModifyListener);
		createTextLabelLayout(parent, "");

		// ********* Melange support ****
		// in a future version this should be extracted from the xdml itself
		createTextLabelLayout(parent, "Melange URI query");
		_melangeQueryText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		_melangeQueryText.setLayoutData(createStandardLayout());
		_melangeQueryText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		createTextLabelLayout(parent, "ex: ?mm=http://yourmetamodelextended");

		
		//Language
		createTextLabelLayout(parent, "Melange languages");
		_melangeCombo = new Combo(parent, SWT.NONE);
		_melangeCombo.setLayoutData(createStandardLayout());
		
		ArrayList<String> languagesNames = new ArrayList<String>();
		IConfigurationElement[] melangeLanguages = Platform.getExtensionRegistry().getConfigurationElementsFor("fr.inria.diverse.melange.language");
		for (IConfigurationElement lang : melangeLanguages) {
			languagesNames.add(lang.getAttribute("id"));
		}
		_melangeCombo.setItems(languagesNames.toArray(empty));
		_melangeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selection =_melangeCombo.getText();
				IConfigurationElement[] melangeLanguages = Platform.getExtensionRegistry().getConfigurationElementsFor("fr.inria.diverse.melange.language");
				List<String> modelTypeNames = new ArrayList<String>();
				for (IConfigurationElement lang : melangeLanguages) {
					if(lang.getAttribute("id").equals(selection)){
						IConfigurationElement[] adapters = lang.getChildren("adapter");
						for(IConfigurationElement adapter : adapters){
							modelTypeNames.add(adapter.getAttribute("modeltypeId"));
						}
					}
				}
				_modelTypeCombo.setItems(modelTypeNames.toArray(empty));
			}
		});
		createTextLabelLayout(parent, "");
		
		//ModelType
		createTextLabelLayout(parent, "Available ModelType");
		_modelTypeCombo = new Combo(parent, SWT.NONE);
		_modelTypeCombo.setLayoutData(createStandardLayout());
		

		return parent;
	}

	private Composite createK3Layout(Composite parent, Font font) {
		createTextLabelLayout(parent, "Main model element");
		_entryPointModelElementText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		_entryPointModelElementText.setLayoutData(createStandardLayout());
		_entryPointModelElementText.setFont(font);
		_entryPointModelElementText.setEditable(false);
		_entryPointModelElementText.addModifyListener(fBasicModifyListener);
		Button mainModelElemBrowseButton = createPushButton(parent, "Browse", null);
		mainModelElemBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Resource model = getModel();
				if(model != null){
					SelectAnyEObjectDialog dialog = new SelectAnyEObjectDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),model.getResourceSet(), new ENamedElementQualifiedNameLabelProvider());
					int res = dialog.open();
					if (res == WizardDialog.OK) {
						EObject selection = (EObject) dialog.getFirstResult();
						String uriFragment = selection.eResource().getURIFragment(selection);
						_entryPointModelElementText.setText(uriFragment);
					}
				}
			}
		});
		
		createTextLabelLayout(parent, "Main method");
		_entryPointMethodText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		_entryPointMethodText.setLayoutData(createStandardLayout());
		_entryPointMethodText.setFont(font);
		_entryPointMethodText.setEditable(false);
		_entryPointMethodText.addModifyListener(fBasicModifyListener);
		Button mainMethodBrowseButton = createPushButton(parent, "Browse", null);
		mainMethodBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				EObject modelElem = getModelElem();
				Set<Class> candidateAspects = getAllAspects();
				SelectMainMethodDialog dialog = new SelectMainMethodDialog(candidateAspects, modelElem, new ENamedElementQualifiedNameLabelProvider());
				int res = dialog.open();
				if (res == WizardDialog.OK) {
					Method selection = (Method) dialog.getFirstResult();
					_entryPointMethodText.setText(selection.toString());
				}
			}
		});
		
		return parent;
	}

	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();		
		_k3Area.setVisible(true);
	}
	
	// should have some cahce mecanism in order to avoid multiple load
	protected SequentialLanguageDefinition getLanguageDefinition(String xDSMLFilePath) {
		

		// Loading languagedef model
		ResourceSet rs = new ResourceSetImpl();
		URI uri = URI.createPlatformPluginURI(xDSMLFilePath, true);
		Resource res = rs.createResource(uri);
		try {
			res.load(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EcoreUtil.resolveAll(rs);// IMPORTANT

		if (res != null) {
			EObject first = res.getContents().get(0);

			// Follow-up in other operation...
			if (first instanceof SequentialLanguageDefinition) {
				return (SequentialLanguageDefinition) first;
			}
		}
		return null;
	}
	
	private Properties loadProperties(String bundleSymbolicName) {
		Bundle bundle = Platform.getBundle(bundleSymbolicName);	
		Properties properties = new Properties();
		String searchedPropertyFileName = "/META-INF/xtend-gen/" + bundleSymbolicName + ".k3_aspect_mapping.properties";
		InputStream inputStream = null;
		try {
			inputStream = bundle.getEntry(searchedPropertyFileName).openStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (inputStream != null) {
			try{
				properties.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		return properties;
	}
	
	/**
	 * Get aspects from DSA project.
	 */
	private Set<Class> getAllAspects(){
		Set<Class> res = new HashSet<Class>();

		try {
			String xdsmlPath = SequentialLanguageDefinitionExtensionPoint.findDefinition(_languageCombo.getText()).getXDSMLFilePath();
			SequentialLanguageDefinition xdsml = PlainK3ExecutionEngine.getLanguageDefinition(xdsmlPath);
			String dsaProjectName = xdsml.getDsaProject().getProjectName();
			
			
			Bundle bundle = Platform.getBundle(dsaProjectName);
			
			Properties properties = loadProperties(dsaProjectName);
			for(String key : properties.stringPropertyNames()){
				String value = properties.getProperty(key);
				String[] values = value.replaceAll(" ", "").split(",");
				for(String val : values){
					try {
						Class asp = bundle.loadClass(val);
						res.add(asp);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			// chut
		}
		
		return res;
	}
	
	private Resource getModel(){
		URI modelURI = URI.createPlatformResourceURI(_modelLocationText.getText(), true);
		return PlainK3ExecutionEngine.loadModel(modelURI);
	}
	
	private EObject getModelElem(){
		try{
			EObject caller = getModel().getEObject(_entryPointModelElementText.getText());
			return caller;
		}
		catch(Exception e){
			//chut
		}
		return null;
	}
}
