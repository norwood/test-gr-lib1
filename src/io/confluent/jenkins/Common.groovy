def notifySlack2() {
  switch (currentBuild.currentResult) {
    case 'SUCCESS':
      if (currentBuild.previousBuild != null && currentBuild.previousBuild.currentResult != 'SUCCESS') {
        slackSend(channel: '#clients-eng', color: 'good', message: "${env.JOB_NAME} - #[${env.BUILD_NUMBER}] Success <${env.BUILD_URL}|(Open)>", teamDomain: 'confluent')
      }
      break;
    case 'UNSTABLE':
      slackSend(channel: '#clients-eng', color: 'YELLOW', message: "${env.JOB_NAME} - #[${env.BUILD_NUMBER}] Unstable <${env.BUILD_URL}|(Open)>", teamDomain: 'confluent')
      break;
    case 'FAILURE':
    default:
      slackSend(channel: '#clients-eng', color: 'bad', message: "${env.JOB_NAME} - #[${env.BUILD_NUMBER}] Failure <${env.BUILD_URL}|(Open)>", teamDomain: 'confluent')
      break;
  }
}