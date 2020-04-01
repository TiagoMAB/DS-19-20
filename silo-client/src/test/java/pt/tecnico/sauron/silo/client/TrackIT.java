package pt.tecnico.sauron.silo.client;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import org.junit.jupiter.api.*;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.CamJoinRequest;
import pt.tecnico.sauron.silo.grpc.Observation;
import pt.tecnico.sauron.silo.grpc.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Type;

import java.sql.Timestamp;

public class TrackIT extends BaseIT {

    private static String name = "Casa";
    private static double latitude = 5;
    private static double longitude = 10;
    private static Type type = Type.PERSON;
    private static String identifier = "123456";
    private static Timestamp t = new Timestamp(1000);
    private static com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(t.getTime()/1000).build();

    private static Observation validObs = Observation.newBuilder().setType(type).setIdentifier(identifier).setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();
    private static Observation invalidTypeObs = Observation.newBuilder().setIdentifier(identifier).setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();
    private static Observation notFoundObs = Observation.newBuilder().setType(type).setIdentifier("111111").setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();


    @BeforeAll
    public static void oneTimeSetUp() {
        frontend.camJoin(CamJoinRequest.newBuilder().setName(name).setLatitude(latitude).setLongitude(longitude).build());

        frontend.report(ReportRequest.newBuilder().addObservations(validObs).build());
    }

    @AfterAll
    public static void oneTimeTearDown() { }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void clear() {
    }

    @Test
    public void validObservationTest() {
        assertEquals(validObs, frontend.track(trackBuildRequest(validObs.getType(), validObs.getName())).getObservation());
    }

    @Test
    public void invalidTypeObservationTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.track(trackBuildRequest(invalidTypeObs.getType(), invalidTypeObs.getName()))).getStatus()
                        .getCode());
    }

    @Test
    public void notFoundObservationTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.track(trackBuildRequest(notFoundObs.getType(), notFoundObs.getName()))).getStatus()
                        .getCode());
    }
}
