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

package mlsub.typing.lowlevel;

class Interface {
  Interface(K0 k0, int iid) {
    this.k0 = k0;
    this.iid = iid;
  }
  private K0 k0;
  
  // index in the corresponding vector  K0.interfaces
  private int iid;
  int getIndex() {
    return iid;
  }

  // the set of IDs of all the sub-interfaces of this interface
  BitVector subInterfaces = new BitVector();

  // the set of all rigid indexes that implement this interface
  BitVector rigidImplementors = null;

  // the set of all indexes (rigid and soft) that implement this interface
  BitVector implementors = new BitVector();
  
  // the set of all (rigid) indexes that abstract this interface
  BitVector abstractors = new BitVector();
  
  // the approximation of each node of the original context
  private final IntVect approx = new IntVect(BitVector.UNDEFINED_INDEX);
  private final BitVector hasapprox = new BitVector();

  public BitVector getHasApprox()
  {
    return hasapprox;
  }
   
  public void setApprox(int node, int approx)
  {
    if (node >= this.approx.size())
      this.approx.setSize(node+1, BitVector.UNDEFINED_INDEX);
    this.approx.set(node, approx);
    if (approx == BitVector.UNDEFINED_INDEX)
      hasapprox.clear(node);
    else    
      hasapprox.set(node);
  }
  
  /**
   * Get the approximation of a node
   *
   * @param node the index of a node
   * @return its approximation
   */
  public int getApprox(int node)
  {
    return approx.get(node);
  }

  void setIndexSize(int n) {
    implementors.truncate(n);
    approx.setSize(n,BitVector.UNDEFINED_INDEX);
    hasapprox.truncate(n);
  }
  void indexMove(int src, int dest) {
    implementors.bitCopy(src, dest);
    approx.set(dest,approx.get(src));
    hasapprox.set(dest);
    if (k0.isRigid(src)) {
      // strange as the relation on the rigid indexes should already be
      // condensed... but let's be general
      rigidImplementors.bitCopy(src, dest);
      abstractors.bitCopy(src, dest);
    }
  }
  void indexMerge(int src, int dest) {
    implementors.bitMerge(src, dest);
    if (k0.isRigid(src)) {
      // this is strange... (see above)
      rigidImplementors.bitMerge(src, dest);
      abstractors.bitMerge(src, dest);
    }
  }

  public String toString()
  {
    return k0.interfaceToString(iid);
  }
}
