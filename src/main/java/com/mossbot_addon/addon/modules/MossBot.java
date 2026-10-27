package com.mossbot_addon.addon.modules;

import com.mossbot_addon.addon.mossbot_addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MossBot extends Module {
    private static final Set<Block> ALL_FULL_LIGHT_BLOCKS = Set.of(
        Blocks.GLOWSTONE,
        Blocks.SEA_LANTERN,
        Blocks.JACK_O_LANTERN,
        Blocks.SHROOMLIGHT,
        Blocks.OCHRE_FROGLIGHT,
        Blocks.VERDANT_FROGLIGHT,
        Blocks.PEARLESCENT_FROGLIGHT,
        Blocks.CRYING_OBSIDIAN,
        Blocks.MAGMA_BLOCK,
        Blocks.BEACON,
        Blocks.COPPER_BULB,
        Blocks.EXPOSED_COPPER_BULB,
        Blocks.WEATHERED_COPPER_BULB,
        Blocks.OXIDIZED_COPPER_BULB,
        Blocks.WAXED_COPPER_BULB,
        Blocks.WAXED_EXPOSED_COPPER_BULB,
        Blocks.WAXED_WEATHERED_COPPER_BULB,
        Blocks.WAXED_OXIDIZED_COPPER_BULB
    );

    private static final Set<Block> CONVERTIBLE_BLOCKS = Set.of(
        Blocks.STONE,
        Blocks.DEEPSLATE,
        Blocks.GRANITE,
        Blocks.DIORITE,
        Blocks.ANDESITE,
        Blocks.TUFF,
        Blocks.DIRT,
        Blocks.COARSE_DIRT,
        Blocks.PODZOL,
        Blocks.MYCELIUM,
        Blocks.ROOTED_DIRT
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCrafting = settings.createGroup("Auto Crafting");
    private final SettingGroup sgLighting = settings.createGroup("Spawn Proofing");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum block reach range for bonemealing and lighting.")
        .defaultValue(4.5)
        .min(1.0)
        .max(6.0)
        .build()
    );

    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Tick delay between bonemeal actions.")
        .defaultValue(0)
        .min(0)
        .max(20)
        .build()
    );

    private final Setting<Integer> actionsPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("actions-per-tick")
        .description("How many grass-break or bonemeal actions to perform per tick.")
        .defaultValue(1)
        .min(1)
        .max(10)
        .sliderMin(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> fovAngle = sgGeneral.add(new IntSetting.Builder()
        .name("fov-angle")
        .description("Maximum angle from your crosshair to target blocks.")
        .defaultValue(80)
        .min(15)
        .max(180)
        .sliderMin(15)
        .sliderMax(180)
        .build()
    );

    private final Setting<Boolean> smartSpread = sgGeneral.add(new BoolSetting.Builder()
        .name("smart-spread")
        .description("Prioritize moss blocks next to stone/dirt/deepslate so moss expands outward.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> bonemealAzalea = sgGeneral.add(new BoolSetting.Builder()
        .name("bonemeal-azaleas")
        .description("Also bonemeal nearby azalea trees/bushes to grow oak trees.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> breakGrass = sgGeneral.add(new BoolSetting.Builder()
        .name("break-grass")
        .description("Automatically break grass and vegetation spawned on moss.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Automatically rotate towards the target block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> craftBoneBlocks = sgCrafting.add(new BoolSetting.Builder()
        .name("craft-bone-blocks")
        .description("Automatically craft bone blocks into bone meal when space allows.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> craftBlocksPerTick = sgCrafting.add(new IntSetting.Builder()
        .name("craft-blocks-per-tick")
        .description("Maximum number of bone blocks to convert into bone meal per craft cycle.")
        .defaultValue(4)
        .min(1)
        .max(64)
        .sliderMin(1)
        .sliderMax(64)
        .build()
    );

    private final Setting<Integer> craftLimitPercent = sgCrafting.add(new IntSetting.Builder()
        .name("craft-limit-percent")
        .description("Percentage of available inventory space allowed to be filled with bone meal.")
        .defaultValue(50)
        .min(1)
        .max(100)
        .sliderMin(1)
        .sliderMax(100)
        .build()
    );

    private final Setting<Boolean> spawnProof = sgLighting.add(new BoolSetting.Builder()
        .name("spawn-proof")
        .description("Automatically place full light blocks to light up dark surrounding areas.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> maxBlockLight = sgLighting.add(new IntSetting.Builder()
        .name("max-block-light")
        .description("Place a light block when the floor's block light level is at or below this value.")
        .defaultValue(0)
        .min(0)
        .max(14)
        .sliderMin(0)
        .sliderMax(14)
        .visible(spawnProof::get)
        .build()
    );

    private final Setting<Boolean> lightWhitelistMode = sgLighting.add(new BoolSetting.Builder()
        .name("light-whitelist-mode")
        .description("Only use the light blocks selected in the whitelist below.")
        .defaultValue(false)
        .visible(spawnProof::get)
        .build()
    );

    private final Setting<List<Block>> lightWhitelist = sgLighting.add(new BlockListSetting.Builder()
        .name("light-whitelist")
        .description("Select which full light blocks are allowed to be placed.")
        .defaultValue(
            Blocks.GLOWSTONE,
            Blocks.SEA_LANTERN,
            Blocks.JACK_O_LANTERN,
            Blocks.SHROOMLIGHT,
            Blocks.OCHRE_FROGLIGHT,
            Blocks.VERDANT_FROGLIGHT,
            Blocks.PEARLESCENT_FROGLIGHT
        )
        .filter(ALL_FULL_LIGHT_BLOCKS::contains)
        .visible(() -> spawnProof.get() && lightWhitelistMode.get())
        .build()
    );

    private int timer = 0;
    private int craftCooldown = 0;
    private boolean pendingCraftCollect = false;
    private int pendingCraftTimeout = 0;

    public MossBot() {
        super(
            mossbot_addon.CATEGORY,
            "moss-boss",
            "Automatically bonemeals moss and azaleas with smart crafting and spawn-proofing - By JerkinOffInMyCar."
        );

        if (this.color != null) {
            this.color.set(mossbot_addon.ADDON_COLOR);
        }

        try {
            Field addonField = Module.class.getDeclaredField("addon");
            addonField.setAccessible(true);
            addonField.set(this, null);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDeactivate() {
        pendingCraftCollect = false;
        pendingCraftTimeout = 0;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (craftCooldown > 0) craftCooldown--;

        if (craftBoneBlocks.get()) {
            handleBoneMealCrafting();
        }

        if (spawnProof.get()) {
            trySpawnProofArea();
        }

        FindItemResult bonemeal = getAvailableBonemeal();
        if (!bonemeal.found()) return;

        if (timer > 0) {
            timer--;
            return;
        }

        BlockPos playerPos = mc.player.getBlockPos();
        int searchRadius = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d lookVec = mc.player.getRotationVec(1.0F).normalize();
        double minCosAngle = Math.cos(Math.toRadians(fovAngle.get()));

        List<TargetCandidate> candidates = new ArrayList<>();

        for (BlockPos pos : BlockPos.iterate(
            playerPos.add(-searchRadius, -searchRadius, -searchRadius),
            playerPos.add(searchRadius, searchRadius, searchRadius)
        )) {
            Vec3d center = Vec3d.ofCenter(pos);
            double distSq = eyePos.squaredDistanceTo(center);
            if (distSq > rangeSq) continue;

            Vec3d toBlock = center.subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toBlock);
            if (fovAngle.get() < 180 && dot < minCosAngle) continue;

            BlockState state = mc.world.getBlockState(pos);
            boolean isMoss = state.isOf(Blocks.MOSS_BLOCK);
            boolean isAzalea = bonemealAzalea.get() && (state.isOf(Blocks.AZALEA) || state.isOf(Blocks.FLOWERING_AZALEA));

            if (!isMoss && !isAzalea) continue;

            BlockPos immutablePos = pos.toImmutable();
            BlockPos abovePos = immutablePos.up();
            BlockState aboveState = mc.world.getBlockState(abovePos);
            boolean hasGrassAbove = isBreakableVegetation(aboveState);

            if (isMoss && !aboveState.isAir() && !(breakGrass.get() && hasGrassAbove)) {
                continue;
            }

            double projection = center.subtract(eyePos).dotProduct(lookVec);
            Vec3d closestPointOnRay = eyePos.add(lookVec.multiply(Math.max(0.0, projection)));
            double rayDistSq = center.squaredDistanceTo(closestPointOnRay);

            boolean canSpread = !isMoss || !smartSpread.get() || hasConvertibleNeighbor(immutablePos);

            candidates.add(new TargetCandidate(immutablePos, hasGrassAbove, canSpread, rayDistSq));
        }

        if (candidates.isEmpty()) return;

        candidates.sort(
            Comparator.comparing((TargetCandidate c) -> !c.canSpread)
                .thenComparingDouble(c -> c.rayDistSq)
        );

        int actionsDone = 0;
        int maxActions = actionsPerTick.get();

        for (TargetCandidate candidate : candidates) {
            if (actionsDone >= maxActions) break;

            if (breakGrass.get() && candidate.hasGrassAbove) {
                BlockPos abovePos = candidate.pos.up();
                if (mc.player.squaredDistanceTo(Vec3d.ofCenter(abovePos)) <= rangeSq) {
                    BlockUtils.breakBlock(abovePos, true);
                    actionsDone++;
                    continue;
                }
            }

            InvUtils.swap(bonemeal.slot(), true);

            BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(candidate.pos),
                Direction.UP,
                candidate.pos,
                false
            );

            BlockUtils.interact(hitResult, Hand.MAIN_HAND, rotate.get());
            InvUtils.swapBack();

            actionsDone++;
        }

        if (actionsDone > 0) {
            timer = delaySetting.get();
        }
    }

    private boolean isBreakableVegetation(BlockState state) {
        return state.isOf(Blocks.SHORT_GRASS)
            || state.isOf(Blocks.FERN)
            || state.isOf(Blocks.TALL_GRASS)
            || state.isOf(Blocks.LARGE_FERN);
    }

    private boolean hasConvertibleNeighbor(BlockPos mossPos) {
        if (mc.world == null) return false;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos checkPos = mossPos.add(dx, dy, dz);
                    if (CONVERTIBLE_BLOCKS.contains(mc.world.getBlockState(checkPos).getBlock())
                        && mc.world.getBlockState(checkPos.up()).isAir()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void trySpawnProofArea() {
        if (mc.player == null || mc.world == null) return;

        FindItemResult lightItem = getAvailableLightBlock();
        if (!lightItem.found()) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int searchRadius = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        int lightLimit = maxBlockLight.get();

        BlockPos bestPos = null;
        double closestDistSq = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterate(
            playerPos.add(-searchRadius, -searchRadius, -searchRadius),
            playerPos.add(searchRadius, searchRadius, searchRadius)
        )) {
            double distSq = mc.player.squaredDistanceTo(Vec3d.ofCenter(pos));
            if (distSq > rangeSq || distSq >= closestDistSq) continue;

            if (!mc.world.getBlockState(pos).isAir()) continue;

            BlockPos floorPos = pos.down();
            BlockState floorState = mc.world.getBlockState(floorPos);
            if (ALL_FULL_LIGHT_BLOCKS.contains(floorState.getBlock())) continue;
            if (!floorState.isSideSolidFullSquare(mc.world, floorPos, Direction.UP)) continue;

            if (mc.world.getLightLevel(LightType.BLOCK, pos) <= lightLimit && BlockUtils.canPlace(pos, true)) {
                bestPos = pos.toImmutable();
                closestDistSq = distSq;
            }
        }

        if (bestPos != null) {
            BlockUtils.place(bestPos, lightItem, rotate.get(), 50, true);
        }
    }

    private FindItemResult getAvailableLightBlock() {
        FindItemResult hotbarLight = InvUtils.findInHotbar(this::isValidLightStack);
        if (hotbarLight.found()) {
            return hotbarLight;
        }

        FindItemResult invLight = InvUtils.find(this::isValidLightStack);
        if (invLight.found()) {
            InvUtils.move().from(invLight.slot()).toHotbar(1);
            return InvUtils.findInHotbar(this::isValidLightStack);
        }

        return invLight;
    }

    private boolean isValidLightStack(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();
        if (!ALL_FULL_LIGHT_BLOCKS.contains(block)) return false;
        return !lightWhitelistMode.get() || lightWhitelist.get().contains(block);
    }

    private FindItemResult getAvailableBonemeal() {
        FindItemResult hotbarBonemeal = InvUtils.findInHotbar(Items.BONE_MEAL);
        if (hotbarBonemeal.found()) {
            return hotbarBonemeal;
        }

        FindItemResult invBonemeal = InvUtils.find(Items.BONE_MEAL);
        if (invBonemeal.found()) {
            InvUtils.move().from(invBonemeal.slot()).toHotbar(0);
            return InvUtils.findInHotbar(Items.BONE_MEAL);
        }

        return invBonemeal;
    }

    private void handleBoneMealCrafting() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof PlayerScreenHandler handler)) return;

        if (pendingCraftCollect) {
            pendingCraftTimeout++;
            if (handler.getSlot(0).hasStack()) {
                mc.interactionManager.clickSlot(handler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                if (handler.getSlot(1).hasStack()) {
                    mc.interactionManager.clickSlot(handler.syncId, 1, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
                pendingCraftCollect = false;
                pendingCraftTimeout = 0;
                craftCooldown = 5;
                return;
            }
            if (pendingCraftTimeout > 10) {
                if (handler.getSlot(1).hasStack()) {
                    mc.interactionManager.clickSlot(handler.syncId, 1, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
                pendingCraftCollect = false;
                pendingCraftTimeout = 0;
                craftCooldown = 5;
            }
            return;
        }

        if (craftCooldown > 0) return;

        FindItemResult boneBlocks = InvUtils.find(Items.BONE_BLOCK);
        if (!boneBlocks.found()) return;

        int totalMainSlots = 36;
        int otherItemSlots = 0;
        int emptySlots = 0;
        int currentBoneMealItems = 0;
        int partialBoneMealSpace = 0;

        for (int i = 0; i < totalMainSlots; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                emptySlots++;
            } else if (stack.isOf(Items.BONE_MEAL)) {
                currentBoneMealItems += stack.getCount();
                partialBoneMealSpace += (stack.getMaxCount() - stack.getCount());
            } else {
                otherItemSlots++;
            }
        }

        int availableSlotsForBonemeal = totalMainSlots - otherItemSlots;
        if (availableSlotsForBonemeal <= 0) return;

        int targetMaxBoneMeal = (int) Math.floor((availableSlotsForBonemeal * 64) * (craftLimitPercent.get() / 100.0));
        int neededBoneMeal = targetMaxBoneMeal - currentBoneMealItems;

        int physicalSpace = (emptySlots * 64) + partialBoneMealSpace;
        int allowedAdditionalBoneMeal = Math.min(neededBoneMeal, physicalSpace);

        if (allowedAdditionalBoneMeal < 3) return;

        ItemStack sourceStack = mc.player.getInventory().getStack(boneBlocks.slot());
        int blocksInSlot = sourceStack.getCount();

        int maxConvertibleBlocks = Math.min(blocksInSlot, allowedAdditionalBoneMeal / 3);
        int blocksThisTick = Math.min(maxConvertibleBlocks, craftBlocksPerTick.get());

        if (blocksThisTick <= 0) return;

        int screenSlotId = boneBlocks.slot() < 9 ? boneBlocks.slot() + 36 : boneBlocks.slot();

        mc.interactionManager.clickSlot(handler.syncId, screenSlotId, 0, SlotActionType.PICKUP, mc.player);

        if (blocksThisTick == blocksInSlot) {
            mc.interactionManager.clickSlot(handler.syncId, 1, 0, SlotActionType.PICKUP, mc.player);
        } else {
            for (int i = 0; i < blocksThisTick; i++) {
                mc.interactionManager.clickSlot(handler.syncId, 1, 1, SlotActionType.PICKUP, mc.player);
            }
            mc.interactionManager.clickSlot(handler.syncId, screenSlotId, 0, SlotActionType.PICKUP, mc.player);
        }

        pendingCraftCollect = true;
        pendingCraftTimeout = 0;
    }

    private record TargetCandidate(BlockPos pos, boolean hasGrassAbove, boolean canSpread, double rayDistSq) {}
}
