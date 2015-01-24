/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import java.net.URL;

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import oracle.adfmf.common.util.McAppUtils;

import oracle.ide.model.Node;
import oracle.ide.model.NodeFactory;
import oracle.ide.model.Project;

import oracle.ide.model.TextNode;
import oracle.ide.net.URLFactory;
import oracle.ide.net.URLPath;

import oracle.javatools.buffer.TextBuffer;
import oracle.javatools.parser.java.v2.model.SourceElement;
import oracle.javatools.parser.java.v2.model.SourceFile;
import oracle.javatools.parser.java.v2.write.SourceTransaction;

import oracle.jdeveloper.java.JavaManager;
import oracle.jdeveloper.java.TransactionDescriptor;
import oracle.jdeveloper.model.JavaSourceNode;
import oracle.jdeveloper.model.PathsConfiguration;

public class FileUtils
{

  /**
   * Get URL for file stored under (subdir of) public_html dir
   * @param project
   * @param packageName
   * @param fileName
   * @return
   */
  public static URL getWebURL(Project project, String subdir, String fileName)
  {
    URL publicHtmlDir = McAppUtils.getProjectPublicHtmlDir(project);
    URL dirURL = URLFactory.newDirURL(publicHtmlDir, subdir);
    return URLFactory.newURL(dirURL, fileName);
  }

  public static URL getSourceURL(Project project, String packageName, String fileName)
  {
    final URLPath srcPath = PathsConfiguration.getInstance(project).getSourcePath();

    // Get the first sourcepath entry.
    if (srcPath.size() > 0)
    {
      URL srcDir = srcPath.asList().get(0);
      URL dirURL = URLFactory.newDirURL(srcDir, packageName.replace('.', '/'));
      return URLFactory.newURL(dirURL, fileName);
    }
    return null;
  }

  public static void formatJavaFile(Project project, URL sourceURL)
  {
    JavaSourceNode jsn = NodeFactory.findOrCreateOrFail(JavaSourceNode.class, sourceURL);
    JavaManager javaMgr = JavaManager.getJavaManager(project);
    SourceFile javaFile = javaMgr.getSourceFile(jsn.getURL());
    if (javaFile != null)
    {
      final SourceTransaction st = javaMgr.beginTransaction(javaFile);
      try
      {
        javaFile.reformatSelf(SourceElement.REFORMAT_ALL);
        // My work here is done.
        javaMgr.commitTransaction(st, new TransactionDescriptor("Reformat Java file"));
      }
      catch (Throwable e)
      {
        st.abort();
        e.printStackTrace();
      }
      // reformat
    }
  }
  
  public static boolean fileExists(URL sourceURL)
  {
    Node node = NodeFactory.find(sourceURL);
    return node!=null;
  }

  public static void addFileToProject(URL sourceURL, final String content, TextBuffer existingTextBuffer)
  {
    try
    {
      TextNode node = (TextNode) NodeFactory.findOrCreate(sourceURL);
      TextBuffer tb = existingTextBuffer;
      if (tb == null)
      {
        tb = node.acquireTextBuffer();
      }
      tb.beginEdit();
      tb.removeToEnd(0);
      tb.append(content.toCharArray());
      tb.endEdit();
      node.save();  
      node.releaseTextBuffer();
      node.markDirty(true);  
    }
    catch (IllegalAccessException e)
    {
    }
    catch (InstantiationException e)
    {
    }
    catch (IOException e)
    {
    }
  }

  public static String getNodeContent(TextNode node, TextBuffer tb)
  {
    Reader reader;
    try
    {
      reader = node.getReader();
      StringBuilder builder = new StringBuilder();
      int charsRead = -1;
      char[] chars = new char[100];
      do
      {
        charsRead = reader.read(chars, 0, chars.length);
        //if we have valid chars, append them to end of string.
        if (charsRead > 0)
          builder.append(chars, 0, charsRead);
      }
      while (charsRead > 0);
      String stringReadFromReader = builder.toString();
      return stringReadFromReader;
    }
    catch (IOException e)
    {
    }
    return "";
  }

  public static InputStream getInputStream(URL sourceURL)
  {
    InputStream is = null;
    try
    {
      is = sourceURL.openStream();
    }
    catch (Exception e)
    {
      // do nothing, file does not yet exist
    }    
    return is;
  }

  public static String getStringFromInputStream(InputStream is)
  {
    if (is==null)
    {
      return null;
    }
    BufferedReader br = null;
    StringBuilder sb = new StringBuilder();

    String line;
    try
    {

      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null)
      {
        sb.append(line);
        sb.append("\n");
      }

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (br != null)
      {
        try
        {
          br.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }

    return sb.toString();

  }
  
  public static void extractZipFile(String filePath, String targetDir)
  {
    try
    {
      ZipFile zipFile = new ZipFile(filePath);
      Enumeration entries = zipFile.entries();

      File unzipDir = new File(targetDir);
      if (!unzipDir.exists())
      {        
        unzipDir.mkdir();
      }
      while (entries.hasMoreElements())
      {
        ZipEntry entry = (ZipEntry) entries.nextElement();
        String name = entry.getName();
        if (!entry.isDirectory() && !name.startsWith("/") && !name.startsWith("_"))
        {
          String file = targetDir+"/"+name;
          copyInputStream(zipFile.getInputStream(entry),
                          new BufferedOutputStream(new FileOutputStream(file)));
          
        }
      }

      zipFile.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public static final void copyInputStream(InputStream in, OutputStream out)
  {
    try
    {
      byte[] buffer = new byte[1024];
      int len;

      while ((len = in.read(buffer)) >= 0)
        out.write(buffer, 0, len);

      in.close();
      out.close();
    }
    catch (Exception e)
    {
      // TODO: Add catch code
      e.printStackTrace();
    }
  }
 
}
