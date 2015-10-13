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

package org.eclipse.buildship.wtp.gradle.plugin;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import org.eclipse.buildship.wtp.gradle.plugin.model.builder.DependencyModelBuilder;
import org.eclipse.buildship.wtp.gradle.plugin.model.builder.SourceSetModelBuilder;
import org.eclipse.buildship.wtp.gradle.plugin.model.builder.WarModelBuilder;

/**
 * A Gradle plug-in that registers all model builders.
 */
public class JavaEEPlugin implements Plugin<Project> {
    private final ToolingModelBuilderRegistry registry;

    @Inject
    public JavaEEPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(Project project) {
        this.registry.register(new WarModelBuilder());
        this.registry.register(new DependencyModelBuilder());
        this.registry.register(new SourceSetModelBuilder());
    }
}
