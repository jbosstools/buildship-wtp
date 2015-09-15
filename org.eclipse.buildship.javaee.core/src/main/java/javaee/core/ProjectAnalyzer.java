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

import javaee.model.WarModel;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;

import org.eclipse.core.runtime.IPath;

public class ProjectAnalyzer {

    private static final String INIT_FILE_PATH = Activator.getDefault().getStateLocation().append("init.gradle").toString();

    /**
     * Analyzes the project located at projectPath.
     */
    public List<String> analyzeProject(String projectPath) {
        GradleConnector connector = initializeConnector(projectPath);
        ProjectConnection connection = null;
        List<String> warProperties;
        System.out.println(INIT_FILE_PATH);
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
        return getWarModel(projectPath).hasWarPlugin();
    }

    public WarModel getWarModel(String projectPath) {
        GradleConnector connector = initializeConnector(projectPath);
        ProjectConnection connection = null;
        try {
            connection = connector.connect();
            WarModel model = getCustomModel(connection);
            return model;
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
        IPath pluginDirectory = Activator.getDefault().getStateLocation().append("repo");
        System.out.println("pluginDir: " + pluginDirectory);
        customModelBuilder.withArguments("--init-script", INIT_FILE_PATH, "-DpluginDirectory=" + pluginDirectory);
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