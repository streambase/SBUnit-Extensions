#! /bin/bash

# build the relevant javadoc and test stuff
# then push it over to the git-hub pages section

ant
hg up gh-pages
cp -r build/java-doc .
cp -r build/test-doc .
hg ci -m 'updating the java doc and test-doc'
hg up master
