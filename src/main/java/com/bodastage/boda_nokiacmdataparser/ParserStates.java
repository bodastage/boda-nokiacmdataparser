/*
 * Parser processing states.
 */
package com.bodastage.boda_nokiacmdataparser;

/**
 *
 * @author info@bodastage.com
 * 
 * @since 1.1.0
 */
public final class ParserStates {
    
    /**
     * Managed Object parameters extraction stage.
     */
    public static final int EXTRACTING_PARAMETERS = 1;
    
    /**
     * Parameter value extraction stage
     */
    public static final int EXTRACTING_VALUES = 2;
    
    /**
     * Parsing completed
     */
    public static final int EXTRACTING_DONE = 3;
}
