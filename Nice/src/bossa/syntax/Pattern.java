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

// File    : Pattern.java
// Created : Mon Jul 05 14:36:52 1999 by bonniot
//$Modified: Sat Jul 24 19:16:15 1999 by bonniot $
// Description : Syntactic pattern for method bodies declaration

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class Pattern
{
  public Pattern(LocatedString name)
  {
    this(name,null);
  }

  public Pattern(LocatedString name, TypeConstructor tc)
  {
    this.name=name;
    this.typeConstructor=tc;
  }

  Domain getDomain()
  {
    if(typeConstructor==null)
      return Domain.bottom();
        
    return new Domain(Constraint.True(), 
		      new MonotypeConstructor(typeConstructor,null));
  }
  
  static Collection getDomain(Collection patterns)
  {
    Iterator i=patterns.iterator();
    Collection res=new ArrayList(patterns.size());

    while(i.hasNext())
      res.add(((Pattern)i.next()).getDomain());

    return res;
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  void resolve(TypeScope scope)
  {
    if(typeConstructor==null)
      return;
    
    typeConstructor=typeConstructor.resolve(scope);
  }
  
  static void resolve(TypeScope scope, Collection patterns)
  {
    Iterator i=patterns.iterator();

    while(i.hasNext())
      ((Pattern)i.next()).resolve(scope);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    String res=name.toString();
    if(typeConstructor!=null)
      res=res+"@"+typeConstructor;
    return res;
  }

  /** expresses that this pattern was a fake one.
   *  @see MethodBodyDefinition
   */
  boolean thisAtNothing()
  {
    return typeConstructor==null && name.content.equals("this");
  }

  LocatedString name;
  TypeConstructor typeConstructor;
}
