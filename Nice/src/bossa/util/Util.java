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

// File    : Util.java
// Created : Fri Jul 02 18:35:58 1999 by bonniot
//$Modified: Tue Jul 18 17:58:45 2000 by Daniel Bonniot $
// Description : Usefull fonctions used everywhere

package bossa.util;

import java.util.*;

/**
   Set of useful static methods.
*/
public class Util
{
  /****************************************************************
   * String Iterations
   ****************************************************************/

  /** iterates toString on the collection */
  public static String map(Collection c)
  {
    return map("","","",c);
  }

  public static String map(String init, String delim, String end, Collection c)
  {
    if(c==null || c.size()==0)
      return "";

    String res = init;

    Iterator i = c.iterator();
    while(i.hasNext()){
      res += String.valueOf(i.next());
      if(i.hasNext())
	res += delim;
    }
    return res+end;
  }

  public static String map(String init, String delim, String end, 
			   Collection c, int param)
  {
    if(c==null || c.size()==0)
      return "";

    String res = init;

    Iterator i = c.iterator();
    while(i.hasNext()){
      Object o = i.next();
      if(o instanceof Printable)
	res += ((Printable) o).toString(param);
      else
	res += o.toString();
      if(i.hasNext())
	res += delim;
    }
    return res+end;
  }

  public static String map(String init, String delim, String end, 
			   boolean alwaysInitEnd, Collection c)
  {
    if(alwaysInitEnd)
      return init + map("",delim,"",c) + end;
    
    switch(c.size())
      {
      case 0  : return "";
      case 1  : return c.iterator().next().toString();
      default : return map(init,delim,end,c);
      }
  }

  /** iterates on a set of pairs */
  public static String map(String init, boolean keyFirst, String delim1, String delim2,
			   String end, Set s)
  {
    String res = init;
    Iterator i;
    i = s.iterator();

    while(i.hasNext()){
      Map.Entry e = (Map.Entry)i.next();
      res += e.getValue().toString()
	+delim1
	+e.getKey().toString()
	+delim2;
    }
    return res+end;
  }

  public static String map(String init, String delim, String end, Object[] c)
  {
    if(c==null || c.length==0)
      return "";

    String res = init;

    for(int i = 0; i<c.length-1; i++)
      res += String.valueOf(c[i]) + delim;
    return res + c[c.length-1] + end;
  }

  /****************************************************************
   * Names
   ****************************************************************/

  public static String simpleName(String fullyQualifiedName)
  {
    int lastDot = fullyQualifiedName.lastIndexOf('.');
    return fullyQualifiedName.substring(lastDot + 1);
  }


  /****************************************************************
   * English
   ****************************************************************/

  public static String plural(int number, String word)
  {
    StringBuffer res = new StringBuffer(number == 0 ? "no" : "" + number);
    res.append(' ');
    res.append(word);
    if (number != 1)
      res.append("s");
    return res.toString();
  }

  /**
     returns:
       " has no bars"
       " has 1 bar"
       " has n bars"
     or, if 0 < number < not
       " has only 1 bar"
       " has only n bars"
  */
  public static String has(int number, String word, int not)
  {
    StringBuffer res = new StringBuffer(" has ");
    if (0 < number && number < not)
      res.append("only ");
    res.append(number == 0 ? "no" : "" + number);
    res.append(' ');
    res.append(word);
    if (number != 1)
      res.append('s');
    return res.toString();
  }
}
