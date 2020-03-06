pipeline{
  agent any
  environment {
    MAVEN_HOME = '$WORKSPACE/.m2/'
    MAVEN_USER_HOME = '$MAVEN_HOME'
  }
  stages{
      stage("Maven Build"){
          steps {
              withMaven(jdk: 'jdk1.8.0-latest') {
                sh './mvnw clean verify -B -Pci,generate-p2'
              }
          }
      }
  }
}