package pt.tecnico.sauron.silo.client;

import io.grpc.*;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

public class SiloFrontend implements AutoCloseable {

    private ManagedChannel channel;
    private SiloGrpc.SiloBlockingStub stub;

    private String path = "/grpc/sauron/silo";
    private String host;
    private String port;
    private int instance = 0;
    private int errors = 0;

    private Cache cache;
    private List<Integer> ts_vector;

    public SiloFrontend(String host, String port, int instance) throws Exception {

        this.host = host;
        this.port = port;
        this.instance = instance;

        this.cache = new Cache(5);
        this.ts_vector = new ArrayList<>();
        for (int i= 0; i < 9; i++) {
            this.ts_vector.add(0);
        }

        connect();
    }

    public void camJoin(CamJoinRequest request) throws Exception {               //TODO: communication error handle failure

        try {
            stub.camJoin(request);
            errors = 0;
        }
        catch (StatusRuntimeException e) {
            handleNetworkFailure(e);
            if (errors == 30) {
                throw e;
            }
            else {
                errors++;
                camJoin(request);
            }
        }
    }

    public CamInfoResponse camInfo(CamInfoRequest request) throws Exception {              //TODO: communication error handle failure

        CamInfoResponse r = null;

        try {
            r = stub.camInfo(request);
            errors = 0;
            return r;
        }
        catch (StatusRuntimeException e) {
            handleNetworkFailure(e);
            if (errors == 30) {
                throw e;
            }
            else {
                errors++;
                return r = camInfo(request);
            }
        }
    }

    public ReportResponse report(ReportRequest request) throws Exception {              //TODO: communication error handle failure

        ReportResponse r = null;

        try {
            r = stub.report(request);
            errors = 0;
            return r;
        }
        catch (StatusRuntimeException e) {
            handleNetworkFailure(e);
            if (errors == 30) {
                throw e;
            }
            else {
                errors++;
                return r = report(request);
            }
        }
    }

    public TrackResponse track(TrackRequest request) throws Exception {              //TODO: communication error handle failure

        TrackResponse r = null;

        try {
            r = stub.track(request);

            errors = 0;

            if (!checkTs(r.getTsVectorList())) {
                return cache.coherentTrack(r, request.getIdentifier(), request.getType());
            }
            else {
                cache.addObservation(r.getObservation());
                return r;
            }
        }
        catch (StatusRuntimeException e) {
            handleNetworkFailure(e);
            if (errors == 30) {
                throw e;
            }
            else {
                errors++;
                return r = track(request);
            }
        }
    }

    public TrackMatchResponse trackMatch(TrackMatchRequest request) throws Exception {              //TODO: communication error handle failure

        TrackMatchResponse r = null;

        try {
            r = stub.trackMatch(request);

            errors = 0;

            if (!checkTs(r.getTsVectorList())) {
                return cache.coherentTrackMatch(r, request.getPartialIdentifier(), request.getType());
            }
            else {
                cache.addSpotObservations(r.getObservationsList());
                return r;
            }
        }
        catch (StatusRuntimeException e) {
            handleNetworkFailure(e);
            if (errors == 30) {
                throw e;
            }
            else {
                errors++;
                return r = trackMatch(request);
            }
        }

    }

    public TraceResponse trace(TraceRequest request) throws Exception {              //TODO: communication error handle failure

        TraceResponse r = null;

        try {
            r = stub.trace(request);

            errors = 0;

            if (!checkTs(r.getTsVectorList())) {
                return cache.coherentTrace(r, request.getIdentifier(), request.getType());
            }
            else {
                cache.addTrailObservations(r.getObservationsList());
                return r;
            }
        }
        catch (StatusRuntimeException e) {
            handleNetworkFailure(e);
            if (errors == 3) {
                throw e;
            }
            else {
                errors++;
                return r = trace(request);
            }
        }
    }

    public CtrlPingResponse ctrlPing(CtrlPingRequest request) throws Exception {              //TODO: communication error handle failure

        CtrlPingResponse r = null;

        try {
            r = stub.ctrlPing(request);
            errors = 0;
            return r;
        }
        catch (StatusRuntimeException e) {
            handleNetworkFailure(e);
            if (errors == 3) {
                throw e;
            }
            else {
                errors++;
                return r = ctrlPing(request);
            }
        }
    }

    public CtrlClearResponse ctrlClear(CtrlClearRequest request) throws Exception{              //TODO: communication error handle failure

        CtrlClearResponse r = null;

        try {
            r = stub.ctrlClear(request);
            errors = 0;
            return r;
        }
        catch (StatusRuntimeException e) {
            handleNetworkFailure(e);
            if (errors == 3) {
                throw e;
            }
            else {
                errors++;
                return r = ctrlClear(request);
            }
        }
    }

    public CtrlInitResponse ctrlInit(CtrlInitRequest request) throws Exception {              //TODO: communication error handle failure

        CtrlInitResponse r = null;

        try {
            r = stub.ctrlInit(request);
            errors = 0;
            return r;
        }
        catch (StatusRuntimeException e) {
            handleNetworkFailure(e);

            if (errors == 3) {
                throw e;
            }
            else {
                errors++;
                return r = ctrlInit(request);
            }
        }
    }

    @Override
    public final void close() {
        channel.shutdown();
    }

    public boolean checkTs(List<Integer> ts_vector) {

        for (int index = 0; index < ts_vector.size(); index++) {
            if (this.ts_vector.get(index) > ts_vector.get(index)) {
                return false;
            }
        }

        for (int index = 0; index < ts_vector.size(); index++) {
            this.ts_vector.set(index, ts_vector.get(index));
        }

        return true;
    }

    public void handleNetworkFailure(StatusRuntimeException e) throws Exception {

        if (e.getStatus().getCode() == Status.Code.UNAVAILABLE || e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
            connect();
        }
        else {
            throw e;
        }
    }

    public void connect() throws Exception {

        ZKNaming zkNaming = new ZKNaming(host,port);
        ZKRecord record;

        //if instance not provided chooses random instance, otherwise finds instance provided
        if (instance == 0) {

            //gets list of instances
            ArrayList<ZKRecord> records = (ArrayList) zkNaming.listRecords(path);       //TODO: check if we can do this downcast


            //chooses random instance
            Random rand = new Random();
            record = records.get(rand.nextInt(records.size()));

        } else {
            record = zkNaming.lookup(path + "/" + instance);
        }

        String target = record.getURI();

        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = SiloGrpc.newBlockingStub(channel);
    }
}
