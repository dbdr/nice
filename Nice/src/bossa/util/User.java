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

// File    : User.java
// Created : Wed Jul 07 18:20:58 1999 by bonniot
//$Modified: Wed Oct 04 11:46:29 2000 by Daniel Bonniot $

package bossa.util;

/**
   Messages for the user.
   
   When the error or warning is conditional, put it inside a 'if'.
   This is better than passing a boolean to error(), 
   since the latter, forcing evaluation of the string,
   would slow down compilation.
 */
public final class User
{
  public static UserError error(Located responsible, Exception exn)
  {
    return error(responsible, exn.toString());
  }

  public static UserError error(Exception exn)
  {
    return error(exn.toString());
  }

  public static UserError error(Located responsible, String message, String dbgMsg)
  {
    if(Debug.powerUser)
      return error(responsible,message+dbgMsg);
    else
      return error(responsible,message);
  }

  public static UserError error(Located responsible, String message, Exception dbgExn)
  {
    if(Debug.powerUser)
      return error(responsible,message+" ["+dbgExn+"]");
    else
      return error(responsible,message);
  }

  public static UserError error(Located responsible, String message)
  {
    if (responsible == null)
      throw new UserError(message);
    else 
      throw new UserError(responsible, message);
  }

  public static UserError error(String message)
  {
    throw new UserError(message);
  }

  public static void warning(Located responsible, String message)
  {
    if(Debug.alwaysDumpStack)
      Internal.printStackTrace();
    
    nice.tools.compiler.OutputMessages.warning
      ("\n"+(responsible == null ? "" : responsible.location().toString() + ": ") +
       "Warning:\n" + message);
  }

  public static void warning(String message)
  {
    warning(null, message);
  }
}
