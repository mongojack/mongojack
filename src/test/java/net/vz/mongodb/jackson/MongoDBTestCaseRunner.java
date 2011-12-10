/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vz.mongodb.jackson;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Runner that runs the tests through different permutations of configurations
 */
public class MongoDBTestCaseRunner extends Suite {

    public MongoDBTestCaseRunner(Class<?> klass) throws InitializationError {
        super(klass, createChildren(klass));
    }

    private static List<Runner> createChildren(Class<?> klass) throws InitializationError {
        MongoTestParams.DeserializerType deserializerType = MongoTestParams.DeserializerType.BOTH;
        MongoTestParams params = klass.getAnnotation(MongoTestParams.class);
        if (params != null) {
            deserializerType = params.deserializerType();
        }
        List<Runner> runners = new ArrayList<Runner>();
        if (deserializerType != MongoTestParams.DeserializerType.OBJECT) {
            runners.add(new TestRunner(klass, "stream", "[stream]", true));
        }
        if (deserializerType != MongoTestParams.DeserializerType.STREAM) {
            runners.add(new TestRunner(klass, "object", "[object]", false));
        }
        return runners;
    }

    /**
     * A parent runner with a given name
     */
    public static class NamedParentRunner extends ParentRunner<Runner> {

        private final List<Runner> runners;
        private final String name;

        protected NamedParentRunner(Class<?> klass, List<Runner> runners, String name) throws InitializationError {
            super(klass);
            this.runners = runners;
            this.name = name;
        }

        protected List<Runner> getChildren() {
            return runners;
        }

        protected Description describeChild(Runner child) {
            return child.getDescription();
        }

        protected void runChild(Runner child, RunNotifier notifier) {
            child.run(notifier);
        }

        protected String getName() {
            return name;
        }
    }

    /**
     * This runner holds the configuration information
     */
    private static class TestRunner extends BlockJUnit4ClassRunner {
        private final String name;
        private final String description;
        private final boolean useStreamDeserialisation;

        private TestRunner(Class<?> klass, String name, String description, boolean useStreamDeserialisation) throws InitializationError {
            super(klass);
            this.name = name;
            this.description = description;
            this.useStreamDeserialisation = useStreamDeserialisation;
        }

        @Override
        public Object createTest() throws Exception {
            Object test = super.createTest();
            if (test instanceof MongoDBTestBase) {
                ((MongoDBTestBase) test).setUseStreamParser(useStreamDeserialisation);
            }
            return test;
        }

        @Override
        protected String getName() {
            return name;
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format(method.getName() + description);
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
        }

        @Override
        protected Statement classBlock(RunNotifier notifier) {
            return childrenInvoker(notifier);
        }
    }

}
