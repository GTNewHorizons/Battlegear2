package mods.battlegear2.coremod.transformers;

import java.util.List;
import java.util.ListIterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import mods.battlegear2.api.core.BattlegearTranslator;

public final class NetClientHandlerTransformer extends TransformerBase {

    public NetClientHandlerTransformer() {
        super("net.minecraft.client.network.NetHandlerPlayClient");
    }

    private String entityOtherPlayerMPClassName;
    private String playerInventoryFieldName;

    private String netClientHandlerHandleNamedEntitySpawnMethodName;
    private String netClientHandlerHandleNamedEntitySpawnMethodDesc;

    private String netClientHandlerHandleBlockItemSwitchMethodName;
    private String netClientHandlerHandleBlockItemSwitchMethodDesc;

    @Override
    boolean processMethods(List<MethodNode> methods) {
        int found = 0;
        for (MethodNode method : methods) {
            if (method.name.equals(netClientHandlerHandleNamedEntitySpawnMethodName)
                    && method.desc.equals(netClientHandlerHandleNamedEntitySpawnMethodDesc)) {
                sendPatchLog("handleSpawnPlayer");

                replaceInventoryArrayAccess(method, entityOtherPlayerMPClassName, playerInventoryFieldName, 9, 14);
                found++;
            } else if (method.name.equals(netClientHandlerHandleBlockItemSwitchMethodName)
                    && method.desc.equals(netClientHandlerHandleBlockItemSwitchMethodDesc)) {
                        sendPatchLog("handleHeldItemChange");

                        ListIterator<AbstractInsnNode> insn = method.instructions.iterator();
                        InsnList newList = new InsnList();

                        while (insn.hasNext()) {

                            AbstractInsnNode nextNode = insn.next();

                            if (nextNode instanceof JumpInsnNode && nextNode.getOpcode() == IFLT) {
                                LabelNode label = ((JumpInsnNode) nextNode).label;
                                newList.add(
                                        new MethodInsnNode(
                                                INVOKESTATIC,
                                                "mods/battlegear2/api/core/InventoryPlayerBattle",
                                                "isValidSwitch",
                                                "(I)Z"));
                                newList.add(new JumpInsnNode(IFEQ, label)); // "if equal" branch

                                found++;
                                nextNode = insn.next();
                                while (insn.hasNext() && !(nextNode instanceof JumpInsnNode)
                                        && nextNode.getOpcode() != IF_ICMPGE) {
                                    nextNode = insn.next(); // continue till "if int greater than or equal to" branch
                                }

                            } else {
                                newList.add(nextNode);
                            }
                        }

                        method.instructions = newList;
                    }
        }
        return found == 2;
    }

    @Override
    boolean processFields(List<FieldNode> fields) {
        return true;
    }

    @Override
    void setupMappings() {

        entityOtherPlayerMPClassName = BattlegearTranslator.getMapedClassName("client.entity.EntityOtherPlayerMP");
        playerInventoryFieldName = BattlegearTranslator.getMapedFieldName("field_71071_by", "inventory");

        netClientHandlerHandleNamedEntitySpawnMethodName = BattlegearTranslator
                .getMapedMethodName("func_147237_a", "handleSpawnPlayer");
        netClientHandlerHandleNamedEntitySpawnMethodDesc = "(Lnet/minecraft/network/play/server/S0CPacketSpawnPlayer;)V";

        netClientHandlerHandleBlockItemSwitchMethodName = BattlegearTranslator
                .getMapedMethodName("func_147257_a", "handleHeldItemChange");
        netClientHandlerHandleBlockItemSwitchMethodDesc = "(Lnet/minecraft/network/play/server/S09PacketHeldItemChange;)V";
    }
}
