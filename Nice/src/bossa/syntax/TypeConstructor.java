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

// File    : TypeConstructor.java
// Created : Thu Jul 08 11:51:09 1999 by bonniot
//$Modified: Fri Jul 23 19:33:03 1999 by bonniot $
// Description : A class. It "needs" type parameters to become a Monotype

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class TypeConstructor
  implements Located, TypeSymbol
{
  /**
   * Constructs a new type constructor from a well known class
   *
   * @param d the definition of the class
   */
  public TypeConstructor(ClassDefinition d)
  {
    this.definition=d;
    this.name=d.name;
    this.variance=new Variance(d.typeParameters.size());
  }

  /**
   * Constructs a type constructor whose class is not known yet
   * We just know the name for the moment, 
   * the scoping will find the class definition later
   *
   * @param className the name of the class
   */
  public TypeConstructor(LocatedString className)
  {
    this.name=className;
    this.definition=null;
    this.variance=new Variance(-1);
  }

  static Collection toLocatedString(Collection c)
  {
    Collection res=new ArrayList(c.size());
    Iterator i=c.iterator();
    while(i.hasNext())
      res.add(((TypeConstructor)i.next()).name);
    return res;
  }

  public boolean hasName(LocatedString s)
  {
    return name.equals(s);
  }

  Type getType()
  {
    if(!(definition instanceof ClassDefinition))
      return null;
    return ((ClassDefinition)definition).getType();
  }

  Type instantiate(TypeParameters tp)
    throws BadSizeEx
  {
    if(variance.size!=tp.size())
      throw new BadSizeEx(variance.size,tp.size());
    return new Polytype(new MonotypeConstructor(this,tp));
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  TypeConstructor resolve(TypeScope typeScope)
  {
    //    if(definition==null)
      {
	TypeSymbol s=typeScope.lookup(name);
	if(s==null)
	  User.error(name,"Class "+name+" is not defined");
	if(s instanceof TypeConstructor)
	  return (TypeConstructor)s;
	else
	  {
	    throw new Error();
	    /* Internal.error(name,"type constructor is not a type symbol but a "
	       +s.getClass()); 
	       return null;*/
	  }
      }
  }

  /** iterates resolve on the collection of TypeConstructor */
  static Collection resolve(TypeScope typeScope, Collection c)
  {
    Collection res=new ArrayList(c.size());
    Iterator i=c.iterator();
    while(i.hasNext())
      res.add(((TypeConstructor)i.next()).resolve(typeScope));

    return res;
  }

  VarScope memberScope()
  {
    return definition.getScope();
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return name.location();
  }

  public String toString()
  {
    if(definition!=null)
      return definition.name.toString();
    else 
      return "\""+name.toString()+"\"";
  }

  public LocatedString getName()
  {
    return name;
  }

  /****************************************************************
   * Fields
   ****************************************************************/

  ClassDefinition definition;
  LocatedString name;
  Variance variance;
}
