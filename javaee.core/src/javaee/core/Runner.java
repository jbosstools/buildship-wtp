package javaee.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;

public class Runner {

    private static final String INIT_FILE_PATH = Activator.getDefault().getStateLocation().append("init.gradle").toString();

    /**
     * Analyzes the project located at projectPath.
     * 
     * @param projectPath
     *            The path of the project to be analyzed.
     */
    public List<String> analyzeProject(String projectPath) {
        GradleConnector connector = initializeConnector(projectPath);
        ProjectConnection connection = null;
        List<String> lis;
        try {
            connection = connector.connect();
            WarModel model = getCustomModel(connection);
            lis = analyzeWarProperties(model);
        } finally {
            closeConnection(connection);
        }
        return lis;
    }

    private GradleConnector initializeConnector(String projectPath) {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File(projectPath));
        return connector;
    }

    private static WarModel getCustomModel(ProjectConnection connection) {
        ModelBuilder<WarModel> customModelBuilder = connection.model(WarModel.class);
        customModelBuilder.withArguments("--init-script", INIT_FILE_PATH);
        return customModelBuilder.get();
    }

    private List<String> analyzeWarProperties(WarModel model) {
        if (model.hasWarPlugin()) {
            String a = model.getWebAppDirName();
            String b = model.getWebXmlName();
            return Arrays.asList(a, b);
        }
        return new ArrayList<String>();
    }

    private void closeConnection(ProjectConnection connection) {
        if (connection != null) {
            connection.close();
        }
    }
}