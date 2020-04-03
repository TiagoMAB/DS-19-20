package pt.tecnico.sauron.spotter;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.*;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class SpotterApp {

	private static final String EXIT_CMD = "exit";
	private static final String SPOT_CMD = "spot";
	private static final String TRAIL_CMD = "trail";
	private static final String PING_CMD = "ping";
	private static final String CLEAR_CMD = "clear";
	
	public static void main(String[] args) {
		System.out.println(SpotterApp.class.getSimpleName());

		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host server host port %n", SpotterApp.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		try (SiloFrontend frontend = new SiloFrontend(host, port); Scanner scanner = new Scanner(System.in)) {
			while (true) {
				try {
					String line = scanner.nextLine();
					String[] tokens = line.split(" ");

					// exit
					if (tokens.length == 1 && EXIT_CMD.equals(tokens[0]))
						break;

					// spot
					else if (tokens.length == 3 && SPOT_CMD.equals(tokens[0])) {
						Type type;
						if (tokens[1].equals("car")) type = Type.CAR;
						else if (tokens[1].equals("person")) type = Type.PERSON;
						else {
							System.out.println("Invalid type, must be either \"car\" or \"person\"");
							continue;
						}

						if (tokens[2].contains("*")) {
							TrackMatchResponse getResponse = frontend.trackMatch(TrackMatchRequest.newBuilder().setType(type).setPartialIdentifier(tokens[2]).build());

							List<Observation> observationsList = new ArrayList<Observation>(getResponse.getObservationsList());
							Collections.sort(observationsList, Comparator.comparing(Observation::getIdentifier));

							printObservationsList(getResponse.getObservationsList());
						}
						else {
							TrackResponse getResponse = frontend.track(TrackRequest.newBuilder().setType(type).setIdentifier(tokens[2]).build());
							Observation o = getResponse.getObservation();

							if (o == Observation.getDefaultInstance()) {
								System.out.println();
							}
							else {
								String t = o.getType() == Type.CAR ? "car" : "person";

								LocalDateTime date = Instant.ofEpochSecond(o.getDate().getSeconds(), o.getDate().getNanos()).atZone(ZoneId.of("GMT+0")).toLocalDateTime();
								System.out.println(t + "," + o.getIdentifier() + "," + date + "," + o.getName() + "," + o.getLatitude() + "," + o.getLongitude());
							}

							continue;
						}
					}

					// trail
					else if (tokens.length == 3 && TRAIL_CMD.equals(tokens[0])) {
						Type type;
						if (tokens[1].equals("car")) type = Type.CAR;
						else if (tokens[1].equals("person")) type = Type.PERSON;
						else {
							System.out.println("Invalid type, must be either \"car\" or \"person\"");
							continue;
						}

						TraceResponse getResponse = frontend.trace(TraceRequest.newBuilder().setType(type).setIdentifier(tokens[2]).build());
						printObservationsList(getResponse.getObservationsList());
					}

					else if (tokens.length == 2 && PING_CMD.equals(tokens[0])) {
						CtrlPingResponse getResponse = frontend.ctrlPing(CtrlPingRequest.newBuilder().setInputText(tokens[1]).build());
						System.out.println(getResponse.getOutputText());
					}

					else if (tokens.length == 1 && CLEAR_CMD.equals(tokens[0])) {
						frontend.ctrlClear(CtrlClearRequest.newBuilder().build());
					}

					else {
						System.out.println("Unrecognized command");
					}

				} catch (StatusRuntimeException e) {
					System.out.println(e.getStatus().getDescription());
				}
			}

		} finally {
			System.out.println("> Closing");
		}
	}

	private static void printObservationsList(List<Observation> observationsList) {
		if (observationsList.size() == 0)
			System.out.println();
		else {
			for (int i = 0; i < observationsList.size(); i++) {
				Observation o = observationsList.get(i);
				String t = o.getType() == Type.CAR ? "car" : "person";

				LocalDateTime date = Instant.ofEpochSecond(o.getDate().getSeconds(), o.getDate().getNanos()).atZone(ZoneId.of("GMT+0")).toLocalDateTime();
				System.out.println(t + "," + o.getIdentifier() + "," + date + "," + o.getName() + "," + o.getLatitude() + "," + o.getLongitude());
			}
		}
	}
}
