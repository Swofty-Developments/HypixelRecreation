package net.swofty.pvp.entity.explosion;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class TntEntity extends Entity {
	private final Entity causingEntity;
	
	public TntEntity(@Nullable Entity causingEntity) {
		super(EntityType.TNT);
		this.causingEntity = causingEntity;
		
		double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
		setVelocity(new Vec(-Math.sin(angle) * 0.02, 0.2f, -Math.cos(angle) * 0.02)
				.mul(ServerProperties.SERVER_TICKS_PER_SECOND.get()));
	}
	
	public int getFuse() {
		return ((PrimedTntMeta) getEntityMeta()).getFuseTime();
	}
	
	public void setFuse(int fuse) {
		((PrimedTntMeta) getEntityMeta()).setFuseTime(fuse);
	}
	
	@Override
	public void update(long time) {
		if (onGround) velocity = velocity.mul(0.7, -0.5, 0.7);
		int newFuse = getFuse() - 1;
		setFuse(newFuse);
		if (newFuse <= 0) {
			Instance instance = this.instance;
			Pos position = this.position;
			BoundingBox boundingBox = this.boundingBox;
			
			remove();
			if (instance.getExplosionSupplier() != null) instance.explode(
					(float) position.x(),
					(float) (position.y() + boundingBox.height() * 0.0625),
					(float) position.z(),
					4.0f,
					causingEntity == null ? null
							: CompoundBinaryTag.builder()
							.putString("causingEntity", causingEntity.getUuid().toString())
							.build()
			);
		}
	}
	
	@Override
	public double getEyeHeight() {
		return 0.15;
	}
}
