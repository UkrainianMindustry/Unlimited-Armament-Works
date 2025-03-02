package UAW.type.weapon;

import UAW.world.meta.UAWStatValues;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.math.*;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Strings;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.type.*;
import mindustry.world.meta.*;

public class UAWWeapon extends Weapon {
	public UAWWeapon(String name) {
		this.name = name;
	}

	public UAWWeapon() {
		this("");
	}

	@Override
	public void addStats(UnitType u, Table t) {
		if (inaccuracy > 0) {
			t.row();
			t.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (int) inaccuracy + " " + StatUnit.degrees.localized());
		}
		t.row();
		t.add("[lightgray]" + Stat.reload.localized() + ": " + (mirror ? "2x " : "") + "[white]" + Strings.autoFixed(60f / reload * shots, 2) + " " + StatUnit.perSecond.localized());

		UAWStatValues.ammo(ObjectMap.of(u, bullet)).display(t);
	}
}
