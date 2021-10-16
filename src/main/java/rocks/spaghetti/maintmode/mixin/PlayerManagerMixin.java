package rocks.spaghetti.maintmode.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rocks.spaghetti.maintmode.CanJoinCallback;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
    private void onCheckCanJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> info) {
        ActionResult result = CanJoinCallback.EVENT.invoker().checkCanJoin(address, profile);

        if (result == ActionResult.FAIL) {
            info.setReturnValue(new LiteralText("Server is currently undergoing maintenance."));
        }
    }
}
