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

// File    : FunType.java
// Created : Thu Jul 22 09:15:17 1999 by bonniot
//$Modified: Tue Jun 13 15:21:45 2000 by Daniel Bonniot $

package mlsub.typing;

import mlsub.typing.lowlevel.Kind;

/**
 * A functional monotype.
 */
public class FunType extends Monotype
{
  public FunType(Monotype[] in, Monotype out)
  {
    this.in = (in == null ? zeroMonotypes : in);
    this.out = out;

    this.kind = new FunTypeKind(this.in.length);
  }
  
  Monotype substitute(java.util.Map map)
  {
    return new FunType(Monotype.substitute(map, in), out.substitute(map));
  }
  
  /****************************************************************
   * Functional types
   ****************************************************************/

  /** the list of input Monotypes if this type is functional */
  public Monotype[] domain()
  {
    return in;
  }

  /** the return type if this type is functional */
  public Monotype codomain()
  {
    return out;
  }

  /****************************************************************
   * low-level interface
   ****************************************************************/

  public int getId() 		{ throw new Error(); }
  public void setId(int value) 	{ throw new Error(); }
  
  Kind kind;
  
  public Kind getKind() 	  { return kind; }  
  public void setKind(Kind value) { throw new Error(); }
  
  /****************************************************************
   * Misc.
   ****************************************************************/

  public boolean equals(Object o)
  {
    if(!(o instanceof FunType))
      return false;
    FunType that = (FunType) o;
    
    return out.equals(that.out) && in.equals(that.in);
  }
  
  public String toString()
  {
    return "fun(" + bossa.util.Util.map("", ", ", "", in)
      + ")(" + out + ")";
  }

  Monotype[] in;
  Monotype out;
}
