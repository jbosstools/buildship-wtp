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

package org.eclipse.buildship.wtp.core;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import org.eclipse.buildship.wtp.core.OmniGradleSourceSet;

/**
 * Gets Gradle main and test source sets.
 */
public class GradleSourceSet implements Serializable, OmniGradleSourceSet {
    private static final long serialVersionUID = -3649406572717928289L;

    private final Set<File> mainSourceSet;
    private final Set<File> testSourceSet;

    public GradleSourceSet(Set<File> mainSourceSet, Set<File> testSourceSet) {
        this.mainSourceSet = mainSourceSet;
        this.testSourceSet = testSourceSet;
    }

    @Override
    public Set<File> getMainSourceSet() {
        return this.mainSourceSet;
    }

    @Override
    public Set<File> getTestSourceSet() {
        return this.testSourceSet;
    }
}
