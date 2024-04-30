package io.github.diiiaz.super_pistons.mixin;

import io.github.diiiaz.super_pistons.block.piston.SuperPistonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.diiiaz.super_pistons.block.piston.SuperPistonBlock.EXTENDED;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {

    @Inject(
            method = "isMovable",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/block/BlockState.isOf (Lnet/minecraft/block/Block;)Z",
                    shift = At.Shift.AFTER
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/block/Blocks;STICKY_PISTON:Lnet/minecraft/block/Block;",
                            shift = At.Shift.AFTER),
                    to = @At(
                            value = "INVOKE",
                            target = "net/minecraft/block/BlockState.getHardness (Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F")
            ),
            cancellable = true)
    private static void isPistonMoveable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof SuperPistonBlock) {
            if (state.get(EXTENDED)) {
                cir.setReturnValue(false);
            }
        }
    }
}
