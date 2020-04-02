package pt.tecnico.sauron.silo.client;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TraceIT extends BaseIT {

    private static String name1 = "Trabalho";
    private static double latitude1 = 5;
    private static double longitude1 = 10;
    private static String name2 = "Casa";
    private static double latitude2 = 30;
    private static double longitude2 = 50;
    private static Type type = Type.PERSON;
    private static String identifier1 = "123456";
    private static String identifier2 = "345678";
    private static Timestamp t = new Timestamp(1000);
    private static com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(t.getTime()/1000).build();

    private static String notFoundIdentifier = "111111";

    private static Observation validObs1 = Observation.newBuilder().setType(type).setIdentifier(identifier1).setName(name1).setLatitude(latitude1).setLongitude(longitude1).build();
    private static Observation validObs2 = Observation.newBuilder().setType(type).setIdentifier(identifier1).setName(name2).setLatitude(latitude2).setLongitude(longitude2).build();
    private static Observation validObs3 = Observation.newBuilder().setType(type).setIdentifier(identifier2).setName(name2).setLatitude(latitude2).setLongitude(longitude2).build();
    private static Observation invalidTypeObs = Observation.newBuilder().setIdentifier(identifier1).setName(name1).setLatitude(latitude1).setLongitude(longitude1).build();

    private static List<Observation> observationsList1 = new ArrayList<Observation>();
    private static List<Observation> observationsList2 = new ArrayList<Observation>();

    @BeforeAll
    public static void oneTimeSetUp() {
        frontend.camJoin(CamJoinRequest.newBuilder().setName(name1).setLatitude(latitude1).setLongitude(longitude1).build());
        frontend.camJoin(CamJoinRequest.newBuilder().setName(name2).setLatitude(latitude2).setLongitude(longitude2).build());

        frontend.report(ReportRequest.newBuilder().addObservations(validObs1).build());
        frontend.report(ReportRequest.newBuilder().addObservations(validObs2).build());
        frontend.report(ReportRequest.newBuilder().addObservations(validObs3).build());

        observationsList1.add(validObs2);
        observationsList1.add(validObs1);
        observationsList2.add(validObs3);
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
    public void validObservationTest1() {
        List<Observation> responseObsList = frontend.trace(traceBuildRequest(type, identifier1)).getObservationsList();
        assertEquals(responseObsList.size(), 2);
        assertEqualsObservation(observationsList1.get(0), responseObsList.get(0));
        assertEqualsObservation(observationsList1.get(1), responseObsList.get(1));
    }

    @Test
    public void validObservationTest2() {
        List<Observation> responseObsList = frontend.trace(traceBuildRequest(type, identifier2)).getObservationsList();
        assertEquals(responseObsList.size(), 1);
        assertEqualsObservation(responseObsList.get(0), observationsList2.get(0));
    }

    @Test
    public void invalidTypeTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.trace(traceBuildRequest(invalidTypeObs.getType(), identifier1))).getStatus()
                        .getCode());
    }

    @Test
    public void observationNotFoundTest() {
        List<Observation> responseObsList = frontend.trace(traceBuildRequest(type, notFoundIdentifier)).getObservationsList();
        assertEquals(Collections.emptyList(), responseObsList);
    }
}
