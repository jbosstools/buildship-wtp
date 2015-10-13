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

import java.util.List;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.specs.Spec;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.util.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.eclipse.buildship.javaee.core.GradleDependency;
import org.eclipse.buildship.javaee.core.GradleProjectDependency;
import org.eclipse.buildship.javaee.core.OmniGradleDependency;
import org.eclipse.buildship.javaee.core.OmniGradleProjectDependency;
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
        List<OmniGradleDependency> compileDependencies = getDependenciesForConfiguration(project, "compile");
        List<OmniGradleDependency> runtimeDependencies = getDependenciesForConfiguration(project, "runtime");
        List<OmniGradleDependency> testCompileDependencies = getDependenciesForConfiguration(project, "testCompile");
        List<OmniGradleDependency> testRuntimeDependencies = getDependenciesForConfiguration(project, "testRuntime");
        List<OmniGradleDependency> providedCompileDependencies = getDependenciesForConfiguration(project, "providedCompile");
        List<OmniGradleDependency> providedRuntimeDependencies = getDependenciesForConfiguration(project, "providedRuntime");

        List<OmniGradleDependency> compileProjectDependencies = getProjectDependenciesForConfiguration(project, "compile");
        List<OmniGradleDependency> runtimeProjectDependencies = getProjectDependenciesForConfiguration(project, "runtime");
        List<OmniGradleDependency> testCompileProjectDependencies = getProjectDependenciesForConfiguration(project, "testCompile");
        List<OmniGradleDependency> testRuntimeProjectDependencies = getProjectDependenciesForConfiguration(project, "testRuntime");
        List<OmniGradleDependency> providedCompileProjectDependencies = getProjectDependenciesForConfiguration(project, "providedCompile");
        List<OmniGradleDependency> providedRuntimeProjectDependencies = getProjectDependenciesForConfiguration(project, "providedRuntime");

        return new DefaultDependencyModel(
                compileDependencies,
                runtimeDependencies,
                testCompileDependencies,
                testRuntimeDependencies,
                providedCompileDependencies,
                providedRuntimeDependencies,
                compileProjectDependencies,
                runtimeProjectDependencies,
                testCompileProjectDependencies,
                testRuntimeProjectDependencies,
                providedCompileProjectDependencies,
                providedRuntimeProjectDependencies);
    }

    private List<OmniGradleDependency> getDependenciesForConfiguration(Project project, String configuration) {
        DependencySet dependencies = project.getConfigurations().getByName(configuration).getAllDependencies();

        return FluentIterable.from(dependencies).transform(new Function<Dependency, OmniGradleDependency>() {

            @Override
            public OmniGradleDependency apply(Dependency dependency) {
                return new GradleDependency(dependency.getName(), dependency.getGroup(), dependency.getVersion());

            }
        }).toList();
    }

    // Adopted from https://github.com/gradle/gradle/blob/release/subprojects/ide/src/main/java/org/gradle/plugins/ide/internal/resolver/DefaultIdeDependencyResolver.java#L51-L58
    private List<OmniGradleProjectDependency> getProjectDependenciesForConfiguration(Project project, String configuration) {

        ResolutionResult result = project.getConfigurations().getByName(configuration).getIncoming().getResolutionResult();
        final Set<ResolvedComponentResult> projectComponents = CollectionUtils.filter(result.getAllComponents(), new Spec<ResolvedComponentResult>() {
            @Override
            public boolean isSatisfiedBy(ResolvedComponentResult element) {
                return element.getId() instanceof ProjectComponentIdentifier;
            }
        });

        return FluentIterable.from(projectComponents).transform(new Function<ResolvedComponentResult, OmniGradleProjectDependency>() {

            @Override
            public OmniGradleProjectDependency apply(ResolvedComponentResult dependency) {
                String projectPath = ((ProjectComponentIdentifier) dependency.getId()).getProjectPath();
                return new GradleProjectDependency(dependency.getId().toString(), projectPath);

            }
        }).toList();
    }

}
