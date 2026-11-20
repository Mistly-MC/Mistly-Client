package online.mistylauncher.client.mixin;

//? if >=1.21.2 {
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import online.mistylauncher.client.cosmetics.Cosmetic;
import online.mistylauncher.client.cosmetics.HatHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** The Misty hat in the player render state. */
@Mixin(AvatarRenderState.class)
abstract class AvatarRenderStateMixin implements HatHolder {
	@Unique
	private Cosmetic misty$hat = Cosmetic.NONE;

	@Override
	public Cosmetic misty$hat() {
		return this.misty$hat;
	}

	@Override
	public void misty$setHat(final Cosmetic hat) {
		this.misty$hat = hat;
	}
}
//?} else {
/*import org.spongepowered.asm.mixin.Mixin;

// No player render state before 1.21.2: the hat is read from the player (see CloudHatLayer).
// Skipped by MistyMixinPlugin: its target is not in this version.
@Mixin(targets = "net.minecraft.client.renderer.entity.state.PlayerRenderState")
abstract class AvatarRenderStateMixin {
}
*///?}
