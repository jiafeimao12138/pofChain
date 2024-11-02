!#bin/bash
cd AFL
./deletefiles.sh

afl-fuzz -i fuzz_in/ -o fuzz_out ./afl_testfiles/objfiles/string_length
