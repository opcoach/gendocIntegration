package com.opcoach.gendoc;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.GendocProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.IProgressMonitorService;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
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
				// Can get the path of current object and initialize the gendoc
				// generator.
				GeneratorDesc gd = OPCGendocActivator.getDocumentGeneratorRegistry().get(genId);
				generateDocument((EObject)selected, gd);
			}
		}

		return null;
	}
	
	public static void generateDocument(EObject source, GeneratorDesc gd)
	{
		System.out.println("Generating the document '" + gd.getName() + "' using template '" + gd.getTemplateURL()
				+ "' for object Type '" + gd.getTargetClassName() + "'");
		
		try {
		       IRunnableWithProgress op = new GendocRunnable(source, gd);
		       new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, true, op);
		    } catch (InvocationTargetException e) {
		       // handle exception
		    } catch (InterruptedException e) {
		       // handle cancelation
		    }
	}
}
	

	class GendocRunnable implements IRunnableWithProgress
	{
		private EObject source;
		private GeneratorDesc gd;
		
		public GendocRunnable(EObject source, GeneratorDesc gd)
		{
			this.source = source;
			this.gd = gd;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
		{
			OPCGendocActivator activator = OPCGendocActivator.getDefault();

			IGendocDiagnostician diagnostician = GendocServices.getDefault().getService(IGendocDiagnostician.class);
			diagnostician.init();
			IProgressMonitorService monitorService = (IProgressMonitorService) GendocServices.getDefault().getService(IProgressMonitorService.class);
			monitorService.setMonitor(monitor);
			
			try
			{
			/*	IConfigurationService parameter = GendocServices.getDefault().getService(IConfigurationService.class);
				parameter.addParameter(replacePercentBySpace(page.getSelected().getOutputKey(), 3), replacePercentBySpace(page.getFullOutputPath(), 3));
				parameter.addParameter(replacePercentBySpace(page.getSelected().getModelKey(), 3), replacePercentBySpace(page.getModel(), 3));
				for(AdditionnalParameterItem item : page.getAdditionnalParameters()) {
					parameter.addParameter(replacePercentBySpace(item.getParamName(),3), replacePercentBySpace(item.getValue(),3));
				}
				*/
				
				GendocProcess gendocProcess = new GendocProcess();
				URL templateUrl = gd.getTemplateURL();
				URL fUrl = FileLocator.toFileURL(templateUrl);
				File f = new File(fUrl.toURI());
				String resultFile = gendocProcess.runProcess(f);
				
				handleDiagnostic(diagnostician.getResultDiagnostic(), "The file has been generated but contains errors :\n", resultFile);
			}
			catch (GenDocException e)
			{
				activator.getLog().log(new Status(IStatus.ERROR, OPCGendocActivator.PLUGIN_ID, e.getUIMessage(), e));
				diagnostician.addDiagnostic(new BasicDiagnostic(Diagnostic.ERROR, OPCGendocActivator.PLUGIN_ID, 0, e.getUIMessage(), null));
				handleDiagnostic(diagnostician.getResultDiagnostic(), "An error occured during generation.", null);
			}
			catch (Throwable t)
			{
				activator.getLog().log(new Status(IStatus.ERROR, OPCGendocActivator.PLUGIN_ID, t.getMessage(), t));
				diagnostician.addDiagnostic(new BasicDiagnostic(Diagnostic.ERROR, OPCGendocActivator.PLUGIN_ID, 0, t.getMessage(), t.getStackTrace()));
				handleDiagnostic(diagnostician.getResultDiagnostic(), "An unexpected error occured during the generation.", null);
			}
			finally
			{
				GendocServices.getDefault().clear();
			}
		}
		
		/**
		 * Handle diagnostic.
		 * 
		 * @param resultDiagnostic the result diagnostic
		 */
		private void handleDiagnostic(final Diagnostic resultDiagnostic, final String message, final String resultFilePath)
		{
			if (resultDiagnostic.getSeverity() == Diagnostic.OK)
			{
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Document generator", "The document has been generated successfully: \n"
								+ resultFilePath);
					}
				});
			}
			else
			{
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Document generator", "Error generating documentation.\n", BasicDiagnostic.toIStatus(resultDiagnostic));
					}
				});
			}
	}
	}


	
