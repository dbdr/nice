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

// File    : Alternative.java
// Created : Mon Nov 15 12:20:40 1999 by bonniot
//$Modified: Tue Jul 18 16:40:57 2000 by Daniel Bonniot $

package bossa.link;

import bossa.util.*;

import mlsub.typing.*;

import bossa.syntax.MethodDefinition;
import bossa.syntax.Pattern;
import bossa.syntax.LocatedString;
import bossa.syntax.Node;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

/**
 * Represents a method alternative in the link.
 *
 * It can be build either from information in a Bossa source,
 * or read from a compiled bytecode file.
 *
 * @author bonniot
 */

public class Alternative
{
  /**
   * When read from a source file.
   */
  public Alternative(MethodDefinition m, TypeConstructor[] patterns, 
		     ClassType c, Method method)
  {
    this.definitionClass = c;
    this.primProcedure = new PrimProcedure(method);
    
    this.methodName=m.getFullName();
    this.patterns = patterns;
    add();
  }

  /**
   * When read from a bytecode file.
   */
  public Alternative(String s, ClassType c, Method m)
  {
    this.definitionClass = c;
    this.primProcedure = new PrimProcedure(m);
    
    //Debug.println(s);
    
    int numCode = s.indexOf('$');
    if(numCode==-1)
      Internal.error("Methd "+s+" in class "+c.getName()+
		     " has no valid name");

    int at = s.indexOf(Pattern.AT_encoding, numCode+1);
    if(at==-1)
      // This is valid if this method has no parameter
      at = s.length();
    
    methodName = c.getName().substring(0,c.getName().length()-".package".length()).replace('.','$')
      +"$"+s.substring(0,at);
    
    //Debug.println("name="+methodName);
    
    ArrayList patterns = new ArrayList(5);

    while(at<s.length())
      {
	int next = s.indexOf(Pattern.AT_encoding,at+Pattern.AT_len);
	if(next==-1)
	  next=s.length();
	
	String name = s.substring(at+Pattern.AT_len,next);
	//Debug.println("pattern="+name);
	if(name.equals("_"))
	  patterns.add(null);
	else
	  {
	    TypeSymbol tc = Node.getGlobalTypeScope().lookup(name);
	    
	    patterns.add((TypeConstructor) tc);
	  }
	
	at = next;
      }
    
    this.patterns = (TypeConstructor[]) 
      patterns.toArray(new TypeConstructor[patterns.size()]);
    
    add();
  }

  /****************************************************************
   * Graph traversal
   ****************************************************************/

  static final int 
    UNVISITED = 1,
    VISITING  = 2,
    VISITED   = 3;
  
  int mark = UNVISITED;
  
  /****************************************************************
   * Order on alternatives
   ****************************************************************/

  /**
   * Returns true iff 'a' is more precise than 'b'.
   */
  static boolean leq(Alternative a, Alternative b)
  {
    for(int i=0; i<a.patterns.length; i++)
      {
	TypeConstructor tb = b.patterns[i];
	if(tb==null)
	  continue;
	
	TypeConstructor ta = a.patterns[i];
	if(ta==null)
	  return false;
	
	if(!Typing.testRigidLeq(ta, tb))
	  return false;
      }
    return true;
  }

  /**
   * Tests the matching of tags against a method alternative.
   */
  boolean matches(TypeConstructor[] tags)
  {
    for(int i=0; i<patterns.length; i++)
      {
	TypeConstructor td = patterns[i];
	if(td ==null)
	  continue;
	
	TypeConstructor tc = tags[i];
	// a null tc is an unmatchable argument (e.g. function)
	if(tc==null)
	  return false;
	
	if(!Typing.testRigidLeq(tc, td))
	  return false;
      }

    return true;
  }

  /**
   * @return the expression that represents the method body.
   */
  gnu.expr.Expression methodExp()
  {
    return new gnu.expr.QuoteExp(primProcedure);
  }
  
  /**
   * @return the expression that tests if this alternative 
   * the tuple of 'parameters'.
   */
  gnu.expr.Expression matchTest(gnu.expr.Expression[] parameters)
  {
    if (parameters.length != patterns.length)
      Internal.error("Incorrect parameters "+
		     Util.map("",", ","",parameters)+
		     " for " + this);
    
    gnu.expr.Expression result = QuoteExp.trueExp;
    
    for(int n = parameters.length-1; n>=0; n--)
      result = new gnu.expr.IfExp(matchTest(patterns[n],parameters[n]),
				  result,
				  QuoteExp.falseExp);
    
    return result;
  }
  
  private static gnu.expr.Expression matchTest(TypeConstructor dom, 
					       gnu.expr.Expression parameter)
  {
    if(dom == null)
      return QuoteExp.trueExp;

    /*
    ListIterator types = d.getMonotype().getTC().getJavaInstanceTypes();
    
    gnu.expr.Expression res =
      instanceOfExp(parameter,(gnu.bytecode.Type) types.next());
    
    while(types.hasNext())
      res = orExp(instanceOfExp(parameter,(gnu.bytecode.Type) types.next()),res);
    */

    return instanceOfExp(parameter, bossa.CodeGen.javaType(dom));
  }

  private static final gnu.mapping.Procedure instanceProc = 
    new gnu.kawa.reflect.InstanceOf(gnu.expr.Interpreter.getInterpreter());
  
  static gnu.expr.Expression instanceOfExp(gnu.expr.Expression value, gnu.bytecode.Type ct)
  {
    gnu.expr.Expression[] callParams = new gnu.expr.Expression[2];
    callParams[0]=value;
    callParams[1]=new QuoteExp(ct);
    
    return new ApplyExp(new QuoteExp(instanceProc),callParams);
  }
  
  private static final gnu.mapping.Procedure orProc =
    new kawa.standard.or();
  
  private static gnu.expr.Expression orExp(gnu.expr.Expression e1, gnu.expr.Expression e2)
  {
    gnu.expr.Expression[] callParams = new gnu.expr.Expression[2];
    callParams[0]=e1;
    callParams[1]=e2;
    
    return new ApplyExp(new QuoteExp(orProc),callParams);
  }
  
  public String toString()
  {
    return methodName+Util.map("(",", ",")",patterns);
  }

  /** The bytecode class this alternative is defined in */
  ClassType definitionClass;

  private PrimProcedure primProcedure;
  
  String methodName;
  TypeConstructor[] patterns;

  boolean visiting;
  
  /****************************************************************
   * Regrouping alternatives per method
   ****************************************************************/

  private static HashMap alternatives = new HashMap();
  
  private void add()
  {
    List l = (List) alternatives.get(methodName);
    if(l==null)
      {
	l = new ArrayList();
	alternatives.put(methodName,l);
      }
    l.add(this);
  }
  
  static List listOfAlternative(MethodDefinition m)
  {
    return (List) alternatives.get(m.getFullName());
  }
}
