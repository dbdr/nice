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
//$Modified: Tue Jul 27 11:16:48 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A monotype variable.
 * 
 * @author bonniot
 */

public class MonotypeVar extends Monotype
  implements TypeSymbol
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

  bossa.syntax.Monotype cloneType()
  {
    return this;
  }

  public boolean hasName(LocatedString s)
  {
    return name.equals(s);
  }

  /****************************************************************
   * Functional type
   ****************************************************************/

  public Monotype functionalCast(int arity)
    throws bossa.typing.TypingEx
  {
    if(equivalentCodomain==null)
      {
	equivalentCodomain=Monotype.fresh(new LocatedString(name.content+"0",name.location()));
	equivalentDomain=Monotype.freshs(arity,name);

	functionalType=new FunType(equivalentDomain,equivalentCodomain);
	
	//bossa.typing.Typing.leq(this,functionalType);
	//bossa.typing.Typing.leq(functionalType,this);
      }
    return functionalType;
  }
  
  public Monotype codomain()
  {
    return equivalentCodomain;
  }
  
  public Collection domain()
  {
    return equivalentDomain;
  }
  
  public TypeConstructor decomposeTC(Variance v)
  {
    if(equivalentTC==null)
      equivalentTC=new TypeConstructor(this,v);
    return equivalentTC;
  }
  
  public TypeParameters decomposeTP(Variance v)
  {
    if(equivalentTP==null)
      equivalentTP=new TypeParameters(this,v);
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
  /**
   * If true,  this is really a variable monotype
   * If false, this must be found in scope
   */
  private boolean soft;

  /** When this variable is comparable to a functional type */
  private Monotype equivalentCodomain;
  private Collection equivalentDomain;
  private Monotype functionalType;

  private TypeConstructor equivalentTC;
  private TypeParameters equivalentTP;
}
