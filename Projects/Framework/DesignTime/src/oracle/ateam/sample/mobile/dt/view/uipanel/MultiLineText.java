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
