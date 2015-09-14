/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ian Stewart-Binks (Red Hat, Inc.) - initial API and implementation and initial documentation
 */
package javaee.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;

import javaee.model.WarModel;

public class ProjectAnalyzer {

    private static final String INIT_FILE_PATH = Activator.getDefault().getStateLocation().append("init.gradle").toString();

    /**
     * Analyzes the project located at projectPath.
     */
    public List<String> analyzeProject(String projectPath) {
        GradleConnector connector = initializeConnector(projectPath);
        ProjectConnection connection = null;
        List<String> warProperties;
        try {
            connection = connector.connect();
            WarModel model = getCustomModel(connection);
            warProperties = analyzeWarProperties(model);
        } finally {
            closeConnection(connection);
        }
        return warProperties;
    }
    
    public boolean isWarProject(String projectPath) {
        GradleConnector connector = initializeConnector(projectPath);
        ProjectConnection connection = null;
        try {
            connection = connector.connect();
            WarModel model = getCustomModel(connection);
            return model.hasWarPlugin();
        } finally {
            closeConnection(connection);
        }
    }

    private GradleConnector initializeConnector(String projectPath) {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File(projectPath));
        return connector;
    }

    private static WarModel getCustomModel(ProjectConnection connection) {
        ModelBuilder<WarModel> customModelBuilder = connection.model(WarModel.class);
        customModelBuilder.withArguments("--init-script", INIT_FILE_PATH, "-DpluginDirectory=" + Activator.getDefault().getStateLocation().append("repo"));
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