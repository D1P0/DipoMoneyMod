package sk.dipo.moneymod.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import sk.dipo.moneymod.client.gui.AtmScreen;
import sk.dipo.moneymod.client.gui.widget.AtmTextComponent;
import sk.dipo.moneymod.container.ContainerHelper;

import java.util.function.Supplier;

public class AtmCardSignedMsg {

    private final boolean cardSigned;
    private final String owner;

    public AtmCardSignedMsg(boolean cardSigned, String owner) {
        this.cardSigned = cardSigned;
        this.owner = owner;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeBoolean(cardSigned);
        buffer.writeString(owner);
    }

    public static AtmCardSignedMsg decode(PacketBuffer buffer) {
        return new AtmCardSignedMsg(buffer.readBoolean(), buffer.readString());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final Screen screen = Minecraft.getInstance().currentScreen;
            if (screen instanceof AtmScreen) {
                AtmScreen atmScreen = (AtmScreen) screen;
                atmScreen.keyPadMode = cardSigned ? AtmScreen.KeyPadMode.Login : AtmScreen.KeyPadMode.SetPin;
                atmScreen.displayPIN.clear();
                if (cardSigned)
                    atmScreen.displayMain = new AtmTextComponent(
                            ContainerHelper.getUnlocalizedText("atm_login"),
                            this.owner
                    );
                else
                    atmScreen.displayMain = new AtmTextComponent(
                            ContainerHelper.getUnlocalizedText("card_not_signed")
                    );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
