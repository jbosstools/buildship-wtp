package javaee.configurator

import org.junit.Rule
import spock.lang.Specification
import org.junit.rules.TemporaryFolder

import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.core.resources.IWorkspaceRoot
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.common.project.facet.core.internal.JavaFacetUtil;
import org.eclipse.wst.common.project.facet.core.IFacetedProject
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import com.gradleware.tooling.toolingclient.GradleDistribution
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.buildship.core.util.progress.AsyncHandler

class WebApplicationProjectConfiguratorTest extends Specification {


    @Rule
    TemporaryFolder tempFolder

    def "Java facet is added to War Project"() {
        def monitor = new NullProgressMonitor()

        given:
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        File projectLocation = newProject(false, true)
        def job = newProjectImportJob(projectLocation)

        when:
        job.schedule()
        job.join()

        then:
        def project = workspaceRoot.getProject(projectLocation.name);
        1 == 1
        IFacetedProject facetedProject = ProjectFacetsManager.create(project, true, monitor);
        IProjectFacetVersion javaFacetVersion = JavaFacet.FACET.getVersion(JavaFacetUtil.getCompilerLevel(project));
        facetedProject.hasProjectFacet(javaFacetVersion)
    }


    def newProject(boolean projectDescriptorExists, boolean applyWarPlugin) {
        def root = tempFolder.newFolder('simple-project')
        new File(root, 'build.gradle') << (applyWarPlugin ? 'apply plugin: "war"' : '')
        new File(root, 'settings.gradle') << ''
        new File(root, 'src/main/webapp').mkdirs()
        new File(root, '.settings').mkdirs()
//        new File(root.toString() + '/.settings', 'gradle.prefs') << ''

        if (projectDescriptorExists) {
            new File(root, '.project') << '''<?xml version="1.0" encoding="UTF-8"?>
                <projectDescription>
                  <name>simple-project</name>
                  <comment>original</comment>
                  <projects></projects>
                  <buildSpec></buildSpec>
                  <natures></natures>
                </projectDescription>'''
            if (applyWarPlugin) {
                new File(root, '.classpath') << '''<?xml version="1.0" encoding="UTF-8"?>
                    <classpath>
                      <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
                      <classpathentry kind="src" path="src/main/java"/>
                      <classpathentry kind="output" path="bin"/>
                    </classpath>'''
            }
        }
        root
    }

    def newProjectImportJob(File location) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(GradleDistribution.fromBuild())
        configuration.projectDir = location
        configuration.applyWorkingSets = true
        configuration.workingSets = []
//        new ProjectImportJob(configuration, AsyncHandler.NO_OP)
    }

}
