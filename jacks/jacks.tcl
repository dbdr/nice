package require Tcl 8.3



# Util method we use to clean up after ourselves

proc _rm_classes {} {
  delete *.class
}

proc _rm_generated_files {} {
  delete core *.warn *.err *.stack *.out
}

proc cat { f } {
    set fd [open $f r]
    set buff [read $fd]
    close $fd
    return $buff
}

proc doclean { } {
    foreach subdir [subdirectories] {
        set here [pwd]
        cd $subdir
        doclean
        cd $here
    }

    _rm_classes
    _rm_generated_files

    set cleanup .cleanup
    if {[file exists $cleanup]} {
        foreach file [split [cat $cleanup] \n] {
            if {[file isdirectory $file]} {
                # file delete will only delete
                # a directory if it is empty.
                # Otherwise it will generate
                # an error.
                #puts "deleting dir \"$file\""
                if {[catch {file delete $file}]} {
                    #global errorInfo
                    #puts $errorInfo
                    #puts "glob results \"[glob $file/*]\""
                }
            } else {
                delete $file
            }
        }
        delete $cleanup
    }
}

# Get a list of each subdirectory WRT the current directory

proc subdirectories { } {
    set all [glob -nocomplain *]
    set dirs {}
    foreach file $all {
        if {[file isdirectory $file]} {
            # Skip known bad subdirectories
            switch -- $file {
                SVN -
                CVS -
                RCS {
                    continue
                }
            }
            lappend dirs [file join [pwd] $file]
        }
    }
    return [lsort -dictionary $dirs]
}

# Save the given buffer in the given file and
# return the name of the file. This procedure
# is most useful in conjunction with the compile
# command. The method also adds the given file
# to the list of tempory files for a directory.

proc saveas { file data } {
    global requested_compiler

    if { $requested_compiler == "nicec" } {
	set file [join [split $file .] /]/main.nice
    }

    set dir [file dirname $file]

    # Don't create/delete the cur dir on a filename file "Foo.java"!
    if {$dir != "."} {
        file mkdir $dir
    }

    cleanup $file
    set fd [open $file w]
    fconfigure $fd -translation binary -encoding binary
    puts -nonewline $fd $data
    close $fd

    # We need the dir to be empty before it can be deleted, so
    # the file needs to get removed first.
    if {$dir != "."} {
        cleanup $dir
    }

    return $file
}

proc compile { args } {
    global requested_compiler

    set args [_set_classpath $args]
    set args [_set_compiler_encoding $args]

    _verify_files_exist $args

    # Get a unique prefix for this compile (like Foo.java.javac)
    set file [lindex $args end]
    set prefix $file.$requested_compiler

    # Convert pkg1/File.java to pkg1\File.java on Windows
    set native_args [list]
    foreach arg $args {
        if {[file exists $file]} {
            set arg [file nativename $arg]
        }
	if { $requested_compiler == "nicec" } {
	    set dir [file dirname $arg]
	    set arg [join [split $dir /] .]
	}
	lappend native_args $arg
    }

    # Run the compiler and return the results
    _exec_impl $prefix $::JAVAC $::JAVAC_FLAGS $native_args
}


# The compile_and_run command will invoke the compile command for
# each of your command line arguments, then it will start up the
# runtime to test out the produced code and return the value
# printed to stdout by the runtime

proc compile_and_run { args } {
    global requested_compiler last_compile_warn_or_error last_compile_output

    set result [eval compile $args]
    if {$result != "PASS"} {
        return "COMPILE $result"
    }

    # Set the CLASSPATH again, but this time make
    # sure that  . is on the path so that the JVM
    # runtime can actually find the class we just
    # compiled.
    set args [_set_classpath $args 1]

    # Get the name of the class from the last argument:
    # pkg1/pkg2/MyClass.java -> pkg1.pkg2.MyClass
    set file_list [file split [lindex $args end]]
    set class_list [lrange $file_list 0 end-1]
    set tmp [lindex $file_list end]
    set tmp [lindex [split $tmp .] 0]
    lappend class_list $tmp
    set class [join $class_list .]

    # Get a unique prefix for this run (like Foo.jikes)
    set prefix $class.$requested_compiler

    # Run the compiler and return the results
    set result [_exec_impl $prefix $::JAVA $::JAVA_FLAGS $class]

    switch $result {
        FAIL -
        WARN {
            return [cat $last_compile_warn_or_error]
        }
        PASS {
            if {$last_compile_output == {}} {
                return ""
            } else {
                return [cat $last_compile_output]
            }
        }
        COREDUMP {
            return $result
        }
        default {
            error "unknown branch \"$result\""
        }
    }
}

# This little util proc is used to extract any -classpath
# argument that the user might have passed to a compile
# or compile_and_run method. It will set the env(CLASSPATH)
# variable taking into account the platform dir separator.
# This method will return the new value for argl.

proc _set_classpath { argl {adddot 0} } {
    global env tcl_platform JAVA_CLASSPATH

    # We expect -classpath to be separated by :
    # if running under windows, we need to use ; not :
    if {$tcl_platform(platform) == "windows"} {
        set PATH_SEP \;
    } else {
        set PATH_SEP :
    }

    # The user can pass a -classpath argument to set env(CLASSPATH)
    set CLASSPATH $JAVA_CLASSPATH

    # If adddot is true, then append . to the CLASSPATH
    if {$CLASSPATH != "" && $adddot} {
        append CLASSPATH ${PATH_SEP}
        append CLASSPATH .
    }

    set ind [lsearch $argl -classpath]
    if {$ind != -1} {
      set end [expr {$ind + 1}]
      set arg [lindex $argl $end]
      set argl [lreplace $argl $ind $end]

      if {$tcl_platform(platform) == "windows"} {
          regsub -all : $arg \; foo
      }
      if {$CLASSPATH != ""} {
          append CLASSPATH ${PATH_SEP}
      }
      append CLASSPATH $arg
    }

    #puts "CLASSPATH for $outfile is \"${CLASSPATH}\""
    set env(CLASSPATH) $CLASSPATH

    return $argl
}

# This little util proc is used to extract any -encoding
# argument that the user might have passed to a compile
# or compile_and_run method. It will modify the command
# line based on the value of JAVAC_ENCODING_FLAG. For
# example, with gcj the arguments {-encoding utf-8}
# would get translated to {--encoding=utf-8}

proc _set_compiler_encoding { argl } {
    global JAVAC_ENCODING_FLAG

    set ind [lsearch $argl -encoding]
    if {$ind != -1} {
      set end [expr {$ind + 1}]
      set arg [lindex $argl $end]
      set tmp [split ${JAVAC_ENCODING_FLAG}${arg} " "]
      if {[llength $tmp] == 2} {
          set argl [lreplace $argl $ind $end \
                   [lindex $tmp 0] [lindex $tmp 1]]
      } else {
          set argl [lreplace $argl $ind $end $tmp]
      }
    }
    return $argl
}

# Walk over the arguments and make sure any .java file
# we are going to pass to the compiler actually
# exists. Don't try to find files that look like
# a glob pattern match because those need to be
# passed into the compiler for pattern expansion under
# windows. We need to do this so that a missing file
# will not be reported as FAIL.

proc _verify_files_exist { argl } {
    foreach arg $argl {
        if {![regexp {(\*|\?|\[|\])} $arg] &&
                [string match *.java $arg]} {
            if {! [file exists $arg]} {
                error "source file \"$arg\" could not be found."
            }
        }
    }
}

# The implementation of exec that is shared between compile
# and compile_and_run. This method will run the given code
# and check for core dumps, output to stderr, n stuff.

proc _exec_impl { prefix prog prog_flags prog_args } {
    global last_compile_warn_or_error last_compile_output

    # Clean up any old tmp files or core files.

    delete $prefix.err $prefix.warn $prefix.stack
    delete core *.stackdump

    if {[catch {
# It might be interesting to open a non blocking pipe instead
# that way we could kill a process if it was taking too long.

        set cmd "exec $prog $prog_flags $prog_args > exec.out 2> exec.err"

        if {[info exists ::env(JACKS_EXEC_DEBUG)]} {
            puts stderr "JACKS_EXEC_DEBUG: $cmd"
        }

        eval $cmd
    } err]} {
        #puts "err is \"$err\""

# FIXME: suck the error into the variable now, instead of using a file ???
        set last_compile_warn_or_error $prefix.err
        file rename -force exec.err $last_compile_warn_or_error
        delete exec.out

        # Check for a stackdump file under Cygwin
        set sd [glob -nocomplain *.stackdump]
        if {[llength $sd] > 0} {
            if {[llength $sd] != 1} {
                error "found multiple stackdump files \{$sd\}"
            }
            file rename $sd $prefix.stack
            return COREDUMP
        }

        # Check for a core file and try to get a backtrace from it
        if {[file exists core]} {
            set gdb [_get_gdb_exe]
            if {$gdb == {}} {
                # Can't get a backtrace and can't keep huge core
                # file, just delete it and create a dummy file.
                delete core
                saveas $prefix.stack "gdb not found : could not generate stacktrace"
            } else {
                saveas gdb.run bt
                if {[catch {exec $gdb $prog core --batch --command=gdb.run > $prefix.stack} err]} {
                    error "could not get backtrace : \"$err\""
                }
                delete gdb.run core
            }
            return COREDUMP
        }
	return FAIL
    } else {
        set last_compile_output {}
        if {[file size exec.out] != 0} {
            set last_compile_output $prefix.out
            file rename -force exec.out $last_compile_output
        } else {
            delete exec.out
        }

        set last_compile_warn_or_error {}
	if {[file size exec.err] != 0} {
	    set last_compile_warn_or_error $prefix.warn
	    file rename -force exec.err $last_compile_warn_or_error
	    return WARN
	} else {
	    delete exec.err
	    return PASS
	}
   }
}

# Return {} is gdb is not available, return the name of the
# gdb executable to use if one is available.

proc _get_gdb_exe {} {
    global GDB _GDB_OK
    if {[info exists _GDB_OK]} {
        return $GDB
    }
    if {[info exists GDB]} {
        if {![catch {exec $GDB --version} err]} {
            set _GDB_OK 1
            return $GDB
        }
    }
    return {}
}


# This is a little utility proc use to check the output of
# the error file generated by the last call to "compile"

proc match_err_or_warn { pattern } {
    global last_compile_warn_or_error

    if {$last_compile_warn_or_error == {}} {
	return 0
    } else {
	set buff [cat $last_compile_warn_or_error]
	return [string match $pattern $buff]
    }
}

# This little utility proc will return the string OK
# if PASS or WARN is passed in as the result, otherwise
# it will return the passed in test result

proc ok_pass_or_warn { result } {
    if {$result == "PASS" || $result == "WARN"} {
        return OK
    } else {
        return $result
    }
}

# FIXME: Doc this method for public use

proc cleanup { dir_or_file } {
    _append_to_file .cleanup $dir_or_file
}

# Append a line to the end of a file
proc _append_to_file { file line } {
    set fd [open $file a+]
    puts $fd $line
    close $fd
}

# Delete a file name, do not generate an error if it does not exist
proc delete { args } {

    foreach arg $args {
        if {[regexp {(\*|\?|\[|\])} $arg]} {
            foreach match [glob -nocomplain $arg] {
                #puts "deleting matched file \"$match\""
                file delete -force $match
            }
        } else {
            if {[file exists $arg]} {
                #puts "deleting arg \"$arg\""
                file delete -force $arg
            }
        }
    }

    return
}

proc _dotests { } {
    set testfile tests.tcl

    if {[file exists $testfile]} {
        # Print the test file name without
        # the home prefix. When run from
        # /.../jacks/tests/7/5/2 we
        # will print "Running tests/7/5/2/..."

        set curdir [file split [pwd]]
        set homesplit [file split $::JACKS_HOME]
        set homesplit_len [llength $homesplit]

        #puts "curdir is \{$curdir\}"
        #puts "homesplit is \{$homesplit\}, len is $homesplit_len"

        set shortdir [lrange $curdir $homesplit_len end]

        if {$shortdir == {}} {
            error "test should not be run out of main jacks directory!"
        }

        #puts "shortdir is \{$shortdir\}"

        puts [eval {file join} $shortdir]
        _rm_classes
        #_rm_generated_files
        source $testfile
    }

    foreach subdir [subdirectories] {
        cd $subdir
        _dotests
    }

}


# -------------- End of Utility proc definitions ---------------------



# set JACKS_HOME to the directory where jacks.tcl lives.
set JACKS_HOME [file dirname [info script]]

# Expand . out the the current working directory

if {$JACKS_HOME == "."} {
    set JACKS_HOME [pwd]
}

if {! [file exists [file join $JACKS_HOME jacks.tcl]]} {
  error "Can not find jacks.tcl in directory $JACKS_HOME"
}

# Load up our utility commands.
set f [file join $JACKS_HOME utils.tcl]
if {! [file exists $f]} {
  error "Can not find utils.tcl in directory $JACKS_HOME"
} else {
  source $f
}

set compiler_paths {}
set compiler_names {}

foreach fname [glob [file join $JACKS_HOME *_setup]] {
    # See if we are meant to ignore this configuration.
    if {[file exists $fname.ignore]} {
        continue
    }

    set short_name [file tail $fname]
    # grab "jikes" from a file named "jikes_setup"
    set compiler [lindex [split $short_name _] 0]

    lappend compiler_paths $fname
    lappend compiler_names $compiler
}

# Create available compiler description string like "javac, jikes, gcj"
set compiler_desc [join $compiler_names ", "]

set argc [llength $argv]
regsub {\r} $argv \n argv
regsub {\n} $argv {} argv

#puts "argc is $argc"
#puts "argv is $argv"


if {$argc == 0 || $argc > 2} {
    set tail [file tail $argv0]
    puts stderr "usage :"
    puts stderr "\t $tail compiler : compiler is one of $compiler_desc"
    puts stderr "\t $tail list : run tests with each compiler named in list"
    puts stderr "\t $tail all : run tests with all the supported compilers"
    puts stderr "\t $tail compiler|list|all pattern : run tests matching pattern"
    puts stderr "\t $tail clean : remove files created by previous run"
    puts stderr "\t $tail docgen|loggen ?compiler? : regenerate logs or docs"
    exit 1
} else {
    set requested_compiler [lindex $argv 0]
    if {[llength $argv] == 2} {
        set pattern [lindex $argv 1]
    } else {
        set pattern {}
    }

    # Check for any "special" options that can be passed instead a plain
    # compiler name

    if {$requested_compiler == "clean"} {
        if {$pattern != {}} {error "you can not pass an argument to clean"}
        if {$JACKS_HOME == [pwd]} { cd tests }
        doclean
        exit 0
    }

    if {$requested_compiler == "docgen"} {
        if {$pattern != {}} {error "you can not pass an argument to docgen"}
        cd [file join $JACKS_HOME docs]
        exec [info nameofexecutable] docgen.tcl >@ stdout 2>@ stderr
        exit 0
    }

    if {$requested_compiler == "loggen"} {
        cd [file join $JACKS_HOME logging]
        if {$pattern != {}} {
            set pattern_logs [list]
            foreach pat $pattern {
                lappend pattern_logs $pat.log
            }
            eval {exec [info nameofexecutable] cvs_changes.tcl} $pattern_logs >@ stdout 2>@ stderr
        } else {
            exec [info nameofexecutable] cvs_changes.tcl >@ stdout 2>@ stderr
        }
        exit 0
    }

    set requested_compilers [list]

    # The string "all" can be passed to indicate that the given tests
    # should be run for each of the supported compiler configurations.

    if {$requested_compiler == "all"} {
        set requested_compilers $compiler_names
    }

    # A compiler name like "javac jikes" means the user wanted
    # to run with each of the compilers passed in the second argument.

    set tmp [split $requested_compiler " "]

    if {[llength $tmp] > 1} {
        foreach compiler $tmp {
            # Split turns multiple spaces into empty list elements
            if {$compiler != {}} {
                lappend requested_compilers $compiler
            }
	}
    }

    # If the user wanted to run multiple compilers with a given pattern,
    # we invoke this script again passing a single compiler at a time.

    if {[llength $requested_compilers] > 0} {
        foreach compiler $requested_compilers {
            if {[catch {exec [info nameofexecutable] [info script] $compiler $pattern >@ stdout 2>@ stderr} err]} {
                puts stderr "Problem exec'ing $compiler:"
                puts stderr $err
                exit 1
            }
        }
        exit 0
    }

    # If the requested compile in not in the supported list, error out
    set ind [lsearch $compiler_names $requested_compiler]

    if {$ind == -1} {
        puts stderr "no support for \"$requested_compiler\", must be one of $compiler_desc"
        exit 1
    }

    # Note that we define constraint defaults here so that a
    # compiler that does not support a given feature can
    # turn it off in the setup file. This is better than
    # forcing every compiler setup file to set the defaults.
    namespace eval tcltest {
        set testConstraints(atfiles) 1
        set testConstraints(runtime) 1
    }

    # Source the setup file for the given compiler configuration

    set setup [lindex $compiler_paths $ind]

    source $setup

    # Each of the following variables must have been defined in the setup file.

    foreach required {JAVA_CLASSPATH JAVAC JAVAC_FLAGS} {
        if {! [info exists $required]} {
            puts stderr "Variable $required must be defined in $setup"
	    exit 1
        }
    }

    # These variables are optional, a Java runtime is not required to run compile tests!

    foreach optional {JAVA JAVA_FLAGS} {
        if {! [info exists $optional]} {
            set $optional ""
        }
    }

    # Make sure that both JAVAC and JAVA are fully qualified
    # path names, that they actually exist, that they are
    # regular files, and that they are executable.
    
    foreach prog [list $JAVAC $JAVA] {
	set err {}
        if {$prog == ""} {continue} ; # because JAVA is optional

	if {[file pathtype $prog] != "absolute"} {
	    set err "$prog is not an fully qualified path name."
	} elseif {![file exists $prog]} {
	    set err "$prog does not exist."
	} elseif {![file isfile $prog]} {
	    set err "$prog is not a regular file."
	}

	if {$err != {}} {
	    puts stderr "Configuration error in [file tail $setup]:"
	    puts stderr $err
	    exit 1
	}
    }

    puts "\nTesting $requested_compiler"

    puts "Working directory is [pwd]"
    if {$JAVA_CLASSPATH != ""} {
        puts "CLASSPATH = ${JAVA_CLASSPATH}"
    }
    puts "compile = $JAVAC $JAVAC_FLAGS ..."
    if {$JAVA != ""} {
        puts "run     = $JAVA $JAVA_FLAGS ..."
    }

    # Run a sanity check of the compiler/runtime setup. We need to do this
    # before running the main tests so that a bad configuration does not
    # generate a run of all failures (that can be confusing).
    # Skip this sanity check if there is a ..._setup.checked file.

    if {! [file exists $setup.checked]} {
	set result [compile [saveas CompilerCheck.java "class CompilerCheck {}"]]

	if {$result != "PASS"} {
	    puts stderr "Compiler Problem: $JAVAC $JAVAC_FLAGS CompilerCheck.java"
            puts stderr [cat $last_compile_warn_or_error]
            puts stderr ""
            puts stderr "The most likely cause of this problem is an incorrect"
            puts stderr "setting for the JAVAC, JAVAC_FLAGS, or JAVA_CLASSPATH"
            puts stderr "variables defined in [file tail $setup]."
            puts stderr ""
            puts stderr "It is possible to disregard this error and run the"
            puts stderr "test suite by creating a [file tail $setup].checked file,"
            puts stderr "but this is not recommended."
	    exit 1
	}

        saveas RuntimeCheck.java {
public class RuntimeCheck {
  public static void main(String[] argv) {
    System.out.print("1");
  }
}
        }

        if {$JAVA == ""} {
            set tcltest::testConstraints(runtime) 0
        } else {
            set result [compile_and_run RuntimeCheck.java]
            if {$result != "1"} {
                puts stderr "Skipping Runtime tests:"
                puts stderr $result
                puts stderr ""
                set tcltest::testConstraints(runtime) 0
            }
        }

        delete CompilerCheck.* RuntimeCheck.*
    }

    # Load our custom tcltest implementation, we need to do this after setting argv
    # because we want to use our own command line options not the default ones
    set tcltestfile [file join $JACKS_HOME logging tcltest.tcl]

    if {! [file exists $tcltestfile]} {
        error "Can not find $tcltestfile, you might need to do a \"cvs update -d\""
    }

    # Set up argc, argv. They are used to pass info to tcltest

    set argv {}
    set argc 0
    if {$pattern != {}} {
        lappend argv -match $pattern
        incr argc
    }

    source $tcltestfile

    # Log pass/fail results to a file only when run from the
    # jacks home directory.

    if {$JACKS_HOME == [pwd]} {
        set logfile [file join $JACKS_HOME logging $requested_compiler.log]

        # Create a backup if not using CVS
        if {[file exists ${logfile}] && ![file exists ${logfile}old]
            && ![file isdirectory [file join $JACKS_HOME CVS]]} {
            file rename ${logfile} ${logfile}old
        }

        # Enable logging of test pass/fail results to a file
        tcltest::logfile $logfile

        # Enable automatic crash recovery (requires a log file)
        tcltest::autorecovery 1
    }
}



# Now we actually run the tests in the current directory and subdirectories.
# We expect to get sourced from the directory where the tests will be run.
# If this program is find from the JACKS_HOME directory, then just do a cd
# to the tests dir before we start the recursion.

if {$JACKS_HOME == [pwd]} { cd tests }

_dotests

tcltest::cleanupTests 0 $requested_compiler



