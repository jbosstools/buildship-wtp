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

package org.eclipse.buildship.javaee.gradle.plugin.model.builder;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.javaee.gradle.plugin.GradleDependency;
import org.eclipse.buildship.javaee.gradle.plugin.model.DefaultDependencyModel;

/**
 * A model builder that builds the dependency model.
 */
public class DependencyModelBuilder implements ToolingModelBuilder {

    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals("org.eclipse.buildship.javaee.core.model.DependencyModel");
    }

    @Override
    public Object buildAll(String modelName, Project project) {
        ImmutableList<GradleDependency> compileDependencies = getDependenciesForConfiguration(project, "compile");
        ImmutableList<GradleDependency> runtimeDependencies = getDependenciesForConfiguration(project, "runtime");
        ImmutableList<GradleDependency> testCompileDependencies = getDependenciesForConfiguration(project, "testCompile");
        ImmutableList<GradleDependency> testRuntimeDependencies = getDependenciesForConfiguration(project, "testRuntime");
        ImmutableList<GradleDependency> providedCompileDependencies = getDependenciesForConfiguration(project, "providedCompile");
        ImmutableList<GradleDependency> providedRuntimeDependencies = getDependenciesForConfiguration(project, "providedRuntime");

        return new DefaultDependencyModel(compileDependencies, runtimeDependencies, testCompileDependencies, testRuntimeDependencies, providedCompileDependencies,
                providedRuntimeDependencies);
    }

    private ImmutableList<GradleDependency> getDependenciesForConfiguration(Project project, String configuration) {
        DependencySet dependencies = project.getConfigurations().getByName(configuration).getAllDependencies();

        return FluentIterable.from(dependencies).transform(new Function<Dependency, GradleDependency>() {

            @Override
            public GradleDependency apply(Dependency dependency) {
                return new GradleDependency(dependency.getName(), dependency.getGroup(), dependency.getVersion());

            }
        }).toList();
    }

}
