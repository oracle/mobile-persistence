/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 29-dec-2015   Steven Davelaar 
 1.2           added methods getCurrentDateTime, getCurrentTimeZone
 31-mar-2015   Steven Davelaar 
 1.2           If date parsing fails, try with english locale. API's like JCS have date formats
               like Thu Feb 12 ..., this fails when default locale is something else than english
 19-mar-2015   Steven Davelaar / Puja Subramanyam
 1.1           Fix in convertToDate to keep time component 
 01-jul-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.util;

import java.sql.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.Locale;

import oracle.adfmf.framework.exception.AdfException;

public class DateUtils
{
  
  private static Date convertToDate(String value, String format)
  {
    if (value==null || "".equals(value))
    {
      return null;
    }
    // a common date formnat returned by rest services is a date with time plus timezone
    // indicator. If the timezone is specified with a colon between the hour and minutes
    // standard JDK 1.4 date parsing will fail, so we remove the colon from the value in
    // this case to make the conversion work
    // for example:
    // Date: "2004-02-17T00:00:00+08:00";
    // Format = "yyyy-MM-dd'T'HH:mm:ssZ";
    // this conversion will fail, but succeeds when we change the value to
    // "2004-02-17T00:00:00+0800"
    int length = value.length();
    if (format.endsWith("Z") && length>6 && value.substring(length-3,length-2).equals(":"))
    {
      value = value.substring(0,length-3)+value.substring(length-2);
    }
    Date dateValue = null;
    // first try using default locale, then use english locale
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    try
    {
        dateValue = sdf.parse(value);
    }
    catch (ParseException e)
    {
        sdf = new SimpleDateFormat(format,Locale.ENGLISH);
        try
        {
            dateValue = sdf.parse(value);
        }
        catch (ParseException e2)
        {
          // do nothing, return null;
        }
    }
    return dateValue;
  }

  public static Date convertToDate(Class dateType, String value, String dateFormat, String dateTimeFormat)
  {
    // first try datetime pattern so we don't loose time component if present
    Date convertedValue = convertToDate(value, dateTimeFormat);
    if (convertedValue==null)
    {
      // try date format
      convertedValue = convertToDate(value, dateFormat);
    }
    if (convertedValue==null)
    {
      throw new AdfException("Cannot convert "+value+" to "+dateType.getName()+" using datetime format "+dateTimeFormat+" or date format "+dateFormat,AdfException.ERROR);      
    }
    if (dateType==Timestamp.class)
    {
      convertedValue = new Timestamp(convertedValue.getTime());
    }
    return convertedValue;
  }

  /**
   * Get current date/time as string using format yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
   * @return
   */
  public static String getCurrentDateTime()
  {
    //    String pattern = "yyyy-MM-dd HH:mm:ss Z";
    String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    String sdate = sdf.format(now);
    return sdate;
  }

  /**
   * Get current time zone
   * @return
   */
  public static String getCurrentTimeZone()
  {
    //        String pattern = "zzzz";
    String pattern = "Z";
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    String sdate = sdf.format(now);
    return sdate;
  }

}
