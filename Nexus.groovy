@Grapes([
        @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7'),
        @GrabConfig(systemClassLoader=true)
])
import hudson.model.*
import groovyx.net.http.RESTClient
import org.apache.http.entity.*


void pull() {
    ArtifactName = System.getenv('ARTIFACT_NAME')
    BuildNumber = ArtifactName.split('-')[1]
    Repo = "maven-releases"
    Workspace = System.getenv('WORKSPACE')
    Gav = GetGav()
    NewPath = Gav[0].replaceAll('\\.','/')
    ArtifactId = Gav[1]
    Version = Gav[2].split('-')[0]
    Nexus = new RESTClient("http://192.168.1.3/repository/${Repo}/")
    Nexus.auth.basic 'admin', 'admin123'
    ArtDown = Nexus.get(path: "http://192.168.1.3/repository/${Repo}/${NewPath}/${ArtifactId}/${BuildNumber}/${ArtifactName}.war")
    new File("./${artifactId}.war") << Art.data
}

void push() {
    BuildNumber = System.getenv('BUILD_NUMBER')
    Repo = "maven-releases"
    Workspace = System.getenv('WORKSPACE')
    Gav = GetGav()
    Nexus = new RESTClient("http://nexus/repository/${Repo}/")
    Nexus.auth.basic 'admin', 'admin123'
    Nexus.encoder.'application/zip' = this.&encodingZipFile
    NewPath = Gav[0].replaceAll('\\.','/')
    ArtifactId = Gav[1]
    Version = Gav[2].split('-')[0]
    ArtUp = Nexus.put(path: "http://nexus/repository/${Repo}/${NewPath}/${ArtifactId}/1.${BuildNumber}/${ArtifactId}-${BuildNumber}.war",
            body: new File("target/${ArtifactId}-${BuildNumber}.war"), requestContentType: 'application/zip')
}

String[] GetGav() {
    def pom = new XmlSlurper().parse(new File("pom.xml"))
    def gav = []
    gav.add(pom.groupId)
    gav.add(pom.artifactId)
    gav.add(pom.version)
    return gav
}

Artifact = System.getenv('ARTIFACT_NAME')
if (Artifact == null) {
    pull()
} else push()