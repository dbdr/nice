/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

/**
   A function is either a method body or a lambda expression.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

interface Function
{
  /**
     Called with each type returned from the function.
     Can be used for either type checking or type inference.
   */
  void checkReturnedType(mlsub.typing.Polytype returned) 
   throws WrongReturnType;

  class WrongReturnType extends Exception 
  {
    WrongReturnType(mlsub.typing.TypingEx typingException,
		    mlsub.typing.Monotype expectedReturnType)
    {
      this.typingException = typingException;
      this.expectedReturnType = expectedReturnType;
    }

    mlsub.typing.TypingEx typingException;
    mlsub.typing.Monotype expectedReturnType;
  }
}
