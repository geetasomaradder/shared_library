def call(String registryCred = 'a', String registryin = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a') {

pipeline {
environment { 
		registryCredential = "${registryCred}"
		registry = "$registryin" 	
		dockerTag = "${docTag}$BUILD_NUMBER"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
	}
		
	agent none
	
	stages {
		stage("POLL SCM"){
      		agent{label 'kub'}
			steps {
				println('step 1')
				checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredId", url: "$gitRepo"]]])
			}
		}	
					
		stage('BUILD IMAGE') {
       			agent{label 'kub'}
			steps { 
				 script { 
					 dockerimage = dockerImage = docker.build registry + ":$dockerTag" 
				 }
			} 
		}
					
		stage('PUSH HUB') { 
       			agent{label 'kub'}
			steps { 
				script {
					docker.withRegistry( '', registryCredential ) { 
			        		dockerImage.push() 
                    			}
                		}		
			} 
		}
					
		stage('DEPLOY IMAGE') {
      			agent{label 'kub'}
			steps {
				sh 'kubectl set image deploy webapp-deployment nodejs="$registry:$dockerTag" --record'
			}
		}
	}
			  
}

}
