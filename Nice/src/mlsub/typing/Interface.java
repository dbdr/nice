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

// File    : Interface.java
// Created : Fri Jun 02 17:26:52 2000 by Daniel Bonniot
//$Modified: Thu Sep 21 17:47:38 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * Structural interface.
 * 
 * @author Daniel Bonniot
 */

public final class Interface implements TypeSymbol
{
  /**
     Creates a new interface in the given variance.
     The interface is introduced in the rigid context.
   */
  public Interface(Variance variance)
  {
    this.variance = variance;
    itf = variance.newInterface();

    // variance.top should be null only if this interface is top,
    // as a top interface is created automatically with the Variance object
    if(variance.top != null)
      variance.subInterface(itf, variance.top.itf);
  }

  /**
     @param associatedTC a type constructor linked to this interface
     (should this go outside mlsub.typing?)
  */
  public Interface(Variance variance, TypeConstructor associatedTC)
  {
    this(variance);
    this.associatedTC = associatedTC;
  }

  /**
     @param name the name of the interface
  */
  public Interface(Variance variance, String name)
  {
    this(variance);
    this.name = name;
  }
  
  public TypeSymbol cloneTypeSymbol()
  {
    return new Interface(variance, associatedTC);
  }
  
  public TypeConstructor associatedTC()
  {
    return associatedTC;
  }

  public String toString()
  {
    if (associatedTC != null)
      return associatedTC.toString();
    else if (name != null)
      return name;
    else
      return super.toString();
  }
  
  int itf; // lowlevel interface
  Variance variance;
  private TypeConstructor associatedTC;

  /** Can be set for debugging reasons */
  public String name;
}
