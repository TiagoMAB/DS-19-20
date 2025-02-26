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

    private static String notFoundIdentifier = "111111";

    private static Observation validObs1 = Observation.newBuilder().setType(type).setIdentifier(identifier1).build();
    private static Observation validObs2 = Observation.newBuilder().setType(type).setIdentifier(identifier2).build();
    private static Observation invalidTypeObs = Observation.newBuilder().setIdentifier(identifier1).build();

    private static List<Observation> observationsList1 = new ArrayList<Observation>();
    private static List<Observation> observationsList2 = new ArrayList<Observation>();

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        frontend.camJoin(CamJoinRequest.newBuilder().setName(name1).setLatitude(latitude1).setLongitude(longitude1).build());
        frontend.camJoin(CamJoinRequest.newBuilder().setName(name2).setLatitude(latitude2).setLongitude(longitude2).build());

        frontend.report(ReportRequest.newBuilder().setName(name1).addObservations(validObs1).build());
        frontend.report(ReportRequest.newBuilder().setName(name2).addObservations(validObs1).build());
        frontend.report(ReportRequest.newBuilder().setName(name2).addObservations(validObs2).build());

        observationsList1.add(validObs1);
        observationsList1.add(validObs1);
        observationsList2.add(validObs2);
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
    public void validObservation1Test() throws Exception {
        List<Observation> responseObsList = frontend.trace(traceBuildRequest(type, identifier1)).getObservationsList();
        assertEquals(responseObsList.size(), 2);
        assertEqualsObservation(observationsList1.get(0), responseObsList.get(0));
        assertEqualsObservation(observationsList1.get(1), responseObsList.get(1));

        assertEquals(name2, responseObsList.get(0).getName());
        assertEquals(latitude2, responseObsList.get(0).getLatitude());
        assertEquals(longitude2, responseObsList.get(0).getLongitude());

        assertEquals(name1, responseObsList.get(1).getName());
        assertEquals(latitude1, responseObsList.get(1).getLatitude());
        assertEquals(longitude1, responseObsList.get(1).getLongitude());
    }

    @Test
    public void validObservation2Test() throws Exception {
        List<Observation> responseObsList = frontend.trace(traceBuildRequest(type, identifier2)).getObservationsList();
        assertEquals(responseObsList.size(), 1);
        assertEqualsObservation(responseObsList.get(0), observationsList2.get(0));

        assertEquals(name2, responseObsList.get(0).getName());
        assertEquals(latitude2, responseObsList.get(0).getLatitude());
        assertEquals(longitude2, responseObsList.get(0).getLongitude());
    }

    @Test
    public void invalidTypeTest() throws Exception {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.trace(traceBuildRequest(invalidTypeObs.getType(), identifier1))).getStatus()
                        .getCode());
    }

    @Test
    public void observationNotFoundTest() throws Exception {
        List<Observation> responseObsList = frontend.trace(traceBuildRequest(type, notFoundIdentifier)).getObservationsList();
        assertEquals(Collections.emptyList(), responseObsList);
    }
}
