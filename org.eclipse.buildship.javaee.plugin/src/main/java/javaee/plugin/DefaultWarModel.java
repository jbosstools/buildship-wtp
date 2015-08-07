package javaee.plugin;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import org.gradle.api.plugins.WarPlugin;

public class DefaultWarModel implements Serializable {
    private final List<String> pluginClassNames;
    private final File webAppDir;
    private final String webAppDirName;
    private final String webXml;

    public DefaultWarModel(List<String> pluginClassNames, File webAppDir, String webAppDirName, String webXml) {
        this.pluginClassNames = pluginClassNames;
        this.webAppDir = webAppDir;
        this.webAppDirName = webAppDirName;
        this.webXml = webXml;
    }

    public boolean hasWarPlugin() {
        return pluginClassNames.contains(WarPlugin.class.getName());
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
}