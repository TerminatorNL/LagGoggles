package com.github.terminatornl.laggoggles.tickcentral;

import com.github.terminatornl.laggoggles.profiler.ProfileManager;
import com.github.terminatornl.tickcentral.api.ClassDebugger;
import com.github.terminatornl.tickcentral.api.ClassSniffer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Field;

import static com.github.terminatornl.laggoggles.profiler.ProfileManager.timingManager;

public class EventBusTransformer implements IClassTransformer {

	public static final Field ownerField;

	static {
		try {
			ownerField = ASMEventHandler.class.getDeclaredField("owner");
			ownerField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null || transformedName.equals("net.minecraftforge.fml.common.eventhandler.EventBus") == false) {
			return basicClass;
		}
		ClassReader reader = new ClassReader(basicClass);
		ClassNode classNode = new ClassNode();
		reader.accept(classNode, 0);

		for (MethodNode method : classNode.methods) {
			//if (method.desc.equals("(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z")) {
				Initializer.convertTargetInstruction(
						"net/minecraftforge/fml/common/eventhandler/IEventListener",
						"invoke",
						"(Lnet/minecraftforge/fml/common/eventhandler/Event;)V",
						getClass().getName().replace(".","/"),
						"redirectEvent",
						"(Lnet/minecraftforge/fml/common/eventhandler/IEventListener;Lnet/minecraftforge/fml/common/eventhandler/Event;)V",
						Opcodes.INVOKESTATIC,
						method.instructions);
			//}
		}

		try {
			return ClassDebugger.WriteClass(classNode, transformedName);
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	@SuppressWarnings("unused")
	public static void redirectEvent(IEventListener listener, Event event){
		if(ProfileManager.PROFILER_ENABLED_UPDATE_SAFE == false){
			listener.invoke(event);
			return;
		}

		long start = System.nanoTime();
		listener.invoke(event);
		long nanos = System.nanoTime() - start;

		if(listener instanceof ASMEventHandler) {
			String identifier;
			try {
				ModContainer container = (ModContainer) ownerField.get(listener);
				identifier = container.getName() + " (" + container.getSource().getName() + ")";
			} catch (IllegalAccessException e) {
				identifier = "Unknown: " + listener.hashCode();
			}
			timingManager.addEventTime(identifier, event, nanos);
		}
	}
}
