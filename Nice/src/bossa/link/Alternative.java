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

// File    : Alternative.java
// Created : Mon Nov 15 12:20:40 1999 by bonniot
//$Modified: Tue Nov 16 20:28:02 1999 by bonniot $

package bossa.link;

import bossa.syntax.*;
import bossa.util.*;

import java.util.*;

/**
 * Represents a method alternative in the link.
 *
 * It can be build either from information in a Bossa source,
 * or read from a compiled bytecode file.
 *
 * @author bonniot
 */

public class Alternative
{
  /**
   * When read from a source file.
   */
  public Alternative(MethodDefinition m, List patterns)
  {
    this.method=m.bytecodeName();
    this.patterns=patterns;
    add();
  }

  /**
   * When read from a bytecode file.
   */
  public Alternative(String s)
  {
    int at = s.indexOf('@');
    method = s.substring(0,at);
    
    this.patterns = new ArrayList();

    while(at<s.length())
      {
	int next = s.indexOf('@',at+1);
	if(next==-1)
	  next=s.length();
	
	String name = s.substring(at+1,next);
	if(name.equals("_"))
	  patterns.add(Domain.bot); // Domain Ex a .a 
	else
	  {
	    Monotype m = new MonotypeConstructor(new TypeConstructor(new LocatedString(name,Location.nowhere())),null,Location.nowhere());
	    m = m.resolve(Node.getGlobalTypeScope());    
	    patterns.add(new Domain(null,m));
	  }
	
	at = next;
      }
    
    add();
  }

  /****************************************************************
   * Graph traversal
   ****************************************************************/

  static final int 
    UNVISITED = 1,
    VISITING  = 2,
    VISITED   = 3;
  
  int mark = UNVISITED;
  
  /****************************************************************
   * Order on alternatives
   ****************************************************************/

  /**
   * Returns true iff 'a' is more precise than 'b'.
   */
  static boolean leq(Alternative a, Alternative b)
  {
    Iterator i = a.patterns.iterator();
    Iterator j = b.patterns.iterator();
    while(i.hasNext())
      {
	Domain pa = (Domain) i.next();
	Domain pb = (Domain) j.next();
	if(!Domain.leq(pa,pb))
	  return false;
      }
    return true;
  }

  /**
   * Tests the matching of tags against a method alternative.
   */
  boolean matches(TypeConstructor[] tags)
  {
    int n=0;
    for(Iterator i=patterns.iterator();
	i.hasNext();n++)
      if(!Domain.in(tags[n], (Domain) i.next()))
	return false;
    
    return true;
  }
  
  public String toString()
  {
    return method+Util.map("(",", ",")",patterns);
  }

  String method;
  List /* of Domain */ patterns;

  boolean visiting;
  
  /****************************************************************
   * Regrouping alternatives per method
   ****************************************************************/

  private static HashMap alternatives = new HashMap();
  
  private void add()
  {
    List l = (List) alternatives.get(method);
    if(l==null)
      {
	l = new ArrayList();
	alternatives.put(method,l);
      }
    l.add(this);
  }
  
  static List listOfAlternative(MethodDefinition m)
  {
    return (List) alternatives.get(m.bytecodeName());
  }
}
