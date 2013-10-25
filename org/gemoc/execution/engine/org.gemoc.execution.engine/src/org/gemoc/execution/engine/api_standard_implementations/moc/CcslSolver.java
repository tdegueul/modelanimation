package org.gemoc.execution.engine.api_standard_implementations.moc;

import java.io.IOException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gemoc.execution.engine.Activator;
import org.gemoc.gemoc_language_workbench.api.moc.Solver;

import fr.inria.aoste.timesquare.ccslkernel.model.TimeModel.Clock;
import fr.inria.aoste.timesquare.ccslkernel.modelunfolding.exception.UnfoldingException;
import fr.inria.aoste.timesquare.ccslkernel.solver.exception.SolverException;
import fr.inria.aoste.timesquare.ccslkernel.solver.launch.CCSLKernelSolverWrapper;
import fr.inria.aoste.timesquare.simulationpolicy.maxcardpolicy.MaxCardSimulationPolicy;
import fr.inria.aoste.timesquare.trace.util.HelperFactory;
import fr.inria.aoste.trace.EventOccurrence;
import fr.inria.aoste.trace.LogicalStep;
import fr.inria.aoste.trace.ModelElementReference;
import fr.inria.aoste.trace.Reference;

/**
 * The interface of the CCSLKernelSolver as seen by the Execution Engine.
 * 
 * @author flatombe
 * 
 */
public class CcslSolver implements Solver {

	CCSLKernelSolverWrapper solverWrapper = null;
	URI modelOfExecutionURI = null;

	public CcslSolver() {
	}

	@Override
	public void forbidEventOccurrenceReferencing(EObject target,
			EOperation operation) {
		// TODO: Julien complete the code so as to force the correct clocks.
		this.solverWrapper.forceClockAbsence(HelperFactory
				.createModelElementReference(target));
	}

	@Override
	public void forceEventOccurrenceReferencing(EObject target,
			EOperation operation) {
		// TODO: Julien complete the code so as to force the correct clocks.
		this.solverWrapper.forceClockPresence(HelperFactory
				.createModelElementReference(target));
	}

	@Override
	public LogicalStep getNextStep() {
		try {
			LogicalStep res = this.solverWrapper.getSolver()
					.doOneSimulationStep();

			for (EventOccurrence eventOccurrence : res.getEventOccurrences()) {
				Clock c = this.getClockLinkedToOccurrence(eventOccurrence);
				if (c != null) {
					EList<EObject> linkedObjects = c.getTickingEvent()
							.getReferencedObjectRefs();
					if (linkedObjects.size() == 2) {
						eventOccurrence.setContext(HelperFactory
								.createModelElementReference(linkedObjects
										.get(0)));
						eventOccurrence.setReferedElement(HelperFactory
								.createModelElementReference(linkedObjects
										.get(1)));
					}
				}
			}
			return res;
		} catch (SolverException e) {
			String errorMessage = "SolverException while trying to get next Ccsl step";
			Activator.getMessagingSystem().error(errorMessage,
					Activator.PLUGIN_ID);
			Activator.error(errorMessage, e);
			return null;
		}
	}

	private Clock getClockLinkedToOccurrence(EventOccurrence eventOcc) {
		Reference ref = eventOcc.getReferedElement();
		if (ref instanceof ModelElementReference) {
			ModelElementReference mer = (ModelElementReference) ref;
			EList<EObject> eobjects = mer.getElementRef();
			EObject actualObject = eobjects.get(eobjects.size() - 1);
			if (actualObject instanceof Clock) {
				// you got the clock that ticked
				return (Clock) actualObject;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public String toString() {
		return this.getClass().getName() + "@[modelOfExecutionURI="
				+ this.modelOfExecutionURI + "]";
	}

	@Override
	public void setModelOfExecutionFile(URI modelOfExecutionURI) {
		this.modelOfExecutionURI = modelOfExecutionURI;
		try {
			ResourceSet resourceSet = new ResourceSetImpl();
			Resource ccslResource = resourceSet.getResource(
					this.modelOfExecutionURI, true);
			ccslResource.load(null);
			EcoreUtil.resolveAll(resourceSet);
			this.solverWrapper = new CCSLKernelSolverWrapper();
			this.solverWrapper.getSolver().loadModel(ccslResource);
			this.solverWrapper.getSolver().initSimulation();
			this.solverWrapper.getSolver().setPolicy(
					new MaxCardSimulationPolicy());
		} catch (IOException e) {
			String errorMessage = "IOException while instantiating the CcslSolver";
			Activator.getMessagingSystem().error(errorMessage,
					Activator.PLUGIN_ID);
			Activator.error(errorMessage, e);
		} catch (UnfoldingException e) {
			String errorMessage = "UnfoldingException while instantiating the CcslSolver";
			Activator.getMessagingSystem().error(errorMessage,
					Activator.PLUGIN_ID);
			Activator.error(errorMessage, e);
		} catch (SolverException e) {
			String errorMessage = "SolverException while instantiating the CcslSolver";
			Activator.getMessagingSystem().error(errorMessage,
					Activator.PLUGIN_ID);
			Activator.error(errorMessage, e);
		}

	}

}