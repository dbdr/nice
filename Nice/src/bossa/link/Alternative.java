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
//$Modified: Mon Jan 24 19:20:46 2000 by Daniel Bonniot $

package bossa.link;

import bossa.syntax.*;
import bossa.util.*;

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
  public Alternative(MethodDefinition m, List patterns, ClassType c, Method method)
  {
    this.definitionClass = c;
    this.primProcedure = new PrimProcedure(method);
    
    this.methodName=m.getFullName();
    this.patterns=patterns;
    add();
  }

  /**
   * When read from a bytecode file.
   */
  public Alternative(String s, ClassType c, Method m)
  {
    this.definitionClass = c;
    this.primProcedure = new PrimProcedure(m);
    
    int numCode = s.indexOf(Pattern.AT_encoding);
    if(numCode==-1)
      Internal.error("Method "+s+" in class "+c.getName()+
		     " has not a valid name");

    int at = s.indexOf(Pattern.AT_encoding,numCode+1);
    if(at==-1)
      // This is valid if this method has no parameter
      at = s.length();
    
    methodName = c.getName()+"$"+s.substring(0,at);
    
    this.patterns = new ArrayList();

    while(at<s.length())
      {
	int next = s.indexOf(Pattern.AT_encoding,at+1);
	if(next==-1)
	  next=s.length();
	
	String name = s.substring(at+1,next);
	if(name.equals("_"))
	  patterns.add(Domain.bot); // Domain Ex a .a 
	else
	  {
	    bossa.util.Location loc = bossa.util.Location.nowhere();
	    
	    Monotype type = new MonotypeConstructor
	      (new TypeConstructor(new LocatedString(name,loc)),
	       null,loc);

	    type = type.resolve(Node.getGlobalTypeScope());    
	    patterns.add(new Domain(null,type));
	  }
	
	at = next;
      }
    
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
    Iterator i = a.patterns.iterator();
    Iterator j = b.patterns.iterator();
    while(i.hasNext())
      {
	Domain pa = (Domain) i.next();
	Domain pb = (Domain) j.next();
	if(!Domain.leq(pa,pb))
	  return false;
      }
    return true;
  }

  /**
   * Tests the matching of tags against a method alternative.
   */
  boolean matches(TypeConstructor[] tags)
  {
    int n=0;
    for(Iterator i=patterns.iterator();
	i.hasNext();n++)
      if(!Domain.in(tags[n], (Domain) i.next()))
	return false;
    
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
    if(parameters.length!=patterns.size())
      Internal.error("Incorrect parameters "+
		     Util.map("",", ","",parameters)+
		     " for " + this);
    
    gnu.expr.Expression result = QuoteExp.trueExp;
    
    for(int n = parameters.length-1; n>=0; n--)
      result = new gnu.expr.IfExp(matchTest((Domain) patterns.get(n),parameters[n]),
				  result,
				  QuoteExp.falseExp);
    
    return result;
  }
  
  private static gnu.expr.Expression matchTest(Domain d, gnu.expr.Expression parameter)
  {
    if(d == Domain.bot)
      return QuoteExp.trueExp;

    ListIterator types = d.getMonotype().getTC().getJavaInstanceTypes();
    
    gnu.expr.Expression res =
      instanceOfExp(parameter,(gnu.bytecode.Type) types.next());
    
    while(types.hasNext())
      res = orExp(instanceOfExp(parameter,(gnu.bytecode.Type) types.next()),res);
    
    return res;
  }

  private static final gnu.mapping.Procedure instanceProc = 
    new kawa.standard.instance();
  
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
  List /* of Domain */ patterns;

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
