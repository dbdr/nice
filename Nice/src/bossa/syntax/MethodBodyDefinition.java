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
//$Modified: Wed Nov 10 18:52:01 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;

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
    User.error(d==null,this,"Method \""+name+"\" has not been declared");
    this.definition=d;
    
    // if the method is not a class member,
    // the "this" formal is useless
    if(!d.isMember())
      {
	User.error(!((Pattern)formals.get(0)).thisAtNothing(),
		   this,
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
	    Typing.implies();
	    Typing.in(Pattern.getPolytype(formals),
		      Domain.fromMonotypes(m.getType().domain()));
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
    User.error(symbols.size()==0,this,
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

    User.error(s==null,this,name+" is not defined");
    User.error(!(s instanceof MethodDefinition),this,name+" is not a method");
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

  public void compile(bossa.modules.Module module)
  {
    if(Debug.codeGeneration)
      Debug.println("Compiling "+name);
    
    Method m=module.bytecode.addMethod
      (name.toString()+Pattern.bytecodeRepresentation(formals),
       definition.javaArgTypes(),definition.javaReturnType(),
       Access.PUBLIC|Access.STATIC|Access.FINAL);

    if(name.content.equals("main"))
      mainAlternative=m;

    m.initCode();

    Statement.currentMethodBlock=new gnu.expr.BlockExp();
    Statement.currentMethodBlock.exitLabel=new gnu.bytecode.Label(m);

    // args
    gnu.expr.Expression[] eVal=new gnu.expr.Expression[parameters.size()];
    gnu.expr.LetExp let=new gnu.expr.LetExp(eVal);
    
    int n=0;
    for(Iterator i=parameters.iterator();
	i.hasNext();n++)
      {
	MonoSymbol param = (MonoSymbol) i.next();
	eVal[n]=new gnu.expr.QuoteExp(null);
	param.decl=let.addDeclaration(param.name.toString());
      }
    let.setBody(body.compile());
    
    // obscure
    gnu.expr.Compilation comp=new gnu.expr.Compilation(m,module.bytecode,module.name,module.name,"prefix??",false);
    
    //gnu.expr.Expression bodyExp=body.compile();
    //Statement.currentMethodBlock.setBody(bodyExp);
    Statement.currentMethodBlock.setBody(let);
    Statement.currentMethodBlock.compile(comp,definition.javaReturnType());
    m.getCode().emitReturn();
  }

  private static gnu.bytecode.Method mainAlternative=null;
  
  static void compileMain(bossa.modules.Module module)
  {
    if(mainAlternative==null)
      return;
    
    if(Debug.codeGeneration)
      Debug.println("Compiling main method");
    
    Method m=module.bytecode.addMethod
      ("main",
       "([Ljava.lang.String;)V",
       Access.PUBLIC | Access.STATIC);
    m.initCode();

    Statement.currentMethodBlock=new gnu.expr.BlockExp();
    Statement.currentMethodBlock.exitLabel=new gnu.bytecode.Label(m);

    // args
    gnu.expr.Expression[] eVal=new gnu.expr.Expression[1];
    eVal[0]=new gnu.expr.QuoteExp(null);

    gnu.expr.LetExp let=new gnu.expr.LetExp(eVal);
    gnu.expr.Declaration args=let.addDeclaration("args");
    gnu.expr.Expression mainBody=new gnu.expr.ApplyExp
      (new gnu.expr.QuoteExp(new gnu.expr.PrimProcedure(mainAlternative)),eVal);

    let.setBody(mainBody);

    // obscure
    gnu.expr.Compilation comp=new gnu.expr.Compilation(m,module.bytecode,module.name,module.name,"prefix??",false);
    
    Statement.currentMethodBlock.setBody(let);
    Statement.currentMethodBlock.compile(comp,gnu.bytecode.Type.void_type);
    m.getCode().emitReturn();
  }




//    static void compileMain(bossa.modules.Module)
//    {
//      if(mainAlternative==null)
//        // No main method alternative in this module.
//        // It won't be runnable
//        return;
    
//      if(Debug.codeGeneration)
//        Debug.println("Compiling main method");
    
//      Method m=module.bytecode.addMethod
//        ("main",
//         "([Ljava.lang.String;)V",
//         Access.PUBLIC | Access.STATIC);
//      m.initCode();

//      Statement.currentMethodBlock=new gnu.expr.BlockExp();
//      Statement.currentMethodBlock.exitLabel=new gnu.bytecode.Label(m);
//      // Body
//      gnu.expr.Expression[] callArg = new gnu.expr.Expression[1];
//      callArg[0]=new gnu.expr.QuoteExp(null);
//      Statement.currentMethodBlock.setBody
//        (new gnu.expr.ApplyExp
//         (new gnu.expr.QuoteExp(new gnu.expr.PrimProcedure(mainAlternative)),callArg));

//      gnu.expr.LambdaExp bodyExp = new gnu.expr.LambdaExp(Statement.currentMethodBlock);
//      bodyExp.outer=new gnu.expr.ModuleExp();
//      gnu.expr.Declaration arg0=bodyExp.addDeclaration("arg0");

//      gnu.expr.Compilation comp=new gnu.expr.Compilation(bodyExp,module.name,"prefix??",false);
//      comp.method=m;
//      bodyExp.primMethod=m;
    
//      //Statement.currentMethodBlock.compile(comp,gnu.bytecode.Type.void_type);
//      bodyExp.compile(comp,gnu.bytecode.Type.void_type);
//      //m.getCode().emitReturn();
//    }

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
