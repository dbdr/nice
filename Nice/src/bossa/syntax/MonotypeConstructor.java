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
//$Modified: Thu Jan 27 16:06:48 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;

import bossa.util.*;
import bossa.engine.*;

/**
 * A monotype, build by application of
 * a type constructor to type parameters.
 */
public class MonotypeConstructor extends Monotype
{
  /**
   * Constructs a monotype by application of the type constructor
   * to the type parameters
   *
   * @param tc the type constructor
   * @param parameters the type parameters
   */
  public MonotypeConstructor(TypeConstructor tc, TypeParameters parameters,
			     Location loc)
  {
    Internal.error(tc==null,"Null tc in MonotypeConstructor");
    this.tc=tc;
    if(parameters==null)
      this.parameters=new TypeParameters(null);
    else
      this.parameters=parameters;
    this.loc=loc;
  }

  Monotype cloneType()
  {
    return new MonotypeConstructor(tc,parameters,loc);
  }

  public gnu.bytecode.Type getJavaType()
  {
    return tc.getJavaType();
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  public Monotype resolve(TypeScope typeScope) 
  {
    return new MonotypeConstructor
      (tc.resolve(typeScope),parameters.resolve(typeScope),loc);
  }
  
  Monotype substitute(Map map)
  {
    return new MonotypeConstructor
      (tc.substitute(map),
       new TypeParameters(Monotype.substitute(map,parameters.content)),
       loc);
  }

  public TypeConstructor getTC()
  {
    return tc;
  }
  
  public TypeParameters getTP()
  {
    return parameters;
  }
  
  void typecheck()
  {
    if(tc.variance.size!=parameters.size())
      User.error(this,
		 tc.variance.size+" type parameters expected for "+tc);
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
    if(value instanceof bossa.typing.Variance)
      tc.setVariance((bossa.typing.Variance) value);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    if(loc==null)
      return tc.location();
    else
      return loc;
  }

  public String toString()
  {
    return ""+tc+parameters;
  }

  public TypeConstructor tc;
  TypeParameters parameters;
  Location loc;
}
