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

import java.io.File;
import java.io.Serializable;
import java.util.List;
import org.gradle.api.plugins.WarPlugin;

/**
 * The model implementation for projects that apply the 'war' plug-in.
 */
public class DefaultWarModel implements Serializable {
    private static final long serialVersionUID = 9018119464493565582L;
    private final List<String> pluginClassNames;
    private final File webAppDir;
    private final String webAppDirName;
    private final String webXml;
    private final String deps;

    public DefaultWarModel(List<String> pluginClassNames, File webAppDir, String webAppDirName, String webXml, String deps) {
        this.pluginClassNames = pluginClassNames;
        this.webAppDir = webAppDir;
        this.webAppDirName = webAppDirName;
        this.webXml = webXml;
        this.deps = deps;
    }

    /**
     * Determines whether the project applies the 'war' plug-in.
     */
    public boolean hasWarPlugin() {
        return this.pluginClassNames.contains(WarPlugin.class.getName());
    }

    public File getWebAppDir() {
        return this.webAppDir;
    }

    public String getWebAppDirName() {
        return this.webAppDirName;
    }

    public String getWebXmlName() {
        return this.webXml;
    }

    public String getDeps() {
        return this.deps;
    }

}
