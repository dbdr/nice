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
//$Modified: Fri Aug 13 14:47:16 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.engine.*;
import bossa.typing.Variance;

/**
 * A class. It "needs" type parameters to become a Monotype
 */
public class TypeConstructor
  implements Located, TypeSymbol, bossa.engine.Element
{
  /**
   * Constructs a new type constructor from a well known class.
   *
   * @param d the definition of the class
   */
  public TypeConstructor(ClassDefinition d)
  {
    this.definition=d;
    this.name=d.name;
    setVariance(new Variance(d.typeParameters.size()));
    this.id=-1;
  }

  /**
   * Constructs a type constructor whose class is not known yet.
   * We just know the name for the moment, 
   * the scoping will find the class definition later.
   *
   * @param className the name of the class
   */
  public TypeConstructor(LocatedString className)
  {
    this.name=className;
    this.definition=null;
    variance=null;
    this.id=-1;
  }

  /**
   * Constructs a fresh TypeConstructor.
   *
   * @param source a MonotypeVariable which shall deconstruct to this type constructor
   * @param v the variance of this type constructor
   */
  TypeConstructor(MonotypeVar source, Variance v)
  {
    this.name=new LocatedString(source.name.content+"'",source.name.location());

    Internal.error(v==null,name+" : null Variance");
    
    setVariance(v);
    this.definition=null;
  }

  private void setVariance(Variance v)
  {
    this.variance=v;
    Internal.error(kind!=null,"Kind should be null in TC.setVariance");
    
    this.kind=bossa.engine.Engine.getConstraint(v);
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
    Internal.error(variance==null,"Null variance in TypeConstructor");
    
    if(variance.size!=tp.size())
      throw new BadSizeEx(variance.size,tp.size());
    return new Polytype(new MonotypeConstructor(this,tp,this.location()));
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
	    //	    throw new Error();
	    Internal.error(name,"type constructor is not a type symbol but a "
	       +s.getClass()); 
	       return null;
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
   * Kinding
   ****************************************************************/

  private int id=-1;
  
  public int getId() 		{ return id; }
  
  public void setId(int value) 	{ id=value; }
  
  private Kind kind;
  
  public Kind getKind() 	{ return kind; }
  
  public void setKind(Kind value)
  {
    Internal.warning("Variance set in TC by engine for "+name);
    Internal.error(kind!=null,"Variance already set in TypeConstructor");
    
    kind=value;
  }
  
  /****************************************************************
   * Fields
   ****************************************************************/

  ClassDefinition definition;
  LocatedString name;
  public Variance variance;
}
