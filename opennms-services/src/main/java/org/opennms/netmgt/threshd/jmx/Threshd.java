//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.threshd.jmx;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;


/**
 * <p>Threshd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Threshd extends AbstractServiceDaemon implements ThreshdMBean {

    /**
     * <p>Constructor for Threshd.</p>
     */
    public Threshd() {
        super(NAME);
    }

    /**
     * Log4j category
     */
    private final static String NAME = "OpenNMS.Threshd";

    /**
     * <p>onInit</p>
     */
    protected void onInit() {
        // Load threshd configuration file
        //
        try {
            ThreshdConfigFactory.reload();
            ThresholdingConfigFactory.reload();
        } catch (MarshalException ex) {
            log().fatal("start: Failed to load threshd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            log().fatal("start: Failed to load threshd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            log().fatal("start: Failed to load threshd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }
        
        // Load up the configuration for the scheduled outages.
        //
        try {
            PollOutagesConfigFactory.reload();
        } catch (MarshalException ex) {
            log().fatal("start: Failed to load poll-outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            log().fatal("start: Failed to load poll-outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            log().fatal("start: Failed to load poll-outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }


        
        getInstance().setThreshdConfig(ThreshdConfigFactory.getInstance());

        getInstance().init();
    }

    private org.opennms.netmgt.threshd.Threshd getInstance() {
        return org.opennms.netmgt.threshd.Threshd.getInstance();
    }

    /**
     * <p>onStart</p>
     */
    protected void onStart() {
        getInstance().start();
    }

    /**
     * <p>onStop</p>
     */
    protected void onStop() {
        getInstance().stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus() {
        return getInstance().getStatus();
    }
}