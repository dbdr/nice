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
//$Modified: Thu Sep 07 17:26:50 2000 by Daniel Bonniot $

package bossa.link;

import bossa.util.*;

import mlsub.typing.*;

import bossa.syntax.MethodDefinition;
import bossa.syntax.Pattern;
import bossa.syntax.LocatedString;
import bossa.syntax.Node;
import nice.tools.code.*;

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
    
    this.methodName = m.getFullName();
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
    if (numCode == -1)
      Internal.error("Method " + s + " in class " + c.getName() +
		     " has no valid name");

    int at = s.indexOf(Pattern.AT_encoding, numCode+1);
    if (at == -1)
      // This is valid if this method has no parameter
      at = s.length();
    
    MiscAttr attr = (MiscAttr) Attribute.get(m, "definition");
    if (attr == null)
      Internal.error("Method " + s + " in class " + c.getName() +
		     " has no definition");
    
    methodName = new String(attr.data);
    
    /*methodName = c.getName().substring(0,c.getName().length()-".package".length()).replace('.','$')
      +"$"+s.substring(0,at);*/
    
    //Debug.println("name=" + methodName);
    
    ArrayList patterns = new ArrayList(5);

    while(at<s.length())
      {
	int next = s.indexOf(Pattern.AT_encoding,at+Pattern.AT_len);
	if (next == -1)
	  next = s.length();
	
	String name = s.substring(at+Pattern.AT_len,next);
	//Debug.println("pattern=" + name);
	if (name.equals("_"))
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
  
  /** Marks the state of this node in the graph traversal. */
  int mark = UNVISITED;
  
  /****************************************************************
   * Order on alternatives
   ****************************************************************/

  /**
   * Returns true iff 'a' is more precise than 'b'.
   */
  static boolean leq(Alternative a, Alternative b)
  {
    for(int i = 0; i<a.patterns.length; i++)
      {
	TypeConstructor tb = b.patterns[i];
	if (tb == null)
	  continue;
	
	TypeConstructor ta = a.patterns[i];
	if (ta == null)
	  return false;
	
	if (!Typing.testRigidLeq(ta, tb))
	  return false;
      }
    return true;
  }

  /**
   * Tests the matching of tags against a method alternative.
   */
  boolean matches(TypeConstructor[] tags)
  {
    for(int i = 0; i<patterns.length; i++)
      {
	TypeConstructor td = patterns[i];
	if (td == null)
	  continue;
	
	TypeConstructor tc = tags[i];
	// a null tc is an unmatchable argument (e.g. function)
	if (tc == null)
	  return false;
	
	if (!Typing.testRigidLeq(tc, td))
	  return false;
      }

    return true;
  }

  /**
   * @return the expression that represents the method body.
   */
  Expression methodExp()
  {
    return new gnu.expr.QuoteExp(primProcedure);
  }
  
  /**
     @return the expression that tests if this alternative matches
     the tuple <code>parameters</code>.
   */
  Expression matchTest(Expression[] parameters)
  {
    if (parameters.length != patterns.length)
      Internal.error("Incorrect parameters "+
		     Util.map("",", ","",parameters)+
		     " for " + this);
    
    Expression result = QuoteExp.trueExp;
    
    for(int n = parameters.length-1; n >= 0; n--)
      result = new gnu.expr.IfExp(matchTest(patterns[n],parameters[n]),
				  result,
				  QuoteExp.falseExp);
    
    return result;
  }
  
  private static Expression matchTest(TypeConstructor dom, 
				      Expression parameter)
  {
    if (dom == null)
      return QuoteExp.trueExp;

    return instanceOfExp(parameter, nice.tools.code.Types.javaType(dom));
  }

  static Expression instanceOfExp(Expression value, Type ct)
  {
    return Inline.inline(new InstanceOfProc(ct), value);
  }
  
  /** Procedure to emit <code>or</code>. Shared since it is not parametrized.*/
  private static final Expression orProc = 
    new gnu.expr.QuoteExp(new nice.tools.code.OrProc());
  
  private static Expression orExp(Expression e1, Expression e2)
  {
    return new ApplyExp(orProc, new Expression[]{ e1, e2 });
  }
  
  public String toString()
  {
    return methodName + Util.map("(", ", ", ")", patterns);
  }

  /** The bytecode class this alternative is defined in. */
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
    if (l == null)
      {
	l = new ArrayList();
	alternatives.put(methodName,l);
      }
    // Dispatch.sort(final List alternatives) assumes that new alternatives
    // are added at the end
    l.add(this);
  }
  
  static List listOfAlternative(MethodDefinition m)
  {
    return (List) alternatives.get(m.getFullName());
  }
}
