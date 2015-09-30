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
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.buildship.core.util.progress.AsyncHandler

class WebApplicationProjectConfiguratorTest extends ProjectImportSpecification {

    def "Java facet is added to War Project"() {
        def monitor = new NullProgressMonitor()
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        setup:
        def location = folder('app')
        folder('app', 'src', 'main', 'java')
        file('app', 'settings.gradle') << ''
        file('app', 'build.gradle') <<
        '''apply plugin: "war"'''
        file('app', 'src/main/java/TestClass.java') << ''

        when:
        executeProjectImportAndWait(location)
        Thread.sleep(60000)

        then:
        def project = workspaceRoot.getProject('app');
        IFacetedProject facetedProject = ProjectFacetsManager.create(project, true, monitor);
        IProjectFacetVersion javaFacetVersion = JavaFacet.FACET.getVersion(JavaFacetUtil.getCompilerLevel(project));

        facetedProject.hasProjectFacet(javaFacetVersion)
    }

}
