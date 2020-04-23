package com.github.terminatornl.laggoggles.tickcentral;

import com.github.terminatornl.laggoggles.Main;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import javax.annotation.Nonnull;
import java.util.*;

public class Initializer implements com.github.terminatornl.tickcentral.api.TransformerSupplier {

	Side side;

	@Override
	public void onLoad(LaunchClassLoader loader, Side side) {
		this.side = side;
		loader.addTransformerExclusion("com.github.terminatornl.laggoggles.tickcentral.EventBusTransformer");
		loader.addTransformerExclusion("com.github.terminatornl.laggoggles.tickcentral.RenderManagerTransformer");
		loader.addTransformerExclusion("com.github.terminatornl.laggoggles.Main");
	}

	@Nonnull
	@Override
	public Collection<Class<? extends IClassTransformer>> getLastTransformers() {
		ArrayList<Class<? extends IClassTransformer>> list = new ArrayList<>();
		list.add(EventBusTransformer.class);
		if(side.isClient()){
			list.add(RenderManagerTransformer.class);
		}
		return list;
	}

	@Nonnull
	@Override
	public Collection<String> getTransformers() {
		ArrayList<String> list = new ArrayList<>();
		list.add(EventBusTransformer.class.getName());
		if(side.isClient()){
			list.add(RenderManagerTransformer.class.getName());
		}
		return list;
	}



	public static void convertTargetInstruction(String targetOwner, String targetName, String targetDesc, String newOwner, String newName, String newDesc, int newOpcode, InsnList instructions) {
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			switch (node.getOpcode()) {
				case Opcodes.INVOKEINTERFACE:
				case Opcodes.INVOKESPECIAL:
				case Opcodes.INVOKESTATIC:
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

	public static void renameTargetInstruction(String targetName, String newName, InsnList instructions) {
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
		int count = 0;
		List<String> checked = new LinkedList<>();
		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			switch (node.getOpcode()) {
				case Opcodes.INVOKEINTERFACE:
				case Opcodes.INVOKESPECIAL:
				case Opcodes.INVOKESTATIC:
				case Opcodes.INVOKEVIRTUAL:
					MethodInsnNode methodNode = (MethodInsnNode) node;
					if (methodNode.name.equals(targetName)) {
						methodNode.name = newName;
						count++;
					}else{
						checked.add(methodNode.name + methodNode.desc);
					}
					break;
			}
		}
		if(count == 0){
			throw new IllegalStateException("Did not find target method: " + targetName + ". Checked: " + StringUtils.join(checked, System.lineSeparator()));
		}
	}
}
