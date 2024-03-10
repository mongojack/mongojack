package org.junit.rules;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Exists so that we can exclude junit 4 from our dependencies and still use testcontainers.
 */
public interface TestRule {
    Statement apply(Statement base, Description description);
}
