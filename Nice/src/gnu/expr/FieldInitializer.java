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
   Initializes a class field with the value passed to the default
   constructor.

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

class FieldInitializer extends Initializer
{
  FieldInitializer(ClassExp c, Declaration field, int rank)
  {
    this.classe = c;
    this.field = field;
    this.rank = rank;
  }
  
  ClassExp classe;
  Declaration field;
  int rank;

  public void emit(Compilation comp)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    code.emitPushThis();
    code.emitLoad(code.getCurrentScope().getVariable(1 + classe.numFieldsSuper + rank));
    code.emitPutField(field.field);
  }
}
