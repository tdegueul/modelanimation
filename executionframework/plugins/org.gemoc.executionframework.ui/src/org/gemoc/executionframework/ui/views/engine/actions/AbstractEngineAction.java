package org.gemoc.executionframework.ui.views.engine.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPartSite;
import org.gemoc.executionframework.ui.Activator;
import org.gemoc.executionframework.ui.views.engine.IEngineSelectionListener;
import org.gemoc.gemoc_language_workbench.api.core.EngineStatus.RunStatus;
import org.gemoc.gemoc_language_workbench.api.core.ExecutionMode;
import org.gemoc.gemoc_language_workbench.api.core.IBasicExecutionEngine;

public abstract class AbstractEngineAction extends Action  implements IMenuCreator, IEngineSelectionListener{

	public AbstractEngineAction(){	
		super("fake", AS_PUSH_BUTTON);
		setMenuCreator(this);
		setEnabled(false);
		
		init();
		updateButton();
		
		Activator.getDefault().getEngineSelectionManager().addEngineSelectionListener(this);
	}
	public AbstractEngineAction(int style){	
		super("fake", style);
		setMenuCreator(this);
		setEnabled(false);
		
		init();
		updateButton();
		
		Activator.getDefault().getEngineSelectionManager().addEngineSelectionListener(this);
	}
	
	protected void init(){
		
	}
	protected void updateButton(){
		
	}

	@Override
	public void dispose() 
	{
		Activator.getDefault().getEngineSelectionManager().removeEngineSelectionListener(this);
	}
	
	protected void showMessage(IWorkbenchPartSite partSite, String message) {
		MessageDialog.openInformation(
			partSite.getShell(),
			"Gemoc Engines Status",
			message);
	}
	
	
	private IBasicExecutionEngine _currentSelectedEngine;
	public IBasicExecutionEngine getCurrentSelectedEngine(){
		return _currentSelectedEngine;
	}
	
	@Override
	public void engineSelectionChanged(IBasicExecutionEngine engine) 
	{
		_currentSelectedEngine = engine;
		
		if (_currentSelectedEngine == null)
		{
			setEnabled(false);			
		}
		else
		{
			setEnabled(
					!_currentSelectedEngine.getRunningStatus().equals(RunStatus.Stopped)
					&& _currentSelectedEngine.getExecutionContext().getExecutionMode().equals(ExecutionMode.Animation));
						
		}
	}
	
	@Override
	public Menu getMenu(Control parent) {
		return null;
	}

	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}
	
}
