package org.eclipse.buildship.javaee.core;

import java.io.File;
import java.util.Set;

public interface OmniGradleSourceSet {
    Set<File> getMainSourceSet();
    Set<File> getTestSourceSet();
}
