package pt.tecnico.sauron.silo.client;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.*;


public class CamInfoIT extends BaseIT {


    private static String validName = "Alameda";
    private static String validName2 = "AlamedaIST";
    private static double validLatitude = 38.737000;
    private static double validLongitude = -9.136596;
    private static CamInfoResponse validResponse = CamInfoResponse.newBuilder().setLatitude(validLatitude).setLongitude(validLongitude).build();

    private static String invalidName1 = "";


    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        frontend.camJoin(CamJoinRequest.newBuilder().setName(validName).setLatitude(validLatitude).setLongitude(validLongitude).build());
    }

    @AfterAll
    public static void oneTimeTearDown() throws Exception { frontend.ctrlClear(CtrlClearRequest.newBuilder().build()); }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void clear() {
    }

    @Test
    public void validCameraInfoTest() throws Exception {
        CamInfoResponse response = frontend.camInfo(camInfoBuildRequest(validName));
        assertEquals(response, validResponse);
    }

    @Test
    public void NoCameraFoundTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () ->  frontend.camInfo(camInfoBuildRequest(validName2))).getStatus()
                        .getCode());
    }

    @Test
    public void BlankNameTest() {
        assertEquals(INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () ->  frontend.camInfo(camInfoBuildRequest(invalidName1))).getStatus()
                        .getCode());
    }

}
