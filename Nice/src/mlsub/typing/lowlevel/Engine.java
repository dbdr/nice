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
  public static void enter(boolean tentative)
  {
    if(dbg) Debug.println("Enter");
    floating.mark();
    soft.mark();
    formerFree.mark();

    // Once we are in existential mode, we don't mark/backtrack.
    if (!tentative && existentialLevel > 0)
      existentialLevel++;
    else
      {
        frozenLeqs.mark();

        for(Iterator i = constraints.iterator(); i.hasNext();)
          {
            Engine.Constraint k = (Engine.Constraint)i.next();
            k.mark();
          }
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
  public static void leave(boolean tentative, boolean commit)
    throws Unsatisfiable
  {
    // 'tentative' is only meaningful when in existential mode
    tentative &= existentialLevel > 0;
    // We only 'commit' in tentative mode
    commit &= tentative;
      
    boolean ok = false;
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
      ok = true;
    }
    finally{
      // Even if commit is true, if an error appeared during leaving
      // then we don't want to keep the changes.
      backtrack(tentative, ok && commit);
    }
  }
  
  public static void backtrack(boolean tentative, boolean commit)
  {
    floating.backtrack();

    if (existentialLevel <= 1 || tentative)
      {
        for(Iterator i = constraints.iterator();
            i.hasNext();)
          { 
            Engine.Constraint k = (Engine.Constraint) i.next();
            k.backtrack(commit);
          }

        if (! commit)
          frozenLeqs.backtrack();

        if (tentative && !commit)
          {
            // These type variables used to be free. Since we don't commit, 
            // we must set them free again!
            try{
              for (Iterator i = formerFree.iterator(); i.hasNext();)
                {
                  Element e = (Element) i.next();
                  e.setKind(null);
                  floating.add(e);
                }
            }
            finally{
              formerFree.endOfIteration();
            }
          }
      }

    soft.backtrack();
    formerFree.backtrack();

    if (!tentative && existentialLevel > 0)
      existentialLevel--;
  }

  /** Marker used to know how deep we are inside existential mode, so that
      we know when to exit from it.
  */
  public static int existentialLevel = 0;

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
  
  public static void setTop(Element top)
  {
    Engine.top = top;
  }

  private static Element top;

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
    if (e2 == top) return;

    Kind k1 = e1.getKind(), k2 = e2.getKind();

    // If e2 is Top, this is trivial.
    if (k2 == mlsub.typing.TopMonotype.TopKind.instance)
      return;

    if(k1!=null)
      if(k2!=null)
	{
	  if(k1.equals(k2))
	    k1.leq(e1,e2,initial);
	  else
	    {
              /* If a non-rigid type variable was previously compared to some
                 rigid element, it will have its type.
                 it is still possible for it to be greater than Top
                 (and therefore equal to Top). For this, we just need to
                 forget its previous kind.
              */
              if (k1 == mlsub.typing.TopMonotype.TopKind.instance &&
                  e2 instanceof mlsub.typing.MonotypeVar &&
                  ! isRigid(e2))
                {
                  ((mlsub.typing.MonotypeVar) e2).resetKind(k1);
                  return;
                }

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
	  if(dbg)
	    {
	      Debug.println("Freezing "+e1+" <: "+e2+
			    " ("+e1.getId()+" <: "+e2.getId()+")");
	      if (!(floating.contains(e1)))
		throw new InternalError("Unknown floating element 1 : " + e1);
	      if (!(floating.contains(e2)))
		throw new InternalError("Unknown floating element 2 : " + e2);
	    }

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

    if (e.isExistential())
      if (existentialLevel == 0)
        existentialLevel = 1;

    if(e.getKind()!=null)
      e.getKind().register(e);
    else
      {
	e.setId(FLOATING);
	floating.add(e);
      }
  }
  
  private static final int FLOATING = -3;
  private static final int RIGID = -4;

  public static boolean isRigid(Element e)
  {
    if (e.getId() == FLOATING)
      return false;

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
      return;
      //throw new InternalError("null kind for "+e);
    Engine.Constraint k = getConstraint(kind);
    if(k==null)
      throw new InternalError("null constraint for "+e);
    k.tag(e, variance);
  }

  /**
     Return the simplified constraint.
     Must be surrounded by startSimplify and stopSimplify.
  */
  public static void simplify(final ArrayList binders, final ArrayList atoms)
  {
    for(Iterator it = constraints.iterator();
	it.hasNext();)
      { 
	final Engine.Constraint k = (Engine.Constraint) it.next();
	k.simplify();
	
	final int soft = k.k0.weakMarkedSize();
	final int n = k.k0.size();
	for (int b = soft; b < n; b++)
	  binders.add(k.getElement(b));
	
	// add 'implements' constraints
	try{
	  k.k0.implementsIter(new K0.ImplementsIterator(){
	      protected void iter(int x, int iid)
	      {
		if (x < soft) return;
		
		mlsub.typing.TypeConstructor tc = 
		  (mlsub.typing.TypeConstructor) k.getElement(x);
		atoms.add(new mlsub.typing.ImplementsCst
		  (tc, ((mlsub.typing.Variance) tc.variance).getInterface(iid)));
	      }
	    });

	// add every constraint, except between two rigid varaibles
	for (int b = soft; b < n; b++)
	  for (int i = 0; i < n; i++)
	    addIfLeq(b, i, k, atoms);
	for (int i = 0; i < soft; i++)
	  for (int b = soft; b < n; b++)
	    addIfLeq(i, b, k, atoms);
	}
	catch(Unsatisfiable ex) {}
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
    if(kind==null) return e;
    //throw new InternalError("null kind for "+e);
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
    boolean toTop = k == mlsub.typing.TopMonotype.TopKind.instance;

    Stack s = new Stack();

    s.push(element);
    while(!s.empty())
      {
	Element e = (Element)s.pop();

	if(e.getKind()!=null)
	  if(e.getKind()==k)
	    continue;
	  else
	    throw new LowlevelUnsatisfiable
	      ("Bad Kinding for " + e + ":\nhad: " + e.getKind() +
	       "\nnew: " + k);
	
        if (e.isExistential())
          formerFree.add(e);

	// assert e.getKind()==null
	k.register(e);
	e.setKind(k);

	floating.remove(e);
        if (e.getId() == FLOATING)
          soft.add(e);

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
                // If e is Top, e1 <: e is trivial and can be discarded.
                if (toTop)
                  i.remove();
                else if (leq.e1.getKind() == null)
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
      for (Iterator i = soft.iterator(); i.hasNext();)
	{
	  Element e = (Element) i.next();
          e.setId(RIGID);
        }
    }
    finally{
      soft.endOfIteration();
    }
    soft.clear();

    boolean more;
    do {
      more = false;
      try {
        for(Iterator i = frozenLeqs.iterator(); i.hasNext();)
          {
            Leq leq = (Leq) i.next();
            Element e1 = leq.e1;
            Element e2 = leq.e2;

            // If at least one of the two is existential, then we must
            // keep both frozen.
            if ((e1.isExistential() && ! e2.isExistential())
                ||
                (e2.isExistential() && ! e1.isExistential()))
              {
                // Since we are marking an element as existential, we should
                // do one more pass to make sure all related elements are
                // marked
                more = true;

                if (e1.isExistential())
                  ((mlsub.typing.MonotypeVar) e2).setExistential();
                else if (e2.isExistential())
                  ((mlsub.typing.MonotypeVar) e1).setExistential();
              }
          }
      }
      finally{
        frozenLeqs.endOfIteration();
      }
    }
    while (more);

    try {
      for(Iterator i = floating.iterator();
	  i.hasNext();)
	{
	  Element e = (Element) i.next();
	  
	  // useful for nullness head on monotype vars
          // we don't set existential in stone either, because they
          // might be put into a kind later on.
	  if (e.getKind() != null || e.isExistential())
            continue;

	  if(dbg) Debug.println("Registering variable "+e);

	  e.setKind(variablesConstraint);
	  variablesConstraint.register(e);
          i.remove();
	}
    }
    finally{
      floating.endOfIteration();
    }
    
    try {
      for(Iterator i = frozenLeqs.iterator(); i.hasNext();)
	{
	  Leq leq = (Leq) i.next();
          Element e1 = leq.e1;
          Element e2 = leq.e2;

          // By the above code, if e2 is existential, e1 was marked as
          // existential too, so we don't need to test e2.
          if (e1.isExistential())
            continue;

	  variablesConstraint.leq(leq.e1, leq.e2, initialContext);
          i.remove();
	}
    }
    finally {
      frozenLeqs.endOfIteration();
    }
  }
  
  /** Maps a Kind to its lowlevel constraint */
  private static HashMap kindsMap; 
  
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
  
  /** The elements that have not yet been rigidified. */
  private static final BackableList soft = new BackableList();
  
  /** The elements that have been put into a kind since the last mark. */
  private static final BackableList formerFree = new BackableList();
  
  /** The constraint of monotype variables */
  public static Engine.Constraint variablesConstraint;
  
  /** The list of Constraints */
  private static ArrayList constraints;

  public static Iterator listConstraints()
  {
    return constraints.iterator();
  }

  /** Return to the initial virgin state. */
  public static void reset()
  {
    kindsMap = new HashMap();
    variablesConstraint = new Engine.Constraint("type variables",true);
    //kindsMap.put(variablesConstraint,variablesConstraint);

    constraints = new ArrayList(10);
    constraints.add(variablesConstraint);

    initialContext = true;
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
    public mlsub.typing.Monotype freshMonotype(boolean existential)
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
	return "" + ((mlsub.typing.Variance) associatedKind).getInterface(iid);
      }
    }
    
    public Kind associatedKind; 
    /* the kind of the elements of this constraint
     * This is used in TypeConstructor.setKind
     */
  
    public String toString()
    {
      return "Constraint " + name + " for "+associatedKind+":\n"+k0.toString();
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
	  if (e1.getId() < 0 || e1.getId() >= k0.size())
	    {
	      Debug.println(e1 + " has invalid index");
	      bossa.util.Internal.printStackTrace();
	    }
      
	  if (e2.getId() < 0 || e2.getId() >= k0.size())
	    {
	      Debug.println(e2 + " has invalid index");
	      bossa.util.Internal.printStackTrace();
	    }
	}

      if(initial)
	k0.initialLeq(e1.getId(),e2.getId());
      else
	k0.leq(e1.getId(),e2.getId()); 
    }

    public final void assertMinimal(Element e)
    {
      if(dbg)
	Debug.println("Minimal: "+ e);

      k0.minimal(e.getId());
    }
    
    public final boolean isMinimal(Element e)
    {
      return k0.isMinimal(e.getId());
    }
    
    public Element lowestInstance(Element e)
    {
      //FIXME: Suboptimal: doesn't always return an instance when there is one.
      // Maybe this should be done by enumeration of the solutions 
      // of the constraint. 
      
      final int id = e.getId();
      int res = -1;
      
      for (int elem = 0; elem < k0.initialContextSize(); elem++)
	// We use wasEntered, since id is assumed not to be rigid
	// i and res are rigid, so we use isLeq.
	// an alternative would be to (require) rigidify 
	// (at least a closure of leq relation + other axioms)
	// and use isLeq.
	// Could be more precise in presence of interfaces.
	if (k0.wasEntered(id, elem) && (res == -1 || k0.isLeq(elem, res)))
	  res = elem;

      if (res == -1)
	for (int elem = 0; elem < k0.initialContextSize(); elem++)
	  if (k0.wasEntered(elem, id) && (res == -1 || k0.isLeq(res, elem)))
	    res = elem;

      if (res != -1)
	// Check we really found an instance.
	{
	  final boolean[] ok = { true };
	  final int candidate = res;

	  try {
	  k0.ineqIter(new K0.IneqIterator() {
	      protected void iter(int x1, int x2) {
		if (x1 == id)
		  ok[0] &= k0.isLeq(candidate, x2);
		else if (x2 == id)
		  ok[0] &= k0.isLeq(x1, candidate);
	      }
	    });
	  } catch(Unsatisfiable ex) { throw new Error("assert false"); }

	  if (! ok[0])
	    res = -1;
	}

      if (res == -1)
	return null;
      else
	return getElement(res);
    }
    
    void mark()
    {
      k0.mark();
    }

    void backtrack(boolean ignore)
    {
      k0.backtrack(ignore);
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
