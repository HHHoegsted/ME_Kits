package com.turenidk.mekits;

import com.mojang.logging.LogUtils;
import com.turenidk.mekits.block.MEKitPackagerBlock;
import com.turenidk.mekits.blockentity.MEKitPackagerBlockEntity;
import com.turenidk.mekits.component.ModDataComponents;
import com.turenidk.mekits.item.MEKitItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import appeng.api.AECapabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import appeng.api.crafting.PatternDetailsHelper;
import com.turenidk.mekits.crafting.TestKitPattern;


@Mod(MEKits.MODID)
public class MEKits {
    public static final String MODID = "mekits";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MODID);

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    MODID
            );

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<MEKitItem> ME_KIT =
            ITEMS.register(
                    "me_kit",
                    () -> new MEKitItem(
                            new Item.Properties().stacksTo(1)
                    )
            );

    public static final DeferredItem<Item> ENCODED_ME_KIT_PATTERN =
            ITEMS.register(
                    "encoded_me_kit_pattern",
                    () -> PatternDetailsHelper
                            .encodedPatternItemBuilder(TestKitPattern::new)
                            .build()
            );

    public static final DeferredBlock<MEKitPackagerBlock> ME_KIT_PACKAGER =
            BLOCKS.register(
                    "me_kit_packager",
                    () -> new MEKitPackagerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.5F)
                                    .requiresCorrectToolForDrops()
                    )
            );

    public static final DeferredItem<BlockItem> ME_KIT_PACKAGER_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "me_kit_packager",
                    ME_KIT_PACKAGER
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEKitPackagerBlockEntity>>
            ME_KIT_PACKAGER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "me_kit_packager",
                    () -> BlockEntityType.Builder.of(
                            MEKitPackagerBlockEntity::new,
                            ME_KIT_PACKAGER.get()
                    ).build(null)
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ME_KITS_TAB =
            CREATIVE_MODE_TABS.register(
                    "me_kits",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.mekits"))
                            .icon(() -> ME_KIT.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(ME_KIT.get());
                                output.accept(ENCODED_ME_KIT_PATTERN.get());
                                output.accept(ME_KIT_PACKAGER_ITEM.get());
                            })
                            .build()
            );

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ME_KIT_PACKAGER_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> blockEntity
        );
    }

    public MEKits(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(MEKits::registerCapabilities);
    }
}