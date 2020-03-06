pipeline{
  agent any
  environment {
    MAVEN_HOME = '$WORKSPACE/.m2/'
    MAVEN_USER_HOME = '$MAVEN_HOME'
  }
  stages{
      stage("Maven Build"){
          steps {
              withMaven() {
                sh './mvnw clean verify -B'
              }
          }
      }
  }
}