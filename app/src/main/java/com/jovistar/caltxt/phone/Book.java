package com.jovistar.caltxt.phone;

import com.jovistar.commons.bo.IDTObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class Book {
    private static final String TAG = "Book";

    private ArrayList<IDTObject> _log;
    private HashMap<Object/*pid for XCtx, username for XMob*/, IDTObject> _logMap;

    protected Book() {
        _logMap = new HashMap<Object, IDTObject>();
        _log = new ArrayList<IDTObject>();
    }

    protected synchronized void load(ArrayList<IDTObject> l) {

        if (l == null || l.size() == 0)
            return;

        Iterator<IDTObject> iterator = l.iterator();
        while (iterator.hasNext()) {
            IDTObject o = iterator.next();
            _log.add(o);
            _logMap.put(o.getKey(), o);
//			Log.i(TAG, "add "+o.toString());
        }
    }

    public synchronized void prepend(Object key, IDTObject obj) {
//		long id = Persistence.getInstance(Globals.getCustomAppContext()).insert(obj);
//		obj.setPersistenceId(id);
//		_log.add(obj);
        _log.add(0, obj);
        _logMap.put(key, obj);

		/*if(obj.getCName().equals("XCtx")) {
            _logMap.put(id, obj);
		} else if (obj.getCName().equals("XMob")) {
			_logMap.put(((XMob)obj).getUsername(), obj);
		}
		return id;*/
    }

    public synchronized void remove(Object key, IDTObject obj) {
        _log.remove(obj);
        _logMap.remove(key);
    }

    public synchronized void clear() {
        _logMap.clear();
        _log.clear();
    }

    public IDTObject get(Object key) {
        return _logMap.get(key);
    }

    public ArrayList<IDTObject> getList() {
        return _log;
    }

    public int getCount() {
        if (_log == null)
            return 0;
        return _log.size();
    }
}
