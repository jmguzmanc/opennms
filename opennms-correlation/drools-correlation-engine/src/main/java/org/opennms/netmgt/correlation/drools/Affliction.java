package org.opennms.netmgt.correlation.drools;

import java.util.ArrayList;
import java.util.List;

public class Affliction {
    Long m_nodeid;
    String m_ipAddr;
    String m_svcName;
    
    public static enum Type {
        UNDECIDED,
        ISOLATED,
        WIDE_SPREAD
    }
    
    List<Integer> m_reporters = new ArrayList<Integer>();
    private Type m_type  = Type.UNDECIDED;
    
    public Affliction(Long nodeId, String ipAddr, String svcName, Integer reporter) {
        m_nodeid = nodeId;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_reporters.add(reporter);
    }

    public String getIpAddr() {
        return m_ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        m_ipAddr = ipAddr;
    }

    public Long getNodeid() {
        return m_nodeid;
    }

    public void setNodeid(Long nodeid) {
        m_nodeid = nodeid;
    }

    public List<Integer> getReporters() {
        return m_reporters;
    }

    public void setReporters(List<Integer> reporters) {
        m_reporters = reporters;
    }

    public String getSvcName() {
        return m_svcName;
    }

    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }
    
    public int getReporterCount() {
        return m_reporters.size();
    }
    
    public void addReporter(Integer reporter) {
        m_reporters.add( reporter );
    }
    
    public void removeReporter(Integer reporter) {
        m_reporters.remove(reporter);
    }
    
    public Type getType() {
        return m_type;
    }
    
    public void setType(Type type) {
        m_type = type;
    }
    
}
