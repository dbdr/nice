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

import java.util.*;
import bossa.util.*;

import mlsub.typing.Polytype;

public class ExpLocalVariable extends Expression
{
  public ExpLocalVariable(LocatedString name, Expression value, 
			 boolean constant, Monotype type)
  {
    this.variable = new Block.LocalVariable(name, type, constant, null);
    this.initValue = value;
    setLocation(name.location());
  }

  void computeType()
  {
    this.type = variable.left.getType();
  }

  protected gnu.expr.Expression compile()
  {
    return compileAssign(initValue.generateCode());
  }

  gnu.expr.Declaration getDeclaration()
  {
    return variable.getSymbol().getDeclaration();
  }

  Block.LocalVariable variable;
  Expression initValue;
}
