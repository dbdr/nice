package bossa.engine;

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
  private int[] approx;

  public void setApprox(int[] approx)
  {
    this.approx=approx;
  }
  
  /**
   * Get the approximation of a node
   *
   * @param node the index of a node from the initial context (0 <= node < k0.m0)
   * @return its approximation
   */
  public int getApprox(int node)
  {
    return approx[node];
  }

  void setIndexSize(int n) {
    implementors.truncate(n);
  }
  void indexMove(int src, int dest) {
    implementors.bitCopy(src, dest);
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
}
