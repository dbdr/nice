/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : MethodDefinition.java
// Created : Thu Jul 01 18:12:46 1999 by bonniot
//$Modified: Thu Jun 15 17:41:22 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import mlsub.typing.*;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

import bossa.util.Location;
import bossa.util.Debug;

/**
 * Abstract syntax for a global method declaration.
 */
public class MethodDefinition extends Definition
{
  /**
   * The method is a class member if c!=null.
   *
   * @param c the class this method belongs to, or 'null'
   * @param name the name of the method
   * @param typeParameters the type parameters
   * @param constraint the constraint
   * @param returnType the return type
   * @param parameters the MonoTypes of the parameters
   */
  public MethodDefinition(ClassDefinition c,
			  LocatedString name, 
			  Constraint constraint,
			  Monotype returnType,
			  List parameters)
  {
    super(name, Node.global);
    this.memberOf = c;

    List params = null;

    // if it is a class method, there is an implicit "this" argument
    if(c!=null)
      {
	boolean hasAlike = returnType.containsAlike() 
	  || Monotype.containsAlike(parameters);

	// create type parameters with the same names as in the class
	mlsub.typing.MonotypeVar[] thisTypeParams =
	  c.createSameTypeParameters();

	int thisTypeParamsLen = (thisTypeParams == null ? 0 
				 : thisTypeParams.length);
	    
	// if the constraint is True
	// we must create a new one, otherwise we would
	// modify other methods!
	if(constraint==Constraint.True)
	  constraint = new Constraint
	    (new ArrayList(thisTypeParamsLen + (hasAlike ? 1 : 0)),
	     new ArrayList((hasAlike ? 1 : 0)));
	
	constraint.addBinders(thisTypeParams);
	
	mlsub.typing.Monotype thisType;
	if(hasAlike)
	  {
	    TypeConstructor alikeTC = 
	      new TypeConstructor("Alike", 
				  c.variance(), false);
	    
	    constraint.addBinder(alikeTC);
	    // added in front. Important for rebinding in method alternatives

	    mlsub.typing.AtomicConstraint atom;
	    if(c.getAssociatedInterface()!=null)
	      atom = new mlsub.typing.ImplementsCst
		(alikeTC, c.getAssociatedInterface());
	    else
	      atom = new mlsub.typing.TypeConstructorLeqCst(alikeTC, c.tc);
	    constraint.addAtom(AtomicConstraint.create(atom));
	    
	    thisType = new mlsub.typing.MonotypeConstructor(alikeTC, thisTypeParams);

	    Map map = new HashMap();
	    map.put(Alike.id, alikeTC);
	    returnType = returnType.substitute(map);
	    parameters = Monotype.substitute(map, parameters);
	  }
	else
	  thisType = 
	    new mlsub.typing.MonotypeConstructor(c.tc, thisTypeParams);
	
	params = new ArrayList(parameters.size()+1);
	params.add(Monotype.create(thisType));
      }

    if(params==null)
      params = parameters;
    else
      params.addAll(parameters);
    
    if(returnType!=null)
      // otherwise, symbol and arity are supposed to be set by someone else
      // a child class for instance
      // This should them be done through setLowlevelTypes(...)
      {
	// remember it to print the interface
	syntacticConstraint = constraint.toString();
	
	symbol = new MethodDefinition.Symbol
	  (name, new Polytype(constraint, new FunType(params, returnType)));
	addChild(symbol);

	this.arity = params.size();
      }

    boolean isConstructor = name.toString().equals("<init>");
    
    // do not generate mangled names for methods
    // that are not defined in a bossa file 
    // (e.g. native methods automatically imported).
    if(module!=null && !isConstructor)
      bytecodeName = module.mangleName(name.toString());  

    if(!isConstructor)
      bossa.link.Dispatch.register(this);
  }

  public MethodDefinition(LocatedString name, 
			  Constraint constraint,
			  Monotype returnType,
			  List parameters)
  {
    this(null,name,constraint,returnType,parameters);
  }
  
  /** 
      Does not specify the type of the method.
      Used in JavaMethodDefinition to lazyfy
      the lookup of java types.
  */
  MethodDefinition(LocatedString name)
  {
    super(name, Node.global);
  }
  
  void setLowlevelTypes(mlsub.typing.Constraint cst,
			mlsub.typing.Monotype[] parameters, 
			mlsub.typing.Monotype returnType)
  {
    arity = (parameters==null ? 0 : parameters.length);
    symbol = new MethodDefinition.Symbol(name, null);
    symbol.type = new mlsub.typing.Polytype
      (cst, new mlsub.typing.FunType(parameters, returnType));
  }
  
  public Collection associatedDefinitions()
  {
    return null;
  }
  
  /** true iff the method was declared inside a class */
  boolean isMember()
  {
    return memberOf!=null;
  }

  // from VarSymbol 
  boolean isAssignable()
  {
    return false;
  }

  int getArity()
  {
    return arity;
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

  void typecheck()
  {
    try{
      getType().checkWellFormedness();
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
  private gnu.bytecode.Method   dispatchPrimMethod;

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    dispatchPrimMethod = module.addDispatchMethod(this);
    return new gnu.expr.PrimProcedure(dispatchPrimMethod);
  }
  
  public final void setDispatchMethod(gnu.mapping.Procedure p) 
  {
    if(dispatchMethod!=null)
      Internal.error("dispatch method already set");
    
    dispatchMethod = p;
  }
  
  public final gnu.mapping.Procedure getDispatchMethod() 
  { 
    if(dispatchMethod==null)
      {
	dispatchMethod = computeDispatchMethod();
      
	if(dispatchMethod==null)
	  Internal.error(this,"Null dispatch method for "+this);
      }
    
    return dispatchMethod;
  }
  
  public final gnu.bytecode.Method getDispatchPrimMethod() 
  { 
    if(dispatchMethod==null)
      dispatchMethod = computeDispatchMethod();
 
    if(dispatchPrimMethod==null)
      Internal.error(this,"dispatchPrimMethod not computed in "+this);
    
    return dispatchPrimMethod;
  }
  
  public gnu.bytecode.Type javaReturnType()
  {
    return bossa.CodeGen.javaType(getType().codomain());
  }
  
  public gnu.bytecode.Type[] javaArgTypes()
  {
    return bossa.CodeGen.javaType(getType().domain());
  }
  
  public void compile()
  {
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  String syntacticConstraint;
  
  public void printInterface(java.io.PrintWriter s)
  {
    s.print(
	    //mlsub.typing.Constraint.toString(getType().getConstraint())
	    syntacticConstraint
	    + getType().codomain()
	    + " "
	    + symbol.name.toQuotedString()
	    + "("
	    + Util.map("",", ","",getType().domain())
	    + ");\n");
    syntacticConstraint = null;
  }
  
  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    if(getType()==null)
      return "method "+getName();
    
    return
      mlsub.typing.Constraint.toString(getType().getConstraint())
      + String.valueOf(getType().codomain())
      + " "
      + getName().toQuotedString()
      + "("
      + Util.map("",", ","",getType().domain())
      + ")"
      ;
  }

  private ClassDefinition memberOf;
  protected int arity;

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
  MethodDefinition.Symbol symbol;

  class Symbol extends PolySymbol
  {
    Symbol(LocatedString name, Polytype type)
    {
      super(name, type);
      this.definition = MethodDefinition.this;
    }

    MethodDefinition definition;
  }
}
