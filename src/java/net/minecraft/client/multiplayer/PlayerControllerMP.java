package net.minecraft.client.multiplayer;

import dev.tenacity.event.impl.player.SyncCurrentItemEvent;
import dev.tenacity.Client;
import dev.tenacity.module.impl.combat.KillAura;
import dev.tenacity.module.impl.movement.Flight;
import dev.tenacity.event.impl.player.AttackEvent;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class PlayerControllerMP {
    /**
     * The Minecraft instance.
     */
    @Setter
    private Minecraft mc;
    private final NetHandlerPlayClient netClientHandler;
    private BlockPos currentBlock = new BlockPos(-1, -1, -1);

    /**
     * The Item currently being used to destroy a block
     */
    private ItemStack currentItemHittingBlock;

    /**
     * Current block damage (MP)
     */
    private float curBlockDamageMP;

    /**
     * Tick counter, when it hits 4 it resets back to 0 and plays the step sound
     */
    private float stepSoundTickCounter;

    /**
     * Delays the first damage on the block after the first click on the block
     */
    public int blockHitDelay;

    /**
     * Tells if the player is hitting a block
     */
    private boolean isHittingBlock;

    /**
     * Current game type for the player
     */
    private WorldSettings.GameType currentGameType = WorldSettings.GameType.SURVIVAL;

    /**
     * Index of the current item held by the player in the inventory hotbar
     */
    public int currentPlayerItem;

    public PlayerControllerMP(Minecraft mcIn, NetHandlerPlayClient netHandler) {
        this.mc = mcIn;
        this.netClientHandler = netHandler;
    }

    public static void clickBlockCreative(Minecraft mcIn, PlayerControllerMP playerController, BlockPos pos, EnumFacing facing) {
        if (!mcIn.theWorld.extinguishFire(mcIn.thePlayer, pos, facing)) {
            playerController.onPlayerDestroyBlock(pos, facing);
        }
    }

    /**
     * Sets player capabilities depending on current gametype. params: player
     *
     * @param player The player's instance
     */
    public void setPlayerCapabilities(EntityPlayer player) {
        this.currentGameType.configurePlayerCapabilities(player.capabilities);
    }

    /**
     * None
     */
    public boolean isSpectator() {
        return this.currentGameType == WorldSettings.GameType.SPECTATOR;
    }

    /**
     * Sets the game type for the player.
     *
     * @param type The GameType to set
     */
    public void setGameType(WorldSettings.GameType type) {
        this.currentGameType = type;
        this.currentGameType.configurePlayerCapabilities(this.mc.thePlayer.capabilities);
    }

    /**
     * Flips the player around.
     */
    public void flipPlayer(EntityPlayer playerIn) {
        playerIn.rotationYaw = -180.0F;
    }

    public boolean shouldDrawHUD() {
        return this.currentGameType.isSurvivalOrAdventure();
    }

    /**
     * Called when a player completes the destruction of a block
     */
    public boolean onPlayerDestroyBlock(BlockPos pos, EnumFacing side) {
        if (this.currentGameType.isAdventure()) {
            if (this.currentGameType == WorldSettings.GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.thePlayer.isAllowEdit()) {
                Block block = this.mc.theWorld.getBlockState(pos).getBlock();
                ItemStack itemstack = this.mc.thePlayer.getCurrentEquippedItem();

                if (itemstack == null) {
                    return false;
                }

                if (!itemstack.canDestroy(block)) {
                    return false;
                }
            }
        }

        if (this.currentGameType.isCreative() && this.mc.thePlayer.getHeldItem() != null && this.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            return false;
        } else {
            World world = this.mc.theWorld;
            IBlockState iblockstate = world.getBlockState(pos);
            Block block1 = iblockstate.getBlock();

            if (block1.getMaterial() == Material.air) {
                return false;
            } else {
                world.playAuxSFX(2001, pos, Block.getStateId(iblockstate));
                boolean flag = world.setBlockToAir(pos);

                if (flag) {
                    block1.onBlockDestroyedByPlayer(world, pos, iblockstate);
                }

                this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());

                if (!this.currentGameType.isCreative()) {
                    ItemStack itemstack1 = this.mc.thePlayer.getCurrentEquippedItem();

                    if (itemstack1 != null) {
                        itemstack1.onBlockDestroyed(world, block1, pos, this.mc.thePlayer);

                        if (itemstack1.stackSize == 0) {
                            this.mc.thePlayer.destroyCurrentEquippedItem();
                        }
                    }
                }

                return flag;
            }
        }
    }

    /**
     * Called when the player is hitting a block with an item.
     */
    public boolean clickBlock(BlockPos loc, EnumFacing face) {
        if (this.currentGameType.isAdventure()) {
            if (this.currentGameType == WorldSettings.GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.thePlayer.isAllowEdit()) {
                Block block = this.mc.theWorld.getBlockState(loc).getBlock();
                ItemStack itemstack = this.mc.thePlayer.getCurrentEquippedItem();

                if (itemstack == null) {
                    return false;
                }

                if (!itemstack.canDestroy(block)) {
                    return false;
                }
            }
        }

        if (!this.mc.theWorld.getWorldBorder().contains(loc)) {
            return false;
        } else {
            if (this.currentGameType.isCreative()) {
                this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                clickBlockCreative(this.mc, this, loc, face);
                this.blockHitDelay = 5;
            } else if (!this.isHittingBlock || !this.isHittingPosition(loc)) {
                if (this.isHittingBlock) {
                    this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, face));
                }

                this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                Block block1 = this.mc.theWorld.getBlockState(loc).getBlock();
                boolean flag = block1.getMaterial() != Material.air;

                if (flag && this.curBlockDamageMP == 0.0F) {
                    block1.onBlockClicked(this.mc.theWorld, loc, this.mc.thePlayer);
                }

                if (flag && block1.getPlayerRelativeBlockHardness(mc.thePlayer) >= 1.0F) {
                    this.onPlayerDestroyBlock(loc, face);
                } else {
                    this.isHittingBlock = true;
                    this.currentBlock = loc;
                    this.currentItemHittingBlock = this.mc.thePlayer.getHeldItem();
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    /**
     * Resets current block damage and isHittingBlock
     */
    public void resetBlockRemoving() {
        if (this.isHittingBlock) {
            this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, EnumFacing.DOWN));
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0F;
            this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), this.currentBlock, -1);
        }
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
        this.syncCurrentPlayItem();

        if (this.blockHitDelay > 0) {
            --this.blockHitDelay;
            return true;
        } else if (this.currentGameType.isCreative() && this.mc.theWorld.getWorldBorder().contains(posBlock)) {
            this.blockHitDelay = 5;
            this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
            clickBlockCreative(this.mc, this, posBlock, directionFacing);
            return true;
        } else if (this.isHittingPosition(posBlock)) {
            Block block = this.mc.theWorld.getBlockState(posBlock).getBlock();

            if (block.getMaterial() == Material.air) {
                this.isHittingBlock = false;
                return false;
            } else {
                this.curBlockDamageMP += block.getPlayerRelativeBlockHardness(mc.thePlayer);

                if (this.stepSoundTickCounter % 4.0F == 0.0F) {
                    if (!Flight.hiddenBlocks.contains(posBlock)) {
                        this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getStepSound()), (block.stepSound.getVolume() + 1.0F) / 8.0F, block.stepSound.getFrequency() * 0.5F, (float) posBlock.getX() + 0.5F, (float) posBlock.getY() + 0.5F, (float) posBlock.getZ() + 0.5F));
                    }
                }

                ++this.stepSoundTickCounter;

                if (this.curBlockDamageMP >= 1.0F) {
                    this.isHittingBlock = false;
                    this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
                    this.onPlayerDestroyBlock(posBlock, directionFacing);
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.blockHitDelay = 5;
                }

                this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
                return true;
            }
        } else {
            return this.clickBlock(posBlock, directionFacing);
        }
    }

    /**
     * player reach distance = 4F
     */
    public float getBlockReachDistance() {
        return this.currentGameType.isCreative() ? 5.0F : 4.5F;
    }

    public void updateController() {
        this.syncCurrentPlayItem();

        if (this.netClientHandler.getNetworkManager().isChannelOpen()) {
            this.netClientHandler.getNetworkManager().processReceivedPackets();
        } else {
            this.netClientHandler.getNetworkManager().checkDisconnected();
        }
    }

    private boolean isHittingPosition(BlockPos pos) {
        ItemStack itemstack = this.mc.thePlayer.getHeldItem();
        boolean flag = this.currentItemHittingBlock == null && itemstack == null;

        if (this.currentItemHittingBlock != null && itemstack != null) {
            flag = itemstack.getItem() == this.currentItemHittingBlock.getItem() && ItemStack.areItemStackTagsEqual(itemstack, this.currentItemHittingBlock) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == this.currentItemHittingBlock.getMetadata());
        }

        return pos.equals(this.currentBlock) && flag;
    }

    /**
     * Syncs the current player item with the server
     */
    public void syncCurrentPlayItem() {
        InventoryPlayer inventoryPlayer = this.mc.thePlayer.inventory;
        int i = inventoryPlayer.currentItem;

        final SyncCurrentItemEvent event = new SyncCurrentItemEvent(i);
        Client.INSTANCE.getEventManager().call(event);

        i = event.getSlot();

        if (i != this.currentPlayerItem) {
            this.currentPlayerItem = i;
            this.netClientHandler.addToSendQueue(new C09PacketHeldItemChange(this.currentPlayerItem));
        }
    }

    public boolean onPlayerRightClick(EntityPlayerSP player, WorldClient worldIn, ItemStack heldStack, BlockPos hitPos, EnumFacing side, Vec3 hitVec) {
        this.syncCurrentPlayItem();
        float f = (float) (hitVec.xCoord - (double) hitPos.getX());
        float f1 = (float) (hitVec.yCoord - (double) hitPos.getY());
        float f2 = (float) (hitVec.zCoord - (double) hitPos.getZ());
        boolean flag = false;

        if (!this.mc.theWorld.getWorldBorder().contains(hitPos)) {
            return false;
        } else {
            if (this.currentGameType != WorldSettings.GameType.SPECTATOR) {
                IBlockState iblockstate = worldIn.getBlockState(hitPos);

                if ((!player.isSneaking() || player.getHeldItem() == null) && iblockstate.getBlock().onBlockActivated(worldIn, hitPos, iblockstate, player, side, f, f1, f2)) {
                    flag = true;
                }

                if (!flag && heldStack != null && heldStack.getItem() instanceof ItemBlock) {
                    ItemBlock itemblock = (ItemBlock) heldStack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(worldIn, hitPos, side, player, heldStack)) {
                        return false;
                    }
                }
            }

            this.netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(hitPos, side.getIndex(), player.inventory.getCurrentItem(), f, f1, f2));

            if (!flag && this.currentGameType != WorldSettings.GameType.SPECTATOR) {
                if (heldStack == null) {
                    return false;
                } else if (this.currentGameType.isCreative()) {
                    int i = heldStack.getMetadata();
                    int j = heldStack.stackSize;
                    boolean flag1 = heldStack.onItemUse(player, worldIn, hitPos, side, f, f1, f2);
                    heldStack.setItemDamage(i);
                    heldStack.stackSize = j;
                    return flag1;
                } else {
                    return heldStack.onItemUse(player, worldIn, hitPos, side, f, f1, f2);
                }
            } else {
                return true;
            }
        }
    }

    /**
     * Notifies the server of things like consuming food, etc...
     */
    public boolean sendUseItem(EntityPlayer playerIn, World worldIn, ItemStack itemStackIn) {
        if (this.currentGameType == WorldSettings.GameType.SPECTATOR) {
            return false;
        } else {
            this.syncCurrentPlayItem();
            this.netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(playerIn.inventory.getCurrentItem()));
            int i = itemStackIn.stackSize;
            ItemStack itemstack = itemStackIn.useItemRightClick(worldIn, playerIn);

            if (itemstack != itemStackIn || itemstack.stackSize != i) {
                playerIn.inventory.mainInventory[playerIn.inventory.currentItem] = itemstack;

                if (itemstack.stackSize == 0) {
                    playerIn.inventory.mainInventory[playerIn.inventory.currentItem] = null;
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public EntityPlayerSP func_178892_a(World worldIn, StatFileWriter statWriter) {
        return new EntityPlayerSP(this.mc, worldIn, this.netClientHandler, statWriter);
    }

    /**
     * Attacks an entity
     */
    public void attackEntity(EntityPlayer playerIn, Entity targetEntity) {
        boolean cancelled = false;
        if (targetEntity instanceof EntityLivingBase) {
            AttackEvent attackEvent = new AttackEvent((EntityLivingBase) targetEntity);
            Client.INSTANCE.getEventManager().call(attackEvent);
            cancelled = attackEvent.isCancelled();
        }
        if (!cancelled) {
            this.syncCurrentPlayItem();
            this.netClientHandler.addToSendQueue(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.ATTACK));

            if (this.currentGameType != WorldSettings.GameType.SPECTATOR) {
                playerIn.attackTargetEntityWithCurrentItem(targetEntity);
            }
        }
    }

    /**
     * Send packet to server - player is interacting with another entity (left click)
     */
    public boolean interactWithEntitySendPacket(EntityPlayer playerIn, Entity targetEntity) {
        this.syncCurrentPlayItem();
        this.netClientHandler.addToSendQueue(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.INTERACT));
        return this.currentGameType != WorldSettings.GameType.SPECTATOR && playerIn.interactWith(targetEntity);
    }

    /**
     * Return true when the player rightclick on an entity
     *
     * @param player       The player's instance
     * @param entityIn     The entity clicked
     * @param movingObject The object clicked
     */
    public boolean isPlayerRightClickingOnEntity(EntityPlayer player, Entity entityIn, MovingObjectPosition movingObject) {
        this.syncCurrentPlayItem();
        Vec3 vec3 = new Vec3(movingObject.hitVec.xCoord - entityIn.posX, movingObject.hitVec.yCoord - entityIn.posY, movingObject.hitVec.zCoord - entityIn.posZ);
        this.netClientHandler.addToSendQueue(new C02PacketUseEntity(entityIn, vec3));
        return this.currentGameType != WorldSettings.GameType.SPECTATOR && entityIn.interactAt(player, vec3);
    }

    /**
     * Handles slot clicks sends a packet to the server.
     */
    public ItemStack windowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn) {
        short short1 = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
        ItemStack itemstack = playerIn.openContainer.slotClick(slotId, mouseButtonClicked, mode, playerIn);
        this.netClientHandler.addToSendQueue(new C0EPacketClickWindow(windowId, slotId, mouseButtonClicked, mode, itemstack, short1));
        return itemstack;
    }

    /**
     * GuiEnchantment uses this during multiplayer to tell PlayerControllerMP to send a packet indicating the
     * enchantment action the player has taken.
     *
     * @param windowID The ID of the current window
     * @param button   The button id (enchantment selected)
     */
    public void sendEnchantPacket(int windowID, int button) {
        this.netClientHandler.addToSendQueue(new C11PacketEnchantItem(windowID, button));
    }

    /**
     * Used in PlayerControllerMP to update the server with an ItemStack in a slot.
     */
    public void sendSlotPacket(ItemStack itemStackIn, int slotId) {
        if (this.currentGameType.isCreative()) {
            this.netClientHandler.addToSendQueue(new C10PacketCreativeInventoryAction(slotId, itemStackIn));
        }
    }

    /**
     * Sends a Packet107 to the server to drop the item on the ground
     */
    public void sendPacketDropItem(ItemStack itemStackIn) {
        if (this.currentGameType.isCreative() && itemStackIn != null) {
            this.netClientHandler.addToSendQueue(new C10PacketCreativeInventoryAction(-1, itemStackIn));
        }
    }

    public void onStoppedUsingItem(EntityPlayer playerIn) {
        if (Client.INSTANCE.isEnabled(KillAura.class)) {
            if (KillAura.blocking) {
                return;
            }
        }
        this.syncCurrentPlayItem();
        this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        playerIn.stopUsingItem();
    }

    public boolean gameIsSurvivalOrAdventure() {
        return this.currentGameType.isSurvivalOrAdventure();
    }

    /**
     * Checks if the player is not creative, used for checking if it should break a block instantly
     */
    public boolean isNotCreative() {
        return !this.currentGameType.isCreative();
    }

    /**
     * returns true if player is in creative mode
     */
    public boolean isInCreativeMode() {
        return this.currentGameType.isCreative();
    }

    /**
     * true for hitting entities far away.
     */
    public boolean extendedReach() {
        return this.currentGameType.isCreative();
    }

    /**
     * Checks if the player is riding a horse, used to chose the GUI to open
     */
    public boolean isRidingHorse() {
        return this.mc.thePlayer.isRiding() && this.mc.thePlayer.ridingEntity instanceof EntityHorse;
    }

    public boolean isSpectatorMode() {
        return this.currentGameType == WorldSettings.GameType.SPECTATOR;
    }

    public WorldSettings.GameType getCurrentGameType() {
        return this.currentGameType;
    }

    /**
     * Return isHittingBlock
     */
    public boolean getIsHittingBlock() {
        return this.isHittingBlock;
    }
}
