FROM jenkins/jenkins:lts-jdk21

# Skip setup wizard
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Install plugins via jenkins-plugin-cli (bundled in image)
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Copy JCasC config and Job DSL definitions
COPY jenkins.yaml /usr/share/jenkins/ref/jenkins.yaml
COPY jobs/ /usr/share/jenkins/ref/jobs/
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/jenkins.yaml
