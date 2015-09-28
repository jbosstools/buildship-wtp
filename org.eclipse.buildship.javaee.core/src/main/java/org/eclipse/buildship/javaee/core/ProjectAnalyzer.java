/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat, Inc.) - initial API and implementation and initial documentation
 */
package org.eclipse.buildship.javaee.core;

import java.io.File;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;

import org.eclipse.core.runtime.IPath;

import org.eclipse.buildship.javaee.core.model.WarModel;

/**
 * Accesses Gradle build settings for a given project.
 */
public class ProjectAnalyzer {

    private static final String INIT_FILE_PATH = Activator.getInstance().getStateLocation().append("init.gradle").toString();

    /**
     * Determines whether the project located at projectPath is a war project.
     */
    public static boolean isWarProject(String projectPath) {
        return getWarModel(projectPath).hasWarPlugin();
    }

    /**
     * Gets the War model of the project located at projectPath.
     */
    public static WarModel getWarModel(String projectPath) {
        ProjectConnection connection = null;
        try {
            GradleConnector connector = initializeConnector(projectPath);
            connection = connector.connect();
            WarModel model = buildWarModel(connection);
            return model;
        } finally {
            closeConnection(connection);
        }

    }

    private static GradleConnector initializeConnector(String projectPath) {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File(projectPath));
        return connector;
    }

    /**
     * Builds the WarModel based on what's defined in the project's Gradle build script.
     */
    private static WarModel buildWarModel(ProjectConnection connection) {
        ModelBuilder<WarModel> customModelBuilder = connection.model(WarModel.class);
        IPath pluginDirectory = Activator.getInstance().getStateLocation().append("repo");
        customModelBuilder.withArguments("--init-script", INIT_FILE_PATH, "-DpluginDirectory=" + pluginDirectory);
        return customModelBuilder.get();
    }

    private static void closeConnection(ProjectConnection connection) {
        if (connection != null) {
            connection.close();
        }
    }

}