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

// File    : ClassType.java
// Created : Thu Jul 08 11:51:09 1999 by bonniot
//$Modified: Fri Jul 09 15:44:42 1999 by bonniot $
// Description : the basic type that represents a class

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class ClassType extends Type
{
  public ClassType(ClassDefinition d)
  {
    this.definition=d;
  }

  VarScope memberScope()
  {
    return definition.getScope();
  }

  Type instantiate(Collection c)
  {
    Internal.error("ClassType.instantiate");
    return null;
  }

  Type resolve(TypeScope s)
  {
    return this;
  }

  public String toString()
  {
    return definition.name.toString();
  }

  ClassDefinition definition;
}
