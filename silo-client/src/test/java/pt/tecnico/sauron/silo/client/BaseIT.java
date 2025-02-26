package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import pt.tecnico.sauron.silo.grpc.*;

import java.io.IOException;
import java.util.List;
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
		final String port = testProps.getProperty("server.port");
		try {
			frontend = new SiloFrontend(host, port, 0);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
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

	protected ReportRequest reportBuildRequest(String name, Observation o) {
		return ReportRequest.newBuilder().setName(name).addObservations(o).build();
	}
	
	protected ReportRequest reportBuildRequest(String name, List<Observation> all_os) {
		return ReportRequest.newBuilder().setName(name).addAllObservations(all_os).build();
	}

	protected CamInfoRequest camInfoBuildRequest(String name) {
		return CamInfoRequest.newBuilder().setName(name).build();
	}

	protected CamJoinRequest camJoinBuildRequest(String name, double latitude, double longitude) {
		return CamJoinRequest.newBuilder().setName(name).setLatitude(latitude).setLongitude(longitude).build();
	}

	protected void assertEqualsObservation(Observation obs1, Observation obs2) {
		assertEquals(obs1.getType(), obs2.getType());
		assertEquals(obs1.getIdentifier(), obs2.getIdentifier());
	}
	
}
