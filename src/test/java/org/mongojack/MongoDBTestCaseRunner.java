/*
 * Copyright 2011 VZ Netzwerke Ltd
 * Copyright 2014 devbliss GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.mongojack.testing.DbManager;

import java.util.Collections;
import java.util.List;

/**
 * Runner that runs the tests through different permutations of configurations
 */
public class MongoDBTestCaseRunner extends Suite {

    public MongoDBTestCaseRunner(Class<?> klass) throws InitializationError {
        super(klass, Collections.singletonList(new TestRunner(klass)));
    }

    /**
     * This runner holds the configuration information
     */
    private static class TestRunner extends BlockJUnit4ClassRunner {
        private TestRunner(Class<?> klass)
            throws InitializationError {
            super(klass);
        }

        @Override
        public Object createTest() throws Exception {
            return super.createTest();
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
        }

        @Override
        protected Statement classBlock(RunNotifier notifier) {
            return childrenInvoker(notifier);
        }
    }

    @Override
    public void run(final RunNotifier notifier) {
        // Maven instantiates the DbRunListener directly via the Surefire plugin
        // If we're running in Eclipse then we start and stop the DB here since there
        // doesn't appear to be an easy way to use a RunListener in Eclipse
        String isMavenProperty = System.getProperty("isMaven", "false");
        final boolean isMaven = Boolean.parseBoolean(isMavenProperty);
        if (!isMaven) {
            DbManager.startDb();
        }
        super.run(notifier);
        if (!isMaven) {
            DbManager.stopDb();
        }
    }

}
