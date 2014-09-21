package com.opcoach.gendoc;

import java.net.URL;

public class GeneratorDesc
{
	private String id, name, targetClassName;
	private URL templateURL;

	private DocumentType docType;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getTargetClassName()
	{
		return targetClassName;
	}

	public void setTargetClassName(String targetClassName)
	{
		this.targetClassName = targetClassName;
	}

	public URL getTemplateURL()
	{
		return templateURL;
	}

	public void setTemplateURL(URL templateURL)
	{
		this.templateURL = templateURL;
	}

	public DocumentType getDocType()
	{
		return docType;
	}

	public void setDocType(DocumentType docType)
	{
		this.docType = docType;
	}

}
