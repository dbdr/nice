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

// File    : ItfType.java
// Created : Thu Jul 08 11:19:31 1999 by bonniot
//$Modified: Fri Jul 09 15:46:33 1999 by bonniot $
// Description : Interface considered as a type

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class ItfType extends Type
{
  public ItfType(InterfaceDefinition i)
  {
    this.itf=i;
  }

  Type instantiate(Collection parameters)
  {
    Internal.error("Itf.instantiate");
    return null;
  }

  Type resolve(TypeScope s)
  {
    return this;
  }

  public String toString()
  {
    return itf.name.toString();
  }

  private InterfaceDefinition itf;
}
