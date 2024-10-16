#!/usr/bin/expect
set timeout 1
spawn su root
expect "Password:"
send "xjtuse12138\r"
expect "$ "
send "echo core >/proc/sys/kernel/core_pattern\r"

expect "$ "
send "exit\r"
expect eof

