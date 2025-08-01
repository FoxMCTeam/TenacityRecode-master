package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TileEntityBeacon extends TileEntityLockable implements ITickable, IInventory
{
    /** List of effects that Beacon can apply */
    public static final Potion[][] effectsList = new Potion[][] {{Potion.moveSpeed, Potion.digSpeed}, {Potion.resistance, Potion.jump}, {Potion.damageBoost}, {Potion.regeneration}};
    private final List<TileEntityBeacon.BeamSegment> beamSegments = Lists.<TileEntityBeacon.BeamSegment>newArrayList();
    private long beamRenderCounter;
    private float field_146014_j;
    private boolean isComplete;

    /** Level of this beacon's pyramid. */
    private int levels = -1;

    /** Primary potion effect given by this beacon. */
    private int primaryEffect;

    /** Secondary potion effect given by this beacon. */
    private int secondaryEffect;

    /** Item given to this beacon as payment. */
    private ItemStack payment;
    private String customName;

    //betterfps
    private int tickCount = 0;

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        if (this.worldObj.getTotalWorldTime() % 80L == 0L)
        {
            this.updateBeacon();
        }
        tickCount--;
        if(tickCount == 100) {
            updateEffects(pos.getX(), pos.getY(), pos.getZ());
        } else if(tickCount <= 0) {
            updateBeacon();
        }
    }

    private void updateEffects(int x, int y, int z) {
        if((isComplete) && (levels > 0) && (!worldObj.isRemote) && (primaryEffect > 0)) {
            int radius = (levels + 1) * 10;
            byte effectLevel = 0;
            if((levels >= 4) && (primaryEffect == secondaryEffect)) {
                effectLevel = 1;
            }
            AxisAlignedBB box = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
            box = box.expand(radius, radius, radius).addCoord(0.0D, worldObj.getHeight(), 0.0D);
            Iterator iterator = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box).iterator();

            boolean hasSecondaryEffect = (levels >= 4) && (primaryEffect != secondaryEffect) && (secondaryEffect > 0);

            while(iterator.hasNext()) {
                EntityPlayer player = (EntityPlayer)iterator.next();
                player.addPotionEffect(new PotionEffect(primaryEffect, 180, effectLevel, true, true));
                if(hasSecondaryEffect) {
                    player.addPotionEffect(new PotionEffect(secondaryEffect, 180, 0, true, true));
                }
            }
        }
    }

    public void updateBeacon()
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if(worldObj.isRemote) {
            updateGlassLayers(x, y, z);
        } else {
            updateActivation(x, y, z);
        }
        updateLevels(x, y, z);
        updateEffects(x, y, z);
        tickCount = 200;
        this.updateSegmentColors();
        this.addEffectsToPlayers();
    }

    private void updateGlassLayers(int x, int y, int z) {
        // Checks if the beacon should be active and searches for stained glass to color the beam.
        // Should be only called client-side
        isComplete = true;
        beamSegments.clear();
        BeamSegment beam = new BeamSegment(EntitySheep.getDyeRgb(EnumDyeColor.WHITE));
        float[] oldColor = null;
        beamSegments.add(beam);
        int height = worldObj.getActualHeight();
        for(int blockY = y + 1; blockY < height; blockY++) {
            BlockPos pos = new BlockPos(x, blockY, z);
            IBlockState state = this.worldObj.getBlockState(pos);
            Block b = state.getBlock();
            float[] color;
            if(b == Blocks.stained_glass) {
                color = EntitySheep.getDyeRgb(state.getValue(BlockStainedGlass.COLOR));
            } else if(b == Blocks.stained_glass_pane) {
                color = EntitySheep.getDyeRgb(state.getValue(BlockStainedGlassPane.COLOR));
            } else {
                if(b.getLightOpacity() >= 15) {
                    isComplete = false;
                    beamSegments.clear();
                    break;
                }
                beam.incrementHeight();
                continue;
            }

            if(oldColor != null) {
                color = new float[]{(oldColor[0] + color[0]) / 2.0F, (oldColor[1] + color[1]) / 2.0F, (oldColor[2] + color[2]) / 2.0F};
            }
            if(Arrays.equals(color, oldColor)) {
                beam.incrementHeight();
            } else {
                beam = new BeamSegment(color);
                beamSegments.add(beam);
                oldColor = color;
            }
        }
    }

    private void updateActivation(int x, int y, int z) {
        // Checks if the beacon should be activated
        // Should be called only server-side. updateGlassLayers do the trick on the client
        isComplete = true;
        int height = worldObj.getActualHeight();
        for(int blockY = y + 1; blockY < height; blockY++) {
            BlockPos pos = new BlockPos(x, blockY, z);
            IBlockState state = this.worldObj.getBlockState(pos);
            Block b = state.getBlock();
            if(b.getLightOpacity() >= 15) {
                isComplete = false;
                break;
            }
        }
    }

    private void updateLevels(int x, int y, int z) {
        // Checks if the beacon should be active and how many levels it should have.
        boolean isClient = worldObj.isRemote;
        int levelsOld = levels;
        lvlLoop: for(int lvl = 1; lvl <= 4; lvl++) {
            levels = lvl;
            int blockY = y - lvl;
            if(blockY < 0) break;

            for(int blockX = x - lvl; blockX <= x + lvl; blockX++) {
                for(int blockZ = z - lvl; blockZ <= z + lvl; blockZ++) {
                    BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);
                    Block block = worldObj.getBlockState(blockPos).getBlock();
                    if(!block.isBeaconBase(worldObj, blockPos, pos)) {
                        levels--;
                        break lvlLoop;
                    }
                }
            }

            if(isClient) break;
        }
        if(levels == 0) {
            this.isComplete = false;
        }

        if((!isClient) && (levels == 4) && (levelsOld < levels)) {
            AxisAlignedBB box = new AxisAlignedBB(x, y, z, x, y - 4, z).expand(10.0, 5.0, 10.0);
            Iterator iterator = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box).iterator();
            while(iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer)iterator.next();
                entityplayer.triggerAchievement(AchievementList.fullBeacon);
            }
        }
    }



    private void addEffectsToPlayers()
    {
        updateEffects(pos.getX(), pos.getY(), pos.getZ());
    }

    private void updateSegmentColors()
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if(worldObj.isRemote) {
            updateGlassLayers(x, y, z);
        } else {
            updateActivation(x, y, z);
        }
        updateLevels(x, y, z);
    }

    public List<TileEntityBeacon.BeamSegment> getBeamSegments()
    {
        return this.beamSegments;
    }

    public float shouldBeamRender()
    {
        if (!this.isComplete)
        {
            return 0.0F;
        }
        else
        {
            int i = (int)(this.worldObj.getTotalWorldTime() - this.beamRenderCounter);
            this.beamRenderCounter = this.worldObj.getTotalWorldTime();

            if (i > 1)
            {
                this.field_146014_j -= (float)i / 40.0F;

                if (this.field_146014_j < 0.0F)
                {
                    this.field_146014_j = 0.0F;
                }
            }

            this.field_146014_j += 0.025F;

            if (this.field_146014_j > 1.0F)
            {
                this.field_146014_j = 1.0F;
            }

            return this.field_146014_j;
        }
    }

    /**
     * Allows for a specialized description packet to be created. This is often used to sync tile entity data from the
     * server to the client easily. For example this is used by signs to synchronise the text to be displayed.
     */
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.pos, 3, nbttagcompound);
    }

    public double getMaxRenderDistanceSquared()
    {
        return 65536.0D;
    }

    private int func_183001_h(int p_183001_1_)
    {
        if (p_183001_1_ >= 0 && p_183001_1_ < Potion.potionTypes.length && Potion.potionTypes[p_183001_1_] != null)
        {
            Potion potion = Potion.potionTypes[p_183001_1_];
            return potion != Potion.moveSpeed && potion != Potion.digSpeed && potion != Potion.resistance && potion != Potion.jump && potion != Potion.damageBoost && potion != Potion.regeneration ? 0 : p_183001_1_;
        }
        else
        {
            return 0;
        }
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.primaryEffect = this.func_183001_h(compound.getInteger("Primary"));
        this.secondaryEffect = this.func_183001_h(compound.getInteger("Secondary"));
        this.levels = compound.getInteger("Levels");
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("Primary", this.primaryEffect);
        compound.setInteger("Secondary", this.secondaryEffect);
        compound.setInteger("Levels", this.levels);
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory()
    {
        return 1;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index)
    {
        return index == 0 ? this.payment : null;
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count)
    {
        if (index == 0 && this.payment != null)
        {
            if (count >= this.payment.stackSize)
            {
                ItemStack itemstack = this.payment;
                this.payment = null;
                return itemstack;
            }
            else
            {
                this.payment.stackSize -= count;
                return new ItemStack(this.payment.getItem(), count, this.payment.getMetadata());
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index)
    {
        if (index == 0 && this.payment != null)
        {
            ItemStack itemstack = this.payment;
            this.payment = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index == 0)
        {
            this.payment = stack;
        }
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName()
    {
        return this.hasCustomName() ? this.customName : "container.beacon";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return this.customName != null && this.customName.length() > 0;
    }

    public void setName(String name)
    {
        this.customName = name;
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit()
    {
        return 1;
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return this.worldObj.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
    {
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return stack.getItem() == Items.emerald || stack.getItem() == Items.diamond || stack.getItem() == Items.gold_ingot || stack.getItem() == Items.iron_ingot;
    }

    public String getGuiID()
    {
        return "minecraft:beacon";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerBeacon(playerInventory, this);
    }

    public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return this.levels;

            case 1:
                return this.primaryEffect;

            case 2:
                return this.secondaryEffect;

            default:
                return 0;
        }
    }

    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                this.levels = value;
                break;

            case 1:
                this.primaryEffect = this.func_183001_h(value);
                break;

            case 2:
                this.secondaryEffect = this.func_183001_h(value);
        }
    }

    public int getFieldCount()
    {
        return 3;
    }

    public void clear()
    {
        this.payment = null;
    }

    public boolean receiveClientEvent(int id, int type)
    {
        if (id == 1)
        {
            this.updateBeacon();
            return true;
        }
        else
        {
            return super.receiveClientEvent(id, type);
        }
    }

    public static class BeamSegment
    {
        private final float[] colors;
        private int height;

        public BeamSegment(float[] p_i45669_1_)
        {
            this.colors = p_i45669_1_;
            this.height = 1;
        }

        protected void incrementHeight()
        {
            ++this.height;
        }

        public float[] getColors()
        {
            return this.colors;
        }

        public int getHeight()
        {
            return this.height;
        }
    }
}
