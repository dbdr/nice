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

import mlsub.typing.Polytype;

/**
   A variable (local, field of a class, parameter of a method or function).

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

abstract class VarSymbol extends Node implements Located
{
  public VarSymbol(LocatedString name)
  {
    super(Node.upper);
    this.name = name;
    addSymbol(this);
  }

  public boolean hasName(LocatedString i)
  {
    return this.name.equals(i);
  }

  boolean isAssignable()
  {
    return true;
  }

  abstract Polytype getType();

  /**
   * Maps getType over a collection of VarSymbols
   *
   * @param varsymbols the colleciton of Varsymbols
   * @return the collection of their Types
   */
  static Polytype[] getType(Collection varsymbols)
  {
    Iterator i=varsymbols.iterator();
    Polytype[] res = new Polytype[varsymbols.size()];

    int n = 0;
    while(i.hasNext())
      res[n++] = ((VarSymbol) i.next()).getType();

    return res;
  }

  /****************************************************************
   * Overloading resolution
   ****************************************************************/

  /**
     @return
     0 : doesn't match
     1 : wasn't even a function
     2 : matches
  */
  int match(Arguments arguments)
  {
    return 1;
  }

  /****************************************************************
   * Cloning types
   ****************************************************************/

  // explained in OverloadedSymbolExp

  abstract void makeClonedType();
  abstract void releaseClonedType();
  abstract Polytype getClonedType();
  
  /****************************************************************
   * Misc.
   ****************************************************************/

  public Location location()
  {
    return name.location();
  }
  
  LocatedString name;

  public final LocatedString getName() { return name; }

  /****************************************************************
   * Code generation
   ****************************************************************/

  /** @return code that accesses this variable. */
  gnu.expr.Expression compile()
  {
    // Allow a sub-class to compute decl a la demande in getDeclaration().
    gnu.expr.Declaration decl = getDeclaration();

    if (decl == null)
      Internal.error(this + " has no bytecode declaration");
    
    return new gnu.expr.ReferenceExp(name.toString(), decl);
  }

  public void setDeclaration(gnu.expr.Declaration declaration)
  {
    this.decl = declaration;
    if (name != null) this.decl.setLine(name.location().getLine());
    this.decl.setCanRead(true);
    this.decl.setCanWrite(true);
  }
  
  gnu.expr.Declaration getDeclaration()
  {
    return decl;
  }
  
  private gnu.expr.Declaration decl = null;
}
