package ir.moke;

public class Ethernet {
    @SnmpColumn(0)
    private long index;
    @SnmpColumn(1)
    private String name ;
    @SnmpColumn(5)
    private String mac ;
    @SnmpColumn(3)
    private int mtu ;
    @SnmpColumn(6)
    private boolean state ;
    @SnmpColumn(10)
    private long rxPkt;
    @SnmpColumn(16)
    private long txPkt;
    @SnmpColumn(9)
    private long rxBytes;
    @SnmpColumn(15)
    private long txBytes;

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public long getRxPkt() {
        return rxPkt;
    }

    public void setRxPkt(long rxPkt) {
        this.rxPkt = rxPkt;
    }

    public long getTxPkt() {
        return txPkt;
    }

    public void setTxPkt(long txPkt) {
        this.txPkt = txPkt;
    }

    public long getRxBytes() {
        return rxBytes;
    }

    public void setRxBytes(long rxBytes) {
        this.rxBytes = rxBytes;
    }

    public long getTxBytes() {
        return txBytes;
    }

    public void setTxBytes(long txBytes) {
        this.txBytes = txBytes;
    }

    @Override
    public String toString() {
        return "Ethernet{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                ", mtu=" + mtu +
                ", state=" + state +
                ", rxPkt=" + rxPkt +
                ", txPkt=" + txPkt +
                ", rxBytes=" + rxBytes +
                ", txBytes=" + txBytes +
                '}';
    }
}
