// Copyright (c) 1997  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.bytecode;
import java.io.*;

public class Scope
{
  /** The enclosing scope. */
  Scope parent;
  Scope nextSibling;
  Scope firstChild, lastChild;

  int start_pc;
  int end_pc;
  Variable vars;
  Variable last_var;
  //  Variable lookup (String name);
  public final Variable firstVar () { return vars; }

  public VarEnumerator allVars () { return new VarEnumerator (this); }

  /** Link this scope as the next child of its parent scope. */
  public void linkChild (Scope parent)
  {
    this.parent = parent;
    if (parent == null)
      return;
    if (parent.lastChild == null)
      parent.firstChild = this;
    else
      parent.lastChild.nextSibling = this;
    parent.lastChild = this;
  }

  public Variable addVariable (CodeAttr code, Type type, String name)
  {
    Variable var = new Variable(name, type);
    addVariable (code, var);
    return var;
   }

  public void addVariable (Variable var)
  {
    if (last_var == null)
      vars = var;
    else
      last_var.next = var;
    last_var = var;
  }

  /* Add a new Variable, linking it in after a given Variable, */
  public void addVariableAfter (Variable prev, Variable var)
  {
    if (prev == null)
      { // Put first
	var.next = vars;
	vars = var;
      }
    else
      {
	var.next = prev.next;
	prev.next = var;
      }
    if (last_var == prev)
      last_var = var;
    if (var.next == var)
      throw new Error("cycle");
  }

  public void addVariable (CodeAttr code, Variable var)
  {
    var.start_pc = code == null ? 0 : code.PC;
    addVariable (var);
    if (var.isSimple() && code != null)
      var.allocateLocal(code);
  }

  /**
   * Return a variable the scope, by numerical index.
   * @param index the number of the variable
   */
  public Variable getVariable(int index) {
    Variable var = vars;
    while (--index >= 0)
      var = var.next;
    return var;
  }

  static boolean equals (byte[] name1, byte[] name2) {
    if (name1.length != name2.length)
      return false;
    if (name1 == name2)
      return true;
    for (int i = name1.length; --i >= 0; )
      if (name1[i] != name2[i])
	return false;
    return true;
  }

  public void setStartPC(int PC)
  {
    start_pc = PC;
    for (Variable var = firstVar ();  var != null;  var = var.nextVar ())
      var.start_pc = PC;
  }

  /**
   * Search by name for a Variable in this Scope (only).
   * @param name name to search for
   * @return the Variable, or null if not found (in this scope).
   */
  Variable lookup (String name) {
    for (Variable var = vars;  var != null;  var = var.next) {
      if (name.equals(var.name))
	return var;
    }
    return null;
  }
};
