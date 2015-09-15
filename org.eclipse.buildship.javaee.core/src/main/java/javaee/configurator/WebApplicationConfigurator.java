package javaee.configurator;

import java.util.LinkedHashSet;
import java.util.Set;

import javaee.core.Activator;
import javaee.core.ProjectAnalyzer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.common.project.facet.core.internal.JavaFacetUtil;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

import org.eclipse.buildship.core.configuration.IProjectConfigurator;
import org.eclipse.buildship.core.configuration.ProjectConfigurationRequest;

public class WebApplicationConfigurator implements IProjectConfigurator {

    @Override
    public boolean canConfigure(ProjectConfigurationRequest configurationRequest) {
        ProjectAnalyzer analyzer = new ProjectAnalyzer();
        String projectPath = configurationRequest.getWorkspaceProject().getLocationURI().getPath();
        return analyzer.isWarProject(projectPath);
    }

    @Override
    public IStatus configure(ProjectConfigurationRequest configurationRequest, IProgressMonitor monitor) {
        MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, "", null);
        configureFacets(configurationRequest, monitor, multiStatus);
        return multiStatus;
    }

    private void configureFacets(ProjectConfigurationRequest configurationRequest, IProgressMonitor monitor, MultiStatus multiStatus) {
        Set<Action> actions = new LinkedHashSet<Action>();
        IProject project = configurationRequest.getWorkspaceProject();
        IFacetedProject facetedProject;

        try {
            facetedProject = ProjectFacetsManager.create(project);
            installJavaFacet(actions, project, facetedProject);
            facetedProject.modify(actions, monitor);
        } catch (CoreException e) {
            // change multistatus
            e.printStackTrace();
        }
    }

    // 'Inspired by' https://github.com/eclipse/m2e.wtp/blob/3ef31ea28a4ea6f5728eb38a6f4d2712b8a4d333/org.eclipse.m2e.wtp/src/org/eclipse/m2e/wtp/WebProjectConfiguratorDelegate.java
    private void installJavaFacet(Set<Action> actions, IProject project, IFacetedProject facetedProject) {
        // Source for JavaFacetUtil: https://eclipse.googlesource.com/webtools-common/webtools.common.fproj/+/b01e5326cd9de1afb60f6a25a81c7b152a08b526/plugins/org.eclipse.jst.common.project.facet.core/src/org/eclipse/jst/common/project/facet/core/internal/JavaFacetUtil.java
        // May be worthwhile not using an internal method.
        IProjectFacetVersion javaFacetVersion = JavaFacet.FACET.getVersion(JavaFacetUtil.getCompilerLevel(project));

        if (!facetedProject.hasProjectFacet(JavaFacet.FACET)) {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.INSTALL, javaFacetVersion, null));
        } else if (!facetedProject.hasProjectFacet(javaFacetVersion)) {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.VERSION_CHANGE, javaFacetVersion, null));
        }
    }

}
