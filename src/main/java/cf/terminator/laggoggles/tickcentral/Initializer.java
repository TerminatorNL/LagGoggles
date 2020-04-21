package cf.terminator.laggoggles.tickcentral;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class Initializer implements com.github.terminatornl.tickcentral.api.TransformerSupplier {

	@Override
	public void onLoad(LaunchClassLoader loader) {
		loader.addTransformerExclusion("cf.terminator.laggoggles.tickcentral.EventBusTransformer");
	}

	@Nonnull
	@Override
	public Collection<Class<? extends IClassTransformer>> getLastTransformers() {
		return Collections.singleton(EventBusTransformer.class);
	}

	@Nonnull
	@Override
	public Collection<String> getTransformers() {
		return Collections.singleton(EventBusTransformer.class.getName());
	}
}
