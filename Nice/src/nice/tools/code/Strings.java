/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : Strings.java
// Created : Fri Dec 10 19:03:56 1999 by bonniot
//$Modified: Mon Aug 07 15:42:05 2000 by Daniel Bonniot $

package nice.tools.code;

import bossa.util.*;

/**
   String escaping.
   
   @author bonniot
 */

public abstract class Strings
{
  /**
     Encodes a string to make it a valid java identifier.

     Any offending character is replaced by '$$' followed by its
     (uni)code in Hexadecimal.
  */
  public static String escape(String s)
  {
    if(s==null || s.length()==0)
      return null;
    if(s.equals("<init>") || s.equals("<clinit>"))
      return s;
    
    char[] chars = s.toCharArray();
    StringBuffer res = new StringBuffer();
    
    if(!Character.isJavaIdentifierStart(chars[0]))
      {
	res.append("$$");
	appendHexRepr(res,(int) chars[0]);
      }
    else
      res.append(chars[0]);
    
    for(int i=1; i<chars.length; i++)
      if(//chars[i]!='.' &&
	 !Character.isJavaIdentifierPart(chars[i]))
	{
	  res.append("$$");
	  appendHexRepr(res,(int) chars[i]);
	}
      else
	res.append(chars[i]);
    
    return res.toString();
  }

  private static void appendHexRepr(StringBuffer sb, int n)
  {
    String num = Integer.toHexString(n);
    for(int pad = num.length(); pad<4; pad++)
      sb.append("0");
    sb.append(num);
  }
  
  public static String unescape(String s)
  {
    if(s==null)
      return null;
    
    char[] chars = s.toCharArray();
    StringBuffer res = new StringBuffer();

    for(int i=0; i<chars.length; i++)
      if(chars[i]=='$' && chars[i+1]=='$')
	{
	  res.append((char) Integer.parseInt(s.substring(i+2,i+6),16));
	  i+=5;
	}
      else
	res.append(chars[i]);

    return res.toString();
  }  
}
