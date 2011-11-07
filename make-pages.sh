#! /bin/bash

# build the relevant javadoc and test stuff
# then push it over to the git-hub pages section

VERSION=`hg bookmarks | grep '*' | cut -c4-6`
echo Prepping for $VERSION
mkdir -p $VERSION
ant clean && \
ant && \
hg up gh-pages && \
cp -r build/java-doc $VERSION && \
cp -r build/test-doc $VERSION && \
hg addremove && \
hg ci -m "updating the java doc and test-doc for version $VERSION" && \
hg up $VERSION && \
echo "Successfully updated the doc for $VERSION"
