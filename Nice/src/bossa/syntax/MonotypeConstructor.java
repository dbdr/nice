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

// File    : MonotypeConstructor.java
// Created : Thu Jul 22 09:15:17 1999 by bonniot
//$Modified: Tue Jul 27 11:07:50 1999 by bonniot $
// Description : A monotype, build by application of
//   a type constructor to type parameters

package bossa.syntax;

import bossa.util.*;
import bossa.syntax.TypeScope;
import bossa.syntax.Monotype;
import java.util.*;;

public class MonotypeConstructor extends Monotype
{
  /**
   * Constructs a monotype by application of the type constructor
   * to the type parameters
   *
   * @param tc the type constructor
   * @param parameters the type parameters
   */
  public MonotypeConstructor(TypeConstructor tc, TypeParameters parameters)
  {
    this.tc=tc;
    if(parameters==null)
      this.parameters=new TypeParameters(null);
    else
      this.parameters=parameters;
  }

  Monotype cloneType()
  {
    return new MonotypeConstructor(tc,parameters);
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  Monotype resolve(TypeScope typeScope) 
  {
    tc=tc.resolve(typeScope);
    parameters=parameters.resolve(typeScope);
    return this;
  }
  
  Monotype substitute(Map map)
  {
    return new MonotypeConstructor
      (tc,
       new TypeParameters(Monotype.substitute(map,parameters.content)));
  }

  public TypeConstructor decomposeTC(Variance v)
  {
    return tc;
  }
  
  public TypeParameters decomposeTP(Variance v)
  {
    return parameters;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return tc.location();
  }

  public String toString()
  {
    return ""+tc+parameters;
  }

  public TypeConstructor tc;
  TypeParameters parameters;
}
