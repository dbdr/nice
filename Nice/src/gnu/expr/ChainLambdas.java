package gnu.expr;

/** Sets up the firstChild/nextSibling links of each LambdaExp.
 * Setup 'outer' links of ScopeExp and its sub-classes.
 * Also generates a class name for each ClassExp and registers each class.
 * Also, if lambda is bound to a unique declaration, make that its name.
 */

public class ChainLambdas extends ExpWalker
{
  Compilation comp;
  ScopeExp currentScope;

  public static void chainLambdas (Expression exp, Compilation comp)
  {
    ChainLambdas walker = new ChainLambdas();
    walker.comp = comp;
    exp.walk(walker);
    //or:  walter.walkExpression(exp);
  }

  protected Expression walkScopeExp (ScopeExp exp)
  {
    ScopeExp saveScope = currentScope;
    try
      {
	/* Nice: We keep outer if it exists. This is used for Nice classes,
	   whose outer is the 'fun' class which holds the anonymous
	   functions occuring in the class methods (overriding of
	   native methods).
	*/
	if (exp.outer == null)
	  exp.outer = currentScope;
	currentScope = exp;
	exp.walkChildren(this);
	return exp;
      }
    finally
      {
	currentScope = saveScope;
      }
  }

  protected Expression walkLambdaExp (LambdaExp exp)
  {    
    LambdaExp parent = currentLambda;
    if (parent != null && ! (parent instanceof ClassExp))
      {
	exp.nextSibling = parent.firstChild;
	parent.firstChild = exp;
      }

    ScopeExp saveScope = currentScope;
    try
      {
	exp.outer = currentScope;
	currentScope = exp;
	exp.walkChildrenOnly(this);
      }
    finally
      {
	currentScope = saveScope;
      }
    exp.walkProperties(this);

    // Put list of children in proper order.
    LambdaExp prev = null, child = exp.firstChild;
    while (child != null)
      {
	LambdaExp next = child.nextSibling;
	child.nextSibling = prev;
	prev = child;
	child = next;
      }
    exp.firstChild = prev;

    if (exp.getName() == null && exp.nameDecl != null)
      exp.setName(exp.nameDecl.getName());
    return exp;
  }

  protected Expression walkClassExp (ClassExp exp)
  {    
    LambdaExp parent = currentLambda;
    if (parent != null && ! (parent instanceof ClassExp))
      {
	exp.nextSibling = parent.firstChild;
	parent.firstChild = exp;
      }

    walkScopeExp(exp);

    // Give name to object class.
    exp.getCompiledClassType(comp);
    comp.addClass(exp.type);
    if (exp.isMakingClassPair())
      {
	exp.instanceType.setName(exp.type.getName()+"$class");
	comp.addClass(exp.instanceType);
      }
    return exp;
  }
}
