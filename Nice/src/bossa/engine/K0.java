package bossa.engine;

//import fr.ensmp.cma.jazz.typing.Unsatisfiable;
//import fr.ensmp.cma.jazz.util.*;
//import fr.ensmp.cma.jazz.S;

/**
 * A lowlevel constraint on integers.
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
abstract public class K0 implements Cloneable {
  public K0() {
    this.R = null;
    this.Rt = null;
    this.m = 0;
    this.C = new BitMatrix();
    this.Ct = new BitMatrix();
    this.n = 0;
    this.minimal = new BitVector();
    this.maximal = new BitVector();
    this.garbage = new BitVector();
    this.posTagged = new BitVector();
    this.negTagged = new BitVector();
    this.virtual = new BitVector();
  }
  
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
   * Returns an index that is garanteed to be greater than any rigid index and
   * less or equal to any non-rigid index.
   **/
  final public int firstNonRigid() {
    return m;
  }
  
  /**
   * Returns true if x is the index of a rigid variable.
   * the index is assumed valid
   **/
  final public boolean isRigid(int x) {
    return x < m;
  }

  final public boolean hasNoSoft() {
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
   **/
  private int n;

  public int size() {
    return n;
  }
  
  /**
   * Maintains the constraints "x is minimal"
   **/
  private BitVector minimal;
  // assume x is valid
  public boolean isMinimal(int x) {
    return minimal.get(x);
  }

  private BitVector maximal;
  public boolean isMaximal(int x) {
    return maximal.get(x);
  }

  // tells if an index should be collected in the next call to collect()
  private BitVector garbage;

  final public boolean isValidIndex(int x) {
    return x >= 0 && x < n && !garbage.get(x);
  }

  // tells if an index is virtual. Virtual indexes are used to code
  // interfaces. They cannot be solutions of constraints
  private BitVector virtual;
  /**
   * Assume x is a valid index
   **/
  final public boolean isVirtual(int x) {
    return virtual.get(x);
  }
    
  private void setSize(int n) {
    this.n = n;
    C.setSize(n);
    Ct.setSize(n);
    domains.setSize(n - m);
    garbage.truncate(n);
    minimal.truncate(n);
    maximal.truncate(n);
    posTagged.truncate(n);
    negTagged.truncate(n);
    virtual.truncate(n);
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
  private void collect() {
    // first collect the rigid indexes (shouldn't be necessary actually but
    // let's be general !)
    if (m > 0) {
      m = collect(0, m);
      R.setSize(m);
      Rt.setSize(m);
    }

    setSize(collect(m, n));
  }      

  // assume src > dest && !garbage.get(src) && garbage.get(dest)
  private void indexMove(int src, int dest) {
    domainMove(src, dest);
    minimal.bitCopy(src, dest);
    maximal.bitCopy(src, dest);
    posTagged.bitCopy(src, dest);
    negTagged.bitCopy(src, dest);
    virtual.bitCopy(src, dest);
    garbage.set(src);
    garbage.clear(dest);
    C.rowMove(src, dest);
    C.colMove(src, dest);
    Ct.rowMove(src, dest);
    Ct.colMove(src, dest);

    if (isRigid(src)) {
      // strange as the relation on the rigid indexes should already be
      // condensed... but let's be general
      R.rowMove(src, dest);
      R.colMove(src, dest);
      Rt.rowMove(src, dest);
      Rt.colMove(src, dest);
    }
    
    // tell the subclass
    indexMoved(src, dest);
  }
  
  // assume src > dest && !garbage.get(src) && !garbage.get(dest)
  private void indexMerge(int src, int dest) throws Unsatisfiable {
    domainMerge(src, dest);
    C.rowMerge(src, dest);
    C.colMerge(src, dest);
    Ct.rowMerge(src, dest);
    Ct.colMerge(src, dest);
    garbage.set(src);
    minimal.bitMerge(src, dest);
    maximal.bitMerge(src, dest);
    // cannot merge a virtual and a non-virtual index
    S.assert(S.a&& ((isVirtual(src) && isVirtual(dest))
                    || (!isVirtual(src) && !isVirtual(dest))));
    posTagged.bitMerge(src, dest);
    negTagged.bitMerge(src, dest);
    if (isRigid(src)) {
      // strange as the relation on the rigid indexes should already be
      // condensed... but let's be general
      R.rowMerge(src, dest);
      R.colMerge(src, dest);
      Rt.rowMerge(src, dest);
      Rt.colMerge(src, dest);
    }
    // tell the subclass
    indexMerged(src, dest);
  }
    
  /**
   * Called when indexes src and dest have been merged.
   * Only dest remains valid: src is invalidated
   **/
  abstract protected void indexMerged(int src, int dest) throws Unsatisfiable;

  /**
   * Called when index src has been moved to index dest.
   **/
  abstract protected void indexMoved(int src, int dest);
  

  /**
   * Called when simplification discards index
   **/
  abstract protected void indexDiscarded(int index);
  
  /**
   * Called when the constraint is resized
   */
  protected void onResize(int newSize)
  {
  }
  
  /**
   * Add a new variable to the constraint and returns its index
   **/
  final public int extend() {
    try {
      int result = extend0();
      domains.exclude(result, virtual);
      onResize(result+1);
      return result;
    } catch (LowlevelUnsatisfiable e) {
      throw S.panic();
    }
  }
  //To remove
  //Everything with 'virtual' is a hack for old interfaces
  final public int extendVirtual() {
    try {
      int result = extend0();
      virtual.set(result);
      domains.reduce(result, virtual);
      return result;
    } catch (LowlevelUnsatisfiable e) {
      throw S.panic();
    }
  }
  private int extend0() {
    C.extend();
    Ct.extend();
    garbage.clear(n);
    domains.extend();
    return n++;
  }
  /**
   * Enter the constraint x1 = x2
   * Assume x1 and x2 are valid indexes
   **/
  final public void eq(int x1, int x2) throws Unsatisfiable {
    leq(x1, x2);
    leq(x2, x1);
  }

  /**
   * Enter the constraint x1 <= x2.
   * Assume x1 and x2 are valid indexes.
   **/
  public void leq(int x1, int x2) throws Unsatisfiable {
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

  /**
   * Enter the constraint Min(x)
   * Assume x is a valid index
   **/
  final public void minimal(int x) throws Unsatisfiable {
    extremal(x, MINIMAL);
  }
  final public void maximal(int x) throws Unsatisfiable {
    extremal(x, MAXIMAL);
  }
  final private static int MINIMAL = 0;
  final private static int MAXIMAL = 1;
  private void extremal(int x, int what) throws Unsatisfiable {
    if (LowlevelUnsatisfiable.refinedReports) {
      try {
        extremal0(x, what);
      } catch (LowlevelUnsatisfiable e) {
        throw refine(e);
      }
    } else {
      extremal0(x, what);
    }
  }

  static boolean debugK0 = S.getBoolean(false, "debug.k0");
  
  // versions that does not make any effort to report precise errors
  // in case of rigid clash
  private void leq0(int x1, int x2) throws LowlevelUnsatisfiable {
    if (debugK0) {
      S.dbg.println(x1 + " <: " + x2);
    }
    if (x1 == x2) { return; }
    if (C.get(x1, x2)) { return; }
    if (isRigid(x1) && isRigid(x2)) {
      if (!R.get(x1, x2)) {
        throw new LowlevelRigidClash(x1, x2);
      } else {
        return;
      }
    }
    if (debugK0) {
      S.dbg.println("C.set(" + x1 + ", " + x2 + ")");
    }
    C.set(x1, x2);
    Ct.set(x2, x1);
    if (isRigid(x1)) {
      // !isRigid(x2)
      domainIntersect(x2, R.getRow(x1));
    }
    if (isRigid(x2)) {
      // !isRigid(x1)
      domainIntersect(x1, Rt.getRow(x2));
    }
    if (isMinimal(x2) && !isVirtual(x1)) {
      extremal0(x1, MINIMAL);
      leq0(x2, x1);
    }
    if (isMaximal(x1) && !isVirtual(x2)) {
      extremal0(x2, MAXIMAL);
      leq0(x2, x1);
    }
  }
  

  private void extremal0(int x, int what) throws LowlevelUnsatisfiable {
    S.assert(S.a&& !isVirtual(x));
    if (debugK0) {
      S.dbg.println(x + " is extemal " + what);
    }
    if (   (what == MINIMAL && !(isMinimal(x)))
        || (what == MAXIMAL && !(isMaximal(x)))) {
      if (debugK0) {
        S.dbg.println(x + " was not extremal before");
      }
      if (isRigid(x)) {
        throw new LowlevelUnsatisfiable("");
      } else {
        switch(what) {
        case MINIMAL: minimal.set(x); domains.reduce(x, minimal); break;
        case MAXIMAL: maximal.set(x); domains.reduce(x, maximal); break;
        default: throw S.panic();
        }
        // walk through all the y such that y <_what x
        BitVector lx = (what == MINIMAL ? Ct.getRow(x) : C.getRow(x)) ;
        if (lx != null) {
          int y = -1;
          while ((y = lx.getLowestSetBit(++y)) >= 0) {
            if (!virtual.get(y)) {
              if (debugK0) {
                S.dbg.println(" found " + y + " << " + x);
              }
              extremal0(y, what);
              // merge x and y
              switch (what) {
              case MINIMAL: leq0(x, y); break;
              case MAXIMAL: leq0(y, x); break;
              }
            }
          }
        }
      }
    }
  }
  

  // try to refine the exception e, in case of rigid clashes
  // the constraint is known to be unsatisfiable at this point
  private LowlevelUnsatisfiable refine(LowlevelUnsatisfiable e) {
    if (K0.debugK0) {
      S.dbg.println("Trying to refine " + e);
    }
    if (e instanceof LowlevelRigidClash) {
      // nothing to refine
      return e;
    } else {
      C.closure();
      int[] rigidClash = C.includedIn(m, R);
      if (rigidClash != null) {
        return new LowlevelRigidClash(rigidClash[0], rigidClash[1]);
      } else {
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
        Ct.closure();
        clash
          = refineIncompatible(Ct, Rt,
                               LowlevelIncompatibleClash.NO_COMMON_SUBTYPE);
        if (clash != null) {
          return clash;
        }

        // At this point, we failed to give a better error message
        // than "constraint is unsatisfiable"...
        return e;
      }
    }
  }

  private LowlevelIncompatibleClash refineIncompatible(BitMatrix C,
                                                       BitMatrix R,
                                                       int what) {
    for (int a = 0; a < m; a++) {
      for (int b = a + 1; b < m; b++) {
        if (R.getRow(a).getLowestSetBitAndNotIn(R.getRow(b), virtual) < 0) {
          // R(a) and R(b) do not intersect
          BitVector Ca = C.getRow(a);
          if (Ca != null) {
            BitVector Cb = C.getRow(b);
            if (Cb != null) {
              int z = Ca.getLowestSetBitAndNotIn(Cb, virtual);
              if (z >= 0) {
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
   * Constraint preparation
   ***********************************************************************/

  /**
   * Condense the relation by merging equivalent indexes and collecting the
   * garbage nodes.
   **/
  public void condense() throws Unsatisfiable {
    return ;
    
    // XXX: TODO: verify safetyness when indexMerged creates new nodes
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
//      BitMatrix T = (BitMatrix)C.clone();
//      T.closure();

//      if (m > 0) {
//        int[] rigidClash = T.includedIn(m, R);
//        if (rigidClash != null) {
//          throw new LowlevelRigidClash(rigidClash[0], rigidClash[1]);
//        }
//      }
//      BitMatrix Tt = (BitMatrix)Ct.clone();
//      Tt.closure();
//      for (int i = 0; i < n; i++) {
//        int root = T.getRow(i).getLowestSetBitAnd(Tt.getRow(i));
//        if (root != i) {
//          if (isRigid(root) && !isRigid(i)) {
//            // domains of variables comparable to i is incorrect after
//            // the merge, so recompute them
//            for (int j = m; j < n; j++) {
//              if (i != j && isValidIndex(j)) {
//                if (C.get(i, j)) {
//                  domainIntersect(j, R.getRow(root));
//                }
//                if (Ct.get(i, j)) {
//                  domainIntersect(j, Rt.getRow(root));
//                }
//              }
//            } 
//          }
//          indexMerge(i, root);
//        }
//      }
//      collect();
  }

  /***********************************************************************
   * Domains management
   ***********************************************************************/
  public DomainVector domains = new DomainVector(0, 0);
  private void domainIntersect(int x, BitVector constraint)
  throws LowlevelUnsatisfiable {
    domains.reduce(x, constraint);
  }
  // merge domain src and dest, put the result in dest and discard src
  // if the result becomes empty, it means that the constraint is
  // unsatisfiable
  private void domainMerge(int src, int dest) throws Unsatisfiable {
    domains.merge(src, dest);
  }
  private void domainMove(int src, int dest) {
    domains.move(src, dest);
  }

  /***********************************************************************
   * satisfiability
   ***********************************************************************/
  final public void enumerate(LowlevelSolutionHandler handler)
  throws Unsatisfiable {
    condense();
    domains.trimToSize();
    if (0 < m && m < n) {
      int[] strategy = Satisfier.compileStrategy(C, Ct, m, n);
      Satisfier.enumerateSolutions(strategy, domains, C, Ct, R, Rt, m, n, handler);
    }
  }

  final public void reduceDomain(int x, BitVector domain)
  throws LowlevelUnsatisfiable {
    if (x < m) {
      if (!domain.get(x)) {
        throw new LowlevelUnsatisfiable("");
      }
    } else {
      domains.reduce(x, domain);
    }
  }

  final public void enumerate(BitVector observers,
                              LowlevelSolutionHandler handler) {
    try {
      condense();
    } catch (Unsatisfiable e) {
      // not satisfiable
      return;
    }
    domains.trimToSize();
    if (m > 0) {
      if (m < n) {
        int[] strategy = Satisfier.compileStrategy(C, Ct, m, n);
        Satisfier.enumerateSolutions(strategy, domains,
                                       C, Ct, R, Rt, m, n,
                                       observers, handler);
      } else {
        // no variables, but some constant...
        handler.handle(domains);
      }
    }
  }

  final public void satisfy() throws Unsatisfiable {
    try {
      condense();
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
   * Rigidify the current constraint. NB: this does not satisfy the constraint.
   * If you want to test well-formedness of nested contexts, call satisfy()
   * before rigidify(). In particular, minimal constraints are not handled
   * properly if the constraint is not satisfied unless you garantee that no
   * node are below minimal nodes.
   **/
  public void rigidify() {
    R = (BitMatrix)C.clone();
    R.closure();
    Rt = (BitMatrix)Ct.clone();
    Rt.closure();
    m = n;
    domains = new DomainVector(m, m);
  }

  /***********************************************************************
   * Marking / Backtracking
   ***********************************************************************/
  private class Backup {
    Backup savedBackup = backup;
    BitMatrix savedC = (BitMatrix)C.clone();
    
    // XXX: necessary to clone ?
    BitMatrix savedR = (R == null ? null : (BitMatrix)R.clone()); 
    BitVector savedMinimal = (BitVector)minimal.clone();
    BitVector savedMaximal = (BitVector)maximal.clone();
    BitVector savedGarbage = (BitVector)garbage.clone();
    BitVector savedVirtual = (BitVector)virtual.clone();
    DomainVector savedDomains = (DomainVector)domains.clone();
  }

  private Backup backup = null;
  public void mark() {
    backup = new Backup();
  }

  public void backtrack() {
    if (backup == null) {
      this.C = new BitMatrix();
      this.Ct = new BitMatrix();
      this.n = 0;
      this.R = null;
      this.Rt = null;
      this.m = 0;
      this.minimal = new BitVector();
      this.maximal = new BitVector();
      this.garbage = new BitVector();
      this.virtual = new BitVector();
    } else {
      this.C = backup.savedC;
      this.Ct = backup.savedC.transpose();
      this.n = backup.savedC.size();
      if (backup.savedR == null) {
        this.R = null;
        this.Rt = null;
        this.m = 0;
      } else {
        this.R = backup.savedR;
        this.Rt = backup.savedR.transpose();
        this.m = backup.savedR.size();
      }
      this.minimal = backup.savedMinimal;
      this.maximal = backup.savedMaximal;
      this.garbage = backup.savedGarbage;
      this.domains = backup.savedDomains;
      this.virtual = backup.savedVirtual;
      this.backup  = backup.savedBackup; // leave this statement at the end !
    }
    onResize(n);
    //mark();
    clearTags(); //TODO:?? should I leave this ?
  }

  /***********************************************************************
   * Miscellaneous
   ***********************************************************************/
  public static abstract class IneqIterator {
    abstract protected void iter(int x1, int x2) throws Unsatisfiable;
  }
  public void ineqIter(IneqIterator iterator) throws Unsatisfiable {
    for (int i = 0; i < n; i++) {
      if (!garbage.get(i)) {
        for (int j = 0; j < n; j++) {
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
        
  
  /***********************************************************************
   * Pretty printing
   ***********************************************************************/
  protected String indexToString(int index) {
    return Integer.toString(index);
  }

  public String domainsToString() {
    StringBuffer sb = new StringBuffer();
    for (int i = m; i < n; i++) {
      if (isValidIndex(i)) {
        sb.append("D(").append(indexToString(i)).append(") = ");
        Domain d = domains.getDomain(i);
        if (d == null) {
          sb.append(d);
        } else {
          sb.append("{");
          boolean needSeparator = false;
          for (int a = 0; a < m; a++) {
            if (d.get(a)) {
              if (needSeparator) {
                sb.append(", ");
              } else {
                needSeparator = true;
              }
              sb.append(indexToString(a));
            }
          }
          sb.append("}");
        }
        sb.append("; ");
      }
    }
    return sb.toString();
  }
   
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    boolean needSeparator = false;
    sb.append("{");
    for (int i = 0; i < n; i++) {
      if (isValidIndex(i)) {
        if (needSeparator) {
          sb.append(", ");
        } else {
          needSeparator = true;
        }
        //        if (minimal.get(i)) {
        //          sb.append('_');
        //        }
        //        if (maximal.get(i)) {
        //          sb.append('!');
        //        }
        sb.append(indexToString(i));
        if (S.debug) {
          sb.append("{").append(i).append("}");
        }
        //        if (isRigid(i)) {
        //          sb.append('*');
        //        }
        //        if (posTagged(i)) {
        //          sb.append("+");
        //        }
        //        if (negTagged(i)) {
        //          sb.append("-");
        //        }
      }
    }
    sb.append(" | ");
    try {
      ineqIter(new IneqIterator () {
        boolean needSeparator = false;
        protected void iter(int x1, int x2) {
          if (needSeparator) {
            sb.append("; ");
          } else {
            needSeparator = true;
          }
          if (!isVirtual(x1) && isVirtual(x2)) {
            sb.append(indexToString(x1)).append(": ").append(indexToString(x2));
          } else {
            sb.append(indexToString(x1)).append(" <: ").append(indexToString(x2));
          }
        }
      });
    } catch (Unsatisfiable e) {
      throw S.panic();
    }
    sb.append("}");
    if (debugK0) { sb.append(domainsToString()); }
    return sb.toString();
  }

  /***********************************************************************
   * Constraint snapping
   ***********************************************************************/
  static abstract public class IndexSelector {
    abstract protected boolean select(int index);
  }

  final public class Snap {
    BitVector quantified;
    BitMatrix snapC;
    //    BitMatrix snapCt;
    int snapN;
    
    // selector selects the quantified indexes
    Snap(IndexSelector selector) {
      snapN = n;
      quantified = new BitVector(n);
      for (int i = 0; i < n; i++) {
        if (!garbage.get(i) && selector.select(i)) {
          quantified.set(i);
        }
      }
      if (!quantified.isEmpty()) {
        // XXX: Possible optimization: keep only the quantified rows
        // XXX: drawback: needs to keep C and Ct
        snapC = (BitMatrix)C.clone();
        //        snapCt = (BitMatrix)Ct.clone();
        
        // the constraint has been satisfied. Hence it is in condensed
        // form, so no need to bother with garbage...
        // XXX: should we be concerned with minimal ?
        // XXX: and with virtual ??
      }
    }

    public void indexIter(boolean quant, IndexIterator iterator) {
      for (int i = 0; i < snapN; i++) {
        if (quant == quantified.get(i)) {
          iterator.iter(i);
        }
      }
    }
    public boolean isQuantified(int i) {
      return quantified.get(i);
    }
    public void quantifiedIter(IndexIterator iterator) {
      indexIter(true, iterator);
    }
    public void ineqIter(IneqIterator iterator) throws Unsatisfiable {
      for (int i = 0; i < snapN; i++) {
        if (quantified.get(i)) {
          for (int j = 0; j < snapN; j++) {
            if (snapC.get(i, j)) {
              iterator.iter(i, j);
            }
          }
        } else {
          for (int j = 0; j < snapN; j++) {
            if (quantified.get(j) && snapC.get(i, j)) {
              iterator.iter(i, j);
            }
          }
        }
      }
    }

    public String toString() {
      final StringBuffer sb = new StringBuffer();
      quantifiedIter
        (new IndexIterator() {
          protected void iter(int i) {
            sb.append(i).append("; ");
          }
        });
      try {
        ineqIter
          (new IneqIterator() {
            protected void iter(int i, int j) {
              sb.append(i).append(" <: ").append(j).append("; ");
            }
          });
      } catch (Unsatisfiable e) {
        throw S.panic();
      }
      return sb.toString();
    }
    
    /**
     * Sets in free all the free (non quantified) variables of this snapped
     * constraint.
     **/
    public void addFreeVariables(BitVector free) {
      if (!quantified.isEmpty()) {
        for (int i = 0; i < snapN; i++) {
          BitVector row = snapC.getRow(i);
          if (row != null) {
            if (quantified.get(i)) {
              free.orNotIn(row, quantified);
            } else {
              // test if i is in relation with some quantified variable
              if (row.getLowestSetBitAnd(quantified) >= 0) {
                // so it is free
                free.set(i);
              }
            }
          }
        }
      }   
    }
  }

  /**
   * Get a snapshot of this constraint. The quantified indexes are selected by
   * selector.
   **/
  public Snap getSnap(IndexSelector selector) {
    Snap result = new Snap(selector);
    if (result.quantified.isEmpty()) {
      return null;
    } else {
      return result;
    }
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
    weakMarkedSize = n;
    weakMarkedM = m;
  }
  public void weakBacktrack() {
    R.setSize(weakMarkedM);
    Rt.setSize(weakMarkedM);
    int nMin = weakMarkedSize;
    int nMax = n;
    setSize(weakMarkedSize);
    weakMarkedSize = -1;
    weakMarkedM = -1;
    for (int i = nMin; i < nMax; i++) {
      indexDiscarded(i);
    }
  }

  /***********************************************************************
   * Constraint simplification
   ***********************************************************************/
  // XXX: should be entirely worked out...

  // the size of the constraint when startSimplify() has been called
  public void startSimplify() {
    weakMark();

    // necessary ?
    posTagged.fill(n);
    negTagged.fill(n);
  }
  public void stopSimplify() {
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

  public void simplify() {
    if (weakMarkedSize >= 0) {
      if (weakMarkedSize == n) {
        return;
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
      domains = new DomainVector(initN, n, n - initN);
      for (int i = initN; i < n; i++) {
        if (!simplified.get(i)) {
          domains.clear(i);
        }
      }
    }
    int initN;                  // don't simplify below this index
    BitVector simplified;          // the vector of indexes being simplified
      
    BitMatrix R;
    BitMatrix Rt;
    DomainVector domains;

    private void computeInitialDomains() {
      // 
      // Compute the initial value of D(x) for all simplified variable x
      //
      for (int x = initN; x < n; x++) {
        Domain dx = domains.getDomain(x);
        if (dx != null) {
          if (isMinimal(x)) {
            dx.and(minimal);
          }
          if (isMaximal(x)) {
            dx.and(maximal);
          }
          if (isVirtual(x)) {
            dx.and(virtual);
          } else {
            dx.andNot(virtual);
          }
          BitVector ux = R.getRow(x);
          if (ux != null) {
            // XXX: Use ux.getLowestSetBit(int pos) to walk through the bit in ux
            for (int x0 = 0; x0 < n; x0++) {
              if (!simplified.get(x0) && ux.get(x0)) {
                // we have x <C x0
                // sigma(x0) = x0 for all auto-solution
                // do D(x) = D(x) \inter \Lower{x0}
                dx.and(Rt.getRow(x0));
              }
            }
          }
          BitVector lx = Rt.getRow(x);
          if (lx != null) {
            for (int x0 = 0; x0 < n; x0++) {
              if (!simplified.get(x0) && lx.get(x0)) {
                // x0 <C x
                // do D(x) = D(x) \inter \Upper{x0}
                dx.and(R.getRow(x0));
              }
            }
          }
          if (posTagged(x)) {
            // We must have sigma(x) <= x
            // Do D(x) = D(x) \inter \Lower{x}
            dx.and(Rt.getRow(x));
          }
          if (negTagged(x)) {
            // We must have sigma(x) >= x
            dx.and(R.getRow(x));
          }
        }
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
          (strategy, domains, C, Ct, R, Rt, initN, n,
           new LowlevelSolutionHandler() {
            protected void handle() {
              for (int x = initN; x < n; x++) {
                if (domains.getDomain(x) != null) {
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
     * Fills (solution, nker) with a non-surjective solution or throws Normal.
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
          DomainVector savedDomains = (DomainVector)domains.clone();
          try {
            if (S.debugSimpl) {
              S.dbg.println("Try excluding " + excludedA);
            }
            domains.exclude(excludedA);
            if (S.debugSimpl) {
              S.dbg.println("Satisfying with " + domains.dump());
            }
            findSolution(strategy, solution);
            return excludedA;
          }
          catch (LowlevelUnsatisfiable e) {
            if (S.debugSimpl) {
              S.dbg.println("Failed");
            }
            // try another value
          }
          finally {
            domains = savedDomains;
          }
        }
        excludedA--;
      }
      throw new Normal();
    }

    /**
     * assume solution contains a solution of C |= C.
     * Do one stroke of simplification.
     **/
    private void simplifyIndex(int x, int[] solution, int[] nker) {
      if (S.debugSimpl) {
        S.dbg.println("x = " + x + "nker[x - initN] = " + nker[x - initN] + "garbage.get(x) = " + garbage.get(x));
      }
      while (simplified.get(x) && nker[x - initN] == 0) {
        // OK, x is not in the codomain of sigma, we can eliminate it

        if (S.debugSimpl) {
          S.dbg.println("Eliminate " + x);
        }
        try {
          //
          // Propagate the constraints int (C, Ct) that go through x
          //
          // XXX: is it inconvenient that it can modify the context ?
          // XXX: normally not: it only adds consequences of the context...
          
          for (int y = 0; y < n; y++) {
            if (x != y && C.get(y, x)) {
              // y < x
              // for each x < z, add y < z
              BitVector ux = C.getRow(x);
              if (ux != null) {
                // C.getRow(y) is necessarily non-null since C.get(y, x) is true
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
          domains.clear(x);
          domains.exclude(x);
          simplified.clear(x);
          garbage.set(x);
          int sx = solution[x - initN];
          if (sx >= initN) {
            nker[sx - initN]--;
          }
          if (posTagged(x) || negTagged(x)) {
            // discard x by merging with sx
            if (S.debugSimpl) {
              S.dbg.println("Merge " + x + " and " + sx);
            }
            if (simplified.get(sx)) {
              if (posTagged(x) && !posTagged(sx)) {
                domains.reduce(sx, Rt.getRow(sx));
                posTagged.set(sx);
              }
              if (negTagged(x) && !negTagged(sx)) {
                domains.reduce(sx, R.getRow(sx));
                negTagged.set(sx);
              }
            }
            indexMerged(x, sx);
          } else {
            // just discard x
            indexDiscarded(x);
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
        S.dbg.println("reduce C = " + C + ", R = " + R + ", garbage = " + garbage);
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
                  uj.andNotOr(uk, lk);
                } else {
                  // remove all i such that k <* i strictly and i is
                  // being simplified
                  uj.andNotAndOr(simplified, uk, lk);
                }
              }
            }
          }
        }
      }
      if (S.debugSimpl) {
        S.dbg.println("reduced C = " + C + ", R = " + R + ", garbage = " + garbage);
      }

    }

    /**
     * Performs transitive reduction.
     **/
    void reduce() {
      reduce(C, R, Rt);
      reduce(Ct, Rt, R);
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
	      indexDiscarded(x);
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
            S.dbg.println("Try to simplify " + K0.this.toString());
            S.dbg.println("with domains " + domains.dump());
          }
          excludedA = findNonSurjective(excludedA, strategy, solution);
          if (S.debugSimpl) {
            S.dbg.print(" sigma = {");
            boolean needSeparator = false;
            for (int x = initN; x < n; x++) {
              if (domains.getDomain(x) != null) {
                if (needSeparator) {
                  S.dbg.print(", ");
                } else {
                  needSeparator = true;
                }
                S.dbg.print(indexToString(x));
                S.dbg.print(" -> ");
                S.dbg.print(indexToString(solution[x - initN]));
              }
            }
            S.dbg.println("}");
          }
          simplifyOnce(solution);
        }
      } catch (Normal e) {
        reduce();
        collect();
      }
    }
  }

  public K0 extractRigids() {
    K0 result = null;
    try {
      result = (K0)this.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
    result.R = (R == null ? null : (BitMatrix)R.clone());
    result.Rt = (Rt == null ? null : (BitMatrix)Rt.clone());
    result.m = m;
    
    // XXX; necessary  to clone ?
    result.C = (BitMatrix)C.clone();
    result.C.setSize(m);
    result.Ct = (BitMatrix)Ct.clone();
    result.Ct.setSize(m);
    result.n = m;
    result.domains = new DomainVector(m, m);
    result.minimal = (BitVector)minimal.clone();
    result.maximal = (BitVector)maximal.clone();
    result.minimal.truncate(m);
    result.maximal.truncate(m);
    result.garbage = (BitVector)garbage.clone();
    result.garbage.truncate(m);
    result.posTagged = new BitVector();
    result.negTagged = new BitVector();
    result.virtual = (BitVector)virtual.clone();
    result.virtual.truncate(m);
    return result;
  }

  /**
   * Assume i1 and i2 are rigid
   **/
  public boolean isLeq(int i1, int i2) {
    return R.get(i1, i2);
  }

  /***********************************************************************
   * Overloading resolution
   ***********************************************************************/
  /**
   * Assume this constraint has just been satisfied
   * possibilities contains indexes of rigid variables
   * for each index k in possibilities, try to enter
   * constraint x <= k and delete k from possibilities
   * if this makes the constraint unsatisfiable
   **/
  public void solveOverloading(int x, BitVector possibilities) {
    if (isRigid(x)) {
      possibilities.and(R.getRow(x));
    } else {
      for (int x0 = 0; x0 < m; x0++) {
        if (possibilities.get(x0)) {
          if (!domains.getDomain(x).intersect(Rt.getRow(x0))) {
            possibilities.clear(x0);
          } else {
            Backup savedBackup = backup;
            mark();
            try {
              leq0(x, x0);
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
  }
    
    
}

