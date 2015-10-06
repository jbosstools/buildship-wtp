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

package org.eclipse.buildship.javaee.gradle.plugin.model.builder;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import org.eclipse.buildship.javaee.gradle.plugin.model.DefaultSourceSetModel;

/**
 * A model builder that builds the war model.
 */
public class SourceSetModelBuilder implements ToolingModelBuilder {

    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals("org.eclipse.buildship.javaee.core.model.SourceSetModel");
    }

    @Override
    public Object buildAll(String modelName, Project project) {
        final JavaPluginConvention javaPluginConvention = project.getConvention().findPlugin(JavaPluginConvention.class);
        SourceSet main = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet test = javaPluginConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);
        return new DefaultSourceSetModel(main.getAllSource().getSrcDirs(), test.getAllSource().getSrcDirs());
    }

}
