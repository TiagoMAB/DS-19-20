package pt.tecnico.sauron.spotter;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class SpotterApp {

	private static final String EXIT_CMD = "exit";
	private static final String SPOT_CMD = "spot";
	private static final String TRAIL_CMD = "trail";
	private static final String PING_CMD = "ping";
	private static final String CLEAR_CMD = "clear";
	private static final String HELP_CMD = "help";
	
	public static void main(String[] args) {
		System.out.println(SpotterApp.class.getSimpleName());

		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		if (args.length != 3 && args.length != 2) {
			System.out.println("Too few or too many arguments. Correct format is: $spotter #address #port (#instance_number)");
			return;
		}

		final String host = args[0];
		final String port = args[1];
		int instance = 0;

		if (args.length == 3) {
			instance = Integer.parseInt(args[2]);
		}

		try (SiloFrontend frontend = new SiloFrontend(host, port, instance); Scanner scanner = new Scanner(System.in)) {
			while (true) {
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

					// identifier contains *
					if (tokens[2].contains("*")) {
						TrackMatchResponse getResponse = frontend.trackMatch(TrackMatchRequest.newBuilder().setType(type).setPartialIdentifier(tokens[2]).build());

						List<Observation> observationsList = new ArrayList<Observation>(getResponse.getObservationsList());

						//sort list of observations by identifier attribute
						Collections.sort(observationsList, Comparator.comparing(Observation::getIdentifier));

						printObservationsList(observationsList);
					}

					// identifier doesn't contain *
					else {
						TrackResponse getResponse = frontend.track(TrackRequest.newBuilder().setType(type).setIdentifier(tokens[2]).build());
						Observation o = getResponse.getObservation();

						// in case there's no match for observation with the provided identifier
						if (o == Observation.getDefaultInstance()) {
							System.out.println();
						} else {
							String t = o.getType() == Type.CAR ? "car" : "person";

							// converts date into format for printing
							LocalDateTime date = Instant.ofEpochSecond(o.getDate().getSeconds(), o.getDate().getNanos()).atZone(ZoneId.of("GMT+1")).toLocalDateTime();
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

				// ping
				else if (tokens.length == 2 && PING_CMD.equals(tokens[0])) {
					CtrlPingResponse getResponse = frontend.ctrlPing(CtrlPingRequest.newBuilder().setInputText(tokens[1]).build());
					System.out.println(getResponse.getOutputText());
				}

				// clear
				else if (tokens.length == 1 && CLEAR_CMD.equals(tokens[0])) {
					frontend.ctrlClear(CtrlClearRequest.newBuilder().build());
				}

				// help
				else if (tokens.length == 1 && HELP_CMD.equals(tokens[0])) {
					System.out.println("Commands supported:");
					System.out.println();
					System.out.println("spot - Searches for most recent observation for each object with type and identifier that matches the given type and identifier or partial identifier. Examples:");
					System.out.println("\"spot person 123456\" - Prints most recent observation of person with identifier 123456");
					System.out.println("\"spot car AB*\" - Prints most recent observation of all cars with identifier starting with AB, ordered by identifier");
					System.out.println();
					System.out.println("trail - Searches for all observations (ordered by date) for object with type and identifier that matches the given type and identifier. Example:");
					System.out.println("\"trail person 345678\" - Prints a list of observations of person with identifier 345678, ordered by date");
					System.out.println();
					System.out.println("exit - Closes the spotter client app");
					System.out.println();
					System.out.println("ping - Sends a message to the server and prints the message that the server replied with. Example:");
					System.out.println("\"ping Test\" - Sends \"Test\" to the server and if successful, receives the message \"Message received by silo server: Test\"");
					System.out.println();
					System.out.println("clear - Sends a message to the server to clear its data structures");
				} else {
					System.out.println("Unrecognized command");
				}
			}
		} catch (StatusRuntimeException e) {
			if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
				System.out.println(e.getStatus().getDescription());
			} else {
				System.out.print("Error ");
			}
		} catch (Exception e) {
			System.out.print("Error ");
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

				LocalDateTime date = Instant.ofEpochSecond(o.getDate().getSeconds(), o.getDate().getNanos()).atZone(ZoneId.of("GMT+1")).toLocalDateTime();
				System.out.println(t + "," + o.getIdentifier() + "," + date + "," + o.getName() + "," + o.getLatitude() + "," + o.getLongitude());
			}
		}
	}
}
