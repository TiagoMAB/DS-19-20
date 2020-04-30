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

    public SiloFrontend(String host, String port, int instance) throws ZKNamingException {

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

    public void camJoin(CamJoinRequest request) {
        try {
            stub.camJoin(request);
        }
        catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
            stub.camJoin(request);
        }
    }

    public CamInfoResponse camInfo(CamInfoRequest request) {
        return stub.camInfo(request);
    }

    public ReportResponse report(ReportRequest request) {
        return stub.report(request);
    }

    public TrackResponse track(TrackRequest request) {
        return stub.track(request);
    }

    public TrackMatchResponse trackMatch(TrackMatchRequest request) {
        return stub.trackMatch(request);
    }

    public TraceResponse trace(TraceRequest request) {
        return stub.trace(request);
    }

    public CtrlPingResponse ctrlPing(CtrlPingRequest request) {
        return stub.ctrlPing(request);
    }

    public CtrlClearResponse ctrlClear(CtrlClearRequest request) {
        return stub.ctrlClear(request);
    }

    public CtrlInitResponse ctrlInit(CtrlInitRequest request) {
        return stub.ctrlInit(request);
    }

    @Override
    public final void close() {
        channel.shutdown();
    }
}
