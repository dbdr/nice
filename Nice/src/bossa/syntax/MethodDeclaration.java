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

import mlsub.typing.*;
import mlsub.typing.Polytype;
import mlsub.typing.Monotype;
import mlsub.typing.FunType;
import mlsub.typing.Constraint;

import nice.tools.code.Types;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

/**
   Declaration of a method.
   
   Can be 
   - a {@link bossa.syntax.NiceMethod Nice method}, 
     with several method bodies @see bossa.syntax.MethodBodyDefinition
   - a {@link bossa.syntax.JavaMethod Java method}
   - an {@link bossa.syntax.InlinedMethod inlined method}
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public abstract class MethodDeclaration extends Definition
{
  /**
     @param name the name of the method
     @param constraint the constraint
     @param returnType the return type
     @param parameters the formal parameters
   */
  public MethodDeclaration(LocatedString name, 
			   bossa.syntax.Constraint constraint,
			   bossa.syntax.Monotype returnType,
			   FormalParameters parameters)
  {
    super(name, Node.down);

    this.parameters = parameters;
    addChild(parameters);
    
    // remember it to print the interface
    syntacticConstraint = constraint.toString();
    
    symbol = new MethodDeclaration.Symbol(name, constraint, returnType);    
    symbol.propagate = Node.global;
    addChild(symbol);
    
    this.arity = parameters.size;
  }

  /** 
      Does not specify the type of the method.
      Used in JavaMethod to lazyfy the lookup of java types.
  */
  MethodDeclaration(LocatedString name,
		    Constraint cst,
		    Monotype[] parameters, 
		    Monotype returnType)
  {
    this(name, null, cst, parameters, returnType);
  }

  MethodDeclaration(LocatedString name,
		    FormalParameters formals,
		    Constraint cst,
		    Monotype[] parameters, 
		    Monotype returnType)
  {
    this(name, formals, 
	 new Polytype(cst, new FunType(parameters, returnType)));
  }

  MethodDeclaration(LocatedString name,
		    FormalParameters formals,
		    Polytype type)
  {
    super(name, Node.global);
    this.parameters = formals;
    this.arity = type.domain().length;
    this.type = type;
    symbol = new MethodDeclaration.Symbol(name, type);
  }

  private Polytype type;

  public final Polytype getType()
  {
    return type;
  }
  
  public final Monotype[] getArgTypes()
  {
    if (type == null)
      symbol.resolve();

    return type.domain();
  }
  
  public final Monotype getReturnType()
  {
    return type.codomain();
  }
  
  /****************************************************************
   * Typechecking
   ****************************************************************/

  /** 
      This is called in a pass before typechecking itself.
      This is important, to typecheck and disambiguate the default values
      of optional parameters, that will be used to typecheck code.
  */
  void typedResolve()
  {
    Polytype type = getType();
    
    if (!Constraint.hasBinders(type.getConstraint()))
      {
	parameters.typecheck(type.domain());
	return;
      }
    
    try{
      Typing.enter();
    
      try{
	type.getConstraint().enter();

        /*
          We typecheck with rigid type parameters. It means that
          default values must be valid for any instance of the method type.

          Alternatively, it could be decided that it is ok to have default
          values that satisfy only specific instances. In that case, it would
          be necessary to do some changes in K0. The difficulty is with
          overloading: the (now soft) type variables could be modified
          in an unsuccesful overloading resolution, and that modifications
          need to be discarded.
        */
        mlsub.typing.Typing.implies();

	parameters.typecheck(type.domain());
      }
      finally{
	Typing.leave();
      }
    }
    catch(TypingEx e){
      User.error(this,
		 "The type of method " + symbol.name +
		 " is not well formed: " + type + "\n" + e);
    }
  }

  /**
     Do further typechecking, once the context of the method is entered.
  */
  void innerTypecheck() throws TypingEx
  {
  }
  
  void typecheck()
  {
    // what we do here is equivalent to getType().checkWellFormedness();
    // except we also want to find the bytecode types when
    // the constraint is asserted

    // see getType().checkWellFormedness

    Polytype type = getType();
    
    if (!Constraint.hasBinders(type.getConstraint()))
      {
	try{
	  innerTypecheck();
	}
	catch(TypingEx e){
	  User.error(this, "Type error in method " + symbol.name);
	}
	return;
      }
    
    try{
      Typing.enter();
    
      try{
	type.getConstraint().enter();
	innerTypecheck();
      }
      finally{
	Typing.leave();
      }
    }
    catch(TypingEx e){
      User.error(this,
		 "The type of method " + symbol.name +
		 " is not well formed: " + type + "\n" + e);
    }
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  private String syntacticConstraint;
  
  public abstract void printInterface(java.io.PrintWriter s);

  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    if(getType() == null)
      return "method " + getName();
    
    return
      (syntacticConstraint != null ? syntacticConstraint
       : Constraint.toString(getType().getConstraint()))
      + String.valueOf(getReturnType())
      + " "
      + getName().toQuotedString()
      + "("
      // parameters can be null if type was set lowlevel (native code, ...)
      + (parameters != null ?
	 parameters.toString() : Util.map("",", ","", getType().domain()))
      + ")"
      ;
  }
  
  protected int arity;
  protected FormalParameters parameters;
  
  public int getArity()
  {
    return arity;
  }
  
  public FormalParameters formalParameters()
  {
    return parameters;
  }
  
  /** 
   * true if this method represent the access to the field of an object.
   */
  public boolean isFieldAccess() { return false; }

  void checkSpecialRequirements(Expression[] arguments)
  {
    // Do nothing by default.
  }

  String explainWhyMatchFails(Arguments arguments)
  {
    return symbol.defaultExplainWhyMatchFails(arguments);
  }

  private MethodDeclaration.Symbol symbol;
  MethodDeclaration.Symbol getSymbol() { return symbol; }

  public class Symbol extends FunSymbol
  {
    Symbol(LocatedString name, bossa.syntax.Constraint constraint, 
	   bossa.syntax.Monotype returnType)
    {
      super(name, constraint,
	    MethodDeclaration.this.formalParameters(), 
	    returnType);
    }

    Symbol(LocatedString name, Polytype type)
    {
      super(name, Types.addSure(type), 
	    MethodDeclaration.this.formalParameters(), 
	    MethodDeclaration.this.arity);
    }

    FieldAccess getFieldAccessMethod()
    {
      if(getMethodDeclaration() instanceof FieldAccess)
	return (FieldAccess) getMethodDeclaration();

      return null;
    }

    void checkSpecialRequirements(Expression[] arguments)
    {
      getMethodDeclaration().checkSpecialRequirements(arguments);
    }

    void resolve()
    {
      // Check that resolving has not already been done.
      if (syntacticType != null)
        {
          super.resolve();

          // The method has a raw type, while the symbol needs a nullness marker
          MethodDeclaration.this.type = this.type;
          this.type = Types.addSure(this.type);
        }
    }

    public Definition getDefinition()
    {
      return MethodDeclaration.this;
    }
 
    public MethodDeclaration getMethodDeclaration()
    {
      return MethodDeclaration.this;
    }

    gnu.expr.Expression compile()
    {
      return getMethodDeclaration().getCode();
    }

    gnu.expr.Expression compileInCallPosition()
    {
      return getMethodDeclaration().getCodeInCallPosition();
    }

    String explainWhyMatchFails(Arguments arguments)
    {
      return getMethodDeclaration().explainWhyMatchFails(arguments);
    }

    String defaultExplainWhyMatchFails(Arguments arguments)
    {
      return super.explainWhyMatchFails(arguments);
    }

    public String toString()
    {
      return getMethodDeclaration().toString();
    }
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  /** @return a string that uniquely represents this method */
  public String getFullName()
  {
    return "NONE";
  }

  private gnu.expr.Expression code;

  protected abstract gnu.expr.Expression computeCode();
  
  final void setCode(gnu.expr.Expression code) 
  {
    if(this.code != null)
      Internal.error("code already set");
    
    this.code = code;
  }
  
  gnu.expr.Expression getCode() 
  {
    // Default implementation.
    return getCodeInCallPosition();
  }
  
  final gnu.expr.Expression getCodeInCallPosition() 
  {
    if (code == null)
      {
	code = computeCode();
	
	if (code == null)
	  Internal.error(this, "No code for " + this);
      }

    return code;
  }
  
  public gnu.bytecode.Type javaReturnType()
  {
    return Types.javaType(getReturnType());
  }
  
  public gnu.bytecode.Type[] javaArgTypes()
  {
    return Types.javaType(getType().domain());
  }
  
  public void compile()
  {
  }
}
