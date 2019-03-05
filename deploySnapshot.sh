VERSION=$(./mvnw -q -Dexec.executable=echo -Dexec.args='${project.version}' -N exec:exec)

./mvnw --settings travis-settings.xml deploy -DskipTests -Pci,generate-p2

if [ $? -eq 0 ]
then
  echo "Deployed to Maven repository."
  echo "Now uploading the plug-ins to the p2 repo on Artifactory..."
  BINTRAY_URL=https://api.bintray.com/content/lsp4xml/p2/lsp4xml
  curl -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" -H 'X-Explode-Archive-Atomic: true' -H 'X-Explode-Archive: true' https://lsp4xml.jfrog.io/lsp4xml/generic-local/p2/lsp4xml/$VERSION  --upload-file org.eclipse.lsp4xml/target/org.eclipse.lsp4xml-p2repo.zip
else
  echo "Could not deploy to Maven repository"
fi
