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
//$Modified: Fri Oct 06 12:55:08 2000 by Daniel Bonniot $

package bossa.link;

import bossa.util.*;

import mlsub.typing.*;

import bossa.syntax.NiceMethod;
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
  public Alternative(NiceMethod m, Pattern[] patterns, ReferenceExp method)
  {
    this.code = method;
    
    this.methodName = m.getFullName();
    this.patterns = patterns;
    add();
  }

  /**
   * When read from a bytecode file.
   */
  public static void read(ClassType c, Method method)
    {
      String methodName = nice.tools.code.Strings.unescape(method.getName());

      MiscAttr attr = (MiscAttr) Attribute.get(method, "definition");
      if (attr == null)
	// this must be a toplevel function, a constructor, ...
	return;

      new Alternative(c, method, attr);
    }

  private Alternative(ClassType c, Method method, MiscAttr attr)
  {
    methodName = new String(attr.data);

    attr = (MiscAttr) Attribute.get(method, "patterns");
    if (attr == null)
      Internal.error("Method " + method.getName() + 
		     " in class " + c.getName() + " has no patterns");
    String rep = new String(attr.data);

    this.code = new QuoteExp(new PrimProcedure(method));
    
    int[]/*ref*/ at = new int[]{ 0 };

    ArrayList patterns = new ArrayList(5);

    Pattern p;
    while ((p = Pattern.read(rep, at, methodName)) != null)
      {
	if (p.tc == bossa.syntax.ConstantExp.arrayTC)
	  /* Special treatment for arrays:
	     they are compiled into Object,
	     but we want a SpecialArray in the method bytecode type.
	  */
	  {
	    int argnum = patterns.size();
	    if (method.arg_types[argnum] == Type.pointer_type)
	      method.arg_types[argnum] = SpecialArray.unknownTypeArray();
	  }

	patterns.add(p);
      }
    
    this.patterns = (Pattern[]) 
      patterns.toArray(new Pattern[patterns.size()]);
    
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
      if (!a.patterns[i].leq(b.patterns[i]))
	return false;
    return true;
  }

  /**
   * Tests the matching of tags against a method alternative.
   */
  boolean matches(TypeConstructor[] tags)
  {
    for(int i = 0; i<patterns.length; i++)
      if (!patterns[i].matches(tags[i]))
	return false;

    return true;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  /**
   * @return the expression that represents the method body.
   */
  Expression methodExp()
  {
    return code;
  }

  private Expression code;  
  
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
    
    for(int n = parameters.length; --n >= 0; )
      result = gnu.expr.IfExp.make(patterns[n].matchTest(parameters[n]),
				   result,
				   QuoteExp.falseExp);
    
    return result;
  }
  
  public String toString()
  {
    return methodName + Util.map("(", ", ", ")", patterns);
  }

  String methodName;
  Pattern[] patterns;

  boolean visiting;
  
  /****************************************************************
   * Regrouping alternatives per method
   ****************************************************************/

  private static HashMap alternatives;
  public static void reset()
  {
    alternatives = new HashMap();
  }
  
  private void add()
  {
    List l = (List) alternatives.get(methodName);
    if (l == null)
      {
	// XXX change to LinkedList
	l = new ArrayList();
	alternatives.put(methodName,l);
      }
    // Dispatch.sort(final List alternatives) assumes that new alternatives
    // are added at the end
    l.add(this);
  }
  
  static Stack sortedAlternatives(NiceMethod m)
  {
    List list = (List) alternatives.get(m.getFullName());
    
    if (list == null)
      // It's not an error for a method to have no alternative
      // as long as its domain is empty.
      // this will be checked later
      return new Stack();
    
    return sort(list);
  }

  /****************************************************************
   * Sorting alternatives by generality
   ****************************************************************/

  /**
     Computes a topological sorting of the list of alternatives.
     
     Uses a postfix travsersal of the graph.
  */
  private static Stack sort(final List alternatives)
  {
    Stack sortedAlternatives = new Stack();
    
    if (alternatives.size() == 0)
      return sortedAlternatives;

    // Test if another sort has been done before.
    // In that case reset the marks.
    if (((Alternative) alternatives.get(0)).mark != Alternative.UNVISITED)
      for(Iterator i = alternatives.iterator(); i.hasNext();)
	((Alternative) i.next()).mark = Alternative.UNVISITED;
	
    for(Iterator i = alternatives.iterator(); i.hasNext();)
      {
	Alternative a = (Alternative) i.next();
	if (a.mark == Alternative.UNVISITED)
	  visit(a, alternatives, sortedAlternatives);
      }

    return sortedAlternatives;
  }
  
  private final static void visit
    (final Alternative a, 
     final List alternatives,
     final Stack sortedAlternatives
     )
  {
    // Cycles are possible
    //   * if two alternatives are identical (same patterns)
    //   * if there is a cyclic subclass relation
    //   * that's all, folks ! :-)
    switch(a.mark)
      {
      case Alternative.VISITING : User.error("Cycle in alternatives: "+a);
      case Alternative.UNVISITED: a.mark = Alternative.VISITING; break;
      case Alternative.VISITED  : return; //should not happen
      }
    
    for(Iterator i = alternatives.iterator();
	i.hasNext();)
      {
	Alternative daughter = (Alternative) i.next();
	if(daughter != a 
	   && daughter.mark == Alternative.UNVISITED 
	   && Alternative.leq(daughter,a))
	  visit(daughter,alternatives,sortedAlternatives);
      }
    a.mark = Alternative.VISITED;
    sortedAlternatives.push(a);
  }
}
