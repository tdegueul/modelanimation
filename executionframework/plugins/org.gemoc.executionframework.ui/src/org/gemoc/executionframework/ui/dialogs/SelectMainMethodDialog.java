package org.gemoc.executionframework.ui.dialogs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import fr.inria.diverse.k3.al.annotationprocessor.Aspect;


public class SelectMainMethodDialog extends ElementListSelectionDialog  {

	private Set<Class> _aspects;

	public SelectMainMethodDialog(Set<Class> aspects, EObject modelElem, ILabelProvider renderer) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), renderer);
		_aspects = aspects;
		
		if(modelElem != null && aspects != null)
			update(modelElem);
	}

	/**
	 * Display only methods with @main from Aspects applied on modelElem
	 */
	public void update(EObject modelElem){
		Class target = modelElem.eClass().getClass();
		List<Method> selection = new ArrayList<Method>();
		
		for (Class asp : _aspects)
		{
			if(getTarget(asp) == target)
			{
				for(Method m : asp.getMethods())
				{
					if(isAnnotedMain(m))
					{
						selection.add(m);
					}
				}
			}
		}
		
		this.setElements(selection.toArray());
	}

	/**
	 * Return true is their is @Main
	 */
	private boolean isAnnotedMain(Method m){
		return m.isAnnotationPresent(fr.inria.diverse.k3.al.annotationprocessor.Main.class);
	}

	/**
	 * Return the targeted class from the @Aspect
	 */
	private Class getTarget(Class aspect){
		
		Annotation annotation = aspect.getAnnotation(fr.inria.diverse.k3.al.annotationprocessor.Aspect.class);
		if(annotation != null){
				Aspect k3tag = (Aspect) annotation;
				return k3tag.className();
		}
		
		return null;
	}

}

