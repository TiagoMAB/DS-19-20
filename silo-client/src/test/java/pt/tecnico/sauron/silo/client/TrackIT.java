package pt.tecnico.sauron.silo.client;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import org.junit.jupiter.api.*;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.*;

import java.sql.Timestamp;

public class TrackIT extends BaseIT {

    private static String name = "Casa";
    private static double latitude = 5;
    private static double longitude = 10;
    private static Type type = Type.PERSON;
    private static String identifier = "123456";

    private static String notFoundIdentifier = "111111";

    private static Observation validObs = Observation.newBuilder().setType(type).setIdentifier(identifier).build();
    private static Observation invalidTypeObs = Observation.newBuilder().setIdentifier(identifier).build();


    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        frontend.camJoin(CamJoinRequest.newBuilder().setName(name).setLatitude(latitude).setLongitude(longitude).build());

        frontend.report(ReportRequest.newBuilder().setName(name).addObservations(validObs).build());
    }

    @AfterAll
    public static void oneTimeTearDown() throws Exception {
        frontend.ctrlClear(CtrlClearRequest.newBuilder().build());
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void clear() {
    }

    @Test
    public void validObservationTest() throws Exception {
        Observation responseObs = frontend.track(trackBuildRequest(type, identifier)).getObservation();
        assertEqualsObservation(validObs, responseObs);
        assertEquals(name, responseObs.getName());
        assertEquals(latitude, responseObs.getLatitude());
        assertEquals(longitude, responseObs.getLongitude());
    }

    @Test
    public void invalidTypeTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.track(trackBuildRequest(invalidTypeObs.getType(), identifier))).getStatus()
                        .getCode());
    }

    @Test
    public void observationNotFoundTest() throws Exception {
        Observation responseObs = frontend.track(trackBuildRequest(type, notFoundIdentifier)).getObservation();
        assertEquals(Observation.getDefaultInstance(), responseObs);
    }
}
