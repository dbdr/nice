package mlsub.typing.lowlevel;

import java.util.ArrayList;

/**
   A square matrix of bits, used to represent a relation between integers.
   
   @version $Revision$, $Date$
   @author Alexandre Frey
   @author Daniel Bonniot (Optimization for sparse and reflexives matrices)
 **/
final public class BitMatrix implements Cloneable {
  /**
   * a vector of BitVectors. rows.get(i) is the ith line of
   * the matrix. 
   **/
  private BitVector[] rows;
  private int size;

  // A BitVector instance can be saved if the only bit set is the one
  // on the diagonal, which is usual here.
  private boolean reflexive;
  
  /**
   * Constructs an empty matrix
   **/
  public BitMatrix() {
    this(10);
  }
  private BitMatrix(int rowCapacity) {
    if(rowCapacity==0)
      rowCapacity=5;
    rows = new BitVector[rowCapacity];
  }

  /**
   * Returns the number of rows and columns of this matrix
   **/
  public int size() {
    return size;
  }

  /**
   * Set the size of the matrix. If the new size is greater than the current
   * size, new empty rows and columns are added. If the new size is less than
   * the current size, all rows and columns at index greater than newSize are
   * discarded.
   **/
  public void setSize(int newSize) {
    if (newSize < size) {
      // clear the columns beyond newSize, so that subsequent calls to extend()
      // or setSize will find them empty.
      for (int i = 0; i < newSize; i++) {
        BitVector row = rows[i];
        if (row != null) {
          row.truncate(newSize);
        }
      }
      for (int i = size; i>newSize; )
	rows[--i] = null;
    }
    else {
      if(rows.length<newSize)
	{
	  BitVector[] newRows = new BitVector[rows.length*2];
	  System.arraycopy(rows, 0, newRows, 0, rows.length);
	  rows = newRows;
	}
    }
    size = newSize;
  }

  /**
   * Grow the matrix by one column and one row, initially empty. Returns the
   * index of the new row.
   **/
  public int extend() {
    setSize(size+1);
    return size-1;
  }
  
  /**
     To be able to return a correct row for reflexive matrices
     without allocating a new one, we share the short ones
     with only one bit set.
  */
  private static BitVector[] reflexiveRow = new BitVector[256];
  
  /**
   * Get ith row. May return null if it is empty or if row is beyond size()
   *
   * <p>If the matrix is reflexive, the row MUST NOT be modified by the caller.
   **/
  BitVector getRow(int i) {
    if (i < size) {
      if (reflexive && rows[i] == null)
	{
	  if (i >= reflexiveRow.length || reflexiveRow[i] == null)
	    {
	      BitVector res = new BitVector(i+1);
	      res.set(i);
	      if (i < reflexiveRow.length)
		reflexiveRow[i] = res;
	      return res;
	    }
	  
	  return reflexiveRow[i];
	}
      else
	{
	  if (reflexive) 
	    rows[i].set(i);
	  return rows[i];
	}
    } else {
      return null;
    }
  }

  /**
   * get element at position (i, j), i.e., returns the value of i < j.
   * Assume i and j are valid indexes
   **/
  public boolean get(int i, int j) {
    if (i == j && reflexive)
      return true;
    
    BitVector row = rows[i];
    return row != null && row.get(j);
  }

  /**
   * Set element at position (i, j) to true. Assume i and j are valid indexes.
   **/
  public void set(int i, int j) {
    if (i == j && reflexive)
      return;
    
    BitVector row = rows[i];
    if (row == null) {
      row = new BitVector(size);
      rows[i] = row;
    }
    row.set(j);
  }

  /**
   * Set element at position (i, j) to false. Assume i and j are valid indexes.
   **/
  public void clear(int i, int j) {
    if (i == j && reflexive)
      {
	// Outch! We have to get back to a non-reflexive representation
	reflexive = false;
	for (int k = 0; k<size; k++)
	  if (k != i) 
	    set(k, k);
      }
    
    BitVector row = rows[i];
    if (row != null) {
      row.clear(j);
    }
  }

  /**
   * Performs in-place reflexive transitive closure of the relation.
   **/
  public void closure() {
    if (S.debug) {
      BitMatrix testcopy = (BitMatrix)this.clone();
      BitMatrix original = (BitMatrix)this.clone();
      this.closure2();
      testcopy.closure1();
      boolean equal = true;
      for(int i = 0; i<size; i++)
	if (! ( (this.getRow(i)==testcopy.getRow(i)) || 
		(this.getRow(i).equals( testcopy.getRow(i))))) 
	  equal = false;
      if (!equal) {
	Debug.println("Warning new closure method produced incorrect results.");
	Debug.println("orginal matrix:");
	Debug.println(original.toString());
	Debug.println("closure using new method:");
	Debug.println(this.toString());
	Debug.println("closure using standard method:");
	Debug.println(testcopy.toString());
      }

    } else if (size < 16)
      closure1();
    else
      closure2();
  }

  private void closure1() {
    // Warshall algorithm
    int size = this.size;
    for (int k = 0; k < size; k++) {
      BitVector row_k = rows[k];
      if (row_k != null) {
        for (int i = 0; i < size; i++) {
          BitVector row_i = rows[i];
          if (row_i != null && row_i.get(k)) { // i < k
            /* for all j such that k < j, add i < j */
            row_i.or(row_k);
          }
        }
      }
    }
    //for (int i = 0; i < size; i++) set(i, i);
    reflexive = true;
  }

  /**
     This algorithm is O(n*m) (m = average number bits set per row) when there 
     are no cylcic relations in the a matrix, while closure1 is O(n2). 
     This algorithm has a much larger overhead so for small matrices 
     the old one still faster. 
  */
  private void closure2() {
    int size = this.size;
    boolean[] done = new boolean[size];
    boolean[] busy = new boolean[size];
    int[] index = new int[size];
    int[] bitpos = new int[size];
    int ndone = 0;
    while (ndone < size) {
      if ((rows[ndone] != null) && !done[ndone]) {
	int stackpos = 0;
	index[0] = ndone;
	bitpos[0] = 0;
	busy[ndone] = true;
	BitVector current = rows[ndone];
	while (stackpos >= 0) {
	  if (bitpos[stackpos] < 0) {
	    if (stackpos > 0){
	      rows[index[stackpos-1]].or(rows[index[stackpos]]);
	      current = rows[index[stackpos-1]];
	    }	
	    done[index[stackpos]] = true;
	    busy[index[stackpos--]] = false;
	  } else {
	    int nextbitpos = current.getLowestSetBit(bitpos[stackpos]);
	    if (nextbitpos < 0 || nextbitpos >= size) bitpos[stackpos] = -1;
	    else{
	      bitpos[stackpos] = nextbitpos+1;
	      if (rows[nextbitpos] != null) {
		if (done[nextbitpos]) {
		  rows[index[stackpos]].or(rows[nextbitpos]);
		} else {    
		  if (!busy[nextbitpos]) {
		    busy[nextbitpos] = true;
		    index[++stackpos] = nextbitpos;
		    bitpos[stackpos] = 0;
		    current = rows[index[stackpos]];
		  } else {
		    if (index[stackpos] != nextbitpos) {
		      int tempsp = stackpos-1;
		      BitVector cyclicmask = new BitVector(size);
		      do {
			bitpos[tempsp] = -1;
			rows[index[stackpos]].or(rows[index[tempsp]]);
			cyclicmask.set(index[tempsp]);
		      } while (index[tempsp--] != nextbitpos);
		      current = (BitVector)current.clone();
		      current.andNot(cyclicmask);
		      bitpos[stackpos] = 0;
		    }
		  }
		}
	      }
	    }
	  }
	}
      }
      ndone++;
    }
    reflexive = true;
  }

  /**
   * Returns a newly-allocated BitMatrix initialized with the transpose
   * of this BitMatrix
   **/
  public BitMatrix transpose() {
    BitMatrix m = new BitMatrix(size);
    m.setSize(size);
    for (int i = 0; i < size; i++) {
      BitVector row = rows[i];
      if (row != null) {
	for (int j = row.getLowestSetBit();
	     j>=0;
	     j = row.getLowestSetBit(j+1))
	  m.set(j, i);
      }
    }
    m.reflexive = reflexive;
    return m;
  }

  public Object clone() {
    try {
      BitMatrix m = (BitMatrix)super.clone();
      BitVector[] v = (BitVector[])rows.clone();
      
      for (int i = 0; i < size; i++) {
        BitVector row = v[i];
        if (row != null) {
          v[i] = (BitVector) row.clone();
        }
      }
      m.rows = v;
      m.size = size;
      m.reflexive = reflexive;
      return m;
    } catch (CloneNotSupportedException e) {
      throw new InternalError
	("Should never happen, since BitMatrix implements Cloneable");
    }
  }

  
  public String toString() {
    StringBuffer sb = new StringBuffer("{");

    boolean needSeparator = false;

    for (int row = 0; row < size; row++) 
      for (int col = 0; col < size; col++)
	if (get(row, col)) 
	  {
	    if (needSeparator) {
	      sb.append(", ");
	    } else {
	      needSeparator = true;
	    }
	    sb.append("(").append(row).append(",").append(col).append(")");
	  }
    return sb.append("}").toString();
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj == null || !(obj instanceof BitMatrix))
      return false;
    
    BitMatrix that = (BitMatrix) obj;

    for (int row = 0; row < size; row++) 
      for (int col = 0; col < size; col++)
	if (get(row, col) != that.get(row, col))
	  return false;
    
    return true;
  } 

  /**
   * Fills the array S with a topological sort of the relation on [m, size()[
   * described by this matrix. Assume that S is large enough to hold size() -
   * m integers. Assume 0 <= m <= this.size().
   *
   * <p>After a call to topologicalSort() and if the relation is a DAG, for
   * all i, j such that this.get(i, j) is true, i appears before j in S
   **/
  public void topologicalSort(int m, int[] S) {
    // The algorithm should be modified to handle reflexive case.
    // Easy way out: insert the reflexive points.
    // However I'm not sure this case ever happens anyway:
    // topologicalSort is only used in Satisfier on C which is not reflexive
    
    int n = size;
    int[] sortStack = new int[n - m];
    int sp = -1;                // index in sortStack
    int s = n - m;              // index in S
    BitVector touched = new BitVector(n);
    touched.fill(m);
    for (int i = m; i < n; i++) {
      if (!touched.get(i)) {
        touched.set(i);
        sortStack[++sp] = i;
        loop: while(sp >= 0) {
          int j = sortStack[sp];
          BitVector row_j = getRow(j);
          if (row_j != null) {
            int succ = row_j.getLowestSetBitNotIn(touched);
            if (succ >= 0) {
              // do the successor first
              touched.set(succ);
              sortStack[++sp] = succ;
              continue loop;
            }
          }
          // no untouched successor found
          sp--;                 // pop j
          S[--s] = j;           // push it on the sort array
        }
      }
    }
  }


  /**
   * Tests if this bit matrix is included in M on the first n columns and
   * lines. Assume m <= this.size() and m <= M.size(). If there exists (i, j)
   * < (m, m) such that this.get(i, j) but !M.get(i, j), return an array a
   * such that a[0] = i and a[1] = j. Otherwise, return null.
   **/
  public int[] includedIn(int m, BitMatrix M) {
    if (this == M) {
      return null;
    } else {
      for (int i = 0; i < m; i++) {
        BitVector row1 = this.getRow(i);
        if (row1 != null) {
          int j;
          BitVector row2 = M.getRow(i);
          if (row2 != null) {
            j = row1.getLowestSetBitNotIn(row2);
          } else {
            j = row1.getLowestSetBit();
          }
          if (0 <= j && j < m) {
            return new int[] {i, j};
          }
        }
      }
      return null;
    }
  }

  /**
     Move index src to dest.
   */
  public void indexMove(int src, int dest)
  {
    rowMove(src, dest);
    colMove(src, dest);
  }

  /**
   * In-place copy row src to row dest and clear row src. The previous results
   * of getRow(src) must not be used after this call.
   **/
  private void rowMove(int src, int dest) {
    rows[dest] = getRow(src);
    rows[src] = null;
  }

  /**
   * copy column src to column dest and clear column src.
   * Assume src != dest
   **/
  private void colMove(int src, int dest) {
    for (int i = 0; i < size; i++) {
      BitVector row = getRow(i);
      if (row != null) {
        row.bitCopy(src, dest);
      }
    }
  }

  /**
     Merge indexes src and dest, put the result in dest.
  */
  public void indexMerge(int src, int dest)
  {
    rowMerge(src, dest);
    colMerge(src, dest);
  }

  /**
   * Merge row src and row dest, put the result in row dest. 
   * Assume src != dest. Previous results of getRow(src) must not be used
   * after this call.
   **/
  private void rowMerge(int src, int dest) {
    BitVector srcRow = getRow(src);
    if (srcRow != null) {
      BitVector destRow = getRow(dest);
      if (destRow == null) {
        rows[dest] = srcRow;
      } else {
        destRow.or(srcRow);
      }
    }
    rows[src] = null;
  }

  /**
   * Merge column src and column dest, put the result in column dest. Assume
   * src != dest.
   **/
  private void colMerge(int src, int dest) {
    for (int i = 0; i < size; i++) {
      BitVector row = getRow(i);
      if (row != null) {
        row.bitMerge(src, dest);
      }
    }
  }

  /**
   * Compute the set of y such that x <* y.
   **/
  public BitVector ideal(int x) {
    BitVector ideal = new BitVector(size);
    addIdeal(x, ideal);
    return ideal;
  }

  // assume !ideal.get(x)
  private void addIdeal(int x, BitVector ideal) {
    ideal.set(x);
    BitVector xUp = getRow(x);
    if (xUp != null) {
      int y;
      while ((y = xUp.getLowestSetBitNotIn(ideal)) >= 0) {
        addIdeal(y, ideal);
      }
    }
  }
}
