/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.modules;

import java.io.*;
import java.util.*;
import bossa.syntax.LocatedString;

/**
   The interface of a parser for the Nice language

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public interface Parser
{
  LocatedString readImports(Reader r, List imports, Collection opens);

  void read(Reader r, List definitions);

  /*bossa.syntax.FormalParameters*/Object formalParameters(String parameters);
}
