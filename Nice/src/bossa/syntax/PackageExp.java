/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;

/**
   Temporary expression to represent a package or a package prefix.
   
   For instance, in java.lang.System.exit(0), 
   java and java.lang is represented by a package exp.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

class PackageExp extends Expression
{
  PackageExp(String name)
  {
    this.name = new StringBuffer(name);
  }

  LocatedString locatedName()
  {
    return new LocatedString(name.toString(), location());
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
    User.error(this, 
	       name + " is neither a valid expression nor a valid package");
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
  
  StringBuffer name;
}
