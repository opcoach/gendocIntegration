package com.opcoach.gendoc;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.widgets.Menu;

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
		// TODO Auto-generated constructor stub
	}

	public DocumentGeneratorMenuContribution(String id)
	{
		super(id);
		// TODO Auto-generated constructor stub

	}

	@Override
	public void fill(Menu menu, int index)
	{
		System.out.println("--->Enter in fill of contribution item");
		super.fill(menu, index);
	}

}
