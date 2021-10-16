package rocks.spaghetti.maintmode;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

import java.net.SocketAddress;

public interface CanJoinCallback {
    Event<CanJoinCallback> EVENT = EventFactory.createArrayBacked(CanJoinCallback.class,
            listeners -> (address, profile) -> {
                for (CanJoinCallback listener : listeners) {
                    ActionResult result = listener.checkCanJoin(address, profile);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult checkCanJoin(SocketAddress address, GameProfile profile);
}
