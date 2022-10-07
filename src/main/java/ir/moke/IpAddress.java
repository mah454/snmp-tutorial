package ir.moke;

public class IpAddress {
    @SnmpColumn(1)
    private int interfaceIndex;
    @SnmpColumn(0)
    private String ip;
    @SnmpColumn(2)
    private String netmask;

    public int getInterfaceIndex() {
        return interfaceIndex;
    }

    public void setInterfaceIndex(int interfaceIndex) {
        this.interfaceIndex = interfaceIndex;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    @Override
    public String toString() {
        return "IpAddress{" +
                "interfaceIndex=" + interfaceIndex +
                ", ip='" + ip + '\'' +
                ", netmask='" + netmask + '\'' +
                '}';
    }
}
