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
//$Modified: Tue Jun 06 18:14:16 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

import mlsub.typing.*;
import mlsub.typing.Monotype;
import mlsub.typing.FunTypeKind;

/**
 * Functional type
 */
public class FunType extends bossa.syntax.Monotype
{
  public FunType(List /* of Monotype */ in, bossa.syntax.Monotype out)
  {
    if(in==null)
      in=new ArrayList(0);
    this.in=in;
    this.out=out;
  }

  /*
  Monotype cloneType()
  {
    return new FunType(cloneTypes(in),out.cloneType());
  }
  */
  /****************************************************************
   * Scoping
   ****************************************************************/

  public Monotype resolve(TypeScope typeScope)
  {
    return new mlsub.typing.FunType
      (bossa.syntax.Monotype.resolve(typeScope,in),
       out.resolve(typeScope));
  }

  bossa.syntax.Monotype substitute(Map map)
  {
    return new bossa.syntax.FunType
      (bossa.syntax.Monotype.substitute(map,in),out.substitute(map));
  }

  boolean containsAlike()
  {
    return bossa.syntax.Monotype.containsAlike(in) || out.containsAlike();
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
    return "fun"+Util.map("(",", ",")",true,in)+"("+out+")";
      //return "("+toStringExtern()+")";
  }
  
  public String toStringExtern()
  {
    return Util.map("(",", ",")",true,in)+"->"+out.toStringExtern();
  }

  public boolean equals(Object o)
  {
    if(!(o instanceof FunType))
      return false;
    FunType that = (FunType) o;
    
    return out.equals(that.out) && in.equals(that.in);
  }
  
  private List in;
  private bossa.syntax.Monotype out;
}
