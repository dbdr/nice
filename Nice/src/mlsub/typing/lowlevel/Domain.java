package mlsub.typing.lowlevel;

/**
 * A "Domain" is a BitVector that holds all the possible values of a variable.
 *
 * A value can be any rigid variable or a special constant called "unit" which
 * implements all the interfaces of the constraint but is comparable with no
 * rigid variable. This is to handle satisfiability in the absence of any
 * satisfiability witness For example, the constraint x <= x IS satisfiable
 * even if there is no rigid variable. In this case, the domain of x is {unit}
 *
 * When a domain becomes empty, i.e., when it does not contain any rigid
 * variable nor unit, it means that the contraint is unsatisfiable.
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
final class Domain extends BitVector {
  private boolean containsUnit; // true if this domain contains index -1 (or unit)
  
  final static int UP = 0;
  final static int DOWN = 1;
  int cardUp = Integer.MAX_VALUE; // cardinal of the domain the last time
                                // it has been propagated up (including unit)
  int cardDown = Integer.MAX_VALUE;// and down
  public Domain(int width) {
    super(width);
    fill(width);
    this.containsUnit = true;
    this.cardUp = width;
    this.cardDown = width;
  }

  // size of this domain (possibly including unit)
  int cardinal() {
    if (containsUnit) {
      return super.bitCount() + 1;
    } else {
      return super.bitCount();
    }
  }

  public boolean isEmpty() {
    return !containsUnit && super.isEmpty();
  }

  public boolean containsUnit() {
    return containsUnit;
  }
  
  public boolean needPropagation(int direction) {
    int card = cardinal();
    switch(direction) {
    case UP:
      if (card < cardUp) {
        cardUp = card;
        return true;
      }
      break;
    case DOWN:
      if (card < cardDown) {
        cardDown = card;
        return true;
      }
      break;
    }
    return false;
  }

  void initGfpCardinals() {
    cardUp = Integer.MAX_VALUE;
    cardDown = Integer.MAX_VALUE;
  }

  /**
   * Constrain this domain to be included in (set, unit)
   **/
  public void reduce(boolean unit, BitVector set)
  throws LowlevelUnsatisfiable {
    this.and(set);
    this.containsUnit &= unit; 
    if (this.isEmpty()) {
      throw LowlevelUnsatisfiable.instance;
    }
  }

  /**
   * Exclude all the values in set
   **/
  public void exclude(BitVector set)
  throws LowlevelUnsatisfiable {
    this.andNot(set);
    if (this.isEmpty()) {
      throw LowlevelUnsatisfiable.instance;
    }
  }

  void rawExcludeUnit() {
    this.containsUnit = false;
  }
  
  public void excludeUnit() throws LowlevelUnsatisfiable {
    this.containsUnit = false;
    if (this.isEmpty()) {
      throw LowlevelUnsatisfiable.instance;
    }
  }

  /**
   * Exclude value of this domain
   * assume value >= -1 (-1 represents unit)
   **/
  public void exclude(int value) throws LowlevelUnsatisfiable {
    if (value == -1) {
      this.containsUnit = false;
    } else {
      super.clear(value);
    }
    if (this.isEmpty()) {
      throw LowlevelUnsatisfiable.instance;
    }
  }

  /**
   * choose a value in this domain
   * Returns -1 if the only possible value is unit
   * @exception LowlevelUnsatisfiable if this.isEmpty()
   **/
  public int chooseValue() throws LowlevelUnsatisfiable {
    return chooseValue(true, null);
  }

  /**
   * Choose a value within (unit, set)
   * @exception LowlevelUnsatisfiable is this contains
   * no element in (unit, set)
   **/
  public int chooseValue(boolean unit, BitVector set)
  throws LowlevelUnsatisfiable {
    int res = (set == null ?
               getLowestSetBit() :
               getLowestSetBitAnd(set));
    if (res != BitVector.UNDEFINED_INDEX) {
      return res;
    } else {
      if (unit && containsUnit()) {
        return -1;
      } else {
        throw LowlevelUnsatisfiable.instance;
      }
    }
  }

  /**
   * value >= -1 (-1 represents unit)
   **/
  public boolean containsValue(int value) {
    if (value == -1) {
      return containsUnit;
    } else {
      return super.get(value);
    }
  }
  
  /**
   * Restrict this domain to be exactly {value}
   * value is required to be >= -1 (-1 represents unit)
   * @exception LowlevelUnsatisfiable if value is not in the domain
   **/
  public void instantiate(int value) throws LowlevelUnsatisfiable {
    if (!containsValue(value)) {
      throw LowlevelUnsatisfiable.instance;
    }
    clearAll();
    this.containsUnit = false;
    if (value == -1) {
      this.containsUnit = true;
    } else {
      set(value);
    }
  }

  /**
   * Returns true if there is a common value in this and set
   * i.e., if chooseValue(false, set) will not throw LowlevelUnsatisfiable
   **/
  public boolean intersect(BitVector set) {
    return this.getLowestSetBitAnd(set) != BitVector.UNDEFINED_INDEX;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString());
    if (containsUnit) {
      sb.append("+{-1}");
    }
    return sb.toString();
  }


  /**
   * Iteration thru the domain elements
   **/
  int getFirstBit() { // unused method ???
    int result = super.getLowestSetBit();
    if (result == UNDEFINED_INDEX && containsUnit) {
      // don't forget unit !
      result = -1;
    }
    return result;
  }
  public int getNextBit(int i) {
    int result;
    if (i >= 0) {
      result = super.getNextBit(i);
      if (result == UNDEFINED_INDEX && containsUnit) {
        result = -1;
      }
    } else {
      // i is either -1 or UNDEFINED_INDEX
      result = UNDEFINED_INDEX;
    }
    return result;
  }
}
