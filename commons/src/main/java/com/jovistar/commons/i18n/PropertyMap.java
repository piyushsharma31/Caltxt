package com.jovistar.commons.i18n;
/*
package com.jovistar.ccm.i18n;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import com.jovistar.ccm.exception.CCMException;
//import com.jovistar.ccm.i18n.TextMap;

public class PropertyMap extends Hashtable {

    public final char RECORD_DELIMITER = '=';
    private PropertyMap parent;
    private String name;

    public PropertyMap() {
        this.name = "nokiae61";
    }

    private PropertyMap(String name) {
        this.name = name;
    }

    private PropertyMap(PropertyMap parent, String name) {
        this.name = name;
        this.parent = parent;
    }

    public void init() throws CCMException {
        InputStream is = null;
        StringBuffer path = new StringBuffer();
        path.append("/config/").append(name).append(".properties");

        try {
            is = getClass().getResourceAsStream(path.toString());
            parse(is);
        } catch (IOException e) {
            throw new CCMException(name+":"+e.getMessage());
        } finally {
            try {
                path = null;
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
    }

    public void parse(InputStream is) throws IOException {
        int b;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((b = is.read()) != -1) {
            if (b == '\n') {
                addLine(new String(baos.toByteArray()));
                baos.reset();
            } else {
                if (b != '\n' && b != '\r') {
                    baos.write(b);
                }
            }
        }

        if (baos.size() != 0) {
            addLine(new String(baos.toByteArray()));
        }
    }

    private void addLine(String line) {
        if (line.charAt(0) != '#') {
            int ii = line.indexOf(RECORD_DELIMITER);
            if (ii != -1 && ii != (line.length() - 1)) {
                put(line.substring(0, ii), line.substring(ii + 1));
            }
        }
    }
*//*
    public void delete() throws CCMException {
        String recordStoreName = "props-" + name;
        try {
            RecordStore.deleteRecordStore(recordStoreName);
        } catch (RecordStoreException e) {
            throw new CCMException(TextMap.getInstance().get("recordstoreerr")+":delete "+name);
        }
    }

    public void read() throws CCMException {
        String recordStoreName = "props-" + name;
        RecordStore preferenceDB = null;

        try {
            preferenceDB = RecordStore.openRecordStore(recordStoreName, false);
            RecordEnumeration renum = preferenceDB.enumerateRecords(null, null, false);

            while (renum.hasNextElement()) {
                addLine(new String(renum.nextRecord()));
            }
        } catch (RecordStoreNotFoundException e) {
            throw new CCMException(TextMap.getInstance().get("recordstorenotopen")+":read "+name);
        } catch (RecordStoreException e) {
            throw new CCMException(TextMap.getInstance().get("recordstoreerr")+":read "+name);
        } finally {
            try {
                if (preferenceDB != null) {
                    preferenceDB.closeRecordStore();
                }
            } catch (RecordStoreException e) {
//                e.printStackTrace();
            }
        }
    }

    public void write() {
        String recordStoreName = "props-" + name;
        RecordStore recordStore = null;

        try {
            delete();

            if (!isEmpty()) {
                recordStore = RecordStore.openRecordStore(recordStoreName, true);

                Object key;
                byte[] bytes;

                StringBuffer buffer = new StringBuffer();
                Enumeration keys = keys();
                while (keys.hasMoreElements()) {
                    key = keys.nextElement();
                    buffer.append(key);
                    buffer.append(RECORD_DELIMITER);
                    buffer.append(get(key));

                    bytes = buffer.toString().getBytes();

                    recordStore.addRecord(bytes, 0, bytes.length);
                    buffer.setLength(0);
                }
            }

            //Logger.instance.log("propertyMap.write()", name + " saved to " + recordStoreName);
        } catch (Exception e) {
            //Logger.instance.log("propertyMap.write().1", e);
        } finally {
            try {
                if (recordStore != null) {
                    recordStore.closeRecordStore();
                }
            } catch (RecordStoreException e) {
//                e.printStackTrace();
                //Logger.instance.log("propertyMap.write().2", e);
            }
        }
    }
*/
/*    //TODO: only strings
    public Object put(Object arg0, Object arg1) {
        return super.put(arg0.toString(), arg1.toString());
    }

    public String get(String name) {
        String value = (String) super.get(name);
        if (value == null) {
            return (parent == null) ? null : parent.get(name);
        } else {
            return value;
        }
    }

    public String get(String name, String def) {
        String value = get(name);
        return (value == null) ? def : String.valueOf(value);
    }

    public boolean getBoolean(String name, boolean def) {
        String value = get(name);
        return (value == null) ? def : value.equalsIgnoreCase("true");
    }

    public int getInt(String name, int def) {
        String value = get(name);
        if (value != null) {
            if (value.startsWith("-")) {
                return -1 * Integer.parseInt(value.substring(1));
            } else {
                return Integer.parseInt(value);
            }
        }

        return def;
    }

    public int getInt(String name, int def, int r) {
        String value = get(name);
        if (value != null) {
                if (value.startsWith("-")) {
                    return -1 * Integer.parseInt(value.substring(1), r);
                } else {
                    return Integer.parseInt(value, r);
                }
        }

        return def;
    }
}
*/