package com.cometproject.server.network.messages.incoming.room.action;

import com.cometproject.server.game.rooms.objects.entities.RoomEntityStatus;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.room.permissions.FloodFilterMessageComposer;
import com.cometproject.server.protocol.messages.MessageEvent;
import com.cometproject.server.network.sessions.Session;


public class ApplySignMessageEvent implements Event {
    public void handle(Session client, MessageEvent msg) {
        if (client.getPlayer() == null || client.getPlayer().getEntity() == null) return;

        if(!client.getPlayer().getEntity().isVisible()) {
            return;
        }

        final long time = System.currentTimeMillis();

        if (!client.getPlayer().getPermissions().getRank().floodBypass()) {
            if (time - client.getPlayer().getRoomLastMessageTime() < 750) {
                client.getPlayer().setRoomFloodFlag(client.getPlayer().getRoomFloodFlag() + 1);

                if (client.getPlayer().getRoomFloodFlag() >= 3) {
                    client.getPlayer().setRoomFloodTime(client.getPlayer().getPermissions().getRank().floodTime());
                    client.getPlayer().setRoomFloodFlag(0);

                    client.getPlayer().getSession().send(new FloodFilterMessageComposer(client.getPlayer().getRoomFloodTime()));
                }
            } else {
                client.getPlayer().setRoomFloodFlag(0);
            }

            if (client.getPlayer().getRoomFloodTime() >= 1) {
                return;
            }

            client.getPlayer().setRoomLastMessageTime(time);
        }
        
        // / UnIdle the entity
        client.getPlayer().getEntity().unIdle();

        // Add the sign status
        client.getPlayer().getEntity().addStatus(RoomEntityStatus.SIGN, String.valueOf(msg.readInt()));

        // Set the entity to displaying a sign
        client.getPlayer().getEntity().markDisplayingSign();
        client.getPlayer().getEntity().markNeedsUpdate();
    }
}
