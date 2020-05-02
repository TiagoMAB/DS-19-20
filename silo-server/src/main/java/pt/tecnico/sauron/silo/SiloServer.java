package pt.tecnico.sauron.silo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.Object;
import pt.tecnico.sauron.silo.domain.Silo;
import pt.tecnico.sauron.silo.domain.UpdateLog;
import pt.tecnico.sauron.silo.domain.exceptions.NoObservationFoundException;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static io.grpc.Status.INVALID_ARGUMENT;

public class SiloServer extends SiloGrpc.SiloImplBase {

    private static final Logger LOGGER = Logger.getLogger(SiloServer.class.getName());

    private final UpdateLog log;
    private final Silo silo;

    public SiloServer(int instance) {
        this.log = new UpdateLog(instance);
        this.silo = new Silo();
    }

    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver) {
        LOGGER.info("camJoin()...");
        LOGGER.info("Arguments received... name: " + request.getName() + " | latitude: " + request.getLatitude() + " | longitude: " + request.getLongitude());

        try{
            //Creates camera in the server
            silo.camJoin(request.getName(), request.getLatitude(), request.getLongitude());

            //Adds camJoin operation to update log
            log.addUpdate(silo.camInfo(request.getName()));

            //Signals that the response was built successfully
            responseObserver.onNext(CamJoinResponse.newBuilder().build());
            responseObserver.onCompleted();

            LOGGER.info("camJoin() successful... ");
        }
        catch (Exception e) {

            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {
        LOGGER.info("camJoin()...");
        LOGGER.info("Arguments received... name: " + request.getName());

        try {
            //Gets the camera information from the server
            Camera camera = silo.camInfo(request.getName());
            double latitude = camera.getLatitude();
            double longitude = camera.getLongitude();

            //Signals that the response was built successfully
            responseObserver.onNext(CamInfoResponse.newBuilder().setLongitude(longitude).setLatitude(latitude).build());
            responseObserver.onCompleted();

            LOGGER.info("camInfo() response... " + camera);
        }
        catch (Exception e) {

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
            LOGGER.info("Observations List: " + ol);

            for (int i = 0; i < ol.size(); i++) {
                Observation o = ol.get(i);
                LOGGER.info("Received Observation " + i + " Object type: " + o.getType() + " Object identifier: " + o.getIdentifier());

                // Creates object and timestamp information for observation
                Object obj = new Object(o.getType().ordinal(), o.getIdentifier());
                Timestamp time = new Timestamp(System.currentTimeMillis());

                // Creates observation with camera gotten from camInfo, object and timestamp from previous 2 lines
                pt.tecnico.sauron.silo.domain.Observation observation = new pt.tecnico.sauron.silo.domain.Observation(obj, time, camera);
                observationsList.add(observation);
            }

            // Sends list of observations updated with time and camera information to silo
            silo.report(observationsList);

            //Adds report operation to update log
            log.addUpdate(camera, observationsList);

            LOGGER.info("report() successful... ");

            //Signals that the response was built successfully
            responseObserver.onNext(ReportResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
        catch (Exception e) {

            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void track(TrackRequest request, StreamObserver<TrackResponse> responseObserver) {
        LOGGER.info("track()...");

        Type t = request.getType();
        String i = request.getIdentifier();

        LOGGER.info("Arguments received... type: " + t + " | identifier: " + i);

        TrackResponse.Builder response = TrackResponse.newBuilder();

        //Sends ts_vector with response
        int[] ts_vector = log.getTs_vector();
        for (int ts_value: ts_vector) {
            response.addTsVector(ts_value);
        }

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

            LOGGER.info("track() response... " + o);

            //Signals that the response was built successfully
            responseObserver.onNext(response.setObservation(obs).build());
            responseObserver.onCompleted();
        }
        catch (NoObservationFoundException e) {

            LOGGER.info("track() no observation was found... ");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
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

        LOGGER.info("Arguments received... type: " + t + " | partial identifier: " + i);

        List<pt.tecnico.sauron.silo.domain.Observation> observations = new ArrayList<>();
        TrackMatchResponse.Builder response = TrackMatchResponse.newBuilder();

        //Sends ts_vector with response
        int[] ts_vector = log.getTs_vector();
        for (int ts_value: ts_vector) {
            response.addTsVector(ts_value);
        }

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
                LOGGER.info("trackMatch() response... " + o);

            }

            //Signals that the response was built successfully
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
        catch (NoObservationFoundException e) {

            LOGGER.info("trackMatch() no observation was found... ");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
        catch (Exception e) {

            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void trace(TraceRequest request, StreamObserver<TraceResponse> responseObserver) {
        LOGGER.info("trace()...");

        Type t = request.getType();
        String i = request.getIdentifier();

        LOGGER.info("Arguments received... type: " + t + " | identifier: " + i);

        List<pt.tecnico.sauron.silo.domain.Observation> observations = new ArrayList<pt.tecnico.sauron.silo.domain.Observation>();
        TraceResponse.Builder response = TraceResponse.newBuilder();

        //Sends ts_vector with response
        int[] ts_vector = log.getTs_vector();
        for (int ts_value: ts_vector) {
            response.addTsVector(ts_value);
        }

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

                LOGGER.info("trace() response... " + o);
            }

            //Signals that the response was built successfully
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
        catch (NoObservationFoundException e) {

            LOGGER.info("trace() no observation was found... ");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
        catch (Exception e) {

            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }

    }

    @Override
    public void ctrlPing(CtrlPingRequest request, StreamObserver<CtrlPingResponse> responseObserver) {

        String input = request.getInputText();
        String output = "Message received by silo server: " + input;
        CtrlPingResponse response = CtrlPingResponse.newBuilder().setOutputText(output).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(CtrlClearRequest request, StreamObserver<CtrlClearResponse> responseObserver) {
        CtrlClearResponse.Builder response = CtrlClearResponse.newBuilder();

        try {
            silo.clear();

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
        catch (Exception e) {

            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void ctrlInit(CtrlInitRequest request, StreamObserver<CtrlInitResponse> responseObserver) {
        super.ctrlInit(request, responseObserver);
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {

        try {
            for (Update update : request.getUpdatesList()) {

                //skips updates already registered in updateLog
                if (log.skipUpdate(update.getInstance(), update.getSeqNumber())) {
                    continue;
                }

                if (update.getObservationsList().isEmpty()) {
                    silo.camJoin(update.getName(), update.getLatitude(), update.getLongitude());
                    log.addUpdate(silo.camInfo(update.getName()), update.getInstance());
                } else {
                    // Calls camInfo to get camera information for camera with name n, throws exception if camera with that name doesn't exist
                    Camera camera = silo.camInfo(update.getName());

                    List<Observation> ol = update.getObservationsList();

                    List<pt.tecnico.sauron.silo.domain.Observation> observationsList = new ArrayList<pt.tecnico.sauron.silo.domain.Observation>();

                    LOGGER.info("gossip() received update... Instance: " + update.getInstance() + " update number: " + update.getSeqNumber() + " Camera name:" + update.getName());

                    for (int i = 0; i < ol.size(); i++) {
                        Observation o = ol.get(i);

                        // Creates object and timestamp information for observation
                        Object obj = new Object(o.getType().ordinal(), o.getIdentifier());

                        //Converts protobuf.timestamp to java.sql.timestamp
                        com.google.protobuf.Timestamp ts = o.getDate();
                        Long milliseconds = ts.getSeconds()*1000;
                        Timestamp time = new Timestamp(milliseconds);

                        // Creates observation with camera gotten from camInfo, object and timestamp from previous 2 lines
                        pt.tecnico.sauron.silo.domain.Observation observation = new pt.tecnico.sauron.silo.domain.Observation(obj, time, camera);
                        observationsList.add(observation);
                    }

                    // Sends list of observations updated with time and camera information to silo
                    silo.report(observationsList);

                    log.addUpdate(camera, observationsList, update.getInstance());
                }
            }

            GossipResponse.Builder response = GossipResponse.newBuilder();

            //Sends ts_vector with response
            int[] ts_vector = log.getTs_vector();
            for (int ts_value: ts_vector) {
                response.addTsVector(ts_value);
            }

            //Signals that the response was built successfully
            responseObserver.onNext(response.setInstance(log.getInstance()).build());
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            LOGGER.info(e.getMessage());
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    public GossipRequest createGossipRequest(int dest_instance) {
        GossipRequest.Builder request = GossipRequest.newBuilder();
        List<pt.tecnico.sauron.silo.domain.Update> updates = log.getUpdates();

        for (pt.tecnico.sauron.silo.domain.Update u: updates) {

            //predicts which updates the other instance already has and doesn't send them
            if (log.skipUpdate(dest_instance, u.getInstance(), u.getSeq_number())) {
                continue;
            }

            int instance = u.getInstance();
            int seq_number = u.getSeq_number();
            String name = u.getCamera().getName();
            double latitude = u.getCamera().getLatitude();
            double longitude = u.getCamera().getLongitude();

            LOGGER.info("createGossipRequest() add update... Instance: " + instance + " update number: " + seq_number + " Camera name:" + name);

            pt.tecnico.sauron.silo.grpc.Update.Builder update = pt.tecnico.sauron.silo.grpc.Update.newBuilder().setInstance(instance)
                    .setSeqNumber(seq_number).setName(name).setLatitude(latitude).setLongitude(longitude);

            for (pt.tecnico.sauron.silo.domain.Observation o : u.getObservations()) {

                //Gets type (dto)
                Type t;
                if (o.getObject().getType() == Object.Type.car) {
                    t = Type.CAR;
                } else {
                    t = Type.PERSON;
                }

                //Converts java.sql.timestamp to protobuf.timestamp
                Timestamp timestamp = o.getTimestamp();
                Long milliseconds = timestamp.getTime();
                com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(milliseconds / 1000).build();

                //Converts internal representation of observation to a data transfer object
                Observation obs = Observation.newBuilder().setType(t).setIdentifier(o.getObject().getIdentifier()).setDate(ts).setName(name).setLatitude(latitude).setLongitude(longitude).build();

                //Adds observation (dto) to list of observations to be sent
                update.addObservations(obs);
            }

            request.addUpdates(update.build());
        }

        return request.build();
    }

    public void handleGossipResponse(GossipResponse response) {

        log.updateTs_vector(response.getInstance(), response.getTsVectorList());
        LOGGER.info("handleGossipResponse() received timestamp vector... " + response.getTsVectorList());
    }
}