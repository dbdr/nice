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

// File    : InterfaceDefinition.java
// Created : Thu Jul 01 17:00:14 1999 by bonniot
//$Modified: Fri Aug 27 17:26:43 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.engine.BitVector;
import bossa.typing.Variance;

/** 
 * Abstract syntax for an interface definition
 */
public class InterfaceDefinition extends Node
  implements Definition,TypeSymbol
{
  public InterfaceDefinition(LocatedString name, Collection typeParameters, List extensions)
  {
    super(Node.forward);
    this.name=name;
    this.parameters=typeParameters;
    this.variance=new Variance(typeParameters.size());
    bossa.engine.Engine.getConstraint(variance).addInterface(this);
    addTypeSymbol(this);
    this.extensions=addChildren(extensions);
  }

  public TypeSymbol cloneTypeSymbol()
  {
    return new InterfaceDefinition(name,parameters,extensions);
  }
  
  public boolean hasName(LocatedString s)
  {
    return name.equals(s);
  }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  void typecheck()
  {
    bossa.typing.Typing.assertLeq(this,extensions);
  }
  
  /****************************************************************
   * Constraints
   ****************************************************************/

  public BitVector impv=new BitVector();
  
  public void resize(int newSize)
  {
    impv.truncate(newSize);
  }
  
  public boolean imp(int id)
  {
    return impv.get(id);
  }
  
  public void addImp(int id)
  {
    impv.set(id);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    String res="interface ";
    res=res+name;
    return res+";\n";
  }

  public LocatedString getName()
  {
    return name;
  }

  public Location location()
  {
    return name.location();
  }

  LocatedString name;
  Collection /* of TypeSymbol */ parameters;
  public Variance variance;
  private List /* of Interface */ extensions; // the surinterfaces
}
