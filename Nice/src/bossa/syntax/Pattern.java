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

// File    : Pattern.java
// Created : Mon Jul 05 14:36:52 1999 by bonniot
//$Modified: Fri Jul 23 13:17:23 1999 by bonniot $
// Description : Syntactic pattern for method bodies declaration

package bossa.syntax;

import java.util.Collection;
import bossa.util.*;

public class Pattern
{
  public Pattern(LocatedString name)
  {
    this(name,null);
  }

  public Pattern(LocatedString name, TypeConstructor tc)
  {
    this.name=name;
    this.typeConstructor=tc;
  }

  public String toString()
  {
    String res=name.toString();
    if(typeConstructor!=null)
      res=res+"@"+typeConstructor;
    return res;
  }

  /** expresses that this pattern was not a true one.
   *  @see MethodBodyDefinition
   */
  boolean thisAtNothing()
  {
    return typeConstructor==null && name.content.equals("this");
  }

  LocatedString name;
  TypeConstructor typeConstructor;
}
