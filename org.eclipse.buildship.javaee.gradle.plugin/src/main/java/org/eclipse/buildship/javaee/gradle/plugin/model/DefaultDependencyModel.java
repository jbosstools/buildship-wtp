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

import org.eclipse.buildship.javaee.core.GradleDependency;
import org.eclipse.buildship.javaee.core.OmniGradleDep;

/**
 * Implementation of the DependencyModel.
 */
public class DefaultDependencyModel implements Serializable {

    private static final long serialVersionUID = -8931280253307342724L;
    private final List<OmniGradleDep> compileDependencies;
    private final List<OmniGradleDep> runtimeDependencies;
    private final List<OmniGradleDep> testCompileDependencies;
    private final List<OmniGradleDep> testRuntimeDependencies;
    private final List<OmniGradleDep> providedCompileDependencies;
    private final List<OmniGradleDep> providedRuntimeDependencies;

    public DefaultDependencyModel(List<OmniGradleDep> compileDependencies, List<OmniGradleDep> runtimeDependencies,
            List<OmniGradleDep> testCompileDependencies, List<OmniGradleDep> testRuntimeDependencies,
            List<OmniGradleDep> providedCompileDependencies, List<OmniGradleDep> providedRuntimeDependencies) {
        this.compileDependencies = compileDependencies;
        this.runtimeDependencies = runtimeDependencies;
        this.testCompileDependencies = testCompileDependencies;
        this.testRuntimeDependencies = testRuntimeDependencies;
        this.providedCompileDependencies = providedCompileDependencies;
        this.providedRuntimeDependencies = providedRuntimeDependencies;
    }

    public List<OmniGradleDep> getDependenciesForConfiguration(String configuration) {
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
        return new ArrayList<OmniGradleDep>();
    }

}
