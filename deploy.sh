#echo "Setting release version"
#./mvnw versions:set -DnewVersion=$TRAVIS_TAG

echo "Deploying to Maven repo"
./mvnw --settings travis-settings.xml deploy -DskipTests