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
//$Modified: Tue Jul 27 12:39:23 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/** Abstract syntax for an interface definition
 *
 */
public class InterfaceDefinition extends Node
  implements Definition,TypeSymbol
{
  public InterfaceDefinition(LocatedString name, Collection typeParameters)
  {
    this.name=name;
    this.parameters=typeParameters;
    
    //    this.type=new ItfType(this); // we have to do this 
                                 // since this cannot be used 
                                 // in the above line
  }

  public boolean hasName(LocatedString s)
  {
    return name.equals(s);
  }

  /****************************************************************
   * 
   ****************************************************************/

  TypeSymbol resolve(TypeScope scope)
  {
    return this;
  }

  static Collection resolve(TypeScope typeScope, Collection c)
  {
    Collection res=new ArrayList(c.size());
    Iterator i=c.iterator();
    while(i.hasNext())
      res.add(((TypeConstructor)i.next()).resolve(typeScope));

    return res;
  }

void resolveScope()
  {
    
  }

  VarScope memberScope()
  {
    return null;
  }

  void typecheck()
  {
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
}
