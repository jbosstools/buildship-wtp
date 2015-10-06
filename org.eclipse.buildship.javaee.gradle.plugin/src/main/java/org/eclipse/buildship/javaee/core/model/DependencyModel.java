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

package org.eclipse.buildship.javaee.core.model;

import java.util.List;

import org.eclipse.buildship.javaee.core.OmniGradleDependency;

/**
 * Used to interact with a project's dependencies.
 */
public interface DependencyModel {
    List<OmniGradleDependency> getDependenciesForConfiguration(String configuration);
}
