package pt.tecnico.sauron.silo;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;
import java.util.Scanner;

public class SiloServerApp {

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
		final BindableService impl = new SiloServer(Integer.parseInt(path.substring(path.length() - 1)), zooHost, zooPort, host, port);		//TODO: change this later IMPORTANT or let it be, we'll see

		ZKNaming zkNaming = null;

		try {

			zkNaming = new ZKNaming(zooHost, zooPort);

			zkNaming.rebind(path, host, port);						//TODO: check if bind or rebind

			// Create a new server to listen on port
			Server server = ServerBuilder.forPort(Integer.parseInt(port)).addService(impl).build();

			// Start the server
			server.start();

			// Server threads are running in the background.
			System.out.println("Server started");

			// Create new thread where we wait for the user input.
			new Thread(() -> {
				System.out.println("<Press enter to shutdown>");
				new Scanner(System.in).nextLine();

				server.shutdown();
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
