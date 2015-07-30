package javaee.plugin;

import javax.inject.Inject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

public class JavaEEPlugin implements Plugin<Project> {
	private final ToolingModelBuilderRegistry registry;

    @Inject
    public JavaEEPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

	@Override
	public void apply(Project project) {
		registry.register(new JavaEEModelBuilder());
	}
}