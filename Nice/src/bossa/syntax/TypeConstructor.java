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
//$Modified: Thu Jan 20 14:12:16 2000 by bonniot $

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
    setVariance(Variance.make(d.typeParameters.size()));
    bossa.typing.Typing.introduce(this); /* The introduction is done here,
					  * in order to enable recursive class definitions
					  * (the id is determined here)
					  */
    //if(!name.location().isValid()) Internal.warning(name+"");
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
    //if(!name.location().isValid()) Internal.warning(name+"");
  }

  /**
   * Constructs a fresh TypeConstructor.
   *
   * @param hint a base for the new name
   * @param v the variance of this type constructor
   */
  public TypeConstructor(LocatedString hint, Variance v)
  {
    this.name=new LocatedString(hint.content+"'",hint.location());

    Internal.error(v==null,name+" : null Variance");
    
    setVariance(v);
    this.definition=null;
    //if(!name.location().isValid()) Internal.warning(name+"");
  }

  /**
   * Tell which variance this TypeConstructor has.
   * Can be called from ImplementsCst.
   */
  void setVariance(Variance v)
  {
    if(variance!=null)
      {
	if(!(variance.equals(v)))
	  User.error(this,"Incorrect variance for "+this);
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

  gnu.bytecode.Type getJavaType()
  {
    if(name.content.equals("Array"))
      return 
	//new gnu.bytecode.ArrayType(gnu.bytecode.Type.pointer_type);
	bossa.SpecialTypes.arrayType;
    
    if(definition!=null)
      return ClassDefinition.javaClass(definition);
    else
      return 
	gnu.bytecode.Type.pointer_type;
	//gnu.bytecode.ClassType.make(name.content);
  }

  /**
   * @return a list L of gnu.bytecode.Type such that
   * a bytecode value V is below 'this' iff
   * there exists T in L such that V instanceof T
   */
  public ListIterator getJavaInstanceTypes()
  {
    List illegitimateChildren = definition.getIllegitimateChildren();
    
    List res = new ArrayList(1+illegitimateChildren.size());
    addType(res,this.getJavaType());
    
    for(Iterator i = illegitimateChildren.iterator();
	i.hasNext();)
      addType(res,((ClassDefinition) i.next()).tc.getJavaType());
    
    return res.listIterator();
  }
  
  /**
   * Usefull for types with implicit coercion.
   */
  private void addType(List res, gnu.bytecode.Type type)
  {
    res.add(type);
    if(type instanceof gnu.bytecode.ArrayType)
      res.add(gnu.bytecode.ClassType.make("_Array"));
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  TypeConstructor resolve(TypeScope typeScope)
  {
    if(definition==null
       // FIXME: bad hack
       && !(name.toString().equals("dummy type symbol"))
       )
      {
	TypeSymbol s=typeScope.lookup(name.toString());
	if(s==null)
	  User.error(name,"Class "+name+" is not defined"," in "+typeScope);
	
	if(s instanceof TypeConstructor)
	  return (TypeConstructor) s;
	else
	  {
	    String what;
	    if(s instanceof InterfaceDefinition)
	      what="an interface";
	    else
	      what="a "+s.getClass();
	    
	    User.error(name,name+" should be a class, not "+what);
	  }
	return null;
      }
    return this;
  }

  TypeSymbol resolveToTypeSymbol(TypeScope typeScope)
  {
    if(definition==null)
      {
	TypeSymbol s=typeScope.lookup(name.toString());
	if(s==null)
	  User.error(this,"Class or interface "+name+" is not defined");

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

  boolean instantiable()
  {
    return definition!=null && !definition.isAbstract;
  }
  
  boolean constant()
  {
    return definition!=null;
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
  
  public boolean isConcrete()
  {
    return definition!=null && definition.isSharp;
  }

  /****************************************************************
   * Fields
   ****************************************************************/

  public int enumerateTagIndex=-1; // used in Typing.enumerate. ugly ! Subclass

  ClassDefinition definition;
  public LocatedString name;
  public Variance variance;
}
