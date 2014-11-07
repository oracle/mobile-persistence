package oracle.ateam.sample.mobile.dt;

import java.net.URL;

import oracle.ide.Ide;
import oracle.ide.controller.Command;
import oracle.ide.model.Project;

import java.io.IOException;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;

import java.net.URL;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import oracle.ide.Ide;
import oracle.ide.controller.Command;
import oracle.ide.editor.EditorManager;
import oracle.ide.model.Element;
import oracle.ide.model.NodeFactory;
import oracle.ide.model.Project;
import oracle.ide.net.URLFactory;
import oracle.ide.net.URLFileSystem;
import oracle.ide.net.URLPath;

import oracle.javatools.buffer.ReadTextBuffer;
import oracle.javatools.parser.java.v2.JavaConstants;
import oracle.javatools.parser.java.v2.SourceFactory;
import oracle.javatools.parser.java.v2.internal.symbol.BlockSym;
import oracle.javatools.parser.java.v2.internal.symbol.ClassSym;
import oracle.javatools.parser.java.v2.internal.symbol.FieldDeclSym;
import oracle.javatools.parser.java.v2.internal.symbol.FieldSym;
import oracle.javatools.parser.java.v2.internal.symbol.FormalParameterSym;
import oracle.javatools.parser.java.v2.internal.symbol.FormalsSym;
import oracle.javatools.parser.java.v2.internal.symbol.ImportSym;
import oracle.javatools.parser.java.v2.internal.symbol.InterfacesSym;
import oracle.javatools.parser.java.v2.internal.symbol.MethodSym;
import oracle.javatools.parser.java.v2.internal.symbol.NameSym;
import oracle.javatools.parser.java.v2.internal.symbol.SuperclassSym;
import oracle.javatools.parser.java.v2.internal.symbol.Sym;
import oracle.javatools.parser.java.v2.internal.symbol.TypeSym;
import oracle.javatools.parser.java.v2.internal.symbol.ClassBodySym;
import oracle.javatools.parser.java.v2.model.JavaMethod;
import oracle.javatools.parser.java.v2.model.JavaType;
import oracle.javatools.parser.java.v2.model.SourceBlock;
import oracle.javatools.parser.java.v2.model.SourceClass;
import oracle.javatools.parser.java.v2.model.SourceClassBody;
import oracle.javatools.parser.java.v2.model.SourceElement;
import oracle.javatools.parser.java.v2.model.SourceFieldDeclaration;
import oracle.javatools.parser.java.v2.model.SourceFieldVariable;
import oracle.javatools.parser.java.v2.model.SourceFile;
import oracle.javatools.parser.java.v2.model.SourceFormalParameterList;
import oracle.javatools.parser.java.v2.model.SourceImport;
import oracle.javatools.parser.java.v2.model.SourceMethod;
import oracle.javatools.parser.java.v2.model.SourceTypeReference;
import oracle.javatools.parser.java.v2.model.doc.SourceDocComment;
import oracle.javatools.parser.java.v2.util.SourceVisitor;
import oracle.javatools.parser.java.v2.write.SourceTransaction;

import oracle.javatools.buffer.CharArrayReadTextBuffer;

import oracle.javatools.parser.java.v2.model.SourceMember;

import oracle.javatools.parser.java.v2.model.SourceTypeParameter;

import oracle.jdeveloper.java.JavaManager;
import oracle.jdeveloper.java.TransactionDescriptor;
import oracle.jdeveloper.model.JavaSourceNode;
import oracle.jdeveloper.model.PathsConfiguration;

/**
 * NO LONGER USED
 */
public class EntityClassModifier
  extends Command
{

  boolean noArgConstructorFixed = false;

  private JavaManager javaManager;
  private SourceFile file;
  private SourceFactory factory;

  public EntityClassModifier()
  {
    super(Ide.findCmdID("oracle.ateam.sample.mobile.EntityClassModifier"));
  }

  public EntityClassModifier(int i, int i1, String aap)
  {
    super(Ide.findCmdID("oracle.ateam.sample.mobile.EntityClassModifier"), NO_CHANGE);
  }

  public EntityClassModifier(int i, int i1)
  {
    super(Ide.findCmdID("oracle.ateam.sample.mobile.EntityClassModifier"), NO_CHANGE);
  }

  public EntityClassModifier(int i)
  {
    super(Ide.findCmdID("oracle.ateam.sample.mobile.EntityClassModifier"));
  }


  private void visitClassTree(List<SourceElement> elems, boolean printinfo, int depth)
  {
    for (SourceElement elem: elems)
    {
      if (printinfo)
      {
        String indent = "";
        for (int i = 0; i < depth; i++)
        {
          indent = indent + "   ";
        }

        //        System.err.println(indent+"Class: "+elem.getClass());
        //        System.err.println(indent+"Text: "+elem.getText());
      }
      callback(elem);
      visitClassTree(elem.getChildren(), printinfo, depth + 1);
    }
  }

  private void callback(SourceElement elem)
  {
    if (elem instanceof ImportSym)
    {
      ImportSym imp = (ImportSym) elem;
      if (imp.getName().equals("org.eclipse.persistence.indirection.ValueHolderInterface"))
      {
        fixValueHolderInterfaceImport(imp);
      }
    }

    else if (elem instanceof ClassSym)
    {
      addExtendsEntity((ClassSym) elem);
      fixConstructors((ClassSym) elem);
    }
    else if (elem instanceof FieldSym)
    {
      FieldSym field = (FieldSym) elem;
      if (field.getTypeName().equals("ValueHolderInterface"))
      {
        fixValueHolderInterfaceField(field);
      }
    }
  }

  @Override
  public int doit()
    throws Exception
  {
    Project prj = Ide.getActiveProject();
    URL classUrl = null;
    if (context.getElement() instanceof JavaSourceNode)
    {
      classUrl = ((JavaSourceNode) context.getElement()).getURL();
      javaManager = JavaManager.getJavaManager(prj);
      file = javaManager.getSourceFile(classUrl);
      List<SourceElement> elems = file.getChildren();

      final SourceTransaction st = javaManager.beginTransaction(file);
      factory = file.getFactory();

      visitClassTree(elems, true, 1);

      file.reformatSelf(SourceElement.REFORMAT_ALL);
      javaManager.commitTransaction(st, new TransactionDescriptor("Generate file"));

    }
    return 0;
  }

  private URL getSourceURL(Project project, String packageName, String className)
  {
    final URLPath srcPath = PathsConfiguration.getInstance(project).getSourcePath();

    // Get the first sourcepath entry.
    if (srcPath.size() > 0)
    {
      URL srcDir = srcPath.asList().get(0);
      URL dirURL = URLFactory.newDirURL(srcDir, packageName.replace('.', '/'));
      return URLFactory.newURL(dirURL, className + ".java");
    }
    return null;
  }


  private void changeMethod(MethodSym method)
  {
    //  visitClassTree(method.getChildren(), printinfo);
    // A transaction will be created.
    final SourceTransaction st = javaManager.beginTransaction(file);
    try
    {

      SourceFactory factory = file.getFactory();
      SourceMethod init =
        factory.createMethod(factory.createType(JavaConstants.PRIMITIVE_VOID), "setAapjesenzo", null, null,
                             // laatste arg is throw clause
          factory.createBlock("{aap=noot; noot=aap;}"));
      method.setModifiers(Modifier.PROTECTED);

      method.setBlock(factory.createBlock("{aap=noot; noot=aap;}"));

      //     method.removeSelf();
      //      method.replaceSelf(init);


      SourceDocComment comment = factory.createDocCommentFromText("/** An addin class, created by SDA" + "*/");
      comment.addSelf(init);

      file.reformatSelf(SourceElement.REFORMAT_ALL);

      // My work here is done.
      javaManager.commitTransaction(st, new TransactionDescriptor("Generate file"));
    }
    catch (Throwable e)
    {
      st.abort();
      e.printStackTrace();
      IOException ioe = new IOException("Unexpected exception: " + e.getMessage());
      ioe.initCause(e);
    }
  }

  private void addExtendsEntity(ClassSym classSym)
  {
    if (classSym.getSourceSuperclass() == null)
    {
      SourceTypeReference superClass = factory.createType("Entity");
      classSym.setSourceSuperclass(superClass);
      SourceImport importVh = factory.createImportDeclaration("oracle.ateam.sample.mobile.persistence.model.Entity");
      importVh.addSelfBefore(classSym);
    }
  }

  private void fixConstructors(ClassSym classSym)
  {
    List constructors = classSym.getSourceConstructors();
    for (int i = 0; i < constructors.size(); i++)
    {
      SourceMethod cons = (SourceMethod) constructors.get(i);
      // remove all constructors except for no-arg constructor
      if (cons.getFormalParameterList().getSourceParameters().size() > 0)
      {
        cons.removeSelf();
      }
      else if (!noArgConstructorFixed)
      {
        // for some reason we enter here twice, so use boolean flag, because secvond time getText
        // does not retrieve the whole body
        noArgConstructorFixed = true;
        updateJavaMethod(cons, "org.eclipse.persistence.indirection", "");

        //        SourceClass clazz =  factory.createClass("package view; public class Aap { }");
        //        SourceClass base = (SourceClass) classSym.getFile().getSourceElement() ("oracle.demo.hrcrud.mobile.model.Department");
        //        if (base==null)
        //        {
        //          System.err.println("EASY");
        //          base = classSym.getSourceClass("Department");
        //        }
        //        base.replaceSelf(clazz);

        //        String body = classSym.getBodySym().getBlockSym().getText();
        //        body = substitute(body, "this.manager", "this.managerHolder");
        //        System.err.println(body);
        //        SourceBlock sb2 = factory.createBlock("aap=noot;");
        //        classSym.getBodySym().getBlockSym().getBlock().removeSelf();
        //        sb2.addSelf(classSym.getBlockSym());

        //        cons.setBlock(sb);
        //    System.err.println("CLASS NAME: "+  classSym.getName());
        ////        SourceMethod newCons= factory.createMethod(cons.getSourceReturnType(), "Department", cons.getFormalParameterList(), cons.getThrowsClause(), sb);
        //        SourceMethod newCons= factory.createMethod(null, "Department", null, null, sb);
        //        newCons.addSelfBefore(cons);
        //        cons.removeSelf();
      }
    }

  }

  private void fixValueHolderInterfaceImport(ImportSym importSym)
  {
    importSym.getNameSym().setText("oracle.ateam.sample.mobile.persistence.indirection.ValueHolderInterface");

    // als add ValueHolder interface, because that one is not imported for some reason
    SourceImport importVh = factory.createImportDeclaration("oracle.ateam.sample.mobile.persistence.indirection.ValueHolder");
    importVh.addSelfAfter(importSym);
  }

  private void fixValueHolderInterfaceField(FieldSym fieldSym)
  {
    String oldName = fieldSym.getName();
    if (oldName.endsWith("Holder"))
    {
      // the just added field is also processed here, strange, just skip it
      return;
    }
    fieldSym.getNameSym().setValue(oldName + "Holder");
    // change all refrences to this.oldName to this

    // fix name in getter and setter methods, including holder getter/setter
    String methodVar = oldName.substring(0, 1).toUpperCase() + oldName.substring(1);

    String methodName = "get" + methodVar;
    JavaMethod m = findJavaMethod(fieldSym.getOwningClassSym(), methodName);
    JavaType returnType = m.getReturnType();
    updateJavaMethod(m, "this." + oldName, "this." + oldName + "Holder");

    methodName = "get" + methodVar + "Holder";
    m = findJavaMethod(fieldSym.getOwningClassSym(), methodName);
    updateJavaMethod(m, "this." + oldName, "this." + oldName + "Holder");

    methodName = "set" + methodVar;
    m = findJavaMethod(fieldSym.getOwningClassSym(), methodName);
    updateJavaMethod(m, "this." + oldName, "this." + oldName + "Holder");

    methodName = "set" + methodVar + "Holder";
    m = findJavaMethod(fieldSym.getOwningClassSym(), methodName);
    updateJavaMethod(m, "this." + oldName, "this." + oldName + "Holder");    
    // TODO this method must be made public as well!
    
    // and fix it in constructor
    SourceMethod cons = (SourceMethod) fieldSym.getOwningClassSym().getSourceConstructors().get(0);
    updateJavaMethod(cons, "this." + oldName, "this." + oldName + "Holder");
    
    // add dummy field with old name that is of actual type and marked as transient
    SourceTypeReference fieldType = factory.createType(returnType.getName());
    SourceFieldVariable createFieldVariable = factory.createFieldVariable(fieldType, oldName);
    createFieldVariable.addModifiers(Modifier.PRIVATE);
    createFieldVariable.addModifiers(Modifier.TRANSIENT);
    SourceFieldDeclaration fieldDeclType = factory.createFieldDeclaration(createFieldVariable);
    fieldDeclType.addSelfAfter(fieldSym.getParentSym());

  }

  private JavaMethod findJavaMethod(ClassSym classSym, String name)
  {
    Collection methods = classSym.getMethods(name);
    if (methods.size() > 0)
    {
      //take the first one, should be OK
      Object javaMethod;
      JavaMethod m = (JavaMethod) methods.iterator().next();
      return m;
    }
    return null;
  }

  /**
   * <p>
   * substitute returns a string in which 'find' is substituted by 'newString'
   *
   * @param in String to edit
   * @param find string to match
   * @param newString string to substitude for find
   * @return The edited string
   */
  private String substitute(String in, String find, String newString)
  {
    // when either of the strings are null, return the original string
    if (in == null || find == null || newString == null)
      return in;

    char[] working = in.toCharArray();
    StringBuffer stringBuffer = new StringBuffer();

    // when the find string could not be found, return the original string
    int startindex = in.indexOf(find);
    if (startindex < 0)
      return in;

    int currindex = 0;
    while (startindex > -1)
    {
      for (int i = currindex; i < startindex; i++)
      {
        stringBuffer.append(working[i]);
      } // for
      currindex = startindex;
      stringBuffer.append(newString);
      currindex += find.length();
      startindex = in.indexOf(find, currindex);
    } // while

    for (int i = currindex; i < working.length; i++)
    {
      stringBuffer.append(working[i]);
    } // for

    return stringBuffer.toString();
  } //substitute


  private void updateJavaMethod(JavaMethod javaMethod, String oldString, String newString)
  {
    if (javaMethod==null)
    {
      return;
    }
    SourceMethod sm = (SourceMethod) javaMethod.getSourceElement();
    updateJavaMethod(sm,oldString,newString);
  }

  private void updateJavaMethod(SourceMethod sm, String oldString, String newString)
  {
    if (sm==null)
    {
      return;
    }
    String text = sm.getBlock().getText();
    // we subs three type of occurrences of this string: suffixed with space, dot or semi-colon
    text = substitute(text, oldString+" ", newString+" ");
    if ("".equals(newString))
    {
      text = substitute(text, oldString+".", newString);      
    }
    else
    {
      text = substitute(text, oldString+".", newString+".");      
    }
    text = substitute(text, oldString+";", newString+";");
    SourceBlock sb = factory.createBlock(text);
    sm.getBlock().removeSelf();
    sb.addSelf(sm);
  }

}
