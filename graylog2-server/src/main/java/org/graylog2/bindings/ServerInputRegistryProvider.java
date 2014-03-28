/*
 * Copyright 2012-2014 TORCH GmbH
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.graylog2.bindings;

import org.graylog2.inputs.InputService;
import org.graylog2.inputs.ServerInputRegistry;
import org.graylog2.notifications.NotificationService;
import org.graylog2.shared.ServerStatus;
import org.graylog2.shared.buffers.ProcessBuffer;
import org.graylog2.shared.inputs.MessageInputFactory;
import org.graylog2.system.activities.ActivityWriter;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Dennis Oelkers <dennis@torch.sh>
 */
public class ServerInputRegistryProvider implements Provider<ServerInputRegistry> {
    private static ServerInputRegistry serverInputRegistry = null;

    @Inject
    public ServerInputRegistryProvider(MessageInputFactory messageInputFactory,
                                       ProcessBuffer processBuffer,
                                       ServerStatus serverStatus,
                                       ActivityWriter activityWriter,
                                       InputService inputService,
                                       NotificationService notificationService) {
        if (serverInputRegistry == null)
            serverInputRegistry = new ServerInputRegistry(messageInputFactory, processBuffer,
                    serverStatus, activityWriter, inputService, notificationService);
    }

    @Override
    public ServerInputRegistry get() {
        return serverInputRegistry;
    }
}