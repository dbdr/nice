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

package bossa.link;

import bossa.util.*;
import bossa.syntax.*;

/**
   An alternative present in the source code.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class SourceAlternative extends Alternative implements Located
{
  public SourceAlternative(MethodImplementation implementation)
  {
    super(implementation.getDeclaration().getName().toString(), 
	  implementation.getPatterns());
    this.implementation = implementation;

    add(implementation.getDeclaration().getFullName());

    java.util.Iterator specializedMethods = 
      implementation.getDeclaration().listSpecializedMethods();
    if (specializedMethods != null)
      for (; specializedMethods.hasNext();)
        {
          add(((MethodDeclaration) specializedMethods.next()).getFullName());
        }
  }

  private MethodImplementation implementation;

  public Location location()
  {
    return implementation.location();
  }

  String printLocated()
  {
    return implementation.location() + ": " + toString();
  }

  public gnu.expr.Expression methodExp()
  {
    return implementation.getRefExp();
  }
}
