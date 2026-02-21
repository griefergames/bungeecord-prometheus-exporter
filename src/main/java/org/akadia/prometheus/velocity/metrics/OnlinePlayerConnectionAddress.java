package org.akadia.prometheus.velocity.metrics;

import com.velocitypowered.api.proxy.ProxyServer;
import org.akadia.prometheus.PrometheusExporter;
import org.akadia.prometheus.interfaces.GauageMetric;
import org.akadia.prometheus.velocity.PrometheusVelocityExporter;

import java.net.InetSocketAddress;
import java.util.*;

public class OnlinePlayerConnectionAddress extends GauageMetric {

    public OnlinePlayerConnectionAddress(PrometheusExporter plugin) {
        super(plugin);
    }

    public void doCollect() {
        this.getGauge().clear();

        Map<String, Set<String>> playerAddresses = new HashMap<String, Set<String>>();

        ProxyServer proxy = ((PrometheusVelocityExporter) getPlugin()).getProxyServer();
        proxy.getAllPlayers().forEach(player -> {
            Optional<InetSocketAddress> virtualHost = player.getVirtualHost();

            String address = virtualHost.isPresent() ? virtualHost.get().getHostName() : "unknown";
            if(!playerAddresses.containsKey(address)) {
                playerAddresses.put(address, new HashSet<>());
            }
            Set<String> players = playerAddresses.get(address);
            players.add(player.getUsername());
            playerAddresses.put(address, players);
        });

        for(Map.Entry<String, Set<String>> addressEntry : playerAddresses.entrySet()) {
            String address = addressEntry.getKey();
            for(String player : addressEntry.getValue()) {
                this.getGauge().labels(address, player).set(1);
            }
            this.getGauge().labels(address, "").set(addressEntry.getValue().size());
        }
    }

    public String getConfigKey() {
        return "online_player_connection_adress";
    }

    public String getHelp() {
        return "the connection adress of the online player in Velocity";
    }

    public String[] getLabels() {
        return new String[]{"address", "player"};
    }
}
