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

import nice.tools.typing.Types;
import nice.tools.code.Gen;
import mlsub.typing.Typing;
import mlsub.typing.TypeConstructor;
import mlsub.typing.Monotype;
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
  
  /** Where no patterns are present, add those corresponding to the method
      declaration.
  */
  void addPatterns()
  {
    Monotype[] parameters = Types.parameters(declaration.getType());
    for (int i = 0; i < formals.length; i++)      
      if (formals[i].tc == null)
        {
          formals[i].tc = Types.concreteConstructor(parameters[i]);

          if (formals[i].tc == null && Types.isSure(parameters[i]))
            formals[i].tc = PrimitiveType.sureTC;

          if (Types.isPrimitive(formals[i].tc))
            formals[i].tc = null;
        }
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

    // Register this alternative for the link test
    alternative = new bossa.link.SourceAlternative(this);
  }

  private bossa.link.Alternative alternative;

  bossa.link.Alternative getAlternative() { return alternative; }

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

    createSerializationMethod();
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
      (new MiscAttr("definition", declaration.getAllFullNames().getBytes()));
    compiledMethod.addBytecodeAttribute
      (new MiscAttr("patterns", 
		    Pattern.bytecodeRepresentation(formals).getBytes()));
  }

  abstract TypeConstructor firstArgument();

  /**
     If the method implemented corresponds to readObject or writeObject,
     create private member methods in the class of the first argument,
     so that the Java serialization process picks them up.
  */
  private void createSerializationMethod()
  {
    final int arity = formals.length;
    String name = this.name.toString();
    boolean isPrivate;

    if ((arity == 2) && (name.equals("writeObject") || name.equals("readObject")))
      isPrivate = true;
    else if ((arity == 1) && (name.equals("writeReplace") || name.equals("readResolve")))
      isPrivate = false;
    else 
      return;

    ClassDefinition def = ClassDefinition.get(firstArgument());
    if (def == null || ! (def.getImplementation() instanceof NiceClass))
      return;

    NiceClass c = (NiceClass) def.getImplementation();

    gnu.expr.Expression[] params = new gnu.expr.Expression[arity];
    gnu.expr.LambdaExp method = Gen.createMemberMethod
          (name.toString(), 
           c.getClassExp().getType(),
           arity==2 ? new Type[]{declaration.javaArgTypes()[1]} : null,
           declaration.javaReturnType(),
           params);

    Gen.setMethodBody(method, new gnu.expr.ApplyExp(getRefExp(), params));

    c.getClassExp().addMethod(method, isPrivate);

  }

  /****************************************************************
   * Fields
   ****************************************************************/

  protected MethodDeclaration declaration;
  protected MonoSymbol[] parameters;
  protected Pattern[] formals;
  protected Statement body;
}
