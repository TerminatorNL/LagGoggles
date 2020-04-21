package cf.terminator.laggoggles.tickcentral;

import cf.terminator.laggoggles.profiler.ProfileManager;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.apache.logging.log4j.ThreadContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

import static cf.terminator.laggoggles.profiler.ProfileManager.timingManager;

public class EventBusTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null || transformedName.equals("net.minecraftforge.fml.common.eventhandler.EventBus") == false) {
			return basicClass;
		}
		ClassReader reader = new ClassReader(basicClass);
		ClassNode classNode = new ClassNode();
		reader.accept(classNode, 0);

		for (MethodNode method : classNode.methods) {
			if (method.name.equals("post") && method.desc.equals("(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z")) {
				convertTargetInstruction(
						"net/minecraftforge/fml/common/eventhandler/IEventListener",
						"invoke",
						"(Lnet/minecraftforge/fml/common/eventhandler/Event;)V",
						"cf.terminator.laggoggles.tickcentral.EventBusTransformer",
						"redirectEvent",
						"(Lnet/minecraftforge/fml/common/eventhandler/IEventListener;Lnet/minecraftforge/fml/common/eventhandler/Event;)V",
						Opcodes.INVOKESTATIC,
						method.instructions);
			}
		}


		return basicClass;
	}

	public static void test(IEventListener e, Event n){
		redirectEvent(e,n);
	}

	@SuppressWarnings("unused")
	public static void redirectEvent(IEventListener listener, Event event){
		if(ProfileManager.PROFILE_ENABLED.get() == false){
			listener.invoke(event);
		}

		long start = System.nanoTime();
		listener.invoke(event);
		long nanos = System.nanoTime() - start;

		if(listener instanceof ASMEventHandler) {
			timingManager.addEventTime(ThreadContext.get("mod"), event, nanos);
		}
	}


	public static void convertTargetInstruction(String targetOwner, String targetName, String targetDesc, String newOwner, String newName, String newDesc, int newOpcode,InsnList instructions) {
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			switch (node.getOpcode()) {
				case org.objectweb.asm.Opcodes.INVOKEINTERFACE:
				case org.objectweb.asm.Opcodes.INVOKESPECIAL:
				case org.objectweb.asm.Opcodes.INVOKESTATIC:
				case Opcodes.INVOKEVIRTUAL:
					MethodInsnNode methodNode = (MethodInsnNode) node;
					if (methodNode.owner.equals(targetOwner) && methodNode.name.equals(targetName) && methodNode.desc.equals(targetDesc)) {
						methodNode.name = newName;
						methodNode.owner = newOwner;
						methodNode.setOpcode(newOpcode);
						methodNode.desc = newDesc;
					}
					break;
			}
		}
	}


	/*
	INVOKEINTERFACE net/minecraftforge/fml/common/eventhandler/IEventListener.invoke (Lnet/minecraftforge/fml/common/eventhandler/Event;)V
	*/

}
