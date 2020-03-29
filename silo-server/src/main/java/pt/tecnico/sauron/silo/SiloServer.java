package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.Silo;
import pt.tecnico.sauron.silo.grpc.SiloGrpc;

import java.util.logging.Logger;

public class SiloServer extends SiloGrpc.SiloImplBase {

    private static final Logger LOGGER = Logger.getLogger(SiloServer.class.getName());

    private final Silo silo = new Silo();



}
