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
//$Modified: Tue Aug 29 17:00:16 2000 by Daniel Bonniot $

package mlsub.typing;

import mlsub.typing.lowlevel.Kind;

/**
 * A functional monotype.
 */
public final class FunType extends Monotype
{
  FunType(FunTypeKind kind, Monotype[] in, Monotype out)
  {
    this.in = (in == null ? Monotype.zeroMonotypes : in);
    this.out = out;
    this.kind = kind;
  }

  public FunType(Monotype[] in, Monotype out)
  {
    this(FunTypeKind.get(in == null ? 0 : in.length), in, out);
  }
  
  /** 
      Returns true if this monotype is only made of
      top-level, rigid type constructors
  */
  public boolean isRigid()
  {
    return out.isRigid() && Monotype.isRigid(in);
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

  public int getId()           { return mlsub.typing.lowlevel.Engine.INVALID; }
  public void setId(int value) { throw new Error(); }
  
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
    return toString(false, null);
  }

  public String toString(boolean isNull, String suffix)
  {
    StringBuffer res = new StringBuffer();
    // If there is a suffix, use parenthesis to disambiguate.
    if (suffix != null)
      res.append('(');
    res.append('(').append(bossa.util.Util.map("", ", ", "", in)).append(')');
    if (isNull)
      res.append('?');
    res.append("->").append(out);
    if (suffix != null)
      res.append(')').append(suffix);
    return res.toString();
  }

  private Monotype[] in;
  private Monotype out;

  /****************************************************************
   * Simplification
   ****************************************************************/

  void tag(int variance)
  {
    out.tag(variance);
    Monotype.tag(in, -1 * variance);
  }

  Monotype canonify()
  {
    out = out.canonify();
    in = Monotype.canonify(in);
    return this;
  }
}
