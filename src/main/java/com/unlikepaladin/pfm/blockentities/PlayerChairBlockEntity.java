package com.unlikepaladin.pfm.blockentities;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;


import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;


public class PlayerChairBlockEntity extends SkullBlockEntity {
    private BlockState cachedState;



    public PlayerChairBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        this.cachedState = state;
    }

    public BlockRenderType getRenderType(BlockState state) {
        // When inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
        return BlockRenderType.INVISIBLE;
    }
    private GameProfile owner;
    public BlockState getCachedState() {
        return this.cachedState;
    }


    @Nullable
    public GameProfile getOwner() {
        return this.owner;
    }
     @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.owner != null) {
            NbtCompound nbtCompound = new NbtCompound();
            NbtHelper.writeGameProfile(nbtCompound, this.owner);
            nbt.put("SkullOwner", nbtCompound);
            System.out.println("Writing NBT: " + nbt );
        }
        return nbt;
    }
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        System.out.println("Reading NBT");
        if (nbt.contains("SkullOwner", 10)) {
            this.setOwner(NbtHelper.toGameProfile(nbt.getCompound("SkullOwner")));
            System.out.println("NBT Contains SkullOwner: " + nbt.getCompound("SkullOwner"));
        } else if (nbt.contains("ExtraType", 8)) {
            String string = nbt.getString("ExtraType");
            if (!ChatUtil.isEmpty(string)) {
                System.out.println("Contains Extra Type: " + string);
                this.setOwner(new GameProfile((UUID)null, string));
            }
        }
    }
    public void setOwner(@Nullable GameProfile owner) {
        synchronized(this) {
            System.out.println("Setting Owner");
            this.owner = owner;
            System.out.println("setOwner owner: " + owner);
        }
        this.loadOwnerProperties();
    }
    public void loadOwnerProperties() {
        System.out.println("Loading Properties");
        loadProperties(this.owner, (owner) -> {
            this.owner = owner;
            System.out.println("from Load properties: " + owner);
            this.markDirty();

        });
    }


    @Nullable
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return new BlockEntityUpdateS2CPacket(this.pos, BlockEntityUpdateS2CPacket.SKULL, this.toInitialChunkDataNbt());
    }

}
