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
//$Modified: Wed Nov 10 16:47:27 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

/**
 * Abstract syntax for a global method declaration.
 */
public class MethodDefinition extends PolySymbol implements Definition
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
    // hack, super must be the first call
    super(name,null);
    this.propagate=Node.global;
    
    List params=new ArrayList();
    // if it is a class method, there is an implicit "this" argument
    //TODO    if(c!=null)
    //params.add(c.type);
    params.addAll(parameters);
    
    this.arity=params.size();
    this.type=new Polytype(constraint,new FunType(params,returnType));
    addChild(type);

    this.memberOf=c;
  }

  /** the Method is global */
  public MethodDefinition(LocatedString name,
			  Constraint constraint,
			  Monotype returnType,
			  List parameters)
  {
    this(null,name,constraint,returnType,parameters);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }
  
  /****************************************************************
   * Native methods
   ****************************************************************/

  private static ArrayList methods = new ArrayList();
  
  static void addMethod(MethodDefinition m)
  {
    methods.add(m);
  }
  
  static void compileMethods(bossa.modules.Module module)
  {
    for(Iterator i=methods.iterator();i.hasNext();)
      ((MethodDefinition)i.next()).compile(module);
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
   * Code generation
   ****************************************************************/

  protected gnu.mapping.Procedure dispatchMethod;
  public gnu.mapping.Procedure getDispatchMethod() { return dispatchMethod; }
  
  private gnu.bytecode.Type type(Monotype m)
  {
    // FIXME
    if(m.toString().equals("void"))
      return gnu.bytecode.Type.void_type;
    else
      return ClassType.make(m.toString());
  }

  protected gnu.bytecode.Type javaReturnType()
  {
    return type(this.type.codomain());
  }
  
  protected gnu.bytecode.Type[] javaArgTypes()
  {
    List domain=this.type.domain();
    gnu.bytecode.Type[] res=new ClassType[domain.size()];
    int n=0;
    for(Iterator i=domain.iterator();i.hasNext();n++)
      res[n]=((Monotype)i.next()).getJavaType();
    return res;
  }
  
  public void compile(bossa.modules.Module module)
  {
    dispatchMethod=new gnu.expr.PrimProcedure
      (
       module.dispatchClass.addMethod
       (name.toString()
	//+Pattern.bytecodeRepresentation(parameters)
	,
	javaArgTypes(),javaReturnType(),
	Access.PUBLIC|Access.STATIC|Access.FINAL)
       );
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(toString());
  }
  
  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    return
      type.codomain().toString()
      + " "
      + name
      + Util.map("<",", ",">",type.getTypeParameters())
      + type.getConstraint().toString()
      + "("
      + Util.map("",", ","",type.domain())
      + ");\n"
      ;
  }

  private ClassDefinition memberOf;
  protected int arity;
  boolean isFieldAccess=false; //true if this method represent the access to the field of an object.
}
