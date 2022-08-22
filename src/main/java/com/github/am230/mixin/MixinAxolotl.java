package com.github.am230.mixin;

import net.minecraft.entity.AngledModelEntity;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AxolotlEntity.class)
public abstract class MixinAxolotl extends AnimalEntity implements AngledModelEntity, Bucketable {


    protected MixinAxolotl(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * @author am230
     * @reason
     */
    @Overwrite
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        return Bucketable.tryBucket(player, hand, this).orElseGet(() -> {
            if (player.shouldCancelInteraction()) {
                return ActionResult.PASS;
            } else {
                if (!this.world.isClient) {
                    return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
                } else {
                    return ActionResult.SUCCESS;
                }
            }
        });
    }

    /**
     * @author am230
     * @reason
     */
    @Overwrite
    public void travel(Vec3d movementInput) {
        if (this.isTouchingWater()) {
            if (this.hasPlayerRider()) {
                this.getPassengerList().stream().filter(it -> it instanceof PlayerEntity).findFirst().ifPresent(player -> {
                    this.setYaw(player.getYaw());
                    this.setPitch(player.getPitch());

                    this.setVelocity(this.getVelocity().add(Vec3d.fromPolar(player.getPitch(), player.getYaw()).multiply(this.getMovementSpeed())));
                    this.move(MovementType.SELF, this.getVelocity());
                    this.setVelocity(this.getVelocity().multiply(0.9));
                });
            } else if (this.canMoveVoluntarily()) {
                this.updateVelocity(this.getMovementSpeed(), movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.9));
            }
        } else {
            super.travel(movementInput);
        }

    }

    @Override
    public boolean canBeRiddenInWater() {
        return true;
    }
}
