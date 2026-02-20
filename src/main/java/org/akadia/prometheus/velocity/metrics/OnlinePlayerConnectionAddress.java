package org.akadia.prometheus.velocity.metrics;

import com.velocitypowered.api.proxy.ProxyServer;
import org.akadia.prometheus.PrometheusExporter;
import org.akadia.prometheus.interfaces.GauageMetric;
import org.akadia.prometheus.velocity.PrometheusVelocityExporter;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OnlinePlayerConnectionAddress extends GauageMetric {

    public OnlinePlayerConnectionAddress(PrometheusExporter plugin) {
        super(plugin);
    }

    public void doCollect() {
        this.getGauge().clear();

        Map<String, List<String>> playerAddresses = new HashMap<String, List<String>>();

        ProxyServer proxy = ((PrometheusVelocityExporter) getPlugin()).getProxyServer();
        proxy.getAllPlayers().forEach(player -> {
            Optional<InetSocketAddress> virtualHost = player.getVirtualHost();

            String address = virtualHost.isPresent() ? virtualHost.get().getHostName() : "unknown";
            if(!playerAddresses.containsKey(address)) {
                playerAddresses.put(address, List.of(player.getUsername()));
            } else {
                List<String> players = playerAddresses.get(address);
                players.add(player.getUsername());
                playerAddresses.put(address, players);
            }
        });

        for(Map.Entry<String, List<String>> addressEntry : playerAddresses.entrySet()) {
            String address = addressEntry.getKey();
            this.getGauge().labels(address, "").set(addressEntry.getValue().size());
            for(String player : addressEntry.getValue()) {
                this.getGauge().labels(address, player).set(1);
            }
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
