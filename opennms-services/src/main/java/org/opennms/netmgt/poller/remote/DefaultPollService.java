/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 18, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.poller.remote;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.springframework.util.Assert;

/**
 * <p>DefaultPollService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DefaultPollService implements PollService {
    
    TimeAdjustment m_timeAdjustment;
	
    Collection<ServiceMonitorLocator> m_locators;
    Map<String, ServiceMonitor> m_monitors;
    

    /**
     * @param timeAdjustment the timeAdjustment to set
     */
    public void setTimeAdjustment(TimeAdjustment timeAdjustment) {
        m_timeAdjustment = timeAdjustment;
    }

    /** {@inheritDoc} */
    public void setServiceMonitorLocators(Collection<ServiceMonitorLocator> locators) {
        m_locators = locators;
        
        Map<String, ServiceMonitor> monitors = new HashMap<String, ServiceMonitor>();
        for (ServiceMonitorLocator locator : locators) {
            monitors.put(locator.getServiceName(), locator.getServiceMonitor());
        }
        
        m_monitors = monitors;
    }
    
    /** {@inheritDoc} */
    public void initialize(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        monitor.initialize(polledService);
    }

    /** {@inheritDoc} */
    public PollStatus poll(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        PollStatus result = monitor.poll(polledService, polledService.getMonitorConfiguration());
        result.setTimestamp(m_timeAdjustment.adjustDateToMasterDate(result.getTimestamp()));
        return result;
    }

    private ServiceMonitor getServiceMonitor(PolledService polledService) {
        Assert.notNull(m_monitors, "setServiceMonitorLocators must be called before any other operations");
        ServiceMonitor monitor = (ServiceMonitor)m_monitors.get(polledService.getSvcName());
        Assert.notNull(monitor, "Unable to find monitor for service "+polledService.getSvcName());
        return monitor;
    }

    // FIXME: this is never called but should be
    // also monitor.release() isn't called either
    /** {@inheritDoc} */
    public void release(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        monitor.release(polledService);
    }




}