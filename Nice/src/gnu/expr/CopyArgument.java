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

package gnu.expr;

/**
   Copy the value of a previous argument in the same function call.

   Used to implement optional arguments whose default value refers to
   another argument.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class CopyArgument extends Expression
{
  public CopyArgument (Declaration argument)
  {
    this.argument = argument;
  }

  public void compile(gnu.expr.Compilation comp, gnu.expr.Target target) 
  {
    gnu.bytecode.Variable value = argument.getCopyVariable();
    comp.getCode().emitLoad(value);
    target.compileFromStack(comp, value.getType());
  }

  public void print(gnu.mapping.OutPort out)
  {}

  Declaration argument;
}
