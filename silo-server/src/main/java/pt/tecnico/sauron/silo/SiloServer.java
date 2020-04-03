package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.Object;
import pt.tecnico.sauron.silo.domain.Silo;
import pt.tecnico.sauron.silo.grpc.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static io.grpc.Status.INVALID_ARGUMENT;

public class SiloServer extends SiloGrpc.SiloImplBase {

    private static final Logger LOGGER = Logger.getLogger(SiloServer.class.getName());

    private final Silo silo = new Silo();

    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver) {
        try{        
            //check name
            LOGGER.info("camJoin()...");
            LOGGER.info("Received name: " + request.getName() + " received latitude: " + request.getLatitude() + " received longitude: " + request.getLongitude());

            silo.camJoin(request.getName(), request.getLatitude(), request.getLongitude());
            responseObserver.onNext(CamJoinResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
        catch (Exception e){
            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {
        try {
            //get camera Info
            Camera camera = silo.camInfo(request.getName());
            double latitude = camera.getLatitude();
            double longitude = camera.getLongitude();
            
            responseObserver.onNext(CamInfoResponse.newBuilder().setLatitude(latitude).build());
            responseObserver.onNext(CamInfoResponse.newBuilder().setLongitude(longitude).build());
            responseObserver.onCompleted();
        }
        catch (Exception e){
            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {
        LOGGER.info("report()...");
        String n = request.getName();

        try {
            // Calls camInfo to get camera information for camera with name n, throws exception if camera with that name doesn't exist
            Camera camera = silo.camInfo(n);

            List<Observation> ol = request.getObservationsList();

            List<pt.tecnico.sauron.silo.domain.Observation> observationsList = new ArrayList<pt.tecnico.sauron.silo.domain.Observation>();

            LOGGER.info("Received name: " + n);
            LOGGER.info("Observations List: " + ol.toString());

            for (int i = 0; i < ol.size(); i++) {
                Observation o = ol.get(i);
                LOGGER.info("Received Observation " + i + " Object type: " + o.getType() + " Object identifier: " + o.getIdentifier());

                Object obj = new Object(o.getType().ordinal(), o.getIdentifier());
                Timestamp time = new Timestamp(System.currentTimeMillis());

                pt.tecnico.sauron.silo.domain.Observation observation = new pt.tecnico.sauron.silo.domain.Observation(obj, time, camera);
                observationsList.add(observation);
            }
            silo.report(observationsList);

            responseObserver.onNext(ReportResponse.newBuilder().build());
            responseObserver.onCompleted();
            LOGGER.info("Sent response");
        }
        catch (Exception e) {
            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void track(TrackRequest request, StreamObserver<TrackResponse> responseObserver) {               //TODO: Validation/Verification of arguments
        LOGGER.info("track()...");

        Type t = request.getType();
        String i = request.getIdentifier();

        LOGGER.info("Received type: " + t + " | identifier: " + i);

        try {
            //Calls track and gets the observation asked (if one was found)
            pt.tecnico.sauron.silo.domain.Observation o = silo.track(Object.findType(t.getNumber()), i);

            //Gets camera information from observation
            String name = o.getCamera().getName();
            double latitude = o.getCamera().getLatitude();
            double longitude = o.getCamera().getLongitude();

            //Converts java.sql.timestamp to protobuf.timestamp
            Timestamp timestamp = o.getTimestamp();
            Long milliseconds = timestamp.getTime();
            com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(milliseconds/1000).build();

            //Converts internal representation of observation to a data transfer object
            Observation obs = Observation.newBuilder().setType(t).setIdentifier(i).setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();

            //Signals that the response was built successfully
            responseObserver.onNext(TrackResponse.newBuilder().setObservation(obs).build());
            responseObserver.onCompleted();
            LOGGER.info("Sent Observation(type: " + t + " | identifier: " + i + "ts: " + timestamp.toString());
        }
        catch (Exception e) {
            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }

    }

    @Override
    public void trackMatch(TrackMatchRequest request, StreamObserver<TrackMatchResponse> responseObserver) {
        LOGGER.info("trackMatch()...");

        Type t = request.getType();
        String i = request.getPartialIdentifier();

        LOGGER.info("Received type: " + t + " | identifier: " + i);

        List<pt.tecnico.sauron.silo.domain.Observation> observations = new ArrayList<>();
        TrackMatchResponse.Builder response = TrackMatchResponse.newBuilder();

        try {
            //Calls trackMatch and gets the list of observations asked (if one was found)
            observations = silo.trackMatch(Object.findType(t.getNumber()), i);

            for (pt.tecnico.sauron.silo.domain.Observation o: observations) {

                //Gets camera information from observation
                String name = o.getCamera().getName();
                double latitude = o.getCamera().getLatitude();
                double longitude = o.getCamera().getLongitude();

                //Converts java.sql.timestamp to protobuf.timestamp
                Timestamp timestamp = o.getTimestamp();
                Long milliseconds = timestamp.getTime();
                com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(milliseconds/1000).build();

                //Converts internal representation of observation to a data transfer object
                Observation obs = Observation.newBuilder().setType(t).setIdentifier(o.getObject().getIdentifier()).setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();

                //Adds observation (dto) to list of observations to be sent
                response.addObservations(obs);
                LOGGER.info("Sent Observation(type: " + t + " | identifier: " + i + "ts: " + timestamp.toString());

            }
        }
        catch (Exception e) {
            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }

        //Signals that the response was built successfully
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void trace(TraceRequest request, StreamObserver<TraceResponse> responseObserver) {
        LOGGER.info("trace()...");

        Type t = request.getType();
        String i = request.getIdentifier();

        LOGGER.info("Received type: " + t + " | identifier: " + i);

        List<pt.tecnico.sauron.silo.domain.Observation> observations = new ArrayList<pt.tecnico.sauron.silo.domain.Observation>();
        TraceResponse.Builder response = TraceResponse.newBuilder();

        try {
            //Calls trace and gets the list of observations asked (if one was found)
            observations = silo.trace(Object.findType(t.getNumber()), i);

            for (pt.tecnico.sauron.silo.domain.Observation o: observations) {

                //Gets camera information from observation
                String name = o.getCamera().getName();
                double latitude = o.getCamera().getLatitude();
                double longitude = o.getCamera().getLongitude();

                //Converts java.sql.timestamp to protobuf.timestamp
                Timestamp timestamp = o.getTimestamp();
                Long milliseconds = timestamp.getTime();
                com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(milliseconds/1000).build();

                //Converts internal representation of observation to a data transfer object
                Observation obs = Observation.newBuilder().setType(t).setIdentifier(i).setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();

                //Adds observation (dto) to list of observations to be sent
                response.addObservations(obs);
                LOGGER.info("Sent Observation(type: " + t + " | identifier: " + i + "ts: " + timestamp.toString());
            }
        }
        catch (Exception e) {
            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlPing(CtrlPingRequest request, StreamObserver<CtrlPingResponse> responseObserver) {

        String input = request.getInputText();
        String output = "Hello " + input + "!";
        CtrlPingResponse response = CtrlPingResponse.newBuilder().setOutputText(output).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(CtrlClearRequest request, StreamObserver<CtrlClearResponse> responseObserver) {
        CtrlClearResponse.Builder response = CtrlClearResponse.newBuilder();

        try {
            silo.clear();
        }
        catch (Exception e) {
            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlInit(CtrlInitRequest request, StreamObserver<CtrlInitResponse> responseObserver) {
        super.ctrlInit(request, responseObserver);
    }

}