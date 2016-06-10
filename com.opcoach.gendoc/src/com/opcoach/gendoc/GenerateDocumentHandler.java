package com.opcoach.gendoc;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.ui.dialogs.ResourceDialog;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gendoc.GendocProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.IProgressMonitorService;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.ModelNotFoundException;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.tags.handlers.IEMFModelLoaderService;
import org.eclipse.gendoc.tags.handlers.impl.context.EMFModelLoaderService;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
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
				
				GendocProcess gendocProcess = new GendocProcess();
				
				EMFModelLoaderService service = new EMFModelLoaderService(){
					@Override
					public EObject getModel(URI path) throws ModelNotFoundException {
						return source;
					}
				};
				service.setServiceId("IEMFModelLoaderService");
				GendocServices.getDefault().setService(IEMFModelLoaderService.class, service);
				

				IConfigurationService conf = GendocServices.getDefault().getService(IConfigurationService.class);
				conf.setOutput(getOutpuPath());
				URL templateUrl = gd.getTemplateURL();
				
				String resultFile = gendocProcess.runProcess(templateUrl);
				
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
		
		private String getOutpuPath() {
			String path = gd.getOutputPath();
			String outputPath = null;
			if ("ui".equals(path)) {
				outputPath = dialog ();
			}
			else if (path.startsWith(".")) {
				outputPath = relative(path);
			}
			else if (path.startsWith("/")) {
				outputPath = absolute(path);
			}
			return outputPath;
		}

		private String absolute(String path) {
			path = substitute(path);
			if (source.eResource().getURI().isPlatformResource()){
				IFile f = WorkspaceSynchronizer.getFile(source.eResource());
				IFile resultFile = f.getProject().getFile(new Path(path));
				resultFile.getParent().getLocation().toFile().mkdirs();
				return resultFile.getLocation().toFile().getAbsolutePath();
			}
			return null;
		}

		/**
		 * Returns an absolute file path corresponding to a project URI
		 * @param path
		 * @return
		 */
		private String getFileFromProjectPath(String path) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
			file.getParent().getLocation().toFile().mkdirs();
			return file.getLocation().toFile().getAbsolutePath();
		}

		private String substitute(String path) {
			path = path.replace("${resourceName}", getResourceName());
			return path;
		}

		private CharSequence getResourceName() {
			return source.eResource().getURI().trimFileExtension().lastSegment();
		}

		private String relative(String path) {
			path = substitute (path);
			URI sourceURI = source.eResource().getURI();
			URI result = URI.createURI(path).resolve(sourceURI);
			if (result.isFile()){
				return result.path();
			}
			else if (result.isPlatformResource()){
				return getFileFromProjectPath(result.toPlatformString(true));
			}
			return "";
		}

		private String dialog() {
			final StringBuffer buffer = new StringBuffer();
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					ResourceDialog d = new ResourceDialog(Display.getDefault().getActiveShell(), "Output template path", SWT.SAVE);
					if (d.open() == ResourceDialog.OK){
						URI uri = URI.createURI(d.getURIText());
						if (uri.isPlatformResource()){
							buffer.append(getFileFromProjectPath(uri.toPlatformString(true)));
						}
						else {
							buffer.append(uri.toFileString());
						}
					}
					
				}
			});
			return buffer.toString();
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


	
