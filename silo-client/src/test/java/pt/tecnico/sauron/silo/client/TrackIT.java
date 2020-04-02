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
    private static Timestamp t = new Timestamp(1000);
    private static com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(t.getTime()/1000).build();

    private static String notFoundIdentifier = "111111";

    private static Observation validObs = Observation.newBuilder().setType(type).setIdentifier(identifier).setName(name).setLatitude(latitude).setLongitude(longitude).build();
    private static Observation invalidTypeObs = Observation.newBuilder().setIdentifier(identifier).setName(name).setLatitude(latitude).setLongitude(longitude).build();


    @BeforeAll
    public static void oneTimeSetUp() {
        frontend.camJoin(CamJoinRequest.newBuilder().setName(name).setLatitude(latitude).setLongitude(longitude).build());

        frontend.report(ReportRequest.newBuilder().addObservations(validObs).build());
    }

    @AfterAll
    public static void oneTimeTearDown() {
        frontend.ctrlClear(CtrlClearRequest.newBuilder().build());
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void clear() {
    }

    @Test
    public void validObservationTest() {
        Observation responseObs = frontend.track(trackBuildRequest(type, identifier)).getObservation();
        assertEqualsObservation(validObs, responseObs);
    }

    @Test
    public void invalidTypeTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.track(trackBuildRequest(invalidTypeObs.getType(), identifier))).getStatus()
                        .getCode());
    }

    @Test
    public void observationNotFoundTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.track(trackBuildRequest(type, notFoundIdentifier))).getStatus()
                        .getCode());
    }
}
