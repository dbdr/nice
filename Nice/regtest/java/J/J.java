package regtest.java.J;

public class J {
  public J() {}

  // A constructor with default visibility.
  // We'll make sure that we can subclass this class in a different package
  // in Nice, in which case this constructor must be igored.
  J(Object dummy) {}

  public void access(Object o) {}
  void access(String o) { throw new Error("Not accessible"); }

  Object obj;

  void jMethod(Other o) {}

  public void init(java.util.Map m) {}
}
