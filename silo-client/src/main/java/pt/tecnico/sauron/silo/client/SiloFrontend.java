package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.*;

public class SiloFrontend implements AutoCloseable {

    private final ManagedChannel channel;
    private final siloGrpc.siloBlockingStub stub;

    public SiloFrontend(String host, int port) {

        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = siloGrpc.newBlockingStub(channel);

    }

    public CamJoinResponse camJoin(CamJoinRequest request) {
        return stub.camJoin(request);
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
