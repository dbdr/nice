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

/**
   An implementation of a method

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

import nice.tools.code.Types;
import nice.tools.code.Gen;
import mlsub.typing.Typing;
import mlsub.typing.TypeConstructor;
import mlsub.typing.MonotypeVar;
import gnu.bytecode.*;
import bossa.util.Debug;
import bossa.util.User;

public abstract class MethodImplementation extends Definition
  implements Function
{
  MethodImplementation (LocatedString name, Statement body, Pattern[] formals)
  {
    super(name, Node.down);
    this.body = body;
    this.formals = formals;
  }

  public MethodDeclaration getDeclaration()
  {
    return declaration;
  }

  public Pattern[] getPatterns() { return formals; }

  boolean hasThis() { return declaration.formalParameters().hasThis(); }

  void buildSymbols()
  {
    mlsub.typing.Monotype[] types = declaration.getArgTypes();
    if(formals.length != types.length)
      switch(types.length){
      case 0: User.error(this,"Method "+name+" has no parameters");
      case 1: User.error(this,"Method "+name+" has 1 parameter");
      default:User.error(this,
			 "Method "+name+" has "+types.length+" parameters");
      }
    
    MonoSymbol[] res = new MonoSymbol[formals.length];
    for(int tn = 0; tn < formals.length; tn++)
      {
	Pattern p = formals[tn];

	mlsub.typing.Monotype type;

        if (p.atAny())
          {
            // When a parameter is not dispatched on, it has the declared type
            // of that parameter in the method declaration.
            type = types[tn];
          }
        else if (p.getRuntimeTC() != null)
	  {
	    mlsub.typing.AtomicKind v = p.tc.variance;
	    p.getRuntimeTC().setVariance(v);
	    type = new mlsub.typing.MonotypeConstructor(p.getRuntimeTC(), mlsub.typing.MonotypeVar.news(v.arity()));
	    type.setKind(v);

	    type = bossa.syntax.Monotype.sure(type);
	  }
	else
	  {
	    if (p.name == null)
	      // anonymous pattern
	      type = new MonotypeVar(types[tn].toString()+ "(argument_" + tn+")");
	    else
	      type = new MonotypeVar(types[tn].toString()+ "(" + p.name + ")");
	  }

	res[tn] = new MonoSymbol(p.name, type);
      }
    scope.addSymbols(res);
    parameters = res;
  }
  
  void resolveBody()
  {
    if (hasThis())
      Node.thisExp = new SymbolExp(parameters[0], location());

    try {
      body = bossa.syntax.dispatch.analyse
        (body, scope, typeScope, !Types.isVoid(declaration.getReturnType()));
    }
    finally {
      Node.thisExp = null;
    }
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  public mlsub.typing.Monotype getExpectedType()
  {
    return declaration.getReturnType();
  }

  public void checkReturnedType(mlsub.typing.Polytype returned)
    throws Function.WrongReturnType
  {
    try {
      Typing.leq(returned, declaration.getReturnType());
    }
    catch (mlsub.typing.TypingEx e) {
      throw new Function.WrongReturnType(e, declaration.getReturnType());
    }
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  protected abstract Type[] javaArgTypes();

  gnu.expr.ReferenceExp ref;
  gnu.expr.LambdaExp compiledMethod;

  public gnu.expr.ReferenceExp getRefExp()
  {
    if (ref == null)
      {
        ref = createRef();
        Gen.setMethodBody(compiledMethod, body.generateCode());
      }

    return ref;
  }

  public void compile()
  {
    if (Debug.codeGeneration)
      Debug.println("Compiling method body " + this);

    getRefExp();
  }

  private gnu.expr.ReferenceExp createRef ()
  {
    createMethod(name.toString());
    gnu.expr.ReferenceExp ref = module.addMethod(compiledMethod, true);

    return ref;
  }

  private void createMethod (String bytecodeName)
  {
    compiledMethod = 
      Gen.createMethod(bytecodeName, 
		       javaArgTypes(),
		       declaration.javaReturnType(),
		       parameters,
		       true, false);

    compiledMethod.addBytecodeAttribute
      (new MiscAttr("definition", declaration.getFullName().getBytes()));
    compiledMethod.addBytecodeAttribute
      (new MiscAttr("patterns", 
		    Pattern.bytecodeRepresentation(formals).getBytes()));
  }

  /****************************************************************
   * Fields
   ****************************************************************/

  protected MethodDeclaration declaration;
  protected MonoSymbol[] parameters;
  protected Pattern[] formals;
  protected Statement body;
}