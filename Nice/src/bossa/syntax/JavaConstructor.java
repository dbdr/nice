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

import gnu.expr.*;
import gnu.expr.Expression;

/**
   A Java constructor method.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

class JavaConstructor extends JavaMethod
{
  JavaConstructor
    (
     String methodName,
     // Nice information
     LocatedString name, 
     mlsub.typing.Monotype returnType,
     mlsub.typing.Monotype[] parameters
    )
  {
    super(null, methodName, null, name, null, returnType, parameters);
  }

  protected Expression computeCode()
  {
    return new QuoteExp(new InstantiateProc(reflectMethod));
  }
  
  Expression getConstructorInvocation()
  {
    return new QuoteExp(new InitializeProc(reflectMethod));
  }
}
