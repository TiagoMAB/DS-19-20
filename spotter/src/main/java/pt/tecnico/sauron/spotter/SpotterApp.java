package pt.tecnico.sauron.spotter;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.*;

import java.util.List;
import java.util.Scanner;

public class SpotterApp {

	private static final String EXIT_CMD = "exit";
	private static final String SPOT_CMD = "spot";
	private static final String TRAIL_CMD = "trail";
	
	public static void main(String[] args) {
		System.out.println(SpotterApp.class.getSimpleName());

		// TODO: use patterns for identifier checks
		// String[] patterns = {"^\\d{2}[A-Z]{2}\\d{2}", "^[A-Z]{2}\\d{4}", "^\\d{4}[A-Z]{2}", "^\\d{2}[A-Z]{4}", "^[A-Z]{4}\\d{2}", "^[A-Z]{2}\\d{2}[A-Z]{2}"};

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		if (args.length < 3) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host server host port %n", SpotterApp.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		try (SiloFrontend frontend = new SiloFrontend(host, port); Scanner scanner = new Scanner(System.in)) {
			while (true) {
				// TODO: maybe add a prompt message
				// TODO: functions to abstract code
				try {
					String line = scanner.nextLine();
					String[] tokens = line.split(" ");

					// exit
					if (tokens.length >= 1 && EXIT_CMD.equals(tokens[0])) // TODO: maybe change to length == 1
						break;

					// spot
					if (tokens.length == 3 && SPOT_CMD.equals(tokens[0])) {
						Type type;
						if (tokens[1].matches("car")) type = Type.CAR;
						else if (tokens[1].matches("person")) type = Type.PERSON;
						else continue;	// TODO: change continue to printing an error

						// TODO: add error checking for identifier token with patterns
						if (tokens[2].contains("*")) {
							// TODO: error check response type, all fields of response
							TrackMatchResponse getResponse = frontend.trackMatch(TrackMatchRequest.newBuilder().setType(type).setPartialIdentifier(tokens[2]).build());

							printObservationsList(getResponse.getObservationsList());
						}
						else {
							// TODO: error check response type, all fields of response
							TrackResponse getResponse = frontend.track(TrackRequest.newBuilder().setType(type).setIdentifier(tokens[2]).build());
							Observation o = getResponse.getObservation();
							String t = o.getType() == Type.CAR ? "car" : "person";

							System.out.println(t + "," + o.getIdentifier() + "," + o.getDate() + "," + o.getName() + "," + o.getLatitude() + "," + o.getLongitude());
							continue;
						}
					}

					// trail
					if (tokens.length == 3 && TRAIL_CMD.equals(tokens[0])) {
						// TODO: repeated code, abstract
						Type type;
						if (tokens[1].matches("car")) type = Type.CAR;
						else if (tokens[1].matches("person")) type = Type.PERSON;
						else continue;	// TODO: change continue to printing an error

						// TODO: error check response type, all fields of response
						TraceResponse getResponse = frontend.trace(TraceRequest.newBuilder().setType(type).setIdentifier(tokens[2]).build());
						printObservationsList(getResponse.getObservationsList());
					}
					// TODO: add print for unrecognized commands

				} catch (StatusRuntimeException e) {
					System.out.println(e.getStatus().getDescription());
				}
			}

		} finally {
			System.out.println("> Closing");
		}
	}

	private static void printObservationsList(List<Observation> observationsList2) {
		List<Observation> observationsList = observationsList2;
		for (int i = 0; i < observationsList.size(); i++) { // TODO: assumes list is ordered, error check order
			Observation o = observationsList.get(i);
			String t = o.getType() == Type.CAR ? "car" : "person";

			System.out.println(t + "," + o.getIdentifier() + "," + o.getDate() + "," + o.getName() + "," + o.getLatitude() + "," + o.getLongitude());
		}
	}
}
