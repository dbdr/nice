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

// File    : Kind.java
// Created : Wed Jul 28 14:53:22 1999 by bonniot
//$Modified: Wed Aug 02 16:51:36 2000 by Daniel Bonniot $

package mlsub.typing.lowlevel;

/** Something that knows how to assert constraints on objects of this "Kind"
 * 
 * (implemented by Variance, Low level constraints... )
 * 
 * @author bonniot
 */
public interface Kind
{
  /** Asserts that two elements are in a certain order
   * 
   * @exception Unsatisfiable 
   * @param e1 The smaller element
   * @param e2 The greater element
   */
  void leq(Element e1, Element e2) throws Unsatisfiable;
  void leq(Element e1, Element e2, boolean initial) throws Unsatisfiable;

  /** Introduce a new Element of this kind
   * 
   * @param e 
   */
  void register(Element e);

  /**
     Return a fresh monotype of this kind, or null if that does not make sense.

     This makes a dependancy from mlsub.typing.lowlevel to mlsub.typing,
     but they are likely to be used together anyway.
  */
  mlsub.typing.Monotype freshMonotype();
}
