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
import java.util.Collections;
import java.util.List;

public class TrackMatchIT extends BaseIT {

    private static String name = "Trabalho";
    private static double latitude = 5;
    private static double longitude = 10;
    private static Type type = Type.CAR;
    private static String identifier1 = "AB1234", identifier2 = "AB34CD";

    private static Observation validObs1 = Observation.newBuilder().setType(type).setIdentifier(identifier1).build();
    private static Observation validObs2 = Observation.newBuilder().setType(type).setIdentifier(identifier2).build();
    private static Observation invalidTypeObs = Observation.newBuilder().setIdentifier(identifier1).build();

    private static String partialIdentifier1 = "AB*";
    private static String partialIdentifier2 = "*D";
    private static String invalidPartialIdentifier = "A*3*CD";
    private static String notFoundPartialIdentifier = "*B";

    private static List<Observation> observationsList1 = new ArrayList<Observation>();
    private static List<Observation> observationsList2 = new ArrayList<Observation>();

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        frontend.camJoin(CamJoinRequest.newBuilder().setName(name).setLatitude(latitude).setLongitude(longitude).build());

        frontend.report(ReportRequest.newBuilder().setName(name).addObservations(validObs2).build());
        frontend.report(ReportRequest.newBuilder().setName(name).addObservations(validObs1).build());

        observationsList1.add(validObs1);
        observationsList1.add(validObs2);
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
        List<Observation> responseObsList = frontend.trackMatch(trackMatchBuildRequest(type, partialIdentifier1)).getObservationsList();
        assertEquals(responseObsList.size(), 2);
        assertEqualsObservation(observationsList1.get(0), responseObsList.get(0));
        assertEqualsObservation(observationsList1.get(1), responseObsList.get(1));

        assertEquals(name, responseObsList.get(0).getName());
        assertEquals(latitude, responseObsList.get(0).getLatitude());
        assertEquals(longitude, responseObsList.get(0).getLongitude());

        assertEquals(name, responseObsList.get(1).getName());
        assertEquals(latitude, responseObsList.get(1).getLatitude());
        assertEquals(longitude, responseObsList.get(1).getLongitude());
    }

    @Test
    public void validObservation2Test() throws Exception {
        List<Observation> responseObsList = frontend.trackMatch(trackMatchBuildRequest(type, partialIdentifier2)).getObservationsList();
        assertEquals(responseObsList.size(), 1);
        assertEqualsObservation(responseObsList.get(0), observationsList2.get(0));

        assertEquals(name, responseObsList.get(0).getName());
        assertEquals(latitude, responseObsList.get(0).getLatitude());
        assertEquals(longitude, responseObsList.get(0).getLongitude());
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
    public void observationNotFoundTest() throws Exception {
        List<Observation> responseObsList = frontend.trackMatch(trackMatchBuildRequest(type, notFoundPartialIdentifier)).getObservationsList();
        assertEquals(Collections.emptyList(), responseObsList);
    }
}
