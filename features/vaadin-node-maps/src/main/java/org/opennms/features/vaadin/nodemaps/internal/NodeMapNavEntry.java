/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.internal;

import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.navigate.ConditionalPageNavEntry;
import org.opennms.web.navigate.DisplayStatus;

public class NodeMapNavEntry implements ConditionalPageNavEntry {
    private String m_name;
    private String m_url;

    @Override public DisplayStatus evaluate(final HttpServletRequest request, final Object target) {
        if (target instanceof OnmsNode) {
            final OnmsNode node = (OnmsNode)target;
            if (node.getAssetRecord() != null && node.getAssetRecord().getGeolocation() != null) {
                final OnmsGeolocation geolocation = node.getAssetRecord().getGeolocation();
                if (geolocation.getLongitude() != null && geolocation.getLatitude() != null) {
                    return DisplayStatus.DISPLAY_LINK;
                }
            }
        }
        return DisplayStatus.NO_DISPLAY;
    }

    @Override public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    @Override public String getUrl() {
        return m_url;
    }

    public void setUrl(final String url) {
        m_url = url;
    }

    @Override public String toString() {
        return "NodeMapNavEntry[url=" + m_url + ",name=" + m_name +"]";
    }
}