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
//$Modified: Fri Jul 09 20:13:05 1999 by bonniot $
// Description : Functional type

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class FunType extends Type
{
  public FunType(Collection/* of Type */ in, Type out)
  {
    this.in=in;
    this.out=out;
  }

  /** the list of input types */
  Collection domain()
  {
    return in;
  }

  /** the return type */
  Type codomain()
  {
    return out;
  }

  Type instantiate(Collection parameters)
  {
    Internal.error("to implement");
    return null;
  }

  Type resolve(TypeScope s)
  {
    in=resolve(s,in);
    out=out.resolve(s);
    return this;
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
  private Type out;
}
