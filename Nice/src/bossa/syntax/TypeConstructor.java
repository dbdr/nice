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
//$Modified: Thu Nov 04 15:31:30 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.engine.*;
import bossa.typing.Variance;

/**
 * A class. It "needs" type parameters to become a Monotype
 */
public class TypeConstructor
  implements Located, TypeSymbol, bossa.engine.Element, Printable
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
    bossa.typing.Typing.introduce(this); /* The introduction is done here,
					  * in order to enable recursive class definitions
					  * (the id is determined here)
					  */
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
    this.id=-1-new Random().nextInt(100000);
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

  /**
   * Tell which variance this TypeConstructor has.
   * Can be called from ImplementsCst.
   */
  void setVariance(Variance v)
  {
    if(variance!=null)
      {
	User.error(!(variance.equals(v)),this,"Incorrect variance for "+this);
	return;
      }
    this.variance=v;
    this.kind=bossa.engine.Engine.getConstraint(v);
  }

  TypeConstructor substitute(Map map)
  {
    if(map.containsKey(this))
      return (TypeConstructor)map.get(this);
    else
      return this;
  }

  public TypeSymbol cloneTypeSymbol()
  {
    return new TypeConstructor(name);
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

  Polytype getType()
  {
    if(!(definition instanceof ClassDefinition))
      return null;
    return ((ClassDefinition)definition).getType();
  }

  Polytype instantiate(TypeParameters tp)
    throws BadSizeEx
  {
    Internal.error(variance==null,"Null variance in TypeConstructor");
    
    if(variance.size!=tp.size())
      throw new BadSizeEx(variance.size,tp.size());
    return new Polytype(new MonotypeConstructor(this,tp,this.location()));
  }

  boolean instantiable()
  {
    Internal.error(definition==null,
		   "Null definition TypeConstructor.instatiable");
    return !definition.isAbstract;
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  TypeConstructor resolve(TypeScope typeScope)
  {
    if(definition==null)
      {
	TypeSymbol s=typeScope.lookup(name);
	User.error(s==null,this,"Class "+name+" is not defined"," in "+typeScope);
	
	if(s instanceof TypeConstructor)
	  return (TypeConstructor)s;
	else
	  Internal.error(name,name+" is not a type constructor but a "
			 +s.getClass().getName()); 
	return null;
      }
    return this;
  }

  TypeSymbol resolveToTypeSymbol(TypeScope typeScope)
  {
    if(definition==null)
      {
	// Should not be usefull anymore
	/*
	  if(name.content.startsWith("Top"))
	  return InterfaceDefinition.top(Integer.parseInt(name.content.substring(3)));
	*/
	TypeSymbol s=typeScope.lookup(name);
	User.error(s==null,this,"Class or interface "+name+" is not defined");
	return s;
      }
    return this;
  }

  /** iterates resolve on the collection of TypeConstructor */
  static List resolve(TypeScope typeScope, List c)
  {
    List res=new ArrayList(c.size());
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
    String res;
    if(definition!=null)
      res=definition.name.toString();
    else 
      if(bossa.typing.Typing.dbg) 
	res="\""+name.toString()+"\"";
      else
	res=name.toString();
    if(bossa.typing.Typing.dbg) res+="("+id+")";
    //if(bossa.typing.Typing.dbg) res+=super.toString();
    return res;
  }

  public String toString(int param)
  {
    return toString();
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
    Internal.error(kind!=null,"Variance already set in TypeConstructor");
    variance=(Variance)((Engine.Constraint)value).associatedKind;
    kind=value;
  }
  
  /****************************************************************
   * Fields
   ****************************************************************/

  ClassDefinition definition;
  public LocatedString name;
  public Variance variance;
}
