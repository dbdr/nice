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
    super(name, Node.global);

    this.parameters = parameters;
    
    if(returnType != null)
      // otherwise, symbol and arity are supposed to be set by someone else
      // a child class for instance
      // This should them be done through setLowlevelTypes(...)
      {
	// remember it to print the interface
	syntacticConstraint = constraint.toString();
	
	List domain = parameters.types();
	symbol = new MethodDeclaration.Symbol
	  (name, new Polytype(constraint, 
			      new FunType(domain, returnType)));
	addChild(symbol);

	this.arity = (domain == null ? 0 : domain.size());
      }

    boolean isConstructor = name.toString().equals("<init>");
    
    // do not generate mangled names for methods
    // that are not defined in a bossa file 
    // (e.g. native methods automatically imported).
    if(module != null && !isConstructor)
      bytecodeName = module.mangleName(name.toString());  

    if(!isConstructor)
      bossa.link.Dispatch.register(this);
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
    arity = (parameters==null ? 0 : parameters.length);
    symbol = new MethodDeclaration.Symbol(name, null);
    symbol.type = new mlsub.typing.Polytype
      (cst, new mlsub.typing.FunType(parameters, returnType));
  }
  
  /****************************************************************
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
    //Nothing
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
	parameters.typecheck(scope, typeScope, getType().domain());
	return;
      }
    
    try{
      Typing.enter();
    
      try{
	type.getConstraint().assert(false);

	parameters.typecheck(scope, typeScope, getType().domain());

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
  protected gnu.bytecode.Method   dispatchPrimMethod;

  protected abstract gnu.mapping.Procedure computeDispatchMethod();
  
  public final void setDispatchMethod(gnu.mapping.Procedure p) 
  {
    if(dispatchMethod != null)
      Internal.error("dispatch method already set");
    
    dispatchMethod = p;
  }
  
  public final gnu.mapping.Procedure getDispatchMethod() 
  {
    if(dispatchMethod == null)
      {
	dispatchMethod = computeDispatchMethod();
      
	if(dispatchMethod==null)
	  Internal.error(this,"No dispatch method for "+this);
      }
    
    return dispatchMethod;
  }
  
  public final gnu.bytecode.Method getDispatchPrimMethod() 
  { 
    if(dispatchMethod == null)
      dispatchMethod = computeDispatchMethod();
 
    if(dispatchPrimMethod == null)
      Internal.error(this, "dispatchPrimMethod not computed in "+this);
    
    return dispatchPrimMethod;
  }
  
  public gnu.bytecode.Type javaReturnType()
  {
    return Types.javaType(getType().codomain());
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
    if(getType() == null)
      return "method " + getName();
    
    return
      (syntacticConstraint == null ? "" : syntacticConstraint)
      + String.valueOf(getType().codomain())
      + " "
      + getName().toQuotedString()
      + "("
      // parameters can be null if type was set lowlevel (native code, ...)
      + (parameters != null ? parameters.toString()
	 : Util.map("",", ","",getType().domain()))
      + ")"
      ;
  }
  
  protected MethodContainer memberOf;
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

  /****************************************************************
   * Module and unique name
   ****************************************************************/
  
  public String getBytecodeName()
  {
    return bytecodeName;
  }
  
  public String getFullName()
  {
    return module.getName().replace('.','$')+'$'+bytecodeName;
  }

  public final mlsub.typing.Polytype getType()
  {
    return symbol.getType();
  }
  
  String bytecodeName;
  MethodDeclaration.Symbol symbol;

  class Symbol extends PolySymbol
  {
    Symbol(LocatedString name, Polytype type)
    {
      super(name, type);
      this.definition = MethodDeclaration.this;
    }

    public String toString()
    {
      return definition.toString();
    }
    
    MethodDeclaration definition;
  }
}
