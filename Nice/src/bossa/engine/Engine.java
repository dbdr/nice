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

// File    : Engine.java
// Created : Tue Jul 27 15:34:53 1999 by bonniot
//$Modified: Thu Feb 24 12:41:53 2000 by Daniel Bonniot $

package bossa.engine;

import bossa.util.*;

import java.util.*;

/**
 * Public interface to the lowlevel constraint implication checker.
 * 
 * All accesses are done through this Engine.
 *
 * @author bonniot
 */
public abstract class Engine
{
  static
  {
    LowlevelUnsatisfiable.refinedReports=true;
  }
  
  /**
   * Enters a new typing context
   */
  public static void enter()
  {
    if(dbg) Debug.println("Enter");
    floating.mark();
    frozenLeqs.mark();
    for(Iterator i=constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k=(Engine.Constraint)i.next();
	k.mark();
	//k.rigidify();
      }
  }
  
  /**
   * Rigidify the current constraint.
   * This means that no further assumptions will be made on it.
   *
   * @exception Unsatisfiable if the current context was not well kinded.
   */
  public static void implies()
    throws Unsatisfiable
  {
    assertFrozens();

    if(dbg) Debug.println("Implies");

    for(Iterator i=constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k=(Engine.Constraint)i.next();
	k.satisfy();
	k.rigidify();
      }
  }
  
  /**
   * Returns to the state we had before the last 'enter'.
   *
   * @exception Unsatisfiable if the constraint was not satisfiable.
   */
  public static void leave()
    throws Unsatisfiable
  {
    assertFrozens();

    if(dbg) Debug.println("Leave");
      
    for(Iterator i=constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k=(Engine.Constraint)i.next();
	try{
	  if(dbg) Debug.println("** Satisfying "+k);
	  
	  k.satisfy();
	}
	catch(Unsatisfiable e){
	  if(dbg) Debug.println("** In "+k);
	  throw e;
	}
      }
    backtrack();
  }
  
  public static void backtrack()
  {
    for(Iterator i=constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k=(Engine.Constraint)i.next();
	k.backtrack();
      }
    floating.backtrack();
    for(Iterator i=floating.iterator();i.hasNext();)
      ((Element)i.next()).setKind(null);
    floating.endOfIteration();
    frozenLeqs.backtrack();
  }

  /**
   * Iterates leq on two collections of Element.
   *
   * @param e1 the smaller elements
   * @param e2 the greater elements
   * @exception Unsatisfiable 
   */
  public static void leq(Collection e1, Collection e2) 
    throws Unsatisfiable
  {
    Internal.error(e1.size()!=e2.size(),"Bad size in Engine.leq(Collections)");
    
    Iterator i1=e1.iterator(),i2=e2.iterator();
    for(int i=e1.size();i>0;i--)
      leq((Element)i1.next(),(Element)i2.next());
  }
      
  public static void leq(Element e1, Element e2) 
    throws Unsatisfiable
  {
    leq(e1,e2,false);
  }
  
  /**
   * Asserts that elements have some ordering relation.
   *
   * @param e1 a value of type 'Element'
   * @param e2 a value of type 'Element'
   * @exception Unsatisfiable if the constraint is not satisfiable.
   * However this fact may also be discovered later
   */
  public static void leq(Element e1, Element e2, boolean initial) 
    throws Unsatisfiable
  {
    Kind k1=e1.getKind(),k2=e2.getKind();
    
    if(k1!=null)
      if(k2!=null)
	{
	  if(k1.equals(k2))
	    k1.leq(e1,e2,initial);
	  else
	    {
	      if(dbg) 
		Debug.println("Bad kinding discovered by Engine : "+
			      k1+" != "+k2+
			      " for elements "+e1+" and "+e2);
	      throw new LowlevelUnsatisfiable("Bad Kinding for "+e1+ " and "+e2);
	    }
	}
      else
	{
	  setKind(e2,k1);
	  k1.leq(e1,e2,initial);
	}
    else
      if(k2!=null)
	{
	  setKind(e1,k2);
	  k2.leq(e1,e2,initial);
	}
      else // k1==null and k2==null
	{
	  if(!(floating.contains(e1)))
	    {
	      floating.add(e1);
	      Internal.warning("Engine: floating added 1 : "+e1);
	    }
	  if(!(floating.contains(e2)))
	    {
	      floating.add(e2);
	      Internal.error("Engine: floating added 2 : "+e2);
	    }
	  if(dbg)
	    Debug.println("Freezing "+e1+" <: "+e2+
			  " ("+e1.getId()+" <: "+e2.getId()+")");
	  frozenLeqs.add(new Leq(e1,e2));
	}
  }
  
  /****************************************************************
   * New nodes
   ****************************************************************/
  
  /**
   * Prepare a new Element to be used.
   */
  public static void register(Element e)
  {
    if(dbg) Debug.println("Registering "+e);
    
    if(e.getKind()!=null)
      e.getKind().register(e);
    else
      {
	e.setId(-2); // for debugging purposes
	floating.add(e);
      }
  }
  
  public static boolean isRigid(Element e)
  {
    Kind kind=e.getKind();
    Internal.error(kind==null,"null kind in Engine.isRigid for "+e);
    Engine.Constraint k=getConstraint(kind);
    Internal.error(k==null,"null constraint in Engine.isRigid for "+e);
    return k.isRigid(e);
  }
  
  /****************************************************************
   *                          Private
   ****************************************************************/

  private static class Leq
  {
    Leq(Element e1,Element e2)
    {
      this.e1=e1;
      this.e2=e2;
    }
    public String toString()
    {
      return e1+" <: "+e2;
    }
    Element e1,e2;
  }

  private static final BackableList frozenLeqs=new BackableList();
  
  public static void setKind(Element element, Kind k)
    throws Unsatisfiable
  {
    Stack s=new Stack();

    s.push(element);
    while(!s.empty())
      {
	Element e=(Element)s.pop();
	
	if(e.getKind()!=null)
	  if(e.getKind()==k)
	    continue;
	  else
	    throw new LowlevelUnsatisfiable("Bad Kinding for "+e);
	
	// e.getKind()==null
	k.register(e);
	e.setKind(k);
	
	floating.remove(e);

	// Propagates the kind to all comparable elements
	for(Iterator i=frozenLeqs.iterator();
	    i.hasNext();)
	  {
	    Leq leq=(Leq)i.next();
	    if(leq.e1==e)
	      if(leq.e2.getKind()==null)
		{
		  s.push(leq.e2);
		}
	      else
		{
		  Internal.error(leq.e1.getKind()!=leq.e2.getKind(),
				 "Bad kinding in Engine.setKind 1");
		  i.remove();
		  k.leq(leq.e1,leq.e2,initialContext);
		}
	    else if(leq.e2==e)
	      if(leq.e1.getKind()==null)
		s.push(leq.e1);
	      else
		{
		  Internal.error(leq.e1.getKind()!=leq.e2.getKind(),
				 "Bad kinding in Engine.setKind 2");
		  i.remove();
		  k.leq(leq.e1,leq.e2,initialContext);
		}
	  }
	frozenLeqs.endOfIteration();
      }
  }
  
  /** 
   * Doesn't check kinding. 
   * Used in Typing.enumerate 
   */
  public static void forceKind(Element element, Kind k)
    throws Unsatisfiable
  {
    Stack s=new Stack();

    s.push(element);
    while(!s.empty())
      {
	Element e=(Element)s.pop();
	
	e.setKind(k);
	k.register(e);
	
	floating.remove(e);

	// Propagates the kind to all comparable elements
	for(Iterator i=frozenLeqs.iterator();
	    i.hasNext();)
	  {
	    Leq leq=(Leq)i.next();
	    if(leq.e1==e)
	      if(leq.e2.getKind()==null)
		s.push(leq.e2);
	      else
		{
		  i.remove();
		  k.leq(leq.e1,leq.e2);
		}
	    else if(leq.e2==e)
	      if(leq.e1.getKind()==null)
		s.push(leq.e1);
	      else
		{
		  i.remove();
		  k.leq(leq.e1,leq.e2);
		}
	  }
	frozenLeqs.endOfIteration();
      }
  }
  
  /**
   * Enter all the 'floating' elements into the variablesConstraint
   * and add their frozen constraints
   *
   */
  private static void assertFrozens()
    throws Unsatisfiable
  {
    for(Iterator i=floating.iterator();
	i.hasNext();)
      {
	Element e=(Element) i.next();
	
	if(dbg) Debug.println("Registering variable "+e);
	
	e.setKind(variablesConstraint);
	variablesConstraint.register(e);
      }
    floating.endOfIteration();
    floating.clear();
    
    for(Iterator i=frozenLeqs.iterator();
	i.hasNext();)
      {
	Leq leq=(Leq)i.next();
	variablesConstraint.leq(leq.e1,leq.e2,initialContext);
      }
    frozenLeqs.endOfIteration();
    frozenLeqs.clear();
  }
  
  /** Maps a Kind to its lowlevel constraint */
  private static final HashMap kindsMap=new HashMap();
  
  public static Engine.Constraint getConstraint(Kind kind)
  {
    if(kind instanceof Engine.Constraint)
      return (Engine.Constraint) kind;
    
    Engine.Constraint res=(Engine.Constraint) kindsMap.get(kind);
    if(res!=null)
      return res;

    if(dbg)
      Debug.println("Creating new Lowlevel constraint for "+kind);
    
    res=new Engine.Constraint("kind "+kind.toString());
    res.associatedKind=kind;
    // if the constraint is created after the initial context has been defined,
    // we have to change the state of the constraint here
    if(!initialContext)
      try{
	if(dbg)
	  Debug.println("createInitialContext() and mark() called for new constraint");
	res.createInitialContext();
	res.mark();
      }
    catch(Unsatisfiable e){
      Internal.error("This shouldn't happen, Engine.Constraint is empty here !");
    }
    constraints.add(res);
    kindsMap.put(kind,res);
    return res;
  }
  
  /** The elements that have not yet been added to a Kind  */
  private static final BackableList floating=new BackableList();
  
  /** The constraint of monotype variables */
  public static final Engine.Constraint variablesConstraint;
  static
  {
    variablesConstraint = new Engine.Constraint("type variables",true);
    //kindsMap.put(variablesConstraint,variablesConstraint);
  }  
  
  /** The list of Constraints */
  private static final ArrayList constraints;
  static
  {
    constraints=new ArrayList(10);
    constraints.add(variablesConstraint);
  }
  public static Iterator listConstraints()
  {
    return constraints.iterator();
  }
  
  public static void createInitialContext()
    throws Unsatisfiable
  {
    for(Iterator i=constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k=(Engine.Constraint)i.next();
	k.createInitialContext();
      }
    initialContext=false;
  }
  
  public static void releaseInitialContext()
  {
    for(Iterator i=constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k=(Engine.Constraint)i.next();
	k.releaseInitialContext();
      }
    initialContext=true;
  }
  
  private static boolean initialContext=true;
  public static boolean isInRigidContext() { return !initialContext; }

  /****************************************************************
   * Engine.Constraint
   ****************************************************************/

  final public static class Constraint
    implements Kind
  {
    static
    {
      K0.debugK0=Debug.K0;
    }
    
    Constraint(String name)
    {
      this.name=name;  
    }
    private String name;

    Constraint(String name, boolean variables)
    {
      this(name);
      this.variables=variables;
    }

    private boolean variables = false;
    
    /**
     * Returns true iff there is a concrete #class in this constraint.
     */
    public boolean hasConstants()
    {
      return !variables;
    }
    
    final K0 k0 = new K0(K0.BACKTRACK_UNLIMITED,new Callbacks());
    
    private Vector elements=new Vector(10); // ArrayList would be better, but has no setSize method
    private BitVector concreteElements = new BitVector();
    
    public final void register(Element e)
    {
      int id=k0.extend();
      e.setId(id);
      if(id>=elements.size())
	elements.setSize(id+1);
      elements.set(id,e);
      
      if(e.isConcrete())
	concreteElements.set(id);
      
      if(dbg) Debug.println(e+" has id "+e.getId());
    }    
  
    // public for Typing.enumerate...
    public Element getElement(int index)
    {
      return (Element)elements.get(index);
    }
    
    class Callbacks extends K0.Callbacks
    {
      protected void indexMerged(int src, int dest) {
	if(dbg)
	  Debug.println("Merged "+indexToString(src)+" into "+
			indexToString(dest));
	getElement(src).setId(dest);
      }
      protected void indexMoved(int src, int dest) {
	if(dbg)
	  Debug.println("Changed index of "+indexToString(src));
	// indexToString(dest) is meaningless

	Element movedElement = getElement(src);
	movedElement.setId(dest);
	elements.set(dest,movedElement);
      }
      protected void indexDiscarded(int index) {
	if(dbg)
	  Debug.println("Discarded "+indexToString(index));
	getElement(index).setId(-2);
	elements.set(index,null); // enable garbage collection, maybe
      }
      /*
       * Pretty printing
       */
      protected String getName() {
	return name;
      }
      protected String indexToString(int x) {
	if(x==BitVector.UNDEFINED_INDEX)
	  return "[NONE]";
	else if(x==-1)
	  return "[BOTTOM]";
	else
	  return getElement(x).toString();
      }
      protected String interfaceToString(int iid) {
	return Integer.toString(iid);
      }
    }
    
    public Kind associatedKind; 
    /* the kind of the elements of this constraint
     * This is used in TypeConstructor.setKind
     */
  
    public String toString()
    {
      return "Constraint for "+associatedKind+":\n"+k0.toString();
    }
    
    public final void leq(Element e1, Element e2)
      throws Unsatisfiable
    {
      leq(e1,e2,false);
    }
    
    public final void leq(Element e1, Element e2, boolean initial)
      throws Unsatisfiable
    {
      if(dbg) Debug.println(e1+" <: "+e2+" ("+e1.getId()+" <: "+e2.getId()+")");
      try{ 
	if(initial)
	  k0.initialLeq(e1.getId(),e2.getId());
	else
	  k0.leq(e1.getId(),e2.getId()); 
      }
      catch(Unsatisfiable e){
	// We call backtrack here to go the good state
	// since the leave() method will not be called
	// (or the call to backtrack() in leave will not occur
	// if we are in leave now)
	Engine.backtrack();
	throw e;
      }
    }

    void mark()
    {
      k0.mark();
    }

    void backtrack()
    {
      k0.backtrack();
    }
    
    void satisfy() throws Unsatisfiable
    {
      k0.satisfy();
    }

    void rigidify()
    {
      k0.rigidify();
    }

    boolean isRigid(Element e)
    {
      return k0.isRigid(e.getId());
    }
    
    void createInitialContext() throws Unsatisfiable
    {
      k0.createInitialContext();
    }

    void releaseInitialContext()
    {
      k0.releaseInitialContext();
    }

    public int newInterface()
    {
      return k0.newInterface();
    }
    
    public void subInterface(int i1, int i2)
    {
      k0.subInterface(i1,i2);
    }
    
    public void initialImplements(int x, int iid)
    {
      k0.initialImplements(x,iid);
    }
  
    public void initialAbstracts(int x, int iid)
    {
      k0.initialAbstracts(x,iid);
    }
  
    public void indexImplements(int x, int iid) throws Unsatisfiable
    {
      k0.indexImplements(x,iid);
    }

    public void enumerate(BitVector observers,
			  LowlevelSolutionHandler handler)
    {
      k0.enumerate(observers,handler);
    }

    public void reduceDomainToConcrete(Element e) throws Unsatisfiable
    {
      k0.reduceDomain(e.getId(),false,concreteElements);
    }

    /**
     * Assume e1 and e2 are rigid
     **/
    public boolean isLeq(Element e1, Element e2) {
      return k0.isLeq(e1.getId(),e2.getId());
    }
  }
  
  static boolean dbg = Debug.engine;
}
