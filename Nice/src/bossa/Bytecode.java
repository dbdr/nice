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

// File    : Bytecode.java
// Created : Fri Dec 10 19:03:56 1999 by bonniot
//$Modified: Fri Feb 18 18:57:15 2000 by Daniel Bonniot $

package bossa;

import bossa.util.*;

/**
 * Usefull function for bytecode generation.
 * 
 * @author bonniot
 */

public abstract class Bytecode
{
  private static final char firstEscapeChar = '£';
  private static final char escapeChar = '£';

  static
  {
    //System.out.println(Character.isJavaIdentifierStart(firstEscapeChar));
    //System.out.println(Character.isJavaIdentifierPart(escapeChar));  
  }
  
  public static String escapeString(String s)
  {
    if(s==null || s.length()==0)
      return null;
    if(s.equals("<init>") || s.equals("<clinit>"))
      return s;
    
    char[] chars = s.toCharArray();
    StringBuffer res = new StringBuffer();
    
    if(!Character.isJavaIdentifierStart(chars[0]))
      {
	res.append(""+firstEscapeChar+escapeChar);
	appendHexRepr(res,(int) chars[0]);
      }
    else
      res.append(chars[0]);
    
    for(int i=1; i<chars.length; i++)
      if(chars[i]!='.' &&
	 !Character.isJavaIdentifierPart(chars[i]))
	{
	  res.append(escapeChar);
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
    
  public static String unescapeString(String s)
  {
    if(s==null)
      return null;
    
    char[] chars = s.toCharArray();
    StringBuffer res = new StringBuffer();

    int i;
    if(chars[0]==firstEscapeChar && chars[1]==escapeChar)
      {
	res.append((char) Integer.parseInt(s.substring(2,6),16));
	i=6;
      }
    else
      {
	res.append(chars[0]);
	i=1;
      }
    
    for(; i<chars.length; i++)
      if(chars[i]==escapeChar)
	{
	  res.append((char) Integer.parseInt(s.substring(i+1,i+5),16));
	  i+=4;
	}
      else
	res.append(chars[i]);

    return res.toString();
  }  
}
