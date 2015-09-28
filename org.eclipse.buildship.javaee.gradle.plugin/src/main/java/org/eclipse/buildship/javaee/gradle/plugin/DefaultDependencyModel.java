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

package org.eclipse.buildship.javaee.gradle.plugin;

import java.io.Serializable;

import com.google.common.collect.ImmutableList;

/**
 * Implementation of the DependencyModel.
 */
public class DefaultDependencyModel implements Serializable {

    private static final long serialVersionUID = -8931280253307342724L;
    private final ImmutableList<String> compileDependencies;
    private final ImmutableList<String> runtimeDependencies;
    private final ImmutableList<String> testCompileDependencies;
    private final ImmutableList<String> testRuntimeDependencies;
    private final ImmutableList<String> providedCompileDependencies;
    private final ImmutableList<String> providedRuntimeDependencies;

    public DefaultDependencyModel(ImmutableList<String> compileDependencies, ImmutableList<String> runtimeDependencies, ImmutableList<String> testCompileDependencies,
            ImmutableList<String> testRuntimeDependencies, ImmutableList<String> providedCompileDependencies, ImmutableList<String> providedRuntimeDependencies) {
        this.compileDependencies = compileDependencies;
        this.runtimeDependencies = runtimeDependencies;
        this.testCompileDependencies = testCompileDependencies;
        this.testRuntimeDependencies = testRuntimeDependencies;
        this.providedCompileDependencies = providedCompileDependencies;
        this.providedRuntimeDependencies = providedRuntimeDependencies;
    }

    ImmutableList<String> getDependenciesForConfiguraion(String configuration) {
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
