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
//$Modified: Mon Dec 13 15:38:42 1999 by bonniot $

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
    super(Node.global);
    this.name=name;
    this.parameters=typeParameters;
    this.variance=Variance.make(typeParameters.size());
    addTypeSymbol(this);
    this.extensions=addChildren(extensions);
    itf=variance.newInterface();
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
    this.variance=Variance.make(arity);
    addTypeSymbol(this);
    extensions=null;
    itf=variance.newInterface();
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
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
    bossa.typing.Typing.assertLeq(this,extensions);
    bossa.typing.Typing.assertLeq(this,top(parameters.size()));
  }
  
  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print("interface "
	    +name
	    +Util.map("<",", ",">",parameters)
	    +Util.map(" extends ",", ","",extensions)
	    +";\n");
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public void compile()
  {
    // Nothing
  }

  /****************************************************************
   * Module
   ****************************************************************/
  
  private bossa.modules.Module module;
  
  public void setModule(bossa.modules.Module module)
  {
    this.module = module;
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    String res=name.toString();
    if(bossa.typing.Typing.dbg) res+="("+itf+")";
    return res;
  }

  public LocatedString getName()
  {
    return name;
  }

  public Location location()
  {
    return name.location();
  }

  public int itf; // The associated lowlevel interface
  LocatedString name;
  Collection /* of TypeSymbol */ parameters;
  public Variance variance;
  private List /* of Interface */ extensions; // the surinterfaces
}
