Summary: The Nice compiler
Name: Nice
Version: VERSION
Release: 1
License: GPL
Group: Development/Nice
Source: Nice-VERSION.tar.gz

%description
Extension of Java with parametric types, multi-methods, and more.

Nice is a new object-oriented programming language.
It extends Java with many advanced features:
   * Parametric types: this is especially useful for containers
     (lists, hash-tables) and allows for shorter and safer code.
   * Anonymous functions: functions can be created and manipulated as
     first-class expressions, just like in Lisp and ML.
     This is much lighter than Java's anonymous classes in many situations,
     for instance with listeners in a GUI.
   * Multi-methods: they allow methods to be defined outside classes.
     This means that new methods can be defined on classes that
     belong to a different package (even in java.*).
     Multi-methods alse extend usual methods with the possibility to
     dispatch on every argument, instead of only the receiver class.
     This supersedes the Visitor pattern.
   * Tuples: this allows in particular methods to return several values.
   * Optional parameters to methods.
     Optional parameters have a default value that is used when the parameter
     is not present in the call. This is much simpler than in Java, where one
     has to write several versions of the method for each combination of
     parameters.
   * Nice detects more errors during compilation: programs written in Nice
     never throw NullPointerException nor ClassCastException.

For more information see http://nice.sourceforge.net

%prep
%setup
echo setup...

%build
echo build...

%install
echo install...

%post
if [ -f /etc/SuSE-release ]; then
  ln -sf nice-startup.el /usr/share/emacs/site-lisp/suse-start-nice.el
fi

%preun
if [ "$1" = "0" ] ; then # last uninstall
  rm -f /usr/share/emacs/site-lisp/suse-start-nice.el
fi

%clean
echo clean...

%files
%attr(-, root, root) /usr

