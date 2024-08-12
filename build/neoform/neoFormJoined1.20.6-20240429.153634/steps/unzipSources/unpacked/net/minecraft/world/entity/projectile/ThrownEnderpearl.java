package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEnderpearl extends ThrowableItemProjectile {
    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> p_37491_, Level p_37492_) {
        super(p_37491_, p_37492_);
    }

    public ThrownEnderpearl(Level p_37499_, LivingEntity p_37500_) {
        super(EntityType.ENDER_PEARL, p_37500_, p_37499_);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37502_) {
        super.onHitEntity(p_37502_);
        p_37502_.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult p_37504_) {
        super.onHit(p_37504_);

        for (int i = 0; i < 32; i++) {
            this.level()
                .addParticle(
                    ParticleTypes.PORTAL,
                    this.getX(),
                    this.getY() + this.random.nextDouble() * 2.0,
                    this.getZ(),
                    this.random.nextGaussian(),
                    0.0,
                    this.random.nextGaussian()
                );
        }

        if (!this.level().isClientSide && !this.isRemoved()) {
            Entity entity = this.getOwner();
            if (entity instanceof ServerPlayer serverplayer) {
                if (serverplayer.connection.isAcceptingMessages() && serverplayer.level() == this.level() && !serverplayer.isSleeping()) {
                    net.neoforged.neoforge.event.entity.EntityTeleportEvent.EnderPearl event = net.neoforged.neoforge.event.EventHooks.onEnderPearlLand(serverplayer, this.getX(), this.getY(), this.getZ(), this, 5.0F, p_37504_);
                    if (!event.isCanceled()) { // Don't indent to lower patch size
                    if (this.random.nextFloat() < 0.05F && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        Endermite endermite = EntityType.ENDERMITE.create(this.level());
                        if (endermite != null) {
                            endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                            this.level().addFreshEntity(endermite);
                        }
                    }

                    if (entity.isPassenger()) {
                        serverplayer.dismountTo(this.getX(), this.getY(), this.getZ());
                    } else {
                        entity.teleportTo(this.getX(), this.getY(), this.getZ());
                    }

                    entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
                    entity.resetFallDistance();
                    entity.hurt(this.damageSources().fall(), event.getAttackDamage());
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
                    } //Forge: End
                }
            } else if (entity != null) {
                entity.teleportTo(this.getX(), this.getY(), this.getZ());
                entity.resetFallDistance();
            }

            this.discard();
        }
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();
        if (entity instanceof ServerPlayer && !entity.isAlive() && this.level().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard();
        } else {
            super.tick();
        }
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel p_37506_, net.neoforged.neoforge.common.util.ITeleporter teleporter) {
        Entity entity = this.getOwner();
        if (entity != null && entity.level().dimension() != p_37506_.dimension()) {
            this.setOwner(null);
        }

        return super.changeDimension(p_37506_, teleporter);
    }
}
