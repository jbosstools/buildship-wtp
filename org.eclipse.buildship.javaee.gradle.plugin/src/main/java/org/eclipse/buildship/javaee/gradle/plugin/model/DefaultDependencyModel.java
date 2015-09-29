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

package org.eclipse.buildship.javaee.gradle.plugin.model;

import java.io.Serializable;

import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.javaee.gradle.plugin.GradleDependency;

/**
 * Implementation of the DependencyModel.
 */
public class DefaultDependencyModel implements Serializable {

    private static final long serialVersionUID = -8931280253307342724L;
    private final ImmutableList<GradleDependency> compileDependencies;
    private final ImmutableList<GradleDependency> runtimeDependencies;
    private final ImmutableList<GradleDependency> testCompileDependencies;
    private final ImmutableList<GradleDependency> testRuntimeDependencies;
    private final ImmutableList<GradleDependency> providedCompileDependencies;
    private final ImmutableList<GradleDependency> providedRuntimeDependencies;

    public DefaultDependencyModel(ImmutableList<GradleDependency> compileDependencies, ImmutableList<GradleDependency> runtimeDependencies,
            ImmutableList<GradleDependency> testCompileDependencies, ImmutableList<GradleDependency> testRuntimeDependencies,
            ImmutableList<GradleDependency> providedCompileDependencies, ImmutableList<GradleDependency> providedRuntimeDependencies) {
        this.compileDependencies = compileDependencies;
        this.runtimeDependencies = runtimeDependencies;
        this.testCompileDependencies = testCompileDependencies;
        this.testRuntimeDependencies = testRuntimeDependencies;
        this.providedCompileDependencies = providedCompileDependencies;
        this.providedRuntimeDependencies = providedRuntimeDependencies;
    }

    ImmutableList<GradleDependency> getDependenciesForConfiguraion(String configuration) {
        if (configuration == "compile") {
            return this.compileDependencies;
        } else if (configuration == "runtime") {
            return this.runtimeDependencies;
        } else if (configuration == "testCompile") {
            return this.testCompileDependencies;
        } else if (configuration == "testRuntime") {
            return this.testRuntimeDependencies;
        } else if (configuration == "providedCompile") {
            return this.providedCompileDependencies;
        } else if (configuration == "providedRuntime") {
            return this.providedRuntimeDependencies;
        }
        return ImmutableList.of();
    }

}
