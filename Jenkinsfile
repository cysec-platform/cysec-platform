pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr:'50'))
    disableConcurrentBuilds()
    timeout(time: 30, unit: 'MINUTES')
  }
  stages {
    stage ('Initialize') {
      steps {
        script{
            checkout scm
        }
      }
    }
    stage ('Version update') {
      steps {
        script {
          switch(BRANCH_NAME) {
            case 'main':
              sh 'mvn -DskipTests versions:set -DremoveSnapshot=true'
              sh 'mvn -DskipTests versions:use-latest-releases versions:commit'
            case 'integration':
              sh 'mvn -DskipTests versions:use-latest-releases versions:commit'
            default:
              sh 'mvn -DskipTests versions:set -DnextSnapshot=true'
              sh 'mvn -DskipTests versions:use-latest-snapshots versions:commit'
          }
        }
      }
    }
    stage ('Build') {
      steps {
        sh 'mvn -DskipTests clean install'
      }
    }
    stage ('Test on JDK8') {
      steps{
        //sh 'mvn test jacoco:report'
        sh 'mvn test surefire-report:report -Daggregate=true'
      }
    }
    
    stage ('Package all') {
      steps {
        sh 'mvn -DskipTests clean -pl cysec-platform-core package'
      }
    }
    /*stage('SonarQube analysis') {
      steps {
        withSonarQubeEnv('localhost sonarQube') {
          sh "mvn sonar:sonar"
        }
        sh '/bin/true'
      }
    }*/
    stage('Deploy') {
      steps {
        sh 'mvn -DskipTests deploy'
      }
    }
  }
  post {
    always {
      publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'cysec-platform-bridge/target/surefire-reports', reportFiles: 'index.html', reportName: 'CySeC platform Report', reportTitles: 'CySeC-platform'])
    }
    success {
      archiveArtifacts artifacts: 'cysec-platform-core/target/*.war,cysec-platform-bridge/target/*.jar', fingerprint: true
      updateGitlabCommitStatus(name: 'build', state: 'success')
    }
    failure {
      updateGitlabCommitStatus(name: 'build', state: 'failed')
    }
  }
}
