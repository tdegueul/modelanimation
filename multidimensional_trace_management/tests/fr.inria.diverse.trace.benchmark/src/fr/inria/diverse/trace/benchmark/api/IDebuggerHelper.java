package fr.inria.diverse.trace.benchmark.api;

import java.io.File;
import java.util.Collection;

import org.gemoc.gemoc_language_workbench.extensions.k3.PlainK3ExecutionEngine;

import fr.inria.diverse.trace.benchmark.Language;

public interface IDebuggerHelper {

	boolean canJump();

	void jump(int i);

	int getTraceSize();

	int getTraceMemoryFootprint(Language l, File dumpFolder, int traceSize)  throws Exception ;
	
	int computeTraceMemoryFootprint(Language l, File dumpFile, int traceSize) throws Exception;

	String getDebuggerName();

	Collection<? extends String> getAddons();

	void setExecutionEngine(PlainK3ExecutionEngine _executionEngine);

	void init();
	
	void unloadTraceResource();

	
}