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
import nice.tools.code.*;

import mlsub.typing.TypeConstructor;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

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
		     Constraint constraint,
		     Monotype returnType,
		     FormalParameters parameters)
  {
    super(name, constraint, returnType, parameters);
  }
  
  /** 
   * true if this method represent the access to the field of an object.
   */
  public boolean isFieldAccess() { return true; }

  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    Internal.error("Should not be used as a real method: " + this);
    return null;
  }

  protected Field field;
  
  public gnu.expr.Expression compileAccess(Arguments arguments)
  {
    if (arguments.size() == 0)
      return Inline.inline(new GetFieldProc(field));
    else
      return Inline.inline(new GetFieldProc(field), 
			   arguments.getExp(0).generateCode());
  }
  
  public gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    return Inline.inline
      (new SetStaticFieldProc(field), value);
  }

  public gnu.expr.Expression compileAssign(Expression parameter, 
					   gnu.expr.Expression value)
  {
    return Inline.inline
      (new SetFieldProc(field), parameter.generateCode(), value);
  }
}

