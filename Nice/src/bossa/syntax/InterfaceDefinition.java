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
//$Modified: Tue Sep 21 17:54:30 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.engine.BitVector;
import bossa.typing.Variance;

/** 
 * Abstract syntax for an interface definition.
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
    variance.getConstraint().addInterface(this);
    addTypeSymbol(this);
    this.extensions=addChildren(extensions);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }
  
  /****************************************************************
   * The top interface
   ****************************************************************/
  
  private InterfaceDefinition(String name, int arity)
  {
    super(Node.forward);
    this.name=new LocatedString(name,Location.nowhere());
    this.parameters=null;
    this.variance=new Variance(arity);
    variance.getConstraint().addInterface(this);
    addTypeSymbol(this);
    extensions=null;
  }
  
  static private Vector tops=new Vector();
  
  static InterfaceDefinition top(int arity)
  {
    if(arity>=tops.size())
      tops.setSize(arity+1);
    InterfaceDefinition res=(InterfaceDefinition)tops.get(arity);
    if(res==null)
      {
	res=new InterfaceDefinition("Top"+arity,arity);
	tops.set(arity,res);
      }
    return res;
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
    bossa.typing.Typing.assertLeq(this,top(parameters.size()));
  }
  
  /****************************************************************
   * Constraints
   ****************************************************************/

  public BitVector impv=new BitVector(),absv=new BitVector();
  
  public void resize(int newSize)
  {
    impv.truncate(newSize);
    absv.truncate(newSize);
    approx.setSize(newSize);
  }
  
  public boolean imp(int id)
  {
    return impv.get(id);
  }
  
  public void addImp(int id)
  {
    impv.set(id);
  }
  
  public boolean abs(int id)
  {
    return absv.get(id);
  }
  
  public void addAbs(int id)
  {
    absv.set(id);
  }
  
  private Vector approx=new Vector();
  
  public void setApprox(int node, int value)
  {
    Internal.error(node>=approx.size(),"Incorrect node "+node+" in "+this);
    approx.set(node,new Integer(value));
  }
  
  public int getApprox(int node)
  {
    return ((Integer)approx.get(node)).intValue();
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

  private int id;
  public void setId(int id)
  {
    this.id=id;
  }
  public int getId()
  {
    return id;
  }
}
