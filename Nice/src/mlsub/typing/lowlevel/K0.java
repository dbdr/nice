package mlsub.typing.lowlevel;

import java.util.Vector;

/**
 * A lowlevel constraint on integers.
 *
 * @version $OrigRevision: 1.22 $, $OrigDate: 1999/10/28 10:56:58 $
 * @author Alexandre Frey
 * @author Daniel Bonniot (Abstractable Interfaces, and unlimited backtrack)
 **/
public final class K0 {
  public static abstract class Callbacks  {
    /**
     * Called when indexes src and dest have been merged.  Only dest remains
     * valid: src is invalidated
     **/
    abstract protected void indexMerged(int src, int dest)
      throws Unsatisfiable;
    
    /**
     * Called when index src has been moved to index dest.
     **/
    abstract protected void indexMoved(int src, int dest);
    
    /**
     * Called when simplification discards index
     **/
    abstract protected void indexDiscarded(int index);

    /*
     * Pretty printing
     */
    protected String getName() {
      return "<k0>";
    }
    protected String indexToString(int x) {
      return Integer.toString(x);
    }
    protected String interfaceToString(int iid) {
      return Integer.toString(iid);
    }
  }
  
  /***********************************************************************
   * Debugging
   ***********************************************************************/
  public static boolean debugK0 = S.debugK0;
  private static int IDs = 0;
  private int ID = IDs++;
  
  // When a K0 is created it is in a special mode where the initial rigid
  // context is created. In this mode, one can use methods extend, initialLeq,
  // minimal, newInterface, subInterface, and initialImplements. Then,
  // one must call method createInitialContext()
  //
  // The constraint is then in normal mode, one can no more use the
  // above-mentioned methods (except extend),
  // but must use leq, eq, indexImplements, satisfy,
  // enumerate, rigidify, mark, backtrack, weakMark, weakBacktrack, getSnap,
  // startSimplify, stopSimplify, tag, simplify, isLeq,
  // solveConstructorOverloading, solveInterfaceOverloading
  //
  // backtrackMode must be either BACKTRACK_ONCE or BACKTRACK_UNLIMITED
  // see below the doc of backtrack() and mark()
  public K0(int backtrackMode, Callbacks callbacks) {
    this.backtrackMode = backtrackMode;
    this.callbacks = callbacks;
    this.R = null;
    this.Rt = null;
    this.m = 0;
    this.C = new BitMatrix();
    this.Ct = new BitMatrix();
    this.n = 0;
    this.minimal = new BitVector();
    this.garbage = new BitVector();
    this.posTagged = new BitVector();
    this.negTagged = new BitVector();
    this.interfaces = new Vector();
    if (debugK0) {
      System.err.println("created K0 #" + ID);
    }
  }

  public K0(Callbacks callbacks) {
    this(BACKTRACK_ONCE, callbacks);
  }

  private Callbacks callbacks;
  
  /**
   * Relation on the rigid indexes
   **/
  private BitMatrix R;
  /**
   * Transpose of R
   **/
  private BitMatrix Rt;
  /**
   * Number of rigid indexes. Below m, the relation is closed.
   **/
  private int m;

  /**
   * Number of rigid indexes in the initial rigid context
   * (m0 <= m)
   **/
  private int m0;

  int initialContextSize()
  {
    return m0;
  }
  
  /**
   * Returns an index that is guaranteed to be greater than any rigid index
   * and less or equal to any non-rigid index.
   **/
  public int firstNonRigid() {
    return m;
  }
  
  /**
   * Returns true if x is the index of a rigid variable.
   * the index is assumed valid
   **/
  public boolean isRigid(int x) {
    return x < m;
  }

  public boolean hasNoSoft() {
    return n == m;
  }
  /**
   * C maintains the constraint as it is entered
   **/
  private BitMatrix C;
  /**
   * Transpose of C
   **/
  private BitMatrix Ct;
  /**
   * Total number of indexes. 
   * Invariant: m <= n && n == C.size()
   **/
  private int n;

  public int size() {
    return n;
  }
  
  /**
   * Maintains the information "x is minimal" on [0, m0[
   **/
  private BitVector minimal;
  public boolean isMinimal(int x) {
    return minimal.get(x);
  }
  public void minimal(int x) {
    minimal.set(x);
  }
  
  // tells if an index should be collected in the next call to collect()
  private BitVector garbage;

  public boolean isValidIndex(int x) {
    return x >= 0 && x < n && !garbage.get(x);
  }

  
  /***********************************************************************
   * Pretty printing
   ***********************************************************************/
  public String getName() {
    return callbacks.getName();
  }
  
  private String indexToString(int index) {
    return callbacks.indexToString(index);
  }
  private String interfaceToString(int iid) {
    return callbacks.interfaceToString(iid);
  }

  public String domainsToString() {
    StringBuffer sb = new StringBuffer();
    for (int i = m; i < n; i++) {
      if (isValidIndex(i)) {
        sb.append("D(").append(indexToString(i)).append(") = ");
        Domain d = (hasBeenInitialized ? domains.getDomain(i) : null);
	sb.append(d);
        sb.append("; ");
      }
    }
    return sb.toString();
  }

  private BitVector weakComponent(int x0) {
    S.assume(S.a&& isValidIndex(x0));
    BitVector component = new BitVector();
    BitVector toInclude = new BitVector();
    toInclude.set(x0);
    while (true) {
      int x = toInclude.getLowestSetBitNotIn(component);
      if (x == BitVector.UNDEFINED_INDEX) {
        break;
      }
      if (!garbage.get(x)) {
        component.set(x);
        BitVector ux = C.getRow(x);
        if (ux != null) {
          toInclude.or(ux);
        }
        BitVector lx = Ct.getRow(x);
        if (lx != null) {
          toInclude.or(lx);
        }
      }
    }
    return component;
  }

  private String ineqToString(int x, int y) {
    return indexToString(x) + " <: " + indexToString(y);
  }

  private String componentToString(BitVector component) {
    StringBuffer sb = new StringBuffer();
    Separator sep = new Separator(", ");
    int nvars = 0;
    for (int x = 0; x < n; x++) {
      if (component.get(x)) {
        sb.append(sep).append(indexToString(x));
        if (isRigid(x)) {
          sb.append("*");
        }
        nvars++;
      }
    }
    if (nvars == 0) {
      return "";
    }

    sb.append(" | ");
    sep.reset();

    // enumerate the (x, y) where x < y and at least one of x or y is >= m0
    for (int x = 0; x < n; x++) {
      if (component.get(x)) {
        for (int y = (x < m0 ? m0 : x+1); y < n; y++) {
          if (component.get(y)) {
            if (C.get(y, x)) {
              sb.append(sep).append(ineqToString(y, x));
            }
            if (C.get(x, y)) {
              sb.append(sep).append(ineqToString(x, y));
            }
          }
        }
      }
    }

    for (int x = m0; x < n; x++) {
      if (component.get(x)) {
        for (int iid = 0; iid < nInterfaces(); iid++) {
          if (getInterface(iid).implementors.get(x)) {
            sb.append(sep)
              .append(indexToString(x))
              .append(": ")
              .append(interfaceToString(iid));
          }
        }
      }
    }
    return sb.toString();
  }
  
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    Separator nl = new Separator("\n");

    BitVector rest = new BitVector(n);
    rest.fill(n);
    String restComp = componentToString(rest);
    if (!restComp.equals("")) {
      sb.append(nl).append(restComp);
    }
    if (sb.length() != 0) {
      sb.append("\n");
    }
    return sb.toString();
  }

  public String dumpInterfaces() {
    final StringBuffer sb = new StringBuffer();
    final Separator sep = new Separator(", ");
    for (int iid = 0; iid < nInterfaces(); iid++) {
      sb.append(sep).append(interfaceToString(iid));
    }
    sb.append(" | ");
    sep.reset();
    for (int iid1 = 0; iid1 < nInterfaces(); iid1++) {
      for (int iid2 = 0; iid2 < nInterfaces(); iid2++) {
        if (getInterface(iid2).subInterfaces.get(iid1)) {
          sb.append(sep)
          .append(interfaceToString(iid1))
          .append(" < ")
          .append(interfaceToString(iid2));
        }
      }
    }
    sb.append("\n");
    sep.reset();
    try{
      implementsIter
      (
       new ImplementsIterator(){
	public void iter(int x, int iid){
	  sb.append(sep)
	    .append(indexToString(x))
	    .append(" : ")
	    .append(interfaceToString(iid));
	}
      }
       );
      }
    catch(Unsatisfiable e){}
    try{
      abstractsIter
      (
       new AbstractsIterator(){
	public void iter(int x, int iid){
	  sb.append(sep)
	    .append(indexToString(x))
	    .append(" :: ")
	    .append(interfaceToString(iid));
	}
      }
       );
      }
    catch(Unsatisfiable e){}
    return sb.toString();
  }

  public String dumpRigid() {
    StringBuffer sb = new StringBuffer();
    Separator sep = new Separator(", ");
    for (int x = 0; x < m; x++) {
      if (isValidIndex(x)) {
        sb.append(sep).append(indexToString(x));
      }
    }
    sb.append(" | ");
    sep.reset();
    for (int x = 0; x < m; x++) {
      for (int y = 0; y < m; y++) {
        if (isValidIndex(x) && isValidIndex(y) && R.get(x, y)) {
          sb.append(sep)
          .append(indexToString(x))
          .append(" < ")
          .append(indexToString(y));
        }
      }
    }
    for (int iid = 0; iid < nInterfaces(); iid++) {
      for (int x = 0; x < m; x++) {
        if (isValidIndex(x) && getInterface(iid).rigidImplementors.get(x)) {
          sb.append(sep)
          .append(indexToString(x))
          .append(": ")
          .append(interfaceToString(iid));
        }
      } 
    }

    return sb.toString();
  }
  
  /***********************************************************************/
  private void setSize(int n) {
    S.assume(S.a&& n >= m);
    this.n = n;
    C.setSize(n);
    Ct.setSize(n);
    if (domains != null) {
      domains.setSize(n - m);
    }
    garbage.truncate(n);
    posTagged.truncate(n);
    negTagged.truncate(n);
    minimal.truncate(n);
    for (int iid = 0; iid < nInterfaces(); iid++) {
      getInterface(iid).setIndexSize(n);
    }
  }
  
  // collect and compact indexes in [i, j[
  // returns the last non garbage index plus one of the compacted matrix
  private int collect(int i, int j) {
    while (true) {
      while (true) {
        if (i >= j) { return i; }
        if (garbage.get(i)) { break; }
        i++;
      }
      while (true) {
        if (i >= j) { return i; }
        j--;
        if (!garbage.get(j)) { break; }
      }
      indexMove(j, i);
      i++;
    }
  }

  // this operation always succeeds
  private void collect() {
    setSize(collect(m, n));
  }      

  // assume src > dest && !garbage.get(src) && garbage.get(dest)
  private void indexMove(int src, int dest) {
    domainMove(src, dest);
    posTagged.bitCopy(src, dest);
    negTagged.bitCopy(src, dest);
    for (int iid = 0; iid < nInterfaces(); iid++) {
      getInterface(iid).indexMove(src, dest);
    }
    garbage.set(src);
    garbage.clear(dest);
    C.indexMove(src, dest);
    Ct.indexMove(src, dest);
    
    // notify the user of K0
    callbacks.indexMoved(src, dest);
  }
  
  // assume src > dest && !garbage.get(src) && !garbage.get(dest)
  private void indexMerge(int src, int dest) throws Unsatisfiable {
    domainMerge(src, dest);
    C.indexMerge(src, dest);
    Ct.indexMerge(src, dest);
    for (int iid = 0; iid < nInterfaces(); iid++) {
      getInterface(iid).indexMerge(src, dest);
    }
    garbage.set(src);
    posTagged.bitMerge(src, dest);
    negTagged.bitMerge(src, dest);
    // notify the subclass
    callbacks.indexMerged(src, dest);
  }
  
  /**
   * Add a new variable to the constraint and returns its index
   **/
  public int extend() {
    C.extend();
    Ct.extend();
    garbage.clear(n);
    if (hasBeenInitialized) {
      domains.extend();
    }
    return n++;
  }

  // a vector of all the Interfaces in this constraint
  private Vector interfaces;
  /**
   * Returns the number of interfaces. Interfaces are garanteed to be
   * numbered from 0 to nInterfaces()-1
   **/
  public int nInterfaces() {
    return interfaces.size();
  }

  // assume 0 <= iid < nInterfaces()
  Interface getInterface(int iid) {
    return (Interface)interfaces.elementAt(iid);
  }

  /***********************************************************************
   * Construction of the initial rigid context
   ***********************************************************************/
  private boolean hasBeenInitialized = false;
  public boolean hasBeenInitialized() {
    return hasBeenInitialized;
  }
  /**
   * Add a new interface to the constraint and returns its ID
   **/
  public int newInterface() {
    S.assume(S.a&& !hasBeenInitialized);
    int iid = nInterfaces();
    Interface iface = new Interface(this, iid);
    interfaces.addElement(iface);
    if (debugK0) {
      System.err.println("newInterface in #" + ID + " -> " + iid);
    }
    return iid;
  }

  /**
   * Enter the assertion that interface iid1 is a subinterface of iid2
   * Assume if1 and if2 are >=0 and < nInterfaces()
   **/
  public void subInterface(int iid1, int iid2) {
    S.assume(S.a&& !hasBeenInitialized);
    getInterface(iid2).subInterfaces.set(iid1);
  }

  /**
   * Enter the initial assertion that x : iid
   **/
  public void initialImplements(int x, int iid) {
    S.assume(S.a&& !hasBeenInitialized);
    getInterface(iid).implementors.set(x);
  }

  /**
   * Enter the initial assertion that x :: iid
   * This means that no node strictly x may implement iid
   **/
  public void initialAbstracts(int x, int iid) {
    S.assume(S.a&& !hasBeenInitialized);
    getInterface(iid).abstractors.set(x);
  }

  /**
   * Enter the initial assertion that x <= y
   **/
  public void initialLeq(int x, int y) {
    S.assume(S.a&& !hasBeenInitialized);
    C.set(x, y);
    Ct.set(y, x);
  }

  /***********************************************************************
   * Initial rigidification
   ***********************************************************************/

  public void createInitialContext() 
    throws LowlevelUnsatisfiable
  {
    S.assume(S.a&& !hasBeenInitialized);
    
    // put in R and Rt, the constraint saturated under
    // x < y and y < z => x < z
    R = (BitMatrix)C.clone();
    R.closure();
    Rt = (BitMatrix)Ct.clone();
    Rt.closure();
    m0 = m = n;
    
    // saturate the interface subtyping under
    // I < J and J < K => I < K
    closeInterfaceRelation();

    BitVector[] rigidImplementors = closeImplements(R, Rt);
    for (int iid = 0; iid < nInterfaces(); iid++) {
      getInterface(iid).rigidImplementors = rigidImplementors[iid];
    }

    computeInitialArrows();
    
    if(debugK0)
      System.err.println("Initial Context (saturated) "+getName()+":\n" +
		    this + "\n" + 
		    dumpInterfaces() + "\n");
    
    domains = new DomainVector(m, m);
    hasBeenInitialized = true;
  }

  public void releaseInitialContext() 
  {
    hasBeenInitialized=false;

    if(m != m0)
      System.err.println("releaseInitialContext should be called when in first rigid context");

    m0 = m = 0;
  }
  
  /***********************************************************************
   * Constraints
   ***********************************************************************/
  /**
   * Enter the constraint x: iid
   * Assume x is a valid index and iid is a valid interface id
   **/
  public void indexImplements(int x, int iid) throws Unsatisfiable {
    S.assume(S.a&& hasBeenInitialized);
    if (LowlevelUnsatisfiable.refinedReports) {
      try {
        indexImplements0(x, iid);
      } catch (LowlevelUnsatisfiable e) {
        throw refine(e);
      }
    } else {
      indexImplements0(x, iid);
    }
  }

  // this method makes no effort to report refined unsatisfiability
  private void indexImplements0(int x, int iid) throws LowlevelUnsatisfiable {
    if (debugK0) {
      System.err.println("#" + ID + " -> " + indexToString(x) + ": " + interfaceToString(iid));
    }
    Interface iface = getInterface(iid);
    if (iface.implementors.get(x)) {
      // nothing to do !
      return;
    }
    if (isRigid(x)) {
      if (!iface.rigidImplementors.get(x)) {
        throw new LowlevelImplementsClash(x, iid);
      } else {
        return;
      }
    }
    iface.implementors.set(x);
    // include unit since unit implements all the interfaces
    reduceDomain(x, true, iface.rigidImplementors);
  }
  
  /**
   * Enter the constraint x1 = x2
   * Assume x1 and x2 are valid indexes
   **/
  public void eq(int x1, int x2) throws Unsatisfiable {
    leq(x1, x2);
    leq(x2, x1);
  }

  /**
   * Enter the constraint x1 <= x2.
   * Assume x1 and x2 are valid indexes.
   **/
  public void leq(int x1, int x2) throws Unsatisfiable {
    S.assume(S.a&& hasBeenInitialized);
    if (LowlevelUnsatisfiable.refinedReports) {
      try {
        leq0(x1, x2);
      } catch (LowlevelUnsatisfiable e) {
        throw refine(e);
      }
    } else {
      leq0(x1, x2);
    }
  }

  public void enterConstraint(int x1, int v0, int x2) throws Unsatisfiable {
    if (v0 > 0) {
      leq(x1, x2);
    }
    if (v0 < 0) {
      leq (x2, x1);
    }
    if (v0 == 0) {
      eq(x1, x2);
    }
  }
  
  // versions that does not make any effort to report precise errors
  // in case of rigid clash
  private void leq0(int x1, int x2) throws LowlevelUnsatisfiable {
    if (debugK0) {
      System.err.println("#" + ID + " -> " + indexToString(x1) + " <: " + indexToString(x2));
    }
    if (x1 == x2) { return; }
    if (C.get(x1, x2)) { return; }
    // Modify C and Ct after test for rigid clash
    // so that the constraint if both x1 and x2 are rigid
    // makes it unnecessary to mark-backtrack in some cases
    if (isRigid(x1) && isRigid(x2)) {
      if (!R.get(x1, x2)) {
        throw new LowlevelRigidClash(indexToString(x1), indexToString(x2));
      } else {
        return;
      }
    }
    C.set(x1, x2);
    Ct.set(x2, x1);
    if (isRigid(x1)) {
      // !isRigid(x2)
      // exclude unit since unit is not comparable to x1
      reduceDomain(x2, false, R.getRow(x1));
    }
    if (isRigid(x2)) {
      // !isRigid(x1)
      // exclude unit
      reduceDomain(x1, false, Rt.getRow(x2));
      if (minimal.get(x2)) {
        // x1 = x2;
        leq0(x2, x1);
      }
    }
  }

  /***********************************************************************
   * Better error messages
   ***********************************************************************/
  // try to refine the exception e, in case of rigid clashes the constraint is
  // known to be unsatisfiable at this point NB: this method is only called in
  // case of unsatisfiability that is to be reported to the user. The
  // important point is to get a message that is precise enough. There is no
  // need to optimize this method.
  private LowlevelUnsatisfiable refine(LowlevelUnsatisfiable e) {
    if (K0.debugK0) {
      System.err.println("Trying to refine " + e);
      System.err.println("The constraint is " + this.toString());
      System.err.println("The rigid constraint is " + dumpRigid());
      e.printStackTrace();
    }
    if (e instanceof LowlevelRigidClash
        ||
        e instanceof LowlevelImplementsClash) {
      return e;
    }
    // close the constraint
    // it does no harm to modify the constraint since
    // it will be backtracked to display the error message
    C.closure();
    Ct.closure();

    BitVector[] saturatedImplementors = closeImplements(C, Ct);
    for (int iid = 0; iid < nInterfaces(); iid++) {
      getInterface(iid).implementors = saturatedImplementors[iid];
    }

    // then try to find a rigid clash, i.e., a constraint a < b where a is not
    // a subtype of b in the rigid context
    int[] rigidClash = C.includedIn(m, R);
    if (rigidClash != null) {
      return new LowlevelRigidClash(indexToString(rigidClash[0]), 
				    indexToString(rigidClash[1]));
    }

    // try to refine even better: find if the unsatisfiability
    // stems from attempt to put a common supertype or subtype
    // to incompatible rigid types
    LowlevelIncompatibleClash clash = null;
    clash
      = refineIncompatible(C, R,
                           LowlevelIncompatibleClash.NO_COMMON_SUPERTYPE);
    if (clash != null) {
      return clash;
    }
    clash
      = refineIncompatible(Ct, Rt,
                           LowlevelIncompatibleClash.NO_COMMON_SUBTYPE);
    if (clash != null) {
      return clash;
    }

    // finally try to find an "implements" clash
    for (int iid = 0; iid < nInterfaces(); iid++) {
      Interface iface = getInterface(iid);
      int x = iface.implementors.getLowestSetBitNotIn(iface.rigidImplementors);
      if (isValidIndex(x) && isRigid(x)) {
        // XXX: la condition est bonne ???
        // we've found an index x such that x: iid in the constraint but not
        // in the rigid context
        return new LowlevelImplementsClash(x, iid);
      }
    }
    
    // At this point, we failed to give a better error message
    // than the original one
    return e;
  }

  private LowlevelIncompatibleClash refineIncompatible(BitMatrix C,
                                                       BitMatrix R,
                                                       int what) {
    for (int a = 0; a < m; a++) {
      for (int b = a + 1; b < m; b++) {
        if (R.getRow(a).getLowestSetBitAnd(R.getRow(b))
            == BitVector.UNDEFINED_INDEX) {
          // R(a) and R(b) do not intersect
          BitVector Ca = C.getRow(a);
          if (Ca != null) {
            BitVector Cb = C.getRow(b);
            if (Cb != null) {
              int z = Ca.getLowestSetBitAnd(Cb);
              if (z != BitVector.UNDEFINED_INDEX) {
                // here is the culprit !
                return new LowlevelIncompatibleClash(what, a, b, z);
              }
            }
          }
        }
      }
    }
    return null;
  }
  
  /***********************************************************************
   * Domains
   ***********************************************************************/
  // each soft variable is associated to a domain, i.e., a subset of the rigid
  // variables plus unit (a special rigid value that implements all the
  // interfaces). Subtyping constraints between a soft variable and a rigid
  // one, and "implements" constraints on a soft variable immediately affect
  // the domain of the soft variable. If a domain becomes empty, the
  // constraint is unsatisfiable. When method satisfy or enumerate is called,
  // the domains already hold an initial approximation of the solutions, which
  // is refined by iteration of the closure operators associated to the
  // subtyping constraints between soft variables. Then, instantiation and
  // backtracking are used to find a solution (see Satisfier)
  private DomainVector domains = null;
  private void domainMerge(int src, int dest) throws Unsatisfiable {
    domains.merge(src, dest);
  }
  private void domainMove(int src, int dest) {
    domains.move(src, dest);
  }

  public void reduceDomain(int x, boolean unit, BitVector set)
  throws LowlevelUnsatisfiable {
    S.assume(S.a&& x >= -1);
    if (x == -1) {
      if (!unit) {
        throw new LowlevelUnsatisfiable();
      }
    } else if (x < m) {
      // the domain must contain x itself (we assume here that the relation
      // is condensed on the rigid variables)
      if (!set.get(x)) {
        throw new LowlevelUnsatisfiable();
      }
    } else {
      if(debugK0){
	System.err.println("Reducing domain of "+indexToString(x));
	System.err.println("from "+domains.getDomain(x));
	System.err.println("with "+set);
      }
      domains.reduce(x, unit, set);
    }
  }
    
  /***********************************************************************
   * Constraint preparation
   ***********************************************************************/

  // The constraint is "prepared" by
  // 1. saturate it under Min and Abs axioms
  // 2. condensing equivalent nodes

  /**
   * Saturate the constraint C, Ct under the axiom:
   * x <* y and Min(y) => y < x (hence x ~ y)
   **/
  private void collapseMinimal() throws Unsatisfiable {
    for (int y = 0; y < m0; y++) {
      if (minimal.get(y)) {
        BitVector uy = R.getRow(y);
        // breadth-first search of the lower ideal of y
        _collapsed.clearAll();
        _toCollapse.clearAll();
        _toCollapse.set(y);
        while (true) {
          int x = _toCollapse.getLowestSetBitNotIn(_collapsed);
          if (x == BitVector.UNDEFINED_INDEX) {
            break;
          }
          _collapsed.set(x);
          BitVector lx = Ct.getRow(x);
          if (lx != null) {
            _toCollapse.or(lx);
          }
          if (x != y && !C.get(y, x)) {
            // set y < x
            C.set(y, x);
            Ct.set(x, y);
            // exclude from D(x) all elements outside uy
            // exclude unit as well
            reduceDomain(x, false, uy);
          }
        }
      }
    }
  }
  // preallocate these bit-vectors
  private BitVector _collapsed = new BitVector(256);
  private BitVector _toCollapse = new BitVector(256);

  /****************************************************************
   * Algorithms on interfaces
   ****************************************************************/

  /**
   * Computes the arrows a ->_i b
   * for a's that abstract some surinterface of i
   */
  private void computeInitialArrows()
    throws LowlevelUnsatisfiable
  {
    for (int iid = 0; iid < nInterfaces(); iid++)
      {
	Interface i = getInterface(iid);
	BitVector abstractors = i.abstractors;
	BitVector subInterfaces = i.subInterfaces;
	
	for (int abs = abstractors.getLowestSetBit(); 
	     abs != BitVector.UNDEFINED_INDEX;
	     abs = abstractors.getNextBit(abs))
	  for (int jid = subInterfaces.getLowestSetBit(); 
	       jid != BitVector.UNDEFINED_INDEX;
	       jid = subInterfaces.getNextBit(jid))
	    // abs is a constant that abstracts i
	    // and j is a subInterface of i
	    setApproxToMinAbove(abs, getInterface(jid), R);
      }
    computeApproxMinimals(0, R);
  }

  /**
     Compute arrows for nodes in [min, n[
  */
  private void computeApproxMinimals(int min, BitMatrix leq)
    throws LowlevelUnsatisfiable
  {
    for (int node = minimal.getLowestSetBit(min);
	 node != BitVector.UNDEFINED_INDEX;
	 node = minimal.getNextBit(node))
      for (int iid = 0; iid < nInterfaces(); iid++)
	setApproxToMinAbove(node, getInterface(iid), leq);
  }
  
  /** 
      Finds a minimum rigid node above 'abs' that implements 'j'
      and set it as the approximation of 'abs' for 'j'.
  */
  private void setApproxToMinAbove(int abs, Interface j, BitMatrix leq)
    throws LowlevelUnsatisfiable
  {
    BitVector implementors = j.rigidImplementors;
   
    boolean toCheck = false;
    int approx = BitVector.UNDEFINED_INDEX;
	      
    for (int x = implementors.getLowestSetBit();
	 x != BitVector.UNDEFINED_INDEX && x < m0;
	 x = implementors.getNextBit(x)) 
      {
	if (leq.get(abs,x))
	  if (approx==BitVector.UNDEFINED_INDEX || leq.get(x,approx))
	    approx = x;
	  else
	    // optimize ? :
	    // make tocheck an int, -1 at start. first to check-> N, second to check->-2
	    // if at then end toCHeck =N, it's fast !
	    toCheck = true;
      }

    // verifies it is minimal
    if(toCheck)
      for (int x = implementors.getLowestSetBit();
	   x != BitVector.UNDEFINED_INDEX;
	   x = implementors.getNextBit(x)) 
	if(leq.get(abs,x) && !leq.get(approx,x))
	  throw new LowlevelUnsatisfiable("Node "+approx+" and "+x+" are uncomparable and both implement "+ j +
					  " above "+abs);
    if(debugK0) 
      System.err.println("Initial approximation for " +
		    j + ": " +
		    indexToString(abs) + " -> " + 
		    indexToString(approx));
    j.setApprox(abs,approx);
  }

  private void computeArrows(BitMatrix leq)
    throws LowlevelUnsatisfiable
  {
    computeApproxMinimals(m, leq);

    for(int iid=0; iid<nInterfaces(); iid++)
      {
	Interface i=getInterface(iid);
	i.setIndexSize(n);
	BitVector abstractors=i.abstractors;
	for (int x = abstractors.getLowestSetBit();
	     x != BitVector.UNDEFINED_INDEX;
	     x = abstractors.getNextBit(x)) 
	  {
	    int approx=i.getApprox(x);
	    if(approx!=BitVector.UNDEFINED_INDEX)
	      {
		// The approximation for indexes below m is fixed
		for(int node=m;node<n;node++)
		  if(leq.get(node,x))
		    {
		      i.setApprox(node,approx);
		      if(debugK0) 
			System.err.println("Approximation for "+iid+": "+
				      indexToString(node)+" -> "
				      +indexToString(approx));
		    }
	      }
	  }
      }
  }
  
  /**
   * Saturate the constraint under the Abs axiom.
   */
  private void saturateAbs(BitMatrix leq)
    throws Unsatisfiable
  {
    boolean changed;

    do
      {
	changed=false;

	for(int iid=0; iid<nInterfaces(); iid++)
	  {
	    Interface i=getInterface(iid);
	    int n1;
	    for(int node=0; node<n; node++)
	      if((n1=i.getApprox(node))!=BitVector.UNDEFINED_INDEX)
		// node  ->_i  n1
		for(int p=0; p<n; p++)
		  // is implementors OK ?
		  // or should not we compute rigidImplementors first ?
		  if(i.implementors.get(p)
		     && leq.get(node,p))
		    if(this.isRigid(p))
		      if(!leq.get(n1,p))
			throw new LowlevelUnsatisfiable
			  ("saturateAbs: there should be "+
			   indexToString(n1)+" <: "+
			   indexToString(p)+
			   " (node="+indexToString(node)+")\n"+
			   "interface "+interfaceToString(iid)+"\n"+
			   this);
		      else ;
		    else 
		      if(!leq.get(n1,p))
			 {
			   if(debugK0) 
			     System.err.println("Abs rule applied : "+
					   indexToString(n1)+" < "+indexToString(p)+
					   " using "+indexToString(node)+
					   " for interface "+interfaceToString(iid));
			   C .set(n1,p);
			   Ct.set(p,n1);
			   leq.set(n1,p);
			   changed=true;
			 }
	  }

	if(changed)
	  leq.closure();
      }
    while(changed);
  }

  private void condense()
    throws Unsatisfiable
  {
    BitMatrix T=(BitMatrix)C.clone();
    T.closure();
    condense(T);
  }
  
  /**
   * Condense the relation by merging equivalent indexes and collecting the
   * garbage nodes.
   * T is a closure of C
   **/
  private void condense(BitMatrix T) throws Unsatisfiable {
    // XXX: TODO: verify safety when indexMerged creates new nodes
    // XXX: actually, the specification should say that indexMerged
    // XXX: cannot access to K0 (neither create new nodes, nor enter new
    // XXX: constraints) (see Undispatched.VarK.indexMerged)

    // computes the connected components of (C, Ct).
    // we computes the transitive closure of C and Ct
    // this looks sub-optimal but:
    // 1. it's efficient in practice (transitive closure is cheap)
    // 2. it allows early test of satisfiability (if C* violates assertions
    //           on rigid variables
    // 3. it is obviously correct (Tarjan algorithm is a mess to debug...)

    if (m > 0) {
      if (T.includedIn(m, R) != null) {
        // T is NOT included in R on [0, m[ x [0, m[
        throw new LowlevelUnsatisfiable();// will be refined if necessary
      }
    }
    BitMatrix Tt = (BitMatrix)Ct.clone();
    Tt.closure();

    if(debugK0){
      System.err.println(toString());
      System.err.println(domainsToString());
    }
    
    for (int i = 0; i < n; i++) {
      if (!garbage.get(i)) {
        int root = T.getRow(i).getLowestSetBitAnd(Tt.getRow(i));
        if (root != i) {
          // we've found that root is equivalent to i
          // we are going to merge them. However, remember that the domains
          // of soft variables are assumed to keep track of all
          // the direct constraints relations between soft and rigid variables
          // and soft and interfaces.
          //
          // if root and i are soft, we simply have to merge their domains
          // (take the intersection) to account for all direct constraints
          //
          // if root is rigid and i is soft, then all the soft variables that
          // are comparable to i should have their domain restricted
          //
          // root cannot be soft if i is rigid because root < i
          if (isRigid(root) && !isRigid(i)) {
            for (int j = m; j < n; j++) {
              if (i != j && isValidIndex(j)) {
                if (C.get(i, j)) {
                  // exclude unit since it is not comparable to root
                  reduceDomain(j, false, R.getRow(root));
                }
                if (Ct.get(i, j)) {
                  // ditto
                  reduceDomain(j, false, Rt.getRow(root));
                }
              }
            } 
          }
          indexMerge(i, root);
        }
      }
    }
    collect();
  }

  private void prepareConstraint() throws Unsatisfiable {
    collapseMinimal();
    BitMatrix leq = (BitMatrix)C.clone();
    leq.closure();
    computeArrows(leq);
    saturateAbs(leq);
    //condense(leq);
  }


  /***********************************************************************
   * Satisfiability
   ***********************************************************************/
  // enumerate the solutions on rigid variables (constructive witness)
  // or throw Unsatisfiable if no solution (right thing to do ???)
  // XXX: refine Unsatisfiable ??
  public void enumerate(LowlevelSolutionHandler handler)
  throws Unsatisfiable {
    S.assume(S.a&& hasBeenInitialized);
    prepareConstraint();
    domains.trimToSize();
    if (m == 0 || m == n) {
      // the constraint is satisfiable
      // if m == 0, all the variables are mapped to unit
      // if m == n, there is no variable
      // In both cases, there is still exactly one solution.
      handler.handle(domains);
    } else {
      // here goes real satisfiability
      int[] strategy = Satisfier.compileStrategy(C, Ct, m, n);
      Satisfier.enumerateSolutions(strategy, domains, C, Ct, R, Rt, m, n, handler);
    }
  }

  // XXX: spec ??
  public void enumerate(BitVector observers,
			LowlevelSolutionHandler handler) {
    S.assume(S.a&& hasBeenInitialized);
    try {
      prepareConstraint();
    } catch (Unsatisfiable e) {
      // not satisfiable
      return;
    }
    domains.trimToSize();
    if (m == 0 || m == n) {
      handler.handle(domains);
    } else {
      int[] strategy = Satisfier.compileStrategy(C, Ct, m, n);
      Satisfier.enumerateSolutions(strategy, domains,
                                   C, Ct, R, Rt, m, n,
                                   observers, handler);
    }
  }

  public void satisfy() throws Unsatisfiable {
    S.assume(S.a&& hasBeenInitialized);

    if (m == 0 || m == n)
      return;

    try {
      prepareConstraint();
      rawSatisfy();
    } catch (LowlevelUnsatisfiable e) {
      if (LowlevelUnsatisfiable.refinedReports) {
        throw refine(e);
      } else {
        throw e;
      }
    }
  }

  private void rawSatisfy() throws Unsatisfiable {
    domains.trimToSize();
    if (0 < m && m < n) {
      int[] strategy = Satisfier.compileStrategy(C, Ct, m, n);
      Satisfier.satisfy(strategy, domains, C, Ct, R, Rt, m, n);
    }
  }
  
  /***********************************************************************
   * Rigidification
   ***********************************************************************/
  /**
   * Saturate the subtyping between interfaces under :
   * I < J and J < K => I < K
   **/
  private void closeInterfaceRelation() {
    // Warshall algorithm
    for (int k = 0; k < nInterfaces(); k++) {
      Interface K = getInterface(k);
      for (int i = 0; i < nInterfaces(); i++) {
        Interface I = getInterface(i);
        if (I.subInterfaces.get(k)) {
          // K < I
          // for all J < K, add J < I
          I.subInterfaces.or(K.subInterfaces);
        }
      }
      // reflexivity
      K.subInterfaces.set(k);
    }
  }

  /**
   * Saturate the "implements" constraint under the following axioms:
   * x: I and I < J => x: J
   * x ~ y and y: I => x: I
   *
   * Returns an array of BitVector rigidImplementors
   * rigidImplementors[iid] is the set of x such that x :* iid
   **/
  BitVector[] closeImplements(BitMatrix R, BitMatrix Rt) {
    BitVector[] rigidImplementors = new BitVector[nInterfaces()];
    for (int iid = 0; iid < nInterfaces(); iid++) {
      Interface I = getInterface(iid);
      rigidImplementors[iid] = (BitVector)I.implementors.clone();
      for (int x = 0; x < n; x++) {
        if (I.implementors.get(x)) {
          // x: iid
          // for all y ~ x, add y: iid
          rigidImplementors[iid].orAnd(Rt.getRow(x),R.getRow(x));
        }
      }
    }
    for (int iid1 = 0; iid1 < nInterfaces(); iid1++) {
      for (int iid2 = 0; iid2 < nInterfaces(); iid2++) {
        if (getInterface(iid2).subInterfaces.get(iid1)) {
          // I1 < I2
          // for all x: I1, add x: I2
          rigidImplementors[iid2].or(rigidImplementors[iid1]);
        }
      }
    }
    return rigidImplementors;
  }
  

  /**
   * Rigidify the current constraint
   * You must have called satisfy before
   **/
  public void rigidify() {
    S.assume(S.a&& hasBeenInitialized);
    R = (BitMatrix)C.clone();
    R.closure();
    Rt = (BitMatrix)Ct.clone();
    Rt.closure();
    m = n;
    BitVector[] rigidImplementors  = closeImplements(R, Rt);
    for (int iid = 0; iid < nInterfaces(); iid++) {
      getInterface(iid).rigidImplementors = rigidImplementors[iid];
    }
    domains = new DomainVector(m, m);
  }

  /***********************************************************************
   * Marking / Backtracking
   ***********************************************************************/
  // There are two modes of backtracking: BACKTRACK_UNLIMITED and
  // BACKTRACK_ONCE
  //
  // 1. BACKTRACK_ONCE: backtrack() may be called to undo all modifications
  // made to the constraint since the last call to mark() (time t1). The
  // constraint may then be modified and another call to backtrack still
  // restores the constraint to the state at time t1. Or the constraint may be
  // modified, then mark() called (at time t2). Any subsequent call to
  // backtrack restores to t2, unless mark() is called another time. This is
  // the default mode adapted to type inference, used by the Jazz compiler.
  //
  // 2. BACKTRACK_UNLIMITED: calls to mark and backtrack may be nested: a call
  // to backtrack undoes all the modification made to this constraint since
  // the last call to mark() in the same nested level. It is an error if there
  // are more calls to backtrack() than to mark(). This mode is adapted to
  // type checking and used by the Nice compiler.
  private int backtrackMode;
  final public static int BACKTRACK_UNLIMITED=1;
  final public static int BACKTRACK_ONCE=2;
  
  private class Backup {
    // may be non-null if backtrackMode==BACKTRACK_UNLIMITED    
    Backup previous;
    int savedM;
    int savedN;
    BitVector savedGarbage;
    DomainVector savedDomains;
    //BitVector[] savedImplementors;

    private Backup() {
      if (K0.this.backtrackMode == K0.BACKTRACK_UNLIMITED) {
        this.previous = K0.this.backup;
      } else {
        this.previous = null;
      }
      this.savedM = K0.this.m;
      this.savedN = K0.this.n;
      
      this.savedGarbage = (BitVector)K0.this.garbage.clone();
      this.savedDomains = (DomainVector)K0.this.domains.clone();
      /*
      this.savedImplementors = new BitVector[K0.this.nInterfaces()];
      for (int iid = 0; iid < savedImplementors.length; iid++) {
        savedImplementors[iid]
          = (BitVector)K0.this.getInterface(iid).implementors.clone();
      }
      */
    }
  }

  private Backup backup = null;

  public void mark() {
    S.assume(S.a&& hasBeenInitialized);

    backup = new Backup();
  }

  // backtrack to the situation the last time mark() has been called
  public void backtrack() {
    S.assume(S.a&& hasBeenInitialized);
    S.assume(S.a&& backup != null, "backtrack() without corresponding mark()");

    this.m = backup.savedM;
    this.R.setSize(m);
    this.Rt.setSize(m);

    this.n = backup.savedN;
    this.C.setSize(n);
    this.Ct.setSize(n);

    this.garbage = backup.savedGarbage;
    this.domains = backup.savedDomains;
    this.minimal.truncate(n);
    
    for (int iid = 0; iid < nInterfaces(); iid++) {
      Interface I = getInterface(iid);
      I.setIndexSize(m);
      //I.implementors = backup.savedImplementors[iid];
    }
    
    clearTags();
    if (backtrackMode == BACKTRACK_UNLIMITED) {
      backup = backup.previous;
    } else {
      backup = null;
      mark();
    }
    if (debugK0) {
      System.err.println("backtracked #" + ID);
    }
  }

  /***********************************************************************
   * Iterate thru the constraint
   ***********************************************************************/
  public static final int 
    ALL = 0,
    SIMPLIFIED = 1;
  
  public static abstract class IneqIterator {
    public IneqIterator()
    { this.range1 = this.range2 = ALL; }
    public IneqIterator(int range1, int range2)
    { this.range1 = range1; this.range2 = range2; }
    int range1, range2;
    
    abstract protected void iter(int x1, int x2) throws Unsatisfiable;
  }
  public void ineqIter(IneqIterator iterator) throws Unsatisfiable {
    int from1, from2, to1=n, to2=n;
    if (iterator.range1 == SIMPLIFIED)
      from1 = weakMarkedSize;
    else
      from1 = 0;
    if (iterator.range2 == SIMPLIFIED)
      from2 = weakMarkedSize;
    else
      from2 = 0;
	
    for (int i = from1; i < to1; i++) {
      if (!garbage.get(i)) {
        for (int j = from2; j < to2; j++) {
          if (!garbage.get(j)) {
            if (C.get(i, j)) {
              iterator.iter(i, j);
            }
          }
        }
      }
    }
  }

  public static abstract class IndexIterator {
    abstract protected void iter(int x);
  }
  public void indexIter(IndexIterator iterator) {
    for (int i = 0; i < n; i++) {
      if (!garbage.get(i)) {
        iterator.iter(i);
      }
    }
  }

  public static abstract class ImplementsIterator {
    abstract protected void iter(int x, int iid) throws Unsatisfiable;
  }
  public void implementsIter(ImplementsIterator iterator) throws Unsatisfiable {
    for (int iid = 0; iid < nInterfaces(); iid++) {
      BitVector implementors = getInterface(iid).implementors;
      for (int x = implementors.getLowestSetBit();
           x != BitVector.UNDEFINED_INDEX;
           x = implementors.getNextBit(x)) {
        if (!garbage.get(x)) {
          iterator.iter(x, iid);
        }
      }
    }
  }

  public static abstract class AbstractsIterator {
    abstract protected void iter(int x, int iid) throws Unsatisfiable;
  }
  public void abstractsIter(AbstractsIterator iterator) throws Unsatisfiable {
    for (int iid = 0; iid < nInterfaces(); iid++) {
      BitVector abstractors = getInterface(iid).abstractors;
      for (int x = abstractors.getLowestSetBit();
           x != BitVector.UNDEFINED_INDEX;
           x = abstractors.getNextBit(x)) {
        if (!garbage.get(x)) {
          iterator.iter(x, iid);
        }
      }
    }
  }

  static abstract public class IndexSelector {
    abstract protected boolean select(int index);
  }

  /***********************************************************************
   * Weak marking / Backtracking (only keeps track of the sizes)
   * NB: calls to mark/backtrack and weakMark/weakBacktrack mustn't
   * be interleaved
   ***********************************************************************/
  // is it really useful ? we'd better do a robust optimized version of
  // mark/backtrack that is lazy to avoid unnecessary copies... 
  private int weakMarkedSize = -1;
  private int weakMarkedM = -1;
  public void weakMark() {
    S.assume(S.a&& weakMarkedSize == -1);
    weakMarkedSize = n;
    weakMarkedM = m;
    if (debugK0) {
      System.err.println("weakMark'ed");
    }
  }
  public void weakBacktrack() {
    S.assume(S.a&& weakMarkedSize >= 0);
    R.setSize(weakMarkedM);
    Rt.setSize(weakMarkedM);
    for (int iid = 0; iid < nInterfaces(); iid++) {
      getInterface(iid).rigidImplementors.truncate(weakMarkedM);
    }
    int nMin = weakMarkedSize;
    int nMax = n;
    setSize(weakMarkedSize);
    weakMarkedSize = -1;
    weakMarkedM = -1;
    /*
      Alex did that, but it is not done for real backtracks.
      It is not convenient for simplification (and a waste of time).

    for (int i = nMin; i < nMax; i++) {
      callbacks.indexDiscarded(i);
    }
    */
    if (debugK0) {
      System.err.println("weakBacktrack'ed");
    }
  }

  int weakMarkedSize()
  {
    return weakMarkedSize;
  }
  
  /***********************************************************************
   * Simplification
   ***********************************************************************/
  // XXX: should be entirely worked out...
  // XXX: comment utiliser mark/backtrack avec recopie lazy ??

  // the size of the constraint when startSimplify() has been called
  public void startSimplify() {
    S.assume(S.a&& hasBeenInitialized);
    weakMark();

    // necessary ?
    posTagged.fill(n);
    negTagged.fill(n);
  }
  public void stopSimplify() {
    S.assume(S.a&& hasBeenInitialized);
    weakBacktrack();
  }

  BitVector negTagged;
  BitVector posTagged;

  public boolean posTagged(int i) {
    return posTagged.get(i);
  }
  public boolean negTagged(int i) {
    return negTagged.get(i);
  }
  public void clearTags() {
    posTagged.clearAll();
    negTagged.clearAll();
  }
  
  public void tag(int i, int variance) {
    if (variance >= 0) {
      posTagged.set(i);
    }
    if (variance <= 0) {
      negTagged.set(i);
    }
  }

  // simplify all the variables introduced since last weakMark()
  public void simplify() {
    S.assume(S.a&& hasBeenInitialized);

    if (weakMarkedSize >= 0) {
      if (weakMarkedSize == n) {
        return;
      }
      if (S.debugSimpl) {
        System.err.println("SIMPL: start simplification");
      }
      BitVector simplified = new BitVector(n);
      simplified.fill(n);
      simplified.fillNot(weakMarkedSize);
      (new Simplifier(simplified)).simplify();
    } else {
      throw S.panic();
    }
  }
  public void simplify(IndexSelector selector) {
    S.assume(S.a&& hasBeenInitialized);
    BitVector simplified = new BitVector(n);
    boolean empty = true;
    for (int i = 0; i < n; i++) {
      if (selector.select(i)) {
        simplified.set(i);
        empty = false;
      }
    }
    if (!empty) {
      (new Simplifier(simplified)).simplify();
    }
  }

  public boolean isBeingSimplified(int i) {
    return i >= weakMarkedSize;
  }

  private final class Simplifier {
    Simplifier(BitVector simplified) {
      this.simplified = simplified;
      this.initN = simplified.getLowestSetBit();
      R = (BitMatrix)C.clone();
      R.closure();
      Rt = (BitMatrix)Ct.clone();
      Rt.closure();

      implementors = closeImplements(R, Rt);
      
      Sdomains = new DomainVector(initN, n, n - initN);
      for (int i = initN; i < n; i++) {
        if (!simplified.get(i)) {
          Sdomains.clear(i);
        }
      }
    }
    
    int initN;                  // don't simplify below this index
    BitVector simplified;       // the vector of indexes being simplified

    // closed constraint
    BitMatrix R;
    BitMatrix Rt;
    BitVector[] implementors;

    DomainVector Sdomains;

    private void computeInitialDomains() {
      if (S.debugSimpl) {
        System.err.println("SIMPL: computing initial domains");
      }
      // 
      // Compute the initial value of D(x) for all simplified variable x
      //
      for (int x = initN; x < n; x++) {
        Domain dx = Sdomains.getDomain(x);
        if (dx != null) {
          // now, we know that simplified.get(x) is true
          
          // can't simplify rigid variable of the initial context
          S.assume(S.a&& x >= m0);
          BitVector ux = R.getRow(x);
          if (ux != null) {
            for (int x0 = ux.getLowestSetBit();
                 x0 != BitVector.UNDEFINED_INDEX;
                 x0 = ux.getNextBit(x0)) {
              if (!simplified.get(x0)) {
                // we have x <C x0
                // sigma(x0) = x0 for all auto-solution
                // do D(x) = D(x) \inter \Lower{x0}
                dx.and(Rt.getRow(x0));
                // and exclude unit since unit is not comparable to x0
                dx.rawExcludeUnit();
              }
            }
          }
          BitVector lx = Rt.getRow(x);
          if (lx != null) {
            for (int x0 = lx.getLowestSetBit();
                 x0 != BitVector.UNDEFINED_INDEX;
                 x0 = lx.getNextBit(x0)) {
              if (!simplified.get(x0)) {
                // x0 <C x
                // do D(x) = D(x) \inter \Upper{x0}
                dx.and(R.getRow(x0));
                dx.rawExcludeUnit();
              }
            }
          }
          for (int iid = 0; iid < nInterfaces(); iid++) {
            if (implementors[iid].get(x)) {
              // we have x: I
              // ("implements" relation has been saturated)
              dx.and(implementors[iid]);
              // don't exclude unit !!
            }
          }
          
          if (posTagged(x)) {
            // We must have sigma(x) <= x
            // Do D(x) = D(x) \inter \Lower{x}
            dx.and(Rt.getRow(x));
            dx.rawExcludeUnit();
          }
          if (negTagged(x)) {
            // We must have sigma(x) >= x
            dx.and(R.getRow(x));
            dx.rawExcludeUnit();
          }
        }
      }
      if (S.debugSimpl) {
        System.err.println("SIMPL: initial domains are : " + domainsToString());
      }
    }

    /**
     * Fills (solution, nker) with a solution of C w.r.t R.
     * After the call, for initN <= x < n, solution[x - initN] = sigma(x)
     * and sigma(x) \in domains(x).
     **/
    private void findSolution(int[] strategy,
                              final int[] solution)
    throws LowlevelUnsatisfiable {
      final RuntimeException abort = new RuntimeException();
      try {
        Satisfier.enumerateSolutions
          (strategy, Sdomains, C, Ct, R, Rt, initN, n,
           new LowlevelSolutionHandler() {
            protected void handle() {
              for (int x = initN; x < n; x++) {
                if (Sdomains.getDomain(x) != null) {
                  solution[x - initN] = getSolutionOf(x);
                } else {
                  solution[x - initN] = x;
                }
              }
              throw abort;
            }
          });
      } catch (RuntimeException e) {
        if (e != abort) {
          throw e;
        }
      }
    }

    private class Normal extends Exception {}
    /**
     * Fills solution with a non-surjective solution or throws Normal.
     * Starts to exclude elements downwards from excludedA.
     *
     * @exception Normal if the constraint is in normal form
     **/
    private int findNonSurjective(int excludedA,
                                  int[] strategy, int[] solution)
    throws Normal {
      while (excludedA >= initN) {
        if (!garbage.get(excludedA) && simplified.get(excludedA)) {
          int x;
          DomainVector savedDomains = (DomainVector)Sdomains.clone();
          try {
            if (S.debugSimpl) {
              System.err.println("Try excluding " + excludedA);
            }
            Sdomains.exclude(excludedA);
            if (S.debugSimpl) {
              System.err.println("Satisfying with " + Sdomains.dump());
            }
            findSolution(strategy, solution);
            return excludedA;
          }
          catch (LowlevelUnsatisfiable e) {
            if (S.debugSimpl) {
              System.err.println("Failed");
            }
            // try another value
          }
          finally {
            Sdomains = savedDomains;
          }
        }
        excludedA--;
      }
      throw new Normal();
    }

    /**
     * assume solution contains a solution of C |= C.
     * Do one stroke of simplification on x.
     **/
    private void simplifyIndex(int x, int[] solution, int[] nker) {
      if (S.debugSimpl) {
        System.err.println("x = " + x + " nker[x - initN] = " + nker[x - initN] + " garbage.get(x) = " + garbage.get(x));
      }
      while (x >= initN && simplified.get(x) && nker[x - initN] == 0) {
        // OK, x is not in the codomain of sigma, we can eliminate it

        if (S.debugSimpl) {
          System.err.println("Eliminate " + x);
        }
        try {
          //
          // Propagate the constraints in (C, Ct) that go through x
          //
          // XXX: is it inconvenient that it can modify the context ?
          // XXX: normally not: it only adds consequences of the context...
          for (int y = 0; y < n; y++) {
            if (!garbage.get(y)) {
              if (x != y && C.get(y, x)) {
                // y < x
                // for each x < z, add y < z
                BitVector ux = C.getRow(x);
                if (ux != null) {
                  // C.getRow(y) is necessarily non-null since C.get(y, x) is
                  // true
                  C.getRow(y).or(ux);
                }
              }
              if (x != y && Ct.get(y, x)) {
                // x < y
                // add z < y for each z < x
                BitVector lx = Ct.getRow(x);
                if (lx != null) {
                  // Ct.getRow(y) is necessarily non-null because Ct.get(y, x)
                  // is true
                  Ct.getRow(y).or(lx);
                }
              }
            }
          }

          // should clear C Ct ?
          Sdomains.clear(x);
          Sdomains.exclude(x);
	  domains.clear(x); // Added by Daniel
          simplified.clear(x);
          garbage.set(x);
          int sx = solution[x - initN];
          if (sx >= initN) {
            nker[sx - initN]--;
          }
          if (posTagged(x) || negTagged(x)) {
            // sx can't be -1 because unit is not comparable with x
            S.assume(S.a&& sx >= 0);
            // discard x by merging with sx
            if (S.debugSimpl) {
              System.err.println("Merge " + x + " and " + sx);
            }
            if (simplified.get(sx)) {
              if (posTagged(x) && !posTagged(sx)) {
                // we now have found that sx is tagged +
                // exclude unit, not comparable to sx
                Sdomains.reduce(sx, false, Rt.getRow(sx));
                posTagged.set(sx);
              }
              if (negTagged(x) && !negTagged(sx)) {
                Sdomains.reduce(sx, false, R.getRow(sx));
                negTagged.set(sx);
              }
            }
            callbacks.indexMerged(x, sx);
          } else {
            // just discard x
            callbacks.indexDiscarded(x);
          }
          // iterate sigma until its codomain is stable
          x = sx;
        } catch (Unsatisfiable e) {
          throw S.panic();
        }
      }
    }

    private void simplifyOnce(int[] solution) {
      int[] nker = new int[n - initN];
      for (int x = initN; x < n; x++) {
        if (!garbage.get(x)) {
          int sx = solution[x - initN];
          if (sx >= initN) {
            nker[sx - initN]++;
          }
        }
      }
      for (int x = initN; x < n; x++) {
        simplifyIndex(x, solution, nker);
      }
    }

    String indexToString(int index) {
      return K0.this.indexToString(index) +
        (posTagged(index) ? "+" : "") +
        (negTagged(index) ? "-" : "");
    }

    private void reduce(BitMatrix C, BitMatrix R, BitMatrix Rt) {
      BitVector garbage = K0.this.garbage;
      if (S.debugSimpl) {
        System.err.println("reduce C = " + C + ", R = " + R + ", garbage = " + garbage);
      }
      for (int k = 0; k < n; k++) {
        if (!garbage.get(k)) {
          C.clear(k, k);
          // uk is necessarily non-null (at least k <* k)
          BitVector uk = R.getRow(k);
          BitVector lk = Rt.getRow(k);
          for (int j = 0; j < n; j++) {
            if (!garbage.get(j) && lk.get(j) && !uk.get(j)) {
              // j <* k and not k <* j
              BitVector uj = C.getRow(j);
              if (uj != null) {
                if (simplified.get(j)) {
                  // delete all j < l when k <* l but not l <* k
                  uj.andNotOr(uk, lk);
                } else {
                  // same but only if l is simplified
                  uj.andNotAndOr(simplified, uk, lk);
                }
              }
            }
          }
        }
      }
      if (S.debugSimpl) {
        System.err.println("reduced C = " + C + ", R = " + R + ", garbage = " + garbage);
      }
    }

    // transitive reduction of the relation x: I
    private void reduceImplements() {
      for (int iid = 0; iid < nInterfaces(); iid++) {
        Interface I = getInterface(iid);
        BitVector lI = implementors[iid]; 
        BitVector Iimplementors = I.implementors;
        for (int k = lI.getLowestSetBit();
             k != BitVector.UNDEFINED_INDEX;
             k = lI.getNextBit(k)) {
          if (!garbage.get(k)) {
            // k:* I
            // for all j in simplified, delete j: I if j <* k strictly
            Iimplementors.andNotAndOr
              (simplified,      // leave the bits outside simplified unchanged
               Rt.getRow(k),    // if j <* k
               R.getRow(k));    // and !(k <* j), then delete j: I
          }
        }
      }
      for (int iid2 = 0; iid2 < nInterfaces(); iid2++) {
        Interface I2 = getInterface(iid2);
        BitVector I2sub = I2.subInterfaces;
        BitVector I2implementors = I2.implementors;
        for (int iid1 = I2sub.getLowestSetBit();
             iid1 != BitVector.UNDEFINED_INDEX;
             iid1 = I2sub.getNextBit(iid1)) {
          Interface I1 = getInterface(iid1);
          if (!I1.subInterfaces.get(iid2)) {
            //  I1 <* I2 strictly
            // delete all j: I2 when j:* I1 and j is simplified
            I2implementors.andNotAnd(implementors[iid1], simplified);
          }            
        }
      }
    }
    
    /**
     * Transitive reduction.
     **/
    void reduce() {
      if (S.debugSimpl) {
        System.err.println("starting reduction: K = " + K0.this);
      }
      reduce(C, R, Rt);
      reduce(Ct, Rt, R);
      reduceImplements();
      if (S.debugSimpl) {
        System.err.println("finished reduction: K = " + K0.this);
      }
    }
    
    void simplify() {
      if (initN < 0) {
	// no index to simplify
        return;
      }
      computeInitialDomains();
      try {
        int[] solution = new int[n - initN];
        int[] strategy = Satisfier.compileStrategy(C, Ct, initN, n);
        int excludedA = n - 1;
        while (true) {
	  if (n - garbage.bitCount() == 1) {
	    // only one variable (including rigid ones)
	    int x = garbage.getLowestClearedBit();
	    if (simplified.get(x) && !posTagged(x) && !negTagged(x)) {
	      // It is not even tagged
	      // eliminate it and make the constraint empty
	      setSize(0);
	      callbacks.indexDiscarded(x);
	      return;
	    } else {
	      // keep it, but the constraint is now in normal form...
	      if (x != 0) {
                indexMove(x, 0);
              }
	      setSize(1);
	      C.clear(0, 0);
	      Ct.clear(0, 0);
	      return;
	    }
	  }
          if (S.debugSimpl) {
            System.err.println("Try to simplify " + K0.this.toString());
            System.err.println("with domains " + Sdomains.dump());
          }
          excludedA = findNonSurjective(excludedA, strategy, solution);
          if (S.debugSimpl) {
            System.err.print(" sigma = {");
            Separator sep = new Separator(", ");
            for (int x = initN; x < n; x++) {
              if (Sdomains.getDomain(x) != null) {
                System.err.print(sep);
                System.err.print(indexToString(x));
                System.err.print(" -> ");
                int sx = solution[x-initN];
                if (sx == -1) {
                  System.err.print("unit");
                } else {
                  System.err.print(indexToString(sx));
                }
              }
            }
            System.err.println("}");
          }
          simplifyOnce(solution);
        }
      } catch (Normal e) {
        reduce();
        collect();
      }
    }
  } // end of inner class Simplify

  public void deleteAllSoft() {
    S.assume(S.a&& hasBeenInitialized);
    setSize(m);
    n = m;
  }

  /**
   * Assume i1 and i2 are rigid
   **/
  public boolean isLeq(int i1, int i2) {
    return R.get(i1, i2);
  }

  /**
   * Test if a constraint i1 <: i2 was explicitely entered.
   **/
  public boolean wasEntered(int i1, int i2) {
    return C.get(i1, i2);
  }

  /***********************************************************************
   * Overloading resolution
   ***********************************************************************/
  private final static int OVERLOADING_CONSTRUCTOR = 0;
  private final static int OVERLOADING_INTERFACE = 1;
  /**
   * Assume this constraint has just been satisfied possibilities contains
   * indexes of rigid variables (if what == OVERLOADING_CONSTRUCTOR) or
   * interfaces (if what == OVERLOADING_INTERFACE). For each index k in
   * possibilities, try to enter constraint x <= k or x: k and delete k from
   * possibilities if this makes the constraint unsatisfiable
   **/
  private void solveOverloading(int what, int x, BitVector possibilities) {
    S.assume(S.a&& hasBeenInitialized);
    S.assume(S.a&& isValidIndex(x));
    if (isRigid(x)) {
      if (what == OVERLOADING_CONSTRUCTOR) {
        // R.getRow(x) is Up(x)
        possibilities.and(R.getRow(x));
      } else {
        for (int iid = possibilities.getLowestSetBit();
             iid != BitVector.UNDEFINED_INDEX;
             iid = possibilities.getNextBit(iid)) {
          if (!getInterface(iid).rigidImplementors.get(x)) {
            possibilities.clear(iid);
          }
        }
      }
    } else {
      // x is soft
      for (int x0 = possibilities.getLowestSetBit();
           x0 != BitVector.UNDEFINED_INDEX;
           x0 = possibilities.getNextBit(x0)) {
        // try simple things first
        if (what == OVERLOADING_CONSTRUCTOR
            &&
            !domains.getDomain(x).intersect(Rt.getRow(x0))) {
          possibilities.clear(x0);
        } else if
          (what == OVERLOADING_INTERFACE
           &&
           !domains.getDomain(x).containsUnit()
           &&
           !domains.getDomain(x)
           .intersect(getInterface(x0).rigidImplementors)) {
          possibilities.clear(x0);
        } else {
          // try to really satisfy
          Backup savedBackup = backup;
          mark();
          try {
            if (what == OVERLOADING_CONSTRUCTOR) {
              leq0(x, x0);
            } else {
              indexImplements0(x, x0);
            }
            rawSatisfy();
          } catch (Unsatisfiable e) {
            possibilities.clear(x0);
          } finally {
            backtrack();
            backup = savedBackup;
          }
        }
      }
    }
  }
  
  public void solveConstructorOverloading(int x, BitVector possibilities) {
    solveOverloading(OVERLOADING_CONSTRUCTOR, x, possibilities);
  }
  public void solveInterfaceOverloading(int x, BitVector possibilities) {
    solveOverloading(OVERLOADING_INTERFACE, x, possibilities);
  }
}
