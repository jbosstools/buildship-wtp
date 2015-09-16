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

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Meant for easy testing purposes.
 */
public class Handler extends AbstractHandler {

    /**
     * the command has been executed, so extract extract the needed information
     * from the application context.
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        List<IProject> projects = collectSelectedProjects(event);
        ProjectAnalyzer tar = new ProjectAnalyzer();
        MessageDialog.openInformation(window.getShell(), "Jboss-gradle",
                tar.analyzeProject(projects.get(0).getLocation().toString()).toString());

        return null;
    }

    private List<IProject> collectSelectedProjects(ExecutionEvent event) {
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        Builder<IProject> result = ImmutableList.builder();
        if (currentSelection instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) currentSelection;
            IAdapterManager adapterManager = Platform.getAdapterManager();
            for (Object selectionItem : selection.toList()) {
                IResource resource = (IResource) adapterManager.getAdapter(selectionItem, IResource.class);
                if (resource != null) {
                    IProject project = resource.getProject();
                    result.add(project);
                }
            }
        }
        return result.build();
    }
}