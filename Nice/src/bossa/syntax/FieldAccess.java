/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;

import nice.tools.code.*;
import gnu.expr.*;

/**
   A field access.
   
   In terms of scoping, this is the symbol
   that is returned when the access to a field is done,
   either a 'get' or a 'set'.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
abstract class FieldAccess extends MethodDeclaration
{
  public FieldAccess(LocatedString name, 
		     bossa.syntax.Constraint constraint,
		     FormalParameters parameters,
		     bossa.syntax.Monotype returnType)
  {
    super(name, constraint, returnType, parameters);
  }
  
  FieldAccess(LocatedString name,
	      mlsub.typing.Constraint cst,
	      mlsub.typing.Monotype[] parameters, 
	      mlsub.typing.Monotype returnType)
  {
    super(name, cst, parameters, returnType);
  }

  /** 
   * true if this method represent the access to the field of an object.
   */
  public boolean isFieldAccess() { return true; }

  /****************************************************************
   * Code generation
   ****************************************************************/

  class UsingAsValue extends Error {}

  protected gnu.expr.Expression computeCode()
  {
    throw new UsingAsValue();
  }

  protected Declaration fieldDecl;
  
  public gnu.expr.Expression compileAccess(gnu.expr.Expression[] arguments)
  {
    if (arguments.length == 0)
      return Inline.inline(new GetFieldProc(fieldDecl));
    else
      return Inline.inline(new GetFieldProc(fieldDecl), arguments[0]);
  }
  
  public gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    return Inline.inline
      (new SetStaticFieldProc(fieldDecl), value);
  }

  public gnu.expr.Expression compileAssign(gnu.expr.Expression parameter, 
					   gnu.expr.Expression value)
  {
    return Inline.inline
      (new SetFieldProc(fieldDecl), parameter, value);
  }
}

