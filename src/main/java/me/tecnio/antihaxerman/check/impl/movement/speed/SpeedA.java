/*
 *  Copyright (C) 2020 - 2021 Tecnio
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package me.tecnio.antihaxerman.check.impl.movement.speed;

import me.tecnio.antihaxerman.check.Check;
import me.tecnio.antihaxerman.check.api.CheckInfo;
import me.tecnio.antihaxerman.data.PlayerData;
import me.tecnio.antihaxerman.exempt.type.ExemptType;
import me.tecnio.antihaxerman.packet.Packet;
import me.tecnio.antihaxerman.util.BlockUtil;
import me.tecnio.antihaxerman.util.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed", type = "A", description = "Detects speed cheats based on friction.")
public final class SpeedA extends Check {

    private double blockSlipperiness = 0.91;
    private double lastHorizontalDistance = 0.0;

    public SpeedA(final PlayerData data) {
        super(data);
    }

    @Override
    public void handle(final Packet packet) {
        if (packet.isFlying()) {
            final Player player = data.getPlayer();

            final double deltaY = data.getPositionProcessor().getDeltaY();

            double blockSlipperiness = this.blockSlipperiness;
            double attributeSpeed = 1.d;

            final boolean onGround = data.getPositionProcessor().isOnGround();
            final boolean sprinting = data.getActionProcessor().isSprinting();

            final boolean exempt = this.isExempt(ExemptType.TPS, ExemptType.TELEPORT, ExemptType.PISTON,
                    ExemptType.FLYING, ExemptType.UNDERBLOCK, ExemptType.VEHICLE, ExemptType.CLIMBABLE,
                    ExemptType.LIQUID, ExemptType.SLIME, ExemptType.CHUNK);

            final int modifierJump = PlayerUtil.getPotionLevel(player, PotionEffectType.JUMP);

            attributeSpeed += PlayerUtil.getPotionLevel(player, PotionEffectType.SPEED) * (float) 0.2 * attributeSpeed;
            attributeSpeed += PlayerUtil.getPotionLevel(player, PotionEffectType.SLOW) * (float) -.15 * attributeSpeed;

            if (onGround) {
                blockSlipperiness *= 0.91f;

                if (sprinting) attributeSpeed *= 1.3;
                attributeSpeed *= 0.16277136 / Math.pow(blockSlipperiness, 3);

                if (deltaY > 0.4199 + modifierJump * 0.1 && sprinting) {
                    attributeSpeed += 0.2;
                }
            } else {
                attributeSpeed = sprinting ? 0.026 : 0.02;

                blockSlipperiness = 0.91f;
            }

            final boolean takingVelocity = data.getVelocityProcessor().isTakingVelocity();
            final double velocityXZ = data.getVelocityProcessor().getVelocityXZ();

            if (takingVelocity) attributeSpeed += velocityXZ + 0.15;

            final double horizontalDistance = data.getPositionProcessor().getDeltaXZ();
            final double movementSpeed = (horizontalDistance - lastHorizontalDistance) / attributeSpeed;

            if (movementSpeed > 1.0 && !exempt) {
                increaseBufferBy(8);

                if (getBuffer() > 50) {
                    fail();

                    multiplyBuffer(0.5);
                }
            } else {
                decreaseBufferBy(5);
            }

            final double x = data.getPositionProcessor().getX();
            final double y = data.getPositionProcessor().getY();
            final double z = data.getPositionProcessor().getZ();

            final Location blockLocation = new Location(data.getPlayer().getWorld(), x, Math.floor(y - 0.1), z);

            this.blockSlipperiness = BlockUtil.getBlockFriction(blockLocation);
            this.lastHorizontalDistance = horizontalDistance * blockSlipperiness;
        }
    }
}
