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

// File    : MonotypeVar.java
// Created : Fri Jul 23 15:36:39 1999 by bonniot
//$Modified: Thu Jul 29 17:42:09 1999 by bonniot $

package bossa.syntax;

import java.util.*;

import bossa.util.*;
import bossa.engine.*;

/**
 * A monotype variable.
 * 
 * @author bonniot
 */

public class MonotypeVar extends Monotype
  implements TypeSymbol, bossa.engine.Element
{
  public MonotypeVar(LocatedString name)
  {
    this(name,false);
  }

  public MonotypeVar(LocatedString name, boolean soft)
  {
    this.name=name;
    this.soft=soft;
  }

  Monotype cloneType()
  {
    return this;
  }

  public boolean hasName(LocatedString s)
  {
    return name.equals(s);
  }

  /****************************************************************
   * Functional Types
   ****************************************************************/

  public Monotype codomain()
  {
    return equivalentCodomain;
  }
  
  public Collection domain()
  {
    return equivalentDomain;
  }
  
  /****************************************************************
   * Kinding
   ****************************************************************/

  private void functionalCast(int arity)
  {
    Internal.error(equivalentCodomain!=null,"equivalentCodomain!=null in MonotypeVar");
    
    equivalentCodomain=Monotype.fresh(new LocatedString(name.content+"0",name.location()));
    equivalentDomain=Monotype.freshs(arity,name);

//      functionalType=new FunType(equivalentDomain,equivalentCodomain);
	
//      return functionalType;
  }
  
  TypeConstructor getTC()
  {
    return equivalentTC;
  }
  
  TypeParameters getTP()
  {
    return equivalentTP;
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  Monotype resolve(TypeScope ts)
  {
    if(soft)
      return this;
    
    TypeSymbol s=ts.lookup(this.name);
    User.error(s==null,this,this.name+" is not defined");

    if(s instanceof Monotype)
      return (Monotype) s;

    // In this case, it was indeed a type constructor,
    // applied to no type parameters
    if(s instanceof TypeConstructor)
      return new MonotypeConstructor((TypeConstructor) s, null);

    Internal.error(this,this.name+" is not well kinded :"+s.getClass());
    return null;
  }

  Monotype substitute(java.util.Map map)
  {
    if(map.containsKey(name))
      return (Monotype) map.get(name);
    return this;
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
    return "\""+name+"\"";
  }

  public LocatedString getName()
  {
    return name;
  }

  LocatedString name;

  /****************************************************************
   * low-level interface
   ****************************************************************/

  private int id=-1;
  
  public int getId() 		{ return id; }
  
  public void setId(int value) 	{ id=value; }
  
  Kind kind;
  
  public Kind getKind() 	{ return kind; }
  
  public void setKind(Kind value)
  {
    // it is ok to reset kind to null
    Internal.error(kind!=null && value!=null,"Variance already set in MonotypeVar");
    
    kind=value;

    if(value==null)
      return;

    // Do the apropriate cast
    if(value instanceof FunTypeKind)
      functionalCast(((FunTypeKind)value).domainArity);
    else if(value instanceof Variance)
      {
	Variance v=(Variance) value;
	
	equivalentTC=new TypeConstructor(this,v);
	bossa.typing.Typing.introduce(equivalentTC);
	equivalentTP=new TypeParameters(this.name,v);
      }
    // in other cases (kind of the rigid monotype variables)
    // nothing to do
  }
  
  /** When this variable is comparable to a functional type */
  private Monotype equivalentCodomain;
  private Collection equivalentDomain;
  private Monotype functionalType;

  private TypeConstructor equivalentTC;
  private TypeParameters equivalentTP;

  /**
   * If true,  this is really a variable monotype
   * If false, this must be found in scope
   */

  private boolean soft;
}
