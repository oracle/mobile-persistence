/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.util;

// Java imports
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * The Utils class holds utility methods of all kinds.
 * <p>
 */
public class StringUtils
{

  public static String getGetterMethodName(String attrName)
  {
    return "get" + initCap(attrName);
  }

  public static String getSetterMethodName(String attrName)
  {
    return "set" + initCap(attrName);
  }
  
  public static String initCap(String name)
  {
    return name.substring(0,1).toUpperCase()+name.substring(1);
  }

  public static String camelCaseToUpperCase(String in)
  {
    String regex = "([a-z])([A-Z])";
    String replacement = "$1_$2";
    String out = in.replaceAll(regex, replacement).toUpperCase();
    return out;
  }

  public static String startWithLowerCase(String in)
  {
    return in.substring(0, 1).toLowerCase() + in.substring(1);
  }

  /**
   * removes spaces and underscores and convert following character to uppercase
   * @param in
   * @return
   */
  public static String toCamelCase(String in)
  {
    // first replace spaces with delimiter
    String string = substitute(in, " ", "_");
    String[] parts = string.split("_");
    String camelCaseString = "";
    // we only camelcase when whole string is in uppercase, or string contains at least one underscoree
    // the string does contain
    if (string.toUpperCase().equals(string) || parts.length>1)
    {
      for (String part: parts)
      {
        camelCaseString = camelCaseString + toProperCase(part);
      }
      return camelCaseString;      
    }
    return in;
  }

  static String toProperCase(String s)
  {
    if (s.length()==0)
    {
      return s;
    }
    else if (s.length()==1)
    {
      return s.toUpperCase();
    }
    // if the remainder is all uppercase, we convert it to lowercase (happens with attrs
    // derived from column names), otherwise we leave the existing camel case
    if (s.substring(1).equals(s.substring(1).toUpperCase()))
    {
      return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();      
    }
    else
    {
      return s.substring(0, 1).toUpperCase() + s.substring(1);            
    }
  }

  public static String listToString(List<String> list, String delimiter)
  {
    StringBuffer strList = new StringBuffer("");
    boolean first = true;
    for (String s: list)
    {
      if (first)
      {
        first = false;
      }
      else
      {
        strList.append(",");
      }
      strList.append(s);
    }
    return strList.toString();
  }

  /**
   * <p>
   * stringToList splits a string into parts that are seperated by a given
   * delimiter into List elements
   *
   * @param string the string to be split, for example "abc,def,xyz"
   * @param delimiter The delimiting string.
   * @return a List with all the elements as String
   */
  public static List<String> stringToList(String string, String delimiter)
  {
    // TO DO: it is arguable that e.g. "one,,two" should return a List with
    // with "one" and "two" as elements, instead of "one", null, "two"

    if (string == null)
    {
      return null;
    }

    if (delimiter == null)
    {
      List<String> returnList = new ArrayList<String>();
      returnList.add(string);
      return returnList;
    }

    StringTokenizer tokenizer = new StringTokenizer(string, delimiter);
    List<String> elements = new ArrayList<String>();
    String element = null;
    while (tokenizer.hasMoreTokens())
    {
      element = ((String) tokenizer.nextToken()).trim();
      elements.add(element);
    }
    return elements;
  }


  /**
   * <p>
   * stringToList splits a string into parts that are seperated by a given
   * delimiter into String Array elements
   *
   * @param string the string to be split, for example "abc,def,xyz"
   * @param delimiter The delimiting string.
   * @return a String[] with all the seperate elements
   */
  public static String[] stringToStringArray(String string, String delimiter)
  {
    // TO DO: it is arguable that e.g. "one,,two" should return a List with
    // with "one" and "two" as elements, instead of "one", null, "two"

    if (string == null)
    {
      return null;
    }

    if (delimiter == null)
    {
      return new String[]
        { string };
    }

    StringTokenizer tokenizer = new StringTokenizer(string, delimiter);
    String[] elements = new String[tokenizer.countTokens()];
    for (int i = 0; tokenizer.hasMoreTokens(); i++)
    {
      elements[i] = ((String) tokenizer.nextToken()).trim();
    }
    return elements;
  }


  /**
   * <p>
   * stringToList splits a string into parts that are seperated by a given
   * delimiter into Integer Array elements
   *
   * @param string the string to be split, for example "7,11,13"
   * @param delimiter The delimiting string.
   * @return a Integer[] with all the seperate elements
   */
  public static Integer[] stringToIntegerArray(String string, String delimiter)
  {
    if (string == null)
    {
      return null;
    }

    if (delimiter == null)
    {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(string, delimiter);
    Integer[] elements = new Integer[tokenizer.countTokens()];
    String token;
    for (int i = 0; tokenizer.hasMoreTokens(); i++)
    {
      token = ((String) tokenizer.nextToken()).trim();
      try
      {
        elements[i] = Integer.valueOf(token);
      }
      catch (NumberFormatException ex)
      {
        throw new NumberFormatException("Invalid number " + token + "in StringToIntegerArray");
      }
    }
    return elements;
  }


  /**
   * <p>
   * Trims a string and if the end result is "", it returns null.
   * This method is especially useful if you run into the following
   * browser and/or servlet engine bug:
   * when a checkbox is changed from checked to unchecked, the returned
   * value is not null but " " (netscape) or "" (internet explorer).
   *
   * @param string the String to be trimmed
   * @return the trimmed String or null if the trimmed result is ""
   */
  public static String trimEmptyToNull(String string)
  {
    if (string == null || string.trim().equals(""))
    {
      return null;
    }
    else
    {
      return string;
    }
  } // end trimEmptyToNull


  /**
   * <p>
   * substitute returns a string in which 'find' is substituted by 'newString'
   *
   * @param in String to edit
   * @param find string to match
   * @param newString string to substitude for find
   * @return The edited string
   */
  public static String substitute(String in, String find, String newString)
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


  /**
   * <p>
   * function returns the part of the string after the last period
   *
   * @param fullClassName The full class name, including the package name
   * @return The class name (= part behind the last dot)
   */
  public static String lastPackageSegment(String fullClassName)
  {
    if (fullClassName == null)
      return null;
    int dotPos = fullClassName.lastIndexOf('.');
    return fullClassName.substring(dotPos + 1);
  } // lastPackageSegment

  public static void main(String[] args)
  {
    String[] array = StringUtils.stringToStringArray("aap, ,noot", ",");
    System.err.println(array.length);
  }

  /**
   * Return language code optionally concatenated with country code separated by underscore, example 'en_GB'.
   * @param locale
   * @return
   */
  public static String localeToString(Locale locale)
  {
    if (locale == null)
    {
      return "";
    }
    else
    {
      return locale.getLanguage().trim() +
        (locale.getCountry() == null || locale.getCountry().trim().length() == 0? "":
         "_" + locale.getCountry().trim());
    }
  }

  /**
   * Create a string array from a string separated by delim
   *
   * @param line the line to split
   * @param delim the delimter to split by
   * @return a string array of the split fields
   */
  public static String[] split(String line, String delim)
  {
    List list = new ArrayList();
    StringTokenizer t = new StringTokenizer(line, delim);
    while (t.hasMoreTokens())
    {
      list.add(t.nextToken());
    }
    return (String[]) list.toArray(new String[list.size()]);
  }

  public static void removeLeadingCharsIfSame(Map names)
  {
    boolean same = true;
    String firstChar = null;
    Iterator it = names.keySet().iterator();
    while (it.hasNext())
    {
      String key = (String) it.next();
      String value = (String) names.get(key);
      if (value==null || value.length()==1)
      {
        same = false;
        break;
      }
      else if (firstChar==null)
      {
        firstChar = value.substring(0,1);
      }
      else if (!value.startsWith(firstChar))
      {
        same = false;
        break;        
      }      
    }
    if (same)
    {
      // remove first char everywhere and do recursive call
      it = names.keySet().iterator();
      HashMap newNames = new HashMap();
      while (it.hasNext())
      {
        String key = (String) it.next();
        String value = (String) names.get(key);
        names.put(key, value.substring(1));              
      }
      removeLeadingCharsIfSame(names);
    }
  }
  public static void removeTrailingNumber(Map names)
  {
    boolean same = true;
    String lastChar = null;
    Iterator it = names.keySet().iterator();
    while (it.hasNext())
    {
      String key = (String) it.next();
      String value = (String) names.get(key);
      if (lastChar==null)
      {
        lastChar = value.substring(value.length()-1,value.length());
      }
      else if (!value.endsWith(lastChar))
      {
        same = false;
        break;        
      }      
    }
    if (same)
    {
      // remove last char everywhere and do recursive call
      it = names.keySet().iterator();
      while (it.hasNext())
      {
        String key = (String) it.next();
        String value = (String) names.get(key);
        names.put(key, value.substring(0,value.length()-1));              
      }
    }
  }
  
  public static boolean isEmpty(String value)
  {
    return value==null || value.trim().equals("");
  }

  public static boolean isNotEmpty(String value)
  {
    return !isEmpty(value);
  }
}
