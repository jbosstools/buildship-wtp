/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ian Stewart-Binks (Red Hat, Inc.) - initial API and implementation and initial documentation
 */
package org.eclipse.buildship.javaee.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.eclipse.buildship.javaee.core"; //$NON-NLS-1$
    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        plugin = this;
        copyGradlePluginFilesToMetadataFolder();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Copies the init.gradle file and the Gradle plug-in from the Core plug-in directory
     * to the workspace's .metadata folder.
     */
    private void copyGradlePluginFilesToMetadataFolder() throws Exception {
        IPath metadataPath = this.getStateLocation();
        IPath initGradlePath = metadataPath.append("init.gradle");
        IPath pluginPath = metadataPath.append("repo").append("redhat-plugin-1.0.jar");

        Bundle bundle = Platform.getBundle(PLUGIN_ID);
        URL initUrl = bundle.getEntry("init.gradle");
        URL pluginUrl = bundle.getEntry("repo/libs/redhat-plugin-1.0.jar");
        File initFile = null;
        File pluginFile = null;

        initFile = new File(FileLocator.resolve(initUrl).toURI());
        pluginFile = new File(FileLocator.resolve(pluginUrl).toURI());

        copyFile(initFile, new File(initGradlePath.toString()));
        copyFile(pluginFile, new File(pluginPath.toString()));
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    @SuppressWarnings("resource")
    /**
     * Copies the contents of sourceFile into destFile. Creates destFile if it does
     * not yet exist.
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.getParentFile().mkdirs();
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

}
