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
import nice.tools.code.Types;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

import bossa.util.Location;
import bossa.util.Debug;

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
abstract public class MethodDeclaration extends Definition
{
  /**
     @param name the name of the method
     @param typeParameters the type parameters
     @param constraint the constraint
     @param returnType the return type
     @param parameters the formal parameters
   */
  public MethodDeclaration(LocatedString name, 
			   Constraint constraint,
			   Monotype returnType,
			   FormalParameters parameters)
  {
    super(name, Node.down);

    if(returnType != null)
      // otherwise, symbol and arity are supposed to be set by someone else
      // a child class for instance
      // This should them be done through setLowlevelTypes(...)
      {
	this.parameters = parameters;
	addChild(parameters);
	
	// remember it to print the interface
	syntacticConstraint = constraint.toString();
	
	List domain = parameters.types();
	symbol = new MethodDeclaration.Symbol
	  (name, new Polytype(constraint, 
			      new FunType(domain, returnType)));
	symbol.propagate = Node.global;
	addChild(symbol);

	this.arity = (domain == null ? 0 : domain.size());
      }
  }

  /** 
      Does not specify the type of the method.
      Used in JavaMethod to lazyfy the lookup of java types.
  */
  MethodDeclaration(LocatedString name)
  {
    super(name, Node.global);
  }
  
  void setLowlevelTypes(mlsub.typing.Constraint cst,
			mlsub.typing.Monotype[] parameters, 
			mlsub.typing.Monotype returnType)
  {
    arity = (parameters == null ? 0 : parameters.length);
    symbol = new MethodDeclaration.Symbol(name, null);
    symbol.type = new mlsub.typing.Polytype
      (cst, new mlsub.typing.FunType(parameters, returnType));
  }

  /****************************************************************
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
  }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  /**
     Do further typechecking, once the context of the method is entered.
  */
  void innerTypecheck()
  {
  }
  
  void typecheck()
  {
    // what we do here is equivalent to getType().checkWellFormedness();
    // except we also want to find the bytecode types when
    // the constraint is asserted

    // see getType().checkWellFormedness

    mlsub.typing.Polytype type = getType();
    
    if (!mlsub.typing.Constraint.hasBinders(type.getConstraint()))
      {
	parameters.typecheck(getType().domain());
	innerTypecheck();
	return;
      }
    
    try{
      Typing.enter();
    
      try{
	type.getConstraint().assert();
	parameters.typecheck(getType().domain());
	innerTypecheck();
      }
      finally{
	Typing.leave();
      }
    }
    catch(TypingEx e){
      User.error(this,
		 "The type of method "+symbol.name+
		 " is not well formed");
    }
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  private gnu.mapping.Procedure dispatchMethod;

  protected abstract gnu.mapping.Procedure computeDispatchMethod();
  
  final void setDispatchMethod(gnu.mapping.Procedure p) 
  {
    if(dispatchMethod != null)
      Internal.error("dispatch method already set");
    
    dispatchMethod = p;
  }
  
  final gnu.mapping.Procedure getDispatchMethod() 
  {
    if(dispatchMethod == null)
      {
	dispatchMethod = computeDispatchMethod();
      
	if(dispatchMethod==null)
	  Internal.error(this,"No dispatch method for "+this);
      }
    
    return dispatchMethod;
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
    if(symbol == null || getType() == null)
      return "method " + getName();
    
    return
      (syntacticConstraint != null ? syntacticConstraint
       : mlsub.typing.Constraint.toString(getType().getConstraint()))
      + String.valueOf(getReturnType())
      + " "
      + getName().toQuotedString()
      + "("
      // parameters can be null if type was set lowlevel (native code, ...)
      + (parameters != null ? parameters.toString()
	 : Util.map("",", ","",getType().domain()))
      + ")"
      ;
  }
  
  protected int arity;
  private FormalParameters parameters;
  
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

  /**
     @return true if this method is the 'main' of the program
  */
  public final boolean isMain() 
  {
    return arity == 1 && "main".equals(name.content);
  }
  
  public final mlsub.typing.Polytype getType()
  {
    return symbol.getType();
  }
  
  public final mlsub.typing.Monotype getReturnType()
  {
    return symbol.getType().codomain();
  }
  
  MethodDeclaration.Symbol symbol;

  class Symbol extends PolySymbol
  {
    Symbol(LocatedString name, Polytype type)
    {
      super(name, type);
    }

    MethodDeclaration getDefinition()
    {
      return MethodDeclaration.this;
    }
    
    /****************************************************************
     * Overloading resolution
     ****************************************************************/
    
    /**
       @return
       0 : doesn't match
       1 : wasn't even a function
       2 : matches
    */
    int match(Arguments arguments)
    {
      if (MethodDeclaration.this.formalParameters() == null)
	// true for constructors, for instance. case might be removed
	if (!arguments.plainApplication(MethodDeclaration.this.getArity()))
	  return 0;
	else
	  return 2;
      else if (!MethodDeclaration.this.formalParameters().match(arguments))
	return 0;
      else
	return 2;
    }

    public String toString()
    {
      return MethodDeclaration.this.toString();
    }
  }
}
