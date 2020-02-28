VERSION=$(./mvnw -q -Dexec.executable=echo -Dexec.args='${project.version}' -N exec:exec)
echo "Deploying version $VERSION"
./mvnw --settings travis-settings.xml deploy -DskipTests -Pci,generate-p2

if [ $? -eq 0 ]
then
  echo "Deployed to Maven repository."
  echo "Now uploading the plug-ins to the p2 repo on Artifactory..."
  BINTRAY_URL=https://api.bintray.com/content/lemminx/p2/lemminx
  curl -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" -H 'X-Explode-Archive-Atomic: true' -H 'X-Explode-Archive: true' https://lemminx.jfrog.io/lemminx/generic-local/p2/lemminx/$VERSION/  --upload-file org.eclipse.lemminx/target/org.eclipse.lemminx-p2repo.zip
else
  echo "Could not deploy to Maven repository"
fi
