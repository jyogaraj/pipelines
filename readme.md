

# Creating Pipeline 

## pre-reqs
- jenkins with basic plugins

## setup
-  Login to jenkins and create a Pipeline job
- In the pipeline definition, select 'Pipeline script from SCM' with below details
	SCM - git
	Repository - https://github.com/jyogaraj/pipelines.git
	Branches - */master
	script path - jenkinsfile-demoapp-deploy.groovy
	save the configuration and run the job
- it will create own docker container and deploy the application which can be accessible at http://servername


## Pipeline explanation
Note: Here just deployed the application in the docker container and running in the same machine, which can be from different machines also[development/production] cases.
As this is not a real solution, skipped that and assumed we are deploying to same server box.


Pipeline is only CI ready not CD ready

it have below stages
	- cleanup -->cleans up workspace
	- prepare --> do neccasry checkouts and manipulations
	- build --> builds image
	- code quality --> does code quality , i have not included that
	- deploy --> validates and deploys to container
