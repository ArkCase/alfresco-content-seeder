package com.armedia.alfresco.extension.mock;

import org.easymock.EasyMock;
import org.springframework.beans.factory.FactoryBean;

public class MockService<T> implements FactoryBean<T> {
	private Class<T> mockedClass;

	private boolean singleton = true;
	private boolean strict = true;

	@SuppressWarnings("unchecked")
	public void setMockedClass(Class<?> mockedClass) {
		this.mockedClass = (Class<T>) mockedClass;
	}

	@Override
	public T getObject() throws Exception {
		return this.strict ? EasyMock.createStrictMock(this.mockedClass) : EasyMock.createMock(this.mockedClass);
	}

	@Override
	public Class<T> getObjectType() {
		return this.mockedClass;
	}

	public boolean isStrict() {
		return this.strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	@Override
	public boolean isSingleton() {
		return this.singleton;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
}