package pt.tecnico.sauron.silo.client;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import org.junit.jupiter.api.*;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.*;

public class CamJoinIT extends BaseIT {

    private static String validName = "Alameda";
    private static String validName2 = "AlamedaIST";
    private static double validLatitude = 38.737000;
    private static double validLongitude = -9.136596;
    private static CamInfoResponse validresponse = CamInfoResponse.newBuilder().setLatitude(validLatitude).setLongitude(validLongitude).build();

    private static String invalidName1 = "AL";
    private static String invalidName2 = "AlamedaCameraPlus";
    private static String invalidName3 = "Alame*da";
    private static double invalidLatitude = 98.737000;
    private static double invalidLongitude = -200.136596;


    @BeforeAll
    public static void oneTimeSetUp() {
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
    public void validCameraTest() {
        frontend.camJoin(camJoinBuildRequest(validName, validLatitude, validLongitude));
        CamInfoResponse response = frontend.camInfo(camInfoBuildRequest(validName));

        assertEquals(response, validresponse);
    }

    @Test
    public void invalidNameTest1() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () ->  frontend.camJoin(camJoinBuildRequest(invalidName1, validLatitude, validLongitude))).getStatus()
                        .getCode());
    }

    @Test
    public void invalidNameTest2() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () ->  frontend.camJoin(camJoinBuildRequest(invalidName2, validLatitude, validLongitude))).getStatus()
                        .getCode());
    }

    @Test
    public void invalidNameTest3() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () ->  frontend.camJoin(camJoinBuildRequest(invalidName3, validLatitude, validLongitude))).getStatus()
                        .getCode());
    }

    @Test
    public void invalidLatitudeTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () ->  frontend.camJoin(camJoinBuildRequest(validName2, invalidLatitude, validLongitude))).getStatus()
                        .getCode());
    }

    @Test
    public void invalidLongitudeTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () ->  frontend.camJoin(camJoinBuildRequest(validName2, validLatitude, invalidLongitude))).getStatus()
                        .getCode());
    }

    @Test
    public void duplicateCameraTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () ->  frontend.camJoin(camJoinBuildRequest(validName, validLatitude, validLongitude))).getStatus()
                        .getCode());
    }
}