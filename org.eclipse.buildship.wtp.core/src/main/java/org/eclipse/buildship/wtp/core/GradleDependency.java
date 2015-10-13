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

/**
 * Bundles a Gradle Dependency such that it can be retrieved from the DependencyModel.
 * The Gradle Dependency cannot itself do this, as it needs to be serializable.
 */
public class GradleDependency implements Serializable {

    private static final long serialVersionUID = 7436966156072507360L;
    private String name;
    private String group;
    private String version;

    public GradleDependency(String name, String group, String version) {
        this.name = name;
        this.group = group;
        this.version = version;
    }

    public String getName() {
        return this.name;
    }

    public String getGroup() {
        return this.group;
    }

    public String getVersion() {
        return this.version;
    }

}
