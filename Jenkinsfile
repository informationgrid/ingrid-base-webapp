pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '5'))
    }

    stages {
        stage('Build') {
            steps {
                script {
                    /*
                        Start an elasticsearch cluster in a docker container
                        Attention: we need to assign the correct network where jenkins was created in
                                   we also should use the IP mask for the port mapping to only allow
                                   access to the right containers
                    */
                    docker.image('docker-registry.wemove.com/ingrid-elasticsearch-with-decompound:6.4.2').withRun('--name "elasticsearch_basewebapp" -e "cluster.name=ingrid" -e "http.host=0.0.0.0" -e "transport.host=0.0.0.0" -e "xpack.security.enabled=false" -e "xpack.monitoring.enabled=false" -e "xpack.ml.enabled=false" --network jenkinsnexussonar_devnet -p 172.20.0.0:9301:9300') { c ->
                        withMaven(
                                // Maven installation declared in the Jenkins "Global Tool Configuration"
                                maven: 'Maven3',
                                // Maven settings.xml file defined with the Jenkins Config File Provider Plugin
                                // Maven settings and global settings can also be defined in Jenkins Global Tools Configuration
                                mavenSettingsConfig: '2529f595-4ac5-44c6-8b4f-f79b5c3f4bae'
                        ) {

                            // Run the maven build
                            sh 'mvn clean deploy -Dmaven.test.failure.ignore=true'

                        } // withMaven will discover the generated Maven artifacts, JUnit Surefire & FailSafe & FindBugs reports...
                    }
                }
            }
        }
        stage ('SonarQube Analysis'){
            steps {
                withMaven(
                    maven: 'Maven3',
                    mavenSettingsConfig: '2529f595-4ac5-44c6-8b4f-f79b5c3f4bae'
                ) {
                    withSonarQubeEnv('Wemove SonarQube') {
                        sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.4.0.905:sonar'
                    }
                }
            }
        }
    }
    post {
        changed {
            // send Email with Jenkins' default configuration
            script { 
                emailext (
                    body: '${DEFAULT_CONTENT}',
                    subject: '${DEFAULT_SUBJECT}',
                    to: '${DEFAULT_RECIPIENTS}')
            }
        }
    }
}
