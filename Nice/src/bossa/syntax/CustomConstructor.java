/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import java.util.*;
import mlsub.typing.*;

/**
   A user defined constructor.
 */

public class CustomConstructor extends UserOperator
{
  public CustomConstructor(LocatedString className, FormalParameters params, Block body)
  {
    super(new LocatedString("<init>", className.location()), Constraint.True, 
	  new TypeIdent(className), params, Contract.noContract);

   this.className = className;
   this.body = body;

   User.error("custom constructors are not implemented yet");
  }

  public void printInterface(java.io.PrintWriter s)
  {
    s.print("new " + className + "(" + parameters + ");\n");
  }

  protected gnu.expr.Expression computeCode()
  {
    return null;
  }

  LocatedString className;
  Block body;

}
