
                 INSTALLING THE NICE COMPILER ON WINDOWS

This file explains how to install and use the Nice compiler on Windows
(any version). 

INSTALLATION STEPS

1. You should have downloaded a file Nice-X.Y.zip, where X.Y is the version
   of the compiler. The latest version of this file is always available at
   http://nice.sourceforge.net/Nice.zip

2. Extract the content of that file. A "Nice" subdirectory will be created.
   It is advised to install in in "C:\".
   Otherwise you must edit the "nicec.bat" script file as explained there.

3. Add the "Nice" directory in your PATH:
     * On Windows 2000, open Control Panel, double-click on System, 
       select the Advanced tab, click the "Environment Variables..." button,
       click on user variable PATH and add C:\Nice; in front of its value.
     * On Windows NT, open Control Panel, double-click on System, 
       select the Environment tab, click on user variable PATH and 
       add C:\Nice; in front of its value. 
     * On Windows 9x, add 
         set PATH=C:\Nice;%PATH%
       at the end of your autoexec.bat.


USING THE COMPILER

You can call the compiler from a command prompt with "nicec".
The file nicec.html lists the command-line options of the compiler.

It is recommended to use Emacs for windows to edit and compile programs.
There are blends, both freely available:
  * NT Emacs http://www.gnu.org/software/emacs/windows/
  * X Emacs  http://www.xemacs.org/Download/win32/

You have to enable the Nice mode for Emacs in the startup file. 
Here is where you can find this file:

Windows 2000
NT Emacs: \WINNT\Profiles\<username>\.emacs (??)
XEmacs:   \Documents and Settings\<username>\.xemacs\init.el

If you know about other versions of windows, please share this information
with the others by writing to the author.

You have to place the following code in this emacs startup file:

;; Nice mode for Emacs

(setq load-path (cons "c:\\Nice\\emacs" load-path))
(setq auto-mode-alist 
  (cons '("\\.nice$" . nice-mode) auto-mode-alist))
(autoload 'nice-mode "nice-mode" 
  "Major mode for editing Nice programs." t)

;; End of Nice mode for Emacs


CONTACT

If you experience any problem with Nice, in particular with the
Windows installation, or if you can contribute information for this document,
you should visit http://nice.sourceforge.net or email 
Daniel Bonniot: bonniot@users.sourceforge.net
