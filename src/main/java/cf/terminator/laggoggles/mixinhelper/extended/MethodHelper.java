package cf.terminator.laggoggles.mixinhelper.extended;

import cf.terminator.laggoggles.mixinhelper.MixinConfigPlugin;
import com.sun.istack.internal.NotNull;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;
import java.util.regex.Pattern;

import static cf.terminator.laggoggles.mixinhelper.MixinConfigPlugin.LOGGER;

public class MethodHelper {

    public static void findMethods(@Nullable String nameRegex, @Nullable String signatureRegex, ClassNode classNode, MethodHandler handler){
        if(nameRegex != null && nameRegex.equals("")){
            nameRegex = null;
        }
        if(signatureRegex != null && signatureRegex.equals("")){
            signatureRegex = null;
        }

        Pattern namePattern = nameRegex == null ? null : Pattern.compile(nameRegex);
        Pattern signaturePattern = signatureRegex == null ? null : Pattern.compile(signatureRegex);

        if(namePattern != null){
            for (MethodNode node : classNode.methods) {
                if (namePattern.matcher(node.name).find()) {
                    if (signaturePattern == null){
                        handler.onFoundMethod(node);
                    }else{
                        if(node.signature != null && signaturePattern.matcher(node.signature).find()){
                            handler.onFoundMethod(node);
                        }
                    }
                }
            }
        }else if(signaturePattern != null){
            for (MethodNode node : classNode.methods) {
                if (signaturePattern.matcher(node.signature).find()){
                    handler.onFoundMethod(node);
                }
            }
        }else{
            throw new IllegalArgumentException("Namepattern and SignaturePattern cannot both be null!");
        }
    }

    public static LinkedList<MethodNode> findMethodCalls(@Nullable String nameRegex, @Nullable String signature, ClassNode classNode, InsnMethodHandler handler){
        if(nameRegex != null && nameRegex.equals("")){
            nameRegex = null;
        }
        Pattern namePattern = nameRegex == null ? null : Pattern.compile(nameRegex);
        LinkedList<MethodNode> changedMethods = new LinkedList<>();
        if(namePattern != null){
            for (MethodNode method : classNode.methods) {
                for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
                    AbstractInsnNode node_raw = it.next();
                    if(node_raw instanceof MethodInsnNode){
                        MethodInsnNode node = (MethodInsnNode) node_raw;
                        if (namePattern.matcher(node.name).find()) {
                            if(Objects.equals(node.desc, signature)){
                                handler.onFoundMethodNode(method, node);
                                if(changedMethods.contains(method) == false){
                                    changedMethods.add(method);
                                }
                            }
                        }
                    }
                }
            }
            return changedMethods;
        }else{
            throw new IllegalArgumentException("Namepattern and SignaturePattern cannot both be null!");
        }
    }

    public interface MethodHandler {
        void onFoundMethod(MethodNode node);
    }
    public interface InsnMethodHandler {
        void onFoundMethodNode(MethodNode method, MethodInsnNode node);
    }
}
