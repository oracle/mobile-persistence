/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller;
import java.awt.Component;

import java.net.URL;

import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import oracle.ide.layout.ViewId;
import oracle.ide.log.DefaultLogPage;


public class GeneratorLogPage extends DefaultLogPage
{

  Icon mIcon;
  String mPageName;
  
  protected static final String MESSAGE_PAGE_ID = "GeneratorMessagePage";  

  private static String TOPNODE_LABEL = "A-Team Generator Messages";
  private static String INFO_LABEL = "Information";
  private static String ERROR_LABEL = "Errors";
  private static String WARNING_LABEL = "Warnings";
  private static String RUNWARNING_LABEL = "Runtime Warnings";

  static DefaultMutableTreeNode topNode = new DefaultMutableTreeNode(TOPNODE_LABEL);
  static DefaultMutableTreeNode errorsNode = new DefaultMutableTreeNode(ERROR_LABEL);
  static DefaultMutableTreeNode runwarningsNode = new DefaultMutableTreeNode(RUNWARNING_LABEL);
  static DefaultMutableTreeNode warningsNode = new DefaultMutableTreeNode(WARNING_LABEL);
  static DefaultMutableTreeNode informationNode = new DefaultMutableTreeNode(INFO_LABEL);

  static final TreePath errorsPath = new TreePath( new Object[] {topNode,errorsNode});
  static final TreePath runwarningsPath = new TreePath( new Object[] {topNode,runwarningsNode});
  static final TreePath warningsPath = new TreePath( new Object[] {topNode,warningsNode});
  static final TreePath informationPath = new TreePath( new Object[] {topNode,informationNode});

  private final static String TITLE = "A-Team Mobile Generator";
  // Implementing Singleton pattern so that only one instance of the JHeadstart
  // Log Window will be created.
  
  private GeneratorLogPage(String pageName, Icon icon)
  { 
    super(new ViewId(MESSAGE_PAGE_ID,pageName), icon, true);
    mIcon = icon;
    mPageName = pageName;
    getTree().setCellRenderer(new GeneratorLogPage.MyRenderer());
    getTree().setAutoscrolls(true);
  }

  static private GeneratorLogPage logWindow;

/*
 * BELOW ARE THE PUBLIC METHODS THAT WILL BE USED DURING REGULAR USAGE
 */

  /**
   * Clears and shows the Log Window.
   */
  public void initialize()
  {
    clearAll();
  }

  /**
   * Adds an error message, and creates the Errors node if necessary.
   */
  private void addError( String msg)
  {
    errorsNode.add(new DefaultMutableTreeNode(msg));
    if (errorsNode.getChildCount() == 1)
    {
      topNode.insert(errorsNode,0);
    }
    logTree(topNode);
  }

  /**
   * Adds an runtime warning message, and creates the Runtime Warnings node if necessary.
   */
  private void addRunWarning( String msg)
  {
    runwarningsNode.add(new DefaultMutableTreeNode(msg));
    if (runwarningsNode.getChildCount() == 1)
    {
      if (topNode.getIndex(errorsNode) == -1)
      {
        topNode.insert(runwarningsNode,0);
      }
      else
      {
        topNode.insert(runwarningsNode,1);
      }
    }
    logTree(topNode);
  }

  /**
   * If shouldLogWarnings is true, adds a warning message, and creates the
   * Warnings node if necessary. Otherwise it does nothing.
   */
  private void addWarning( String msg)
  {
    warningsNode.add(new DefaultMutableTreeNode(msg));
    if (warningsNode.getChildCount() == 1)
    {
      int pred = Math.max(topNode.getIndex(errorsNode),topNode.getIndex(runwarningsNode));
      topNode.insert(warningsNode,pred+1);
    }
    logTree(topNode);
  }

  /**
   * Adds an information message, and creates the Information node if necessary.
   */
  private void addInformation( String msg)
  {
    informationNode.add(new DefaultMutableTreeNode(msg));
    if (informationNode.getChildCount() == 1)
    {
      topNode.insert(informationNode,topNode.getChildCount());
    }    
    logTree(topNode);
  }


/*
 * METHODS BELOW THIS POINT WILL NOT BY NEEDED FOR REGULAR USAGE
 */


  public static GeneratorLogPage getPage (String title)
  { 
    if (logWindow==null || !logWindow.getTitleName().equals(title));
    {
      logWindow = new GeneratorLogPage(title,null);
    }
    return logWindow;
  }

  public String getName()
  {
    return mPageName;
  }

  public Icon   getIcon() {
    return mIcon;
  }


  public String getToolTip() {
    return TITLE;
  }

  public void giveInfo(String msg)
  { 
    logWindow.log(msg+"\n");
  }  

  protected void logTree (final TreeNode node)
  { 
    Runnable runner = 
      new Runnable()
      {
        public void run()
        {
          setModel(new DefaultTreeModel(node));
          getTree().expandPath(errorsPath);
          getTree().expandPath(runwarningsPath);
          getTree().expandPath(warningsPath);
          getTree().expandPath(informationPath);
          getTree().scrollRowToVisible(getTree().getRowCount()-1);
        }
      };

    if (!SwingUtilities.isEventDispatchThread())
    {
      SwingUtilities.invokeLater(runner);  
    }
    else
    {
      runner.run();
    }
  }

  public void scrollToTop()
  {
    requestShow();
    Runnable runner = 
      new Runnable()
      {
        public void run()
        {
          getTree().scrollRowToVisible(0);
        }
      };

    if (!SwingUtilities.isEventDispatchThread())
    {
      SwingUtilities.invokeLater(runner);  
    }
    else
    {
      runner.run();
    }    
  }

  public void clearAll()
  {
    topNode.removeAllChildren();
    informationNode.removeAllChildren();
    errorsNode.removeAllChildren();
    warningsNode.removeAllChildren();
    runwarningsNode.removeAllChildren();
    super.clearAll();
    logTree(topNode);
    requestShow();
  }

  public void fatal(String msg)
  {
    addError(msg);
  }

  public void error(String msg)
  {
    addError(msg);
  }

  public void warn(String msg)
  {
    addWarning(msg);
  }

  public void info(String msg)
  {
    addInformation(msg);
  }

  private class MyRenderer extends DefaultTreeCellRenderer
  {
    ImageIcon errorImage;
    ImageIcon infoImage;
    ImageIcon warningImage;
    ImageIcon runwarningImage;

    public MyRenderer() {
      URL iconURL = GeneratorLogPage.class.getClassLoader().getResource("red.gif");     
      if (iconURL != null) errorImage = new ImageIcon(iconURL);

      iconURL = GeneratorLogPage.class.getClassLoader().getResource("green.gif");     
      if (iconURL != null) infoImage = new ImageIcon(iconURL);

      iconURL = GeneratorLogPage.class.getClassLoader().getResource("yellow.gif");     
      if (iconURL != null) warningImage = new ImageIcon(iconURL);

      iconURL = GeneratorLogPage.class.getClassLoader().getResource("orange.gif");     
      if (iconURL != null) runwarningImage = new ImageIcon(iconURL);
    }

    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus)
    {
      super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);

      DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)((DefaultMutableTreeNode)value).getParent();
      if (parentNode == null) return this;
      if (parentNode.getUserObject().equals(ERROR_LABEL)) setIcon(errorImage);
      if (parentNode.getUserObject().equals(INFO_LABEL)) setIcon(infoImage);
      if (parentNode.getUserObject().equals(RUNWARNING_LABEL)) setIcon(runwarningImage);
      if (parentNode.getUserObject().equals(WARNING_LABEL)) setIcon(warningImage);
      return this;
    }
  }// myRenderer

}

