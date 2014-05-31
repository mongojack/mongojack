package org.mongojack;

import java.net.UnknownHostException;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.mongodb.MongoClient;

/**
 * 
 * This rule will start the mongo server before a test starts and ends the mongo
 * server when the test has finished.
 * 
 * <pre>
 * import static org.junit.Assert.*;
 * import org.junit.Rule;
 * import org.junit.Test;
 * 
 * import com.mongodb.MongoClient;
 * 
 * public class MongoRuleTest {
 * 
 *   {@literal @}Rule
 *   // this will start and stop mongodb for every test
 *   public MongoRule mongo = new MongoRule();
 * 
 *   {@literal @}Test
 *   public void testSomethingWithMongoRule() {
 * 
 *     MongoClient client = mongo.getMongoClient();
 *     assertNotNull(client);
 *     // whatever needs to be done in mongo
 * 
 *   }
 * 
 *   {@literal @}Test
 *   {@literal @}WithoutMongo
 *   public void testSomethingWithoutMongoRule() {
 * 
 *     MongoClient client = mongo.getMongoClient();
 *     assertNull(client);
 *     // whatever needs to be done without mongo
 * 
 *   }
 * 
 * }
 * </pre>
 * 
 * @author Niels Bertram
 * 
 */
public class MongoRule implements TestRule {

	private final MongoDBTestBase testCase;

	public MongoRule() {
		this(new MongoDBTestBase() {
		});
	}

	public MongoRule(MongoDBTestBase testCase) {
		this.testCase = testCase;
	}

	@Override
	public Statement apply(final Statement base, Description description) {

		if (description.getAnnotation(WithoutMongo.class) != null) // skip.
		{
			return base;
		}
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				testCase.startMongo();
				before();
				try {
					base.evaluate();
				} finally {
					after();
					testCase.shutdownMongo();
				}
			}
		};

	}

	/**
	 * May be overridden in the implementation to do stuff <em>after</em> the
	 * embedded test case is set up but <em>before</em> the current test is
	 * actually run.
	 * 
	 * @throws Throwable
	 */
	protected void before() throws Throwable {

	}

	/**
	 * May be overridden in the implementation to do stuff after the current
	 * test was run but <em>before<em> the mongo server is shutdown.
	 */
	protected void after() {

	}

	/**
	 * 
	 * @return the host name that was used to start the mongo server
	 * 
	 * @throws UnknownHostException
	 */
	public String getMongoHost() throws UnknownHostException {
		return testCase.getMongoHost();
	}

	/**
	 * @return the host port that was used to start the mongo server
	 */
	public int getMongoPort() {
		return testCase.getMongoPort();
	}

	/**
	 * Force the rule to execute mongod on the default port
	 */
	public MongoRule forceDefaultPort() {
		this.testCase.setForceDefaultPort(true);
		return this;
	}

	/**
	 * @return a configured mongo client that can connect to the embedded server
	 *         managed by the rule
	 * 
	 * @throws UnknownHostException
	 */
	public MongoClient getMongoClient() throws UnknownHostException {
		return this.testCase.getMongoClient();
	}

}
