# docgen.tcl : This file will stub out methods used in the
# regression testing framework and then load the tests.
# By doing this, we can automatically generate documentation!


# stub out the tcltest::test method called inside tests.tcl files.

namespace eval tcltest {}

# tcltest::test : a stub that will allow us to scan case info
# and save it out in as documentation.

# Possible formats
# proc { name desc code expected_result }
# proc { name desc constraint code expected_result }

proc tcltest::test { name desc args } {

    if {[llength $args] == 2} {
        foreach {code expected_result} $args break
        set constraint ""
    } elseif {[llength $args] == 3} {
        foreach {constraint code expected_result} $args break
    } else {
        error "wrong \# args: [llength $args]"
    }

    set script "tcltest::test $name \{$desc\} \{
    $code
\} \{$expected_result\}"

    #puts $::logfd $script

# FIXME: We need to doc constraints too!
    testcase $::testfile $name $desc $script $expected_result $constraint
}

# Stub this out in case it is used outside of a tcltest::test call

proc saveas { filename data } {

}

proc subdirectories { } {
    set all [glob -nocomplain *]
    set dirs {}
    foreach file $all {
        if {[file isdirectory $file]} {
            # Skip known bad subdirectories
            switch -- $file {
                CVS -
                RCS {
                    continue
                }
            }
            lappend dirs [file join [pwd] $file]
        }
    }
    return $dirs
}


proc dodoc { } {
    set ::testfile [file join [pwd] tests.tcl]

    if {[file exists $::testfile]} {
        #puts $::logfd "----------> source $::testfile"
        source $::testfile
    }

    foreach subdir [subdirectories] {
        cd $subdir
        dodoc
    }
}

# Write HTML headers to the output file descriptor

proc write_header { } {
    puts $::fd {\
<html>
  <head>
    <title>Jacks Tests</title>
  </head>

  <body bgcolor="white">

  <h1>Jacks Tests</h1>

  <p>

  <IMG SRC="images/jacks.png" ALIGN=bottom ALT="Jacks Logo">
  Testing Java is easy, if you play your cards right.

  <p>
  }
}

proc write_footer { } {
    puts $::fd {

  </body>
</html>
  }
}

proc testcase { testfilename casename desc script expected_result constraint } {
    # Convert absolute path like /jacks/tests/jls/classes/method-declarations
    # to tests/jls/classes/method-declarations

    set test_root_split_len [llength [file split $::qualified_tests_root]]
    set test_file_split [file split $testfilename]

    # Remove common directory file prefix
    set shortname [eval {file join} [lrange $test_file_split $test_root_split_len end-1]]

    # join to get tests/jls/classes/method-declarations
    set root_relative_dirname [file join tests $shortname]

    # join to get ../tests/jls/classes/method-declarations
    set tests_relative_dirname [file join $::unqualified_tests_root $shortname]

    # Get the top level section name that this test case lives in
    set section_name [lindex $test_file_split $test_root_split_len]

    # Link local vars to the array for this section (like jls_directories)
    upvar #0 ::${section_name}_directories dirarr
    upvar #0 ::${section_name}_sections secarr
    upvar #0 ::${section_name}_tests testarr

    # Check the case name. Test cases outside of the main "jls" directory
    # should have the name of the section they are in somewhere in the
    # name of the test case. This ensures that all test case names will
    # be unique.

    if {$section_name != "jls" && ![string match *${section_name}-* $casename]} {
        error "name of test case \"$casename\" in $shortname does not contain the\
            string \"${section_name}-\" as required to describe a test not in the jls section"
    }

    # Extract the section from the test case name. For example, the section for
    # "15.26.2-string-4" would be the string "15.26.2". A test case that
    # does not seem to use a jls section name will use the directory name instead.

    if {! [regexp -- {([0-9|.]+)-} $casename match section]} {
        set section [lindex [file split $root_relative_dirname] end]
    }

    # Check that same section name is not used in two
    # different directories. This is needed so that people do
    # not create tests for a JLS section like 8.4.1 in different dirs!

    if {[info exists dirarr($section)]} {
        set prev_dir [lindex $dirarr($section) 0]
        if {$prev_dir != $root_relative_dirname} {
            error "section \"$section\" listed in\
                  \"$prev_dir\" and then in \"$root_relative_dirname\""
        }
    }

    # Check that the same test case name is not used twice, that would
    # really hose things up
    if {[info exists testarr($casename)]} {
        error "test case name \"$casename\" defined more than once! : $testfilename"
    }

    set dirarr($section) [list $root_relative_dirname $tests_relative_dirname]
    lappend secarr($section) $casename

    set testarr($casename) [list $desc $script $expected_result]
}

# Actually write out the mapping of JLS test cases to directory
# names. We need to do this after all the test files are processed.

# FIXME: make the table on the left hand side only as large as the
# widest text in the left hand side! This seems to sort of work
# with the WIDTH= thing but it is really ugly.

proc write_map_table { array } {
    global $array

    set section_name [lindex [split $array _] 0]

    puts $::fd {
<table COLS=2 BORDER=1 WIDTH="50%">
<tr>
}

    puts $::fd "<td><B>$section_name section</B></td>\n"

    puts $::fd {
<td><B>directory</B></td>
</tr>
}

    set sections [lsort -dictionary [array names $array]]
    foreach section $sections {
        set jacks_relative_dirname [lindex [set ${array}($section)] 0]
        puts $::fd "
<tr>
<td><A HREF=\"#${section}-${section_name}\">${section}</A></td>
<td>$jacks_relative_dirname</td>
</tr>
"
    }

    puts $::fd {
</table>
}

}


# SECTION HEADER (like "jls tests")
# section title
# section anchor name

proc write_section_header { title anchor_name } {

    puts $::fd "
<hr>
<A NAME=\"$anchor_name\">
<H3>$title</H3>
</A>
<p>
"
}

# SECTION INDEX (like 7.4.1)
# section id
# relative directory
# root relative directory
# list of test cases

proc write_section_index { section anchor root_relative_dir tests_relative_dir testnames } {

    puts $::fd "
<hr>
<A NAME=\"${anchor}\"><h3>${section}</h3></A>

Directory :
<A HREF=\"$tests_relative_dir\">
<code>$root_relative_dir</code>
</A>
"

   write_section_bullet_list $testnames
}

proc write_section_bullet_list { testnames } {

    puts $::fd {
<ul>
    }

    foreach testname $testnames {
        puts $::fd "<li><A HREF=\"#$testname\">$testname</A>"
    }

    puts $::fd {
</ul>

<p>

<hr>
}

}

proc write_sections_index { } {
    global section_names

    puts $::fd {
<h3>Section Index:</h3>

<ul>
}

    foreach section [concat $section_names all] {
        puts $::fd "<li>\n<A HREF=\"\#${section}-tests\">\n${section} tests\n</A>\n"
    }

    puts $::fd {
</ul>

<br>
<br>
<br>
}
}


proc write_cases { } {
    global section_names

    foreach section_name $section_names {
        set title "${section_name} tests"
        set anchor "${section_name}-tests"
        set arrayprefix ${section_name}

        write_section_header $title $anchor

        # Link array "sections" to "jls_sections" for example
        upvar #0 ::${arrayprefix}_sections sections
        upvar #0 ::${arrayprefix}_directories directories
        upvar #0 ::${arrayprefix}_tests tests

        # Write out a section index and the test cases for each JLS section

        foreach section [lsort -dictionary [array names sections]] {
            set root_relative_dir [lindex $directories($section) 0]
            set tests_relative_dir [lindex $directories($section) 1]
            set sections_tests $sections($section)
            set anchor ${section}-${section_name}
            write_section_index $section $anchor $root_relative_dir \
                $tests_relative_dir $sections_tests

            foreach casename $sections_tests {
                foreach {desc script result} $tests($casename) break
                write_case $casename $desc $result $script $tests_relative_dir
            }
        }
    }
}


# EACH CASE
# test id
# description
# source files
# expected result
# entire test in a string format
# Inline each source file.

proc write_case { casename desc result script tests_relative_dir } {

    puts $::fd "

<A NAME=\"$casename\"><b>Test Case:</b> $casename</A><br>

<BLOCKQUOTE>
$desc

<p>
<b>Expected Result:</b> $result<br>

<p>

<b>Regression Test:</b>

<CODE>
<PRE>
$script
</PRE>
</CODE>
</BLOCKQUOTE>
"
}







proc write_all_cases_index { } {
    global section_names

    set cases [list]

    # We want to list the tests in the order they are declared, so
    # loop over the sections and get the test list

    foreach section $section_names {
        set array ::${section}_sections
        foreach section [lsort -dictionary [array names $array]] {
            foreach case [set ${array}($section)] {
                lappend cases $case
	    }
        }
    }

    puts $::fd {
<hr>
<A NAME="all-tests">
}

    puts $::fd "<H3>Index of all [llength $cases] tests</H3>"

    puts $::fd {
</A>

<hr>

<ul>
}

    write_section_bullet_list $cases
}

# Record each of the top level section names.
# Some examples would be "jls", "runtime", and so on.

proc find_section_names { } {
    global section_names

    foreach match [glob *] {
        if {[file isdirectory $match] && $match != "CVS"} {
            lappend sections $match
        }
    }

    # Order the sections

    foreach required_section {jls runtime non-jls} {
        set ind [lsearch -exact $sections $required_section]
        if {$ind == -1} {
            error "jls section directory not found in \"[pwd]\""
        }
        set sections [lreplace $sections $ind $ind]
        lappend section_names $required_section
    }

    foreach optional_section [lsort -dictionary $sections] {
        lappend section_names $optional_section
    }

    set section_names
}



# -----------------------------------------------------------



# Make sure we are running in the docs subdirectory
if {! [file exists docgen.tcl]} {
  puts stderr "docgen.tcl must be run from the docs subdirectory"
  exit 1
}

# Open up a generic log file where
# we dump everything we find. This
# is only used for debugging.

#set logfd [open docgen.log w]

# Open up the JLS -> Jacks generated map file

set fd [open tests.html w]

# Write out the document header
write_header

# cd to the root dir for the tests

set unqualified_tests_root [file join .. tests]
set qualified_docs_root [pwd]

cd $unqualified_tests_root
set qualified_tests_root [pwd]


# Make sure any generated files from tests are cleaned up
# before running the doc generation script.
exec [info nameofexecutable] [file join .. jacks.tcl] clean

# Figure out the section names and order them properly.
find_section_names

# recurse into directories, saving test case info as we go
dodoc

# Write out the map tables
foreach section $section_names {
    set array_name ${section}_directories
    write_map_table $array_name
}

# Add bullet list with links to each section.
write_sections_index

# Add each of the test cases, one section at a time.
write_cases

# Add bullet list with links to every test
write_all_cases_index

# Write out the document footer
write_footer

close $fd
#close $logfd


# Dump variables
if 0 {
proc parray_if_exists { array } {
    if {[uplevel 1 array exists $array]} {
        uplevel 1 parray $array
        puts ""
    }
}

foreach section $section_names {
    parray_if_exists ${section}_sections
    parray_if_exists ${section}_directories
    parray_if_exists ${section}_tests
}

}
