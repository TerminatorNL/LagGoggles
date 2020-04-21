package com.github.terminatornl.laggoggles.tickcentral;

import com.github.terminatornl.laggoggles.Main;
import com.github.terminatornl.tickcentral.asm.Utilities;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
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
		if(transformedName.equals("net.minecraft.client.renderer.entity.RenderManager") == false && transformedName.equals("com.github.terminatornl.laggoggles.tickcentral.RenderManagerAdapter") == false){
			return basicClass;
		}

		ClassReader reader = new ClassReader(basicClass);
		ClassNode classNode = new ClassNode();
		reader.accept(classNode, 0);
		try {
			Utilities.ensureOrderedLoading(transformedName, reader, getClass().getClassLoader());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}


		if(transformedName.equals("net.minecraft.client.renderer.entity.RenderManager")) {

			MethodNode newNode = null;
			for (MethodNode method : classNode.methods) {
				if (method.desc.endsWith(";DDDFFZ)V")) {
					String targetDesc = "(L" + classNode.name + ";" + method.desc.substring(1);

					newNode = CopyMethodAppearance(method);
					newNode.instructions = new InsnList();
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 5));
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 6));
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 7));
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 8));
					newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 9));
					newNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/github/terminatornl/laggoggles/tickcentral/RenderManagerAdapter", "redirectRenderEntity", targetDesc, false));
					newNode.instructions.add(new InsnNode(Opcodes.RETURN));

					NORMAL_RENDER_TICK_NAME = method.name;
					method.name = TRUE_RENDER_TICK;
				}
			}

			classNode.methods.add(newNode);

		}else{
			//com.github.terminatornl.laggoggles.tickcentral.RenderManagerAdapter
			for (MethodNode method : classNode.methods) {
				if(method.name.equals("redirectRenderEntity")){
					Initializer.renameTargetInstruction(NORMAL_RENDER_TICK_NAME, TRUE_RENDER_TICK, method.instructions);
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}


	/**
	 * Copies the method in a way that Java will see this method as identical, but without the body.
	 * @param node the methodnode
	 * @return a shiny new imitator
	 */
	public static MethodNode CopyMethodAppearance(MethodNode node){
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
	public static List<ParameterNode> CopyParameterNodes(@Nonnull List<ParameterNode> nodes){
		List<ParameterNode> list = new LinkedList<>();
		for (ParameterNode node : nodes) {
			list.add(CopyParameterNode(node));
		}
		return list;
	}

	public static ParameterNode CopyParameterNode(ParameterNode node){
		return new ParameterNode(node.name, node.access);
	}
}
