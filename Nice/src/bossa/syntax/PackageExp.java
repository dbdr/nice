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

// File    : PackageExp.java
// Created : Wed Jun 14 12:36:45 2000 by Daniel Bonniot
//$Modified: Wed Jun 14 15:59:04 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

/**
   Temporary expression to represent a package or a package prefix
   
   For instance, in java.lang.System.exit(0), 
   java and java.lang is represented by a package exp.
   
   @author Daniel Bonniot
 */

class PackageExp extends Expression
{
  PackageExp(String name)
  {
    this.name = name;
  }

  String getName()
  {
    return name;
  }
  
  public String toString()
  {
    return "PackageExp " + name;
  }

  /****************************************************************
   * Unimplemented methods
   ****************************************************************/

  private void error()
  {
    User.error(this, name + " is neither a valid expression nor a valid package");
  }

  void computeType()
  {
    error();
  }
  
  protected gnu.expr.Expression compile()
  {
    error();
    return null;
  }
  
  private String name;
}
