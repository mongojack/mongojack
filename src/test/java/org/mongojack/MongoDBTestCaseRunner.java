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

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Runner that runs the tests through different permutations of configurations
 */
public class MongoDBTestCaseRunner extends Suite {

    public MongoDBTestCaseRunner(Class<?> klass) throws InitializationError {
        super(klass, createChildren(klass));
    }

    private static List<Runner> createChildren(Class<?> klass)
            throws InitializationError {
        MongoTestParams.SerializationType deserializerType = MongoTestParams.SerializationType.BOTH;
        MongoTestParams.SerializationType serializerType = MongoTestParams.SerializationType.BOTH;
        MongoTestParams params = klass.getAnnotation(MongoTestParams.class);
        if (params != null) {
            deserializerType = params.deserializerType();
            serializerType = params.serializerType();
        }
        List<Runner> runners = new ArrayList<Runner>();
        for (MongoTestParams.SerializationConfig deser : deserializerType
                .getConfigs()) {
            List<Runner> sers = new ArrayList<Runner>();
            for (MongoTestParams.SerializationConfig ser : serializerType
                    .getConfigs()) {
                sers.add(new TestRunner(klass, ser.getName() + "-serializer",
                        "[" + deser.getName() + "-des][" + ser.getName()
                                + "-ser]", deser.isEnabled(), ser.isEnabled()));
            }
            runners.add(new NamedParentRunner(klass, sers, deser.getName()
                    + "-deserializer"));
        }
        return runners;
    }

    /**
     * A parent runner with a given name
     */
    public static class NamedParentRunner extends ParentRunner<Runner> {

        private final List<Runner> runners;
        private final String name;

        protected NamedParentRunner(Class<?> klass, List<Runner> runners,
                String name) throws InitializationError {
            super(klass);
            this.runners = runners;
            this.name = name;
        }

        @Override
        protected List<Runner> getChildren() {
            return runners;
        }

        @Override
        protected Description describeChild(Runner child) {
            return child.getDescription();
        }

        @Override
        protected void runChild(Runner child, RunNotifier notifier) {
            child.run(notifier);
        }

        @Override
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
        private final boolean useStreamSerialisation;

        private TestRunner(Class<?> klass, String name, String description,
                boolean useStreamDeserialisation, boolean useStreamSerialisation)
                throws InitializationError {
            super(klass);
            this.name = name;
            this.description = description;
            this.useStreamDeserialisation = useStreamDeserialisation;
            this.useStreamSerialisation = useStreamSerialisation;
        }

        @Override
        public Object createTest() throws Exception {
            Object test = super.createTest();
            if (test instanceof MongoDBTestBase) {
                ((MongoDBTestBase) test)
                        .setUseStreamParser(useStreamDeserialisation);
                ((MongoDBTestBase) test)
                        .setUseStreamSerialiser(useStreamSerialisation);
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
