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
//$Modified: Thu May 04 15:28:04 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

import bossa.util.Location;

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

    List params = new ArrayList();

    // if it is a class method, there is an implicit "this" argument
    if(c!=null)
      {
	Polytype t = c.getType().cloneType();
	constraint = Constraint.and(constraint, t.getConstraint());
	params.add(t.getMonotype());
      }
    
    params.addAll(parameters);
    
    this.arity = params.size();

    symbol = new MethodDefinition.Symbol
      (name, new Polytype(constraint, new FunType(params, returnType)), this);
    addChild(symbol);

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
    Constraint cst = getType().getConstraint();
    
    // Optimization
    if(cst==Constraint.True ||
       cst.binders.size()==0)
      return;
    
    bossa.typing.Typing.enter("Definition of "+symbol.name);
    
    try{
      // Explanation for the assert(false) statement:
      // We just want to check the type is well formed,
      // so there is not need to enter top implementations.
      // This is just an optimization, this shouldn't
      // change anything.
      getType().getConstraint().assert(false);
    }
    catch(bossa.typing.TypingEx e){
      User.error(this,
		 "The constraint of method "+symbol.name+
		 " is not well formed");
    }
  }

  void endTypecheck()
  {
    Constraint cst = getType().getConstraint();
    
    if(cst==Constraint.True ||
       cst.binders.size()==0)
      return;
    
    try{
      bossa.typing.Typing.leave();
    }
    catch(bossa.typing.TypingEx e){
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
    return this.getType().codomain().getJavaType();
  }
  
  public gnu.bytecode.Type[] javaArgTypes()
  {
    List domain=this.getType().domain();
    gnu.bytecode.Type[] res=new gnu.bytecode.Type[domain.size()];
    int n=0;
    for(Iterator i=domain.iterator();i.hasNext();n++)
      res[n]=((Monotype)i.next()).getJavaType();
    return res;
  }
  
  public void compile()
  {
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(
	    getType().getConstraint().toString()
	    + String.valueOf(getType().codomain())
	    + " "
	    + symbol.name.toQuotedString()
	    + "("
	    + Util.map("",", ","",getType().domain())
	    + ");\n");
  }
  
  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    return
      module+" "+
      getType().getConstraint().toString()
      + String.valueOf(getType().codomain())
      + " "
      + symbol.name.toQuotedString()
      + "("
      + Util.map("",", ","",getType().domain())
      + ");\n"
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
    return module.getName().replace('.','$')+"$"+bytecodeName;
  }

  public Polytype getType()
  {
    return symbol.getType();
  }
  
  String bytecodeName;
  MethodDefinition.Symbol symbol;

  class Symbol extends PolySymbol
  {
    Symbol(LocatedString name, Polytype type, MethodDefinition definition)
    {
      super(name, type);
      this.definition=definition;
    }

    MethodDefinition definition;
  }
}
