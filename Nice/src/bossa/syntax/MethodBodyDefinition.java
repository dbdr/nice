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

import bossa.util.*;
import mlsub.typing.*;

import gnu.bytecode.*;
import java.util.*;

import bossa.util.Debug;
import bossa.util.Location;
import nice.tools.typing.Types;
import nice.tools.code.Gen;

import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.Constraint;

/**
   Definition of an alternative for a method.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
*/
public class MethodBodyDefinition extends MethodImplementation
{
  /**
   * Create a method alternative.
   *
   * @param name the name of the method.
   * @param binders 
   *    used to rebind the type parameters of the method definition. 
   *    Null if one doesn't want to rebind them.
   * @param newConstraint list of AtomicConstraint
   * @param formals list of Pattern
   * @param body s statement representing the body of the method.
   */
  public MethodBodyDefinition(NiceClass container,
			      LocatedString name, 
			      Collection binders,
			      List formals, 
			      Statement body)
  {
    super(name, body, makeFormals(formals, container, name.location()));

    this.binders = binders; 

    this.declaration = null;
  }

  boolean hasThis()
  {
    return
      this.formals != null &&
      this.formals.length >= 1 && 
      String.valueOf(this.formals[0].name).equals("this");
  }

  private static Pattern[] makeFormals(List formals, NiceClass container,
				       Location loc)
  {
    if (container == null)
      return (Pattern[]) formals.toArray(new Pattern[formals.size()]);

    Pattern[] res = new Pattern[formals.size() + 1];
    res[0] = new Pattern(new LocatedString("this", loc), 
			 new TypeIdent(container.definition.getName()));
    int n = 1;
    for(Iterator f = formals.iterator(); f.hasNext(); n++)
      res[n] = (Pattern) f.next();
    return res;
  }

  final TypeConstructor firstArgument()
  {
    if (formals[0].tc != null)
      return formals[0].tc;
    else
      // Either there is no specialization on the first parameter, or
      // it was equivalent to the declaration type and has been erased,
      // so we return the information in the declaration.
      return Types.equivalent(declaration.getArgTypes()[0]).
        head();
  }

  private void setDeclaration(MethodDeclaration d)
  {
    if (d == null)
      User.error(this, "Method " + name + " is not declared");
    
    this.declaration = d;

    if (d instanceof JavaMethod) {
      ((JavaMethod) d).registerForDispatch();
      if (TypeConstructors.isInterface(formals[0].tc))
        User.error(this, name + " is a native method. Dispatch can only occur if the first argument is not a interface.");
    }
    else if (! (d instanceof NiceMethod))
      User.error(this, "Implementations can only be made for methods, but " + 
		 d.getName() + " is a function.\nIt was defined at:\n" + 
		 d.location());

    buildSymbols();
  }

  /** 
      Returns the symbol of the method this declaration belongs to.
   */
  private VarSymbol findSymbol(List symbols)
  {
    if(symbols.size() == 0) return null;

    TypeConstructor[] tags = Pattern.getTC(formals);
    TypeConstructor[] additionalTags = Pattern.getAdditionalTC(formals);
    boolean hasAdditionalTags = false;
    for (int i = 0; i<tags.length; i++)
      if (additionalTags[i] != null)
        hasAdditionalTags = true;

    for(Iterator i = symbols.iterator(); i.hasNext();){
      VarSymbol s = (VarSymbol)i.next();
      if(s.getMethodDeclaration() == null)
	{
	  i.remove();
	  continue;
	}
      
      MethodDeclaration m = s.getMethodDeclaration();
      if (m.isIgnored())
        {
          i.remove();
          continue;
        }

      if (m.getArity() != formals.length
          || !(m instanceof NiceMethod || m instanceof JavaMethod))
	{
	  i.remove();
	  continue;
	}
	  
      try{
	int level;
	if (Debug.overloading)
	  level = Typing.enter("Trying declaration " + m + 
			       " for method body "+name);
	else
	  level = Typing.enter();

	try{
	  mlsub.typing.Polytype t = m.getType();
	  Constraint.enter(t.getConstraint());
          Pattern.inDomain(formals, t.domain());
	}
	finally{
	  if(Typing.leave() != level)
	    Internal.error("Enter/Leave error");
	}
      }
      catch(TypingEx e){
	if(Debug.overloading) Debug.println("Not the right choice :"+e);
	i.remove();
      }
      catch(mlsub.typing.BadSizeEx e){
	i.remove();
      }
    }

    /* If the method are ambigious and the implementation has additional tc's
     * then try to find the most specific declaration(s) using these tc's
     * TODO: combine this code with removeNonMinimal in OverloadedSymbolExp
     */   
    if (symbols.size() > 1 && hasAdditionalTags)
      {
        MethodDeclaration.Symbol[] tempSymbols = new MethodDeclaration.Symbol[symbols.size()];
	symbols.toArray(tempSymbols);
        int size = symbols.size();
	symbols = new LinkedList();
        int len = formals.length;
	boolean[] removed = new boolean[size];
        for (int m1 = 0; m1 < size; m1++)
          {
	    Monotype[] dom1 = tempSymbols[m1].getMethodDeclaration().getType().domain();
	    for (int m2 = 0; m2 < size; m2++)
	      if (m1 != m2)
                {
                  boolean remove = true;
		  boolean additionalsEqual = true;
		  Monotype[] dom2 = tempSymbols[m2].getMethodDeclaration().getType().domain();
		  for (int i = 0; i < len; i++)
		    if (additionalTags[i] != null)
		      {
	  	        try {
		          domainMonotypeLeq(dom2[i], dom1[i]);
		          try {
		            domainMonotypeLeq(dom1[i], dom2[i]);
		          }
		          catch (TypingEx e) {
			    additionalsEqual = false;
		          }
		        }
		        catch(TypingEx e) {
		          remove = false;
                          break;
		        }
		      }

                  //may not remove a method that is equal for the arguments with an additional.
		  removed[m1] = remove && !additionalsEqual;
                  if (removed[m1])
		    break;
	        }

	    if (!removed[m1])
	      symbols.add(tempSymbols[m1]);
	  }

      }

    // Check that the non-dispatched parameter names match the declaration
    outer: for(Iterator it = symbols.iterator(); it.hasNext();) {
      MethodDeclaration m = ((MethodDeclaration.Symbol) it.next()).getMethodDeclaration();
      if( m instanceof NiceMethod) {
        FormalParameters params = m.formalParameters();
	for (int i = params.hasThis() ? 1 : 0; i < formals.length; i++)
          if (formals[i].atAny() && formals[i].name != null && params.getName(i) != null &&
		!formals[i].name.toString().equals(params.getName(i).toString())) {
	    it.remove();
	    continue outer;
          }
      }
    }

    OverloadedSymbolExp.removeNonMinimal(symbols);

    if(symbols.size() == 1) 
      return (VarSymbol) symbols.get(0);

    if(symbols.size()==0)
      User.error(this,
		 "No method called " + name + 
		 " is compatible with these patterns");

    String methods = "";
    for(Iterator i = symbols.iterator(); i.hasNext();)
      {
	MethodDeclaration m = 
	  ((MethodDeclaration.Symbol) i.next()).getMethodDeclaration();
	methods += m + " defined " + m.location() + "\n";
      }
    
    throw User.error
      (this,
       "There is an ambiguity about which version " + 
       " of the overloaded method \"" + name + 
       "\" this alternative belongs to.\n" +
       "Try to use more patterns.\n\n" +
       "Possible methods:\n" +
       methods);
  }

  private void domainMonotypeLeq(Monotype m1, Monotype m2) throws TypingEx
  {
    if (m1 == m2)
      return;

    Types.setMarkedKind(m1);
    Types.setMarkedKind(m2);
    Typing.leq(Types.rawType(m1), Types.rawType(m2));
  }

  void doResolve()
  {
    //Resolution of the body is delayed to enable overloading

    Pattern.resolve(typeScope, getGlobalScope(), formals);

    symbols = scope.lookup(name);
  }

  private List symbols;

  void lateBuildScope()
  {
    Pattern.resolveValues(formals);

    VarSymbol s = findSymbol(symbols);
    symbols = null;

    if(s==null)
      User.error(this, name+" is not declared");
    
    if(s.getMethodDeclaration() == null)
      User.error(this, name+" is not a method");

    MethodDeclaration decl = s.getMethodDeclaration();
    
    setDeclaration(decl);

    // Get type parameters
    if (binders != null)
      {
        Constraint cst = declaration.getType().getConstraint();
        if (! Constraint.hasBinders(cst))
          User.error(name, "Method " + name + " has no type parameters");

        try{
          typeScope.addMappingsLS(binders, cst.binders());
        }
        catch(mlsub.typing.BadSizeEx e){
          User.error(name, "Method " + name + 
                     " expects " + e.expected + " type parameters");
        }
        catch(TypeScope.DuplicateName e) {
          User.error(this, e);
        }
      }

    try{
      for(int n = 0; n < formals.length; n++)
	{
	  TypeConstructor tc = formals[n].getRuntimeTC();
	  if (tc != null)
	    typeScope.addSymbol(tc);
	}
    }
    catch(TypeScope.DuplicateName e) {
      User.error(this, e);
    }

    if (! declaration.specializesMethods())
      removeUnnecessaryDispatch();
  }

  private void removeUnnecessaryDispatch()
  {
    boolean entered = false;

    if (Constraint.hasBinders(declaration.getType().getConstraint()))
      {
        Typing.enter();
        entered = true;
      }

    try {
      try {
        Constraint.enter(declaration.getType().getConstraint());
        Typing.implies();

        Monotype[] domain = declaration.getType().domain();
        for (int n = 0; n < formals.length; n++)
          {
            TypeConstructor tc = Types.rawType(domain[n]).head();

            if (formals[n].tc != null)
              formals[n].setDomainEq(tc != null &&
                                     Types.isSure(domain[n]) && 
                                     Typing.testLeq(tc, formals[n].tc));
          }
      }
      finally {
        if (entered)
          Typing.leave();
      }
    }
    catch(TypingEx ex) {
      Internal.warning(ex.toString());
    }
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  void typecheck()
  {
    boolean errorFound = false;
    int level;
    if (Debug.typing)
      level = Typing.enter("METHOD BODY " + this + "\n\n");
    else
      level = Typing.enter();

    try {
      try {
	try { 
	  Constraint.enter(declaration.getType().getConstraint());
	}
	catch(TypingEx e){
	  throw User.error(name,
			   "the constraint will never be satisfied",
			   ": "+e.getMessage());
	}

	// Introduce the types of the arguments
	Monotype[] monotypes = MonoSymbol.getMonotype(parameters);

	for(int n = 0; n < formals.length; n++)
	  {
	    TypeConstructor runtimeTC = formals[n].getRuntimeTC();
	    if (runtimeTC == null)
	      Typing.introduce(monotypes[n]);
	    else
	      Typing.introduce(runtimeTC);
	  }

	Monotype[] domain = declaration.getType().domain();

	for(int n = 0; n < formals.length; n++)
	{
	  TypeConstructor tc = Types.rawType(domain[n]).head();
	  if (formals[n].tc != null)
	    formals[n].setDomainTC(tc);
        }

	// The arguments are specialized by the patterns
	try{
	  Pattern.in(monotypes, formals);
	}
	catch(TypingEx e){
	  throw User.error(name,"The patterns are not correct", e);
	}
      
	// The arguments have types below the method declaration domain
	for (int i = 0; i < monotypes.length; i++)
	  try{
	    Typing.leq(monotypes[i], domain[i]);
	  }
	  catch(TypingEx e) {
	    /*
	       This type error originates from an inadequation between
	       the pattern and the domain. The parameter types are just 
	       variables that connected the two.
	    */
	    throw User.error(formals[i], 
			     "Pattern " + formals[i] + 
			     " is incompatible with type " + domain[i]);
	  }

	try {
	  nice.tools.code.Types.setBytecodeType(monotypes);

	  Typing.implies();
	}
	catch(TypingEx e) {
	  throw User.error(name, "Type error in method body \""+name+"\":\n"+e);
	}

	Node.currentFunction = this;
	if (hasThis())
	  Node.thisExp = new SymbolExp(parameters[0], location());

	bossa.syntax.dispatch.typecheck(body);
      } catch(UserError ex){
	module.compilation().error(ex);
	errorFound = true;
      }
    }
    finally{
      Node.currentFunction = null;
      Node.thisExp = null;
      try{
	if (Typing.leave() != level)
	  Internal.error("Unmatching enter/leave");
      }
      catch(TypingEx e){
	if (! errorFound)
	  User.error(this, "Type error in method "+name, ": "+e);
      }
    }

    // If the method we implement specialize others, then we cannot
    // omit the patterns, as we do handle only a special case of those
    // more general methods.
    if (declaration.specializesMethods())
      addPatterns();
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    // Not exported
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  protected Type[] javaArgTypes()
  {
    Type[] res = new Type[parameters.length];

    for(int n = 0; n < parameters.length; n++)
      res[n] = formals[n].atNull() ? 
	nice.tools.code.Types.javaType(PrimitiveType.nullTC) :
	formals[n].tc == bossa.syntax.PrimitiveType.arrayTC ?
	nice.tools.code.SpecialArray.unknownTypeArray() :
	nice.tools.code.Types.javaType(parameters[n].getMonotype());

    return res;
  }

  gnu.expr.Expression[] compiledArguments()
  {
    return VarSymbol.compile(parameters);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return name + "(" + Util.map("", ", ", "", formals) + ")";
  }

  Collection /* of LocatedString */ binders; // Null if type parameters are not bound
}
