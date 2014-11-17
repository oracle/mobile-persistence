/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uipanel;

import javax.swing.JTextArea;
import javax.swing.text.Document;

public class MultiLineText
  extends JTextArea
{
  public MultiLineText(String string)
  {
    super(string);
    setLineWrap(true);
    setWrapStyleWord(true);
    setEditable(false);
    setBorder(null);
  }

  public MultiLineText()
  {
    this(null);
  }
}
