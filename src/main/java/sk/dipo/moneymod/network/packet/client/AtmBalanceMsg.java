package sk.dipo.moneymod.network.packet.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import sk.dipo.moneymod.client.gui.AtmScreen;
import sk.dipo.moneymod.client.gui.widget.AtmBalanceTextComponent;
import sk.dipo.moneymod.client.gui.widget.AtmPinTextComponent;
import sk.dipo.moneymod.client.gui.widget.AtmTextComponent;
import sk.dipo.moneymod.container.ContainerHelper;

import java.util.function.Supplier;

public class AtmBalanceMsg {

    private final int balance;

    public AtmBalanceMsg(int balance) {
        this.balance = balance;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(balance);
    }

    public static AtmBalanceMsg decode(PacketBuffer buffer) {
        return new AtmBalanceMsg(buffer.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final Screen screen = Minecraft.getInstance().currentScreen;
            if (screen instanceof AtmScreen) {
                AtmScreen atmScreen = (AtmScreen) screen;
                atmScreen.keyPadMode = AtmScreen.KeyPadMode.Balance;
                atmScreen.displayPIN.clear();
                atmScreen.displayPIN.setDisplayMode(AtmPinTextComponent.DisplayMode.Balance);
                atmScreen.displayMain = new AtmTextComponent(
                        ContainerHelper.getUnlocalizedText("atm_balance"),
                        new AtmBalanceTextComponent(this.balance)
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
