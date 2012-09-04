package org.ubimix.commons.cursor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kotelnikov
 * @param <K> the type of keys used to define cursor providers
 * @param <P> the type of the parameter for the cursor providers
 * @param <C> the type of the managed cursors
 */
public abstract class CompositeCursorProvider<K, P, E extends Exception, C extends ICursor<?, E>>
    implements
    ICursorProvider<P, E, C> {

    private ICursorProvider<P, E, C> fDefaultProvider;

    private Map<K, ICursorProvider<P, E, C>> fMap = new HashMap<K, ICursorProvider<P, E, C>>();

    public CompositeCursorProvider() {
        this(null);
    }

    public CompositeCursorProvider(ICursorProvider<P, E, C> defaultProvider) {
        fDefaultProvider = defaultProvider;
    }

    public C getCursor(P parameter) throws E {
        K key = getKey(parameter);
        ICursorProvider<P, E, C> provider = fMap.get(key);
        if (provider == null) {
            provider = fDefaultProvider;
        }
        C result = null;
        if (provider != null) {
            result = provider.getCursor(parameter);
        }
        return result;
    }

    public ICursorProvider<P, E, C> getDefaultActivityCursorProvider() {
        return fDefaultProvider;
    }

    /**
     * This method should be overloaded to define the real key of the provider
     * 
     * @param parameter
     * @return
     */
    protected abstract K getKey(P parameter);

    public ICursorProvider<P, E, C> removeProvider(K key) {
        return fMap.remove(key);
    }

    public void setDefaultCursorProvider(
        ICursorProvider<P, E, C> defaultCursorProvider) {
        fDefaultProvider = defaultCursorProvider;
    }

    public void setProvider(K key, ICursorProvider<P, E, C> provider) {
        fMap.put(key, provider);
    }

}