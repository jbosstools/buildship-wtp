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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.buildship.javaee.core.OmniGradleDependency;
import org.eclipse.buildship.javaee.core.OmniGradleProjectDependency;

/**
 * Implementation of the DependencyModel.
 */
public class DefaultDependencyModel implements Serializable {

    private static final long serialVersionUID = -8931280253307342724L;

    private final List<OmniGradleDependency> compileDependencies;
    private final List<OmniGradleDependency> runtimeDependencies;
    private final List<OmniGradleDependency> testCompileDependencies;
    private final List<OmniGradleDependency> testRuntimeDependencies;
    private final List<OmniGradleDependency> providedCompileDependencies;
    private final List<OmniGradleDependency> providedRuntimeDependencies;

    private final List<OmniGradleProjectDependency> compileProjectDependencies;
    private final List<OmniGradleProjectDependency> runtimeProjectDependencies;
    private final List<OmniGradleProjectDependency> testCompileProjectDependencies;
    private final List<OmniGradleProjectDependency> testRuntimeProjectDependencies;
    private final List<OmniGradleProjectDependency> providedCompileProjectDependencies;
    private final List<OmniGradleProjectDependency> providedRuntimeProjectDependencies;

    public DefaultDependencyModel(
            List<OmniGradleDependency> compileDependencies, List<OmniGradleDependency> runtimeDependencies,
            List<OmniGradleDependency> testCompileDependencies, List<OmniGradleDependency> testRuntimeDependencies,
            List<OmniGradleDependency> providedCompileDependencies, List<OmniGradleDependency> providedRuntimeDependencies,
            List<OmniGradleProjectDependency> compileProjectDependencies, List<OmniGradleProjectDependency> runtimeProjectDependencies,
            List<OmniGradleProjectDependency> testCompileProjectDependencies, List<OmniGradleProjectDependency> testRuntimeProjectDependencies,
            List<OmniGradleProjectDependency> providedCompileProjectDependencies, List<OmniGradleProjectDependency> providedRuntimeProjectDependencies) {
        this.compileDependencies = compileDependencies;
        this.runtimeDependencies = runtimeDependencies;
        this.testCompileDependencies = testCompileDependencies;
        this.testRuntimeDependencies = testRuntimeDependencies;
        this.providedCompileDependencies = providedCompileDependencies;
        this.providedRuntimeDependencies = providedRuntimeDependencies;

        this.compileProjectDependencies = compileProjectDependencies;
        this.runtimeProjectDependencies = runtimeProjectDependencies;
        this.testCompileProjectDependencies = testCompileProjectDependencies;
        this.testRuntimeProjectDependencies = testRuntimeProjectDependencies;
        this.providedCompileProjectDependencies = providedCompileProjectDependencies;
        this.providedRuntimeProjectDependencies = providedRuntimeProjectDependencies;
    }

    public List<OmniGradleDependency> getDependenciesForConfiguration(String configuration) {
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
        return new ArrayList<OmniGradleDependency>();
    }

    public List<OmniGradleProjectDependency> getProjectDependenciesForConfiguration(String configuration) {
        if (configuration == "compile") {
            return this.compileProjectDependencies;
        } else if (configuration == "runtime") {
            return this.runtimeProjectDependencies;
        } else if (configuration == "testCompile") {
            return this.testCompileProjectDependencies;
        } else if (configuration == "testRuntime") {
            return this.testRuntimeProjectDependencies;
        } else if (configuration == "providedCompile") {
            return this.providedCompileProjectDependencies;
        } else if (configuration == "providedRuntime") {
            return this.providedRuntimeProjectDependencies;
        }
        return new ArrayList<OmniGradleProjectDependency>();
    }
}
