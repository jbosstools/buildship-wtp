package org.eclipse.buildship.javaee.core.configurator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.common.project.facet.core.internal.JavaFacetUtil;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.web.project.facet.IWebFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetInstallDataModelProvider;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

import org.eclipse.buildship.core.configuration.ProjectConfigurationRequest;
import org.eclipse.buildship.core.configurator.IProjectConfigurator;
import org.eclipse.buildship.core.workspace.GradleClasspathContainer;
import org.eclipse.buildship.javaee.core.Activator;
import org.eclipse.buildship.javaee.core.ProjectAnalyzer;
import org.eclipse.buildship.javaee.core.ResourceCleaner;
import org.eclipse.buildship.javaee.core.model.WarModel;

/**
 * Configures an Eclipse Web Application project. The configurator is applied if and only if the
 * given project applies the Gradle 'war' plugin.
 *
 * This configurator implements the following steps, in this order:
 * 1. Add the Java web facet, and the Dynamic web facet.
 * 2. Remove any redundant files created by the addition of the dynamic web facet.
 * 3. Flag test dependencies as 'non-deploy' in component file.
 *
 */
@SuppressWarnings({ "restriction" })
public class WebApplicationConfigurator implements IProjectConfigurator {

    private static final String GRADLE_CLASSPATH_CONTAINER_PATH = "org.eclipse.buildship.core.gradleclasspathcontainer";

    @Override
    public boolean canConfigure(ProjectConfigurationRequest configurationRequest) {
        String projectPath = configurationRequest.getWorkspaceProject().getLocationURI().getPath();
        Activator.getLogger().info("Checking if war configurator can be applied...: " + ProjectAnalyzer.isWarProject(projectPath));
        return ProjectAnalyzer.isWarProject(projectPath);
    }

    @Override
    public IStatus configure(ProjectConfigurationRequest configurationRequest, IProgressMonitor monitor) {
        System.out.println("Web App Project Configuration Starting");
        MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, "", null);
        IProject workspaceProject = configurationRequest.getWorkspaceProject();
        OmniEclipseProject project = configurationRequest.getProject();
        try {
            configureFacets(configurationRequest, monitor, multiStatus);
            makeGradleContainerDeployable(configurationRequest, monitor);
            removeTestFolderLinks(workspaceProject, project, monitor);
            getTestDependencies(configurationRequest);
        } catch (Exception e) {
            IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
            multiStatus.add(errorStatus);
        }
        return multiStatus;
    }

    @Override
    public int getWeight() {

        // Currently, the WAR configurator's weight is set to 100, to ensure that it is called
        // after the currently existing Java project configurator.
        return 100;
    }

    private void configureFacets(ProjectConfigurationRequest configurationRequest, IProgressMonitor monitor, MultiStatus multiStatus) throws Exception {
        Set<Action> actions = new LinkedHashSet<Action>();
        IProject workspaceProject = configurationRequest.getWorkspaceProject();
        String projectPath = configurationRequest.getWorkspaceProject().getLocationURI().getPath();
        WarModel warModel = ProjectAnalyzer.getWarModel(projectPath);
        System.out.println("Deps" + warModel.getDeps());
        ResourceCleaner cleaner = new ResourceCleaner(workspaceProject, workspaceProject.getFolder(warModel.getWebAppDirName()));
        cleaner.collectWtpFolders(warModel);

        IFacetedProject facetedProject = ProjectFacetsManager.create(workspaceProject, true, monitor);

        installJavaFacet(actions, workspaceProject, facetedProject);
        installWebFacet(actions, workspaceProject, facetedProject, projectPath);

        facetedProject.modify(actions, monitor);

        cleaner.clean(monitor);
    }

    // 'Inspired by'
    // https://github.com/eclipse/m2e.wtp/blob/3ef31ea28a4ea6f5728eb38a6f4d2712b8a4d333/org.eclipse.m2e.wtp/src/org/eclipse/m2e/wtp/WebProjectConfiguratorDelegate.java
    private void installJavaFacet(Set<Action> actions, IProject project, IFacetedProject facetedProject) {
        // Source for JavaFacetUtil:
        // https://eclipse.googlesource.com/webtools-common/webtools.common.fproj/+/b01e5326cd9de1afb60f6a25a81c7b152a08b526/plugins/org.eclipse.jst.common.project.facet.core/src/org/eclipse/jst/common/project/facet/core/internal/JavaFacetUtil.java
        // May be worthwhile not using an internal method.
        IProjectFacetVersion javaFacetVersion = JavaFacet.FACET.getVersion(JavaFacetUtil.getCompilerLevel(project));

        if (!facetedProject.hasProjectFacet(JavaFacet.FACET)) {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.INSTALL, javaFacetVersion, null));
        } else if (!facetedProject.hasProjectFacet(javaFacetVersion)) {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.VERSION_CHANGE, javaFacetVersion, null));
        }
    }

    private void installWebFacet(Set<Action> actions, IProject project, IFacetedProject facetedProject, String projectPath) {
        WarModel warModel = ProjectAnalyzer.getWarModel(projectPath);
        IProjectFacetVersion webFacetVersion = getWebFacetVersion(project, warModel);
        String webAppDirName = warModel.getWebAppDirName();

        // TODO: create only if used
        IDataModel webModelConfig = DataModelFactory.createDataModel(new WebFacetInstallDataModelProvider());
        webModelConfig.setProperty(IJ2EEModuleFacetInstallDataModelProperties.CONFIG_FOLDER, webAppDirName);
        webModelConfig.setProperty(IJ2EEModuleFacetInstallDataModelProperties.GENERATE_DD, false);
        webModelConfig.setBooleanProperty(IWebFacetInstallDataModelProperties.ADD_TO_EAR, false);

        if (!facetedProject.hasProjectFacet(WebFacetUtils.WEB_FACET)) {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.INSTALL, webFacetVersion, webModelConfig));
        } else if (!facetedProject.hasProjectFacet(webFacetVersion)) {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.VERSION_CHANGE, webFacetVersion, webModelConfig));
        }
    }

    /**
     * Reads the web facet version in from the web xml file. If no file exists, or if no web facet
     * version has been declared, web facet version 2.5 is returned. TODO: Why are IOException and
     * coreexception expected?
     */
    private IProjectFacetVersion getWebFacetVersion(IProject project, WarModel warModel) {
        String webXmlName = warModel.getWebXmlName();
        IFile webXmlFile = project.getFile(webXmlName);

        if (webXmlFile.isAccessible()) {
            try {
                InputStream is = webXmlFile.getContents();
                try {
                    JavaEEQuickPeek jqp = new JavaEEQuickPeek(is);
                    switch (jqp.getVersion()) {
                        case J2EEVersionConstants.WEB_2_2_ID:
                            return WebFacetUtils.WEB_22;
                        case J2EEVersionConstants.WEB_2_3_ID:
                            return WebFacetUtils.WEB_23;
                        case J2EEVersionConstants.WEB_2_4_ID:
                            return WebFacetUtils.WEB_24;
                        case J2EEVersionConstants.WEB_2_5_ID:
                            return WebFacetUtils.WEB_25;
                        case J2EEVersionConstants.WEB_3_0_ID:
                            return WebFacetUtils.WEB_30;
                        case J2EEVersionConstants.WEB_3_1_ID:
                            return WebFacetUtils.WEB_31;
                    }
                } finally {
                    is.close();
                }
            } catch (IOException ex) {
                // expected
            } catch (CoreException ex) {
                // expected
            }
        }

        // The default dynamic web facet version has been chosen to be 25
        // such that it complies with Java versions starting from 1.6.
        return WebFacetUtils.WEB_25;
    }

    /**
     * Makes the Gradle container deployable by modifying its classpath entry.
     */
    private void makeGradleContainerDeployable(ProjectConfigurationRequest projectConfigurationRequest, IProgressMonitor monitor) throws JavaModelException {
        IProject workspaceProject = projectConfigurationRequest.getWorkspaceProject();
        IJavaProject javaProject = JavaCore.create(workspaceProject);

        IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
        ArrayList<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();

        for (IClasspathEntry entry : classpathEntries) {
            String path = entry.getPath().toString();
            if (path.equals(GRADLE_CLASSPATH_CONTAINER_PATH)) {
                IClasspathEntry newGradleContainerEntry = modifyGradleContainerEntry(entry);
                newEntries.add(newGradleContainerEntry);
            } else {
                newEntries.add(entry);
            }
        }

        javaProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[newEntries.size()]), monitor);
    }

    /**
     * Adds the org.eclipse.jst.component.dependency attribute to the Gradle classpath container
     * attribute.
     */
    private IClasspathEntry modifyGradleContainerEntry(IClasspathEntry entry) {
        IClasspathAttribute newAttribute = JavaCore.newClasspathAttribute("org.eclipse.jst.component.dependency", "/WEB-INF/lib");
        List<IClasspathAttribute> gradleContainerAttributes = new ArrayList<IClasspathAttribute>(Arrays.asList(entry.getExtraAttributes()));
        gradleContainerAttributes.add(newAttribute);
        return JavaCore.newContainerEntry(entry.getPath(), entry.getAccessRules(), gradleContainerAttributes
                .toArray(new IClasspathAttribute[gradleContainerAttributes.size()]), entry.isExported());
    }

    private void removeTestFolderLinks(IProject workspaceProject, OmniEclipseProject project, IProgressMonitor monitor) {
        IVirtualComponent component = ComponentCore.createComponent(workspaceProject);
        if (component == null) {
            return;
        }

        IVirtualFolder jsrc = component.getRootFolder().getFolder("/");
        try {
            jsrc.removeLink(new Path("src/test/java"), 0, monitor);
        } catch (CoreException e) {
            // Should be returned in Istatus.
            Activator.getLogger().error(e.getMessage(), e);
        }

        return;
    }

    private void getTestDependencies(ProjectConfigurationRequest configurationRequest) {
        OmniEclipseProject project = configurationRequest.getProject();
        IJavaProject javaProject = JavaCore.create(configurationRequest.getWorkspaceProject());
        IClasspathContainer rootContainer = null;
        try {
            rootContainer = JavaCore.getClasspathContainer(new Path(GradleClasspathContainer.CONTAINER_ID), javaProject);
        } catch (JavaModelException e) {
            e.printStackTrace();
        }

        System.out.println("___ classpath entries");
        for (IClasspathEntry i : rootContainer.getClasspathEntries()) {
            System.out.println("____" + i);
        }

        System.out.println("___ extern dependencies");
        for (OmniExternalDependency i : project.getExternalDependencies()) {
            System.out.println("____" + i.toString());
        }


    }

}
