package org.gemoc.executionengine.java.sequential_modeling_workbench.ui.debug

import fr.inria.diverse.trace.gemoc.traceaddon.WrapperSimpleTimeLine
import fr.inria.diverse.trace.api.ITraceManager
import fr.inria.diverse.trace.api.IValueTrace

class WrapperOmniscientDebugTimeLine extends WrapperSimpleTimeLine {

	OmniscientGenericSequentialModelDebugger debugger

	new(ITraceManager manager) {
		super(manager)
	}
	
	new (OmniscientGenericSequentialModelDebugger debugger) {
		super (null)
		this.debugger = debugger
	}

	override getSelectedPossibleStep(int branch, int index) {

		if (branch == 0) {
			if (debugger.getCurrentStateIndex() == index) {
				return -1;
			} else {
				return 0;
			}

		} else {
			val IValueTrace trace = getAllValueTraces().get(branch - 1);
			val int traceCurrentIndex = trace.getCurrentIndex(debugger.currentStateIndex);
			if (traceCurrentIndex == index) {
				return -1;
			} else {
				return 0;

			}
		}

	}

}