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
//$Modified: Wed Jul 26 14:45:00 2000 by Daniel Bonniot $

package mlsub.typing;

import java.util.*;

import mlsub.typing.lowlevel.*;

/**
 * A monotype, build by application of
 * a type constructor to type parameters.
 */
public final class MonotypeConstructor extends Monotype
{
  /**
   * Constructs a monotype by application of the type constructor
   * to the type parameters.
   *
   * @param tc the type constructor
   * @param parameters the type parameters
   */
  public MonotypeConstructor(TypeConstructor tc, Monotype[] parameters)
  throws BadSizeEx
  {
    this.tc = tc;
    this.parameters = parameters;
    if(parameters != null && parameters.length == 0)
      throw new Error("Not optimal");
    
    // variance is not known yet for java classes
    // ill-formedness shall be discovered later, hopefully
    if (tc.variance != null)
      {
	int len = (parameters==null ? 0 : parameters.length);
	if (tc.arity() != len)
	  throw new BadSizeEx(tc.arity(), len);
      }
  }

  public TypeConstructor getTC()
  {
    return tc;
  }
  
  public Monotype[] getTP()
  {
    return parameters;
  }
  
  /** 
      Returns true if this monotype is only made of
      top-level, rigid type constructors
  */
  public boolean isRigid()
  {
    return tc.isRigid() && Monotype.isRigid(parameters);
  }
  
  /**
   * Perform type symbol substitution inside the monotype.
   *
   * Does not need to create a new object, but must not
   * imperatively modify the monotype.
   *
   * @param map a map from TypeSymbols to TypeSymbols
   * @return a monotype with substitution performed
   */
  Monotype substitute(Map map)
  {
    Object newTC = map.get(tc);
    
    return new MonotypeConstructor
      ((newTC==null ? tc : (TypeConstructor) newTC),
       Monotype.substitute(map, parameters));
  }
  
  /****************************************************************
   * low-level interface
   ****************************************************************/

  public int getId() 		{ throw new Error(); }
  public void setId(int value) 	{ throw new Error(); }
  
  public Kind getKind() 	  { return tc.variance; }  
  public void setKind(Kind value) { throw new Error(); 
  // or if(value instanceof bossa.typing.Variance)
  // tc.setVariance((bossa.typing.Variance) value)
  }
  
  public boolean equals(Object o)
  {
    if(!(o instanceof MonotypeConstructor))
      return false;
    MonotypeConstructor that = (MonotypeConstructor) o;
    
    return 
      tc.equals(that.tc) 
      && (parameters==null && that.parameters==null 
	  || parameters.equals(that.parameters));
  }
  
  public String toString()
  {
    return ""+tc+bossa.util.Util.map("<", ", ", ">", parameters);
  }

  public TypeConstructor tc;
  Monotype[] parameters;
}
