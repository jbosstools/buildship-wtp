package org.eclipse.buildship.javaee.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import com.google.common.base.Preconditions;

import org.eclipse.buildship.javaee.core.model.WarModel;

/**
 * When the Dynamic Web Facet is added to a project, multiple redundant resources are also added to
 * a project. This class is meant to identify those resources and delete them.
 */
public class ResourceCleaner {

    private final IProject project;

    private List<IFile> filesToDelete = new ArrayList<IFile>();
    private List<IFolder> foldersToDelete = new ArrayList<IFolder>();
    private Set<IFolder> existingResources = new HashSet<IFolder>();

    public ResourceCleaner(IProject project, IFolder... foldersToKeep) {
        Preconditions.checkNotNull(foldersToKeep);
        this.project = Preconditions.checkNotNull(project);

        for (IFolder folder : foldersToKeep) {
            if (folder.exists()) {
                this.existingResources.add(folder);
                IContainer parent = folder.getParent();
                while (parent instanceof IFolder) {
                    this.existingResources.add((IFolder) parent);
                    parent = parent.getParent();
                }
            }
        }
    }

    /**
     * This method catalogues folders/files that are generated WTP for deletion, except under the
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
        Preconditions.checkNotNull(warModel);

        String webAppDirName = warModel.getWebAppDirName();
        IFolder webAppDir = this.project.getFolder(webAppDirName);
        String customWebXml = warModel.getWebXmlName();

        if (webAppDir.exists()) {
            addFiles(webAppDir.getFile("META-INF/MANIFEST.MF").getProjectRelativePath()); //$NON-NLS-1$
            addFolder(webAppDir.getFolder("WEB-INF/lib").getProjectRelativePath()); //$NON-NLS-1$
        } else {
            addFolder(webAppDir.getProjectRelativePath());
        }

        if (!customWebXml.equals("")) {
            addFiles(webAppDir.getFile("WEB-INF/web.xml").getProjectRelativePath()); //$NON-NLS-1$
        }

    }

    private void addFolder(IPath folderPath) {
        IFolder folder = this.project.getFolder(folderPath);

        if (folder != null && !folder.exists() && !folder.getProject().getFullPath().equals(folder.getFullPath())) {
            this.foldersToDelete.add(folder);
            addNonexistentParentFolders(folder);
        }
    }

    private void addFiles(IPath... filePaths) {
        Preconditions.checkNotNull(filePaths);

        for (IPath fileName : filePaths) {
            IFile fileToDelete = this.project.getFile(fileName);
            if (!fileToDelete.exists()) {
                this.filesToDelete.add(fileToDelete);
                addNonexistentParentFolders(fileToDelete);
            }
        }
    }

    /**
     * Determines if a resource's parent resources are redundant WTP resources.
     */
    private void addNonexistentParentFolders(IResource resource) {
        IContainer parentContainer = resource.getParent();
        while (parentContainer instanceof IFolder) {
            if (this.existingResources.contains(parentContainer) || parentContainer.exists()) {
                break;
            }
            IFolder parent = (IFolder) parentContainer;
            this.foldersToDelete.add(parent);
            parentContainer = parentContainer.getParent();
        }
    }

    /**
     * Cleans up folders that WTP has generated that were catalogued by the collectWtpFolders
     * method. This method *must* be called after the collectWtpFolders in order to delete anything.
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
