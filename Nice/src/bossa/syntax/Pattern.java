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
//$Modified: Fri Jul 09 21:16:42 1999 by bonniot $
// Description : Syntactic pattern for method bodies declaration

package bossa.syntax;

import java.util.Collection;
import bossa.util.*;

public class Pattern
{
  public Pattern(Ident name)
  {
    this.name=name;
    this.className=null;
    this.typeParameters=null;
  }

  public Pattern(Ident name, Ident className, Collection typeParameters)
  {
    this.name=name;
    this.className=className;
    this.typeParameters=typeParameters;
  }

  public String toString()
  {
    String res=name.toString();
    if(className!=null)
      res=res+"@"+className+
	Util.map("<",", ",">",typeParameters);
    return res;
  }

  /** expresses that this pattern was not a true one.
   *  @see MethodBodyDefinition
   */
  boolean thisAtNothing()
  {
    return className==null && name.equals("this");
  }

  Ident name;
  Ident className;
  Collection typeParameters;
}
