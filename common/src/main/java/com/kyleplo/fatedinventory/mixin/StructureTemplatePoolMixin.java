package com.kyleplo.fatedinventory.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolMixin {
	@Accessor("rawTemplates")
	List<Pair<StructurePoolElement, Integer>> getTemplateCounts();

	@Mutable
	@Accessor("rawTemplates")
	void setTemplateCounts(List<Pair<StructurePoolElement, Integer>> elementCounts);

	@Accessor("templates")
	ObjectArrayList<StructurePoolElement> getTemplates();

	@Mutable
	@Accessor("templates")
	void setTemplate(ObjectArrayList<StructurePoolElement> elements);
}