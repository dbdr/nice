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

import java.util.*;
import bossa.util.*;

/**
   Identifier supposed to be a variable (not a type).

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class IdentExp extends Expression
{
  public IdentExp(LocatedString i)
  {
    this.ident = i;
    setLocation(i.location());
  }
  
  /****************************************************************
   * Unimplemented methods
   ****************************************************************/

  void computeType()
  {
    Internal.error(this, "computeType in IdentExp (" + this + ")");
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    Internal.error("compile in IdentExp");
    throw new Error();
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public LocatedString getIdent()
  {
    return ident;
  }
  
  public String toString()
  {
    return ident.toString();
  }

  protected LocatedString ident;
  
  /** Resolve to a ClassExp or a PackageExp if ident is not known. */
  boolean enableClassExp;

  /**
     Force production of overloaded symbol exp, even if there is just one case.
   */
  boolean alwaysOverloadedSymbol;

  /** This ident is the function part of an infix call. */
  boolean infix() { return infix; }
  void setInfix() { infix = true; }

  private boolean infix;
}
