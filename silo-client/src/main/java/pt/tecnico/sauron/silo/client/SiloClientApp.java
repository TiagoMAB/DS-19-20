package pt.tecnico.sauron.silo.client;


import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.*;

import java.util.List;
import java.util.Scanner;

public class SiloClientApp {
	private static final String EXIT_CMD = "exit";
	private static final String TRACE_CMD = "trace";
	private static final String TRACK_CMD = "track";
	private static final String TRACKMATCH_CMD = "trackMatch";


	public static void main(String[] args) {
		System.out.println(SiloClientApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}


		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", SiloClientApp.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		try (SiloFrontend frontend = new SiloFrontend(host, port); Scanner scanner = new Scanner(System.in)) {
			while (true) {
				System.out.print("> Set the name (`exit` to quit)\n> ");
				try {
					String line = scanner.nextLine();

					// exit
					if (EXIT_CMD.equals(line))
						break;

					if (TRACE_CMD.equals(line)) {
						TraceResponse getResponse = frontend.trace(TraceRequest.newBuilder().setType(Type.CAR).setIdentifier("AABB22").build());
						List<Observation> obs = getResponse.getObservationsList();

						for (Observation o: obs) {
							System.out.println("Type: " + o.getType() + " | Identifier: " + o.getIdentifier() + " | Ts: " + o.getDate().toString());
						}

						continue;
					}

					if (TRACKMATCH_CMD.equals(line)) {
						TrackMatchResponse getResponse = frontend.trackMatch(TrackMatchRequest.newBuilder().setType(Type.CAR).setPartialIdentifier("*22").build());
						List<Observation> obs = getResponse.getObservationsList();

						System.out.println("ah");
						for (Observation o: obs) {
							System.out.println("Type: " + o.getType() + " | Identifier: " + o.getIdentifier() + " | Ts: " + o.getDate().toString());
						}

						continue;
					}

				} catch (StatusRuntimeException e) {
					System.out.println(e.getStatus().getDescription());
				}
			}

		} finally {
			System.out.println("> Closing");
		}
	}
	
}
