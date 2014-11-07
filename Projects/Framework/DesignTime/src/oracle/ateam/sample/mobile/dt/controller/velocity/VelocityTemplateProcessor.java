package oracle.ateam.sample.mobile.dt.controller.velocity;


import java.io.StringWriter;


import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

public class VelocityTemplateProcessor
{
//  private static ADFLogger sLog = ADFLogger.createADFLogger(VelocityTemplateProcessor.class);
  private VelocityInitializer velocityInitializer;

  public VelocityTemplateProcessor(VelocityInitializer velocityInitializer)
  {
    this.velocityInitializer = velocityInitializer;
  }

  public String processTemplate(Object model, String templateName)
  {
    Template template;

    try
    {
      template = getTemplate(templateName);
      if (template == null)
      {
        throw new Exception("Could not find template " + templateName);
      }
      Context velocityContext = velocityInitializer.createVelocityContext(model);
      StringWriter stringWriter = new StringWriter();
      stringWriter = new StringWriter();
      template.merge(velocityContext, stringWriter);
      return stringWriter.toString();
    }
    catch (ResourceNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (ParseErrorException e)
    {
      e.printStackTrace();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Get a template from the cache; if template has not been loaded, load it
   *
   * @param templatePath
   * @return
   */
  private Template getTemplate(final String templatePath)
    throws ResourceNotFoundException, ParseErrorException, Exception
  {
    VelocityEngine ve = velocityInitializer.getVelocityEngine();
    return ve.getTemplate(templatePath);
  }
  
}
