package mlsub.typing.lowlevel;

/**
   Same as java.util.BitSet, without the synchronization stuff
   plus additional features.
   
   @see java.util.BitSet
   
   @version $Revision$, $Date$
   @author Alexandre Frey
   @author Daniel Bonniot 
   (replaced the underlying java.util.Vector by an array 
    for efficiency reasons;
    most bitvectors are less than 64 elements, so using a long often avoids
    allocating the array)
 **/
public class BitVector implements Cloneable, java.io.Serializable {
  private final static int BITS_PER_UNIT = 6;
  private final static int MASK = (1<<BITS_PER_UNIT)-1;

  private long bits0; // first 64 bits
  private long bits1[]; // used if more than 64 bits are stored

  /**
     @return number of WORDS allocated
  */
  final private int length()
  {
    if (bits1 == null)
      return 1;
    else
      return bits1.length;
  }
  
  /**
   * Convert bitIndex to a subscript into the bits[] array.
   */
  final private static int subscript(int bitIndex) {
    return bitIndex >> BITS_PER_UNIT;
  }
  /**
   * Convert a subscript into the bits[] array to a (maximum) bitIndex.
   */
  final private static int bitIndex(int subscript) {
    return (subscript << BITS_PER_UNIT) + MASK;
  }

  /**
   * Creates an empty set.
   */
  public BitVector() {
    bits0 = 0L;
    bits1 = null;
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
    if (nbits <= (1 << BITS_PER_UNIT))
      // can fit on a word
      {
	bits0 = 0L;
	bits1 = null;
      }
    else
      {
	/* On wraparound, truncate size; almost certain to o-flo memory. */
	if (nbits + MASK < 0) {
	  System.out.println("Wraparoud");
	  nbits = Integer.MAX_VALUE - MASK;
	}
	/* subscript(nbits + MASK) is the length of the array needed to hold nbits */
	int length = subscript(nbits + MASK);
	bits1 = new long[length];
      }
  }

  /**
   * Ensures that the BitVector can hold at least an nth bit.
   * This cannot leave the bits array at length 0.
   * @param    nth the 0-origin number of the bit to ensure is there.
   */
  final private void ensureCapacity(int nth) {
    int required = subscript(nth) + 1;  /* +1 to get length, not index */
    if (required == 1)
      return;
    if (required > length()) {
      /* Ask for larger of doubled size or required size */
      int request = Math.max(2 * length(), required);
      if (bits1 == null)
	{
	  bits1 = new long[request];
	  bits1[0] = bits0;
	}
      else
	{
	  long[] newBits = new long[request];
	  System.arraycopy(bits1, 0, newBits, 0, bits1.length);
	  bits1 = newBits;
	}
    }
  }

  final private void ensureChunkCapacity(int required) {
    if (required > length()) {
      /* Ask for larger of doubled size or required size */
      int request = Math.max(2 * length(), required);
      if (bits1 == null)
	{
	  bits1 = new long[request];
	  bits1[0] = bits0;
	}
      else
	{
	  long[] newBits = new long[request];
	  System.arraycopy(bits1, 0, newBits, 0, bits1.length);
	  bits1 = newBits;
	}
    }
  }

  /**
   * Sets a bit.
   * @param bit the bit to be set
   */
  final public void set(int bit) {
    if (bit < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(bit));
    }
    ensureCapacity(bit);
    if (bits1 == null)
      bits0 |= (1L << bit); // we know that bit <= 64
    else
      bits1[subscript(bit)] |= (1L << (bit & MASK));
  }

  /**
   * Clears a bit.
   * @param bit the bit to be cleared
   */
  final public void clear(int bit) {
    if (bit < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(bit));
    }
    int sub = subscript(bit);
    if (sub < length())
      andW(sub, ~(1L << (bit & MASK)));
  }

  /**
   * Gets a bit.
   * @param bit the bit to be gotten
   */
  final public boolean get(int bit) {
    if (bit < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(bit));
    }
    if (bits1 == null)
      if (bit <= MASK)
	return (bits0 & (1L << bit)) != 0L;
      else
	return false;

    // long representation

    int n = subscript(bit);     /* always positive */
    
    if (n < bits1.length)
      return (bits1[n] & (1L << (bit & MASK))) != 0L;
    return false;
  }

  /**
   * Logically ANDs this bit set with the specified set of bits.
   * @param set the bit set to be ANDed with
   */
  final public void and(BitVector set) {
    if (bits1 == null)
      {
	if (set.bits1 == null)
	  bits0 &= set.bits0;
	else
	  bits0 &= set.bits1[0];
	return;
      }
    
    if (this == set) {
      return;
    }

    if (set.bits1 == null)
      {
	// go back to a short representation
	bits0 = bits1[0] & set.bits0;
	bits1 = null;
	return;
      }
    
    int bitsLength = length();
    int setLength = set.length();
    int n = Math.min(bitsLength, setLength);
    for (int i = n ; i-- > 0 ; ) {
      bits1[i] &= set.bits1[i];
    }
    for (; n < bitsLength ; n++) {
      bits1[n] = 0L;
    }
  }
  /**
   * do this = this & ~(set)
   */
  final public void andNot(BitVector set) {
    if (this == set)
      {
	clearAll();
	return;
      }
    
    if (bits1 == null)
      {
	if (set.bits1 == null)
	  bits0 &= ~set.bits0;
	else
	  bits0 &= ~set.bits1[0];
	return;
      }

    if (set.bits1 == null)
      {
	bits1[0] &= ~set.bits0;
	return;
      }
    
    int bitsLength = length();
    int setLength = set.length();
    int n = Math.min(bitsLength, setLength);
    for (int i = n ; i-- > 0 ; ) {
      bits1[i] &= ~set.bits1[i];
    }
  }

  /** 
      Assumes i is a valid word index.

      @return  bits[i] 
  */
  final private long getW(int i)
  {
    if (bits1 == null)
      return bits0; // since i is valid, it must be 0
    else
      return bits1[i];
  }
      
  /** 
      Sets bits[i] = w 

      Assumes i is a valid word index.      
  */
  final private void setW(int i, long w)
  {
    if (bits1 == null)
      bits0 = w;
    else
      bits1[i] = w;
  }
  
  /** 
      Do bits[i] &= w 

      Assumes i is a valid word index.
  */
  final private void andW(int i, long w)
  {
    if (bits1 == null)
      bits0 &= w;
    else
      bits1[i] &= w;
  }
      
  /** 
      Do bits[i] |= w 

      Assumes i is a valid word index.
  */
  final private void orW(int i, long w)
  {
    if (bits1 == null)
      bits0 |= w;
    else
      bits1[i] |= w;
  }
      
  /** 
      Do bits[i] ^= w 

      Assumes i is a valid word index.
  */
  final private void xorW(int i, long w)
  {
    if (bits1 == null)
      bits0 ^= w;
    else
      bits1[i] ^= w;
  }
      
  /**
   * do this = this & (~set) on bits >= from
   */
  final public void andNot(int from, BitVector set) {
    int n = Math.min(length(), set.length());
    int i = subscript(from);
    if (i < n) {
      andW(i, ~set.getW(i) & (-1L) << (from & MASK));
      i++;
      for (; i < n; i++) {
        bits1[i] &= ~set.getW(i);
      }
    }
  }

  /**
   * Do this = this & (~set1 | set2) on all bits >= from
   **/
  final public void andNotOr(int from, BitVector set1, BitVector set2) {
    int bitsLength = length();
    int set1Length = set1.length();
    int set2Length = set2.length();
    int n = Math.min(bitsLength, set1Length);
    int i = subscript(from);
    if (i < n) {
      andW(i, ~(set1.getW(i) & ((-1L) << (from & MASK))) | (i < set2Length ? set2.getW(i) : 0L)); // BUG? pas de from pour set2? daniel
      i++;
      for (; i < n; i++) {
        bits1[i] &= ~set1.getW(i) | (i < set2Length ? set2.getW(i) : 0L);
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
    int n = Math.min(this.length(),
                     Math.min(set1.length(), set2.length()));
    for (int i = 0; i < n; i++) {
      andW(i, ~(set1.getW(i) & set2.getW(i)));
    }
  }
  
  /**
   * Do this = this & (~(S1 & S2) | S3)
   * i.e. this = this & ~(S1 & S2 & ~S3)
   **/
  final public void andNotAndOr(BitVector S1, BitVector S2, BitVector S3) {
    int n = length();
    int bits1length = S1.length();
    if (bits1length < n) {
      n = bits1length;
    }
    int bits2length = S2.length();
    if (bits2length < n) {
      n = bits2length;
    }

    if (n<=1)
    {
       andW(0, ~(S1.getW(0) & S2.getW(0)) | S3.getW(0));
    }
    else
    {
      int bits3length = S3.length();
      if (bits3length > n) {
        bits3length = n;
      }

      for (int i = 0; i < bits3length; i++) 
        bits1[i] &= ~(S1.bits1[i] & S2.bits1[i]) | S3.getW(i);

      for (int i = bits3length; i < n; i++)       
        bits1[i] &= ~(S1.bits1[i] & S2.bits1[i]);
    }
  }
  
  /**
   * Do this = this | set
   **/
  final public void or(BitVector set) {
    int setLength = set.length();
    if (setLength > 1) {
      ensureChunkCapacity(setLength);
      for (int i = setLength; i-- > 0 ;) {
        bits1[i] |= set.bits1[i];
      }
    }
    else
    {
      orW(0, set.getW(0));
    }
  }

  /**
   * Do this = this | (set1 & set2)
   **/
  final public void orAnd(BitVector set1, BitVector set2) {
    int setLength = Math.min(set1.nonZeroLength(),set2.nonZeroLength());
    if (setLength > 1) {
      ensureChunkCapacity(setLength);
    }
    for (int i = setLength; i-- > 0 ;) {
      orW(i, set1.getW(i) & set2.getW(i));
    }
  }
  
  /*
   * If there are some trailing zeros, don't count them
   * result is greater or equal to 1
   */
  final private int nonZeroLength() {
    if (bits1 == null)
      return 1;
    
    int n = bits1.length-1;
    while (n > 0 && bits1[n] == 0L) { n--; }
    return n+1;
  }

  /**
   * Do this = this ^ set
   **/
  final public void xor(BitVector set) {
    int setLength = set.length();
    if (setLength > 1) {
      ensureChunkCapacity(setLength);
    }
    for (int i = setLength; i-- > 0 ;) {
      xorW(i, set.getW(i));
    }
  }

  /**
   * Gets the hashcode.
   */
  final public int hashCode() {
    long h = 1234;
    for (int i = length(); --i >= 0; ) {
      h ^= getW(i) * (i + 1);
    }
    return (int)((h >> 32) ^ h);
  }

  /**
   * Calculates and returns the set's size in bits.
   * The maximum element in the set is the size - 1st element.
   */
  final public int size() {
    return length() << BITS_PER_UNIT;
  }

  /**
   * Compares this object against the specified object.
   * @param obj the object to compare with
   * @return true if the objects are the same; false otherwise.
   */
  final public boolean equals(Object obj) {
    if ((obj == null) || !(obj instanceof BitVector))
      return false;
    
    if (this == obj)
      return true;

    BitVector set = (BitVector) obj;
    int bitsLength = length();
    int setLength = set.length();
    int n = Math.min(bitsLength, setLength);
    for (int i = n ; i-- > 0 ;) {
      if (getW(i) != set.getW(i)) {
	return false;
      }
    }
    if (bitsLength > n) {
      for (int i = bitsLength ; i-- > n ;) {
	if (bits1[i] != 0L) {
	  return false;
	}
      }
    } else if (setLength > n) {
      for (int i = setLength ; i-- > n ;) {
	if (set.bits1[i] != 0L) {
	  return false;
	}
      }
    }
    return true;
  }

  /**
   * Clones the BitVector.
   */
  final public Object clone() {
    BitVector result = null;
    try {
      result = (BitVector) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError("this shouldn't happen, since we are Cloneable");
    }

    if (bits1 == null)
      result.bits0 = bits0;
    else
      {
	int n = nonZeroLength();
	if (n <= 1)
        {
	  result.bits0 = bits1[0];
	}
	else
	{
	  result.bits1 = new long[n];
	  System.arraycopy(bits1, 0, result.bits1, 0, n);
	}
      }
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
    int bitsLength = length();
    for (int i = 0; i < bitsLength; i++) {
      long chunk = getW(i);
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
    if (maxChunk >= length())
      return bitCount();
    
    for (int i = 0; i < maxChunk; i++) {
      long chunk = getW(i);
      while (chunk != 0L) {
	chunk &= chunk - 1L;
	cnt++;
      }
    }
    long lastChunk = getW(maxChunk) & ((1L << (n & MASK)) - 1);
    while (lastChunk != 0L) {
      lastChunk &= lastChunk - 1L;
      cnt++;
    }
    return cnt;
  }

  final private 
  static /* XXX: work around Symantec JIT bug: comment static */
  int chunkLowestSetBit(long chunk) {
    int bit = 0;
    chunk &= -chunk; //fix sign bit
    if ((chunk & 0xffffffff00000000L) != 0 )  bit += 32;
    if ((chunk & 0xffff0000ffff0000L) != 0 )  bit += 16;
    if ((chunk & 0xff00ff00ff00ff00L) != 0 )  bit += 8;
    if ((chunk & 0xf0f0f0f0f0f0f0f0L) != 0 )  bit += 4;
    if ((chunk & 0xccccccccccccccccL) != 0 )  bit += 2;
    if ((chunk & 0xaaaaaaaaaaaaaaaaL) != 0 )  bit += 1;
    return bit;
  }

  final public static int UNDEFINED_INDEX = Integer.MIN_VALUE;
  
  /**
   * Compute the first bit set in this BitVector or UNDEFINED_INDEX if this
   * set is empty.
   **/
  public int getLowestSetBit() {
    if (bits1 == null)
    {
      if (bits0 != 0L)
        return chunkLowestSetBit(bits0);
    }	
    else
    {
      int n = bits1.length;
      for (int i = 0; i < n; i++) {
        long chunk = bits1[i];
        if (chunk != 0L)
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
    int n = length();
    int i;
    for (i = 0; i < n; i++) {
      long chunk = getW(i);
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
    int n = length();
    int i = subscript(pos);
    if (i >= n) {
      return UNDEFINED_INDEX; // was -1, Alex's bug?
    } else {
      long chunk = getW(i) & ((~0L) << (pos & MASK));
      while (true) {
        if (chunk != 0L) {
          return (i << BITS_PER_UNIT) + chunkLowestSetBit(chunk);
        }
        i++;
        if (i >= n) {
          return UNDEFINED_INDEX;
        }
        chunk = bits1[i];
      }
    }
  }

  /**
   * Gets the first bit set that is strictly greater than i or
   * UNDEFINED_INDEX if there is none
   **/
  public int getNextBit(int i) {
    return getLowestSetBit(i + 1);
  }
    
  /**
   * Compute the first bit set in this & ~set.
   * @return UNDEFINED_INDEX if there is no such bit
   **/
  final public int getLowestSetBitNotIn(BitVector set) {
    int n = length();
    int setLength = set.length();
    for (int i = 0; i < n; i++) {
      long chunk = getW(i);
      if (i < setLength) {
        chunk &= ~set.getW(i);
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
    int n = length();
    int setLength = set.length();
    int result = 0;
    for (int i = 0; i < n; i++) {
      long chunk = getW(i);
      if (i < setLength) {
        chunk &= set.getW(i);
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
    int n = length();
    int setLength = set.length();
    int excludeLength = exclude.length();
    int result = 0;
    for (int i = 0; i < n; i++) {
      long chunk = getW(i);
      if (i < setLength) {
        chunk &= set.getW(i);
      } else {
        return UNDEFINED_INDEX;
      }
      if (i < excludeLength) {
        chunk &= ~exclude.getW(i);
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
          if ((getW(i) & ~set.getW(i)) != 0L) {
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
    bits0 = 0L;
    bits1 = null;
  }

  /**
   * Returns true if no bit is set in this BitVector
   **/
  public boolean isEmpty() {
    if (bits1 == null)
      return bits0 == 0L;

    for (int i = bits1.length; --i>0; )
      if (bits1[i] != 0L) 
        return false;

    return bits1[0] == 0L;
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
    if (bits1 == null)
    {
      if (i == 0)
	bits0 &= (1L << (newSize & MASK)) - 1;
      return;
    }
    int bitsLength = nonZeroLength();
    if (i < bitsLength)
      andW(i, (1L << (newSize & MASK)) - 1);

    int newlength = Math.min(bitsLength, i+1);

    if (newlength < bits1.length)
    {
      long[] newBits = new long[newlength];
      System.arraycopy(bits1, 0, newBits, 0, newlength);
      bits1 = newBits;
    }
  }

  /**
   * Fills the n first bit of this BitVector
   **/
  final public void fill(int n) {
    ensureCapacity(n - 1);
    int maxChunk = subscript(n);
    for (int i = 0; i < maxChunk; i++) {
      setW(i, ~0L);
    }
    if (maxChunk < length()) {
      orW(maxChunk, (1L << (n & MASK)) - 1);
    }
  }

  /**
   * Clears the first n bits of this BitVector
   **/
  final public void fillNot(int n) {
    int maxChunk = subscript(n);
    if (maxChunk >= length()) {
      clearAll();
    } else {
      for (int i = 0; i < maxChunk; i++) {
        setW(i, 0L);
      }
      andW(maxChunk, (~0L) << (n & MASK));
    }
  }

  /**
   * add to this the product of M by v, that is the BitVector v' such that v' =
   * union of all M.getRow(i) such that v.get(i) is true.
   **/
  final public void addProduct(BitMatrix M, BitVector v) {
    int n = M.size();
    ensureCapacity(n);
    for(int i = v.getLowestSetBit(); 
	i >= 0;
	i = v.getLowestSetBit(i+1))
      {
	BitVector set = M.getRow(i);
	int setLength = set.nonZeroLength();
	if (setLength > 1) {
	  for (int j = setLength; j-- > 0 ;)
	    bits1[j] |= set.bits1[j];
	}
      	else
      	{
          orW(0, set.getW(0));
      	}

      }
  }

  final public void slowaddProduct(BitMatrix M, BitVector v) {
    int n = M.size();
    for (int i = 0; i < n; i++) {
      if (v.get(i)) {
        or(M.getRow(i));
      }
    }
  }
}
