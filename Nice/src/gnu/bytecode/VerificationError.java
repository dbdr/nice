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

package gnu.bytecode;

/**
   The error thrown when an attempt to generate invalid bytecode is made.

   @version $Date$
   @author  ()
 */

public class VerificationError extends Error
{
  VerificationError(String message)
  {
    super(message);
  }
}
