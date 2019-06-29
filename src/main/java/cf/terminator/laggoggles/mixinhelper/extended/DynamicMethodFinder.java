package cf.terminator.laggoggles.mixinhelper.extended;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.lib.tree.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

import static cf.terminator.laggoggles.mixinhelper.MixinConfigPlugin.LOGGER;

/**
 * Remaps method calls to the target method within this class,
 * Much like @Shadow, but can use regex.
 */
public class DynamicMethodFinder implements Transformer {

    /**
     * Redirects the method specified here to the found method
     */
    @Retention(RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(ElementType.METHOD)
    public @interface FindMethod {
        String nameRegex();
        String signatureRegex() default "";
    }

    private final ClassNode classNode;

    public DynamicMethodFinder(ClassNode classNode){
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
                if(annotation.desc.equals("Lcf/terminator/laggoggles/mixinhelper/extended/DynamicMethodFinder$RedirectMethodCalls;")){
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
                        LOGGER.fatal("Invalid annotation found. (@DynamicMethodFinder.RedirectMethodCalls)");
                        FMLCommonHandler.instance().exitJava(-1, true);
                    }else{
                        findTarget(scheduledActions, method, nameRegex, signatureRegex);
                    }
                }
            }
        }
        for(ScheduledAction action : scheduledActions){
            action.apply();
        }
    }

    private void findTarget(LinkedList<ScheduledAction> scheduledActions, MethodNode instructor, String nameRegex, String signatureRegex){
        MethodHelper.findMethods(nameRegex, signatureRegex, classNode, new MethodHelper.MethodHandler() {
            boolean found = false;

            @Override
            public void onFoundMethod(MethodNode node) {
                if(found == true){
                    LOGGER.fatal("@DynamicMethodFinder.RedirectMethodCalls matched multiple targets. This is not allowed.");
                    FMLCommonHandler.instance().exitJava(-1, true);
                }
                found = true;
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
            LOGGER.info("Linking " + instructor.name + " to: " + target.name + "...");
            for (MethodNode method : classNode.methods) {
                for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
                    AbstractInsnNode node = it.next();
                    if(node instanceof MethodInsnNode){
                        MethodInsnNode methodNode = (MethodInsnNode) node;
                        if(methodNode.name.equals(instructor.name)){
                            if(Objects.equals(methodNode.desc, instructor.desc)) {
                                redirectMethodInsnNode(target, methodNode, method);
                            }
                        }
                    }
                }
            }
        }
    }

    private void redirectMethodInsnNode(MethodNode target, MethodInsnNode node, MethodNode method){
        LOGGER.info("Found target inside method: " + method.name);
        LOGGER.info(Debugging.getInstructionText(node));
        node.name = target.name;
        node.desc = target.desc;
        LOGGER.info("becomes...");
        LOGGER.info(Debugging.getInstructionText(node));
    }
}
