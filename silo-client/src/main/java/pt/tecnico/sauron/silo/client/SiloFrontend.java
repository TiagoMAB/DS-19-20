package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.Random;
import java.util.ArrayList;

public class SiloFrontend implements AutoCloseable {

    private ManagedChannel channel;
    private SiloGrpc.SiloBlockingStub stub;

    private String path = "/grpc/sauron/silo";
    private String host;
    private String port;
    private int instance = 0;

    private Cache cache;
    private int[] ts_vector;

    public SiloFrontend(String host, String port, int instance) throws ZKNamingException {

        this.host = host;
        this.port = port;
        this.instance = instance;

        this.cache = new Cache(5);
        this.ts_vector = new int[9];

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

    public void camJoin(CamJoinRequest request) {               //TODO: communication error handle failure
        stub.camJoin(request);
    }

    public CamInfoResponse camInfo(CamInfoRequest request) {              //TODO: communication error handle failure
        return stub.camInfo(request);
    }

    public ReportResponse report(ReportRequest request) {              //TODO: communication error handle failure
        return stub.report(request);
    }

    public TrackResponse track(TrackRequest request) {              //TODO: communication error handle failure
        TrackResponse r = stub.track(request);
        cache.addObservation(r.getObservation());

        return r;
    }

    public TrackMatchResponse trackMatch(TrackMatchRequest request) {              //TODO: communication error handle failure
        TrackMatchResponse r = stub.trackMatch(request);
        cache.addSpotObservations(r.getObservationsList());

        return r;
    }

    public TraceResponse trace(TraceRequest request) {              //TODO: communication error handle failure
        TraceResponse r = stub.trace(request);
        cache.addTrailObservations(r.getObservationsList());

        return r;
    }

    public CtrlPingResponse ctrlPing(CtrlPingRequest request) {              //TODO: communication error handle failure
        return stub.ctrlPing(request);
    }

    public CtrlClearResponse ctrlClear(CtrlClearRequest request) {              //TODO: communication error handle failure
        return stub.ctrlClear(request);
    }

    public CtrlInitResponse ctrlInit(CtrlInitRequest request) {              //TODO: communication error handle failure
        return stub.ctrlInit(request);
    }

    @Override
    public final void close() {
        channel.shutdown();
    }
}
