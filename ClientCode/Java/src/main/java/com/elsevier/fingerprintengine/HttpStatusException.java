package com.elsevier.fingerprintengine;

import org.apache.http.HttpException;

/**
 * An {@link org.apache.http.HttpException} thrown by a non OK response code
 */
public class HttpStatusException extends HttpException {
	private int statusCode;

	public HttpStatusException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public HttpStatusException(int statusCode, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String toString() {
		return "HttpStatusException{" +
			"statusCode=" + statusCode +
			",message=" + getMessage() +
			'}';
	}
}
