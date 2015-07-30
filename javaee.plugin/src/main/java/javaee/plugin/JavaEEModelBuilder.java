package javaee.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.tasks.bundling.War;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import javaee.model.WarModel;
import javaee.model.DefaultWarModel;

public class JavaEEModelBuilder implements ToolingModelBuilder {
	@Override
	public boolean canBuild(String modelName) {
		return modelName.equals(WarModel.class.getName());
	}

	@Override
	public Object buildAll(String modelName, Project project) {
		String webXmlPath = "";
		List<String> pluginClassNames = getPluginClassNames(project);
		String webAppDirName = getWebAppDirName(project);
		File webAppDir = getWebAppDir(project, webAppDirName);
		try {
			War warTask = (War) project.getTasks().getByName("war");
			webXmlPath = getWebXmlPath(warTask);
		} catch (UnknownTaskException e) {
			System.out.println("No war task.");
		}
		return new DefaultWarModel(pluginClassNames, webAppDir, webAppDirName, webXmlPath);
	}

	private static File getWebAppDir(Project project, String webAppDirName) {
		return !project.hasProperty("webAppDir") ? new File(project.getProjectDir(), webAppDirName)
				: new File(project.getProjectDir(), (String) project.property("webAppDirName"));
	}

	private static String getWebAppDirName(Project project) {
		return !project.hasProperty("webAppDir") ? "src/main/webapp"
				: String.valueOf(project.property("webAppDirName"));
	}

	private static List<String> getPluginClassNames(Project project) {
		List<String> pluginClassNames = new ArrayList<String>();
		for (Plugin plugin : project.getPlugins()) {
			pluginClassNames.add(plugin.getClass().getName());
		}
		return pluginClassNames;
	}

	private static String getWebXmlPath(War warTask) {
		String webXmlPath = "";
		File webXml = warTask.getWebXml();
		if (webXml != null) {
			webXmlPath = webXml.getPath();
		} else {
			System.out.println("No web.xml file.");
		}
		return webXmlPath;
	}
}