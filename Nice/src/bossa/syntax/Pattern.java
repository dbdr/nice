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
//$Modified: Mon Nov 08 18:57:05 1999 by bonniot $
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
    
    TypeParameters tp=new TypeParameters(typeConstructor.name,typeConstructor.variance);
    
    return new Domain(new Constraint(tp.content,null),
		      new MonotypeConstructor(typeConstructor,
					      tp,
					      typeConstructor.location()));
  }
  
  Polytype getPolytype()
  {
    if(typeConstructor==null)
      return Polytype.bottom();
    
    TypeParameters tp=new TypeParameters
      (typeConstructor.name,typeConstructor.variance);
    bossa.typing.Typing.introduce(tp.content);
    
    return new Polytype(Constraint.True(), 
			new MonotypeConstructor(typeConstructor,
						tp,
						typeConstructor.location()));
  }
  
  /**
   * Iterates getPolytype on a collection of Pattern.
   */
  static Collection getPolytype(Collection patterns)
  {
    Iterator i=patterns.iterator();
    Collection res=new ArrayList(patterns.size());

    while(i.hasNext())
      {
	Pattern p=(Pattern)i.next();
	if(!p.thisAtNothing())
	  res.add(p.getPolytype());
      }
    return res;
  }
  
  /**
   * Iterates getDomain on a collection of Pattern.
   */
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

  /**
   * Returns a string used to recognize this pattern in the bytecode.
   *
   * This is usefull to distinguish alternatives of a method.
   */
  public String bytecodeRepresentation()
  {
    if(typeConstructor==null)
      return "@_";
    else
      return "@"+typeConstructor.name;
  }
  
  public static String bytecodeRepresentation(List patterns)
  {
    String res="";
    for(Iterator i=patterns.iterator();
	i.hasNext();)
      res+=((Pattern)i.next()).bytecodeRepresentation();
    return res;
  }
  
  /****************************************************************
   * Misc.
   ****************************************************************/
  
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
