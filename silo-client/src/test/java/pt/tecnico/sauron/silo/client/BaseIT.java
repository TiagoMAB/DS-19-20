package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import pt.tecnico.sauron.silo.grpc.*;

import java.io.IOException;
import java.util.Properties;


public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	static SiloFrontend frontend;

	@BeforeAll
	public static void oneTimeSetup () throws IOException {
		testProps = new Properties();
		
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(testProps);
		}catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		final String host = testProps.getProperty("server.host");
		final int port = Integer.parseInt(testProps.getProperty("server.port"));
		frontend = new SiloFrontend(host, port);
	}
	
	@AfterAll
	public static void cleanup() {
		
	}

	protected TrackRequest trackBuildRequest(Type t, String identifier) {
		return TrackRequest.newBuilder().setType(t).setIdentifier(identifier).build();
	}

	protected TrackMatchRequest trackMatchBuildRequest(Type t, String partialIdentifier) {
		return TrackMatchRequest.newBuilder().setType(t).setPartialIdentifier(partialIdentifier).build();
	}

	protected TraceRequest traceBuildRequest(Type t, String identifier) {
		return TraceRequest.newBuilder().setType(t).setIdentifier(identifier).build();
	}

	protected void assertEqualsObservation(Observation obs1, Observation obs2) {
		assertEquals(obs1.getType(), obs2.getType());
		assertEquals(obs1.getIdentifier(), obs2.getIdentifier());
		assertEquals(obs1.getName(), obs2.getName());
		assertEquals(obs1.getLatitude(), obs2.getLatitude());
		assertEquals(obs1.getLongitude(), obs2.getLongitude());
	}
}
