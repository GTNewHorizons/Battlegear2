package mods.battlegear2.asm.transformers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.transformers.MixinClassWriter;

import mods.battlegear2.asm.MethodMapping;
import mods.battlegear2.asm.loader.BattlegearLoadingPlugin;

/**
 * <p>
 * This mod changes the value of the field <tt>EntityPlayer#inventory.currentItem</tt> when the current item is held in
 * the special battlegear inventory slots. Since this field is used in the Vanilla code to access the inventory array,
 * without modifications to the vanilla code, the game would crash with an <tt>ArrayIndexOutOfBoundsException</tt>.
 * </p>
 * <p>
 * Which is why the goal of this transformer is to change all the array access to the EntityPlayer's mainInventory
 * array, by wrapping them with an if statement :
 * </p>
 *
 * <pre>
 * player.inventory.mainInventory[player.inventory.currentItem] = itemStack;
 * </pre>
 * <p>
 * Replaced by :
 * </p>
 *
 * <pre>
 * if (!InventoryAccessHook.setPlayerCurrentItem(player, itemStack)) {
 *     player.inventory.mainIventory[player.inventory.currentItem] = itemStack;
 * }
 * </pre>
 *
 * @author Alexdoru
 */
public class InventoryArrayAccessTransformer implements IClassTransformer, Opcodes {

    private File outputDir = null;
    private final Map<String, MethodMapping> targetClassesAndMethods = new HashMap<>();

    // spotless:off
    public InventoryArrayAccessTransformer() {
        this.targetClassesAndMethods.put("net.minecraft.entity.ai.EntityAIControlledByPlayer", MethodMapping.ENTITYAICONTROLLEDBYPLAYER_UPDATETASK);
        this.targetClassesAndMethods.put("net.minecraft.client.entity.EntityOtherPlayerMP", MethodMapping.ENTITYOTHERPLAYERMP_SETCURRENTITEMORARMOR);
        this.targetClassesAndMethods.put("net.minecraft.entity.player.EntityPlayer", MethodMapping.ENTITYPLAYER_SETCURRENTITEMORARMOR);
        this.targetClassesAndMethods.put("net.minecraft.server.management.ItemInWorldManager", MethodMapping.ITEMINWORLDMANAGER_TRYUSEITEM);
        this.targetClassesAndMethods.put("net.minecraft.client.Minecraft", MethodMapping.MINECRAFT_RIGHTCLICKMOUSE);
        this.targetClassesAndMethods.put("net.minecraft.client.network.NetHandlerPlayClient", MethodMapping.NETHANDLERPLAYCLIENT_HANDLESPAWNPLAYER);
        this.targetClassesAndMethods.put("net.minecraft.network.NetHandlerPlayServer", MethodMapping.NETHANDLERPLAYSERVER_PROCESSPLAYERBLOCKPLACEMENT);
        this.targetClassesAndMethods.put("net.minecraft.client.multiplayer.PlayerControllerMP", MethodMapping.PLAYERCONTROLLERMP_SENDUSEITEM);
    }
    // spotless:on

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (targetClassesAndMethods.containsKey(transformedName)) {
            saveTransformedClass(basicClass, transformedName + "_pre");
            final ClassNode classNode = new ClassNode();
            final ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);
            transform(classNode, targetClassesAndMethods.get(transformedName));
            final ClassWriter classWriter = new MixinClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            basicClass = classWriter.toByteArray();
            saveTransformedClass(basicClass, transformedName + "_post");
            return basicClass;
        }
        return basicClass;
    }

    private static void transform(ClassNode classNode, MethodMapping targetMethod) {
        int injectionCount = -1;
        for (final MethodNode methodNode : classNode.methods) {
            if ((methodNode.name + methodNode.desc).equals(targetMethod.getMapping())) {
                if (targetMethod == MethodMapping.NETHANDLERPLAYSERVER_PROCESSPLAYERBLOCKPLACEMENT) {
                    cleanUnsafeArrayAccess(methodNode.instructions);
                }
                injectionCount = wrapInventoryAccess(methodNode.instructions);
            }
        }
        final int expectedCount = targetMethod.targetCount;
        if (injectionCount == -1) {
            BattlegearLoadingPlugin.logger.error("Couldn't find target method in " + classNode.name);
        } else if (injectionCount == expectedCount) {
            final String msg = "Transformed " + classNode.name
                    + "#"
                    + targetMethod.clearMapping
                    + " to wrap "
                    + injectionCount
                    + " inventory access array";
            if (BattlegearLoadingPlugin.isObf()) {
                BattlegearLoadingPlugin.logger.debug(msg);
            } else {
                BattlegearLoadingPlugin.logger.info(msg);
            }
        } else if (injectionCount < expectedCount) {
            BattlegearLoadingPlugin.logger.error(
                    "Expected " + expectedCount
                            + " injections, but could only inject "
                            + injectionCount
                            + " time when transforming "
                            + classNode.name
                            + "#"
                            + targetMethod.clearMapping);
        } else {
            BattlegearLoadingPlugin.logger.error(
                    "Expected " + expectedCount
                            + " injections, but injected "
                            + injectionCount
                            + " times when transforming "
                            + classNode.name
                            + "#"
                            + targetMethod.clearMapping);
        }
    }

    /**
     * <p>
     * Wraps all the array access to the EntityPlayer's mainInventory with an if statement :
     * </p>
     *
     * <pre>
     * player.inventory.mainInventory[player.inventory.currentItem] = itemStack;
     * </pre>
     * <p>
     * Replaced by :
     * </p>
     *
     * <pre>
     * if (!InventoryAccessHook.setPlayerCurrentItem(player, itemStack)) {
     *     player.inventory.mainIventory[player.inventory.currentItem] = itemStack;
     * }
     * </pre>
     */
    private static int wrapInventoryAccess(InsnList instructions) {
        int injectionCount = 0;
        for (final AbstractInsnNode insnNode : instructions.toArray()) {
            if (isLoadPlayerNode(insnNode)) {
                final boolean isPlayerField = insnNode instanceof FieldInsnNode;
                final AbstractInsnNode prevNode = insnNode.getPrevious();
                if (isPlayerField && !(prevNode instanceof VarInsnNode && prevNode.getOpcode() == ALOAD
                        && ((VarInsnNode) prevNode).var == 0)) {
                    continue;
                }
                final AbstractInsnNode secondNode = insnNode.getNext();
                if (isPlayerInventoryFieldNode(secondNode)) {
                    final AbstractInsnNode thirdNode = secondNode.getNext();
                    if (isMainInventoryFieldNode(thirdNode)) {
                        final AbstractInsnNode fourthNode;
                        if (isPlayerField) {
                            if (!(thirdNode.getNext() instanceof VarInsnNode && thirdNode.getNext().getOpcode() == ALOAD
                                    && ((VarInsnNode) thirdNode.getNext()).var == 0)) {
                                continue;
                            }
                            fourthNode = thirdNode.getNext().getNext();
                        } else {
                            fourthNode = thirdNode.getNext();
                        }
                        if (isLoadPlayerNode(fourthNode)) {
                            final AbstractInsnNode fifthNode = fourthNode.getNext();
                            if (isPlayerInventoryFieldNode(fifthNode)) {
                                final AbstractInsnNode sixthNode = fifthNode.getNext();
                                if (isCurrentItemFieldNode(sixthNode)) {
                                    final InsnList loadItemStackInsnList = new InsnList();
                                    AbstractInsnNode node = sixthNode.getNext();
                                    while (node != null && node.getOpcode() != AASTORE) {
                                        loadItemStackInsnList.add(node.clone(null));
                                        node = node.getNext();
                                    }
                                    if (node instanceof InsnNode && node.getOpcode() == AASTORE) {
                                        final LabelNode label = new LabelNode();
                                        final InsnList list = new InsnList();
                                        AbstractInsnNode firstNode = insnNode;
                                        if (insnNode instanceof FieldInsnNode) {
                                            // we add "this"
                                            list.add(insnNode.getPrevious().clone(null));
                                            firstNode = insnNode.getPrevious();
                                        }
                                        list.add(insnNode.clone(null));
                                        list.add(loadItemStackInsnList);
                                        list.add(
                                                new MethodInsnNode(
                                                        INVOKESTATIC,
                                                        "mods/battlegear2/asm/hooks/InventoryAccessHook",
                                                        "setPlayerCurrentItem",
                                                        deobf(
                                                                "(Lyz;Ladd;)Z",
                                                                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z"),
                                                        false));
                                        list.add(new JumpInsnNode(IFNE, label));
                                        instructions.insertBefore(firstNode, list);
                                        instructions.insert(node, label);
                                        injectionCount++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return injectionCount;
    }

    /**
     * <p>
     * Clean the instruction list when transforming net.minecraft.network.NetHandlerPlayServer. Changes :
     * </p>
     *
     * <pre>
     * this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] AALOAD
     * </pre>
     * <p>
     * With :
     * </p>
     *
     * <pre>
     * this.playerEntity.inventory.getCurrentItem()
     * </pre>
     */
    private static void cleanUnsafeArrayAccess(InsnList list) {
        for (final AbstractInsnNode insnNode : list.toArray()) {
            if (isMainInventoryFieldNode(insnNode)) {
                final AbstractInsnNode secondNode = insnNode.getNext();
                if (secondNode instanceof VarInsnNode && secondNode.getOpcode() == ALOAD
                        && ((VarInsnNode) secondNode).var == 0) {
                    final AbstractInsnNode thirdNode = secondNode.getNext();
                    if (isLoadPlayerNode(thirdNode)) {
                        final AbstractInsnNode fourthNode = thirdNode.getNext();
                        if (isPlayerInventoryFieldNode(fourthNode)) {
                            final AbstractInsnNode fifthNode = fourthNode.getNext();
                            if (isCurrentItemFieldNode(fifthNode)) {
                                final AbstractInsnNode sixthNode = fifthNode.getNext();
                                if (sixthNode instanceof InsnNode && sixthNode.getOpcode() == AALOAD) {
                                    list.insertBefore(
                                            insnNode,
                                            new MethodInsnNode(
                                                    INVOKEVIRTUAL,
                                                    deobf("yx", "net/minecraft/entity/player/InventoryPlayer"),
                                                    deobf("h", "getCurrentItem"),
                                                    deobf("()Ladd;", "()Lnet/minecraft/item/ItemStack;"),
                                                    false));
                                    list.remove(insnNode);
                                    list.remove(secondNode);
                                    list.remove(thirdNode);
                                    list.remove(fourthNode);
                                    list.remove(fifthNode);
                                    list.remove(sixthNode);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isLoadPlayerNode(AbstractInsnNode node) {
        return (node instanceof VarInsnNode && node.getOpcode() == ALOAD)
                || (node instanceof FieldInsnNode && node.getOpcode() == GETFIELD
                        && ((FieldInsnNode) node).owner.equals(deobf("bao", "net/minecraft/client/Minecraft"))
                        && ((FieldInsnNode) node).name.equals(deobf("h", "thePlayer"))
                        && ((FieldInsnNode) node).desc
                                .equals(deobf("Lbjk;", "Lnet/minecraft/client/entity/EntityClientPlayerMP;"))
                        || (node instanceof FieldInsnNode && node.getOpcode() == GETFIELD
                                && ((FieldInsnNode) node).owner
                                        .equals(deobf("nh", "net/minecraft/network/NetHandlerPlayServer"))
                                && ((FieldInsnNode) node).name.equals(deobf("b", "playerEntity"))
                                && ((FieldInsnNode) node).desc
                                        .equals(deobf("Lmw;", "Lnet/minecraft/entity/player/EntityPlayerMP;"))));
    }

    private static boolean isPlayerInventoryFieldNode(AbstractInsnNode node) {
        return node instanceof FieldInsnNode && node.getOpcode() == GETFIELD
                && (((FieldInsnNode) node).owner.equals(deobf("yz", "net/minecraft/entity/player/EntityPlayer"))
                        || ((FieldInsnNode) node).owner
                                .equals(deobf("bll", "net/minecraft/client/entity/EntityOtherPlayerMP"))
                        || ((FieldInsnNode) node).owner
                                .equals(deobf("bjk", "net/minecraft/client/entity/EntityClientPlayerMP"))
                        || ((FieldInsnNode) node).owner
                                .equals(deobf("mw", "net/minecraft/entity/player/EntityPlayerMP")))
                && ((FieldInsnNode) node).name.equals(deobf("bm", "inventory"))
                && ((FieldInsnNode) node).desc.equals(deobf("Lyx;", "Lnet/minecraft/entity/player/InventoryPlayer;"));
    }

    private static boolean isMainInventoryFieldNode(AbstractInsnNode node) {
        return node instanceof FieldInsnNode && node.getOpcode() == GETFIELD
                && ((FieldInsnNode) node).owner.equals(deobf("yx", "net/minecraft/entity/player/InventoryPlayer"))
                && ((FieldInsnNode) node).name.equals(deobf("a", "mainInventory"))
                && ((FieldInsnNode) node).desc.equals(deobf("[Ladd;", "[Lnet/minecraft/item/ItemStack;"));
    }

    private static boolean isCurrentItemFieldNode(AbstractInsnNode node) {
        return node instanceof FieldInsnNode && node.getOpcode() == GETFIELD
                && ((FieldInsnNode) node).owner.equals(deobf("yx", "net/minecraft/entity/player/InventoryPlayer"))
                && ((FieldInsnNode) node).name.equals(deobf("c", "currentItem"))
                && ((FieldInsnNode) node).desc.equals("I");
    }

    private void saveTransformedClass(final byte[] data, final String transformedName) {
        if (BattlegearLoadingPlugin.isObf()) {
            return;
        }
        if (outputDir == null) {
            emptyClassOutputFolder();
        }
        final File outFile = new File(outputDir, transformedName.replace('.', File.separatorChar) + ".class");
        final File outDir = outFile.getParentFile();
        if (!outDir.exists()) {
            // noinspection ResultOfMethodCallIgnored
            outDir.mkdirs();
        }
        if (outFile.exists()) {
            // noinspection ResultOfMethodCallIgnored
            outFile.delete();
        }
        try {
            final OutputStream output = Files.newOutputStream(outFile.toPath());
            output.write(data);
            output.close();
        } catch (IOException ex) {
            BattlegearLoadingPlugin.logger.error("Could not save transformed class " + transformedName);
        }
    }

    private void emptyClassOutputFolder() {
        outputDir = new File(Launch.minecraftHome, "ASM_BG2");
        try {
            FileUtils.deleteDirectory(outputDir);
        } catch (IOException ignored) {}
        if (!outputDir.exists()) {
            // noinspection ResultOfMethodCallIgnored
            outputDir.mkdirs();
        }
    }

    private static String deobf(String obf, String clean) {
        return BattlegearLoadingPlugin.isObf() ? obf : clean;
    }

}
