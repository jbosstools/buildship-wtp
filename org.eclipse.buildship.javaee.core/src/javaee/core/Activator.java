package javaee.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    // The plug-in ID
    public static final String PLUGIN_ID = "javaee.core"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;
    
    public Activator() {
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        IPath metadataPath = plugin.getStateLocation();
        IPath initGradlePath = metadataPath.append("init.gradle");
        IPath pluginPath = metadataPath.append("repo").append("redhat-plugin-1.0.jar");
        Bundle bundle = Platform.getBundle("javaee.core");
        URL initUrl = bundle.getEntry("init.gradle");
        URL pluginUrl = bundle.getEntry("repo/libs/redhat-plugin/1.0/redhat-plugin-1.0.jar");
        File initFile = null;
        File pluginFile = null;
        try {
            initFile = new File(FileLocator.resolve(initUrl).toURI());
            pluginFile = new File(FileLocator.resolve(pluginUrl).toURI());
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        replaceLine(initFile, pluginFile.getParentFile().getAbsolutePath());
        copyFile(initFile, new File(initGradlePath.toString()));
        copyFile(pluginFile, new File(pluginPath.toString()));
    }

    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
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
        }
        finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
    
    public static void replaceLine(File file, String line) {
        Path path = Paths.get(file.getAbsolutePath());
        Charset charset = StandardCharsets.UTF_8;

        String content;
        try {
            content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("REDHAT-PLUGIN", line);
            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
