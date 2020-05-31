package galacy.galacyhcf.entities;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityEnderPearl;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

public class EnderPearl extends EntityEnderPearl {
    private BlockVector3 last;

    public EnderPearl(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) return false;

        if (last == null) {
            Block collided = this.getLevel().getBlock(getPosition());
            last = collided.asBlockVector3();
            if (collided instanceof BlockFenceGate) {
                if (!((BlockFenceGate) collided).isOpen()) {
                    this.level.addSound(this, Sound.MOB_ENDERMEN_PORTAL);
                    this.close();

                    return false;
                }
            }
        }
        if (last.distanceSquared(asBlockVector3()) >= 1) {
            Block collided = this.getLevel().getBlock(getPosition());
            if (collided.getId() == 90) {
                teleport();
                this.close();

                return false;
            }
            if (collided instanceof BlockFenceGate) {
                if (((BlockFenceGate) collided).isOpen()) {
                    teleport();

                } else {
                    teleport(last);

                }
                this.close();
                return false;
            }

            last = collided.asBlockVector3();
        }

        return super.onUpdate(currentTick);
    }

    private void teleport() {
        this.shootingEntity.teleport(new Vector3((double) NukkitMath.floorDouble(this.x) + 0.5D, this.y - 1, (double) NukkitMath.floorDouble(this.z) + 0.5D), PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
        if ((((Player) this.shootingEntity).getGamemode() & 1) == 0) {
            this.shootingEntity.attack(new EntityDamageByEntityEvent(this, this.shootingEntity, EntityDamageEvent.DamageCause.PROJECTILE, 5.0F, 0.0F));
        }

        this.level.addSound(this, Sound.MOB_ENDERMEN_PORTAL);
    }

    private void teleport(BlockVector3 last) {
        this.shootingEntity.teleport(new Vector3((double) NukkitMath.floorDouble(last.x) + 0.5D, last.y, (double) NukkitMath.floorDouble(last.z) + 0.5D), PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
        if ((((Player) this.shootingEntity).getGamemode() & 1) == 0) {
            this.shootingEntity.attack(new EntityDamageByEntityEvent(this, this.shootingEntity, EntityDamageEvent.DamageCause.PROJECTILE, 5.0F, 0.0F));
        }

        this.level.addSound(this, Sound.MOB_ENDERMEN_PORTAL);
    }
}
