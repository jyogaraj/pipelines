pipeline {
  agent any

  environment {
    def gitRepo = "https://github.com/iproperty/simple-sinatra-app.git"
    def gitBranch = "master"
    def devServerName = ''
    def devServerDeployUserName = "root"
    def appName = "demoapp"
    def emailRecipients = "yogaraj.jawahar@gmail.com"
  }

  options {
    timestamps()
    disableConcurrentBuilds()
    timeout(time: 25, unit: 'MINUTES')
    buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5'))
  }

  stages {
    stage('Prepare'){
      steps{
        cleanWs()
        script{
          properties([
            pipelineTriggers([pollSCM('H/5 * * * *')])
          ])
          
          checkout([
            $class: 'GitSCM',
            branches: [[name: gitBranch]], 
            doGenerateSubmoduleConfigurations: false,
            poll: true,
            extensions: [[$class: 'CleanCheckout']], 
            submoduleCfg: [], 
            userRemoteConfigs: [[url: gitRepo ]]
          ])

          writeFile file: 'Dockerfile', text: '''FROM ruby:latest
          MAINTAINER yogaraj.jawahar@gmail.com
          RUN apt-get update && apt-get install -y 
          RUN mkdir -p /app
          WORKDIR /app
          COPY Gemfile ./ 
          RUN gem install bundler && bundle install --jobs 20 --retry 5
          COPY . ./
          EXPOSE 9292
          CMD ["bundle", "exec", "rackup", "--host", "0.0.0.0" ]'''
        }
      }
    }

    stage('Build'){
      steps{
        script{
          sh "docker build -t ${appName}_image ."

        }
      }
    }

    stage('codeQuality'){
      steps{
        echo "code quality comes in here."
      }
    }

    stage('deploy'){
      steps{
        script{
          appRespone=sh returnStdout: true, script: "docker ps --filter 'name=${appName}' -q"
          if(appRespone.trim()){
            sh "docker stop ${appRespone}"
          }
          sh """docker run --rm -d -p 80:9292 --name ${appName} ${appName}_image"""

          //Remote server
          /*docker save -o ${appName}_image.tar ${appName}_image
          sh "scp ${appName}_image.tar ${devServerName}:/var/tmp"
          sshagent (credentials: ['deploy-credentials']) {
              sh """ssh -o StrictHostKeyChecking=no  ${devServerName} docker load --input /var/tmp/${appName}_image.tar;response=docker ps --filter 'name=${appName}' -q';if [[ ! -z \$response ]];then docker stop \$response;docker run --rm -d -p 80:9292 --name ${appName} ${appName}_image"""
          }*/
        }
      }
    }
    stage('postvalidations'){
      steps{
        echo "post validations goes here"
      }
    }
  }

  post{
    always{
      echo "send email"
      //emailext(presendScript: presendScriptContent, attachLog: true, compressLog: true, body: bodyContent, mimeType: 'text/html', subject: mailSub, from: no-reply@jenkinsbuild.com, to: emailRecipients)
      //echo "deployment completed\nApplication running @ http://${devServerName}"
    }
  }
}