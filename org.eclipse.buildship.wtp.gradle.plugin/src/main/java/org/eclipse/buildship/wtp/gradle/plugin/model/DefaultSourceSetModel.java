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

package org.eclipse.buildship.wtp.gradle.plugin.model;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import org.eclipse.buildship.wtp.core.GradleSourceSet;
import org.eclipse.buildship.wtp.core.OmniGradleSourceSet;

/**
 * Gradle source set model.
 */
public class DefaultSourceSetModel implements Serializable {
    private static final long serialVersionUID = 5433452222852877048L;

    private final Set<File> mainSourceSet;
    private final Set<File> testSourceSet;

    public DefaultSourceSetModel(Set<File> mainSourceSet, Set<File> testSourceSet) {
        this.mainSourceSet = mainSourceSet;
        this.testSourceSet = testSourceSet;
    }

    public OmniGradleSourceSet getSourceSets() {
        return new GradleSourceSet(this.mainSourceSet, this.testSourceSet);
    }
}
