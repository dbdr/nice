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

import gnu.bytecode.*;
import java.util.*;

import bossa.util.Debug;
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
   * Describe constructor here.
   *
   * @param name the name of the method.
   * @param binders 
   *    used to rebind the type parameters of the method definition. 
   *    Null if one doesn't want to rebind them.
   * @param newTypeVars list of TypeSymbol
   * @param newConstraint list of AtomicConstraint
   * @param formals list of Pattern
   * @param body s statement representing the body of the method.
   */
  public MethodBodyDefinition(LocatedString name, 
			      Collection binders,
			      //List newTypeVars,
			      List formals, 
			      Statement body)
  {
    super(name,Node.down);

    this.binders = binders; 

    //this.newTypeVars = newTypeVars;

    this.formals = formals;
    this.body = body;
    this.definition = null;
    
    addChild(this.body);

    if(name.content.equals("main") && formals.size()==1) // no dummy "this" parameter
      module.isRunnable(true);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }
  
  private Collection buildSymbols(Collection names, Monotype[] types)
  {
    if(names.size() != types.length)
      switch(types.length){
      case 0: User.error(this,"Method "+name+" has no parameters");
      case 1: User.error(this,"Method "+name+" has 1 parameter");
      default:User.error(this,
			 "Method "+name+" has "+types.length+" parameters");
      }
    
    Collection res = new ArrayList(names.size());
    int tn = 0;
    for(Iterator n = names.iterator();
	n.hasNext();)
      {
	Pattern p = (Pattern)n.next();
	Monotype domt = types[tn++];

	LocatedString typeName = p.name.cloneLS();
	typeName.prepend("type of ");
	
	MonotypeVar type = new MonotypeVar(typeName.toString());
	if(!p.isSharp())
	  type.rememberToImplementTop();
	
	res.add(new MonoSymbol(p.name, type));
      }
    return res;
  }
  
  private void setDeclaration(MethodDeclaration d)
  {
    if(d==null)
      User.error(this,"Method \""+name+"\" has not been declared");

    this.definition = d;
    
    // if the method is not a class member,
    // the "this" formal is useless
    /*
    if(!d.isMember())
      {
	if(!((Pattern)formals.get(0)).thisAtNothing())
	  User.error(this,
		     "Method \""+name+"\" is a global method"+
		     ", it cannot have a main pattern");

	formals.remove(0);
      }
    */
    
    parameters = buildSymbols(this.formals,definition.symbol.type.domain());
    scope.addSymbols(parameters);
  }

  /**
   * Returns the symbol of the method this declaration belongs to
   */
  private VarSymbol findSymbol(VarScope scope)
  {
    VarSymbol res;
    
    List symbols = scope.lookup(name);
    if(symbols.size() == 0) return null;

    if(symbols.size() == 1)
      return (VarSymbol) symbols.get(0);

    TypeConstructor[] tags = Pattern.getTC(formals);
    
    for(Iterator i = symbols.iterator(); i.hasNext();){
      VarSymbol s = (VarSymbol)i.next();
      if(!(s instanceof MethodDeclaration.Symbol))
	{
	  i.remove();
	  continue;
	}
      
      MethodDeclaration m = 
	(MethodDeclaration)((MethodDeclaration.Symbol)s).definition;

      // It doesn't make sense to define a body for a native method, does it ?
      if(!(m instanceof NiceMethod) 
	 || m.getArity() != formals.size())
	{
	  i.remove();
	  continue;
	}
	  
      try{
	int level;
	if(Debug.overloading)
	  level = Typing.enter("Trying definition "+m+" for method body "+name);
	else
	  level = Typing.enter();
	    
	try{
	  Constraint.assert(m.getType().getConstraint());
	  Typing.leq(tags, m.getType().domain());
	}
	finally{
	  if(Typing.leave() != level)
	    Internal.error("Enter/Leave error");
	}
      }
      catch(TypingEx e){
	if(Debug.typing) Debug.println("Not the right choice :"+e);
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
    for(Iterator i = symbols.iterator(); i.hasNext();){
      MethodDeclaration m = 
	((MethodDeclaration.Symbol)i.next()).definition;
      methods+=m+" defined "+m.location()+"\n";
    }
    
    User.error(this,
	       "There is an ambiguity about which version " + 
	       " of the overloaded method \"" + name + 
	       "\" this alternative belongs to.\n" +
	       "Try to use more patterns.\n\n" +
	       "Possible methods:\n" +
	       methods);
    return null;
  }
  
  Scopes buildScope(VarScope outer, TypeScope typeOuter)
  {
    Scopes res = super.buildScope(outer, typeOuter);

    // we just want Java classes to be discovered at this stage,
    // to add them in the rigid context.

    // Binders must be taken into account,
    // to distinguish them from Java Classes
    if(binders != null) 
      {
	try{ this.typeScope.addMappings(binders,null); } 
	catch(mlsub.typing.BadSizeEx e) {
	  Internal.error("Should not happen");
	}
	catch(TypeScope.DuplicateName e) {
	  User.error(this, e);
	}
      }

    //if(newTypeVars != null)
    //this.typeScope.addSymbols(newTypeVars);
    
    return res;
  }

  void doResolve()
  {
    //Resolution of the body is delayed to enable overloading

    // we look for java classes
    body.doFindJavaClasses();
    
    Pattern.resolveTC(typeScope, formals);
  }
  
  void lateBuildScope()
  {
    VarSymbol s = findSymbol(scope);

    if(s==null)
      User.error(this, name+" is not defined");
    
    if(!(s instanceof MethodDeclaration.Symbol))
      User.error(this, name+" is not a method");

    MethodDeclaration def = ((MethodDeclaration.Symbol) s).definition;
    
    if(!(def instanceof NiceMethod))
      User.error(this, name + " is not a Nice method.\n" +
		 "An alternative cannot be defined for it.\n" +
		 name + " was defined at " + def.location()
		 );
    
    //Debug.println("Def for "+this+" is "+s+" "+s.location());
    
    setDeclaration(def);

    // Get type parameters
    if(binders != null)
    try{
      typeScope.addMappings
	(binders,
	 definition.getType().getConstraint().binders());
    }
    catch(mlsub.typing.BadSizeEx e){
      User.error(name,
		 "Method \"" + name + 
		 "\" expects " + e.expected + " type parameters");
    }
    catch(TypeScope.DuplicateName e) {
      User.error(this, e);
    }

    // We can resolve now
    Pattern.resolveType(this.typeScope, formals);
    super.doResolve();
  }

  /****************************************************************
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
    //Nothing
  }
  
  /****************************************************************
   * Type checking
   ****************************************************************/

  public mlsub.typing.Monotype getReturnType()
  {
    return definition.getType().codomain();
  }

  void typecheck()
  {
    lateBuildScope();

    Typing.enter("METHOD BODY " + this + "\n\n");
    // remeber that enter was called, to leave.
    // usefull if previous code throws an exception
    entered = true;

    try{
      try { Constraint.assert(definition.getType().getConstraint()); }
      catch(TypingEx e){
	User.error(name,
		   "the constraint will never be satisfied",
		   ": "+e.getMessage());
      }
      
      Monotype[] monotypes = MonoSymbol.getMonotype(parameters);
      Typing.introduce(monotypes);

      Typing.leqMono
	(monotypes,
	 definition.getType().domain());
      
      try{
	Typing.in
	  (monotypes,
	   Pattern.getDomain(formals));

	nice.tools.code.Types.setBytecodeType(monotypes);

	Typing.implies();
      }
      catch(TypingEx e){
	User.error(name,"The patterns are not correct", e);
      }
      
      // New Constraint
      //if(newTypeVars != null)
      //Typing.enter(newTypeVars,
      //"Binding-constraint for "+name);
      
      for(Iterator f = formals.iterator(), 
	    p = parameters.iterator();
	  f.hasNext();)
	{
	  Pattern pat = (Pattern)f.next();
	  MonoSymbol sym = (MonoSymbol) p.next();
	  
	  Monotype type = pat.getType();
	  if(type==null)
	    continue;
	  
	  try{
	    if(type instanceof MonotypeConstructor)
	      {
		TypeConstructor 
		  tc = ((MonotypeConstructor) type).getTC(),
		  formalTC = ((MonotypeConstructor) sym.getMonotype()).getTC();
		
		tc.setId(formalTC.getId());
	      }
	    else
	      Internal.error("Not implemented ?");
	  
	    Typing.eq(type, sym.getMonotype());
	  }
	  catch(TypingEx e){
	    User.error(pat.name, 
		       "\":\" constraint for argument "+pat.name+
		       " is not correct",
		       ": "+e);
	  }       
	}
      // end of New Constraint
    }
    catch(mlsub.typing.BadSizeEx e){
      Internal.error("Bad size in MethodBodyDefinition.typecheck()");
    }
    catch(TypingEx e) {
      User.error(name,"Typing error in method body \""+name+"\":\n"+e);
    }
  }

  private boolean entered = false;
  
  void endTypecheck()
  {
    try{
      if(entered)
	{
	  Typing.leave();
	  entered = false;
	}
    }
    catch(TypingEx e){
      User.error(this,
		 "Type error in method "+name, " :"+e);
    }
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

  private gnu.expr.BlockExp blockExp;
  public gnu.expr.BlockExp getBlock() { return blockExp; }
  
  private Type[] javaArgTypes()
  {
      //Type[] defTypes = definition.javaArgTypes();
    Type[] res = new Type[parameters.size()];

    Iterator p;
    int n = 0;
    for(p = parameters.iterator(); p.hasNext(); n++)
      {
	MonoSymbol param = (MonoSymbol) p.next();
	res[n] = nice.tools.code.Types.javaType(param.getMonotype());
      }

    return res;
  }

  public void compile()
  {
    if(Debug.codeGeneration)
      Debug.println("Compiling method body " + this);
    
    if(definition == null)
      Internal.error(this, this+" has no definition");
    
    blockExp = new gnu.expr.BlockExp(definition.javaReturnType());

    gnu.expr.LambdaExp lexp = new gnu.expr.LambdaExp(blockExp);
    Statement.currentScopeExp = lexp;
    lexp.setName(name.toString());

    Type[] javaArgTypes = javaArgTypes();

    Method primMethod = module.getOutputBytecode().addMethod
      (nice.tools.code.Strings.escape
       (definition.getBytecodeName()+Pattern.bytecodeRepresentation(formals)),
       javaArgTypes, definition.javaReturnType(),
       Access.PUBLIC|Access.STATIC|Access.FINAL);
    new MiscAttr("definition", 
		 definition.getFullName().getBytes())
      .addToFrontOf(primMethod);

    lexp.setPrimMethod(primMethod);

    if(name.content.equals("main") && formals.size()==1)
      module.setMainAlternative(lexp.getMainMethod());

    // Parameters
    lexp.min_args = lexp.max_args = parameters.size();

    Iterator p;
    int n = 0;
    for(p = parameters.iterator(); p.hasNext(); n++)
      {
	MonoSymbol param = (MonoSymbol) p.next();
	
	gnu.expr.Declaration d = lexp.addDeclaration(param.name.toString());
	d.setParameter(true);
	d.setType(javaArgTypes[n]);
	d.noteValue(null);
	param.setDeclaration(d);
      }

    blockExp.setBody(body.generateCode());

    module.compileMethod(lexp);

    //Register this alternative for the link test
    new bossa.link.Alternative(this.definition,
			       Pattern.getTC(this.formals),
			       module.getOutputBytecode(),
			       lexp.getMainMethod());
  }

  //  public static void compileMethod(gnu.expr.LambdaExp lexp, ClassType inClass, String name)
//    {
//      gnu.expr.Compilation comp = new gnu.expr.Compilation
//        (inClass,name,
//         name,"prefix??",false);
    
//      lexp.compileAsMethod(comp);

//      comp.compileClassInit();
//    }
  
  public static void compileMain(bossa.modules.Package module, 
				 gnu.bytecode.Method mainAlternative)
  {
    if(Debug.codeGeneration)
      Debug.println("Compiling MAIN method");
    
    gnu.expr.BlockExp block = new gnu.expr.BlockExp(Type.void_type);
    gnu.expr.LambdaExp lexp = new gnu.expr.LambdaExp(block);
    
    // Main arguments
    lexp.min_args = lexp.max_args = 1;
    gnu.expr.Declaration args = lexp.addDeclaration("args");
    args.setParameter(true);
    args.noteValue(null);
    args.setType(gnu.bytecode.ArrayType.make(Type.string_type));

    // Call arguments
    gnu.expr.Expression[] eVal = new gnu.expr.Expression[] 
    { new gnu.expr.ReferenceExp("args",args) };

    block.setBody
      (new gnu.expr.ApplyExp(new gnu.expr.PrimProcedure(mainAlternative),
			     eVal));

    lexp.setPrimMethod(module.getOutputBytecode().addMethod
      ("main",
       "([Ljava.lang.String;)V",
       Access.PUBLIC | Access.STATIC));

    module.compileMethod(lexp);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return name + "(" + Util.map("", ", ", "", formals) + ")";
  }

  /*private*/ MethodDeclaration definition;
  protected Collection /* of FieldSymbol */  parameters;
  protected List       /* of Patterns */   formals;
  Collection /* of LocatedString */ binders; // Null if type parameters are not bound
  private Statement body;

  //private List /* of TypeSymbol */  newTypeVars;
}
