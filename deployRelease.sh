VERSION=$TRAVIS_TAG
echo "Setting release version to tag $VERSION"
./mvnw versions:set -DnewVersion=$VERSION

./mvnw --settings travis-settings.xml deploy -DskipTests -Pci,generate-p2
if [ $? -eq 0 ]
then
  echo "Deployed to Maven repository."
  echo "Now uploading the plug-ins to p2 repo on Bintray..."
  BINTRAY_URL=https://api.bintray.com/content/lsp4xml/p2/lsp4xml
  curl -T org.eclipse.lsp4xml/target/org.eclipse.lsp4xml-p2repo.zip	-u ${RELEASES_USERNAME}:${RELEASES_PASSWORD} -H "X-Bintray-Version:${VERSION}" -H "X-Bintray-Explode:1"  ${BINTRAY_URL}/${TRAVIS_TAG}/
else
  echo "Could not deploy to Maven repository"
fi
