package javaee.configurator

import java.util.Arrays;
import java.util.List;

import org.junit.Rule
import spock.lang.Specification
import org.junit.rules.TemporaryFolder

import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.core.resources.IWorkspaceRoot
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.common.project.facet.core.internal.JavaFacetUtil
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.wst.common.project.facet.core.IFacetedProject
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import com.gradleware.tooling.toolingclient.GradleDistribution
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.util.progress.AsyncHandler

class WebApplicationProjectConfiguratorTest extends ProjectImportSpecification {

//    def "Java facet is added to War Project"() {
//        def monitor = new NullProgressMonitor()
//        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot()
//
//        setup:
//        def location = folder('app')
//        folder('app', 'src', 'main', 'java')
//        file('app', 'settings.gradle') << ''
//        file('app', 'build.gradle') <<
//        '''apply plugin: "war"'''
//        file('app', 'src/main/java/TestClass.java') << ''
//
//        when:
//        executeProjectImportAndWait(location)
//
//        then:
//        def project = workspaceRoot.getProject('app');
//        IFacetedProject facetedProject = ProjectFacetsManager.create(project, true, monitor);
//        IProjectFacetVersion javaFacetVersion = JavaFacet.FACET.getVersion(JavaFacetUtil.getCompilerLevel(project));
//
//        facetedProject.hasProjectFacet(javaFacetVersion)
//    }
//
//    def "Web facet is added to War Project"() {
//        def monitor = new NullProgressMonitor()
//        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot()
//
//        setup:
//        def location = folder('app')
//        folder('app', 'src', 'main', 'java')
//        file('app', 'settings.gradle') << ''
//        file('app', 'build.gradle') <<
//        '''apply plugin: "war"'''
//        file('app', 'src/main/java/TestClass.java') << ''
//
//        when:
//        executeProjectImportAndWait(location)
//
//        then:
//        def project = workspaceRoot.getProject('app');
//        IFacetedProject facetedProject = ProjectFacetsManager.create(project, true, monitor);
////        facetedProject.hasProjectFacet(WebFacetUtils.WEB_FACET)
//    }

    def "Gradle Classpath container is deployable"() {
        def monitor = new NullProgressMonitor()
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot()

        setup:
        def location = folder('app')
        folder('app', 'src', 'main', 'java')
        file('app', 'settings.gradle') << ''
        file('app', 'build.gradle') <<
        '''apply plugin: "war"'''
        file('app', 'src/main/java/TestClass.java') << ''


        when:
        executeProjectImportAndWait(location)
        Thread.sleep(3000);

        then:
        def project = workspaceRoot.getProject('app');
        IJavaProject javaProject = JavaCore.create(project);
        IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
        IClasspathEntry entry = classpathEntries.find({
            it.getPath().toString().equals("org.eclipse.buildship.core.gradleclasspathcontainer")
        })

        println 'entry == ' + entry
        IClasspathAttribute[] attrs = entry.getExtraAttributes()
        println "attrs>>" + attrs
        println 'length>> ' + attrs.length
        def attr = attrs.find({
            it.name == "org.eclipse.jst.component.dependency" && it.value == "/WEB-INF/lib"
        })
        attr != null
    }

    def "Test sources are not deployed"() {
        def monitor = new NullProgressMonitor()
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot()

        setup:
        def location = folder('app')
        folder('app', 'src', 'main', 'java')
        file('app', 'settings.gradle') << ''
        file('app', 'build.gradle') <<
        '''apply plugin: "war"'''
        file('app', 'src/main/java/TestClass.java') << ''

        when:
        executeProjectImportAndWait(location)

        then:

    }

//    def "Test and provided dependencies are not deployed"() {
//        def monitor = new NullProgressMonitor()
//        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot()
//
//        setup:
//        def location = folder('app')
//        folder('app', 'src', 'main', 'java')
//        file('app', 'settings.gradle') << ''
//        file('app', 'build.gradle') <<
//        '''apply plugin: "war"'''
//        file('app', 'src/main/java/TestClass.java') << ''
//
//
//        when:
//        executeProjectImportAndWait(location)
//
//    }

}
