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

// File    : Domain.java
// Created : Sat Jul 24 19:10:04 1999 by bonniot
//$Modified: Tue Jul 27 17:20:21 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A domain 
 * 
 * @author bonniot
 */

public class Domain
{
  public Domain(Constraint constraint, Monotype monotype)
  {
    this.constraint=constraint;
    this.monotype=monotype;
  }

  static Domain bottom()
  {
    MonotypeVar alpha=Monotype.fresh(new LocatedString("alpha",
						       Location.nowhere()),
				     null);
    return new Domain
      (new Constraint(alpha,null),
       alpha);
  }
  
  public String toString()
  {
    return "Ex "+constraint+" "+monotype;
  }

  static Collection fromMonotypes(Collection monotypes)
  {
    Collection res=new ArrayList(monotypes.size());
    for(Iterator i=monotypes.iterator();
	i.hasNext();)
      res.add(new Domain(Constraint.True(),(Monotype)i.next()));
    return res;
  }
  
  public Constraint constraint;
  public Monotype monotype;
}
