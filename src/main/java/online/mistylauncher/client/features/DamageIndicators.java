package online.mistylauncher.client.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;

/**
 * Damage indicators: the damage you deal ("-3.5"), in red in the air over what you hit, rising
 * and fading in a second. The client knows the health of what it sees (the server sends it), so
 * what a player or a mob you just hit loses in the next moments is your damage. Players and mobs
 * are two settings.
 */
public final class DamageIndicators {
	/** A loss of health this long after your hit is yours. */
	private static final long WATCH_MS = 1500;
	private static final long SHOWN_MS = 1000;
	private static final float RISE = 0.7F;

	/** What you hit, by entity id, with its health then. */
	private record Watched(LivingEntity entity, float health, long hitAt) {
	}

	private record Shown(Vec3 pos, String text, long at) {
	}

	private static final Map<Integer, Watched> WATCHED = new HashMap<>();
	private static final List<Shown> SHOWN = new ArrayList<>();

	private DamageIndicators() {
	}

	private static float health(final LivingEntity entity) {
		return entity.getHealth() + entity.getAbsorptionAmount();
	}

	private static boolean wanted(final Entity entity) {
		MistyConfig config = MistyClient.config();
		return config.damageIndicators && (entity instanceof Player ? config.damagePlayers : config.damageMobs);
	}

	/** The local player attacks {@code entity} (called before the attack is sent). */
	public static void attacked(final Player player, final Entity entity) {
		if (entity instanceof LivingEntity living && entity != player && wanted(entity)) {
			Watched before = WATCHED.get(entity.getId());
			// hit again while watched: keep the health known, only the time moves
			float health = before != null ? before.health() : health(living);
			WATCHED.put(entity.getId(), new Watched(living, health, Util.getMillis()));
		}
	}

	/** Each client tick: what you hit lost health → an indicator. */
	public static void tick(final Minecraft minecraft) {
		long now = Util.getMillis();
		if (minecraft.level == null) {
			WATCHED.clear();
			SHOWN.clear();
			return;
		}
		Iterator<Map.Entry<Integer, Watched>> it = WATCHED.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Watched> entry = it.next();
			Watched watched = entry.getValue();
			LivingEntity entity = watched.entity();
			float health = health(entity);
			if (health < watched.health() - 0.01F) {
				Vec3 pos = entity.position().add(0.0, entity.getBbHeight() + 0.35, 0.0);
				SHOWN.add(new Shown(pos, String.format(Locale.ROOT, "-%.1f", watched.health() - health), now));
				entry.setValue(new Watched(entity, health, watched.hitAt()));
			} else if (health > watched.health()) {
				entry.setValue(new Watched(entity, health, watched.hitAt()));
			}
			if (now - watched.hitAt() > WATCH_MS || entity.isRemoved()) {
				it.remove();
			}
		}
		SHOWN.removeIf(shown -> now - shown.at() > SHOWN_MS);
	}

	/** While the game collects what it draws in the world: the indicators. */
	public static void emit() {
		if (SHOWN.isEmpty()) {
			return;
		}
		long now = Util.getMillis();
		for (Shown shown : SHOWN) {
			float age = Math.min(1.0F, (now - shown.at()) / (float)SHOWN_MS);
			int alpha = Math.round(255 * (1.0F - age * age));
			int color = alpha << 24 | 0xFF4A4A;
			Gizmos.billboardText(shown.text(), shown.pos().add(0.0, age * RISE, 0.0), new TextGizmo.Style(color, 0.4F, java.util.OptionalDouble.empty()));
		}
	}
}
