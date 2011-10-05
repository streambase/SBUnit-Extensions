#! /bin/bash

ant
hg up gh-pages
cp -r build/java-doc .
cp -r build/test-doc .
hg ci -m 'updating the java doc and test-doc'
hg up master
