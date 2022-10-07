package ir.moke;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class MainClass {
    private static final String NETWORK_IP_TABLE_OID = ".1.3.6.1.2.1.4.20.1";
    private static final String NETWORK_INTERFACE_TABLE_OID = ".1.3.6.1.2.1.2.2.1";
    private static final String TEST_SET_VALUE = ".1.3.6.1.2.1.1.5.0";
    private static final Address address = GenericAddress.parse("udp://10.10.10.2/161");

    public static void main(String[] args) throws Exception {
        CommunityTarget<Address> target = new CommunityTarget<>();
        target.setCommunity(new OctetString("private"));
        target.setAddress(address);
        target.setRetries(5);
        target.setTimeout(15000);
        target.setVersion(SnmpConstants.version2c);

        setValue(TEST_SET_VALUE, target);

//        Map<String, List<String>> map = doWalkTable(NETWORK_INTERFACE_TABLE_OID, target);

//        List<Ethernet> list = mapToPojo(map, Ethernet.class);
//        list.forEach(System.out::println);
    }

    public static <T> List<T> mapToPojo(Map<String, List<String>> map, Class<T> pojoClass) {
        List<T> ipAddressList = new ArrayList<>();
        for (List<String> valueList : map.values()) {
            T instance = newInstance(pojoClass);
            try {
                Field[] declaredFields = pojoClass.getDeclaredFields();
                for (Field field : declaredFields) {
                    if (field.isAnnotationPresent(SnmpColumn.class)) {
                        field.setAccessible(true);
                        int columnIndex = field.getDeclaredAnnotation(SnmpColumn.class).value();
                        String value = valueList.get(columnIndex);
                        if (field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(int.class)) {
                            field.set(instance, Integer.parseInt(value));
                        } else if (field.getType().isAssignableFrom(Long.class) || field.getType().isAssignableFrom(long.class)) {
                            field.set(instance, Long.parseLong(value));
                        } else if (field.getType().isAssignableFrom(Boolean.class) || field.getType().isAssignableFrom(boolean.class)) {
                            if (value.equals("2")) value = "false";
                            if (value.equals("1")) value = "true";
                            field.set(instance, Boolean.parseBoolean(value));
                        } else if (field.getType().isAssignableFrom(String.class)) {
                            field.set(instance, value);
                        } else {
                            System.out.println("Unknown data type");
                        }
                        field.setAccessible(false);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            ipAddressList.add(instance);
        }
        return ipAddressList;
    }

    private static <T> T newInstance(Class<T> pojoClass) {
        try {
            return pojoClass.getConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, String> doWalkTree(String tableOid, Target<Address> target) throws IOException {
        Map<String, String> result = new TreeMap<>();
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(target, new OID(tableOid));
        if (events == null || events.size() == 0) {
            System.out.println("Error: Unable to read table...");
            return result;
        }

        for (TreeEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.out.println("Error: table OID [" + tableOid + "] " + event.getErrorMessage());
                continue;
            }

            VariableBinding[] varBindings = event.getVariableBindings();
            if (varBindings == null || varBindings.length == 0) {
                continue;
            }
            for (VariableBinding varBinding : varBindings) {
                if (varBinding == null) {
                    continue;
                }

                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
            }

        }
        snmp.close();

        return result;
    }

    public static Map<String, List<String>> doWalkTable(String tableOid, Target<Address> target) throws IOException {
        Map<String, List<String>> map = new HashMap<>();
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        TableUtils tableUtils = new TableUtils(snmp, new DefaultPDUFactory());
        List<TableEvent> events = tableUtils.getTable(target, new OID[]{new OID(tableOid)}, null, null);
        if (events == null || events.size() == 0) {
            System.out.println("Error: Unable to read table...");
            return map;
        }

        for (TableEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.out.println("Error: table OID [" + tableOid + "] " + event.getErrorMessage());
                continue;
            }

            VariableBinding[] varBindings = event.getColumns();

            if (varBindings == null || varBindings.length == 0) {
                continue;
            }
            for (VariableBinding varBinding : varBindings) {
                if (varBinding == null) {
                    continue;
                }
                String key = varBinding.getOid().toString();
                String value = varBinding.toValueString();
                List<String> listValues = new ArrayList<>();
                if (key.startsWith(tableOid.substring(1) + ".1.")) {
                    listValues.add(value);
                    map.put(value, listValues);
                } else {
                    String mapKey = map.keySet().stream().filter(key::endsWith).findFirst().orElse(null);
                    listValues = map.get(mapKey);
                    listValues.add(value);
                }
            }
        }
        snmp.close();
        return map;
    }

    public static void setValue(String oid, Target<Address> target) {
        try {
            TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            VariableBinding vb = new VariableBinding(new OID(oid), new IpAddress("20.20.20.12"));
            PDU pdu = new PDU();
            pdu.add(vb);
            pdu.setType(PDU.SET);
            ResponseEvent<Address> event = snmp.send(pdu, target);
            if (event != null) {
                System.out.println("\nResponse:\nGot Snmp Set Response from Agent");
                System.out.println("Snmp Set Request = " + event.getRequest().getVariableBindings());
                PDU responsePDU = event.getResponse();
                System.out.println("\nresponsePDU = " + responsePDU);
                if (responsePDU != null) {
                    int errorStatus = responsePDU.getErrorStatus();
                    int errorIndex = responsePDU.getErrorIndex();
                    String errorStatusText = responsePDU.getErrorStatusText();
                    System.out.println("\nresponsePDU = " + responsePDU);
                    if (errorStatus == PDU.noError) {
                        System.out.println("Snmp Set Response = " + responsePDU.getVariableBindings());
                    } else {
                        System.out.println("errorStatus = " + responsePDU);
                        System.out.println("Error: Request Failed");
                        System.out.println("Error Status = " + errorStatus);
                        System.out.println("Error Index = " + errorIndex);
                        System.out.println("Error Status Text = " + errorStatusText);
                    }
                }
            }
            snmp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
