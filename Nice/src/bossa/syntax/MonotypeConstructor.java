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
//$Modified: Fri Aug 13 12:10:23 1999 by bonniot $
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
  public MonotypeConstructor(TypeConstructor tc, TypeParameters parameters,
			     Location loc)
  {
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

  /****************************************************************
   * Scoping
   ****************************************************************/

  Monotype resolve(TypeScope typeScope) 
  {
    tc=tc.resolve(typeScope);
    parameters=parameters.resolve(typeScope);

    // Check the monotype is well-formed,
    // ie all the parameters in imperative positions are imperative
    User.error(!tc.variance.wellFormed(parameters),
	       this,"Type parameters must be imperative");

    return this;
  }
  
  Monotype substitute(Map map)
  {
    return new MonotypeConstructor
      (tc,
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
  
  /****************************************************************
   * Imperative type variables
   ****************************************************************/

  public boolean isImperative()
  {
    Iterator i=parameters.iterator();
    while(i.hasNext())
      if(!((Monotype)i.next()).isImperative())
	return false;
    return true;
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
