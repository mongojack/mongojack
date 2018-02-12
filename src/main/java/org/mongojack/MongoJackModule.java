package org.mongojack;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class MongoJackModule extends Module {

	public static final Module INSTANCE = new org.mongojack.internal.MongoJackModule();

	@Override
	public String getModuleName() {
		return INSTANCE.getModuleName();
	}

	@Override
	public Version version() {
		// TODO fix this up with some build filtered properties
		return new Version(2, 1, 0, "SNAPSHOT", getClass().getPackage()
				.getName(), getClass().getSimpleName());
	}

	@Override
	public void setupModule(SetupContext context) {
		INSTANCE.setupModule(context);
	}

}
