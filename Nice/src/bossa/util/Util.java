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
//$Modified: Wed Oct 13 14:52:24 1999 by bonniot $
// Description : Usefull fonctions used everywhere

package bossa.util;

import java.util.*;

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

    String res=init;

    Iterator i=c.iterator();
    while(i.hasNext()){
      res=res+i.next().toString();
      if(i.hasNext())
	res=res+delim;
    }
    return res+end;
  }

  public static String map(String init, String delim, String end, 
			   Collection c, int param)
  {
    if(c==null || c.size()==0)
      return "";

    String res=init;

    Iterator i=c.iterator();
    while(i.hasNext()){
      res=res+((Printable)i.next()).toString(param);
      if(i.hasNext())
	res=res+delim;
    }
    return res+end;
  }

  public static String map(String init, String delim, String end, 
			   boolean alwaysInitEnd, Collection c)
  {
    if(alwaysInitEnd)
      return init+map("",delim,"",c)+end;
    
    switch(c.size())
      {
      case 0 : return "";
      case 1 : 
	Iterator i=c.iterator();
	return i.next().toString();
      default : return map(init,delim,end,c);
      }
  }

  /** iterates on a set of pairs */
  public static String map(String init, boolean keyFirst, String delim1, String delim2,
			   String end, Set s)
  {
    String res=init;
    Iterator i;
    i=s.iterator();

    while(i.hasNext()){
      Map.Entry e=(Map.Entry)i.next();
      res=res
	+e.getValue().toString()
	+delim1
	+e.getKey().toString()
	+delim2;
    }
    return res+end;
  }

}
