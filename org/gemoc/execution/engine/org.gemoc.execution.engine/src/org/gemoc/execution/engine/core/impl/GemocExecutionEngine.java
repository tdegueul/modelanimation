package org.gemoc.execution.engine.core.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.resource.Resource;
import org.gemoc.execution.engine.Activator;
import org.gemoc.execution.engine.api_implementations.dsa.EmfAction;
import org.gemoc.execution.engine.api_implementations.dsa.EmfBytecodeNameResolver;
import org.gemoc.execution.engine.api_implementations.dse.EclEvent;
import org.gemoc.execution.engine.core.BasicExecutionEngine;
import org.gemoc.gemoc_language_workbench.api.dsa.Executor;
import org.gemoc.gemoc_language_workbench.api.dse.DomainSpecificEvent;
import org.gemoc.gemoc_language_workbench.api.feedback.FeedbackPolicy;
import org.gemoc.gemoc_language_workbench.api.moc.ModelOfExecutionBuilder;
import org.gemoc.gemoc_language_workbench.api.moc.Solver;
import org.gemoc.gemoc_language_workbench.api.utils.LanguageInitializer;
import org.gemoc.gemoc_language_workbench.api.utils.ModelLoader;

import fr.inria.aoste.trace.EventOccurrence;
import fr.inria.aoste.trace.FiredStateKind;
import fr.inria.aoste.trace.LogicalStep;
import fr.inria.aoste.trace.ModelElementReference;
import fr.inria.aoste.trace.NamedReference;
import fr.inria.aoste.trace.Reference;

public class GemocExecutionEngine extends BasicExecutionEngine {

	public GemocExecutionEngine(LanguageInitializer languageInitializer, ModelLoader modelLoader,
			Resource domainSpecificEventsResource, ModelOfExecutionBuilder modelOfExecutionBuilder, Solver solver,
			Executor executor, FeedbackPolicy feedbackPolicy) throws CoreException {
		super(languageInitializer, modelLoader, domainSpecificEventsResource, modelOfExecutionBuilder, solver,
				executor, feedbackPolicy);
		Activator.getMessagingSystem().info("*** Engine construction done. ***", Activator.PLUGIN_ID);
	}

	@Override
	public void initialize(String modelURI) {
		Activator.getMessagingSystem().info("Initializing GemocExecutionEngine with...", Activator.PLUGIN_ID);
		Activator.getMessagingSystem().info("\tmodelURI: " + modelURI, Activator.PLUGIN_ID);

		this.modelResource = this.modelLoader.loadModel(modelURI);
		this.executor.setModel(this.modelResource);
		Activator.getMessagingSystem().info("Model was successfully loaded: " + modelResource.toString(),
				Activator.PLUGIN_ID);

		// TODO: do something with the DSE file and the model.
		// Programatically generate the .extendedCCSL.
		Resource modelOfExecution = this.modelOfExecutionBuilder.build(this.domainSpecificEventsResource,
				this.modelResource);

		String modelOfExecutionFilePath = "/org.gemoc.execution.engine.example/model/TrafficControl_MoCC_new.extendedCCSL";
		URI modelOfExecutionURI = URI.createPlatformResourceURI(modelOfExecutionFilePath, true);
		// URI modelOfExecutionURI = modelOfExecution.getURI();

		this.solver.setModelOfExecutionFile(modelOfExecutionURI);

		Activator.getMessagingSystem().info("*** Engine initialization done. ***", Activator.PLUGIN_ID);
	}

	@Override
	protected List<DomainSpecificEvent> match(LogicalStep step) {
		Activator.getMessagingSystem().debug(
				"Matching the given step : " + step.toString() + " containing: \n"
						+ step.getEventOccurrences().toString(), Activator.PLUGIN_ID);
		List<DomainSpecificEvent> res = new ArrayList<DomainSpecificEvent>();
		for (EventOccurrence eventOccurrence : step.getEventOccurrences()) {
			if (eventOccurrence.getFState() == FiredStateKind.TICK) {
				Activator.getMessagingSystem().debug("FState is TICK for eventOccurrence: " + eventOccurrence,
						Activator.PLUGIN_ID);
				if (eventOccurrence.getContext() != null & eventOccurrence.getReferedElement() != null) {
					EObject target = this.getEObjectFromReference(eventOccurrence.getContext());
					EOperation operation = (EOperation) this.getEObjectFromReference(eventOccurrence
							.getReferedElement());

					res.add(new EclEvent(new EmfAction(target, operation)));
				}
			}
		}
		return res;
	}

	private EObject getEObjectFromReference(Reference reference) {
		Activator.getMessagingSystem().debug(
				"Trying to retrieve the EObject from the Reference : " + reference.toString(), Activator.PLUGIN_ID);

		EList<EObject> elements = ((ModelElementReference) reference).getElementRef();
		if (reference instanceof ModelElementReference) {
			// Returns EObject thanks to the list of EObjects
			Activator.getMessagingSystem()
					.debug("Returning :" + elements.get(elements.size() - 1), Activator.PLUGIN_ID);
			return elements.get(elements.size() - 1);
		} else if (reference instanceof NamedReference) {
			// Returns EObject thanks to its qualified name
			try {
				EObject res = new EmfBytecodeNameResolver(modelResource)
						.getEObjectFromQualifiedName((((NamedReference) reference).getValue()));
				Activator.getMessagingSystem().debug("Returning :" + res, Activator.PLUGIN_ID);
				return res;
			} catch (SecurityException e) {
				String errorMessage = e.getClass().getSimpleName()
						+ " when trying to retrieve an EObject from the model from a NamedReference";
				Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
				Activator.error(errorMessage, e);
				return null;
			} catch (IllegalArgumentException e) {
				String errorMessage = e.getClass().getSimpleName()
						+ " when trying to retrieve an EObject from the model from a NamedReference";
				Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
				Activator.error(errorMessage, e);
				return null;
			} catch (NoSuchMethodException e) {
				String errorMessage = e.getClass().getSimpleName()
						+ " when trying to retrieve an EObject from the model from a NamedReference";
				Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
				Activator.error(errorMessage, e);
				return null;
			} catch (IllegalAccessException e) {
				String errorMessage = e.getClass().getSimpleName()
						+ " when trying to retrieve an EObject from the model from a NamedReference";
				Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
				Activator.error(errorMessage, e);
				return null;
			} catch (InvocationTargetException e) {
				String errorMessage = e.getClass().getSimpleName()
						+ " when trying to retrieve an EObject from the model from a NamedReference";
				Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
				Activator.error(errorMessage, e);
				return null;
			}

			// Iterator<EObject> modelIterator =
			// this.modelResource.getContents().get(0).eAllContents();
			// while (modelIterator.hasNext()) {
			// EObject eo = modelIterator.next();
			// if (this.getNameOfEObject(eo).equals(elements.get(elements.size()
			// - 1))) {
			// return eo;
			// }
			// }
		} else {
			throw new RuntimeException(
					"Context reference is neither a ModelElementReference nor a NamedElementReference");
		}
	}

	private String getNameOfEObject(EObject eo) {
		return this.getQualifiedName(eo);
	}

	private String getQualifiedName(EObject eo) {
		String res = getSimpleName(eo);
		EObject tmp = eo.eContainer();
		while (tmp != null) {
			res = getSimpleName(tmp) + "::" + res;
			tmp = tmp.eContainer();
		}
		return res;
	}

	private String getSimpleName(EObject eo) {
		return this.invokeGetNameOnEObject(eo);
	}

	private String invokeGetNameOnEObject(EObject eObjectMethod) {
		Method method;
		try {
			method = eObjectMethod.getClass().getMethod("getName");
			Object res = method.invoke(eObjectMethod);
			if (res instanceof String) {
				return (String) res;
			} else {
				return null;
			}
		} catch (SecurityException e) {
			String errorMessage = "SecurityException when trying to get the qualified name of an object";
			Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
			Activator.error(errorMessage, e);
			return null;
		} catch (IllegalArgumentException e) {
			String errorMessage = "IllegalArgumentException when trying to get the qualified name of an object";
			Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
			Activator.error(errorMessage, e);
			return null;
		} catch (NoSuchMethodException e) {
			String errorMessage = "NoSuchMethodException when trying to get the qualified name of an object";
			Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
			Activator.error(errorMessage, e);
			return null;
		} catch (IllegalAccessException e) {
			String errorMessage = "IllegalAccessException when trying to get the qualified name of an object";
			Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
			Activator.error(errorMessage, e);
			return null;
		} catch (InvocationTargetException e) {
			String errorMessage = "InvocationTargetException when trying to get the qualified name of an object";
			Activator.getMessagingSystem().error(errorMessage, Activator.PLUGIN_ID);
			Activator.error(errorMessage, e);
			return null;
		}
	}

}
