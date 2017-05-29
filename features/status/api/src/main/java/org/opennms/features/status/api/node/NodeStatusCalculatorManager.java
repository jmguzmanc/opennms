/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.status.api.node;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.status.api.node.strategy.AlarmNodeStatusCalculator;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculationStrategy;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculator;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculatorConfig;
import org.opennms.features.status.api.node.strategy.OutageNodeStatusCalculator;
import org.opennms.features.status.api.node.strategy.Status;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class NodeStatusCalculatorManager implements NodeStatusCalculationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(NodeStatusService.class);

    protected static final NodeStatusCalculator NULL_STATUS_CALCULATOR = new NodeStatusCalculator() {
        @Override
        public Status calculateStatus(NodeStatusCalculatorConfig query) {
            return new Status();
        }

        @Override
        public Map<OnmsSeverity, Long> calculateStatusOverview(NodeStatusCalculatorConfig query) {
            return new HashMap<>();
        }

        @Override
        public int countStatus(NodeStatusCalculatorConfig config) {
            return 0;
        }
    };

    @Autowired
    private AlarmNodeStatusCalculator alarmStatusCalculator;

    @Autowired
    private OutageNodeStatusCalculator outageStatusCalculator;

    @Override
    public Status calculateStatus(NodeStatusCalculatorConfig query) {
        final Status status = getStatusCalculator(query.getCalculationStrategy()).calculateStatus(query);
        return status;
    }

    public Map<OnmsSeverity, Long> calculateStatusOverview(NodeStatusCalculatorConfig query) {
        final Map<OnmsSeverity, Long> statusOverview = getStatusCalculator(query.getCalculationStrategy()).calculateStatusOverview(query);
        return statusOverview;
    }

    public int countStatus(NodeStatusCalculatorConfig config) {
        return getStatusCalculator(config.getCalculationStrategy()).countStatus(config);
    }

    protected NodeStatusCalculator getStatusCalculator(NodeStatusCalculationStrategy strategy) {
        if (strategy == null) {
            LOG.warn("No strategy defined. Falling back to strategy:{}", NodeStatusCalculationStrategy.None);
            strategy = NodeStatusCalculationStrategy.None;
        }
        switch (strategy) {
            case Alarms:
                return alarmStatusCalculator;
            case Outages:
                return outageStatusCalculator;
        }
        return NULL_STATUS_CALCULATOR;
    }
}
