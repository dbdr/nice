## Process this with automake to create Makefile.in

AUTOMAKE_OPTIONS = foreign

@Make_rules@

java_sources = \
  ApplyExp.java \
  BeginExp.java \
  BindingInitializer.java \
  BlockExp.java \
  CanInline.java \
  CatchClause.java \
  ChainLambdas.java \
  CheckedTarget.java \
  ClassExp.java \
  ClassInitializer.java \
  Compilation.java \
  ConditionalTarget.java \
  ConsumerTarget.java \
  Declaration.java \
  ErrorExp.java \
  ExitExp.java \
  Expression.java \
  ExpWalker.java \
  FindCapturedVars.java \
  FindTailCalls.java \
  FluidLetExp.java \
  GenericProc.java \
  IfExp.java \
  IgnoreTarget.java \
  Initializer.java \
  Inlineable.java \
  InlineCalls.java \
  Interpreter.java \
  KawaConvert.java \
  Keyword.java \
  LambdaExp.java \
  LetExp.java \
  Literal.java \
  LitTable.java \
  ModuleBody.java \
  ModuleMethod.java \
  ModuleExp.java \
  PairClassType.java \
  ObjectExp.java \
  QuoteExp.java \
  Parser.java \
  PrimProcedure.java \
  ProcInitializer.java \
  PushApply.java \
  ReferenceExp.java \
  ScopeExp.java \
  SeriesTarget.java \
  SetExp.java \
  Special.java \
  StackTarget.java \
  Symbol.java \
  SynchronizedExp.java \
  Target.java \
  ThisExp.java \
  TryExp.java \
  Undefined.java

EXTRA_DIST = package.html

PACKAGE_FNAME = gnu-expr
GCJ_INPUTS =  $(java_Java)

all:  Makefile java-classes.stamp $(@GCJ_COMPILED_SELECTED@)
