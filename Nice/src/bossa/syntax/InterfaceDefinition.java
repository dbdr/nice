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
//$Modified: Sat Jul 24 13:36:26 1999 by bonniot $
// Description : Abstract syntax for a class definition

package bossa.syntax;

import bossa.util.*;

public class InterfaceDefinition extends Node//TypeSymbol 
  implements Definition,TypeSymbol
{
  public InterfaceDefinition(LocatedString name)
  {
    this.name=name;
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

  public TypeSymbol resolve(TypeScope scope)
  {
    return this;
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
}
