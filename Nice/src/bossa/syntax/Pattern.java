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
//$Modified: Fri Jan 28 17:01:41 2000 by Daniel Bonniot $
// Description : Syntactic pattern for method bodies declaration

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Represents the information about one argument of a method body.
 *
 * @see MethodBodyDefinition
 */
public class Pattern
{
  /**
   * Builds a new pattern.
   *
   * @param name the name of the argument
   * @param tc a TypeConstructor that the argument must match
       for this alternative to be selected
   * @param type a monotype that must be equal to 
   *    the argument's runtime type. This is usefull to introduce
   *    a type constructor variable with the exact type of the argument.
   */
  public Pattern(LocatedString name, TypeConstructor tc, Monotype type)
  {
    this.name=name;
    this.typeConstructor=tc;
    this.type=type;
  }

  final TypeConstructor getTC()
  {
    return typeConstructor;
  }
  
  final Monotype getType()
  {
    return type;
  }

  final boolean isSharp()
  {
    return typeConstructor!=null &&
      typeConstructor.name.toString().charAt(0)=='#';
  }
  
  Domain getDomain()
  {
    if(typeConstructor==null)
      return Domain.bot;
    
    TypeParameters tp=new TypeParameters(typeConstructor.name,typeConstructor.variance);
    
    return new Domain(new Constraint(tp.content,null),
		      new MonotypeConstructor(typeConstructor,
					      tp,
					      typeConstructor.location()));
  }
  
  /**
   * Iterates getDomain on a collection of Pattern.
   */
  static List getDomain(Collection patterns)
  {
    Iterator i=patterns.iterator();
    List res=new ArrayList(patterns.size());

    while(i.hasNext())
      res.add(((Pattern)i.next()).getDomain());

    return res;
  }
  
  Polytype getPolytype()
  {
    if(typeConstructor==null)
      return Polytype.bottom();
    
    TypeParameters tp=new TypeParameters
      (typeConstructor.name,typeConstructor.variance);
    
    return new Polytype(new Constraint(tp.content,null),
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
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  void resolveTC(TypeScope scope)
  {
    if(typeConstructor!=null)
      typeConstructor=typeConstructor.resolve(scope);
  }
  
  void resolveType(TypeScope scope)
  {
    if(type!=null)
      type=type.resolve(scope);
  }
  
  static void resolveTC(TypeScope scope, Collection patterns)
  {
    Iterator i=patterns.iterator();

    while(i.hasNext())
      ((Pattern)i.next()).resolveTC(scope);
  }
  
  static void resolveType(TypeScope scope, Collection patterns)
  {
    Iterator i=patterns.iterator();

    while(i.hasNext())
      ((Pattern)i.next()).resolveType(scope);
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

  public final static String AT_encoding = "$";
  
  /**
   * Returns a string used to recognize this pattern in the bytecode.
   *
   * This is usefull to distinguish alternatives of a method.
   */
  public String bytecodeRepresentation()
  {
    if(typeConstructor==null)
      return AT_encoding+"_";

    String enc=typeConstructor.name.toString();
    if(isSharp())
      enc=enc.substring(1);
    
    return AT_encoding+enc;
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
  
  /** 
   * Expresses that this pattern was a fake one.
   * @see MethodBodyDefinition
   */
  boolean thisAtNothing()
  {
    return typeConstructor==null && name.content.equals("this");
  }

  LocatedString name;
  TypeConstructor typeConstructor;
  private Monotype type;
}
