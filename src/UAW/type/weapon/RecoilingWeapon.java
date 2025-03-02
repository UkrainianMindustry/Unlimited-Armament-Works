package UAW.type.weapon;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;

public class RecoilingWeapon extends UAWWeapon {
	public TextureRegion turret, turretOutlineRegion, turretCell;
	public float turretOffsetX = 0f, turretOffsetY = 0f;


	public RecoilingWeapon(String name) {
		super(name);
		this.name = name;
	}

	@Override
	public void load() {
		super.load();
		turret = Core.atlas.find(name + "-turret");
		turretOutlineRegion = Core.atlas.find(name + "-turret-outline");
		turretCell = Core.atlas.find(name + "-turret-cell");
	}

	public Color cellColor(Unit unit) {
		float f = Mathf.clamp(unit.healthf());
		return Tmp.c1.set(Color.black).lerp(unit.team.color, f + Mathf.absin(Time.time, Math.max(f * 5f, 1f), 1f - f));
	}

	@Override
	public void draw(Unit unit, WeaponMount mount) {
		float rotation = unit.rotation - 90;
		float weaponRotation = rotation + (rotate ? mount.rotation : 0);
		float recoil = -((mount.reload) / reload * this.recoil);
		float weaponX = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, recoil);
		float weaponY = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, recoil);
		float turretX = unit.x + Angles.trnsx(rotation, x + turretOffsetX, y + turretOffsetY) + Angles.trnsy(weaponRotation, 0, 0);
		float turretY = unit.y + Angles.trnsy(rotation, x + turretOffsetX, y + turretOffsetY) + Angles.trnsy(weaponRotation, 0, 0);

		if (shadow > 0) {
			Drawf.shadow(weaponX, weaponY, shadow);
		}

		if (outlineRegion.found() && top) {
			Draw.rect(outlineRegion,
				weaponX, weaponY,
				outlineRegion.width * Draw.scl * -Mathf.sign(flipSprite),
				region.height * Draw.scl,
				weaponRotation);
			Draw.rect(turretOutlineRegion,
				turretX, turretY,
				turretOutlineRegion.width * Draw.scl,
				region.height * Draw.scl,
				weaponRotation);
		}

		Draw.rect(region,
			weaponX, weaponY,
			region.width * Draw.scl * -Mathf.sign(flipSprite),
			region.height * Draw.scl,
			weaponRotation);

		Draw.rect(turret, turretX, turretY, weaponRotation);
		if (turretCell.found()) {
			Draw.color(cellColor(unit));
			Draw.rect(turretCell, turretX, turretY, weaponRotation);
			Draw.reset();
		}

		if (heatRegion.found() && mount.heat > 0) {
			Draw.color(heatColor, mount.heat);
			Draw.blend(Blending.additive);
			Draw.rect(heatRegion,
				weaponX, weaponY,
				heatRegion.width * Draw.scl * -Mathf.sign(flipSprite),
				heatRegion.height * Draw.scl,
				weaponRotation);
			Draw.blend();
			Draw.color();
		}
	}
}

