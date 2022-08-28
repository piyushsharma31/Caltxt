/*
 * Copyright (C) Alerteyes, 2007
 *
 * Author grants you a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in source
 * and binary code form, provided that this copyright notice and
 * license appear on all copies of the derived software source code.
 *
 * This software is provided "AS IS", without a warranty of any kind.
 * Use at your own risk. Be advised that running network server
 * software like this can severely damage the security of your system.
 *
 */
package com.jovistar.commons.util;

public class XNoSuchElementException extends RuntimeException {

	/**
	 * Constructs a NoSuchElementException with no detail message.
	 */
	public XNoSuchElementException()
	{
	}

	/**
	 *Constructs a NoSuchElementException with a detail message.
	 *
	 * @param detail the detail message for the exception
	 */
	public XNoSuchElementException(String detail)
	{
		super(detail);
	}
}
