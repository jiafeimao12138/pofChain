!#bin/bash
cd afl_testfiles
./deletefiles.sh

afl-fuzz -i fuzz_in/ -o fuzz_out ./objfiles/string_length1
