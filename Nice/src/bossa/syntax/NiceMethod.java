/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 2000                             */
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
   A method defined in Nice.
   
   @version $Date$
   @author Daniel Bonniot
 */
public class NiceMethod extends MethodDeclaration
{
  /**
   * The method is a class or interface member.
   *
   * @param c the class this method belongs to.
   * @param name the name of the method
   * @param typeParameters the type parameters
   * @param constraint the constraint
   * @param returnType the return type
   * @param parameters the MonoTypes of the parameters
   */
  public static NiceMethod create
    (MethodContainer c,
     LocatedString name, 
     Constraint constraint,
     Monotype returnType,
     FormalParameters parameters)
  {
    // it is a class method, there is an implicit "this" argument

    boolean hasAlike = returnType.containsAlike() 
      || parameters.containsAlike();
    
    mlsub.typing.MonotypeVar[] thisTypeParams = c.createSameTypeParameters();
    
    int thisTypeParamsLen = (thisTypeParams == null ? 0 
			     : thisTypeParams.length);

    TypeSymbol container = c.getTypeSymbol();
    // contains the interface if container is one
    Interface itf = null;
    // contains the associated tc if possible
    TypeConstructor tc;
    if (container instanceof TypeConstructor)
      tc = (TypeConstructor) container;
    else
      {
	itf = (Interface) container;
	tc = itf.associatedTC();
      }
    
    // if the constraint is True
    // we must create a new one, otherwise we would
    // modify other methods!
    if(constraint == Constraint.True)
      constraint = new Constraint
	(new ArrayList(thisTypeParamsLen + (hasAlike ? 1 : 0)),
	 new ArrayList((hasAlike ? 1 : 0)));
	
    constraint.addBinders(thisTypeParams);
	
    mlsub.typing.Monotype thisType;

    // "alike" is not created for a non-abstract interface
    // if alike is not present in the type, since it saves
    // a type parameter (more intuituve for rebinding)
    // and it does not change typing to do so.
    if(hasAlike || tc == null)
      {
	TypeConstructor alikeTC = 
	  new TypeConstructor("Alike", c.variance(), false, false);
	
	constraint.addBinder(alikeTC);
	// added in front. Important for rebinding in method alternatives
	
	mlsub.typing.AtomicConstraint atom;
	if (itf != null)
	  atom = new mlsub.typing.ImplementsCst(alikeTC, itf);
	else
	  atom = new mlsub.typing.TypeConstructorLeqCst(alikeTC, tc);
	constraint.addAtom(AtomicConstraint.create(atom));
	
	thisType = new mlsub.typing.MonotypeConstructor(alikeTC, thisTypeParams);
	
	if (hasAlike)
	  {
	    Map map = new HashMap();
	    map.put(Alike.id, alikeTC);
	    returnType = returnType.substitute(map);
	    parameters.substitute(map);
	  }
      }
    else
      thisType = 
	new mlsub.typing.MonotypeConstructor(tc, thisTypeParams);
    
    parameters.addThis(Monotype.create(thisType));
    
    return new NiceMethod(name, constraint, returnType, parameters);
  }

  public NiceMethod(LocatedString name, 
		    Constraint constraint, Monotype returnType, 
		    FormalParameters parameters)
  {
    super(name, constraint, returnType, parameters);
  }

  /****************************************************************
   * Initial Context
   ****************************************************************/

  private void register()
  {
    /* 
       This must not be done in constructor:
       in case both the interface file and the source files are read, 
       both should not be mangled and registered.
    */
    boolean isConstructor = name.toString().equals("<init>");
    
    // do not generate mangled names for methods
    // that are not defined in a bossa file 
    // (e.g. native methods automatically imported).
    if(module != null && !isConstructor)
      bytecodeName = module.mangleName(name.toString());  
    else
      bytecodeName = name.toString();

    if(!isConstructor)
      bossa.link.Dispatch.register(this);
  }

  public void createContext()
  {
    register();
  }
  
  /****************************************************************
   * Typechecking
   ****************************************************************/

  void innerTypecheck()
  {
    // set bytecode types for type variables
    mlsub.typing.FunType ft = (mlsub.typing.FunType) getType().getMonotype();
    
    Types.setBytecodeType(ft.domain());
    Types.setBytecodeType(ft.codomain());
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  String bytecodeName;

  public String getBytecodeName()
  {
    return bytecodeName;
  }
  
  public String getFullName()
  {
    return module.getName().replace('.','$')+'$'+bytecodeName;
  }

  private gnu.bytecode.Method dispatchPrimMethod;

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    dispatchPrimMethod = module.addDispatchMethod(this);
    return new gnu.expr.PrimProcedure(dispatchPrimMethod);
  }
  
  public final gnu.bytecode.Method getDispatchPrimMethod() 
  { 
    getDispatchMethod();
    return dispatchPrimMethod;
  }
  
  public void compile()
  {
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(super.toString() + ";\n");
  }
}
