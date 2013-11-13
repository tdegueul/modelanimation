package org.gemoc.execution.engine.commons.dsa.sentinels;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EParameter;
import org.gemoc.gemoc_language_workbench.api.dsa.BytecodeSentinel;
import org.gemoc.gemoc_language_workbench.api.dsa.ModelSpecificAction;

public class JavaBytecodeSentinel implements BytecodeSentinel {

	@Override
	public Method getMethodFromAction(ModelSpecificAction msa) {
		List<EParameter> parameters = new ArrayList<EParameter>(
				msa.getParameters());
		Object target = (Object) msa.getTarget();
		String methodName = msa.getOperation().getName();

		Class<?>[] parameterTypes = null;
		ArrayList<Class<?>> parameterTypesList = new ArrayList<Class<?>>();
		if (parameters != null) {
			for (Object param : parameters) {
				parameterTypesList.add(param.getClass());
			}
		}
		try {
			return target.getClass().getMethod(methodName, parameterTypes);
		} catch (NoSuchMethodException e) {
			return null;
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

}
