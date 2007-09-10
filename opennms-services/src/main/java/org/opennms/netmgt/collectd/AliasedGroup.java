//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.collectd;

import java.util.Collection;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class AliasedGroup extends AttributeGroup {
	
	AttributeGroup m_group;

	public AliasedGroup(CollectionResource resource, AttributeGroup group) {
		super(resource, group.getGroupType());
		m_group = group;
	}

	public void addAttribute(Attribute attr) {
		m_group.addAttribute(attr);
	}

	public boolean equals(Object obj) {
		return m_group.equals(obj);
	}

	public Collection<Attribute> getAttributes() {
		return m_group.getAttributes();
	}

	public AttributeGroupType getGroupType() {
		return m_group.getGroupType();
	}

	public String getName() {
		return m_group.getName();
	}

	public int hashCode() {
		return m_group.hashCode();
	}

	public boolean shouldPersist(ServiceParameters params) {
		return m_group.shouldPersist(params);
	}

	public String toString() {
		return m_group.toString();
	}
	
	Category log(){
		return ThreadCategory.getInstance(getClass());
	}

	public void visit(CollectionSetVisitor visitor) {
		visitor.visitGroup(this);
		
		for(Attribute attr : getAttributes()) {
		    AliasedAttribute aliased = new AliasedAttribute(getResource(), attr);
		    log().debug("visiting at aliased  = " + aliased);
		    aliased.visit(visitor);
		}
		
		visitor.completeGroup(this);
	}

}
