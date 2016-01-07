package oracle.ateam.sample.mobile.dt.util;

import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;

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

  /**
   * Returns map of project names and projects in current application
   * @return
   */
  public static Map<String,Project> getProjects()
  {
    Map<String,Project> projects = new HashMap<String,Project>();
    Iterator<Element> children = Ide.getActiveWorkspace().getChildren();
    while (children.hasNext())
    {
      Element child = children.next();
      if (child instanceof Project)
      {
        String name = child.getShortLabel();
        // strip off .jpr suffix
        projects.put(name.substring(0,name.length()-4), (Project) child);
      }
    }
    return projects;
  }

}
