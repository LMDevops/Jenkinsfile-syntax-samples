//basic declarative pipeline syntax:
----------------------------------
pipeline { 
  agent any # "agent"- where to execute. if not specified will execute in any available agent

  stages { #where the "work" happens

    stage("build") {

      steps {

      }
    }
  }
}
-------------------------
//basic scripted syntax:
-------------------------
node {
  //groovy script
}
---------------------------


//Declarative pipeline samples:
===============================
//1. basic:
------------
pipeline { 
  agent any 

  stages { 

    stage("build") {

      steps {

      }
    }
  }
}


-----------------------------------------------------------------
//2. adding multiple stages to the pipeline and post attribute:
------------------------------------------------------------------
pipeline { 
  agent any 

  stages { 

    stage("build") {
      steps { #the script that executes some commands on the jenkins server
        #eg. 'npm install'
        #'npm build'
        echo 'building the application...'

      }
    }

  stage("test") {
      steps {
        echo 'testing the application...'
      }
    }

  stage("deploy") {
      steps {
        echo 'deploying the application...'
      }
    }
  }

  post { # build status and build status changes
    always{
      //what to do regardless of the status of the build. eg. send email notifications
    }
    failure {
      // what to do in case of failure
    }
    success {
      // what to do in case of success
    }
  }
}


------------------------------------------
//3. defining conditionals for each stage:
------------------------------------------
CODE_CHANGES = getGitChanges() #this will be a groovy script that checks if there has been any changes in the code and sets the value of the boolean
pipeline { 
  agent any 

  stages { 
    stage("build") {
       when {
      expression { # boolean expressions
          # env.BRANCH_NAME
          BRANCH_NAME == 'dev' && CODE_CHANGES == true
      }
    }
      steps { #this step will only execute if there is a code change in the dev branch
        echo 'building the application...'
      }
    }
  stage("test") {
    when {
      expression { # boolean expressions
          # env.BRANCH_NAME
          BRANCH_NAME == 'dev' || BRANCH_NAME == 'master'

      }
    }
      steps { #this step will not only execute only if the branch is dev or master. the pipe denotes 'and'
        echo 'testing the application...'
      }
    }
  stage("deploy") {
      steps {
        echo 'deploying the application...'
      }
    }
  }
 }

-----------------------------------------------------
 //4. use of environment variables in the Jenkinsfile:
-----------------------------------------------------
# ps. list of variables available for use can be found in JenkinsServerIP:8080/env-vars.html
# you can also define your own env. variables:

 pipeline { 
  agent any 
  environment { #environment variables defined here will be available throughout the pipeline
    NEW_VERSION = '1.3.0' #version of code
    SERVER_CREDENTIALS = credentials('server-credentials')   # using credentials in Jenkins- first, define the credentials on the jenkins GUI
    // this will bind the credentials to your environment variable for use from miscellaneous build steps
    //"credentials("credentialID")"
    //for that you have to need to install the "Credentials Binding Plugin"  and "Credentials Plugin"- "allows you to store credentials in Jenkins"
  }
  stages { 

    stage("build") {
      steps { 
        echo 'building the application...'
        echo "building version ${NEW_VERSION}"   # in groovy, for a variable to be interpreted as a string, it has to be enclosed in double quotes. you can use either single/double quotes if it is a simple string
        # echo 'building version ${NEW_VERSION}' # see difference when single quote is used.
      }
    }
  stage("test") {
      steps {
        echo 'testing the application...'
      }
    }
  stage("deploy") {
      steps {
        echo 'deploying the application...'
        echo "deploying with ${SERVER_CREDENTIALS}"
        sh "${SERVER_CREDENTIALS}"

      # alternatively you can define it as below(wrapper syntax):
        echo 'deploying the application...'
        withCredentials([
          usernamePassword(credentials: 'server-credentials', usernameVariable: USER, passwordVariable: PASS) # type of credential: "Username with Password"
        ]) {
          sh "some script ${USER} ${PASS}"
        }

      }
    }
  }
}

------------------------------------
//5. Tools Attribute for Build Tools:
------------------------------------
pipeline { 
  agent any 
  tools {
    // provides you with build tools for your projects
    // Jenkins only currently supports 3 tools: Gradle, Maven and JDK
    // any other tools have to be installed directly on the jenkins server 

    //gradle 
    //jdk 
    maven 'Maven' # this will make maven comments available in all the stages
  }

  stages { 
    stage("build") {
      steps { 
        echo 'building the application...'
        sh "mvn install"
      }
    }
  stage("test") {
      steps {
        echo 'testing the application...'
      }
    }
  stage("deploy") {
      steps {
        echo 'deploying the application...'
      }
    }
  }
}

--------------------------------------------------------------------------------
//6. Parameters in Jenkinsfile: (build with parameters: choose version on the UI)
--------------------------------------------------------------------------------
pipeline { 
  agent any 
  parameters { 
    // to pass external configurations to your build to change some behavior
    //parameterize your build:
    //types of parameter:

    //string(name, defaultValue, description)
    string(name: 'VERSION', defaultValue: '', description:'version to deploy on prod')

    # alternatively:

    // choice(name, choices, description)
    // booleanParam(name, defaultValue, description) eg. to skip certain build stages
    choice(name: 'VERSION', choices: ['1.1.0', '1.1.1', '1.1.2', '1.1.3'], description: '')
    booleanParam(name: 'executeTests', defaultValue: true, description: '')
  }
  stages { 
    stage("build") {
      steps { 
        echo 'building the application...'
      }
    }
  stage("test") {
    when {
      expression { 
        //params.executeTests == true
        params.executeTests
      }
    }
      steps {
        echo 'testing the application...'
      }
    }
  stage("deploy") {
      steps {
        echo 'deploying the application...'
        echo "deploying version ${params.VERSION}" # it must be params.VERSION
      }
    }
  }
}
-------------------------------------------------
//7. Use of external script in Jenkins:
-----------------------------------------------
def gv 
pipeline { 
  agent any 
parameters { //all environment variables are available in the groovy script
  choice(name: 'VERSION', choices: ['1.1.0', '1.1.1', '1.1.2', '1.1.3'], description:'')
    booleanParam(name: 'executeTests', defaultValue: true, description: '')
  }

  stage("init") {
      steps {
        script { //extract values to an external groovy script
          gv = load "script.groovy"
        }
      }
    }
  stages { 
    stage("build") {
      steps { 
        script { 
          gv.buildApp()
        }
        echo 'building the application...'
      }
    }
  stage("test") {
    when {
      expression { 
        //params.executeTests == true
        params.executeTests
      }
    }
      steps {
        script { 
          gv.testApp()
        }
      }
    }
  stage("deploy") {
    input { //Input Parameters for user input
    //to allow a user to make some choices like which branch to deploy to, which version of the app to build etc.
      message "select the env. to deploy to"
      ok "Done" //or ok "Confirmed" etc. for when the user makes their selection
      parameters {//this is where the actual input options go
        choice(name: 'ENV', choices: ['dev', 'testing', 'staging', 'prod'], description: '')

        //**alternative for multichoice options for user input:
        choice(name: 'ONE', choices: ['dev', 'testing', 'staging', 'prod'], description: '')
        choice(name: 'TWO', choices: ['dev', 'testing', 'staging', 'prod'], description: '')
      }
    }
      steps {
        script {
          gv.deployApp()
          echo "deploying to ${ONE}" //(scoped env. variable, not global. you can make it global if needed for other steps)
          echo "deploying to ${TWO}"
        }

        // alternatively you can make it global. You can define the input variable directly in the script but the syntax will be different:
        steps {
        script {
          env.ENV = input message: "select the env. to deploy to", ok: "Done", parameters:[choice(name: 'ONE', choices: ['dev', 'testing', 'staging', 'prod'], description: '')]
          gv.deployApp()
          echo "deploying to ${ENV}" //(scoped env. variable, not global. you can make it global if needed for other steps)
          
        }
      }
    }
  }
}
//you can chose replay on the UI to view the scripts (Jenkinsfile and script.groovy) in edit mode and make changes that you do not want to necessarily change to the git repo.
// the build will be paused until the user makes a choice of value. also has the option to abort the whole build
//** in the multi-choice for user input, the choice names have to be different from each other.


-----------------------------------------------------
//8. FULL PIPELINE:
-----------------------------------------------------
pipeline { 
  agent any 
  tools{
    maven 'Maven'
  }
 
  stages { 
    stage("build jar") {
      steps { 
        echo "building the application..."
        sh 'mvn package'
      }
    }
  stage("build image") {
      steps {
        echo "building the docker image..."
        //login to DockerHub with the credentials provided to Jenkins, build the image
        withCredentials([usernamePassword(credentialID: 'docker-hub-repo', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
          sh 'docker build -t phyllisn/demo-app:java-maven-app-2.0.'
          sh "echo $PASS | docker login -u $USER --password-stdin"
        //sh "echo $PASSWORD | docker login -u $USERNAME --password-stdin 167.99.241.123:8083" (for any other registry than dockerhub)
          sh 'docker push phyllisn/demo-app:java-maven-app-2.0'
        }
      }
    }
  stage("deploy") {
      steps {
        echo "deploying the application..."
      }
    }
  }
}

--------------------------------
//9. extracting to script.groovy 
--------------------------------
def gv
pipeline { 
  agent any 

  stages { 
     stage("init") {
      steps {
        script { //extract values to an external groovy script
          gv = load "script.groovy"
        }
      }
    }
    stage("build jar") {
      steps { 
        script { 
          gv.buildJar
          }
    }
  stage("test") {
      steps {
        script { 
          gv.buildImage
        }
      }
    }
  stage("deploy") {
      steps {
        script { 
          gv.deployApp
        }
      }
    }
  }
}
//if you are using an external groovy script, you can also extract the commands to the script
//this makes for a cleaner Jenkinsfile and also makes it easier to make changes to the variables in the script.groovy file

---------------------------------------------------
//10. Branch-Based Logic for Multi-branch Pipeline 
---------------------------------------------------
pipeline { 
  agent none 
  stages { 
    stage('test') {//this will be executed for all branches so need to pass if/when statements.
      steps { 
        echo 'testing the application...'
        echo "executing pipeline for branch $BRANCH_NAME"
      }
    }
  stage('build') {
    when{
      expression {
        BRANCH_NAME == 'master'
      }
    }
      steps {
        echo 'building the application...'
      }
    }
  stage('deploy') {
    when {
      expression {
        BRANCH_NAME == 'master'
      }
    }
      steps {
        echo 'deploying the application...'
      }
    }
  }
}
//can merge the Jenkinsfile to make it available to master to substitute the cuurent one in master.
//**Scan Multibranch pipeline now**-->
// this is like the parent branch for all the sub-pipeline indivisual branches for which the regular expression is going to build from
//so of there are code changes in any of the branches it will pick this up
// you can also, if you want to, build from a specific pipeline

-----------------------------------
pipeline { 
  agent any 

  stages { 
    stage("build") {
      steps { 
        echo 'building the application...'
      }
    }
  stage("test") {
      steps {
        echo 'testing the application...'
      }
    }
  stage("deploy") {
      steps {
        echo 'deploying the application...'
      }
    }
  }
}
----------------------------------------------
pipeline { 
  agent any 

  stages { 
    stage("build") {
      steps { 
        echo 'building the application...'
      }
    }
  stage("test") {
      steps {
        echo 'testing the application...'
      }
    }
  stage("deploy") {
      steps {
        echo 'deploying the application...'
      }
    }
  }
}
