/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package mlsub.typing;

import mlsub.typing.lowlevel.Kind;
import mlsub.typing.lowlevel.Element;

/**
   A monotype greater than all others.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class TopMonotype extends Monotype implements TypeSymbol
{
  private TopMonotype() {}

  public static final TopMonotype instance = new TopMonotype();

  public TypeSymbol cloneTypeSymbol() { return this; }

  Monotype canonify() { return this; }

  Monotype substitute(java.util.Map map) { return this; }

  void tag(int variance) {}

  public String toString() { return "Object"; }

  /****************************************************************
   * low-level interface
   ****************************************************************/

  public int getId() 		{ throw new Error(); }
  public void setId(int value) 	{ throw new Error(); }
  
  public Kind getKind() 	  { return TopKind.instance; }
  public void setKind(Kind value) { throw new Error(); }

  /****************************************************************
   * The Top kind
   ****************************************************************/

  public static class TopKind implements Kind
  {
    private TopKind() {}

    public static final TopKind instance = new TopKind();

    public void leq(Element e1, Element e2, boolean initial)
    {
      // Nothing to do.
    }

    public void leq(Element e1, Element e2)
    {
      // Nothing to do.
    }

    public void register(Element e)
    {
      // Nothing to do.
    }

    public Monotype freshMonotype(boolean existential)
    {
      return null;
    }
  }
}
