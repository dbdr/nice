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

// File    : VoidConstantExp.java
// Created : Mon Jul 05 17:30:56 1999 by bonniot
//$Modified: Thu Aug 26 14:59:51 1999 by bonniot $
// Description : An integer constant

package bossa.syntax;

import bossa.util.*;

public class VoidConstantExp extends ConstantExp
{
  public VoidConstantExp()
  {
    className="void";
  }

  public String toString()
  {
    return "()";
  }
}
