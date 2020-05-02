package pt.tecnico.sauron.silo;

import io.grpc.*;
import pt.tecnico.sauron.silo.grpc.SiloGrpc;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class SiloServerApp {

	private static class GossipProtocol extends TimerTask {

		private final String path = "/grpc/sauron/silo";
		private final String zooHost;
		private final String zooPort;
		private final String host;
		private final String port;
		private final SiloServer server;
		private final Logger LOGGER = Logger.getLogger(SiloServer.class.getName());

		public GossipProtocol(SiloServer server, String zooHost, String zooPort, String host, String port) {
			this.server = server;
			this.zooHost = zooHost;
			this.zooPort = zooPort;
			this.host = host;
			this.port = port;
		}

		public void run() {
			ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
			ZKRecord record;

			LOGGER.info("GossipProtocol() starting...");
			try {
				List<ZKRecord> records = (ArrayList) zkNaming.listRecords(path);

				//for each known replica sends a gossip message
				for (ZKRecord zkr: records) {

					//doesn't send gossip messages to itself
					if (zkr.getURI().equals(host + ":" + port)) {
						continue;
					}
					LOGGER.info("GossipProtocol() sending data to..." + zkr.getPath());
					String target = zkr.getURI();

					ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
					SiloGrpc.SiloBlockingStub stub = SiloGrpc.newBlockingStub(channel);

					try {
						int instance = Integer.parseInt(zkr.getPath().substring(zkr.getPath().length() - 1));
						server.handleGossipResponse(stub.gossip(server.createGossipRequest(instance)));
						LOGGER.info("GossipProtocol() finished sending data to " + zkr.getPath());
					}
					catch (Exception e) {
						LOGGER.info("GossipProtocol() error sending data to " + zkr.getPath());
					}
				}
				LOGGER.info("GossipProtocol() finishing...");
			}
			catch (Exception e) {
				LOGGER.info("GossipProtocol() error during gossip: " + e.getMessage());
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println(SiloServerApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port%n", SiloServerApp.class.getName());
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String host = args[2];
		final String port = args[3];
		final String path = args[4];
		final BindableService impl = new SiloServer(Integer.parseInt(path.substring(path.length() - 1)));

		ZKNaming zkNaming = null;

		try {

			// Registers server in server name manager
			zkNaming = new ZKNaming(zooHost, zooPort);
			zkNaming.rebind(path, host, port);

			// Create a new server to listen on port
			Server server = ServerBuilder.forPort(Integer.parseInt(port)).addService(impl).build();

			// Start the server
			server.start();

			// Server threads are running in the background.
			System.out.println("Server started");

			// Initializes gossip protocol
			Timer timer = new Timer();
			TimerTask gossip = new GossipProtocol((SiloServer) impl, zooHost, zooPort, host, port);
			timer.schedule(gossip, Date.from(Instant.now().plusSeconds(30)), 30000);

			// Create new thread where we wait for the user input.
			new Thread(() -> {
				System.out.println("<Press enter to shutdown>");
				new Scanner(System.in).nextLine();

				server.shutdown();

				// Ends periodic gossip protocol
				timer.cancel();
			}).start();


			// Do not exit the main thread. Wait until server is terminated.
			server.awaitTermination();
		} finally {
			if (zkNaming != null) {
				zkNaming.unbind(path, host, port);
			}
		}

	}
}
