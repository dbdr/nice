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
// Created : Fri Jul 02 17:41:24 1999 by bonniot
//$Modified: Fri Jul 23 16:47:04 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Functional type
 */
public class FunType extends Monotype
{
  public FunType(Collection /* of Type */ in, Monotype out)
  {
    this.in=in;
    this.out=out;
  }

  Monotype cloneType()
  {
    return new FunType(cloneTypes(in),out.cloneType());
  }

  /** the list of input types */
  Collection domain()
  {
    return in;
  }

  /** the return type */
  Monotype codomain()
  {
    return out;
  }

  Monotype resolve(TypeScope typeScope)
  {
    in=resolve(typeScope,in);
    out=out.resolve(typeScope);
    return this;
  }

  Monotype substitute(Map map)
  {
    return new FunType(Monotype.substitute(map,in),out.substitute(map));
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return out.location().englobe(in);
  }

  public String toString()
  {
    return "("+toStringExtern()+")";
  }
  
  public String toStringExtern()
  {
    return Util.map("(",", ",")",true,in)+"->"+out.toStringExtern();
  }

  private Collection in;
  private Monotype out;
}
