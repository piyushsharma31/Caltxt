/*
 * Licensed Materials - Property of IBM
 *
 * 5747-SM3
 *
 * (C) Copyright IBM Corp. 1999, 2012 All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 *
 */
package com.jovistar.caltxt.persistence;

import com.jovistar.caltxt.mqtt.client.ConnectionMqtt;

/**
 * Persistence Exception, defines an error with persisting a {@link ConnectionMqtt}
 * fails. Example operations are {@link Persistence#persistConnection(ConnectionMqtt)} and {@link Persistence#restoreConnections(android.content.Context)};
 * these operations throw this exception to indicate unexpected results occurred when performing actions on the database.
 */
public class PersistenceException extends Exception {

    /**
     * Creates a persistence exception with the given error message
     *
     * @param message The error message to display
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Serialisation ID
     **/
    private static final long serialVersionUID = 5326458803268855071L;

}
