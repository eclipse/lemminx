if [[ $TRAVIS_TAG =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then 
  echo "Setting release version to tag $TRAVIS_TAG"
  ./mvnw versions:set -DnewVersion=$TRAVIS_TAG
fi

echo "Deploying to Maven repo"
./mvnw --settings travis-settings.xml deploy -DskipTests -Pci