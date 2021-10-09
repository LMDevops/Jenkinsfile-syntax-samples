basic declarative pipeline syntax:
----------------------------------
pipeline { 
  agent any # "agent"- where to execute. if not specified will execute in any available agent

  stages { #where the "work" happens

    stage("build") {

      steps{

      }
    }
  }
}
-------------------------
basic scripted syntax:
-------------------------
node {
  //groovy script
}
---------------------------


Declarative pipeline samples:
#1. basic:
------------
pipeline { 
  agent any 

  stages { 

    stage("build") {

      steps{

      }
    }
  }
}


-------------------------------------------
#2. adding multiple stages to the pipeline:
-------------------------------------------
pipeline { 
  agent any 

  stages { 

    stage("build") {

      steps{ #the script that executes some commands on the jenkins server
        #eg. 'npm install'
        #'npm build'
        echo 'building the application...'

      }
    }
  stage("test") {

      steps{
        echo 'testing the application...'
      }
    }
  stage("deploy") {

      steps{
        echo 'deploying the application...'
      }
    }
  }
}

