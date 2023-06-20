package me.edoren.skin_changer.server.permission;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PermissionController {
    private final HashMap<String, PermissionNode<?>> nodesMap = new HashMap<>();
    Boolean isRegistered = false;

    public PermissionController() {
        var defaultTrue = (PermissionNode.PermissionResolver<Boolean>) (player, playerUUID, context) -> true;
        var defaultFalse = (PermissionNode.PermissionResolver<Boolean>) (player, playerUUID, context) -> false;

        List<PermissionNode<Boolean>> nodes = Arrays.asList(
                new PermissionNode<>("skin_changer", "skin", PermissionTypes.BOOLEAN, defaultTrue),
                new PermissionNode<>("skin_changer", "skin.set", PermissionTypes.BOOLEAN, defaultTrue),
                new PermissionNode<>("skin_changer", "skin.clear", PermissionTypes.BOOLEAN, defaultTrue),

                new PermissionNode<>("skin_changer", "cape", PermissionTypes.BOOLEAN, defaultTrue),
                new PermissionNode<>("skin_changer", "cape.set", PermissionTypes.BOOLEAN, defaultTrue),
                new PermissionNode<>("skin_changer", "cape.clear", PermissionTypes.BOOLEAN, defaultTrue),

                new PermissionNode<>("skin_changer", "admin", PermissionTypes.BOOLEAN, defaultFalse)
        );

        for (PermissionNode<Boolean> node : nodes) {
            this.nodesMap.put(node.getNodeName(), node);
        }
    }

    public void initialize() {
        if (!isRegistered) {
            MinecraftForge.EVENT_BUS.addListener((PermissionGatherEvent.Nodes event) -> {
                event.addNodes(this.nodesMap.values());
            });
        }
        this.isRegistered = true;
    }

    @SuppressWarnings("unchecked")
    public PermissionNode<Boolean> getNode(String nodeName) {
        return (PermissionNode<Boolean>) nodesMap.get("skin_changer." + nodeName);
    }
}
