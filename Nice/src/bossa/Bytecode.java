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
//$Modified: Thu May 04 16:20:25 2000 by Daniel Bonniot $

package bossa;

import bossa.util.*;

/**
 * Usefull function for bytecode generation.
 * 
 * @author bonniot
 */

public abstract class Bytecode
{
  static
  {
    //System.out.println(Character.isJavaIdentifierStart(firstEscapeChar));
    //System.out.println(Character.isJavaIdentifierPart(escapeChar));  
  }
  
  /**
     Encode une chaine pour en faire un identificateur Java valide.

     Tout character offendant est remplacé par '$$' suivi de son
     (uni)code en Hexadecimal.
  */
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
  
  public static String unescapeString(String s)
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
