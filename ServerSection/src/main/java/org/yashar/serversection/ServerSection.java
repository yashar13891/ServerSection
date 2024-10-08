package org.yashar.serversection;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class ServerSection extends JavaPlugin {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "playerbalancer:main");
        registerCommands();
    }

    private void registerCommands() {
        getCommand("lobbyselector").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                openLobbySelector(player);
            } else {
                sender.sendMessage("this command only use for players");
            }
            return true;
        });
    }

    private void openLobbySelector(Player player) {
        Map<String, Object> lobbies = config.getConfigurationSection("lobbies").getValues(false);
        new Gui(player, 3, "Lobby Selector") {
            @Override
            public void redraw() {
                int slot = 0;
                for (String lobby : lobbies.keySet()) {
                    String displayName = config.getString("lobbies." + lobby + ".name");
                    Material material = Material.valueOf(config.getString("lobbies." + lobby + ".material"));

                    Item item = ItemStackBuilder.of(material)
                            .name(displayName)
                            .build(() -> {
                                connectPlayerToLobby(player, lobby);
                                player.closeInventory();
                            });
                    setItem(slot++, item);
                }
            }
        }.open();
    }

    private void connectPlayerToLobby(Player player, String lobbySection) {
        player.sendMessage("Connecting to server: " + lobbySection);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(lobbySection);


        player.sendPluginMessage(this, "playerbalancer:main", out.toByteArray());
    }

}
