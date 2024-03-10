package org.junit.runners.model;


/**
 * Exists so that we can exclude junit 4 from our dependencies and still use testcontainers.
 */
public abstract class Statement {
    public abstract void evaluate() throws Throwable;
}