echo "Setting bogus version"
./mvnw versions:set -DnewVersion=0.0.0

echo "Deploying to Maven repo"
./mvnw --settings travis-settings.xml deploy -DskipTests