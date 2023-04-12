package com.armedia.alfresco.extension.contentseeder.patch;

public interface AuthenticationWrapper {

	@FunctionalInterface
	public static interface CheckedRunnable<T> {
		public T run() throws Exception;
	}

	public <T> T runAsAdministrator(CheckedRunnable<T> task) throws Exception;

}