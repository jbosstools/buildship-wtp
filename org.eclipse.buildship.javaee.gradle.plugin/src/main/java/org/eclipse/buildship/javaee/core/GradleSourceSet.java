package org.eclipse.buildship.javaee.core;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

public class GradleSourceSet implements Serializable, OmniGradleSourceSet {
    private static final long serialVersionUID = -3649406572717928289L;

    private final Set<File> mainSourceSet;
    private final Set<File> testSourceSet;

    public GradleSourceSet(Set<File> mainSourceSet, Set<File> testSourceSet) {
        this.mainSourceSet = mainSourceSet;
        this.testSourceSet = testSourceSet;
    }

    @Override
    public Set<File> getMainSourceSet() {
        return this.mainSourceSet;
    }

    @Override
    public Set<File> getTestSourceSet() {
        return this.testSourceSet;
    }
}
