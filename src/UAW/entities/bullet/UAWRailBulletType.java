package UAW.entities.bullet;

import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.*;
import mindustry.gen.*;

public class UAWRailBulletType extends UAWBulletType {
	static float furthest = 0;
	public Effect pierceEffect = Fx.hitBulletSmall, updateEffect = Fx.none;
	public float pierceDamageFactor = 1f;
	public float length = 100f;
	public float updateEffectSeg = 20f;

	public UAWRailBulletType() {
		speed = 0f;
		pierceBuilding = true;
		pierce = true;
		reflectable = false;
		hitEffect = Fx.none;
		despawnEffect = Fx.none;
		collides = false;
		lifetime = 1f;
	}

	@Override
	public float range() {
		return length;
	}

	void handle(Bullet b, Posc pos, float initialHealth) {
		float sub = Math.max(initialHealth * pierceDamageFactor, 0);

		if (b.damage <= 0) {
			b.fdata = Math.min(b.fdata, b.dst(pos));
			return;
		}

		if (b.damage > 0) {
			pierceEffect.at(pos.getX(), pos.getY(), b.rotation());
			hitEffect.at(pos.getX(), pos.getY());
		}

		b.damage -= Math.min(b.damage, sub);

		if (b.damage <= 0f) {
			furthest = Math.min(furthest, b.dst(pos));
		}
	}

	@Override
	public void init(Bullet b) {
		super.init(b);

		b.fdata = length;
		furthest = length;
		Damage.collideLine(b, b.team, b.type.hitEffect, b.x, b.y, b.rotation(), length, false, false);
		float resultLen = furthest;

		Vec2 nor = Tmp.v1.trns(b.rotation(), 1f).nor();
		for (float i = 0; i <= resultLen; i += updateEffectSeg) {
			updateEffect.at(b.x + nor.x * i, b.y + nor.y * i, b.rotation());
		}
	}

	@Override
	public boolean testCollision(Bullet bullet, Building tile) {
		return bullet.team != tile.team;
	}

	@Override
	public void hitEntity(Bullet b, Hitboxc entity, float health) {
		super.hitEntity(b, entity, health);
		handle(b, entity, health);
	}

	@Override
	public void hitTile(Bullet b, Building build, float initialHealth, boolean direct) {
		handle(b, build, initialHealth);
	}
}

