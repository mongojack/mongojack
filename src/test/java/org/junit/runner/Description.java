package org.junit.runner;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exists so that we can exclude junit 4 from our dependencies and still use testcontainers.
 */
public class Description implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Pattern METHOD_AND_CLASS_NAME_PATTERN = Pattern
            .compile("([\\s\\S]*)\\((.*)\\)");

    public static Description createSuiteDescription(String name, Annotation... annotations) {
        return new Description(null, name, annotations);
    }

    public static Description createSuiteDescription(String name, Serializable uniqueId, Annotation... annotations) {
        return new Description(null, name, uniqueId, annotations);
    }

    public static Description createTestDescription(String className, String name, Annotation... annotations) {
        return new Description(null, formatDisplayName(name, className), annotations);
    }

    public static Description createTestDescription(Class<?> clazz, String name, Annotation... annotations) {
        return new Description(clazz, formatDisplayName(name, clazz.getName()), annotations);
    }

    public static Description createTestDescription(Class<?> clazz, String name) {
        return new Description(clazz, formatDisplayName(name, clazz.getName()));
    }

    public static Description createTestDescription(String className, String name, Serializable uniqueId) {
        return new Description(null, formatDisplayName(name, className), uniqueId);
    }

    private static String formatDisplayName(String name, String className) {
        return String.format("%s(%s)", name, className);
    }

    public static Description createSuiteDescription(Class<?> testClass) {
        return new Description(testClass, testClass.getName(), testClass.getAnnotations());
    }

    public static Description createSuiteDescription(Class<?> testClass, Annotation... annotations) {
        return new Description(testClass, testClass.getName(), annotations);
    }

    public static final Description EMPTY = new Description(null, "No Tests");

    public static final Description TEST_MECHANISM = new Description(null, "Test mechanism");

    private final Collection<Description> fChildren = new ConcurrentLinkedQueue<Description>();
    private final String fDisplayName;
    private final Serializable fUniqueId;
    private final Annotation[] fAnnotations;
    private volatile /* write-once */ Class<?> fTestClass;

    private Description(Class<?> clazz, String displayName, Annotation... annotations) {
        this(clazz, displayName, displayName, annotations);
    }

    private Description(Class<?> testClass, String displayName, Serializable uniqueId, Annotation... annotations) {
        if ((displayName == null) || (displayName.length() == 0)) {
            throw new IllegalArgumentException(
                    "The display name must not be empty.");
        }
        if ((uniqueId == null)) {
            throw new IllegalArgumentException(
                    "The unique id must not be null.");
        }
        this.fTestClass = testClass;
        this.fDisplayName = displayName;
        this.fUniqueId = uniqueId;
        this.fAnnotations = annotations;
    }

    /**
     * @return a user-understandable label
     */
    public String getDisplayName() {
        return fDisplayName;
    }

    /**
     * Add <code>Description</code> as a child of the receiver.
     *
     * @param description the soon-to-be child.
     */
    public void addChild(Description description) {
        fChildren.add(description);
    }

    /**
     * Gets the copy of the children of this {@code Description}.
     * Returns an empty list if there are no children.
     */
    public ArrayList<Description> getChildren() {
        return new ArrayList<Description>(fChildren);
    }

    /**
     * @return <code>true</code> if the receiver is a suite
     */
    public boolean isSuite() {
        return !isTest();
    }

    /**
     * @return <code>true</code> if the receiver is an atomic test
     */
    public boolean isTest() {
        return fChildren.isEmpty();
    }

    /**
     * @return the total number of atomic tests in the receiver
     */
    public int testCount() {
        if (isTest()) {
            return 1;
        }
        int result = 0;
        for (Description child : fChildren) {
            result += child.testCount();
        }
        return result;
    }

    @Override
    public int hashCode() {
        return fUniqueId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Description)) {
            return false;
        }
        Description d = (Description) obj;
        return fUniqueId.equals(d.fUniqueId);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * @return true if this is a description of a Runner that runs no tests
     */
    public boolean isEmpty() {
        return equals(EMPTY);
    }

    /**
     * @return a copy of this description, with no children (on the assumption that some of the
     *         children will be added back)
     */
    public Description childlessCopy() {
        return new Description(fTestClass, fDisplayName, fAnnotations);
    }

    /**
     * @return the annotation of type annotationType that is attached to this description node,
     *         or null if none exists
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation each : fAnnotations) {
            if (each.annotationType().equals(annotationType)) {
                return annotationType.cast(each);
            }
        }
        return null;
    }

    /**
     * @return all of the annotations attached to this description node
     */
    public Collection<Annotation> getAnnotations() {
        return Arrays.asList(fAnnotations);
    }

    /**
     * @return If this describes a method invocation,
     *         the class of the test instance.
     */
    public Class<?> getTestClass() {
        if (fTestClass != null) {
            return fTestClass;
        }
        String name = getClassName();
        if (name == null) {
            return null;
        }
        try {
            fTestClass = Class.forName(name, false, getClass().getClassLoader());
            return fTestClass;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * @return If this describes a method invocation,
     *         the name of the class of the test instance
     */
    public String getClassName() {
        return fTestClass != null ? fTestClass.getName() : methodAndClassNamePatternGroupOrDefault(2, toString());
    }

    /**
     * @return If this describes a method invocation,
     *         the name of the method (or null if not)
     */
    public String getMethodName() {
        return methodAndClassNamePatternGroupOrDefault(1, null);
    }

    private String methodAndClassNamePatternGroupOrDefault(int group,
            String defaultString) {
        Matcher matcher = METHOD_AND_CLASS_NAME_PATTERN.matcher(toString());
        return matcher.matches() ? matcher.group(group) : defaultString;
    }
}