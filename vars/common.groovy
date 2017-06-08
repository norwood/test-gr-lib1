#!/usr/bin/env groovy

def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // now build, based on the configuration provided
  // variables passed in body are available as ${config.var}
  def slackChannel = ${config.slackChannel} ?: '#github'

  node('docker-openjdk7-wily') {
    stage('Preparation') {
      checkout scm
    }
    stage('Build') {
      sh "mvn --batch-mode -Pjenkins clean install dependency:analyze site"
    }
    stage('Deploy') {
      withMaven(
        globalMavenSettingsConfig: 'jenkins-maven-global-settings',
      ) {
        sh "mvn --batch-mode -Pjenkins -D${env.deployOptions} deploy -DskipTests"
      }
    }
    stage('Notify') {
      junit '**/target/surefire-reports/TEST-*.xml'
      step([$class: 'hudson.plugins.checkstyle.CheckStylePublisher', pattern: '**/target/checkstyle-result.xml', unstableTotalAll:'0'])
      step([$class: 'hudson.plugins.findbugs.FindBugsPublisher', pattern: '**/findbugsXml.xml'])
      archive 'target/*.jar'
      def sendSlackMessage = true
      def slackMessageColor = 'RED'
      def slackMessage = "${env.JOB_NAME} - #[${env.BUILD_NUMBER}] Failure <${env.BUILD_URL}|(Open)>"
      switch (currentBuild.currentResult) {
        case 'SUCCESS':
          slackMessageColor = 'GREEN'
          sendSlackMessage = currentBuild.previousBuild != null && currentBuild.previousBuild.currentResult != 'SUCCESS'
          break;
        case 'UNSTABLE':
          slackMessageColor = 'YELLOW'
          break;
      }
      if (sendSlackMessage) {
        slackSend(
          channel: slackChannel,
          color: slackMessageColor,
          message: "${env.JOB_NAME} - #[${env.BUILD_NUMBER}] ${currentBuild.currentResult} <${env.BUILD_URL}|(Open)>",
          teamDomain: 'confluent'
        )
      }
    }
  }
}
