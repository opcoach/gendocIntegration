package com.opcoach.gendoc;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class GenerateDocumentHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		
		String genId = event.getParameter("com.opcoach.gendoc.generateDocument.generatorID");
		
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection)
		{
			Object selected = ((IStructuredSelection) sel).getFirstElement();
			if (selected instanceof EObject)
			{
				// Can get the path of current object and initialize the gendoc generator. 
				GeneratorDesc gd = OPCGendocActivator.getDocumentGeneratorRegistry().get(genId);
				System.out.println("Generating the document " + gd.getName() + " using template " + gd.getTemplateURL() + " for object Type " + gd.getTargetClassName());
			}
		}
		
		return null;
	}

}
