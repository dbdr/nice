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
//$Modified: Fri Sep 01 18:41:13 2000 by Daniel Bonniot $

package mlsub.typing.lowlevel;

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
    LowlevelUnsatisfiable.refinedReports = false;
  }
  
  /**
   * Enters a new typing context.
   * If an enter() completed successfully,
   * a matching leave() MUST be issued some time later by the caller.
   */
  public static void enter()
  {
    if(dbg) Debug.println("Enter");
    floating.mark();
    frozenLeqs.mark();
    for(Iterator i = constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k = (Engine.Constraint)i.next();
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

    for(Iterator i = constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k = (Engine.Constraint)i.next();
	k.satisfy();
	k.rigidify();
      }
  }
  
  /**
     Used in Polytype.simplify().
   */
  public static void satisfy()
    throws Unsatisfiable
  {
    assertFrozens();

    for(Iterator i = constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k = (Engine.Constraint)i.next();
	k.satisfy();
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
    try{
      assertFrozens();

      if(dbg) Debug.println("Leave");

      for(Iterator i = constraints.iterator();
	  i.hasNext();)
	{ 
	  Engine.Constraint k = (Engine.Constraint)i.next();
	  try{
	    if(dbg)
	      Debug.println("** Satisfying "+k);
	    
	    k.satisfy();
	  }
	  catch(Unsatisfiable e){
	    if(dbg) Debug.println("** Exception in "+k+e);
	    throw e;
	  }
	}
    }
    finally{
      backtrack();
    }
  }
  
  public static void backtrack()
  {
    for(Iterator i = constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k = (Engine.Constraint)i.next();
	k.backtrack();
      }
    floating.backtrack();
    for(Iterator i = floating.iterator();i.hasNext();)
      ((Element)i.next()).setKind(null);
    floating.endOfIteration();
    frozenLeqs.backtrack();
  }

  public static void startSimplify()
  {
    for(Iterator i = constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k = (Engine.Constraint)i.next();
	k.startSimplify();
      }
  }

  public static void stopSimplify()
  {
    for(Iterator i = constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k = (Engine.Constraint)i.next();
	k.stopSimplify();
      }
  }

  /**
   * Iterates leq on two collections of Element.
   *
   * @param e1 the smaller elements
   * @param e2 the greater elements
   * @exception Unsatisfiable 
   */
  public static void leq(Element[] e1, Element[] e2) 
    throws Unsatisfiable
  {
    if (e1.length != e2.length)
      throw new IllegalArgumentException
	("Bad size in Engine.leq(Element[])");
    
    for(int i = e1.length-1;i>=0;i--)
      leq(e1[i], e2[i]);
  }
      
  public static final void leq(Element e1, Element e2) 
    throws Unsatisfiable
  {
    leq(e1,e2,false);
  }
  
  /**
     Asserts that elements have some ordering relation.
     
     @param e1 a value of type 'Element'
     @param e2 a value of type 'Element'
     @exception Unsatisfiable if the constraint is not satisfiable.
     However this fact may also be discovered later.
   */
  public static final void leq(Element e1, Element e2, boolean initial) 
    throws Unsatisfiable
  {
    Kind k1 = e1.getKind(), k2 = e2.getKind();
    
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
			      "\nfor elements "+e1+" and "+e2);
	      throw new LowlevelUnsatisfiable("Bad Kinding for "+
					      e1+ " and "+e2);
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
      else // ie k1==null and k2==null
	{
	  if(!(floating.contains(e1)))
	    throw new InternalError("Unknown floating element 1 : "+e1);
	  if(!(floating.contains(e2)))
	    throw new InternalError("Unknown floating element 2 : "+e2);

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
    Kind kind = e.getKind();
    if(kind==null)
      throw new InternalError("null kind in Engine.isRigid for "+e);
    Engine.Constraint k = getConstraint(kind);
    if(k==null)
      throw new InternalError("null constraint in Engine.isRigid for "+e);
    return k.isRigid(e);
  }
  
  /****************************************************************
   * Simplification
   ****************************************************************/

  public static void tag(Element e, int variance)
  {
    Kind kind = e.getKind();
    if(kind==null)
      throw new InternalError("null kind for "+e);
    Engine.Constraint k = getConstraint(kind);
    if(k==null)
      throw new InternalError("null constraint for "+e);
    k.tag(e, variance);
  }

  /**
     Return the simplified constraint.
     Must be surrounded by startSimplify and stopSimplify.
  */
  public static void simplify(ArrayList binders, ArrayList atoms)
  {
    for(Iterator it = constraints.iterator();
	it.hasNext();)
      { 
	Engine.Constraint k = (Engine.Constraint) it.next();
	k.simplify();
	
	int n = k.k0.size();
	for (int b = k.k0.weakMarkedSize(); b < n; b++)
	  binders.add(k.getElement(b));
	
	for (int b = k.k0.weakMarkedSize(); b < n; b++)
	  for (int i = 0; i < n; i++)
	    addIfLeq(b, i, k, atoms);
	for (int i = 0; i < k.k0.weakMarkedSize(); i++)
	  for (int b = k.k0.weakMarkedSize(); b < n; b++)
	    addIfLeq(i, b, k, atoms);
      }
  }
  
  private static void addIfLeq(int i1, int i2, Engine.Constraint k, List atoms)
  {
    if (k.k0.wasEntered(i1, i2))
      atoms.add(k == variablesConstraint ?
		(mlsub.typing.AtomicConstraint)
		new mlsub.typing.MonotypeLeqCst
		  ((mlsub.typing.MonotypeVar) k.getElement(i1), 
		   (mlsub.typing.MonotypeVar) k.getElement(i2)) :
		new mlsub.typing.TypeConstructorLeqCst
		  ((mlsub.typing.TypeConstructor) k.getElement(i1), 
		   (mlsub.typing.TypeConstructor) k.getElement(i2)));
  }
  
  /**
     Return the element e is equivalent to after simplification.
  */
  public static Element canonify(Element e)
  {
    Kind kind = e.getKind();
    if(kind==null)
      throw new InternalError("null kind for "+e);
    Engine.Constraint k = getConstraint(kind);
    if(k==null)
      throw new InternalError("null constraint for "+e);

    return k.getElement(e.getId());
  }
  
  /****************************************************************
   *                          Private
   ****************************************************************/

  private static class Leq
  {
    Leq(Element e1,Element e2)
    {
      this.e1 = e1;
      this.e2 = e2;
    }
    public String toString()
    {
      return e1+" <: "+e2;
    }
    Element e1,e2;
  }

  private static final BackableList frozenLeqs = new BackableList();
  
  public static void setKind(Element element, Kind k)
    throws Unsatisfiable
  {
    Stack s = new Stack();

    s.push(element);
    while(!s.empty())
      {
	Element e = (Element)s.pop();
	
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
	try{
	  for(Iterator i = frozenLeqs.iterator();
	      i.hasNext();)
	    {
	      Leq leq = (Leq)i.next();
	      if(leq.e1==e)
		if(leq.e2.getKind()==null)
		  {
		    s.push(leq.e2);
		  }
		else
		  {
		    if(leq.e1.getKind()!=leq.e2.getKind())
		      throw new InternalError("Bad kinding in Engine.setKind 1");
		    i.remove();
		    k.leq(leq.e1,leq.e2,initialContext);
		  }
	      else if(leq.e2==e)
		if(leq.e1.getKind()==null)
		  s.push(leq.e1);
		else
		  {
		    if(leq.e1.getKind()!=leq.e2.getKind())
		      throw new InternalError("Bad kinding in Engine.setKind 2");
		    i.remove();
		    k.leq(leq.e1,leq.e2,initialContext);
		  }
	    }
	}
	finally{
	  frozenLeqs.endOfIteration();
	}
      }
  }
  
  /** 
   * Doesn't check kinding. 
   * Used in Typing.enumerate 
   */
  public static void forceKind(Element element, Kind k)
    throws Unsatisfiable
  {
    Stack s = new Stack();

    s.push(element);
    while(!s.empty())
      {
	Element e = (Element) s.pop();
	
	e.setKind(k);
	k.register(e);
	
	floating.remove(e);

	// Propagates the kind to all comparable elements
	try{
	  for(Iterator i = frozenLeqs.iterator();
	      i.hasNext();)
	    {
	      Leq leq = (Leq)i.next();
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
	}
	finally{
	  frozenLeqs.endOfIteration();
	}
      }
  }
  
  /**
     Enter all the 'floating' elements into the variablesConstraint
     and add their frozen constraints.
   */
  private static void assertFrozens()
    throws Unsatisfiable
  {
    try{
      for(Iterator i = floating.iterator();
	  i.hasNext();)
	{
	  Element e = (Element) i.next();
	  
	  if(dbg) Debug.println("Registering variable "+e);
	  
	  e.setKind(variablesConstraint);
	  variablesConstraint.register(e);
	}
    }
    finally{
      floating.endOfIteration();
    }
    floating.clear();
    
    for(Iterator i = frozenLeqs.iterator(); i.hasNext();)
      {
	Leq leq = (Leq) i.next();
	variablesConstraint.leq(leq.e1, leq.e2, initialContext);
      }
    frozenLeqs.endOfIteration();
    frozenLeqs.clear();
  }
  
  /** Maps a Kind to its lowlevel constraint */
  private static final HashMap kindsMap = new HashMap();
  
  public static Engine.Constraint getConstraint(Kind kind)
  {
    if(kind instanceof Engine.Constraint)
      return (Engine.Constraint) kind;
    
    Engine.Constraint res = (Engine.Constraint) kindsMap.get(kind);
    if(res!=null)
      return res;

    if(dbg)
      Debug.println("Creating new Lowlevel constraint for "+kind);
    
    res = new Engine.Constraint("kind "+kind.toString());
    res.associatedKind = kind;
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
      throw new InternalError
	("This shouldn't happen, Engine.Constraint is empty here !");
    }
    constraints.add(res);
    kindsMap.put(kind,res);
    return res;
  }
  
  /** The elements that have not yet been added to a Kind  */
  private static final BackableList floating = new BackableList();
  
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
    constraints = new ArrayList(10);
    constraints.add(variablesConstraint);
  }
  public static Iterator listConstraints()
  {
    return constraints.iterator();
  }
  
  public static void createInitialContext()
    throws Unsatisfiable
  {
    for(Iterator i = constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k = (Engine.Constraint)i.next();
	k.createInitialContext();
      }
    initialContext = false;
  }
  
  public static void releaseInitialContext()
  {
    for(Iterator i = constraints.iterator();
	i.hasNext();)
      { 
	Engine.Constraint k = (Engine.Constraint)i.next();
	k.releaseInitialContext();
      }
    initialContext = true;
  }
  
  private static boolean initialContext = true;
  public static boolean isInRigidContext() { return !initialContext; }

  /****************************************************************
   * Engine.Constraint
   ****************************************************************/

  final public static class Constraint
    implements Kind
  {
    Constraint(String name)
    {
      this.name = name;
    }
    private String name;

    Constraint(String name, boolean variables)
    {
      this(name);
      this.variables = variables;
    }

    private boolean variables = false;
    
    // this is not too logical to have this...
    public mlsub.typing.Monotype freshMonotype()
    {
      return null;
    }
    
    /**
     * Returns true iff there is a concrete #class in this constraint.
     */
    public boolean hasConstants()
    {
      return !variables;
    }
    
    final K0 k0 = new K0(K0.BACKTRACK_UNLIMITED, new Callbacks());
    
    private Vector elements = new Vector(10); // ArrayList would be better, but has no setSize method
    private BitVector concreteElements = new BitVector();
    
    public final void register(Element e)
    {
      int id = k0.extend();
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
      return (Element) elements.get(index);
    }
    
    void tag(Element e, int variance)
    {
      k0.tag(e.getId(), variance);
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
	elements.set(index,null); // enable garbage collection
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
	  return String.valueOf(getElement(x))+ "[" + x + "]";
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

    public final boolean isValid(Element e)
    {
      int id = e.getId();
      return id>=0 && id<k0.size();
    }
    
    public final void leq(Element e1, Element e2)
      throws Unsatisfiable
    {
      leq(e1,e2,false);
    }
    
    public final void leq(Element e1, Element e2, boolean initial)
      throws Unsatisfiable
    {
      if(dbg)
	{	  
	  Debug.println(e1+" <: "+e2+" ("+e1.getId()+" <: "+e2.getId()+")");
	  if(e1.getId()<0 || e1.getId()>=k0.size())
	    Debug.println(e1 + " has invalid index");
      
	  if(e2.getId()<0 || e2.getId()>=k0.size())
	    Debug.println(e2 + " has invalid index");
	}

      if(initial)
	k0.initialLeq(e1.getId(),e2.getId());
      else
	k0.leq(e1.getId(),e2.getId()); 
    }

    public final void assertMinimal(Element e)
    {
      k0.minimal(e.getId());
    }
    
    public Element lowestRigidSuperElement(Element e, boolean allowWidening)
    {
      int id = e.getId();
      int res = -1;
      
      for (int i = 0; i < k0.initialContextSize(); i++)
	// we use wasEntered, since id is assumed not to be rigid
	// i and res are rigid, so we use isLeq.
	// an alternative would be to (require) rigidify 
	// (at least a closure of leq relation + other axioms)
	// and use isLeq.
	// could be more precise in presence of interfaces.
	if (k0.wasEntered(id, i) && (res == -1 || k0.isLeq(i, res)))
	  res = i;

      if (res == -1 && allowWidening)
	for (int i = 0; i < k0.initialContextSize(); i++)
	  if (k0.wasEntered(i, id) && (res == -1 || k0.isLeq(res, i)))
	    res = i;
      
      if (res == -1)
	return null;
      else
	return getElement(res);
    }
    
    void mark()
    {
      k0.mark();
    }

    void backtrack()
    {
      k0.backtrack();
    }

    void startSimplify()
    {
      k0.startSimplify();
    }

    void simplify()
    {
      k0.simplify();
    }
    
    void stopSimplify()
    {
      k0.stopSimplify();
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
  
  public static boolean dbg;
}
