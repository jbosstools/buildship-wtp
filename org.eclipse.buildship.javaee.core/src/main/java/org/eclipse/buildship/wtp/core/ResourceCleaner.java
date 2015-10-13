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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.wtp.core.model.WarModel;

/**
 * When the Dynamic Web Facet is added to a project, multiple redundant resources are also added to
 * a project. This class is meant to identify those resources and delete them.
 */
public class ResourceCleaner {

    private final IProject project;

    private List<IFile> filesToDelete = new ArrayList<IFile>();
    private List<IFolder> foldersToDelete = new ArrayList<IFolder>();
    private Set<IFolder> resourcesToKeep = new HashSet<IFolder>();

    public ResourceCleaner(IProject project, IFolder... foldersToKeep) {
        Preconditions.checkNotNull(foldersToKeep);
        this.project = Preconditions.checkNotNull(project);

        // Essentially, this ensures that the webAppDir that is created
        // by the web facet persists, even though it may not already exist.
        for (IFolder folder : foldersToKeep) {
            this.resourcesToKeep.add(folder);
            IContainer parent = folder.getParent();
            while (parent instanceof IFolder) {
                this.resourcesToKeep.add((IFolder) parent);
                parent = parent.getParent();
            }
        }
    }

    /**
     * This method catalogs folders/files that are generated WTP for deletion, except under the
     * condition that the folder/file that WTP would have created is an existing folder/file in the
     * project.
     *
     * The ResourceCleaner deletes files that are produced by WTP, however WTP may try to produce
     * folders/files that the project already has. In this instance, the existing folders should not
     * be deleted. However, if the folders don't already exist, we want to remember that they didn't
     * exist, such that they can be purged later.
     *
     * This method *must* be called before clean.
     *
     */
    public void collectWtpFolders(WarModel warModel) {
        String webAppDirName = warModel.getWebAppDirName();
        IFolder webAppDir = this.project.getFolder(webAppDirName);
        String customWebXml = warModel.getWebXmlName();

        // The dynamic web facet may have added folders to the web application directory. As such,
        // we determine whether these folders existed *before* the dynamic web facet has been added.
        if (webAppDir.exists()) {
            addFiles(webAppDir.getFile("META-INF/MANIFEST.MF").getProjectRelativePath()); //$NON-NLS-1$
            addFolder(webAppDir.getFolder("WEB-INF/lib").getProjectRelativePath()); //$NON-NLS-1$
        }

        if (!customWebXml.equals("")) {
            addFiles(webAppDir.getFile("WEB-INF/web.xml").getProjectRelativePath()); //$NON-NLS-1$
        }

    }

    /**
     * Adds a folder to the folders to be deleted.
     */
    private void addFolder(IPath folderPath) {
        IFolder folder = this.project.getFolder(folderPath);

        if (folder != null && !folder.exists()) {
            this.foldersToDelete.add(folder);
            addNonexistentParentFolders(folder);
        }
    }

    /**
     * Adds a file set to the set of files to be deleted.
     */
    private void addFiles(IPath... filePaths) {
        for (IPath fileName : filePaths) {
            IFile fileToDelete = this.project.getFile(fileName);
            if (!fileToDelete.exists()) {
                this.filesToDelete.add(fileToDelete);
                addNonexistentParentFolders(fileToDelete);
            }
        }
    }

    /**
     * Determines if a resource's parent resources are redundant resources.
     */
    private void addNonexistentParentFolders(IResource resource) {
        IContainer parentContainer = resource.getParent();

        while (containerShouldBeDeleted(parentContainer)) {
            this.foldersToDelete.add((IFolder) parentContainer);
            parentContainer = parentContainer.getParent();
        }
    }

    /**
     * Determines if a container should be deleted in the future.
     *
     * Containers are flagged as folders to delete in the future if they satisfy three conditions:
     * 1. The container is an instance of IFolder
     * 2. The container has not been flagged as a protected resource.
     * 3. The resource already exists in the project.
     */
    private boolean containerShouldBeDeleted(IContainer container) {
        return container instanceof IFolder && !this.resourcesToKeep.contains(container) && !container.exists();
    }

    /**
     * Cleans up folders that WTP has generated that were cataloged by the collectWtpFolders method.
     * This method *must* be called after the collectWtpFolders in order to delete anything.
     */
    public void clean(IProgressMonitor monitor) throws CoreException {
        for (IFile file : this.filesToDelete) {
            if (file.exists()) {
                file.delete(true, monitor);
            }
        }

        for (IFolder folder : this.foldersToDelete) {
            if (folder.exists()) {
                folder.delete(true, monitor);
            }
        }
    }

}
