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
//$Modified: Thu Jul 29 12:19:11 1999 by bonniot $
// Description : A type that takes TypeParameters 
//  and that returns a Polytype

package bossa.syntax;

import bossa.util.*;
import java.util.*;

public class PolytypeConstructor extends Type
{
  PolytypeConstructor(Collection parameters, Polytype polytype)
  {
    this.parameters=parameters;
    this.polytype=polytype;
  }

  //Acces methods
  public Collection getTypeParameters()
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

    //Debug.println(this+" :: "+typeParameters);
    Polytype res;

    Map map=new HashMap();
    Iterator formals=parameters.iterator(),
      effectives=typeParameters.content.iterator();
    while(formals.hasNext())
      {
	MonotypeVar var=(MonotypeVar) formals.next();
	map.put(var.name,effectives.next());
      }

    res=polytype.substitute(map);

    //    Debug.println("->"+res);
    return res;
  }

  /*******************************************************************
   * Scoping
   *******************************************************************/

  void buildScope(TypeScope ts)
  {
    typeScope=TypeScope.makeScope(ts,parameters);
    polytype.buildScope(typeScope);
  }

  void resolve()
  {
    polytype.resolve();
  }

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

  Collection /* of MonotypeVar */ parameters;
  public Polytype polytype;
}
