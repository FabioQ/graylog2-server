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

package org.graylog2.alarmcallbacks;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.mail.EmailException;
import org.graylog2.alerts.AlertSender;
import org.graylog2.alerts.FormattedEmailAlertSender;
import org.graylog2.notifications.Notification;
import org.graylog2.notifications.NotificationImpl;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.transports.TransportConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.system.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Dennis Oelkers <dennis@torch.sh>
 */
public class EmailAlarmCallback implements AlarmCallback {
    private final Logger LOG = LoggerFactory.getLogger(EmailAlarmCallback.class);
    private final AlertSender alertSender;
    private final NotificationService notificationService;
    private final NodeId nodeId;
    private Configuration configuration;

    @Inject
    public EmailAlarmCallback(AlertSender alertSender,
                              NotificationService notificationService,
                              NodeId nodeId) {
        this.alertSender = alertSender;
        this.notificationService = notificationService;
        this.nodeId = nodeId;
    }

    public void call(Stream stream, AlertCondition.CheckResult result) {
        // Send alerts.
        AlertCondition alertCondition = result.getTriggeredCondition();
        if (stream.getAlertReceivers().size() > 0) {
            try {
                List<Message> backlog = Lists.newArrayList();

                if (alertCondition.getBacklog() > 0 && alertCondition.getSearchHits() != null) {
                    for (Message searchHit : alertCondition.getSearchHits()) {
                        backlog.add(searchHit);
                    }

                    // Read as many messages as possible (max: backlog size) from backlog.
                    // TODO: I think this is not doing anything. (do)
                    int readTo = alertCondition.getBacklog();
                    if(backlog.size() < readTo) {
                        readTo = backlog.size();
                    }
                    backlog = backlog.subList(0, readTo);
                }

                alertSender.sendEmails(stream, result, backlog);
                notificationService.fixed(Notification.Type.EMAIL_TRANSPORT_CONFIGURATION_INVALID, nodeId.toString());
                notificationService.fixed(Notification.Type.EMAIL_TRANSPORT_FAILED, nodeId.toString());
            } catch (Exception e) {
                Notification notification = notificationService.buildNow()
                        .addNode(nodeId.toString())
                        .addSeverity(Notification.Severity.URGENT)
                        .addDetail("stream_id", stream.getId())
                        .addDetail("exception", e.toString());

                if (e instanceof TransportConfigurationException)
                    notification.addType(Notification.Type.EMAIL_TRANSPORT_CONFIGURATION_INVALID);
                else
                    notification.addType(Notification.Type.EMAIL_TRANSPORT_FAILED);

                notificationService.publishIfFirst(notification);
                LOG.warn("Stream [{}] has alert receivers and is triggered, but email transport is not or improperly configured.", stream);
            }
        }
    }

    @Override
    public void initialize(Configuration config) throws AlarmCallbackConfigurationException {
        this.configuration = config;
        this.alertSender.initialize(configuration);
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        ConfigurationRequest configurationRequest = new ConfigurationRequest();
        configurationRequest.addField(new TextField("sender",
                "Sender",
                "graylog2@example.org",
                "The sender of sent out mail alerts",
                ConfigurationField.Optional.NOT_OPTIONAL));

        configurationRequest.addField(new TextField("subject",
                "E-Mail Subject",
                "Graylog2 alert for stream: ${stream.title}",
                "The subject of sent out mail alerts",
                ConfigurationField.Optional.NOT_OPTIONAL));

        configurationRequest.addField(new TextField("body",
                "E-Mail Body",
                FormattedEmailAlertSender.bodyTemplate,
                "The template to generate the body from",
                ConfigurationField.Optional.OPTIONAL,
                TextField.Attribute.TEXTAREA));

        return configurationRequest;
    }

    @Override
    public String getName() {
        return "Email Alerting Callback";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return configuration.getSource();
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {
        if (configuration.getString("sender") == null || configuration.getString("sender").isEmpty()
                || configuration.getString("subject") == null || configuration.getString("subject").isEmpty())
            throw new ConfigurationException("Sender or subject are missing or invalid!");
    }
}
