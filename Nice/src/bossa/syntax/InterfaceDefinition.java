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
//$Modified: Mon Jul 12 11:23:26 1999 by bonniot $
// Description : Abstract syntax for a class definition

package bossa.syntax;

import bossa.util.*;

public class InterfaceDefinition extends TypeSymbol implements Definition
{
  public InterfaceDefinition(Ident name)
  {
    super(name);
    //    this.type=new ItfType(this); // we have to do this 
                                 // since this cannot be used 
                                 // in the above line
  }

  void resolveScope()
  {
    
  }

  VarScope memberScope()
  {
    return null;
  }

  public String toString()
  {
    String res="interface ";
    res=res+name;
    return res+";\n";
  }
}
