package pt.tecnico.sauron.silo.client;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class ReportIT extends BaseIT {
	
	// static members
	private static String validCamName = "Trabalho";
	private static String invalidCamNameLen = "Porta do Trabalho";
	private static double latitude = 5;
	private static double invalidLatitude = 5;
	private static double longitude = 10;
	private static double invalidLongitude = 10;
	private static Type car = Type.CAR, person = Type.PERSON, invalidType=Type.UNRECOGNIZED;
	private static String carIdentifier = "AB1234", personIdentifier = "345678", invalidIdentifier="";
	private static Timestamp t = new Timestamp(1000);
	private static com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(t.getTime()/1000).build();

	
	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){

		
		
	}

	@AfterAll
	public static void oneTimeTearDown() {
		
	}
	
	// initialization and clean-up for each test
	
	@BeforeEach
	public void setUp() {
		
	}
	
	@AfterEach
	public void tearDown() {
		
	}
		
	// tests 
	
	@Test
	public void validReportObservationData() {
		Observation carValidObs = Observation.newBuilder().setType(car).setIdentifier(carIdentifier).
				setName(validCamName).setLatitude(latitude).setLongitude(longitude).build();
		
		//report observation
		frontend.report(reportBuildRequest(carValidObs));
		
		Observation responseObs = frontend.track(trackBuildRequest(car, carIdentifier)).getObservation();
		
		assertEqualsObservation(carValidObs, responseObs);
	}
	
	@Test
	public void reportListObservationData() {
		Observation carValidObs = Observation.newBuilder().setType(car).setIdentifier(carIdentifier).
				setName(validCamName).setLatitude(latitude).setLongitude(longitude).build();
		
		Observation personValidObs = Observation.newBuilder().setType(person).setIdentifier(personIdentifier).
				setName(validCamName).setLatitude(latitude).setLongitude(longitude).build();

		List<Observation> allObs = new ArrayList<Observation>();
		allObs.add(carValidObs);
		allObs.add(personValidObs);
		
		//report observation
		frontend.report(reportBuildRequest(allObs));

		Observation responseCarObs = frontend.track(trackBuildRequest(car, carIdentifier)).getObservation();
		Observation responsePersonObs = frontend.track(trackBuildRequest(person, personIdentifier)).getObservation();

		assertEqualsObservation(carValidObs, responseCarObs);
		assertEqualsObservation(personValidObs, responsePersonObs);
	}
	

	@Test
	public void invalidIdentier() {
		Observation invalidIdentifierObs = Observation.newBuilder().setType(car).setIdentifier(invalidIdentifier).
				setName(validCamName).setLatitude(latitude).setLongitude(longitude).build();

		assertEquals(INVALID_ARGUMENT,
				assertThrows(StatusRuntimeException.class, () ->
						frontend.report(reportBuildRequest(invalidIdentifierObs))).getStatus().getCode());
	}

	@Test
	public void invalidCameraName() {
		Observation invalidCameraLenObs = Observation.newBuilder().setType(car).setIdentifier(invalidIdentifier).
				setName(invalidCamNameLen).setLatitude(latitude).setLongitude(longitude).build();


		assertEquals(INVALID_ARGUMENT,
				assertThrows(StatusRuntimeException.class, () ->
						frontend.report(reportBuildRequest(invalidCameraLenObs))).getStatus().getCode());
	}

	@Test
	public void invalidType() {
		Observation invalidTypeObs = Observation.newBuilder().setIdentifier(carIdentifier).setName(validCamName).
				setLatitude(latitude).setLongitude(longitude).build();

		assertEquals(INVALID_ARGUMENT,
				assertThrows(StatusRuntimeException.class, () ->
						frontend.report(reportBuildRequest(invalidTypeObs))).getStatus().getCode());
		
	}

	@Test
	public void invalidLatitude() {
		Observation InvalidLatitudeObs = Observation.newBuilder().setType(car).setIdentifier(invalidIdentifier).
				setName(validCamName).setLatitude(invalidLatitude).setLongitude(longitude).build();

		assertEquals(INVALID_ARGUMENT,
				assertThrows(StatusRuntimeException.class, () ->
						frontend.report(reportBuildRequest(InvalidLatitudeObs))).getStatus().getCode());
	}

	@Test
	public void invalidLongitude() {
		Observation InvalidLongitudeObs = Observation.newBuilder().setType(car).setIdentifier(invalidIdentifier).
				setName(validCamName).setLatitude(latitude).setLongitude(invalidLongitude).build();
		assertEquals(INVALID_ARGUMENT,
				assertThrows(StatusRuntimeException.class, () ->
						frontend.report(reportBuildRequest(InvalidLongitudeObs))).getStatus().getCode());
	}

	@Test
	public void emptyObservationsReport() {


	}
}
