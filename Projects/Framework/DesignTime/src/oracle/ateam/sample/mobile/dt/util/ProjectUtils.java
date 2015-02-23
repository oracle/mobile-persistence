package oracle.ateam.sample.mobile.dt.util;

import java.util.Iterator;

import oracle.adfmf.common.util.McAppUtils;

import oracle.ide.Ide;
import oracle.ide.model.Element;
import oracle.ide.model.Project;

public class ProjectUtils
{

  /**
   * Returns the MAF ApplicationController project
   * @return
   */
  public static Project getApplicationControllerProject()
  {
    Project appControllerProject = McAppUtils.getApplicationControllerProject(Ide.getActiveWorkspace()
                                                                                      , null);    
    return appControllerProject;
  }

  /**
   * Returns first project in the application that is not the MAF ApplicationController project
   * @return
   */
  public static Project getViewControllerProject()
  {
    Project appControllerProject = getApplicationControllerProject();
    Iterator<Element> children = Ide.getActiveWorkspace().getChildren();
    Project vcProject = null;
    while (children.hasNext())
    {
      Element child = children.next();
      if (child instanceof Project && child!=appControllerProject)
      {
        vcProject = (Project) child;
        break;
      }
    }
    if (vcProject == null)
    {
      // should never happen, used as fallback for unknown scenariop and to preent NPE's
      vcProject = Ide.getActiveProject();
    }
    return vcProject;
  }
}
