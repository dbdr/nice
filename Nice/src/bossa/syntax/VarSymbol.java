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

public abstract class VarSymbol extends Node implements Located
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

  final boolean isFieldAccess()
  {
    return getFieldAccessMethod() != null;
  }

  final boolean isNonStaticFieldAccess()
  {
    FieldAccess access = getFieldAccessMethod();
    return access != null && ! access.isStatic();
  }

  final boolean isStaticFieldAccess()
  {
    FieldAccess access = getFieldAccessMethod();
    return access != null && access.isStatic();
  }

  FieldAccess getFieldAccessMethod()
  {
    return null;
  }

  public MethodDeclaration getMethodDeclaration()
  {
    return null;
  }

  boolean isIgnored()
  {
    return false;
  }

  void checkSpecialRequirements(Expression[] arguments)
  {
    // Do nothing by default.
  }

  public Definition getDefinition()
  {
    return null;
  }

  abstract Polytype getType();

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

  /** This returns a generic explanation.
      A more precise message should be returned if possible in subclasses.
  */
  String explainWhyMatchFails(Arguments arguments)
  {
    return "Incorrect call to " + name;
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
    
    if (isThis)
      return new gnu.expr.ThisExp(decl);
    else
      return new gnu.expr.ReferenceExp(name == null ? null : name.toString(), 
				       decl);
  }

  /** @return code that accesses this variable, when it is used
      as the function of a call. */
  gnu.expr.Expression compileInCallPosition()
  {
    // Default implementation.
    return compile();
  }

  static gnu.expr.Expression[] compile(VarSymbol[] syms)
  {
    gnu.expr.Expression[] res = new gnu.expr.Expression[syms.length];

    for (int i = 0; i < syms.length; i++)
      res[i] = syms[i].compile();

    return res;
  }

  public void setDeclaration(gnu.expr.Declaration declaration)
  {
    setDeclaration(declaration, false);
  }

  public void setDeclaration(gnu.expr.Declaration declaration, boolean isThis)
  {
    this.decl = declaration;
    this.isThis = isThis;
    if (name != null) 
      name.location.write(this.decl);
    this.decl.setCanRead(true);
    this.decl.setCanWrite(true);
  }
  
  gnu.expr.Declaration getDeclaration()
  {
    return decl;
  }
  
  private gnu.expr.Declaration decl = null;
  private boolean isThis;
}
