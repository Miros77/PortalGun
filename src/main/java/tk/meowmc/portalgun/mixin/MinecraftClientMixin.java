package tk.meowmc.portalgun.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tk.meowmc.portalgun.misc.MinecraftClientMethods;

import static tk.meowmc.portalgun.Portalgun.PORTALGUN;

@Mixin(value = MinecraftClient.class, priority = 1500)
public abstract class MinecraftClientMixin {

    @Shadow
    public ClientWorld world;
    @Shadow
    public HitResult crosshairTarget;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Shadow
    public int attackCooldown;

    public MinecraftClientMixin() {
    }

    @Shadow
    protected abstract void doItemPick();

    @Inject(
            method = {"handleBlockBreaking"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
            )},
            cancellable = true
    )
    private void onHandleBlockBreaking(boolean isKeyPressed, CallbackInfo ci) {
        if (MinecraftClientMethods.isPointingToPortal()) {
            MinecraftClientMethods.myHandleBlockBreaking(isKeyPressed);
            ci.cancel();
        }

    }

    @Inject(
            method = {"doAttack"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void onDoAttack(CallbackInfo ci) {
        if (this.attackCooldown <= 0 && MinecraftClientMethods.isPointingToPortal() && !player.isHolding(PORTALGUN)) {
            MinecraftClientMethods.myAttackBlock();
        } else {
            MinecraftClientMethods.doAttack();
        }
        ci.cancel();

    }

    @Inject(
            method = {"doItemUse"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"
            )},
            cancellable = true
    )
    private void onDoItemUse(CallbackInfo ci) {
        if (MinecraftClientMethods.isPointingToPortal()) {
            MinecraftClientMethods.myItemUse(Hand.MAIN_HAND);
            ci.cancel();
        }

    }

}
