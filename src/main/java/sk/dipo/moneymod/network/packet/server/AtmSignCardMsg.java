package sk.dipo.moneymod.network.packet.server;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import sk.dipo.moneymod.capability.capability.ICreditCardInfo;
import sk.dipo.moneymod.capability.provider.CreditCardProvider;
import sk.dipo.moneymod.network.ModPacketHandler;
import sk.dipo.moneymod.network.packet.client.AtmCardSignedMsg;
import sk.dipo.moneymod.network.packet.client.AtmErrorMsg;
import sk.dipo.moneymod.world.AccountWorldSavedData;

import java.util.Objects;
import java.util.function.Supplier;

public class AtmSignCardMsg {

    private final Hand hand;
    private final String pinCode;

    public AtmSignCardMsg(Hand hand, String pinCode) {
        this.hand = hand;
        this.pinCode = pinCode;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeEnumValue(hand);
        buffer.writeString(pinCode, 128);
    }

    public static AtmSignCardMsg decode(PacketBuffer buffer) {
        return new AtmSignCardMsg(buffer.readEnumValue(Hand.class), buffer.readString(128));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final ServerPlayerEntity sender = Objects.requireNonNull(ctx.get().getSender());
            final ICreditCardInfo cap = sender.getHeldItem(this.hand).getCapability(CreditCardProvider.CREDIT_CARD_CAPABILITY).orElseThrow(
                    () -> new NullPointerException("Null CreditCard capability")
            );

            if (cap.hasOwner()) {
                ModPacketHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> sender), new AtmErrorMsg()
                );
                return;
            }

            cap.init(sender.getGameProfile().getId(), Objects.requireNonNull(ctx.get().getSender()).getServerWorld());
            AccountWorldSavedData.get(sender.getServerWorld()).setCardPIN(cap.getCardNumber(), this.pinCode);
            ModPacketHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> ctx.get().getSender()),
                    new AtmCardSignedMsg(true, Objects.requireNonNull(ctx.get().getSender()).getGameProfile().getName())
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
