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
//$Modified: Wed Oct 13 15:01:07 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.typing.FunTypeKind;
import bossa.engine.*;

/**
 * Functional type
 */
public class FunType extends Monotype
{
  public FunType(Collection /* of Monotype */ in, Monotype out)
  {
    if(in==null)
      in=new ArrayList(0);
    this.in=in;
    this.out=out;
    this.kind=new FunTypeKind(in.size());
  }

  Monotype cloneType()
  {
    return new FunType(cloneTypes(in),out.cloneType());
  }

  /** the list of input types */
  public Collection domain()
  {
    return in;
  }

  /** the return type */
  public Monotype codomain()
  {
    return out;
  }

  /****************************************************************
   * Imperative type variables
   ****************************************************************/

  boolean allImperative(List symbols)
  {
    for(Iterator i=symbols.iterator();i.hasNext();)
      {
	TypeSymbol s=(TypeSymbol)i.next();
	if(!appear(s,true,in))
	  return false;
      }
    return true;
  }
  
  public boolean appear(TypeSymbol s, boolean imperatively)
  {
    if(imperatively)
      return false;
    else
      return appear(s,imperatively,in);
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  Monotype resolve(TypeScope typeScope)
  {
    in=Monotype.resolve(typeScope,in);
    out=out.resolve(typeScope);
    return this;
  }

  Monotype substitute(Map map)
  {
    return new FunType(Monotype.substitute(map,in),out.substitute(map));
  }

  /****************************************************************
   * Kinding
   ****************************************************************/

  private int id;
  
  public int getId() 		{ return id; }
  
  public void setId(int value) 	{ id=value; }

  FunTypeKind kind;
  
  public Kind getKind() 	{ return kind; }
  
  public void setKind(Kind value)
  {
    Internal.error("Kind set in FunType");
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

  private Collection in;
  private Monotype out;
}
