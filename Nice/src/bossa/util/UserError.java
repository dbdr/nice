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

package bossa.util;

public class UserError extends RuntimeException
{
  public UserError()
  {
    if(Debug.alwaysDumpStack)
      Internal.printStackTrace();    
  }

  UserError(String message)
  {
    this.message = message;

    if(Debug.alwaysDumpStack)
      Internal.printStackTrace();    
  }

  public UserError(Located responsible, String message)
  {
    this(message);
    this.responsible = responsible;
  }

  public String getMessage()
  {
    if (responsible == null || responsible.location() == null)
      return message;
    else
      return responsible.location() + ":\n" + message;
  }

  public String message;
  public Located responsible;
}
