/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package mlsub.typing;

import java.util.*;

import mlsub.typing.lowlevel.Engine;
import mlsub.typing.lowlevel.Element;
import mlsub.typing.lowlevel.Unsatisfiable;

/**
   Static class for comparing types

   @version $Date$
   @author Daniel Bonniot
 */
public final class Typing
{
  /****************************************************************
   * Typing contexts
   ****************************************************************/

  /**
     Enters a new typing context.
     
     If an enter() completed successfully,
     a matching leave() MUST be issued some time later by the caller.
   */
  public static int enter()
  {
    if(dbg) 
      Debug.println("ENTER "+level);

    Engine.enter();
    return level++;
  }

  // used to verify that enter and leaves match
  static int level = 0;
  
  /**
     Enters a new typing context
     
     @param message A debug message to know where we are
   */
  public static int enter(String message)
  {
    if(message != null && dbg) Debug.println("## Typechecking "+message);
    return enter();
  }

  static public void introduce(Element e)
  {    
    if(dbg) 
      Debug.println("Typing introduced " + e);

    // a monotype var introduced earlier must be given a new null kind
    // the cleaner way would be to reset it to null when we leave this level
    // but it would be a pain, and the result is the same
    if (e instanceof MonotypeVar)
      ((MonotypeVar) e).reset();
    
    Engine.register(e);
  }
  
  static public void introduce(Element[] elements)
  {
    if(elements == null)
      return;
    
    for(int i = 0; i<elements.length; i++)
      if (elements[i] != null)
	introduce(elements[i]);
  }
  
  // TODO: fix this?
  // pb with Interface being a type symbol
  static public void introduceTypeSymbols(TypeSymbol[] elements)
  {
    for(int i = 0; i<elements.length; i++)
      introduce((Element) elements[i]);
    
  }
  
  /**
     Leaves the last typing context.
   */
  public static int leave()
    throws TypingEx
  {
    if(dbg) 
      Debug.println("LEAVE "+(level-1));
    
    try{
      level--;
      Engine.leave();
    }
    catch(Unsatisfiable e){
      if(dbg) e.printStackTrace();
      throw new TypingEx("Unsatisfiable 1:"+e.getMessage());
    }
    return level;
  }

  public static void implies()
    throws TypingEx
  {
    if(dbg) Debug.println("IMPLIES");
    try{
      Engine.implies();
    }
    catch(Unsatisfiable e){
      throw new TypingEx("Not satisfiable "+e.getMessage());
    }    
  }

  /** Called before each independant compilation.
      This must clear any side-effect of previous compilations. 
  */
  public static void startNewCompilation()
  {
    Engine.reset();
    FunTypeKind.reset();
  }

  public static void createInitialContext()
  {
    try{
      Engine.createInitialContext();
    }
    catch(Unsatisfiable e){
      throw new InternalError("Initial context is not satisfiable: " + e);
    }
  }
  
  public static void releaseInitialContext()
  {
    Engine.releaseInitialContext();
  }
  
  public static boolean isInRigidContext() { return Engine.isInRigidContext(); }

  /****************************************************************
   * Assertions
   ****************************************************************/

  public static void leq(Collection c1, Collection c2)
    throws TypingEx
  {
    if(c1.size() != c2.size()) 
      throw new InternalError("Unequal sizes in leq");

    Iterator i1 = c1.iterator();
    Iterator i2 = c2.iterator();
    
    while(i1.hasNext())
      {
	leq((Polytype)i1.next(),
	    (Polytype)i2.next());
      }
  }
  
  public static void initialLeq(TypeConstructor t, TypeConstructor[] ts)
    throws TypingEx
  {
    for(int i = 0; i<ts.length; i++)
      initialLeq(t, ts[i]);
  }
  
  public static void leq(TypeConstructor t, Collection c)
    throws TypingEx
  {
    Iterator i = c.iterator();
    
    while(i.hasNext())
      leq(t,(TypeConstructor)i.next());
  }
  
  /** Test that t is leq that m's head */
  public static void leq(TypeConstructor t, Monotype m)
  throws TypingEx
  {
    if(t == null)
      return;
    
    if (isTop(m))
      // Trivially true.
      return;

    AtomicKind v = t.variance;
    if(v == null)
      throw new InternalError("Don't know how to handle this");
    
    try{
      Engine.setKind(m, v);
    }
    catch(Unsatisfiable e){
      if (dbg)
	throw new TypingEx(t + " < " + m + "'s head :" + e);
      else
	throw new TypingEx("Debugging off");
    }
    leq(t, ((MonotypeConstructor) m.equivalent()).getTC());
  }
  
  public static void leq(TypeConstructor[] ts, Monotype[] ms)
  throws TypingEx
  {
    for(int i = 0; i < ts.length; i++)
      leq(ts[i], ms[i]);
  }
  
  /** Test that t is geq that m's head */
  public static void leq(Monotype m, TypeConstructor t)
  throws TypingEx
  {
    AtomicKind v = t.variance;
    if(v == null)
      throw new InternalError("Don't know how to handle this");
    
    try{
      Engine.setKind(m, v);
    }
    catch(Unsatisfiable e){
      throw new TypingEx(t+" > "+m+"'s head");
    }
    leq(((MonotypeConstructor) m.equivalent()).getTC(), t);
  }
  
  /** Assert that the vars are matching the base head constructors. */
  public static void leqHead(Monotype[] vars, Monotype[] base)
    throws TypingEx
  {
    for (int i = 0; i < vars.length; i++)
      {
        Monotype var = vars[i];
        nice.tools.typing.Types.setMarkedKind(var);
        nice.tools.typing.Types.setMarkedKind(base[i]);

        TypeConstructor baseMarker = base[i].head();
        if (baseMarker != null)
          leq(var.head(), baseMarker);

        TypeConstructor tag = nice.tools.typing.Types.constructor(base[i]);
        if (tag != null)
          leq(nice.tools.typing.Types.equivalent(vars[i]), tag);
      }
  }

  /****************************************************************
   * Testing Polytype <= Polytype
   ****************************************************************/

  public static void leq(Polytype t1, Polytype t2) 
    throws TypingEx
  {
    
    if (!(Constraint.hasBinders(t1.getConstraint()) || 
	  Constraint.hasBinders(t2.getConstraint())))
      {
	leq(t1.getMonotype(), t2.getMonotype());
	return;
      }
    
    if(dbg)
      Debug.println("Polytype leq: "+t1+" <: "+t2);

    int l;
    if(dbg) l=enter("#"); else l=enter();
    
    try{
      Constraint.enter(t2.getConstraint());

      implies();
    
      Constraint.enter(t1.getConstraint());
      leq(t1.getMonotype(), t2.getMonotype());
    }
    finally{
      if(leave() != l)
	throw new InternalError("Unmatched enters and leaves");
    }
  }

  /** Particular case. */
  public static void leq(Polytype t1, Monotype m2) 
    throws TypingEx
  {
    if (!(Constraint.hasBinders(t1.getConstraint())))
      {
	leq(t1.getMonotype(), m2);
	return;
      }
    
    if(dbg)
      Debug.println("Polytype leq: "+t1+" <: "+m2);

    int l;
    if(dbg) l=enter("#"); else l=enter();
    
    try{
      implies();
    
      Constraint.enter(t1.getConstraint());
      leq(t1.getMonotype(), m2);
    }
    finally{
      if(leave() != l)
	throw new InternalError("Unmatched enters and leaves");
    }
  }

  /****************************************************************
   * Monotypes
   ****************************************************************/

  public static void leq(Monotype m1, Monotype m2)
    throws TypingEx
  {
    if(dbg) 
      Debug.println("Monotype leq: " + m1 + " <: " + m2);

    try{
      Engine.leq(m1,m2);
    }
    catch(Unsatisfiable e){
      if(dbg) 
	e.printStackTrace();
      throw new MonotypeLeqEx(m1, m2, e);
    }
  }
  
  public static void eq(Monotype m1, Monotype m2)
    throws TypingEx
  {
    leq(m1,m2);
    leq(m2,m1);
  }
  
  public static void leq(Monotype[] ms1, Monotype[] ms2)
    throws TypingEx
  {
    for(int i = 0; i < ms1.length; i++)
      leq(ms1[i], ms2[i]);
  }

  /** @param dispatchable when true, we require that non dispatchable types be
                          equal, not mere subtypes.
  */
  public static void leq(Monotype[] ms1, Monotype[] ms2, boolean dispatchable)
    throws TypingEx
  {
    if (! dispatchable)
      for(int i = 0; i < ms1.length; i++)
        leq(ms1[i], ms2[i]);
    else
      for(int i = 0; i < ms1.length; i++)
        {
          Monotype m1 = ms1[i];
          Monotype m2 = ms2[i];
          leq(m1, m2);
          if (! nice.tools.typing.Types.isDispatchable(m2))
            leq(m2, m1);
        }
  }

  /****************************************************************
   * Type constructors
   ****************************************************************/

  public static void initialLeq(TypeConstructor t1, TypeConstructor t2)
    throws TypingEx
  {
    if(dbg) Debug.println("Initial leq: "+t1+" < "+t2);
    
    try{
      Engine.leq(t1,t2,true);
    }
    catch(Unsatisfiable e){
      throw new KindingEx(t1,t2);
    }
  }
  
  public static void leq(TypeConstructor t1, TypeConstructor t2)
    throws TypingEx
  {
    if(dbg) Debug.println("TC leq: "+t1+" < "+t2);
    
    try{
      Engine.leq(t1,t2,false);
    }
    catch(Unsatisfiable e){
      throw new TypingEx("Not satisfiable 4:"+e.getMessage());
    }
  }
  
  public static boolean testLeq(TypeConstructor t1, TypeConstructor t2)
  {
    if(t1.getKind() == null 
       || t2.getKind() == null)
      throw new InternalError("Null kind for "+t1+" or "+t2);
    
    try{
      Engine.leq(t1,t2,false);
      return true;
    }
    catch(Unsatisfiable e){
      return false;
    }
  }
  
  /****************************************************************
   * Domains 
   ****************************************************************/

  /** Test if d1 is a subdomain of d2. */
  public static void leq(Domain d1, Domain d2)
    throws TypingEx
  {
    leq(d1, d2, false);
  }

  /** Test if d1 is a subdomain of d2.
      @param dispatchable when true, we require that non dispatchable types be
                          equal, not mere subtypes.
  */
  public static void leq(Domain d1, Domain d2, boolean dispatchable)
    throws TypingEx
  {
    if(dbg) Debug.println(d1+" leq "+d2);
    
    if(d1 == Domain.bot)
      return;

    if (!(Constraint.hasBinders(d1.getConstraint()) || 
	  Constraint.hasBinders(d2.getConstraint())))
      {
	leq(d1.getMonotypes(), d2.getMonotypes(), dispatchable);
	return;
      }
    
    enter();
    try{
      Constraint.enter(d1.getConstraint());

      Typing.implies();

      Constraint.enter(d2.getConstraint());
      leq(d1.getMonotypes(), d2.getMonotypes(), dispatchable);
    }
    finally{
      leave();
    }
  }

  public static boolean smaller(Domain d1, Domain d2)
  {
    try {
      leq(d1, d2);
      return true;
    }
    catch(TypingEx ex) {
      return false;
    }
  }

  /** @param dispatchable when true, we require that non dispatchable types be
                          equal, not mere subtypes.
  */
  public static boolean smaller(Domain d1, Domain d2, boolean dispatchable)
  {
    try {
      leq(d1, d2, dispatchable);
      return true;
    }
    catch(TypingEx ex) {
      return false;
    }
  }

  /** Test if a polytype is in a domain. */
  public static void in(Polytype type, Monotype domain)
    throws TypingEx
  {
    if(dbg) Debug.println(type + " in " + domain);
    
    Constraint.enter(type.getConstraint());
    leq(type.getMonotype(), domain);
  }
  
  /**
   * Checks wether types belong to domains
   *
   * @param types a collection of Polytypes
   * @param domains a collection of domains
   * @exception TypingEx
   */  
  public static void in(Polytype[] types,
			Monotype[] domains)
    throws TypingEx
  {
    int expected = domains.length;
    int actual = types.length;
    if(expected != actual)
      throw new BadSizeEx(expected, actual);

    for(int i = 0; i<actual; i++)
      in(types[i], domains[i]);
  }

  /****************************************************************
   * Interfaces assertions
   ****************************************************************/

  public static void assertLeq(Interface i, Interface j)
  throws KindingEx
  {
    if(dbg) Debug.println(i+" < "+j);
    if(!(i.variance.equals(j.variance)))
      throw new KindingEx(i,j);
    i.variance.subInterface(i.itf,j.itf);
  }
  
  public static void assertLeq(Interface itf, Interface[] is)
  throws KindingEx
  {
    for (int i = is.length; --i >= 0;)
      assertLeq(itf, is[i]);
  }
  
  public static void assertImp(TypeConstructor t, Interface i, 
			       boolean initial)
    throws TypingEx
  {
    if(dbg) Debug.println(t+" imp "+i);

    try{
      Engine.setKind(t,i.variance.getConstraint());
    }
    catch(Unsatisfiable e){
      throw new KindingEx(t, i);
    }

    try{
      if(initial)
	((Variance) t.variance).initialImplements(t.getId(),i.itf);
      else
	((Variance) t.variance).indexImplements(t.getId(),i.itf);

      TypeConstructor tc = i.associatedTC();
      if(tc != null)
	Engine.leq(t, tc, initial);
    }
    catch(Unsatisfiable e){
      throw new TypingEx(e.getMessage());
    }
  }
  
  public static void assertAbs(TypeConstructor t, Interface i)
    throws TypingEx
  {
    if(dbg) Debug.println(t+" abs "+i);

    if(Engine.isRigid(t))
      throw new InternalError
	("Abstraction required on a rigid type constructor : \n"+
	 t+" required to abstract "+i);
    
    i.variance.initialAbstracts(t.getId(),i.itf);
  }

  public static void assertImp(TypeConstructor t, 
			       Interface[] is, 
			       boolean initial)
    throws TypingEx
  {
    for(int i = 0; i<is.length; i++)
      assertImp(t, is[i], initial);
  }
  
  public static void assertAbs(TypeConstructor t, Interface[] is)
    throws TypingEx
  {
    for (int i = 0; i<is.length; i++)
      assertAbs(t, is[i]);
  }

  /****************************************************************
   * Rigid tests
   ****************************************************************/

  public static boolean testRigidLeq(TypeConstructor t1, TypeConstructor t2)
  {
    if(t1.getKind() == null 
       || t2.getKind() == null)
      throw new InternalError("Null kind for "+t1+" or "+t2);
    
    if(t1.getKind() != t2.getKind())
      return false;

    return ((Engine.Constraint) t1.getKind()).isLeq(t1,t2);
  }
  
  /**
     Find an instance of the parameter
     that can exist at runtime (isConcrete() is true).
     It's better to return a lower (more precise) one.
  */
  public static TypeConstructor lowestInstance(TypeConstructor tc)
  {
    Engine.Constraint cst = (Engine.Constraint) tc.getKind();
    
    if (!cst.isValid(tc))
      // we are not in the context for TC anymore
      // be careful to call lowestRigidSuperTC when the context is appropriate.
      {
	System.out.println
	  ("Warning: lowestInstance called inapropriately for " + tc);
	return null;
      }
    
    return (TypeConstructor) cst.lowestInstance(tc);
  }
  
  /****************************************************************
   * Tools
   ****************************************************************/

  static boolean isTop(Monotype m)
  {
    return m.getKind() == TopMonotype.TopKind.instance;
  }

  public static boolean dbg;
}
