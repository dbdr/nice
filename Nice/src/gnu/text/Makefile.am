## Process this with automake to create Makefile.in

AUTOMAKE_OPTIONS = foreign

@Make_rules@

java_sources = \
  Char.java PrettyWriter.java \
  LineBufferedReader.java SourceError.java Lexer.java SyntaxException.java \
  QueueReader.java SourceMessages.java NullReader.java \
  ReportFormat.java LiteralFormat.java PadFormat.java CompoundFormat.java \
  IntegerFormat.java RomanIntegerFormat.java EnglishIntegerFormat.java \
  CaseConvertFormat.java FlushFormat.java WriterManager.java

EXTRA_DIST = package.html

PACKAGE_FNAME = gnu-text
GCJ_INPUTS =  $(java_Java)

all: Makefile java-classes.stamp $(@GCJ_COMPILED_SELECTED@)
