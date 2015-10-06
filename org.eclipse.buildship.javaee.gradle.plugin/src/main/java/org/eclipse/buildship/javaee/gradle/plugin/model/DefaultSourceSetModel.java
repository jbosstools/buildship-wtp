package org.eclipse.buildship.javaee.gradle.plugin.model;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import org.eclipse.buildship.javaee.core.GradleSourceSet;
import org.eclipse.buildship.javaee.core.OmniGradleSourceSet;

public class DefaultSourceSetModel implements Serializable {
    private static final long serialVersionUID = 5433452222852877048L;

    private final Set<File> mainSourceSet;
    private final Set<File> testSourceSet;

    public DefaultSourceSetModel(Set<File> mainSourceSet, Set<File> testSourceSet) {
        this.mainSourceSet = mainSourceSet;
        this.testSourceSet = testSourceSet;
    }

    public OmniGradleSourceSet getSourceSets() {
        return new GradleSourceSet(this.mainSourceSet, this.testSourceSet);
    }
}
