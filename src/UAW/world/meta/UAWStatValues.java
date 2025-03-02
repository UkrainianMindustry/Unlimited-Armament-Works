package UAW.world.meta;

import UAW.entities.bullet.*;
import arc.Core;
import arc.func.Boolf;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Tex;
import mindustry.maps.Map;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

/**
 * Utilities for displaying certain stats in a table.
 */
public class UAWStatValues {

	public static StatValue string(String value, Object... args) {
		String result = Strings.format(value, args);
		return table -> table.add(result);
	}

	public static StatValue bool(boolean value) {
		return table -> table.add(!value ? "@no" : "@yes");
	}

	public static StatValue number(float value, StatUnit unit) {
		return table -> {
			int precision = Math.abs((int) value - value) <= 0.001f ? 0 : Math.abs((int) (value * 10) - value * 10) <= 0.001f ? 1 : 2;

			table.add(Strings.fixed(value, precision));
			table.add((unit.space ? " " : "") + unit.localized());
		};
	}

	public static StatValue liquid(Liquid liquid, float amount, boolean perSecond) {
		return table -> table.add(new LiquidDisplay(liquid, amount, perSecond));
	}

	public static StatValue liquids(Boolf<Liquid> filter, float amount, boolean perSecond) {
		return table -> {
			Seq<Liquid> list = content.liquids().select(i -> filter.get(i) && i.unlockedNow());

			for (int i = 0; i < list.size; i++) {
				table.add(new LiquidDisplay(list.get(i), amount, perSecond)).padRight(5);

				if (i != list.size - 1) {
					table.add("/");
				}
			}
		};
	}

	public static StatValue items(ItemStack... stacks) {
		return items(true, stacks);
	}

	public static StatValue items(boolean displayName, ItemStack... stacks) {
		return table -> {
			for (ItemStack stack : stacks) {
				table.add(new ItemDisplay(stack.item, stack.amount, displayName)).padRight(5);
			}
		};
	}

	public static StatValue items(float timePeriod, ItemStack... stacks) {
		return table -> {
			for (ItemStack stack : stacks) {
				table.add(new ItemDisplay(stack.item, stack.amount, timePeriod, true)).padRight(5);
			}
		};
	}

	public static StatValue items(Boolf<Item> filter) {
		return items(-1, filter);
	}

	public static StatValue items(float timePeriod, Boolf<Item> filter) {
		return table -> {
			Seq<Item> list = content.items().select(i -> filter.get(i) && i.unlockedNow());

			for (int i = 0; i < list.size; i++) {
				Item item = list.get(i);

				table.add(timePeriod <= 0 ? new ItemDisplay(item) : new ItemDisplay(item, 1, timePeriod, true)).padRight(5);

				if (i != list.size - 1) {
					table.add("/");
				}
			}
		};
	}

	public static StatValue content(UnlockableContent content) {
		return table -> {
			table.add(new Image(content.uiIcon)).size(iconSmall).padRight(3);
			table.add(content.localizedName).padRight(3);
		};
	}

	public static StatValue blockEfficiency(Block floor, float multiplier, boolean startZero) {
		return table -> table.stack(
			new Image(floor.uiIcon).setScaling(Scaling.fit),
			new Table(t -> t.top().right().add((multiplier < 0 ? "[scarlet]" : startZero ? "[accent]" : "[accent]+") + (int) ((multiplier) * 100) + "%").style(Styles.outlineLabel))
		);
	}

	public static StatValue blocks(Attribute attr, boolean floating, float scale, boolean startZero) {
		return blocks(attr, floating, scale, startZero, true);
	}

	public static StatValue blocks(Attribute attr, boolean floating, float scale, boolean startZero, boolean checkFloors) {
		return table -> table.table(c -> {
			Runnable[] rebuild = {null};
			Map[] lastMap = {null};

			rebuild[0] = () -> {
				c.clearChildren();
				c.left();

				if (state.isGame()) {
					var blocks = Vars.content.blocks()
						.select(block -> (!checkFloors || block instanceof Floor) && indexer.isBlockPresent(block) && block.attributes.get(attr) != 0 && !((block instanceof Floor f && f.isDeep()) && !floating))
						.<Floor>as().with(s -> s.sort(f -> f.attributes.get(attr)));

					if (blocks.any()) {
						int i = 0;
						for (var block : blocks) {

							blockEfficiency(block, block.attributes.get(attr) * scale, startZero).display(c);
							if (++i % 5 == 0) {
								c.row();
							}
						}
					} else {
						c.add("@none.inmap");
					}
				} else {
					c.add("@stat.showinmap");
				}
			};

			rebuild[0].run();

			//rebuild when map changes.
			c.update(() -> {
				Map current = state.isGame() ? state.map : null;

				if (current != lastMap[0]) {
					rebuild[0].run();
					lastMap[0] = current;
				}
			});
		});
	}

	public static StatValue blocks(Boolf<Block> pred) {
		return blocks(content.blocks().select(pred));
	}

	public static StatValue blocks(Seq<Block> list) {
		return table -> table.table(l -> {
			l.left();

			for (int i = 0; i < list.size; i++) {
				Block item = list.get(i);

				l.image(item.uiIcon).size(iconSmall).padRight(2).padLeft(2).padTop(3).padBottom(3);
				l.add(item.localizedName).left().padLeft(1).padRight(4);
				if (i % 5 == 4) {
					l.row();
				}
			}
		});
	}

	public static StatValue boosters(float reload, float maxUsed, float multiplier, boolean baseReload, Boolf<Liquid> filter) {
		return table -> {
			table.row();
			table.table(c -> {
				for (Liquid liquid : content.liquids()) {
					if (!filter.get(liquid)) continue;

					c.image(liquid.uiIcon).size(3 * 8).padRight(4).right().top();
					c.add(liquid.localizedName).padRight(10).left().top();
					c.table(Tex.underline, bt -> {
						bt.left().defaults().padRight(3).left();

						float reloadRate = (baseReload ? 1f : 0f) + maxUsed * multiplier * liquid.heatCapacity;
						float standardReload = baseReload ? reload : reload / (maxUsed * multiplier * 0.4f);
						float result = standardReload / (reload / reloadRate);
						bt.add(Core.bundle.format("bullet.reload", Strings.autoFixed(result, 2)));
					}).left().padTop(-9);
					c.row();
				}
			}).colspan(table.getColumns());
			table.row();
		};
	}

	public static StatValue strengthBoosters(float multiplier, Boolf<Liquid> filter) {
		return table -> {
			table.row();
			table.table(c -> {
				for (Liquid liquid : content.liquids()) {
					if (!filter.get(liquid)) continue;

					c.image(liquid.uiIcon).size(3 * 8).padRight(4).right().top();
					c.add(liquid.localizedName).padRight(10).left().top();
					c.table(Tex.underline, bt -> {
						bt.left().defaults().padRight(3).left();

						float newRate = (1f + multiplier * liquid.heatCapacity);
						bt.add(Core.bundle.format("bar.strength", Strings.autoFixed(newRate, 2)));
					}).left().padTop(-9);
					c.row();
				}
			}).colspan(table.getColumns());
			table.row();
		};
	}

	public static StatValue weapons(UnitType unit, Seq<Weapon> weapons) {
		return table -> {
			table.row();
			for (int i = 0; i < weapons.size; i++) {
				Weapon weapon = weapons.get(i);

				if (weapon.flipSprite) {
					//flipped weapons are not given stats
					continue;
				}

				TextureRegion region = !weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : unit.fullIcon;

				table.image(region).size(60).scaling(Scaling.bounded).right().top();

				table.table(Tex.underline, w -> {
					w.left().defaults().padRight(3).left();

					weapon.addStats(unit, w);
				}).padTop(-9).left();
				table.row();
			}
		};
	}

	public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map) {
		return ammo(map, 0);
	}

	public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, int indent) {
		return table -> {

			table.row();

			var orderedKeys = map.keys().toSeq();
			orderedKeys.sort();

			for (T t : orderedKeys) {
				boolean compact = t instanceof UnitType || indent > 0;

				BulletType type = map.get(t);

				//no point in displaying unit icon twice
				if (!compact && !(t instanceof PowerTurret)) {
					table.image(icon(t)).size(3 * 8).padRight(4).right().top();
					table.add(t.localizedName).padRight(10).left().top();
				}

				table.table(bt -> {
					bt.left().defaults().padRight(3).left();

					if (type.damage > 0 && (type.collides || type.splashDamage <= 0)) {
						if (type.continuousDamage() > 0) {
							bt.add(Core.bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized());
						} else {
							bt.add(Core.bundle.format("bullet.damage", type.damage));
						}
					}

					if (type.buildingDamageMultiplier != 1) {
						sep(bt, Core.bundle.format("bullet.buildingdamage", (int) (type.buildingDamageMultiplier * 100)));
					}

					if (type.splashDamage > 0) {
						sep(bt, Core.bundle.format("bullet.splashdamage", (int) type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
					}

					if (type instanceof UAWBulletType types) {
						if (types.armorIgnoreScl > 0) {
							sep(bt, Core.bundle.format("bullet.uaw-armorPenetration", (int) (types.armorIgnoreScl * 100)));
						}
						if (types.shieldDamageMultiplier != 1) {
							sep(bt, Core.bundle.format("bullet.uaw-shieldDamageMultiplier", (int) (types.shieldDamageMultiplier * 100)));
						}
					}

					if (type instanceof MineBulletType types) {
						if (types.lifetime != 1) {
							sep(bt, Core.bundle.format("bullet.uaw-mineLifetime", (int) (types.lifetime / 60)));
						}
						if (types.explodeRange != 1) {
							sep(bt, Core.bundle.format("bullet.uaw-explodeRange", (int) (types.explodeRange / tilesize)));
						}
						if (types.explodeDelay != 1) {
							sep(bt, Core.bundle.format("bullet.uaw-explodeDelay", Strings.fixed(types.explodeDelay / 60, 2)));
						}
					}

					if (!compact && !Mathf.equal(type.ammoMultiplier, 1f) && type.displayAmmoMultiplier) {
						sep(bt, Core.bundle.format("bullet.multiplier", (int) type.ammoMultiplier));
					}

					if (!compact && !Mathf.equal(type.reloadMultiplier, 1f)) {
						sep(bt, Core.bundle.format("bullet.reload", Strings.autoFixed(type.reloadMultiplier, 2)));
					}

					if (type.knockback > 0) {
						sep(bt, Core.bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)));
					}

					if (type.healPercent > 0f) {
						sep(bt, Core.bundle.format("bullet.healpercent", Strings.autoFixed(type.healPercent, 2)));
					}

					if (type.pierce || type.pierceCap != -1) {
						sep(bt, type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
					}

					if (type.incendAmount > 0) {
						sep(bt, "@bullet.incendiary");
					}

					if (type.homingPower > 0.01f) {
						sep(bt, "@bullet.homing");
					}

					if (type.lightning > 0) {
						sep(bt, Core.bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
					}

					if (type.status != StatusEffects.none) {
						sep(bt, (type.status.minfo.mod == null ? type.status.emoji() : "") + "[stat]" + type.status.localizedName);
					}

					if (type instanceof MineCanisterBulletType types) {
						if (types.fragBullet != null) {
							sep(bt, Core.bundle.format("bullet.uaw-mineCount", types.fragBullets));
							bt.row();

							ammo(ObjectMap.of(t, types.fragBullet), indent + 1).display(bt);
						}
					}

					if (type.fragBullet != null && !(type instanceof MineCanisterBulletType)) {
						sep(bt, Core.bundle.format("bullet.frags", type.fragBullets));
						bt.row();

						ammo(ObjectMap.of(t, type.fragBullet), indent + 1).display(bt);
					}
				}).padTop(compact ? 0 : -9).padLeft(indent * 8).left().get().background(compact ? null : Tex.underline);

				table.row();
			}
		};
	}

	//for AmmoListValue
	private static void sep(Table table, String text) {
		table.row();
		table.add(text);
	}

	private static TextureRegion icon(UnlockableContent t) {
		return t.uiIcon;
	}
}

