/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.AbstractState;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.GetAmbientOcclusionLightLevelListener.GetAmbientOcclusionLightLevelEvent;
import net.wurstclient.events.IsNormalCubeListener.IsNormalCubeEvent;
import net.wurstclient.hack.HackList;
import net.wurstclient.hacks.HandNoClipHack;

@Mixin(AbstractBlockState.class)
public class AbstractBlockStateMixin extends AbstractState<Block, BlockState>
	implements State<BlockState>
{
	private AbstractBlockStateMixin(WurstClient wurst, Block object_1,
		ImmutableMap<Property<?>, Comparable<?>> immutableMap_1)
	{
		super(object_1, immutableMap_1);
	}
	
	@Inject(at = {@At("TAIL")},
		method = {
			"isFullCube(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z"},
		cancellable = true)
	private void onIsFullCube(BlockView world, BlockPos pos,
		CallbackInfoReturnable<Boolean> cir)
	{
		EventManager eventManager = WurstClient.INSTANCE.getEventManager();
		if(eventManager == null)
			return;
		
		IsNormalCubeEvent event = new IsNormalCubeEvent();
		eventManager.fire(event);
		
		cir.setReturnValue(cir.getReturnValue() && !event.isCancelled());
	}
	
	@Inject(at = {@At("TAIL")},
		method = {
			"getAmbientOcclusionLightLevel(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"},
		cancellable = true)
	private void onGetAmbientOcclusionLightLevel(BlockView blockView,
		BlockPos blockPos, CallbackInfoReturnable<Float> cir)
	{
		GetAmbientOcclusionLightLevelEvent event =
			new GetAmbientOcclusionLightLevelEvent((BlockState)(Object)this,
				cir.getReturnValueF());
		
		WurstClient.INSTANCE.getEventManager().fire(event);
		cir.setReturnValue(event.getLightLevel());
	}
	
	@Inject(at = {@At("HEAD")},
		method = {
			"getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"},
		cancellable = true)
	private void onGetOutlineShape(BlockView view, BlockPos pos,
		ShapeContext context, CallbackInfoReturnable<VoxelShape> cir)
	{
		if(context == ShapeContext.absent())
			return;
		
		HackList hax = WurstClient.INSTANCE.getHax();
		if(hax == null)
			return;
		
		HandNoClipHack handNoClipHack = hax.handNoClipHack;
		if(!handNoClipHack.isEnabled() || handNoClipHack.isBlockInList(pos))
			return;
		
		cir.setReturnValue(VoxelShapes.empty());
	}
}
