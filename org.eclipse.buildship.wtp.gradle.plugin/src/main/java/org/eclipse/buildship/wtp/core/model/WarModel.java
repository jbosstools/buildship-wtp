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

package org.eclipse.buildship.wtp.core.model;

import java.io.File;

/**
 * The interface used to interact with the Gradle War model. Values pulled from the War model will
 * be values directly declared in a project's build.gradle file.
 */
public interface WarModel {
    boolean hasWarPlugin();
    File getWebAppDir();
    String getWebAppDirName();
    String getWebXmlName();
}
