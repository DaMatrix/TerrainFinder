pipeline {
    agent any
    tools {
        git "Default"
        maven "Maven 3"
        jdk "jdk8"
    }
    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '5'))
    }
    stages {
        stage("Build") {
            steps {
                sh "mvn clean package"
            }
            post {
                success {
                    sh "bash ./add_jar_suffix.sh " + sh(script: "git log -n 1 --pretty=format:'%H'", returnStdout: true).substring(0, 8) + "-" + env.BRANCH_NAME.replaceAll("[^a-zA-Z0-9.]", "_")
                    archiveArtifacts artifacts: "build/libs/*.jar", fingerprint: true
                }
            }
        }
    }

    post {
        always {
            deleteDir()
        }
    }
}
