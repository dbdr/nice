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

package bossa.syntax;

/**
   A function is either a method body or a lambda expression.
   
   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
*/

import mlsub.typing.Polytype;

interface Function
{
  /**
     The expected return type of this function.
     Can be null if it is not known (e.g. the type is inferred).
  */
  mlsub.typing.Monotype getExpectedType();

  /**
     Called with each type returned from the function.
     Can be used for either type checking or type inference.
   */
  void checkReturnedType(mlsub.typing.Polytype returned) 
   throws ReturnTypeError;

  abstract class ReturnTypeError extends Exception {}

  class WrongReturnType extends ReturnTypeError
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

  static class IncompatibleReturnType extends ReturnTypeError
  {
    IncompatibleReturnType(Polytype previouslyInferredType)
    {
      this.previouslyInferredType = previouslyInferredType;
    }

    Polytype previouslyInferredType;
  }
}
