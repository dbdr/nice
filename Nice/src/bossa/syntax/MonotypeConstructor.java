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
//$Modified: Wed Jul 28 17:30:33 1999 by bonniot $
// Description : A monotype, build by application of
//   a type constructor to type parameters

package bossa.syntax;

import java.util.*;

import bossa.util.*;
import bossa.engine.*;

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

  TypeConstructor getTC()
  {
    return tc;
  }
  
  TypeParameters getTP()
  {
    return parameters;
  }
  
  /****************************************************************
   * Kinding
   ****************************************************************/

  private int id;
  
  public int getId() 		{ return id; }
  
  public void setId(int value) 	{ id=value; }
  
  public Kind getKind() 	{ return tc.variance; }
  
  public void setKind(Kind value)
  {
    Internal.error("Variance set in MonotypeConstructor");
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
