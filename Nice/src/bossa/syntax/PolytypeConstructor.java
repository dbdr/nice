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

// File    : PolytypeConstructor.java
// Created : Mon Jul 19 17:47:49 1999 by bonniot
//$Modified: Thu Sep 16 10:17:44 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
 * A type that takes type parameters and that returns a Polytype.
 */
public class PolytypeConstructor extends Type
{
  // The only place where this constructor should be used is Type.newType
  PolytypeConstructor(List parameters, Polytype polytype)
  {
    this.parameters=parameters;
    this.polytype=polytype;
    addTypeSymbols(parameters);
    addChild(polytype);
  }

  Type removeUnusefullTypeParameters()
  {
    // We try to remove the need to pass imperative parameters explicitely
    // when the imperative type parameters all appear at imperative places
    // in the monotype
    if(polytype.allImperative(parameters))
      {
	Polytype res=new Polytype
	  (Constraint.and(polytype.getConstraint(),new Constraint(parameters,null)),polytype.getMonotype());
	return res;
      }
    return this;
  }
  
  Type cloneType()
  {
    //TODO:create new type parameters?
    return new PolytypeConstructor(parameters,(Polytype)polytype.cloneType());
  }
  
  //Acces methods

  public List getTypeParameters()
  {
    return parameters;
  }

  public Constraint getConstraint()
  {
    return polytype.getConstraint();
  }

  public Monotype getMonotype()
  {
    return polytype.getMonotype();
  }

  Polytype instantiate(TypeParameters typeParameters)
    throws BadSizeEx
  { 
    if(parameters.size()!=typeParameters.size())
      throw new BadSizeEx(parameters.size(),typeParameters.size());

    Polytype res;

    Map map=new HashMap();
    Iterator formals=parameters.iterator(),
      effectives=typeParameters.content.iterator();
    while(formals.hasNext())
      {
	MonotypeVar var=(MonotypeVar) formals.next();
	map.put(var/*.name*/,effectives.next());
      }

    res=polytype.substitute(map);

    return res;
  }

  /*******************************************************************
   * Scoping
   *******************************************************************/

  Collection /* of Monotype */ domain()
  {
    return polytype.domain();
  }

  Monotype codomain()
  {
    return polytype.codomain();
  }

  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    return Util.map("<",", ",">",parameters)+" "+polytype;
  }

  List /* of MonotypeVar */ parameters;
  public Polytype polytype;
}
