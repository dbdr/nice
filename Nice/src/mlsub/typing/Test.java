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

// File    : Test.java
// Created : Fri Jun 02 18:38:57 2000 by Daniel Bonniot
//$Modified: Thu Jun 22 21:05:59 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * Test class for mlsub.typing
 * 
 * @author Daniel Bonniot
 */

public class Test
{
  public static void main(String[] args)
  {
    System.out.println("Test for mlsub.typing");

    try{
      Variance v0 = Variance.make(new int[0]);

      TypeConstructor a = new TypeConstructor("a", v0, true, false);
      Monotype ma = new MonotypeConstructor(a, null);
      Typing.introduce(a);
      Interface i0 = new Interface(v0);
      Typing.assertImp(a, i0, true);
      Interface i1 = new Interface(v0);
      Typing.assertLeq(i1, i0);
      //Typing.assertImp(a, i1, true);      
      
      TypeConstructor t1 = new TypeConstructor(v0);
      Monotype m1 = new MonotypeConstructor(t1, null);
      Polytype p1 = new Polytype(new Constraint(new TypeConstructor[]{t1},  
						new AtomicConstraint[]{new ImplementsCst(t1, i1)}), m1);

      TypeConstructor t2 = new TypeConstructor(v0);
      Monotype m2 = new MonotypeConstructor(t2, null);
      Polytype p2 = new Polytype(new Constraint(new TypeConstructor[]{t2}, new AtomicConstraint[]{new MonotypeLeqCst(m2, ma),new ImplementsCst(t2, i1)}), m2);
      
      Typing.dbg = false;
    
      Typing.createInitialContext();

      Typing.leq(p1,p1);
    }
    catch(TypingEx e){
      System.out.println("Ill typed: "+e);
    }
  }
}
