package com.yungnickyoung.minecraft.betterdeserttemples.world.processor;

import net.minecraft.world.entity.EntityTypes;
import com.mojang.serialization.MapCodec;
import com.yungnickyoung.minecraft.betterdeserttemples.module.StructureProcessorModule;
import com.yungnickyoung.minecraft.yungsapi.world.spawner.MobSpawnerData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Replaces yellow wool with buffed husk spawner.
 */


public class YellowWoolProcessor implements StructureProcessor {
    public static final YellowWoolProcessor INSTANCE = new YellowWoolProcessor();
    public static final MapCodec<YellowWoolProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             BlockPos blockPosLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (blockInfoGlobal.state().getBlock() == Blocks.WOOL.yellow()) {
            MobSpawnerData spawnerData = MobSpawnerData.builder()
                    .setEntityType(EntityTypes.HUSK)
                    .maxNearbyEntities(8)
                    .requiredPlayerRange(24)
                    .build();
            // SpawnData is not run through the data fixer, so the gear must be written in the current format:
            // the `equipment` map of item stacks. The pre-1.20.5 HandItems/ArmorItems lists are ignored by the game.
            HolderGetter<BannerPattern> bannerPatterns = levelReader.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
            ItemStack shield = new ItemStack(Items.SHIELD);
            shield.set(DataComponents.BASE_COLOR, DyeColor.BLUE);
            shield.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
                    .addIfRegistered(bannerPatterns, BannerPatterns.STRIPE_SMALL, DyeColor.YELLOW)
                    .addIfRegistered(bannerPatterns, BannerPatterns.STRIPE_MIDDLE, DyeColor.YELLOW)
                    .addIfRegistered(bannerPatterns, BannerPatterns.CIRCLE_MIDDLE, DyeColor.YELLOW)
                    .addIfRegistered(bannerPatterns, BannerPatterns.HALF_HORIZONTAL_MIRROR, DyeColor.BLACK)
                    .build());
            ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
            boots.set(DataComponents.DYED_COLOR, new DyedItemColor(0));
            ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
            leggings.set(DataComponents.DYED_COLOR, new DyedItemColor(1114367));

            EntityEquipment equipment = new EntityEquipment();
            equipment.set(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
            equipment.set(EquipmentSlot.OFFHAND, shield);
            equipment.set(EquipmentSlot.FEET, boots);
            equipment.set(EquipmentSlot.LEGS, leggings);
            equipment.set(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
            equipment.set(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            spawnerData.nextSpawnData.getEntityToSpawn().store("equipment", EntityEquipment.CODEC,
                    RegistryOps.create(NbtOps.INSTANCE, levelReader.registryAccess()), equipment);
            CompoundTag nbt = spawnerData.save();
            blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.SPAWNER.defaultBlockState(), nbt);
        }
        return blockInfoGlobal;
    }

    @Override
    public MapCodec<? extends StructureProcessor> codec() {
        return StructureProcessorModule.YELLOW_WOOL_PROCESSOR;
    }
}
