package mlsub.typing.lowlevel;

/**
 * A DomainVector maintains an upper approximation of the set of solutions of
 * a constraint. What is handled dynamically : constraints between rigid and
 * soft variables. Constraints between soft variables are handled in gfp().
 *
 * RMK: only garbage indexes can have null domains
 *
 * @version $OrigRevision: 1.9 $, $OrigDate: 1999/10/03 17:37:05 $
 * @author Alexandre Frey
 **/
final class DomainVector extends java.util.ArrayList {
  int offset;                 // offset
  int width;                  // size of domains
  
  public DomainVector(DomainVector old) {
    super(old.size());
    this.offset = old.offset;
    this.width = old.width;
    for (int i = 0; i < old.size(); i++) {
      Domain d = ((Domain)old.get(i));
      if (d != null)
        add(new Domain(d));
      else
        add(null);
    }
  }

  public DomainVector(int offset, int width) {
    this.offset = offset;
    this.width = width;
  }
  public DomainVector(int offset, int width, int n) {
    super(n);
    //if (width > 0) {
      for (int i = 0; i < n; i++) {
        add(new Domain(width));
      }
      //} 
    this.offset = offset;
    this.width = width;
  }
  public Domain getDomain(int x) {
    return (Domain)get(x - offset);
  }

  void truncate(int x)
  {
    removeRange(x, size());
  }

  private boolean isValidSoft(int x) {
    return x>=offset && x-offset<size() && getDomain(x)!=null;
  }
  private boolean isGarbage(int x) {
    return x>=offset && x-offset<size() && getDomain(x)==null;
  }
  
  public void clear(int x) {
    set(x - offset, null);
  }
  public void reduce(int x, boolean unit, BitVector domain)
  throws LowlevelUnsatisfiable {
    S.assume(S.a&& isValidSoft(x));
    Domain d = getDomain(x);
    d.reduce(unit, domain);
  }
  public void exclude(int x, BitVector domain)
  throws LowlevelUnsatisfiable {
    S.assume(S.a&& isValidSoft(x));
    Domain d = getDomain(x);
    d.exclude(domain);
  }

  public void merge(int src, int dest) throws LowlevelUnsatisfiable {
    S.assume(S.a&& isValidSoft(src));
    Domain srcDomain = getDomain(src);
    if (dest >= offset) {
      // don't forget unit !
      reduce(dest, srcDomain.containsUnit(), srcDomain);
    }
    else {
      // dest is rigid
      S.assume(S.a&& srcDomain.get(dest),src+" merged with "+dest);
    }
    clear(src);
  }

  public void exclude(int value) throws LowlevelUnsatisfiable {
    for (int i = 0; i < size(); i++) {
      Domain d = (Domain)get(i);
      if (d != null) {
        d.exclude(value);
      }
    }
  }
  public void move(int src, int dest) {
    S.assume(S.a&& isValidSoft(src));
    S.assume(S.a&& isGarbage(dest));
    set(dest - offset, getDomain(src));
    clear(src);
  }
  public void extend() {
    // if (width > 0) {
    add(new Domain(width));
    //} else {
    //addElement(null);
    //}
  }

  /***********************************************************************
   * Fix-point computations
   ***********************************************************************/
  private boolean gfpSweep(BitMatrix R, BitMatrix C, 
                           int[] strategy, int dS,
                           int direction)
    throws LowlevelUnsatisfiable {
    boolean changed = false;
    BitVector ideal = new BitVector(width);
    boolean idealContainsUnit = false;
    int length = strategy.length;
    for (int s = (dS > 0 ? 0 : length - 1); s >= 0 && s < length; s += dS) {
      int x = strategy[s];
      Domain dx = getDomain(x);
      if (dx != null) {
        // XXX: already tested ??
        if (dx.isEmpty()) {
          throw LowlevelUnsatisfiable.instance;
        }
        if (dx.needPropagation(direction)) {
          changed = true;
          // compute the ideal of dx
          // the ideal may contain unit !
          ideal.clearAll();
          idealContainsUnit = dx.containsUnit();
          ideal.addProduct(R, dx);
          
          // and intersect the domain of all element j above x with ideal
          for (int j = offset; j < offset + size(); j++) {
            Domain dj = getDomain(j);
            if (dj != null && C.get(x, j)) {
	      if(K0.debugK0){
		S.dbg.println("Reducing domain of "+j);
		S.dbg.println("from "+dj);
		S.dbg.println("with ideal of "+x+": "+ideal);
	      }
              dj.reduce(idealContainsUnit, ideal);
              // XXX: not necessary to test emptyness ??
            }
            // emptyness of dj will normally be tested later on
            // XXX: this comment correct ?
          }
        }
      }
    }
    return changed;
  }
  
  /**
   * Reduce this domain vector to  the greatest fixed-point of a constraint.
   * @param R constraint on the rigid constants (in [0, width[)
   * @param Rt its transpose
   * @param C constraint on [0, offset + size()[
   * @param Ct its tranpose
   * @param strategy an iteration strategy: topological sort of C
   **/
  public void gfp(BitMatrix R, BitMatrix Rt,
                  BitMatrix C, BitMatrix Ct,
                  int[] strategy)
  throws LowlevelUnsatisfiable {
    boolean changed;
    do {
      changed = gfpSweep(R, C, strategy, +1, Domain.UP);
      changed = gfpSweep(Rt, Ct, strategy, -1, Domain.DOWN) || changed;
    } while (changed);
  }

  void initGfpCardinals() {
    for (int i = 0; i < size(); i++) {
      Domain d = (Domain)get(i);
      if (d != null) {
        d.initGfpCardinals();
      }
    }
  }
  
  /**
   * Choose a non-null, non-singleton, domain and return its index. Return -1
   * if all the domains are instantiated. 
   **/
  public int chooseDomain() {
    return chooseDomain(null);
  }

  /**
   * ditto but choose a domain whose index i is in set
   **/
  public int chooseDomain(BitVector set) {
    /*
     * Choose the domain of least cardinal (first-fail heuristic)
     */
    int leastCard = Integer.MAX_VALUE;
    int least = Integer.MIN_VALUE;
    for (int i = 0; i < size(); i++) {
      if (set == null || set.get(i + offset)) {
        Domain d = (Domain)get(i);
        if (d != null){
          int card = d.cardinal();
	  if (card < leastCard && card > 1) {
            least = i + offset;
            leastCard = card;
          }
        }
      }
    }
    return least;
  }


  // toString is final in class java.util.Vector...
  public String dump() {
    Separator sep = new Separator(", ");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < size(); i++) {
      if (get(i) != null) {
        sb.append(sep)
          .append("D(")
          .append(i + offset)
          .append(") = ")
          .append(get(i));
      }
    }
    return sb.toString();
  }
}   

