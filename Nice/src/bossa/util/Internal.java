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

// File    : Internal.java
// Created : Wed Jul 07 18:23:19 1999 by bonniot
//$Modified: Wed Oct 04 11:47:18 2000 by Daniel Bonniot $
// Description : Internal errors...

package bossa.util;

/**
   Internal messages are sent through this static class
 */

public final class Internal
{
  public static void printStackTrace()
  {
    try{  
      throw new Exception();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
        
  public static void warning(Located loc, String message)
  {
    Location l = loc.location();
    if(l==null)
      warning(message);
    else
      warning(l+":\n"+message);
  }
  
  public static void warning(String message)
  {
    if(Debug.alwaysDumpStack)
      printStackTrace();
    nice.tools.compiler.OutputMessages.warning("[Internal warning] "+message);
  }

  public static void error(Located loc, String message, String dbgMsg)
  {
    if(Debug.powerUser)
      error(loc, message + dbgMsg);
    else
      error(loc, message);
  }
  
  public static void error(Located loc, String message)
  {
    Location l = loc.location();
    if(l==null)
      error(message);
    else
      error(l+":\n"+message);
  }

  public static void error(String message, String dbgMsg)
  {
    if(Debug.powerUser)
      error(message+dbgMsg);
    else
      error(message);
  }
  
  public static void error(String message)
  {
    System.out.println("[Internal error] "+message);
    printStackTrace();
    System.exit(1);
  }
  
  public static void error(Throwable e)
  {
    if(e instanceof ExceptionInInitializerError)
      {
	System.out.println("Exception in initializer");
	e.printStackTrace();
	e = ((ExceptionInInitializerError) e).getException();
      }
    
    String msg = e.getMessage();
    if (msg == null)
      msg = "";
    
    System.out.println("[Internal error] "+msg);
    e.printStackTrace();
    System.exit(1);
  }

}
