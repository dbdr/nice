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
//$Modified: Mon Aug 07 15:31:46 2000 by Daniel Bonniot $
// Description : Syntactic pattern for method bodies declaration

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import mlsub.typing.Domain;
import mlsub.typing.TypeConstructor;
import mlsub.typing.Polytype;
import mlsub.typing.Monotype;
import mlsub.typing.MonotypeVar;
import mlsub.typing.Constraint;
import mlsub.typing.MonotypeConstructor;

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
  public Pattern(LocatedString name, TypeIdent tc, bossa.syntax.Monotype type)
  {
    this.name = name;
    this.typeConstructor = tc;
    this.type = type;
  }

  final mlsub.typing.Monotype getType()
  {
    return t;
  }

  final boolean isSharp()
  {
    return tc!=null &&
      TypeConstructors.isSharp(tc);
  }

  Domain getDomain()
  {
    if(tc==null)
      return Domain.bot;
    
    MonotypeVar[] tp = MonotypeVar.news(tc.arity());
    
    return new Domain(new Constraint(tp,null),
		      new MonotypeConstructor(tc, tp));
  }
  
  /**
   * Iterates getDomain on a collection of Pattern.
   */
  static Domain[] getDomain(Collection patterns)
  {
    Iterator i=patterns.iterator();
    Domain[] res = new Domain[patterns.size()];

    int n = 0;
    while(i.hasNext())
      res[n++] = ((Pattern) i.next()).getDomain();

    return res;
  }

  /**
   * Iterates getTypeConstructor on a collection of Pattern.
   */
  static TypeConstructor[] getTC(Collection patterns)
  {
    Iterator i=patterns.iterator();
    TypeConstructor[] res = new TypeConstructor[patterns.size()];

    int n = 0;
    while(i.hasNext())
      res[n++] = ((Pattern)i.next()).tc;

    return res;
  }
  
  Polytype getPolytype()
  {
    if(tc==null)
      return Polytype.bottom();
    
    MonotypeVar[] params = MonotypeVar.news(tc.arity());
    return new Polytype(new Constraint(params, null),
			new MonotypeConstructor(tc, params));
  }
  
  /**
   * Iterates getPolytype on a collection of Pattern.
   */
  static Polytype[] getPolytype(Collection patterns)
  {
    Iterator i=patterns.iterator();
    Polytype[] res = new Polytype[patterns.size()];

    int n = 0;
    while(i.hasNext())
      {
	Pattern p=(Pattern)i.next();
	if(!p.thisAtNothing())
	  res[n++] = p.getPolytype();
      }
    return res;
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/
  
  void resolveTC(TypeScope scope)
  {
    if(typeConstructor!=null)
      {
	tc = typeConstructor.resolveToTC(scope);
	typeConstructor = null;
      }
  }
  
  void resolveType(TypeScope scope)
  {
    if(type!=null)
      {
	t = type.resolve(scope);
	type = null;
      }
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
    String res = name.toString();

    if(typeConstructor!=null)
      res += "@" + typeConstructor;
    else if(tc!=null)
      res += "@" + tc;
    
    return res;
  }

  public final static String AT_encoding = "$";
  public final static int AT_len = AT_encoding.length();
  
  /**
   * Returns a string used to recognize this pattern in the bytecode.
   *
   * This is usefull to distinguish alternatives of a method.
   */
  public String bytecodeRepresentation()
  {
    String enc = nice.tools.code.Types.bytecodeRepresentation(tc);

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
  TypeIdent typeConstructor;
  TypeConstructor tc;
  private bossa.syntax.Monotype type;
  private mlsub.typing.Monotype t;
}
