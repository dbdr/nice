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
import nice.tools.code.Types;
import nice.tools.code.Gen;

import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.Constraint;

/**
   Definition of an alternative for a method.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class MethodBodyDefinition extends Definition 
  implements Function
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
    super(name, Node.down);

    this.binders = binders; 

    this.formals = makeFormals(formals, container);
    this.body = body;
    this.declaration = null;
  }

  private static Pattern[] makeFormals(List formals, NiceClass container)
  {
    if (container == null)
      return (Pattern[]) formals.toArray(new Pattern[formals.size()]);

    Pattern[] res = new Pattern[formals.size() + 1];
    res[0] = new Pattern(THIS, new TypeIdent(container.definition.getName()));
    int n = 1;
    for(Iterator f = formals.iterator(); f.hasNext(); n++)
      res[n] = (Pattern) f.next();
    return res;
  }

  private static LocatedString THIS = 
    new LocatedString("this", Location.nowhere());

  public Collection associatedDefinitions()
  {
    return null;
  }
  
  private MonoSymbol[] buildSymbols(Pattern[] names, Monotype[] types)
  {
    if(names.length != types.length)
      switch(types.length){
      case 0: User.error(this,"Method "+name+" has no parameters");
      case 1: User.error(this,"Method "+name+" has 1 parameter");
      default:User.error(this,
			 "Method "+name+" has "+types.length+" parameters");
      }
    
    MonoSymbol[] res = new MonoSymbol[names.length];
    for(int tn = 0; tn < names.length; tn++)
      {
	Pattern p = names[tn];

	MonotypeVar type;

	if (p.name == null)
	  // anonymous pattern
	  type = new MonotypeVar("anonymous argument " + tn);
	else
	  // XXX optimize using this:
	  // type = new MonotypeVar("type(" + p.name + ")<" + types[tn]);
	  {
	    LocatedString typeName;
	    typeName = p.name.cloneLS();
	    typeName.prepend("type(");	
	    typeName.append(")<" + types[tn]);
	    type = new MonotypeVar(typeName.toString());
	  }

	res[tn] = new MonoSymbol(p.name, type);
      }
    return res;
  }
  
  MethodDeclaration getDeclaration()
  {
    return declaration;
  }

  private void setDeclaration(MethodDeclaration d)
  {
    if (d == null)
      User.error(this, "Method " + name + " is not declared");
    
    // Overriding a mono-method can currently not dispatch on other args
    if (d instanceof JavaMethod)
      {
	for (int i = 1; i < formals.length; i++)
	  if (!(formals[i].atAny()))
	    User.error(this, this + " is a native method. Dispatch can only occur on the first argument");

	((JavaMethod) d).addImplementation(this);
      }
    else if (d instanceof NiceMethod)
      // Register this alternative for the link test
      alternative = new bossa.link.Alternative
	(((NiceMethod) d).getFullName(), getPatterns())
	{
	  public gnu.expr.Expression methodExp()
	  {
	    return MethodBodyDefinition.this.getRefExp();
	  }
	};
    else
      User.error(this, "Implementations can only be made for methods, but " + 
		 d.getName() + " is a function.\nIt was defined at:\n" + 
		 d.location());

    if (d.isMain())
      User.warning(this, "This syntax for the main function is deprecated.\nPlease use instead the following:\n\nvoid main(String[] args)\n{\n  ...\n}");

    this.declaration = d;
    parameters = buildSymbols(this.formals, declaration.getArgTypes());
    scope.addSymbols(parameters);
  }

  /** 
      Returns the symbol of the method this declaration belongs to.
   */
  private VarSymbol findSymbol(VarScope scope)
  {
    VarSymbol res;
    
    List symbols = scope.lookup(name);
    if(symbols.size() == 0) return null;

    if(symbols.size() == 1)
      return (VarSymbol) symbols.get(0);

    TypeConstructor[] tags = Pattern.getTC(formals);
    TypeConstructor[] additionalTags = Pattern.getAdditionalTC(formals);
    
    for(Iterator i = symbols.iterator(); i.hasNext();){
      VarSymbol s = (VarSymbol)i.next();
      if(!(s instanceof MethodDeclaration.Symbol))
	{
	  i.remove();
	  continue;
	}
      
      MethodDeclaration m = ((MethodDeclaration.Symbol) s).getDefinition();

      if (!(m instanceof NiceMethod || m instanceof JavaMethod) 
	  || m.getArity() != formals.length)
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
	  tagLeq(tags, t.domain());
	  tagLeq(additionalTags, t.domain());
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

    if(symbols.size() == 1) 
      return (VarSymbol) symbols.get(0);

    if(symbols.size()==0)
      User.error(this,
		 "No definition of \""+name+"\" is compatible with the patterns");

    String methods = "";
    for(Iterator i = symbols.iterator(); i.hasNext();)
      {
	MethodDeclaration m = 
	  ((MethodDeclaration.Symbol) i.next()).getDefinition();
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

  private void tagLeq(TypeConstructor[] tags, Monotype[] types)
  throws TypingEx
  {
    for (int i = 0; i<tags.length; i++)
      {
	Types.setMarkedKind(types[i]);
	Monotype type = types[i].equivalent();
	Typing.leq(tags[i], ((MonotypeConstructor) type).getTP()[0]);
      }
  }

  void doResolve()
  {
    //Resolution of the body is delayed to enable overloading

    Pattern.resolveTC(typeScope, formals);
  }
  
  void lateBuildScope()
  {
    VarSymbol s = findSymbol(scope);

    if(s==null)
      User.error(this, name+" is not declared");
    
    if(!(s instanceof MethodDeclaration.Symbol))
      User.error(this, name+" is not a method");

    MethodDeclaration decl = ((MethodDeclaration.Symbol) s).getDefinition();
    
    setDeclaration(decl);

    // Get type parameters
    if (binders != null)
    try{
      typeScope.addMappingsLS
	(binders,
	 declaration.getType().getConstraint().binders());
    }
    catch(mlsub.typing.BadSizeEx e){
      User.error(name, "Method " + name + 
		 " expects " + e.expected + " type parameters");
    }
    catch(TypeScope.DuplicateName e) {
      User.error(this, e);
    }

    // We can resolve now
    Pattern.resolveType(this.typeScope, formals);
  }

  void resolveBody()
  {
    body = bossa.syntax.dispatch.analyse
      (body, scope, typeScope, !Types.isVoid(declaration.getReturnType()));
  }
  
  /****************************************************************
   * Type checking
   ****************************************************************/

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

  void typecheck()
  {
    int level = -1;
    try{
      if (Debug.typing)
	level = Typing.enter("METHOD BODY " + this + "\n\n");
      else
	level = Typing.enter();

      // remember that enter was called, to leave.
      // usefull if previous code throws an exception
      entered = true;

      try { 
	Constraint.enter(declaration.getType().getConstraint());
      }
      catch(TypingEx e){
	User.error(name,
		   "the constraint will never be satisfied",
		   ": "+e.getMessage());
      }
	
      // Introduce the types of the arguments
      Monotype[] monotypes = MonoSymbol.getMonotype(parameters);
      Typing.introduce(monotypes);

      // The arguments are specialized by the patterns
      try{
	Pattern.in(monotypes, formals);
      }
      catch(TypingEx e){
	User.error(name,"The patterns are not correct", e);
      }
      
      try{
	// The arguments have types below the method declaration domain
	Monotype[] domain = declaration.getType().domain();
	for (int i = 0; i < monotypes.length; i++)
	  Typing.leq(monotypes[i], domain[i]);
      }
      catch(mlsub.typing.BadSizeEx e){
	Internal.error("Bad size in MethodBodyDefinition.typecheck()");
      }
      catch(TypingEx e) {
	User.error(name,"Type error in arguments of method body \""+name+"\":\n"+e);
      }
      
      try {
	Types.setBytecodeType(monotypes);

	Typing.implies();
      }
      catch(TypingEx e) {
	throw User.error(name, "Type error in method body \""+name+"\":\n"+e);
      }

      // Introduction of binders for the types of the arguments
      // as in f(x@C : X)
      for(int n = 0; n < formals.length; n++)
	{
	  Pattern pat = formals[n];
	  Monotype type = pat.getType();
	  if (type == null)
	    continue;
	    
	  MonoSymbol sym = parameters[n];
	  
	  try{
	    TypeConstructor 
	      tc = type.head(),
	      formalTC = ((MonotypeConstructor) sym.getMonotype()).getTC();
		  
	    if (tc == null)
	      Internal.error("Not implemented ?");

	    tc.setId(formalTC.getId());
	    Typing.eq(type, sym.getMonotype());
	  }
	  catch(TypingEx e){
	    User.error(pat.name, 
		       "\":\" constraint for argument "+pat.name+
		       " is not correct",
		       ": "+e);
	  }
	}

      Node.currentFunction = this;
      if (declaration.hasThis())
	Node.thisExp = new SymbolExp(parameters[0], location());

      try{
	bossa.syntax.dispatch.typecheck(body);
      } catch(UserError ex){
	nice.tools.compiler.OutputMessages.error(ex.getMessage());
      }
    }
    finally{
      Node.currentFunction = null;
      Node.thisExp = null;
      if(entered)
	try{
	  if (Typing.leave() != level)
	    Internal.error("Unmatching enter/leave");
	  entered = false;
	}
	catch(TypingEx e){
	  User.error(this, "Type error in method "+name, ": "+e);
	}
    }
  }

  private boolean entered = false;
  
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

  private Type[] javaArgTypes()
  {
    Type[] res = new Type[parameters.length];

    for(int n = 0; n < parameters.length; n++)
      res[n] = formals[n].atNull() 
	? Types.javaType(PrimitiveType.nullTC)
	: Types.javaType(parameters[n].getMonotype());

    return res;
  }

  gnu.expr.ReferenceExp ref;

  public gnu.expr.ReferenceExp getRefExp()
  {
    if (ref == null)
      {
	if (declaration instanceof NiceMethod)
	  ref = compile((NiceMethod) declaration);
	else
	  compile((JavaMethod) declaration);
      }

    return ref;
  }

  public void compile()
  {
    if(Debug.codeGeneration)
      Debug.println("Compiling method body " + this);

    getRefExp();
  }

  private gnu.expr.ReferenceExp compile (NiceMethod definition)
  {
    gnu.expr.LambdaExp lexp = createMethod(name.toString(), false);
    gnu.expr.ReferenceExp ref = module.addMethod(lexp, true);

    lexp.addBytecodeAttribute
      (new MiscAttr("definition", definition.getFullName().getBytes()));
    lexp.addBytecodeAttribute
      (new MiscAttr("patterns", 
		    Pattern.bytecodeRepresentation(formals).getBytes()));

    return ref;
  }

  final TypeConstructor firstArgument()
  {
    return formals[0].tc;
  }

  NiceClass declaringClass()
  {
    try {
      return (NiceClass) ClassDefinition.get(firstArgument()).implementation;
    }
    catch (ClassCastException e) {
      throw User.error(this, declaration + " is a native method.\nIt can not be overriden because " + formals[0].tc + " is not a class defined in Nice");
    }
  }

  private void compile (JavaMethod declaration)
  {
    gnu.expr.LambdaExp lexp = createMethod(declaration.getName().toString(), true);
    
    // Compile as a method in the class of the first argument
    declaringClass().addJavaMethod(lexp);
  }

  private gnu.expr.LambdaExp createMethod (String bytecodeName, boolean member)
  {
    gnu.expr.LambdaExp lexp = 
      Gen.createMethod(bytecodeName, 
		       javaArgTypes(),
		       declaration.javaReturnType(),
		       parameters,
		       true, member);
    Gen.setMethodBody(lexp, body.generateCode());
    return lexp;
  }

  gnu.expr.Expression[] compiledArguments()
  {
    return VarSymbol.compile(parameters);
  }

  private bossa.link.Alternative alternative;

  bossa.link.Alternative getAlternative() { return alternative; }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return name + "(" + Util.map("", ", ", "", formals) + ")";
  }

  public Pattern[] getPatterns() { return formals; }

  private MethodDeclaration declaration;
  protected MonoSymbol[] parameters;
  private Pattern[] formals;
  Collection /* of LocatedString */ binders; // Null if type parameters are not bound
  private Statement body;
}

