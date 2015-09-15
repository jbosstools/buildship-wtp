package javaee.configurator;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javaee.core.Activator;
import javaee.core.ProjectAnalyzer;
import javaee.model.WarModel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.common.project.facet.core.internal.JavaFacetUtil;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.web.project.facet.IWebFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetInstallDataModelProvider;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

import org.eclipse.buildship.core.configuration.IProjectConfigurator;
import org.eclipse.buildship.core.configuration.ProjectConfigurationRequest;

public class WebApplicationConfigurator implements IProjectConfigurator {

    private static final int WEB_3_1_ID = 31;

    private static final String WEB_3_1_TEXT = "3.1"; //$NON-NLS-1$
    private static final IProjectFacetVersion WEB_31 = WebFacetUtils.WEB_FACET.hasVersion(WEB_3_1_TEXT) ? WebFacetUtils.WEB_FACET.getVersion(WEB_3_1_TEXT) : WebFacetUtils.WEB_30;

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
        String projectPath = configurationRequest.getWorkspaceProject().getLocationURI().getPath();
        IFacetedProject facetedProject;

        try {
            facetedProject = ProjectFacetsManager.create(project, true, monitor);
            installJavaFacet(actions, project, facetedProject);
            installWebFacet(actions, project, facetedProject, projectPath);
            facetedProject.modify(actions, monitor);
        } catch (CoreException e) {
            // change multistatus
            e.printStackTrace();
        }
    }

    // 'Inspired by'
    // https://github.com/eclipse/m2e.wtp/blob/3ef31ea28a4ea6f5728eb38a6f4d2712b8a4d333/org.eclipse.m2e.wtp/src/org/eclipse/m2e/wtp/WebProjectConfiguratorDelegate.java
    private void installJavaFacet(Set<Action> actions, IProject project, IFacetedProject facetedProject) {
        // Source for JavaFacetUtil:
        // https://eclipse.googlesource.com/webtools-common/webtools.common.fproj/+/b01e5326cd9de1afb60f6a25a81c7b152a08b526/plugins/org.eclipse.jst.common.project.facet.core/src/org/eclipse/jst/common/project/facet/core/internal/JavaFacetUtil.java
        // May be worthwhile not using an internal method.
        @SuppressWarnings("restriction")
        IProjectFacetVersion javaFacetVersion = JavaFacet.FACET.getVersion(JavaFacetUtil.getCompilerLevel(project));

        if (!facetedProject.hasProjectFacet(JavaFacet.FACET)) {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.INSTALL, javaFacetVersion, null));
        } else if (!facetedProject.hasProjectFacet(javaFacetVersion)) {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.VERSION_CHANGE, javaFacetVersion, null));
        }
    }

    private void installWebFacet(Set<Action> actions, IProject project, IFacetedProject facetedProject, String projectPath) {
        ProjectAnalyzer analyzer = new ProjectAnalyzer();
        WarModel warModel = analyzer.getWarModel(projectPath);
        IProjectFacetVersion webFacetVersion = getWebFacetVersion(project, warModel);

        IDataModel webModelConfig = DataModelFactory.createDataModel(new WebFacetInstallDataModelProvider());
        webModelConfig.setProperty(IJ2EEModuleFacetInstallDataModelProperties.GENERATE_DD, false);
        webModelConfig.setBooleanProperty(IWebFacetInstallDataModelProperties.ADD_TO_EAR, false);

        if (!facetedProject.hasProjectFacet(WebFacetUtils.WEB_FACET)) {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.INSTALL, webFacetVersion, webModelConfig));
        } else {
            actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.VERSION_CHANGE, webFacetVersion, webModelConfig));
        }
    }

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
                        case WEB_3_1_ID:
                            return WEB_31;
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

        return WebFacetUtils.WEB_25;
    }

}
