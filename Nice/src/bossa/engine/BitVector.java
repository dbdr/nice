package bossa.engine;

/**
 * Same as java.util.BitSet, without the synchronization stuff
 * plus additional features.
 *
 * @see java.util.BitSet
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
public class BitVector implements Cloneable, java.io.Serializable {
  private final static int BITS_PER_UNIT = 6;
  private final static int MASK = (1<<BITS_PER_UNIT)-1;
  private long bits[];

  /**
   * Convert bitIndex to a subscript into the bits[] array.
   */
  private static int subscript(int bitIndex) {
    return bitIndex >> BITS_PER_UNIT;
  }
  /**
   * Convert a subscript into the bits[] array to a (maximum) bitIndex.
   */
  private static int bitIndex(int subscript) {
    return (subscript << BITS_PER_UNIT) + MASK;
  }

  /**
   * Creates an empty set.
   */
  public BitVector() {
    this(1 << BITS_PER_UNIT);
  }

  /**
   * Creates an empty set with the specified size.
   * @param nbits the size of the set
   */
  public BitVector(int nbits) {
    /* nbits can't be negative; size 0 is OK */
    if (nbits < 0) {
      throw new NegativeArraySizeException(Integer.toString(nbits));
    }
    /* On wraparound, truncate size; almost certain to o-flo memory. */
    if (nbits + MASK < 0) {
      nbits = Integer.MAX_VALUE - MASK;
    }
    /* subscript(nbits + MASK) is the length of the array needed to hold nbits */
    bits = new long[subscript(nbits + MASK)];
  }

  /**
   * Ensures that the BitVector can hold at least an nth bit.
   * This cannot leave the bits array at length 0.
   * @param    nth the 0-origin number of the bit to ensure is there.
   */
  private void ensureCapacity(int nth) {
    int required = subscript(nth) + 1;  /* +1 to get length, not index */
    if (required > bits.length) {
      /* Ask for larger of doubled size or required size */
      int request = Math.max(2 * bits.length, required);
      long newBits[] = new long[request];
      System.arraycopy(bits, 0, newBits, 0, bits.length);
      bits = newBits;
    }
  }

  /**
   * Sets a bit.
   * @param bit the bit to be set
   */
  public void set(int bit) {
    if (bit < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(bit));
    }
    ensureCapacity(bit);
    bits[subscript(bit)] |= (1L << (bit & MASK));
  }

  /**
   * Clears a bit.
   * @param bit the bit to be cleared
   */
  public void clear(int bit) {
    if (bit < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(bit));
    }
    ensureCapacity(bit);
    bits[subscript(bit)] &= ~(1L << (bit & MASK));
  }

  /**
   * Gets a bit.
   * @param bit the bit to be gotten
   */
  public boolean get(int bit) {
    if (bit < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(bit));
    }
    boolean result = false;
    int n = subscript(bit);     /* always positive */
    if (n < bits.length) {
      result = ((bits[n] & (1L << (bit & MASK))) != 0L);
    }
    return result;
  }

  /**
   * Logically ANDs this bit set with the specified set of bits.
   * @param set the bit set to be ANDed with
   */
  final public void and(BitVector set) {
    if (this == set) {
      return;
    }
    int bitsLength = bits.length;
    int setLength = set.bits.length;
    int n = Math.min(bitsLength, setLength);
    for (int i = n ; i-- > 0 ; ) {
      bits[i] &= set.bits[i];
    }
    for (; n < bitsLength ; n++) {
      bits[n] = 0L;
    }
  }
  /**
   * do this = this & ~(set)
   */
  final public void andNot(BitVector set) {
    if (this == set) {
      clearAll();
    } else {
      int bitsLength = bits.length;
      int setLength = set.bits.length;
      int n = Math.min(bitsLength, setLength);
      for (int i = n ; i-- > 0 ; ) {
        bits[i] &= ~set.bits[i];
      }
    }
  }

  /**
   * do this = this & (~set) on bits >= from
   */
  final public void andNot(int from, BitVector set) {
    int bitsLength = bits.length;
    int setLength = set.bits.length;
    int n = Math.min(bitsLength, setLength);
    int i = subscript(from);
    if (i < n) {
      bits[i] &= ~set.bits[i] & (-1L) << (from & MASK);
      i++;
      for (; i < n; i++) {
        bits[i] &= ~set.bits[i];
      }
    }
  }

  /**
   * Do this = this & (~set1 | set2) on all bits >= from
   **/
  final public void andNotOr(int from, BitVector set1, BitVector set2) {
    int bitsLength = bits.length;
    int set1Length = set1.bits.length;
    int set2Length = set2.bits.length;
    int n = Math.min(bitsLength, set1Length);
    int i = subscript(from);
    if (i < n) {
      bits[i] &= ~(set1.bits[i] & ((-1L) << (from & MASK))) | (i < set2Length ? set2.bits[i] : 0L);
      i++;
      for (; i < n; i++) {
        bits[i] &= ~set1.bits[i] | (i < set2Length ? set2.bits[i] : 0L);
      }
    }
  }

  /**
   * Do this = this & (~set1 | set2)
   **/
  final public void andNotOr(BitVector set1, BitVector set2) {
    andNotOr(0, set1, set2);
  }

  /**
   * Do this = this & ~(S1 & S2)
   **/
  final public void andNotAnd(BitVector set1, BitVector set2) {
    int n = Math.min(this.bits.length,
                     Math.min(set1.bits.length, set2.bits.length));
    for (int i = 0; i < n; i++) {
      bits[i] &= ~(set1.bits[i] & set2.bits[i]);
    }
  }
  
  /**
   * Do this = this & (~(S1 & S2) | S3)
   * i.e. this = this & ~(S1 & S2 & ~S3)
   **/
  final public void andNotAndOr(BitVector S1, BitVector S2, BitVector S3) {
    int n = bits.length;
    int bits1length = S1.bits.length;
    if (bits1length < n) {
      n = bits1length;
    }
    int bits2length = S2.bits.length;
    if (bits2length < n) {
      n = bits2length;
    }
    int bits3length = S3.bits.length;
    for (int i = 0; i < n; i++) {
      bits[i] &= ~(S1.bits[i] & S2.bits[i]) | (i < bits3length ? S3.bits[i] : 0L);
    }
  }
  
  /**
   * Do this = this | set
   **/
  final public void or(BitVector set) {
    if (this == set) {
      return;
    }
    int setLength = set.nonZeroLength();
    if (setLength > 0) {
      ensureCapacity(bitIndex(setLength-1));// this might cause some problem...
    }
    for (int i = setLength; i-- > 0 ;) {
      bits[i] |= set.bits[i];
    }
  }

  /**
   * Do this = this | (set1 & ~set2)
   **/
  final public void orNotIn(BitVector set1, BitVector set2) {
    if (this == set1) {
      return;
    }
    int set1Length = set1.nonZeroLength();
    int set2Length = set2.bits.length;
    if (set1Length > 0) {
      ensureCapacity(bitIndex(set1Length - 1)); // XXX: problem ?
    }
    for (int i = set1Length; i-- > 0;) {
      if (i < set2Length) {
        bits[i] |= set1.bits[i] & ~set2.bits[i];
      } else {
        bits[i] |= set1.bits[i];
      }
    }
  }
      
      
  /**
   * Do this = this | (set1 & set2)
   **/
  final public void orAnd(BitVector set1, BitVector set2) {
    if (this == set1 || this == set2) {
      return;
    }
    int setLength = Math.min(set1.nonZeroLength(),set2.nonZeroLength());
    if (setLength > 0) {
      ensureCapacity(bitIndex(setLength-1));// this might cause some problem...
    }
    for (int i = setLength; i-- > 0 ;) {
      bits[i] |= set1.bits[i] & set2.bits[i];
    }
  }
      
      

  /*
   * If there are some trailing zeros, don't count them
   */
  private int nonZeroLength() {
    int n = bits.length;
    while (n > 0 && bits[n-1] == 0L) { n--; }
    return n;
  }


  /**
   * Do this = this ^ set
   **/
  final public void xor(BitVector set) {
    int setLength = set.bits.length;
    if (setLength > 0) {
      ensureCapacity(bitIndex(setLength-1));
    }
    for (int i = setLength; i-- > 0 ;) {
      bits[i] ^= set.bits[i];
    }
  }

  /**
   * Gets the hashcode.
   */
  final public int hashCode() {
    long h = 1234;
    for (int i = bits.length; --i >= 0; ) {
      h ^= bits[i] * (i + 1);
    }
    return (int)((h >> 32) ^ h);
  }

  /**
   * Calculates and returns the set's size in bits.
   * The maximum element in the set is the size - 1st element.
   */
  final public int size() {
    return bits.length << BITS_PER_UNIT;
  }

  /**
   * Compares this object against the specified object.
   * @param obj the object to compare with
   * @return true if the objects are the same; false otherwise.
   */
  final public boolean equals(Object obj) {
    if ((obj != null) && (obj instanceof BitVector)) {
      if (this == obj) {
        return true;
      }
      BitVector set = (BitVector) obj;
      int bitsLength = bits.length;
      int setLength = set.bits.length;
      int n = Math.min(bitsLength, setLength);
      for (int i = n ; i-- > 0 ;) {
        if (bits[i] != set.bits[i]) {
          return false;
        }
      }
      if (bitsLength > n) {
        for (int i = bitsLength ; i-- > n ;) {
          if (bits[i] != 0L) {
            return false;
          }
        }
      } else if (setLength > n) {
        for (int i = setLength ; i-- > n ;) {
          if (set.bits[i] != 0L) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }



  /**
   * Clones the BitVector.
   */
  public Object clone() {
    BitVector result = null;
    try {
      result = (BitVector) super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
    result.bits = new long[bits.length];
    System.arraycopy(bits, 0, result.bits, 0, result.bits.length);
    return result;
  }

  /**
   * Converts the BitVector to a String.
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    Separator sep = new Separator(", ");
    buffer.append('{');
    int limit = size();
    for (int i = 0 ; i < limit ; i++) {
      if (get(i)) {
        buffer.append(sep).append(i);
      }
    }
    buffer.append('}');
    return buffer.toString();
  }

  /**
   * Computes the number of bits set in this BitVector.
   * This function is specially efficient on sparse bit sets.
   * NOTE: in previous implementations of MLsub type-checkers, there were
   * an average number of 2 bits set per vector...
   **/
  final public int bitCount() {
    int cnt = 0;
    int bitsLength = bits.length;
    for (int i = 0; i < bitsLength; i++) {
      long chunk = bits[i];
      while(chunk != 0L) {
        chunk &= chunk - 1L;
        cnt++;
      }
    }
    return cnt;
  }

  /**
   * Computes the number of bits set among the first n bits
   **/
  final public int bitCount(int n) {
    int cnt = 0;
    int maxChunk = subscript(n);
    if (maxChunk < bits.length) {
      for (int i = 0; i < maxChunk; i++) {
        long chunk = bits[i];
        while (chunk != 0L) {
          chunk &= chunk - 1L;
          cnt++;
        }
      }
      long lastChunk = bits[maxChunk] & ((1L << (n & MASK)) - 1);
      while (lastChunk != 0L) {
        lastChunk &= lastChunk - 1L;
        cnt++;
      }
      return cnt;
    } else {
      return bitCount();
    }
  }

  private 
  /* static // XXX: work around Symantec JIT bug */
  int chunkLowestSetBit(long chunk) {
    if (chunk == 0L) {
      return 64;
    } else {
      int bit = 0;
      if ((chunk & 0xffffffffL) == 0) { bit += 32; chunk >>>= 32; }
      if ((chunk &     0xffffL) == 0) { bit += 16; chunk >>>= 16; }
      if ((chunk &       0xffL) == 0) { bit +=  8; chunk >>>=  8; }
      if ((chunk &        0xfL) == 0) { bit +=  4; chunk >>>=  4; }
      if ((chunk &        0x3L) == 0) { bit +=  2; chunk >>>=  2; }
      if ((chunk &        0x1L) == 0) { bit++; }
      return bit;
    }
  }

  final public static int UNDEFINED_INDEX = Integer.MIN_VALUE;
  
  /**
   * Compute the first bit set in this BitVector or UNDEFINED_INDEX if this
   * set is empty.
   **/
  final public int getLowestSetBit() {
    int n = bits.length;
    for (int i = 0; i < n; i++) {
      long chunk = bits[i];
      if (chunk != 0L) {
        return (i << BITS_PER_UNIT) + chunkLowestSetBit(chunk);
      }
    }
    return UNDEFINED_INDEX;
  }

  /**
   * Compute the first cleared bit in this BitVector (a BitVector always
   * contains a finite number of set bits, so this method always returns a
   * value)
   **/
  final public int getLowestClearedBit() {
    int n = bits.length;
    int i;
    for (i = 0; i < n; i++) {
      long chunk = bits[i];
      if (chunk != ~0L) {
	return (i << BITS_PER_UNIT) + chunkLowestSetBit(~chunk);
      }
    }
    return i << BITS_PER_UNIT;
  }

  /**
   * Compute the first bit set that is greater or equal than pos, or
   * UNDEFINED_INDEX if there is no such bit
   **/
  final public int getLowestSetBit(int pos) {
    int n = bits.length;
    int i = subscript(pos);
    if (i >= n) {
      return -1;
    } else {
      long chunk = bits[i] & ((~0L) << (pos & MASK));
      while (true) {
        if (chunk != 0L) {
          return (i << BITS_PER_UNIT) + chunkLowestSetBit(chunk);
        }
        i++;
        if (i >= n) {
          return UNDEFINED_INDEX;
        }
        chunk = bits[i];
      }
    }
  }

  /**
   * Gets the first bit set that is strictly greater than i or
   * UNDEFINED_INDEX if there is none
   **/
  public int getNextBit(int i) {
    int result = getLowestSetBit(i + 1);
    if (result < 0) {
      return UNDEFINED_INDEX;
    } else {
      return result;
    }
  }
    

  /**
   * Compute the first bit set in this & ~set.
   * @return UNDEFINED_INDEX if there is no such bit
   **/
  final public int getLowestSetBitNotIn(BitVector set) {
    int n = bits.length;
    int setLength = set.bits.length;
    for (int i = 0; i < n; i++) {
      long chunk = bits[i];
      if (i < setLength) {
        chunk &= ~set.bits[i];
      }
      if (chunk != 0L) {
        return (i << BITS_PER_UNIT) + chunkLowestSetBit(chunk);
      }
    }
    return UNDEFINED_INDEX;
  }
  
  /**
   * Compute the first bit in this & set
   * @return UNDEFINED_INDEX if there is no such bit
   **/
  final public int getLowestSetBitAnd(BitVector set) {
    int n = bits.length;
    int setLength = set.bits.length;
    int result = 0;
    for (int i = 0; i < n; i++) {
      long chunk = bits[i];
      if (i < setLength) {
        chunk &= set.bits[i];
      } else {
        return UNDEFINED_INDEX;
      }
      if (chunk != 0L) {
        return result + chunkLowestSetBit(chunk);
      } else {
        result += 64;
      }
    }
    return UNDEFINED_INDEX;
  }

  final public int getLowestSetBitAndNotIn(BitVector set, BitVector exclude) {
    int n = bits.length;
    int setLength = set.bits.length;
    int excludeLength = exclude.bits.length;
    int result = 0;
    for (int i = 0; i < n; i++) {
      long chunk = bits[i];
      if (i < setLength) {
        chunk &= set.bits[i];
      } else {
        return UNDEFINED_INDEX;
      }
      if (i < excludeLength) {
        chunk &= ~exclude.bits[i];
      }
      if (chunk != 0L) {
        return result + chunkLowestSetBit(chunk);
      } else {
        result += 64;
      }
    }
    return UNDEFINED_INDEX;
  }

  /**
   * Return true if this BitVector is included in set
   **/
  final public boolean includedIn(BitVector set) {
    if (this == set) {
      return true;
    } else {
      int bitsLength = nonZeroLength();
      int setLength = set.nonZeroLength();
      if (bitsLength > setLength) {
        return false;
      } else {
        for (int i = 0; i < bitsLength; i++) {
          if ((bits[i] & ~set.bits[i]) != 0L) {
            return false;
          }
        }
        return true;
      }
    }
  }

  /**
   * Clear all the bits in this BitVector
   **/
  final public void clearAll() {
    int n = bits.length;
    for (int i = 0; i < n; i++) {
      bits[i] = 0L;
    }
  }

  /**
   * Returns true if no bit is set in this BitVector
   **/
  public boolean isEmpty() {
    int n = bits.length;
    for (int i = 0; i < n; i++) {
      if (bits[i] != 0L) {
        return false;
      }
    }
    return true;
  }

      
  /**
   * Copy bit src into bit dest and clear src bit
   **/
  final public void bitCopy(int src, int dest) {
    if (get(src)) {
      set(dest);
      clear(src);
    } else {
      clear(dest);
    }
  }

  /**
   * Merge bit src and bit dest, put the result in bit dest.
   **/
  final public void bitMerge(int src, int dest) {
    if (get(src)) {
      set(dest);
    }
  }

  /**
   * Clear all the bits beyond newSize (newSize included), so that this
   * BitVector has less than newSize bits.
   **/
  final public void truncate(int newSize) {
    int i = subscript(newSize);
    int bitsLength = bits.length;
    if (i < bitsLength) {
      bits[i++] &= (1L << (newSize & MASK)) - 1;
      for (; i < bitsLength; i++) {
        bits[i] = 0L;
      }
    }
    // XXX: should shrink the vector to lower memory usage ?
  }

  /**
   * Fills the n first bit of this BitVector
   **/
  final public void fill(int n) {
    ensureCapacity(n - 1);
    int maxChunk = subscript(n);
    for (int i = 0; i < maxChunk; i++) {
      bits[i] = ~0L;
    }
    if (maxChunk < bits.length) {
      bits[maxChunk] |= (1L << (n & MASK)) - 1;
    }
  }


  /**
   * Clears the first n bits of this BitVector
   **/
  final public void fillNot(int n) {
    int maxChunk = subscript(n);
    if (maxChunk >= bits.length) {
      clearAll();
    } else {
      for (int i = 0; i < maxChunk; i++) {
        bits[i] = 0L;
      }
      bits[maxChunk] &= (~0L) << (n & MASK);
    }
  }

  /**
   * add to this the product of M by v, that is the BitVector v' such that v' =
   * union of all M.getRow(i) such that v.get(i) is true.
   **/
  final public void addProduct(BitMatrix M, BitVector v) {
    int n = M.size();
    for (int i = 0; i < n; i++) {
      if (v.get(i)) {
        or(M.getRow(i));
      }
    }
  }
}
