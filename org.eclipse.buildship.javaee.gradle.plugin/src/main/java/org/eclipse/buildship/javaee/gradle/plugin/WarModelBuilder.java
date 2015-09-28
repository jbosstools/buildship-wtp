/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat, Inc.) -
 */
package org.eclipse.buildship.javaee.gradle.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.tasks.bundling.War;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

/**
 * A model builder that builds the war model.
 */
public class WarModelBuilder implements ToolingModelBuilder {

    @Override
    public boolean canBuild(String modelName) {
        //TODO: Check to see if there is an alternative to hardcoding the name in here.
        return modelName.equals("org.eclipse.buildship.javaee.core.model.WarModel");
    }

    @Override
    public Object buildAll(String modelName, Project project) {
        String webXmlPath = "";
        List<String> pluginClassNames = getPluginClassNames(project);
        String webAppDirName = getWebAppDirName(project);
        File webAppDir = getWebAppDir(project, webAppDirName);
        try {
            War warTask = (War) project.getTasks().getByName("war");
            webXmlPath = getWebXmlPath(warTask);
        } catch (UnknownTaskException e) {
            System.out.println("No war task.");
        }
        return new DefaultWarModel(pluginClassNames, webAppDir, webAppDirName, webXmlPath);
    }

    private static File getWebAppDir(Project project, String webAppDirName) {
        return !project.hasProperty("webAppDir") ? new File(project.getProjectDir(), webAppDirName)
                : new File(project.getProjectDir(), (String) project.property("webAppDirName"));
    }

    private static String getWebAppDirName(Project project) {
        return !project.hasProperty("webAppDir") ? "src/main/webapp"
                : String.valueOf(project.property("webAppDirName"));
    }

    private static List<String> getPluginClassNames(Project project) {
        List<String> pluginClassNames = new ArrayList<String>();
        for (Plugin<?> plugin : project.getPlugins()) {
            pluginClassNames.add(plugin.getClass().getName());
        }
        return pluginClassNames;
    }

    private static String getWebXmlPath(War warTask) {
        String webXmlPath = "src/main/webapp/WEB-INF/web.xml";
        File webXml = warTask.getWebXml();
        if (webXml != null) {
            webXmlPath = webXml.getPath();
        }
        return webXmlPath;
    }

}
