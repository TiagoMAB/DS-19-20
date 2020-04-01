package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.Object;
import pt.tecnico.sauron.silo.domain.Silo;
import pt.tecnico.sauron.silo.grpc.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static io.grpc.Status.INVALID_ARGUMENT;

public class SiloServer extends SiloGrpc.SiloImplBase {

    private static final Logger LOGGER = Logger.getLogger(SiloServer.class.getName());

    private final Silo silo = new Silo();

    @Override
    public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {
        super.camInfo(request, responseObserver);
    }

    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver) {
        super.camJoin(request, responseObserver);
    }

    public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) { //TODO: Validation/Verification of arguments
        LOGGER.info("report()...");

        //TODO: check the use of n, if camera already exists IMPORTANT
        String n = request.getName();
        List<Observation> ol = request.getObservationsList();

        List<pt.tecnico.sauron.silo.domain.Observation> observationsList = new ArrayList<pt.tecnico.sauron.silo.domain.Observation>();

        LOGGER.info("Received name: " + n);

        for (int i = 0; i < ol.size(); i++) {
            Observation o = ol.get(i);
            LOGGER.info("Received Observation " + i + " Object type: " + o.getType() + " Object identifier: " + o.getIdentifier() + " Time: " + o.getDate() +
                    " Camera name: " + o.getName() + " Camera latitude: " + o.getLatitude() + " Camera longitude: " + o.getLongitude());

            try {
                Object obj = new Object(o.getType().ordinal(), o.getIdentifier());
                Timestamp time = new Timestamp(System.currentTimeMillis());   // TODO: check if time calculation is correct
                Camera camera = new Camera(o.getName(), o.getLatitude(), o.getLongitude());

                pt.tecnico.sauron.silo.domain.Observation observation = new pt.tecnico.sauron.silo.domain.Observation(obj, time, camera);
                observationsList.add(observation);
            }
            catch (Exception e) {
                LOGGER.info(e.getMessage());
                responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            }
        }

        try {
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

        try {/*
            Timestamp date = silo.track(t.getNumber(), i);
            Long milliseconds = date.getTime();
            com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(milliseconds/1000).build();
            Observation obs = Observation.newBuilder().setType(t).setIdentifier(i).setDate(ts).build();
            responseObserver.onNext(TrackResponse.newBuilder().setObservation(obs).build());
            responseObserver.onCompleted();
            LOGGER.info("Sent Observation(type: " + t + " | identifier: " + i + "ts: " + date.toString());*/
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
        try {
            observations = silo.trackMatch(t.getNumber(), i);
        }
        catch (Exception e) {
            //LOGGER.info(e.getMessage());
            //responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }

        TrackMatchResponse.Builder response = TrackMatchResponse.newBuilder();
        LOGGER.info("Response built ");
        for (pt.tecnico.sauron.silo.domain.Observation o: observations) {
            Timestamp date = o.getTime();
            Long milliseconds = date.getTime();
            com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(milliseconds/1000).build();

            Observation obs = Observation.newBuilder().setType(t).setIdentifier(o.getObject().getIdentifier()).setDate(ts).build();
            response.addObservations(obs);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void trace(TraceRequest request, StreamObserver<TraceResponse> responseObserver) {               //TODO: Validation/Verification of arguments
        LOGGER.info("trace()...");

        Type t = request.getType();
        String i = request.getIdentifier();

        LOGGER.info("Received type: " + t + " | identifier: " + i);

        List<pt.tecnico.sauron.silo.domain.Observation> observations = new ArrayList<pt.tecnico.sauron.silo.domain.Observation>();
        try {
            observations = silo.trace(t.getNumber(), i);
        }
        catch (Exception e) {
            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }

        TraceResponse.Builder response = TraceResponse.newBuilder();

        for (pt.tecnico.sauron.silo.domain.Observation o: observations) {
            Timestamp date = o.getTime();
            Long milliseconds = date.getTime();
            com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(milliseconds/1000).build();

            Observation obs = Observation.newBuilder().setType(t).setIdentifier(i).setDate(ts).build();
            response.addObservations(obs);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlPing(CtrlPingRequest request, StreamObserver<CtrlPingResponse> responseObserver) {
        super.ctrlPing(request, responseObserver);
    }

    @Override
    public void ctrlClear(CtrlClearRequest request, StreamObserver<CtrlClearResponse> responseObserver) {
        super.ctrlClear(request, responseObserver);
    }

    @Override
    public void ctrlInit(CtrlInitRequest request, StreamObserver<CtrlInitResponse> responseObserver) {
        super.ctrlInit(request, responseObserver);
    }

}