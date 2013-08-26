/**
 * 
 */
package org.gemoc.execution.engine.actions;

import java.lang.reflect.Method;

import fr.inria.aoste.trace.Reference;

/**
 * DomainSpecificAction is what a DomainSpecificEvent references. Its
 * implementation should contain information so as to call a real method (like
 * EMF bytecode). A DomainSpecificAction is executed by an Executor.
 * 
 * @author flatombe
 * 
 */
public interface DomainSpecificAction {
    public Method getMethod();

    public Reference getTarget();
}