package query.tree;

import java.util.HashMap;
import java.util.Map;

public class JoinedRow {
    private Map<Class<?>, Object> data =
        new HashMap<>();
    public <T extends Object> void put(Class<T> cls, Object obj) {
        data.put(cls, obj);
    }
    public <T extends Object> T get(Class<T> cls) {
        return (T) data.get(cls);
    }
	@Override
	public String toString() {
		return "JoinedRow [data=" + data + "]";
	}
    
    
}
