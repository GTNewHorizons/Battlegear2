package mods.battlegear2.packet;

import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;

import mods.battlegear2.api.core.IBattlegearInventoryPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;

import static mods.battlegear2.api.core.Constants.EXTRA_INV_SIZE;
import static mods.battlegear2.api.core.Constants.OFFSET;

/**
 * User: nerd-boy Date: 26/06/13 Time: 1:40 PM
 */
public final class BattlegearSyncItemPacket extends AbstractMBPacket {

    public static final String packetName = "MB2|SyncItem";
    private String user;
    private InventoryPlayer inventory;
    private EntityPlayer player;

    public BattlegearSyncItemPacket(EntityPlayer player) {
        this(player.getCommandSenderName(), player.inventory, player);
    }

    private BattlegearSyncItemPacket(String user, InventoryPlayer inventory, EntityPlayer player) {
        this.user = user;
        this.inventory = inventory;
        this.player = player;
    }

    public BattlegearSyncItemPacket() {}

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        this.user = ByteBufUtils.readUTF8String(inputStream);
        this.player = player.worldObj.getPlayerEntityByName(user);
        if (this.player != null) {
            int current = inputStream.readInt();
            if (BattlegearUtils.isValidSwitch(current)) this.player.inventory.currentItem = current;
            if (player.worldObj.isRemote) {
                ItemStack temp = ByteBufUtils.readItemStack(inputStream);
                if (!ItemStack.areItemStacksEqual(this.player.getCurrentEquippedItem(), temp))
                    BattlegearUtils.setPlayerCurrentItem(this.player, temp);

                for (int i = 0; i < EXTRA_INV_SIZE; i++) {
                    ItemStack stack = ByteBufUtils.readItemStack(inputStream);
                    if (!ItemStack.areItemStacksEqual(
                            this.player.inventory.getStackInSlot(OFFSET + i),
                            stack)) {
                        this.player.inventory
                                .setInventorySlotContents(OFFSET + i, stack);
                        ((IBattlegearInventoryPlayer) this.player.inventory).setHasChanged(false);
                    }
                }
            } else if (BattlegearUtils.isPlayerInBattlemode(this.player)) { // Using data sent only by client
                ItemStack inUse = ByteBufUtils.readItemStack(inputStream);
                int time = inputStream.readInt();
                if (inUse != null && time > 0) {
                    this.player.setItemInUse(inUse, time);
                }
            }
            ((IBattlePlayer) this.player).setSpecialActionTimer(0);
        }
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, user);
        out.writeInt(inventory.currentItem);
        if (!player.worldObj.isRemote) {
            ByteBufUtils.writeItemStack(out, inventory.getCurrentItem());

            for (int i = 0; i < EXTRA_INV_SIZE; i++) {
                ByteBufUtils.writeItemStack(out, inventory.getStackInSlot(i + OFFSET));
            }
        } else if (BattlegearUtils.isPlayerInBattlemode(player)) { // client-side only thing
            ByteBufUtils.writeItemStack(out, player.getItemInUse());
            out.writeInt(player.getItemInUseCount());
        }
    }
}
