//example:
#def function() {
#    echo 'building the application'
# }

#   return this

--------------------

def buildApp() {
    echo 'building the application...'
    
}
def testApp() {
    echo 'testing the application...'
    
}
def deployApp() {
    echo 'deploying the application...'
    echo "deploying version ${params.VERSION}"
    
}
 return this
---------------------

==================================
//groovy script for example 9:
==================================
def buildJar() {
    echo "building the application..."
        sh 'mvn package'
}
def buildImage() {
    echo "building the image..."
    withCredentials([usernamePassword(credentialID: 'docker-hub-repo', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
          sh 'docker build -t phyllisn/demo-app:java-maven-app-2.0.'
          sh "echo $PASS | docker login -u $USER --password-stdin"
        //sh "echo $PASSWORD | docker login -u $USERNAME --password-stdin 167.99.241.123:8083" (for any other registry than dockerhub)
          sh 'docker push phyllisn/demo-app:java-maven-app-2.0'
    }
}
def deployApp() {
    echo "deploying the application..."
}

return this
------------------------------------
//11. shared library
------------------------
// this file can also be extracted to the Jenkins Shared Library
def deployApp() {
    echo "deploying the application..."
}

return this