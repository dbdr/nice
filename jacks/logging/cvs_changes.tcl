# This script will use the CVS to scan the .log file
# looking for changes in the status of test cases.
# It will generate a new $compiler.changes file
# for each .log file that is found. Other program
# should read the .changes file to figure out
# how test results have changed over time.

# The format for an entry in the .changes file is (without the #)
# DATE { TOTALS } {
# TEST_ID TRANSITION
# }
#
# TOTALS is a list of transitions for the test case summary info.
#
# For example:
#
# Passed {5 10} Failed {20 15}
#
# The above would indicate that 5 test cases that were failing
# now pass.
#
# Skipped {0 1} Total {75 76}
#
# The above would indicate that you added a new test case
# that is currently being skipped because of a test constranint.
#
#
# TEST_ID is simply the name of a test case, like "8.1.1.1-7"
#
# TRANSITION is a list of state changes for a test case.
#
# For example:
#
# {FAILED PASSED}
#
# The above would indicate that the named test was failing
# but now passes.
#
# {PASSED FAILED}
#
# The above would indicate a regression, the test was
# passing but now it fails.
#
# A single state entry of PASSED means a new passing test
# was added. A single state entry of FAILED means a
# new test was added and it currently fails.
#
# If a test is removed, the test state will transition
# from PASSED or FAILED to {}.

package require Tcl 8.3

proc debug { str } {
#    puts $str
}

proc cvs { args } {
    debug "now to run : cvs $args"
    if {[catch {eval {exec cvs} $args} output]} {
        # If there was an error, make sure the
        # output of the exec does not contain
        # a stupid "child process exited abnormally"
        # string on the last line
        set lines [split $output \n]
        set last [lindex $lines end]
        debug "last is \"$last\""
        if {$last == "child process exited abnormally"} {
            set output [join [lrange $lines 0 end-1] \n]
        }
        error $output
    }
    return $output
}


# Return a list of revision identifiers in most recent to
# least recent order

proc get_revisions_numbers { file } {
    set logdata [cvs log $file]

    debug "log data is"
    debug $logdata

    # Pull all the revision numbers out of the log file

    set revisions [regexp -all -inline {revision [0-9|\.]+} $logdata]

    set revs [list]

    foreach revision $revisions {
	foreach {dummy rev} $revision break
	debug "rev is $rev"
	lappend revs $rev
    }
    return $revs
}



# Given a file name and a list of revision ids
# this method will return a list of the revision
# and the actual data from that revision.
# {current {DATA} 1.7 {DATA} 1.6 {DATA} ...}

proc get_revision_diffs { file revs } {

    set rev_info [list]

    # First, see if there is a difference between the
    # current file and the last revision that was checked in.

    if {[catch {cvs diff -u $file} data]} {
        debug "diffing current found a local change"
        lappend rev_info [list current $data]
    } else {
        puts "$file has not changed"
    }

    # Go over the diffs starting from the
    # most and goint to the least recent

    set last [lindex $revs 0]

    foreach rev [lrange $revs 1 end] {
	debug "diffing $file $rev -> $last"
	catch {cvs diff -u -r $rev -r $last $file} data
	lappend rev_info [list $last $data]
	set last $rev
    }

    return $rev_info
}





# Given a buffer full of diff data, extract the info
# related to when the cvs revision was made, what
# test were effected and so on. This method depends
# on the data format of the log files.

proc get_results_from_diff { diff_data } {

    # Skip all the lines up to the first diff line indicator

    set lines [split $diff_data \n]
    set lines_len [llength $lines]

    # The actual lines that changed!
    set diff_lines [list]

    # Trim all the lines before the first line that start with @@

    for {set i 0} {$i < $lines_len} {incr i} {
	set line [lindex $lines $i]

	debug "line(1) is \"$line\""

	set diff_date_pat {^\+\+\+[ |\t]+.+[ |\t]+([0-9]+)/([0-9]+)/([0-9]+).+$}
	if {[regexp $diff_date_pat $line whole year month date]} {
	    set diff_date "$year-$month-$date"
	}

	if {[string first @@ $line] == 0} {
	    incr i
	    break
	}
    }

    # Make sure we found a date in the diff output
    if {! [info exists diff_date]} {
	error "no date found in diff output"
    } else {
        debug "rev date is $diff_date"
    }

    # Now trim all the lines that do not start with a + or -

    for {} {$i < $lines_len} {incr i} {
	set line [lindex $lines $i]
	debug "line(2) is \"$line\""
	set first_char [string index $line 0]
	if {$first_char == "-" || $first_char == "+"} {
	    lappend diff_lines $line
	}
    }

    debug "actual diff lines are:"
    foreach line $diff_lines {
	debug $line
    }


    # Accumulate the RESULT changes into the results array
    # Accumulate misc totals into the totals array

    # Keep test cases ids in declared order
    set declared_order [list]

    foreach line $diff_lines {

	debug "line(3) is \"$line\""

	set result_pattern {^(\-|\+)RESULT (.+) (.+)$}

	if {[regexp $result_pattern $line whole pm case result]} {
	    # pm is + or -
	    # case is the id for the regression test
	    # result is PASSED or FAILED

	    # add to or set the state transition buffer for the given test
	    # only one entry per test case name is added to declared_order

	    set exists [info exists results($case)]

	    if {$pm == "+"} {
		lappend results($case) $result
                if {!$exists} {
                    lappend declared_order $case
                }
	    } elseif {$pm == "-"} {
		if {$exists} {
		    set results($case) [list $result $results($case)]
                } else {
		    set results($case) $result
                    lappend declared_order $case
                    set possible_delete($case) ""
		}
	    } else {
		error "pm is \"$pm\", it should have been + or -"
	    }

	    debug "results($case) is now {$results($case)}"

	    continue
	}

	set totals_pattern {^(\-|\+)(Total|Passed|Skipped|Failed) ([0-9]+)$}

	if {[regexp $totals_pattern $line whole pm type num]} {
	    # pm is + or -
	    # type is the name of the field (like Total)
	    # num is the integer value of the filed

	    if {$pm == "+"} {
		lappend totals($type) $num
	    } elseif {$pm == "-"} {
		if {! [info exists totals($type)]} {
		    set totals($type) $num
		} else {
		    set totals($type) [list $num $totals($type)]
		}
	    } else {
		error "pm is \"$pm\", it should have been + or -"
	    }
	}
    }

    # Once all of the transitions have been processed, we
    # need to validate the transitions to protect against
    # goofy things like a transition of {PASSED PASSED}.
    # This transition is not legal but could happen
    # if test cases were run out of order and you ended
    # up with a diff like the following:
    # diff -u r1 r2
    # -RESULT ID PASSED
    # +RESULT ID PASSED

    set new_declared_order [list]

    foreach test $declared_order {
        # Check that a given test name appears only once
        # in the declared_order list.
        if {[info exists declared_order_table($test)]} {
            error "test case name \"$test\" found in declared_order more than once"
        }
        set declared_order_table($test) ""

        # Check for the case of a single transition like "-RESULT ID PASSED"
        # that should not results in {PASSED} as in a test case was added.

        if {[info exists possible_delete($test)] &&
            [llength $results($test)] == 1} {
            debug "test case delete found for $test"
            lappend results($test) {}
        }

        set transition $results($test)

        if {[llength $transition] == 0 ||
            [llength $transition] > 2} {
            error "invalid transition for test id \"$test\" : \{$transition\}"
	}

        if {[llength $transition] == 2} {
            set initial [lindex $transition 0]
            set final [lindex $transition 1]

            if {$initial == $final} {
                continue
            }
        }

        lappend new_declared_order $test
    }

    # If we dropped any cases by not adding them to the new_declared_order
    # then will not show up in the list below, the total would not be effected
    set declared_order $new_declared_order

    # If there were no actual test case changes, the just
    # return {} instead of a {DATE {} {}}

    if {[llength $declared_order] == 0} {
        return
    }

    set results_list [list]
    foreach test $declared_order {
        debug "transition: $test \{$results($test)\}"
        lappend results_list [list $test $results($test)]
    }

    return [list $diff_date [array get totals] $results_list]

}




if {[llength $argv] == 0} {
    set logfiles [glob *.log]
} else {
    set logfiles $argv
}

set master_results [list]

foreach file $logfiles {
    puts "reading log data for $file"
    set revs [get_revisions_numbers $file]
    set rev_diffs [get_revision_diffs $file $revs]

    set rev_results [list]

    foreach rev_d $rev_diffs {
	set rev [lindex $rev_d 0]
	set diff_data [lindex $rev_d 1]

        debug "get_results_from_diff for diff $rev"
        set results [get_results_from_diff $diff_data]
        if {$results != {}} {
            lappend rev_results $results
        }
    }
    lappend master_results [list $file $rev_results]
}

foreach file_revr $master_results {
    set file [lindex $file_revr 0]
    set rev_results [lindex $file_revr 1]

    # Write data to a "changes" file, jikes.log -> jikes.changes
    set file_prefix [lindex [split $file .] 0]
    set changes $file_prefix.changes
    set fd [open $changes w]

    foreach result $rev_results {
        foreach {date totals tests} $result break
        puts $fd "$date \{$totals\} \{"
        foreach test $tests {
            puts $fd $test
        }
        puts $fd "\}"
    }

    close $fd

    puts "changes for $file written to $changes"
}









if 0 {


# VALIDATE DIFF DATA ????

# Make sure the totals jive, if five more tests were added
# then the total should have increased by five.

set tests_changed [array size results]

if {[info exists results(Total)]} {
    foreach {pre post} $results(Total) break
    set change [expr {abs($pre - $post)}]
    if {$change != $num_tests_changed} {
        error "Total change was $change but only $tests_changed test were found"
    }
}


# We could have added new tests that Passed or
# tests could have transitioned from FAILED to PASSED
# The same goes for Failed

if {[info exists results(Passed)]} {
    foreach {pre post} $results(Passed) break
    set change [expr {abs($pre - $post)}]
    if {$change != $num_tests_changed} {
        error "Passed change was $change but only $tests_changed test were found"
    }
}

if {[info exists results(Failed)]} {
    foreach {pre post} $results(Failed) break
    set change [expr {abs($pre - $post)}]
    if {$change != $num_tests_changed} {
        error "Failed change was $change but only $tests_changed test were found"
    }
}

# Not sure what to do with Skipped yet.

if {[info exists results(Skipped)]} {
    foreach {pre post} $results(Skipped) break
    set change [expr {abs($pre - $post)}]
    if {$change != $num_tests_changed} {
        error "Skipped change was $change but only $tests_changed test were found"
    }
}





# TEST CASES!!


package require tcltest
namespace import -force tcltest::test

test add-1 { test added } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2000/01/03 01:00:01
@@ -1,6 +1,3 @@
+started test-1
+finished test-1
+RESULT jikes-1 PASSED
}
} {2000-01-03 {} {{jikes-1 PASSED}}}


test add-2 { two tests added } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2000/01/03 01:00:01
@@ -1,6 +1,3 @@
+started test-1
+finished test-1
+RESULT jikes-1 PASSED
+started test-2
+finished test-2
+RESULT jikes-2 PASSED
}
} {2000-01-03 {} {{jikes-1 PASSED} {jikes-2 PASSED}}}



test remove-1 { test removed } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2000/01/03 01:00:01
@@ -1,6 +1,3 @@
-started test-1
-finished test-1
-RESULT jikes-1 PASSED
}
} {2000-01-03 {} {{jikes-1 {PASSED {}}}}}


test remove-2 { test removed } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2000/01/03 01:00:01
@@ -1,6 +1,3 @@
-started test-1
-finished test-1
-RESULT jikes-1 FAILED
}
} {2000-01-03 {} {{jikes-1 {FAILED {}}}}}






test result-1 { test result changes from PASSED to FAILED } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2001/01/03 01:00:01
@@ -1,6 +1,3 @@
-RESULT jikes-1 PASSED
+RESULT jikes-1 FAILED
}
} {2001-01-03 {} {{jikes-1 {PASSED FAILED}}}}

test result-2 { test result changes from FAILED to PASSED } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2001/01/03 01:00:01
@@ -1,6 +1,3 @@
-RESULT jikes-1 FAILED
+RESULT jikes-1 PASSED
}
} {2001-01-03 {} {{jikes-1 {FAILED PASSED}}}}

test result-3 { test result changes from FAILED to PASSED } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2001/01/03 01:00:01
@@ -1,6 +1,3 @@
+RESULT jikes-1 PASSED
-RESULT jikes-1 FAILED
}
} {2001-01-03 {} {{jikes-1 {FAILED PASSED}}}}




test goofy-result-1 { test result change from PASSED to PASSED
        must be ignored, it could be caused by tests
        that run in different order on different systems } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2001/01/03 01:00:01
@@ -1,6 +1,3 @@
-started jikes-1
-finished jikes-1
-RESULT jikes-1 PASSED
SOME JUNK
+started jikes-1
+finished jikes-1
+RESULT jikes-1 PASSED
}
} {}

test goofy-result-2 { a result should not be deleted twice } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2001/01/03 01:00:01
@@ -1,6 +1,3 @@
-started jikes-1
-finished jikes-1
-RESULT jikes-1 PASSED
SOME JUNK
-started jikes-1
-finished jikes-1
-RESULT jikes-1 PASSED
}
} {}



test totals-1 { test added, totals changed } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2000/01/03 01:00:01
@@ -1,6 +1,3 @@
+started test-1
+finished test-1
+RESULT jikes-1 PASSED
@@ -2,6 +2,3 @@
 finished 3.10.5-runtime-2
 RESULT 3.10.5-runtime-2 FAILED
-Total 3
+Total 4
-Passed 2
+Passed 3
 Skipped 0
}
} {2000-01-03 {Passed {2 3} Total {3 4}} {{jikes-1 PASSED}}}


test totals-2 { failing case fixed, totals changed } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2000/01/03 01:00:01
@@ -1,6 +1,3 @@
-started test-1
-finished test-1
-RESULT jikes-1 FAILED
+started test-1
+finished test-1
+RESULT jikes-1 PASSED
@@ -2,6 +2,3 @@
-Passed 2
+Passed 3
-Failed 3
+Failed 2
 Skipped 0
}
} {2000-01-03 {Passed {2 3} Failed {3 2}} {{jikes-1 {FAILED PASSED}}}}



# Note, there does not appear to be any way to tell a
# removed test and a skipped test apart.

test totals-2 { test skipped } {
    get_results_from_diff {
--- foo.log   2000/01/02 01:00:00     1.1
+++ foo.log   2000/01/03 01:00:01
@@ -1,6 +1,3 @@
-RESULT jikes-1 FAILED
@@ -2,6 +2,3 @@
-Skipped 0
+Skipped 1
}
} {2000-01-03 {Skipped {0 1}} {{jikes-1 {FAILED {}}}}}



}
