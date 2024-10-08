package com.unlikepaladin.pfm.compat.cookingforblockheads.neoforge;

import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.blocks.KitchenCounterBlock;
import com.unlikepaladin.pfm.blocks.KitchenSinkBlock;
import com.unlikepaladin.pfm.blocks.KitchenWallCounterBlock;
import com.unlikepaladin.pfm.blocks.StoveBlock;
import com.unlikepaladin.pfm.blocks.neoforge.StoveBlockImpl;
import com.unlikepaladin.pfm.compat.cookingforblockheads.neoforge.menu.StoveScreenHandlerBalm;
import com.unlikepaladin.pfm.registry.BlockEntities;
import com.unlikepaladin.pfm.registry.TriFunc;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.container.CombinedContainer;
import net.blay09.mods.balm.api.container.ContainerUtils;
import net.blay09.mods.balm.neoforge.provider.NeoForgeBalmProviders;
import net.blay09.mods.cookingforblockheads.KitchenMultiBlock;
import net.blay09.mods.cookingforblockheads.api.SourceItem;
import net.blay09.mods.cookingforblockheads.api.capability.DefaultKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.capability.IngredientPredicate;
import net.blay09.mods.cookingforblockheads.item.ModItems;
import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PFMCookingForBlockHeadsCompat {

    public static void initBlockConnectors() {
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterBlock.class).getAllBlocks().forEach(KitchenMultiBlock::registerConnectorBlock);
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallCounterBlock.class).getAllBlocks().forEach(KitchenMultiBlock::registerConnectorBlock);
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenSinkBlock.class).getAllBlocks().forEach(KitchenMultiBlock::registerConnectorBlock);
    }

    public static final PFMCookingTableBlock COOKING_TABLE_BLOCK = new PFMCookingTableBlock(AbstractBlock.Settings.copy(Blocks.GRAY_CONCRETE));//PaladinFurnitureModBlocksItems.GRAY_STOVE));
    public static <T extends ScreenHandler> TriFunc<Integer, PlayerInventory, PacketByteBuf, T> getStoveScreenHandler() {
        return (integer, playerInventory, packetByteBuf) -> {
            BlockPos pos = packetByteBuf.readBlockPos();
            BlockEntity blockEntity = playerInventory.player.getWorld().getBlockEntity(pos);
            return (T) new StoveScreenHandlerBalm(integer, playerInventory, (StoveBlockEntityBalm)blockEntity);
        };
    }

    public static void openMenuScreen(World world, BlockPos pos, PlayerEntity player) {
        StoveBlockEntityBalm stove = (StoveBlockEntityBalm)world.getBlockEntity(pos);
        if (!world.isClient) {
            Balm.getNetworking().openGui(player, stove);
        }
    }

    public static BlockEntity getStoveBlockEntity(BlockPos pos, BlockState state) {
        return new StoveBlockEntityBalm(pos, state);
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> getStoveTicker(World world, BlockEntityType<T> type) {
        if (world.isClient) {
            return StoveBlockImpl.checkType(type, BlockEntities.STOVE_BLOCK_ENTITY, StoveBlockEntityBalm::clientTick);
        } else {
            return StoveBlockImpl.checkType(type, BlockEntities.STOVE_BLOCK_ENTITY, StoveBlockEntityBalm::serverTick);
        }
    }

    public static ActionResult onUseStove(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);
        if (heldItem.getItem() == ModItems.heatingUnit) {
            return ActionResult.PASS;
        } else if (hit.getSide() == Direction.UP && CookingRegistry.isToolItem(heldItem)) {
            Direction stateFacing = state.get(StoveBlock.FACING);
            double hx =  (hit.getPos().x - hit.getBlockPos().getX());
            double hz = (hit.getPos().z - hit.getBlockPos().getZ());
            switch (stateFacing) {
                case NORTH:
                    hx = 1.0 - (hit.getPos().x - hit.getBlockPos().getX());
                    hz = 1.0 - (hit.getPos().z - hit.getBlockPos().getZ());
                    break;
                case WEST:
                    hz = 1.0 - (hit.getPos().x - hit.getBlockPos().getX());
                    hx = (hit.getPos().z - hit.getBlockPos().getZ());
                    break;
                case EAST:
                    hz = (hit.getPos().x - hit.getBlockPos().getX());
                    hx = 1.0 - (hit.getPos().z - hit.getBlockPos().getZ());
            }
            int index = -1;
            if (hx < 0.5f && hz < 0.5f) {
                index = 1;
            } else if (hx >= 0.5f && hz < 0.5f) {
                index = 0;
            } else if (hx < 0.5f && hz >= 0.5f) {
                index = 3;
            } else if (hx >= 0.5f && hz >= 0.5f) {
                index = 2;
            }
            if (index != -1) {
                StoveBlockEntityBalm tileOven = (StoveBlockEntityBalm)level.getBlockEntity(pos);
                if (tileOven != null && tileOven.getToolItem(index).isEmpty()) {
                    ItemStack toolItem = heldItem.split(1);
                    tileOven.setToolItem(index, toolItem);
                }
            }
            return ActionResult.SUCCESS;
        } else {
            StoveBlockEntityBalm oven = (StoveBlockEntityBalm)level.getBlockEntity(pos);
            if (hit.getSide() == state.get(Properties.HORIZONTAL_FACING) && oven != null) {
                if (player.isSneaking()) {
                    return ActionResult.SUCCESS;
                }

                if (!heldItem.isEmpty() && oven.getSmeltingResult(heldItem) != ItemStack.EMPTY) {
                    heldItem = ContainerUtils.insertItemStacked(oven.getInputContainer(), heldItem, false);
                    player.setStackInHand(hand, heldItem);

                    return ActionResult.SUCCESS;
                } else if (!heldItem.isEmpty() && StoveBlockEntityBalm.isItemFuel(heldItem)) {
                    heldItem = ContainerUtils.insertItemStacked(oven.getFuelContainer(), heldItem, false);
                    player.setStackInHand(hand, heldItem);
                    return ActionResult.SUCCESS;
                }
            }
            if (!level.isClient) {
                Balm.getNetworking().openGui(player, oven);
            }
            return ActionResult.SUCCESS;
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(((NeoForgeBalmProviders)Balm.getProviders()).getBlockCapability(DefaultKitchenItemProvider.class), BlockEntities.FREEZER_BLOCK_ENTITY, (entity, side) -> {
            return new DefaultKitchenItemProvider(entity){
                private final ItemStack snowStack;
                private final ItemStack iceStack;
                {
                    this.snowStack = new ItemStack(Items.SNOWBALL);
                    this.iceStack = new ItemStack(Blocks.ICE);
                }
                private @Nullable SourceItem applyIceUnit(IngredientPredicate predicate, int maxAmount) {
                    if (predicate.test(this.snowStack, 64))
                        return new SourceItem(this, -1, ContainerUtils.copyStackWithSize(this.snowStack, maxAmount));
                    else
                        return predicate.test(this.iceStack, 64) ? new SourceItem(this, -1, ContainerUtils.copyStackWithSize(this.iceStack, maxAmount)) : null;
                }
                public @Nullable SourceItem findSource(IngredientPredicate predicate, int maxAmount, List<IKitchenItemProvider> inventories, boolean requireBucket, boolean simulate) {
                    SourceItem iceUnitResult = this.applyIceUnit(predicate, maxAmount);
                    if (iceUnitResult != null) {
                        return iceUnitResult;
                    } else {
                        return super.findSource(predicate, maxAmount, inventories, requireBucket, simulate);
                    }             }
                public @Nullable SourceItem findSourceAndMarkAsUsed(IngredientPredicate predicate, int maxAmount, List<IKitchenItemProvider> inventories, boolean requireBucket, boolean simulate) {
                    SourceItem iceUnitResult = this.applyIceUnit(predicate, maxAmount);
                    if (iceUnitResult != null) {                     return iceUnitResult;
                    } else {
                        return super.findSourceAndMarkAsUsed(predicate, maxAmount, inventories, requireBucket, simulate);
                    }
                }
            };
        });

        event.registerBlockEntity(((NeoForgeBalmProviders)Balm.getProviders()).getBlockCapability(DefaultKitchenItemProvider.class), BlockEntities.STOVE_BLOCK_ENTITY, (entity, side) -> {
            StoveBlockEntityBalm ovenBlockEntityBalm = (StoveBlockEntityBalm) entity;
            return new DefaultKitchenItemProvider(new CombinedContainer(ovenBlockEntityBalm.toolsContainer, ovenBlockEntityBalm.outputContainer));
        });
    }
}