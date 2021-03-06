/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.restclient.models.api.responses.alarmcallbacks;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author Dennis Oelkers <dennis@torch.sh>
 */
public class AlarmCallbackSummaryResponse {
    public String id;
    @JsonProperty("stream_id")
    public String streamId;
    public String type;
    public Map<String, Object> configuration;
    @JsonProperty("created_at")
    public String createdAt;
    @JsonProperty("creator_user_id")
    public String creatorUserId;
}
