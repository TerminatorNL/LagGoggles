package cf.terminator.laggoggles.mixinhelper.extended;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Pattern;

import static cf.terminator.laggoggles.mixinhelper.MixinConfigPlugin.LOGGER;

/**
 * Remaps method calls to the target method within this class,
 * Much like @Redirect, but can use regex.
 */
public class DynamicMethodReplacer implements Transformer {

    /**
     * Redirects the method specified here to the found method
     */
    @Retention(RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(ElementType.METHOD)
    public @interface RedirectMethodCalls {
        String nameRegex();
        boolean convertSelf() default false;
    }

    private final ClassNode classNode;

    public DynamicMethodReplacer(ClassNode classNode){
        this.classNode = classNode;
    }

    @Override
    public void transform() {
        LinkedList<MethodNode> changedMethods = new LinkedList<>();
        for(MethodNode method : classNode.methods){
            if(method.visibleAnnotations == null){
                continue;
            }
            for(AnnotationNode annotation : method.visibleAnnotations){
                if(annotation.desc.equals("Lcf/terminator/laggoggles/mixinhelper/extended/DynamicMethodReplacer$RedirectMethodCalls;")){
                    String currentKey = null;
                    String nameRegex = null;
                    boolean convertSelf = false;
                    for(Object key_value : annotation.values){
                        if(currentKey == null){
                            currentKey = (String) key_value;
                        }else{
                            if(currentKey.equals("nameRegex")){
                                nameRegex = (String) key_value;
                            }else if(currentKey.equals("convertSelf")){
                                convertSelf = (Boolean) key_value;
                            }
                            currentKey = null;
                        }
                    }
                    if(nameRegex == null){
                        LOGGER.fatal("Invalid annotation found. (@DynamicMethodFinder.RedirectMethodCalls)");
                        FMLCommonHandler.instance().exitJava(-1, true);
                    }else{
                        for (MethodNode changedMethod : findTargets(classNode, method, nameRegex,convertSelf, 1)){
                            if(changedMethods.contains(changedMethod) == false){
                                changedMethods.add(changedMethod);
                            }
                        }
                    }
                }
            }
        }
        for(MethodNode method : changedMethods){
            LOGGER.debug(Debugging.getMethodName(method));
            LOGGER.debug(Debugging.getInstructions(method));
        }
    }

    private LinkedList<MethodNode> findTargets(ClassNode classNode, MethodNode instructor, String nameRegex, boolean convertSelf, int expected){
        String signatureToMatch;
        if(convertSelf){
            signatureToMatch = "(" + Pattern.compile("\\([^;]+;").matcher(instructor.desc).replaceFirst("");
        }else{
            signatureToMatch = instructor.desc;
        }
        LinkedList<MethodNode> changedMethods = MethodHelper.findMethodCalls(nameRegex, signatureToMatch, classNode, new MethodHelper.InsnMethodHandler() {
            @Override
            public void onFoundMethodNode(MethodNode method, MethodInsnNode node) {
                if(Objects.equals(instructor, method)){
                    return;
                }
                String beforeChange = Debugging.getInstructionText(node);
                node.name = instructor.name;
                if(convertSelf) {
                    node.desc = node.desc.replaceFirst("\\(", "(L" + node.owner + ";");
                }
                node.owner = classNode.name;
                if(convertSelf){
                    AbstractInsnNode currentNode = node;
                    while(true){
                        currentNode =  currentNode.getPrevious();
                        if(currentNode instanceof VarInsnNode == false && currentNode instanceof FieldInsnNode == false){
                            method.instructions.insert(currentNode, new VarInsnNode(Opcodes.ALOAD, 0));
                            break;
                        }
                    }
                }
                LOGGER.info("Redirected call in method " + Debugging.getMethodName(method) + "\n from " + beforeChange + "\n   to " + Debugging.getInstructionText(node));
            }
        });
        if(changedMethods.size() < expected){
            LOGGER.fatal(Debugging.printClassNodeMethods(classNode));
            throw new IllegalStateException(changedMethods.size() + " methods changed, expected at least " + expected);
        }
        return changedMethods;
    }

}
