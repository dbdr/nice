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

import gnu.bytecode.*;

/**
   A constructor method.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class ConstructorExp extends LambdaExp
{
  public ConstructorExp(Declaration thisDecl)
  {
    this.thisDecl = thisDecl;
    this.classType = (ClassType) thisDecl.getType();
    thisDecl.context = this;
    this.primary = true;
  }

  public ConstructorExp(ClassType classType)
  {
    this.classType = classType;
    this.primary = false;
  }

  public void setSuperCall(Expression superCall)
  {
    this.superCall = superCall;
  }

  private Declaration thisDecl;
  private ClassType classType;
  private Expression superCall;
  private boolean primary;

  ClassType getClassType() { return classType; }

  public Method getMainMethod()
  {
    if (primMethods == null)
      addMethodFor(classType, null, null);
    return primMethods[0];
  }

  void addMethodFor (ClassType ctype, Compilation comp, ObjectType closureEnvType)
  {
    if (primMethods != null)
      return;

    closureEnv = declareThis(ctype);

    Type[] args = new Type[min_args];
    Declaration var = firstDecl();
    for (int itype = 0; var != null; var = var.nextDecl())
      args[itype++] = var.getType().getImplementationType();

    // Make sure the signature is unique
    while (ctype.getDeclaredMethod("<init>", args) != null)
      {
        Type[] newArgs = new Type[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = Type.int_type;
        args = newArgs;
        dummyArgs++;
        addDeclaration("dummy");
      }

    Method method = ctype.addMethod
      ("<init>", args, Type.void_type, Access.PUBLIC);
    primMethods = new Method[] { method };

    addAttributes(method);
  }

  /** Number of dummy arguments added to make the signature unique. */
  int dummyArgs = 0;

  void enterFunction (Compilation comp)
  {
    if (primary)
      // The super call has to come before anything else.
      superCall.compile(comp, Target.Ignore);

    // Do the normal stuff.
    super.enterFunction(comp);
  }
}
