/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.testing.Test

import eclipsebuild.testing.EclipseTestExecuter
import eclipsebuild.testing.EclipseTestExtension

import javax.inject.Inject


/**
 * Gradle plug-in to build Eclipse test bundles and launch tests.
 * <p/>
 * It contributes the following DSL to describe a testing project:
 * <pre>
 * eclipseTest {
 *     fragmentHost 'host.plugin.id'
 *     applicationName 'org.eclipse.pde.junit.runtime.coretestapplication'
 *     optionsFile file('.options')
 * }
 * </pre>
 * If the test project is an Eclipse plug-in fragment, then the the {@code fragmentHost} specifies
 * the host plug-in's ID (not mandatory). The {@code applicationName} is the PDE test runner class.
 * The {@code optionsFile} specifies a file containing extra arguments for the testing (not
 * mandatory).
 * <p/>
 * The tests are launched with PDE. The process is: (1) Copy the target platform to the build
 * folder. (2) Install the test plug-in and it's dependencies into the copied target platform with
 * P2. (3) Launch Eclipse with the PDE testing application (4) Collect the test results.
 * <p/>
 * The way how the tests are collected from the testing project and how the results are collected is
 * defined in the {@link eclipsebuild.testing} package.
 * <p/>
 * More information on the PDE testing automation:
 * <a href="http://www.eclipse.org/articles/Article-PDEJUnitAntAutomation/">
 * http://www.eclipse.org/articles/Article-PDEJUnitAntAutomation</a>.
 */
class TestBundlePlugin implements Plugin<Project> {

    // name of the root node in the DSL
    static String DSL_EXTENSION_NAME = "eclipseTest"

    // task names
    static final TASK_NAME_ECLIPSE_TEST = 'eclipseTest'

    public final FileResolver fileResolver

    @Inject
    public TestBundlePlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    @Override
    public void apply(Project project) {
        configureProject(project)
        validateDslBeforeBuildStarts(project)
        addTaskCreateEclipseTest(project)
    }

    static void configureProject(Project project) {
        project.extensions.create(DSL_EXTENSION_NAME, EclipseTestExtension)
        project.getPlugins().apply(eclipsebuild.BundlePlugin)
    }

    static void validateDslBeforeBuildStarts(Project project) {
        project.gradle.taskGraph.whenReady {
            // the eclipse application must be defined
            assert project.eclipseTest.applicationName != null
        }
    }

    static void addTaskCreateEclipseTest(Project project) {
        Config config = Config.on(project)
        def eclipseTest = project.task(TASK_NAME_ECLIPSE_TEST, type: Test) {
            group = Constants.gradleTaskGroupName
            description = 'Installs all dependencies into a fresh Eclipse, runs the IDE and executes the test classes with the PDE Test Runner'

            // configure the test runner to execute all classes from the project
            testExecuter = new EclipseTestExecuter(project)
            testClassesDir = project.sourceSets['main'].output.classesDir
            classpath = project.sourceSets.main.output + project.sourceSets.test.output
            testSrcDirs = []
            reports.html.destination = new File("${project.reporting.baseDir}/eclipseTest")

            // set some system properties for the test Eclipse
            systemProperty('osgi.requiredJavaVersion','1.7')
            systemProperty('eclipse.pde.launch','true')
            systemProperty('eclipse.p2.data.area','@config.dir/p2')

            // set the task outputs
            def testDistributionDir = project.file("$project.buildDir/eclipseTest/eclipse")
            def additionalPluginsDir = project.file("$project.buildDir/eclipseTest/additions")
            outputs.dir testDistributionDir
            outputs.dir additionalPluginsDir

            // the input for the task 'eclipseTest' is the output jars from the dependent projects
            // consequently we have to set it after the project is evaluated
            project.afterEvaluate {
                for (tc in project.configurations.compile.dependencies.withType(ProjectDependency)*.dependencyProject.tasks) {
                    def taskHandler = tc.findByPath("jar")
                    if(taskHandler != null) inputs.files taskHandler.outputs.files
                }
            }

            doFirst { beforeEclipseTest(project, config, testDistributionDir, additionalPluginsDir) }
        }

        // Make sure that every time the testing is running the 'eclipseTest' task is also gets executed.
        eclipseTest.dependsOn 'test'
        eclipseTest.dependsOn 'jar'
        project.tasks.check.dependsOn eclipseTest
    }


    static void beforeEclipseTest(Project project, Config config, File testDistributionDir, File additionalPluginsDir) {
        // before testing, create a fresh eclipse IDE with all dependent plugins installed
        // first delete the test eclipse distribution and the original plugins.
        project.logger.info("Delete '${testDistributionDir.absolutePath}'")
        testDistributionDir.deleteDir()
        project.logger.info("Delete '${additionalPluginsDir.absolutePath}'")
        additionalPluginsDir.deleteDir()

        // copy the target platform to the test distribution folder
        project.logger.info("Copy target platform from '${config.nonMavenizedTargetPlatformDir.absolutePath}' into the build folder '${testDistributionDir.absolutePath}'")
        copyTargetPlatformToBuildFolder(project, config, testDistributionDir)

        // publish the dependencies' output jars into a P2 repository in the additions folder
        project.logger.info("Create mini-update site from the test plug-in and its dependencies at '${additionalPluginsDir.absolutePath}'")
        publishDependenciesIntoTemporaryRepo(project, config, additionalPluginsDir)

        // install all elements from the P2 repository into the test Eclipse distribution
        project.logger.info("Install the test plug-in and its dependencies from '${additionalPluginsDir.absolutePath}' into '${testDistributionDir.absolutePath}'")
        installDepedenciesIntoTargetPlatform(project, config, additionalPluginsDir, testDistributionDir)
    }


    static void copyTargetPlatformToBuildFolder(Project project, Config config,  File distro) {
        project.copy {
            from config.nonMavenizedTargetPlatformDir
            into distro
        }
    }

    static void publishDependenciesIntoTemporaryRepo(Project project, Config config, File additionalPluginsDir) {
        // take all direct dependencies and and publish their jar archive to the build folder
        // (eclipsetest/additions subfolder) as a mini P2 update site
        for (ProjectDependency dep : project.configurations.compile.dependencies.withType(ProjectDependency)) {
            Project p = dep.dependencyProject
            project.logger.debug("Publish '${p.tasks.jar.outputs.files.singleFile.absolutePath}' to '${additionalPluginsDir.path}/${p.name}'")
            project.exec {
                commandLine(config.eclipseSdkExe,
                        "-application", "org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher",
                        "-metadataRepository", "file:${additionalPluginsDir.path}/${p.name}",
                        "-artifactRepository", "file:${additionalPluginsDir.path}/${p.name}",
                        "-bundles", p.tasks.jar.outputs.files.singleFile.path,
                        "-publishArtifacts",
                        "-nosplash")
            }
        }

        // and do the same with the current plugin
        project.logger.debug("Publish '${project.jar.outputs.files.singleFile.absolutePath}' to '${additionalPluginsDir.path}/${project.name}'")
        project.exec {
            commandLine(config.eclipseSdkExe,
                    "-application", "org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher",
                    "-metadataRepository", "file:${additionalPluginsDir.path}/${project.name}",
                    "-artifactRepository", "file:${additionalPluginsDir.path}/${project.name}",
                    "-bundles", project.jar.outputs.files.singleFile.path,
                    "-publishArtifacts",
                    "-nosplash")
        }
    }

    static void installDepedenciesIntoTargetPlatform(Project project, Config config, File additionalPluginsDir, File testDistributionDir) {
        // take the mini P2 update sites from the build folder and install it into the test Eclipse distribution
        for (ProjectDependency dep : project.configurations.compile.dependencies.withType(ProjectDependency)) {
            Project p = dep.dependencyProject
            project.logger.debug("Install '${additionalPluginsDir.path}/${p.name}' into '${testDistributionDir.absolutePath}'")
            project.exec {
                commandLine(config.eclipseSdkExe,
                        '-application', 'org.eclipse.equinox.p2.director',
                        '-repository', "file:${additionalPluginsDir.path}/${p.name}",
                        '-installIU', p.name,
                        '-destination', testDistributionDir,
                        '-profile', 'SDKProfile',
                        '-p2.os', Constants.os,
                        '-p2.ws', Constants.ws,
                        '-p2.arch', Constants.arch,
                        '-roaming',
                        '-nosplash')
            }
        }

        // do the same with the current project
        project.logger.debug("Install '${additionalPluginsDir.path}/${project.name}' into '${testDistributionDir.absolutePath}'")
        project.exec {
            commandLine(config.eclipseSdkExe,
                    '-application', 'org.eclipse.equinox.p2.director',
                    '-repository', "file:${additionalPluginsDir.path}/${project.name}",
                    '-installIU', project.name,
                    '-destination', testDistributionDir,
                    '-profile', 'SDKProfile',
                    '-p2.os', Constants.os,
                    '-p2.ws', Constants.ws,
                    '-p2.arch', Constants.arch,
                    '-roaming',
                    '-nosplash')
        }
    }

}

