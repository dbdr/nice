package nice.lang;

/**
   Basic functions, targeted for inlining some time in the future...
*/
public final class Native
{
  public static Object object(Object o) { return o; }  
    
  public static boolean eq (Object o1, Object o2) { return o1 == o2; }
  public static boolean neq(Object o1, Object o2) { return o1 != o2; }
  
  public static boolean eq (boolean b1, boolean b2) { return b1 == b2; }
  public static boolean neq(boolean b1, boolean b2) { return b1 != b2; }

  public static boolean eq (char c1, char c2) { return c1 == c2; }
  public static boolean neq(char c1, char c2) { return c1 != c2; }
  
  /* Numerical comparisons. */
  public static boolean lt (double n1, double n2) { return n1 <  n2; }
  public static boolean leq(double n1, double n2) { return n1 <= n2; }
  public static boolean gt (double n1, double n2) { return n1 >  n2; }
  public static boolean geq(double n1, double n2) { return n1 >= n2; }

  public static boolean lt (float n1, float n2) { return n1 <  n2; }
  public static boolean leq(float n1, float n2) { return n1 <= n2; }
  public static boolean gt (float n1, float n2) { return n1 >  n2; }
  public static boolean geq(float n1, float n2) { return n1 >= n2; }

  public static boolean lt (long n1, long n2) { return n1 <  n2; }
  public static boolean leq(long n1, long n2) { return n1 <= n2; }
  public static boolean gt (long n1, long n2) { return n1 >  n2; }
  public static boolean geq(long n1, long n2) { return n1 >= n2; }

  public static boolean lt (int n1, int n2) { return n1 <  n2; }
  public static boolean leq(int n1, int n2) { return n1 <= n2; }
  public static boolean gt (int n1, int n2) { return n1 >  n2; }
  public static boolean geq(int n1, int n2) { return n1 >= n2; }  

  /* Numerical equality */
  public static boolean eq(double n1, double n2) { return n1 == n2; }
  public static boolean eq(float n1, float n2) { return n1 == n2; }
  public static boolean eq(long n1, long n2) { return n1 == n2; }
  public static boolean eq(int n1, int n2) { return n1 == n2; }      

  /* Numerical inequality */
  public static boolean neq(double n1, double n2) { return n1 != n2; }
  public static boolean neq(float n1, float n2) { return n1 != n2; }
  public static boolean neq(long n1, long n2) { return n1 != n2; }
  public static boolean neq(int n1, int n2) { return n1 != n2; }

  /* Unary minus */
  public static double minus(double n) { return -n; }
  public static float minus(float n) { return -n; }
  public static long minus(long n) { return -n; }
  public static int minus(int n) { return -n; }

  /* Multiplication */
  public static double mult(double n1, double n2) { return n1*n2; }
  public static double div(double n1, double n2) { return n1/n2; }
  public static double remainder(double n1, double n2) { return n1%n2; }
  public static double plus(double n1, double n2) { return n1+n2; }
  public static double minus(double n1, double n2) { return n1-n2; }

  public static float mult(float n1, float n2) { return n1*n2; }
  public static float div(float n1, float n2) { return n1/n2; }
  public static float remainder(float n1, float n2) { return n1%n2; }
  public static float plus(float n1, float n2) { return n1+n2; }
  public static float minus(float n1, float n2) { return n1-n2; }

  public static long mult(long n1, long n2) { return n1*n2; }
  public static long div(long n1, long n2) { return n1/n2; }
  public static long remainder(long n1, long n2) { return n1%n2; }
  public static long plus(long n1, long n2) { return n1+n2; }
  public static long minus(long n1, long n2) { return n1-n2; }

  public static int mult(int n1, int n2) { return n1*n2; }
  public static int div(int n1, int n2) { return n1/n2; }
  public static int remainder(int n1, int n2) { return n1%n2; }
  public static int plus(int n1, int n2) { return n1+n2; }
  public static int minus(int n1, int n2) { return n1-n2; }

  // Primitive type arrays
  public static void set(long   [] array, int index, long value) { array[index] = value; }
  public static void set(int    [] array, int index, int value) { array[index] = value; }
  public static void set(short  [] array, int index, short value) { array[index] = value; }
  public static void set(byte   [] array, int index, byte value) { array[index] = value; }
  public static void set(char   [] array, int index, char value) { array[index] = value; }
  public static void set(double [] array, int index, double value) { array[index] = value; }
  public static void set(float  [] array, int index, float value) { array[index] = value; }
  public static void set(boolean[] array, int index, boolean value) { array[index] = value; }

  public static long    get(long   [] array, int index) { return array[index]; }
  public static int     get(int    [] array, int index) { return array[index]; }
  public static short   get(short  [] array, int index) { return array[index]; }
  public static byte    get(byte   [] array, int index) { return array[index]; }
  public static char    get(char   [] array, int index) { return array[index]; }
  public static double  get(double [] array, int index) { return array[index]; }
  public static float   get(float  [] array, int index) { return array[index]; }
  public static boolean get(boolean[] array, int index) { return array[index]; }
}
