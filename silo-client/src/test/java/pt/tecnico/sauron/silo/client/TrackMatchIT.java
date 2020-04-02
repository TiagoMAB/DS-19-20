package pt.tecnico.sauron.silo.client;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.CamJoinRequest;
import pt.tecnico.sauron.silo.grpc.Observation;
import pt.tecnico.sauron.silo.grpc.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Type;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TrackMatchIT extends BaseIT {

    private static String name = "Casa";
    private static double latitude = 5;
    private static double longitude = 10;
    private static Type type = Type.CAR;
    private static String identifier1 = "AB34CD", identifier2 = "AB1234";
    private static Timestamp t = new Timestamp(1000);
    private static com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(t.getTime()/1000).build();

    private static Observation validObs1 = Observation.newBuilder().setType(type).setIdentifier(identifier1).setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();
    private static Observation validObs2 = Observation.newBuilder().setType(type).setIdentifier(identifier2).setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();
    private static Observation invalidTypeObs = Observation.newBuilder().setIdentifier(identifier1).setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();

    private static String partialIdentifier1 = "AB*";
    private static String partialIdentifier2 = "*D";
    private static String invalidPartialIdentifier = "A*3*CD";
    private static String notFoundPartialIdentifier = "*B";

    private static List<Observation> observationsList1 = new ArrayList<Observation>();
    private static List<Observation> observationsList2 = new ArrayList<Observation>();

    @BeforeAll
    public static void oneTimeSetUp() {
        frontend.camJoin(CamJoinRequest.newBuilder().setName(name).setLatitude(latitude).setLongitude(longitude).build());

        frontend.report(ReportRequest.newBuilder().addObservations(validObs1).build());
        frontend.report(ReportRequest.newBuilder().addObservations(validObs2).build());

        observationsList1.add(validObs1);
        observationsList1.add(validObs2);
        observationsList2.add(validObs2);
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
    public void validObservationTest1() {
        assertEquals(observationsList1, frontend.trackMatch(trackMatchBuildRequest(type, partialIdentifier1)).getObservationsList());
    }

    @Test
    public void validObservationTest2() {
        assertEquals(observationsList2, frontend.trackMatch(trackMatchBuildRequest(type, partialIdentifier2)).getObservationsList());
    }

    @Test
    public void invalidTypeTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(trackMatchBuildRequest(invalidTypeObs.getType(), partialIdentifier1))).getStatus()
                        .getCode());
    }

    @Test
    public void invalidPartialIdentifierTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(trackMatchBuildRequest(type, invalidPartialIdentifier))).getStatus()
                        .getCode());
    }

    @Test
    public void observationNotFoundTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(trackMatchBuildRequest(type, notFoundPartialIdentifier))).getStatus()
                        .getCode());
    }
}
