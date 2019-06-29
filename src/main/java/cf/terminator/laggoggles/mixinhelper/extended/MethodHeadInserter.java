package cf.terminator.laggoggles.mixinhelper.extended;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.lib.tree.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import static cf.terminator.laggoggles.mixinhelper.MixinConfigPlugin.LOGGER;

/**
 * A really nasty @Inject @At("HEAD") alternative.
 */
public class MethodHeadInserter implements Transformer {

    /**
     * Injects the instructor method at the head of the targeted method.
     * No fancy stuff like CallbackReturnable here.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(ElementType.METHOD)
    public @interface InsertHead {
        String nameRegex();
        String signatureRegex() default "";
    }

    private final ClassNode classNode;

    public MethodHeadInserter(ClassNode classNode){
        this.classNode = classNode;
    }

    @Override
    public void transform() {
        LinkedList<ScheduledAction> scheduledActions = new LinkedList<>();
        for(MethodNode method : classNode.methods){
            if(method.visibleAnnotations == null){
                continue;
            }
            for(AnnotationNode annotation : method.visibleAnnotations){
                if(annotation.desc.equals("Lcf/terminator/laggoggles/mixinhelper/extended/MethodHeadInserter$InsertHead;")){
                    String currentKey = null;
                    String nameRegex = null;
                    String signatureRegex = null;
                    for(Object key_value : annotation.values){
                        if(currentKey == null){
                            currentKey = (String) key_value;
                        }else{
                            if(currentKey.equals("nameRegex")){
                                nameRegex = (String) key_value;
                            }else if(currentKey.equals("signatureRegex")){
                                signatureRegex = (String) key_value;
                            }
                            currentKey = null;
                        }
                    }
                    if(nameRegex == null && signatureRegex == null){
                        LOGGER.fatal("Invalid annotation found. (@MethodHeadInserter.InsertHead)");
                        FMLCommonHandler.instance().exitJava(-1, true);
                    }else{
                        findTargets(scheduledActions, method, nameRegex, signatureRegex);
                    }
                }
            }
        }
        for(ScheduledAction action : scheduledActions){
            action.apply();
        }
    }

    private void findTargets(LinkedList<ScheduledAction> scheduledActions, MethodNode instructor, String nameRegex, String signatureRegex){
        MethodHelper.findMethods(nameRegex, signatureRegex, classNode, new MethodHelper.MethodHandler() {
            @Override
            public void onFoundMethod(MethodNode node) {
                if(instructor.equals(node) == false){
                    scheduledActions.add(new ScheduledAction(instructor, node));
                }
            }
        });
    }

    public class ScheduledAction{
        private final MethodNode instructor;
        private final MethodNode target;

        public ScheduledAction(MethodNode instructor, MethodNode target){
            this.instructor = instructor;
            this.target = target;
        }

        public void apply(){
            LOGGER.info("Doing some additional transforming on " + target.name + "... This is a mixin workaround.");

            InsnList instructions = new InsnList();
            Map<LabelNode, LabelNode> map = new HashMap<>();
            for (ListIterator<AbstractInsnNode> it = instructor.instructions.iterator(); it.hasNext(); ) {
                AbstractInsnNode node = it.next();
                if(node instanceof LabelNode){
                    map.putIfAbsent((LabelNode) node, new LabelNode());
                }
            }
            for (ListIterator<AbstractInsnNode> it = instructor.instructions.iterator(); it.hasNext(); ) {
                instructions.add(it.next().clone(map));
            }

            LOGGER.info("Dropping instructor nodes:");
            dropInstruction(instructions, instructions.getLast());
            dropInstruction(instructions, instructions.getLast());
            dropInstruction(instructions, instructions.getLast());
            dropInstruction(instructions, instructions.getLast());

            target.instructions.insert(new LabelNode());

            target.instructions.insert(instructions);
            target.instructions.resetLabels();

            LOGGER.info("Newly generated method (" + target.name + "):");
            for (ListIterator<AbstractInsnNode> it = target.instructions.iterator(); it.hasNext(); ) {
                AbstractInsnNode node = it.next();
                if(node instanceof FrameNode){
                    it.remove();
                    continue;
                }
            }
            LOGGER.info(Debugging.getInstructions(target));
        }
    }

    public static void dropInstruction(InsnList list, AbstractInsnNode node){
        LOGGER.info("Dropping " + Debugging.getInstructionText(node));
        list.remove(node);
    }

}
