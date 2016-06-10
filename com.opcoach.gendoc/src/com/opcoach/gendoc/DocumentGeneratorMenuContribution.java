package com.opcoach.gendoc;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * This class must fill the menu set on an EObject. It gets all generator and
 * check if the targetClass is compliant with selected class. If so, it add the
 * generator in the menu.
 * 
 * @author olivier
 *
 */
public class DocumentGeneratorMenuContribution extends ContributionItem
{

	public DocumentGeneratorMenuContribution()
	{
	}

	public DocumentGeneratorMenuContribution(String id)
	{
		super(id);
	}

	@Override
	public void fill(Menu menu, int index)
	{
		super.fill(menu, index);

		for (GeneratorDesc gd : OPCGendocActivator.getDocumentGeneratorRegistry().values())
			createMenuItem(menu, gd);
	}

	private void createMenuItem(Menu menu, final GeneratorDesc gd)
	{
		if (gd == null)
			return;

		final MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(gd.getName());
		/*
		 * Image image = getImage(descriptor); if (image != null) {
		 * menuItem.setImage(image);
		 * 
		 * }
		 */
		Listener listener = new Listener()
			{
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.
				 * swt.widgets.Event)
				 */
				@Override
				public void handleEvent(Event event)
				{
					switch (event.type)
					{
					case SWT.Selection:


						// This is a good handler can call the generate document
						// on current selected EObject
						ISelectionService ss = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();

						ISelection sel = ss.getSelection();
						if (sel instanceof IStructuredSelection)
						{
							Object selected = ((IStructuredSelection) sel).getFirstElement();
							if (selected instanceof EObject)
							{
								// Can get the path of current object and
								// initialize the gendoc
								// generator.
								GenerateDocumentHandler.generateDocument((EObject) selected, gd);
							}
						}

						break;
					default:
						break;
					}
				}
			};
		menuItem.addListener(SWT.Selection, listener);

	}
}
