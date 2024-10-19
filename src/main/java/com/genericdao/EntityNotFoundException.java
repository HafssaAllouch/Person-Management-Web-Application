package com.genericdao;

public class EntityNotFoundException extends RuntimeException {

	public EntityNotFoundException() {
	}
	public EntityNotFoundException(String msg) {
		super(msg);
	}
	public EntityNotFoundException(Throwable cause) {
		super(cause);
	}

}
