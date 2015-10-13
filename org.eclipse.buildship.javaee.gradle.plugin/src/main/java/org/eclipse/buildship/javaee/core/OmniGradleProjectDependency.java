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

package org.eclipse.buildship.javaee.core;

/**
 * Bundles a Gradle Project Dependency such that it can be retrieved from the DependencyModel.
 */
public interface OmniGradleProjectDependency {
    public String getId();
    public String getProjectPath();
}
