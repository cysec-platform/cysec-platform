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
    stage ('Build') {
      steps {
        sh 'mvn -DskipTests install'
      }
    }
    stage ('Test on JDK8') {
      steps{
        sh 'mvn -DforkCount=0 test jacoco:report'
      }
    }
    
    stage ('Package all') {
      steps {
        sh 'mvn -DskipTests -pl cysec_backend package'
      }
    }
    /*stage('SonarQube analysis') {
      steps {
        //withSonarQubeEnv('SonarQube') {
        withSonarQubeEnv('localhost sonarQube') {
          sh "mvn sonar:sonar"
        }
        sh '/bin/true'
      }
    }*/
    stage('Deploy') {
      steps {
        //fileOperations([
          //fileCopyOperation(includes:"${workspace}/target/smesec-platform.war", targetLocation: "/var/lib/tomcat8/webapps/stage.war")
        //])
        echo "My branch is: ${env.BRANCH_NAME}"
        echo "My branch is: ${BRANCH_NAME}"
        // enable use of if(...)
        //script {
          //if(BRANCH_NAME == 'dev') {
            //sh 'yes | cp -rf cysec_backend/target/smesec-platform.war  /var/lib/tomcat8/webapps/stage.war'
          //}
          //if(BRANCH_NAME == 'integration') {
            //sh 'cp cysec_backend/target/smesec-platform.war  /var/lib/tomcat8/webapps/cysec-eauth.war'
            //sh 'cp smesec-platform.war  /var/lib/tomcat8/webapps/cysec-local.war'
          //}
        //}
      }
    }
  }
  post {
    /*always {
      publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'target/surefire-reports', reportFiles: 'index.html', reportName: 'SMESEC platform Report', reportTitles: 'smesec-platform'])
    }*/
    success {
      archiveArtifacts artifacts: 'cysec_backend/target/*.war,cysec_bridge/target/*.jar', fingerprint: true
      updateGitlabCommitStatus(name: 'build', state: 'success')
    }
    failure {
      updateGitlabCommitStatus(name: 'build', state: 'failed')
    }
  }
}