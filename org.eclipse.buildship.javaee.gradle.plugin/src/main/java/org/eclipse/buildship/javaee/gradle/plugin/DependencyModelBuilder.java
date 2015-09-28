package org.eclipse.buildship.javaee.gradle.plugin;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

public class DependencyModelBuilder implements ToolingModelBuilder {


    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals("org.eclipse.buildship.javaee.core.model.DependencyModel");
    }

    @Override
    public Object buildAll(String modelName, Project project) {
        ImmutableList<String> compileDependencies = getDependenciesForConfiguration(project, "compile");
        ImmutableList<String> runtimeDependencies = getDependenciesForConfiguration(project, "runtime");
        ImmutableList<String> testCompileDependencies = getDependenciesForConfiguration(project, "testCompile");
        ImmutableList<String> testRuntimeDependencies = getDependenciesForConfiguration(project, "testRuntime");
        ImmutableList<String> providedCompileDependencies = getDependenciesForConfiguration(project, "providedCompile");
        ImmutableList<String> providedRuntimeDependencies = getDependenciesForConfiguration(project, "providedRuntime");

        return new DefaultDependencyModel(compileDependencies, runtimeDependencies, testCompileDependencies, testRuntimeDependencies, providedCompileDependencies, providedRuntimeDependencies);
    }

    private ImmutableList<String> getDependenciesForConfiguration(Project project, String configuration) {
        DependencySet dependencies = project.getConfigurations().getByName(configuration).getDependencies();

        return FluentIterable.from(dependencies).transform(new Function<Dependency, String>() {
            @Override
            public String apply(Dependency dependency) {
                return dependency.getName();
            }
        }).toList();
    }

}
