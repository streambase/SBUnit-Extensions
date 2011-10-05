#! /bin/bash

ant
hg up gh-pages
cp -r build/java-doc .
cp -r build/test-doc .
echo hg ci -m 'updating the java doc and test-doc'
