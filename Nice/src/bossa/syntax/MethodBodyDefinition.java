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

// File    : MethodBodyDefinition.java
// Created : Thu Jul 01 18:12:46 1999 by bonniot
//$Modified: Sat Dec 04 12:48:00 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;
import bossa.modules.Module;

import gnu.bytecode.*;
import java.util.*;

/**
 * Definition of an alternative for a method.
 */
public class MethodBodyDefinition extends Node 
  implements Definition, Located
{
  public MethodBodyDefinition(LocatedString name, 
			      Collection binders,
			      List formals, List body)
  {
    super(Node.down);
    this.name=name;
    this.binders=binders; 
    this.formals=formals;
    this.body=new Block(body);
    this.definition=null;
    
    addChild(this.body);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }
  
  private Collection buildSymbols(Collection names, Collection types)
  {
    if(names.size()!=types.size())
      switch(types.size()){
      case 0: User.error(this,"Method "+name+" has no parameters");
      case 1: User.error(this,"Method "+name+" has 1 parameter");
      default:User.error(this,"Method "+name+" has "+types.size()+
			 " parameters");
      }
    
    Collection res=new ArrayList(names.size());
    for(Iterator n=names.iterator(),t=types.iterator();
	n.hasNext();)
      {
	Pattern p=(Pattern)n.next();
	Monotype domt=(Monotype)t.next();
	res.add(new MonoSymbol(p.name,
			       Monotype.fresh(p.name,domt)));
      }
    return res;
  }
  
  private void setDefinition(MethodDefinition d)
  {
    if(d==null)
      User.error(this,"Method \""+name+"\" has not been declared");

    this.definition=d;
    
    // if the method is not a class member,
    // the "this" formal is useless
    if(!d.isMember())
      {
	if(!((Pattern)formals.get(0)).thisAtNothing())
	  User.error(this,
		     "Method \""+name+"\" is a global method"+
		     ", it cannot have a main pattern");

	formals.remove(0);
      }
    
    parameters=buildSymbols(this.formals,definition.type.domain());
    addSymbols(parameters);
  }

  /**
   * Returns the symbol of the method this declaration belongs to
   */
  private VarSymbol findSymbol(VarScope scope)
  {
    VarSymbol res;
    if(!(scope.overloaded(name)))
      return scope.lookupOne(name);

    Collection symbols=scope.lookup(name);
    if(symbols.size()==0) return null;
    
    // TODO
    for(Iterator i=symbols.iterator();i.hasNext();){
      VarSymbol s=(VarSymbol)i.next();
      if(!(s instanceof MethodDefinition))
	i.remove();
      else
	{
	  MethodDefinition m=(MethodDefinition)s;
	  try{
	    Typing.enter("Trying definition "+m+" for method body "+name);
	    Typing.introduce(m.type.getTypeParameters());
	    m.type.getConstraint().assert();
	    Typing.in(Pattern.getPolytype(formals),
		      Domain.fromMonotypes(m.getType().domain()));
	    Typing.implies();
	    Typing.leave();
	  }
	  catch(TypingEx e){
	    if(Typing.dbg) Debug.println("Not the right choice :"+e);
	    i.remove();
	  }
	  catch(BadSizeEx e){
	    i.remove();
	  }
	}
    }
    if(symbols.size()==1) return (VarSymbol)symbols.iterator().next();

    if(symbols.size()==0)
      User.error(this,
		 "No definition of \""+name+"\" is compatible with the patterns");

    User.error(this,"There is an ambiguity about which version of the overloaded method \""+
	       name+"\" this alternative belongs to.\n"+
	       "Try to use more patterns.");
    return null;
  }
  
  Scopes buildScope(VarScope outer, TypeScope typeOuter)
  // The scoping is delayed to enable overloading
  {
    this.scope=outer;
    this.typeScope=typeOuter;
    return new Scopes(outer,typeOuter);
  }

  void doResolve()
  {
    // Resolution is delayed to enable overloading
  }
  
  void lateBuildScope(VarScope outer, TypeScope typeOuter)
  {
    Pattern.resolve(typeScope,formals);
    VarSymbol s=findSymbol(outer);

    if(s==null)
      User.error(this,name+" is not defined");
    
    if(!(s instanceof MethodDefinition))
      User.error(this,name+" is not a method");

    setDefinition((MethodDefinition)s);

    // Get type parameters
    if(binders!=null)
    try{
      addTypeMaps
	(binders,
	 definition.type.getConstraint().binders);
    }
    catch(BadSizeEx e){
      User.error(name,"Method \""+name+"\" expects "+e.expected+
		 " type parameters");
    }

    Scopes res=super.buildScope(outer,typeOuter);
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

  void typecheck()
  {
    lateBuildScope(this.scope,this.typeScope);
    super.doResolve();
    
    Typing.enter(definition.type.getTypeParameters(),
		 "method body of "+name);
    
    try{
      try { definition.type.getConstraint().assert(); }
      catch(TypingEx e){
	User.error(name,"the constraint will never be satisfied",": "+e.getMessage());
      }
      
      Collection monotypes=MonoSymbol.getMonotype(parameters);
      Typing.introduce(monotypes);
      
      Typing.leqMono
	(monotypes,
	 definition.type.domain());
      
      try{
	Typing.in
	  (VarSymbol.getType(parameters),
	   Pattern.getDomain(formals));
      }
      catch(TypingEx e){
	User.error(name,"The patterns are not correct");
      }
      
      Typing.implies();

    }
    catch(BadSizeEx e){
      Internal.error("Bad size in MethodBodyDefinition.typecheck()");
    }
    catch(TypingEx e) {
      User.error(name,"Typing error in method body \""+name+"\":\n"+e);
    }
  }

  void endTypecheck()
  {
    try{
      Polytype t=body.getType();
      if(t==null)
	User.error(this,"Last statement of body should be \"return\"");
      Typing.leq(t,new Polytype(definition.type.codomain()));
      Typing.leave();
    }
    catch(TypingEx e){
      User.error(this,"Return type is not correct"," :"+e);
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

  public void compile()
  {
    if(Debug.codeGeneration)
      Debug.println("Compiling "+name);
    
    gnu.expr.BlockExp block = new gnu.expr.BlockExp();
    Statement.currentMethodBlock=block;
    gnu.expr.LambdaExp lexp = new gnu.expr.LambdaExp(block);
    
    lexp.primMethod=module.bytecode.addMethod
      (definition.getBytecodeName()+Pattern.bytecodeRepresentation(formals),
       definition.javaArgTypes(),definition.javaReturnType(),
       Access.PUBLIC|Access.STATIC|Access.FINAL);

    if(name.content.equals("main"))
      mainAlternative=lexp.primMethod;

    // Parameters
    lexp.min_args=lexp.max_args=parameters.size();
    Iterator p,t;
    for(p=parameters.iterator(),t=definition.type.domain().iterator();
	p.hasNext();)
      {
	MonoSymbol param = (MonoSymbol) p.next();
	Monotype paramType = (Monotype) t.next();
	
	gnu.expr.Declaration d = lexp.addDeclaration(param.name.toString());
	d.setParameter(true);
	d.setType(paramType.getJavaType());
	param.setDeclaration(d);
      }

    block.setBody(body.generateCode());

    lexp.compileAsMethod(module.compilation);

    //Register this alternative for the link test
    new bossa.link.Alternative(this.definition,Pattern.getDomain(this.formals),module.bytecode,lexp.primMethod);
  }

  //  public static void compileMethod(gnu.expr.LambdaExp lexp, ClassType inClass, String name)
//    {
//      gnu.expr.Compilation comp = new gnu.expr.Compilation
//        (inClass,name,
//         name,"prefix??",false);
    
//      lexp.compileAsMethod(comp);

//      comp.compileClassInit();
//    }
  
  private static gnu.bytecode.Method mainAlternative=null;
  
  public static boolean hasMain()
  {
    return mainAlternative!=null;
  }
  
  static void compileMain(bossa.modules.Module module)
  {
    if(mainAlternative==null)
      return;
    
    if(Debug.codeGeneration)
      Debug.println("Compiling main method");
    
    gnu.expr.BlockExp block = new gnu.expr.BlockExp();
    gnu.expr.LambdaExp lexp = new gnu.expr.LambdaExp(block);
    
    // Main arguments
    lexp.min_args=lexp.max_args=1;
    gnu.expr.Declaration args=lexp.addDeclaration("args");
    args.setParameter(true);
    args.setType(new ArrayType(Type.string_type));

    // Call arguments
    gnu.expr.Expression[] eVal=new gnu.expr.Expression[1];
    eVal[0]= new gnu.expr.ReferenceExp("args",args);

    block.setBody
      (new gnu.expr.ApplyExp(new gnu.expr.QuoteExp(new gnu.expr.PrimProcedure(mainAlternative)),eVal));

    lexp.primMethod=module.bytecode.addMethod
      ("main",
       "([Ljava.lang.String;)V",
       Access.PUBLIC | Access.STATIC);

    lexp.compileAsMethod(module.compilation);
  }

  /****************************************************************
   * Module
   ****************************************************************/
  
  private bossa.modules.Module module;
  
  public void setModule(bossa.modules.Module module)
  {
    this.module = module;
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public bossa.util.Location location()
  {
    return name.location();
  }

  public String toString()
  {
    String res;
    res=name.toString()
      +"("
      + Util.map("",", ","",parameters)
      + ") "
      + body
      + "\n";
    return res;
  }

  private MethodDefinition definition;
  protected LocatedString name;
  protected Collection /* of FieldSymbol */  parameters;
  protected List       /* of Patterns */   formals;
  Collection /* of LocatedString */ binders; // Null if type parameters are not bound
  private Block body;
}
