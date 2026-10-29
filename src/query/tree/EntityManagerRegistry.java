package query.tree;

import java.util.HashMap;
import java.util.Map;

import reflection.utility.EntityManager;

public class EntityManagerRegistry {
    private Map<Class<?>, EntityManager<?>> registry =
        new HashMap<>();
    public <T> void register(
            Class<T> cls,
            EntityManager<T> em) {
        registry.put(cls, em);
    }
    public <T> EntityManager<T> get(
            Class<T> cls) {
        return (EntityManager<T>)
            registry.get(cls);
    }
}
