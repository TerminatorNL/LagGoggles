package cf.terminator.laggoggles;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.TreeMap;

public class SpongeIncompatibleTransformer_LEGACY implements IClassTransformer {

    private static final TreeMap<String, String> SRG_NAMES = new TreeMap<String, String>();
    private static final int DELETED_BY_TRANSFORMER = Integer.MIN_VALUE;

    public SpongeIncompatibleTransformer_LEGACY(){
        SRG_NAMES.put("updateEntities",             "func_72939_s");
        SRG_NAMES.put("updateEntitiesWithForce",    "func_72866_a");
        SRG_NAMES.put("Entity.update",            "func_70071_h_");
    }

    @Override
    public byte[] transform(String obfuscated, String name, byte[] basicClass){



        if(name.equals(obfuscated) == false) {
            if(name.equals("net.minecraft.world.World") || SRG_NAMES.containsValue(obfuscated)){
                SRG_NAMES.putIfAbsent(name, obfuscated);
                return patch_World_updateEntities(basicClass);
            }
        }


        return basicClass;
    }


    private byte[] patch_World_updateEntities(byte[] bytes) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            String METHOD_UPDATE_ENTITIES = SRG_NAMES.get("updateEntities");
            String METHOD_UPDATE_ENTITIES_WITH_FORCE = SRG_NAMES.get("updateEntitiesWithForce");
            for(MethodNode method :classNode.methods){
                if(METHOD_UPDATE_ENTITIES.equals(method.name)){
                    System.out.println("Patching: 'updateEntities' -> " + METHOD_UPDATE_ENTITIES);
                    InsnList list = new InsnList();
                    AbstractInsnNode node = method.instructions.getFirst();
                    while(true){
                        /* ITickable */
                        if(node.getOpcode() == Opcodes.CHECKCAST) {
                            TypeInsnNode CAST_NODE = (TypeInsnNode) node;
                            if (CAST_NODE.desc.equals("net/minecraft/util/ITickable")) {
                                System.out.println("Deleting cast (ITickable)");
                                CAST_NODE.setOpcode(DELETED_BY_TRANSFORMER);
                            }
                        }
                        if(node.getOpcode() == Opcodes.INVOKEINTERFACE){
                            MethodInsnNode INTERFACE_NODE = (MethodInsnNode) node;
                            if(INTERFACE_NODE.owner.equals("net/minecraft/util/ITickable")){
                                INTERFACE_NODE.setOpcode(DELETED_BY_TRANSFORMER);
                                System.out.println("Replacing method ITickable#update");
                                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "cf/terminator/laggoggles/profiler/tiles/OnUpdate", "update", "(Lnet/minecraft/tileentity/TileEntity;)V", false));
                            }
                        }




                        if(node.getOpcode() != DELETED_BY_TRANSFORMER) {
                            list.add(node);
                        }
                        node = node.getNext();
                        if(node == null){
                            break;
                        }
                    }

                    method.instructions.clear();
                    method.instructions.add(list);
                }else if(METHOD_UPDATE_ENTITIES_WITH_FORCE.equals(method.name)){
                    System.out.println("Patching: 'updateEntitiesWithForce' -> " + METHOD_UPDATE_ENTITIES);

                    InsnList list = new InsnList();
                    AbstractInsnNode node = method.instructions.getFirst();
                    while(true){
                        if(node.getOpcode() != DELETED_BY_TRANSFORMER) {
                            list.add(node);
                        }
                        node = node.getNext();
                        if(node == null){
                            break;
                        }
                        if(node.getOpcode() == Opcodes.INVOKEVIRTUAL){
                            MethodInsnNode INSERT_NODE = (MethodInsnNode) node;
                            if(INSERT_NODE.owner.equals("net/minecraft/entity/Entity") && INSERT_NODE.name.equals(SRG_NAMES.get("Entity.update"))){
                                INSERT_NODE.setOpcode(DELETED_BY_TRANSFORMER);
                                System.out.println("Redirected entity.update()");
                                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "cf/terminator/laggoggles/profiler/entities/OnUpdate", "update", "(Lnet/minecraft/entity/Entity;)V", false));
                            }

                        }
                    }


                    method.instructions.clear();
                    method.instructions.add(list);
                }
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
