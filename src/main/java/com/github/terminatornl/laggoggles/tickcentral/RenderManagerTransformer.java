package com.github.terminatornl.laggoggles.tickcentral;

import com.github.terminatornl.laggoggles.Main;
import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.api.ClassDebugger;
import com.github.terminatornl.tickcentral.api.ClassSniffer;
import com.github.terminatornl.tickcentral.asm.Utilities;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class RenderManagerTransformer implements IClassTransformer {

	public static final String TRUE_RENDER_TICK = Main.MODID_LOWER + "_trueRender";
	public static String NORMAL_RENDER_TICK_NAME;

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null) {
			return basicClass;
		}
		try {
			if (NORMAL_RENDER_TICK_NAME == null) {
				NORMAL_RENDER_TICK_NAME = ClassSniffer.performOnSource("net.minecraft.client.renderer.entity.RenderManager", k -> {
					ClassNode classNode = new ClassNode();
					k.accept(classNode, 0);

					for (MethodNode method : classNode.methods) {
						if (method.desc.endsWith(";DDDFFZ)V")) {
							return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, method.name, method.desc);
						}
					}
					throw new IllegalStateException("Did not found the render method in RenderManager!");
				});
				Main.LOGGER.info("Found render tick with name: " + NORMAL_RENDER_TICK_NAME);
			}
			ClassReader reader = new ClassReader(basicClass);
			ClassNode classNode = new ClassNode();
			reader.accept(classNode, 0);

			if (transformedName.equals("net.minecraft.client.renderer.entity.RenderManager")) {
				MethodNode newNode = null;
				for (MethodNode method : classNode.methods) {
					if (method.desc.endsWith(";DDDFFZ)V")) {
						String targetDesc = "(L" + classNode.name + ";" + method.desc.substring(1);
						newNode = CopyMethodAppearance(method);
						newNode.instructions = new InsnList();
						newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
						newNode.instructions.add(new VarInsnNode(Opcodes.DLOAD, 2));
						newNode.instructions.add(new VarInsnNode(Opcodes.DLOAD, 4));
						newNode.instructions.add(new VarInsnNode(Opcodes.DLOAD, 6));
						newNode.instructions.add(new VarInsnNode(Opcodes.FLOAD, 8));
						newNode.instructions.add(new VarInsnNode(Opcodes.FLOAD, 9));
						newNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, 10));
						newNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/github/terminatornl/laggoggles/tickcentral/RenderManagerAdapter", "redirectRenderEntity", targetDesc, false));
						newNode.instructions.add(new InsnNode(Opcodes.RETURN));
						method.name = TRUE_RENDER_TICK;
					}
				}
				classNode.methods.add(newNode);

			} else if (transformedName.equals("com.github.terminatornl.laggoggles.tickcentral.RenderManagerAdapter")) {
				//com.github.terminatornl.laggoggles.tickcentral.RenderManagerAdapter
				for (MethodNode method : classNode.methods) {
					if(method.name.equals("redirectRenderEntity")){
						Initializer.renameTargetInstruction(NORMAL_RENDER_TICK_NAME, TRUE_RENDER_TICK, method.instructions);
					}
				}
			} else {
				return basicClass;
			}
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return ClassDebugger.WriteClass(classNode, transformedName);
		} catch (Throwable e) {
			Main.LOGGER.fatal("Something went wrong!", e);
			FMLCommonHandler.instance().exitJava(1, false);
			throw new RuntimeException(e);
		}
	}


	/**
	 * Copies the method in a way that Java will see this method as identical, but without the body.
	 *
	 * @param node the methodnode
	 * @return a shiny new imitator
	 */
	public static MethodNode CopyMethodAppearance(MethodNode node) {
		MethodNode newNode = new MethodNode();
		newNode.access = node.access;
		newNode.name = node.name;
		newNode.desc = node.desc;
		newNode.signature = node.signature;
		newNode.parameters = node.parameters == null ? null : CopyParameterNodes(node.parameters);
		newNode.exceptions = new LinkedList<>(node.exceptions);
		newNode.attrs = node.attrs == null ? null : new LinkedList<>(node.attrs);
		return newNode;
	}

	@Nonnull
	public static List<ParameterNode> CopyParameterNodes(@Nonnull List<ParameterNode> nodes) {
		List<ParameterNode> list = new LinkedList<>();
		for (ParameterNode node : nodes) {
			list.add(CopyParameterNode(node));
		}
		return list;
	}

	public static ParameterNode CopyParameterNode(ParameterNode node) {
		return new ParameterNode(node.name, node.access);
	}
}
