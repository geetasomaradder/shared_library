def call(String registryCred = 'a', String registryname = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a', String depname = 'a', String contname = 'a') {

pipeline {
environment { 
		registryCredential = "${registryCred}"
    		registry = "${registryname}" 	
		dockerTag = "${docTag}$BUILD_NUMBER"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
    		deployment = "${depname}"
    		containerName = "${contname}"
	}
		
    agent none

    stages {
        stage("POLL SCM"){
		agent{label 'node-tomcat'}
            		steps {
			println('before checkout statement')
		        sh 'rm -rf nodejs-k8s'
			sh 'git clone --single-branch --branch main https://github.com/geetasomaradder/nodejs-k8s.git'
			
			//checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredId", url: "$gitRepo"]]])
                	//checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[ url: "$gitRepo"]]]) 
			println('after checkout statement')
			//	println([ 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], url: "$gitRepo"]]])
            		}
        } 
        
        stage('BUILD IMAGE') {
		agent{label 'node-tomcat'}
            		steps {
                	sh 'docker build -t $registry:$dockerTag .'             
            		}
        }
        
        stage('PUSH HUB') { 
		agent{label 'node-tomcat'}
            		steps {
			            sh 'docker push $registry:$dockerTag'                   	
                	}    
        }
        
        stage('DEPLOY IMAGE') {
		agent{label 'kub'}
		          steps {
			            sh 'kubectl set image deploy $deployment $containerName="$registry:$dockerTag" --record'
		          }
	}  
    }
}  
}
