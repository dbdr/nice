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
//$Modified: Fri Aug 27 11:20:36 1999 by bonniot $
// Description : Interface considered as a type

package bossa.syntax;

import java.util.*;
import bossa.util.*;

//TODO: remove ?
public abstract class ItfType extends Polytype
{
  public ItfType(InterfaceDefinition i)
  {
    this.itf=i;
  }

  Type cloneType()
  {
    return this;
  }

  Type resolve()
  {
    return this;
  }

  public String toString()
  {
    return itf.name.toString();
  }

  private InterfaceDefinition itf;
}
