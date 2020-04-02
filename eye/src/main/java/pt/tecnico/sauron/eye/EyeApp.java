package pt.tecnico.sauron.eye;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.*;
import pt.tecnico.sauron.silo.grpc.Observation;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EyeApp {
	private static final String EXIT_CMD = "exit";
	private static final char COMMENT_LINE = '#';
	private static final String SLEEP_LINE = "zzz";
	private static final String CAR = "car";
	private static final String PERSON = "person";
	public static List<String[]> observations_in = new ArrayList<>();
	public static List<Observation> observations = new ArrayList<>();

	public static void main(String[] args) {
		System.out.println(EyeApp.class.getSimpleName());
		
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final String camName = args[2];
		final double latitude = Double.parseDouble(args[3]);
		final double longitude = Double.parseDouble(args[4]);
		long millis = System.currentTimeMillis();
		Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
				.setNanos((int) ((millis % 1000) * 1000000)).build();
		
		try (SiloFrontend frontend = new SiloFrontend(host, port); Scanner scanner = new Scanner(System.in)) {
			try {
				CamJoinResponse getResponse = frontend.camJoin(CamJoinRequest.newBuilder().
					setName(camName).
					setLatitude(latitude).
					setLongitude(longitude).build());

			} catch (StatusRuntimeException e) {
				System.out.println("Error on joining camera to server: " + e.getStatus().getDescription());
				return;
			}
			String line;
			while (scanner.hasNextLine()) {
				try {
					line = scanner.nextLine();
					line = line + '\n';
					// exit
					if (EXIT_CMD.equals(line))
						return;
					
					// line processing
					if(line.length() == 1){
						//process car observation
						//System.out.println("SEND LINE");
						send(camName, timestamp, frontend);

						continue;
					}

					//remove \n
					line = line.substring(0, line.length() - 1);
					// comment
					if(COMMENT_LINE == line.charAt(0)){
						//System.out.println("COMMENT LINE");
						continue;

					}
						
					String[] tokens = line.split(",");
					// sleep line
					if(SLEEP_LINE.equals(tokens[0])){
						Thread.sleep(Long.parseLong(tokens[1]));
						//System.out.println("SLEEP");
						continue;
					}
					
					if(tokens[0].equals(CAR)){
						//process car observation
						//System.out.println("CAR LINE");
						observations_in.add(new String[]{tokens[0], tokens[1]});
						continue;
					}

					if(tokens[0].equals(PERSON)){
						//process person observation
						//System.out.println("PERSON LINE");
						observations_in.add(new String[]{tokens[0], tokens[1]});
					}
				} catch (StatusRuntimeException e) {
					System.out.println(e.getStatus().getDescription());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			send(camName, timestamp, frontend);

			/*
			for (String[] observation : observations) {
				System.out.println(Arrays.toString(observation));
			}*/
		}
		catch (StatusRuntimeException e) {
			System.out.println(e.getStatus().getDescription());
		}
		finally {
			System.out.println("> Closing");
		}
	}

	private static void send(String camName, Timestamp timestamp, SiloFrontend frontend) {
		for (String[] strings : observations_in) {
			Type type = getType(strings);
			String identifier = strings[1];

			Observation observation = Observation.newBuilder().setName(camName).setType(type).
					setIdentifier(identifier).
					setDate(timestamp).build();

			ReportResponse getResponse = frontend.report(ReportRequest.newBuilder().
					 					 addObservations(observation).build());
		}
	}

	private static Type getType(String[] observation) {
		if (observation[0].equals(CAR)) return Type.CAR;
		else if (observation[0].equals(PERSON)) return Type.PERSON;
		else {
			System.out.println("Invalid type, must be either \"car\" or \"person\"");
			return null;
		}
	}
}
