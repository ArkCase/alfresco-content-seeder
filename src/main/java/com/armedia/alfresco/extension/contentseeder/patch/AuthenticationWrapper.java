package com.armedia.alfresco.extension.contentseeder.patch;

@FunctionalInterface
public interface AuthenticationWrapper<T> {

	@FunctionalInterface
	public static interface CheckedRunnable<T> {
		public T run() throws Exception;
	}

	public T runAsAdministrator(CheckedRunnable<T> task) throws Exception;

}