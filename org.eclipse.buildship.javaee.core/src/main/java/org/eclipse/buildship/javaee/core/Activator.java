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

import java.io.File;
import java.net.URL;
import java.util.Dictionary;

import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.buildship.core.Logger;
import org.eclipse.buildship.core.util.logging.EclipseLogger;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends Plugin {

    public static final String PLUGIN_ID = "org.eclipse.buildship.javaee.core"; //$NON-NLS-1$
    private static Activator plugin;
    private ServiceRegistration<?> loggerService;
    private ServiceTracker<?, ?> loggerServiceTracker;

    public static Activator getInstance() {
        return plugin;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        plugin = this;
        registerServices(context);
        copyGradlePluginFilesToMetadataFolder();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        unregisterServices();
        super.stop(context);

    }

    public void registerServices(BundleContext context) {
        this.loggerService = registerService(context, Logger.class, createLogger(), null);
        this.loggerServiceTracker = createServiceTracker(context, Logger.class);
    }

    public void unregisterServices() {
        this.loggerService.unregister();
        this.loggerServiceTracker.close();
    }

    private <T> ServiceRegistration<?> registerService(BundleContext context, Class<T> clazz, T service, Dictionary<String, Object> properties) {
        return context.registerService(clazz.getName(), service, properties);
    }

    private ServiceTracker<?, ?> createServiceTracker(BundleContext context, Class<?> clazz) {
        ServiceTracker<?, ?> serviceTracker = new ServiceTracker<Object, Object>(context, clazz.getName(), null);
        serviceTracker.open();
        return serviceTracker;
    }

    private EclipseLogger createLogger() {
        return new EclipseLogger(getLog(), PLUGIN_ID, isDebugging());
    }

    public static Logger getLogger() {
        return (Logger) getInstance().loggerServiceTracker.getService();
    }

    /**
     * Copies the init.gradle file and the Gradle plug-in from the Core plug-in directory to the
     * workspace's .metadata folder.
     */
    private void copyGradlePluginFilesToMetadataFolder() throws Exception {
        IPath metadataPath = this.getStateLocation();
        IPath initGradlePath = metadataPath.append("init.gradle");
        IPath pluginPath = metadataPath.append("repo").append("redhat-plugin-1.0.jar");
        IPath repoPath = metadataPath.append("repo");
        IPath guavaPath = metadataPath.append("repo").append("guava-15.0.jar");

        Bundle bundle = Platform.getBundle(PLUGIN_ID);
        URL initUrl = bundle.getEntry("init.gradle");
        URL pluginUrl = bundle.getEntry("/repo/libs/redhat-plugin-1.0.jar");
        URL repoUrl = bundle.getEntry("/repo/");
        URL guavaUrl = bundle.getEntry("/lib/guava-15.0.jar");

        getLogger().info("Init URL   : " + initUrl);
        getLogger().info("Plug-in URL: " + pluginUrl);
        File initFile = new File(FileLocator.toFileURL(initUrl).toURI());
        File pluginFile = new File(FileLocator.toFileURL(pluginUrl).toURI());
        File repoFile = new File(FileLocator.toFileURL(repoUrl).toURI());
        File guavaFile = new File(FileLocator.toFileURL(guavaUrl).toURI());

        FileUtils.copyFile(initFile, new File(initGradlePath.toString()));
        FileUtils.copyFile(pluginFile, new File(pluginPath.toString()));
        FileUtils.copyFile(guavaFile, new File(guavaPath.toString()));
        FileUtils.copyDirectory(repoFile, new File(repoPath.toString()));
    }

}
