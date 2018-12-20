package com.redhat.business.automation.adapter.rules.api;

public class InternalRuleIO {

    public enum Type {
        IN_OUT, IN_ONLY, OUT_ONLY
    }

    private final Type type;
    private final String identifier;
    private final Object fact;
    private final Class<?> outputClass;

    private InternalRuleIO( Type type, String identifier, Object fact, Class<?> outputClass ) {
        super();
        this.type = type;
        this.identifier = identifier;
        this.fact = fact;
        this.outputClass = outputClass;
    }

    /**
     * Create an in/out parameter
     * 
     * @param type
     * @param identifier
     * @param fact
     */
    public InternalRuleIO( String identifier, Object fact ) {
        this( Type.IN_OUT, identifier, fact, null );
    }

    /**
     * 
     * Create an in only parameter
     * 
     * @param type
     * @param fact
     */
    public InternalRuleIO( Object fact ) {
        this( Type.IN_ONLY, null, fact, null );
    }

    /**
     * 
     * Create an out only paramter
     * 
     * @param type
     * @param clazz
     */
    public InternalRuleIO( String identifier, Class<?> clazz ) {
        this( Type.OUT_ONLY, identifier, null, clazz );
    }

    public Type getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Object getFact() {
        return fact;
    }

    public Class<?> getOutputClass() {
        return outputClass;
    }

    @Override
    public String toString() {
        return "InternalRuleIO [type=" + type + ", identifier=" + identifier + ", fact=" + fact + ", outputClass=" + outputClass + "]";
    }

}
