package online.mistylauncher.client.features;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.util.Mth;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Motion blur: each frame of the world is mixed with the frames before it, so what moves on the
 * screen (turning the camera, running) leaves a short trail. The HUD and the screens are drawn
 * after it and stay sharp.
 *
 * <p>A post effect of the game ({@code assets/misty/post_effect/motion_blur_<level>.json}): it keeps
 * the last image in a persistent target and mixes the new one with it. One file per strength,
 * since a post effect's values are fixed in its file.
 */
public final class MotionBlur {
	private static final Logger LOG = LoggerFactory.getLogger("misty-client");
	/** Strengths of the slider: 1 (10 % of the last image kept) to 9 (90 %). */
	public static final int MIN = 1;
	public static final int MAX = 9;

	/** The effect failed to load or to run: not tried again until it is turned on again. */
	private static boolean failed;

	private MotionBlur() {
	}

	/** Called by the game renderer once the world of this frame is drawn, before the HUD. */
	public static void apply(final GraphicsResourceAllocator resources) {
		Minecraft minecraft = Minecraft.getInstance();
		MistyConfig config = MistyClient.config();
		if (!config.motionBlur || failed || minecraft.level == null) {
			return;
		}
		try {
			PostChain chain = minecraft.getShaderManager().getPostChain(MistyClient.id("motion_blur_" + strength(config)), LevelTargetBundle.MAIN_TARGETS);
			if (chain != null) {
				chain.process(mainTarget(minecraft), resources);
			}
		} catch (RuntimeException e) {
			failed = true;
			LOG.warn("Misty Client: motion blur turned off, the effect does not work here", e);
		}
	}

	/** Turned on again in the settings: the effect is tried again, even if it failed before. */
	public static void retry() {
		failed = false;
	}

	public static int strength(final MistyConfig config) {
		return Mth.clamp(config.motionBlurStrength, MIN, MAX);
	}

	private static RenderTarget mainTarget(final Minecraft minecraft) {
		//? if >=26.2 {
		return minecraft.gameRenderer.mainRenderTarget();
		//?} else {
		/*return minecraft.getMainRenderTarget();
		*///?}
	}
}
