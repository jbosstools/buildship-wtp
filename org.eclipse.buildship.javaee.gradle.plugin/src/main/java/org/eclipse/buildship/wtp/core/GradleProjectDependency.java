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

import java.io.Serializable;

import org.eclipse.buildship.wtp.core.OmniGradleProjectDependency;

/**
 * Bundles a Gradle Project Dependency such that it can be retrieved from the DependencyModel.
 */
public class GradleProjectDependency implements Serializable, OmniGradleProjectDependency {

    private static final long serialVersionUID = 1L;

    private String id;
    private String projectPath;

    public GradleProjectDependency(String id, String projectPath) {
        this.id = id;
        this.projectPath = projectPath;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getProjectPath() {
        return this.projectPath;
    }

}
