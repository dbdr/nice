/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.link;

import bossa.syntax.JavaMethod;
import bossa.syntax.Pattern;
import nice.tools.typing.Types;

/**
   An alternative for calling an existing Java method.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

class JavaAlternative extends Alternative
{
  JavaAlternative(JavaMethod method)
  {
    super(method.getName().toString(), patterns(method));

    this.method = method;
  }

  JavaMethod method;

  static Pattern[] patterns(bossa.syntax.MethodDeclaration method)
  {
    mlsub.typing.Monotype[] parameters = Types.parameters(method.getType());

    Pattern[] res = new Pattern[parameters.length];

    for (int i = 0; i < res.length; i++)
      res[i] = bossa.syntax.dispatch.createPattern
        (null,
         Types.concreteConstructor(parameters[i]),
         Types.isSure(parameters[i]));

    return res;
  }

  public gnu.expr.Expression methodExp()
  {
    return method.getCode();
  }
}
