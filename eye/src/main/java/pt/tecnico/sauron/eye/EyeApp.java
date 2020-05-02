package pt.tecnico.sauron.eye;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.*;
import pt.tecnico.sauron.silo.grpc.Observation;
import pt.ulisboa.tecnico.sdis.zk.*;


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

	public static void main(String[] args) {
		System.out.println(EyeApp.class.getSimpleName());
		
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length != 5 && args.length != 6) {
			System.out.println("Too few or too many arguments. Correct format is: $eye #address #port #camera_name #camera_latitude #camera_longitude");
			return;
		}

		final String zkhost = args[0];
		final String zkport = args[1];
		final String camName = args[2];
		final double latitude = Double.parseDouble(args[3]);
		final double longitude = Double.parseDouble(args[4]);
		int instance = 0;
    
		if (args.length == 6) {
			instance = Integer.parseInt(args[5]);
		}

		try (SiloFrontend frontend = new SiloFrontend(zkhost, zkport, instance); Scanner scanner = new Scanner(System.in)) {
			String line;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();

				// exit
				if (EXIT_CMD.equals(line))
					return;

				line = line + '\n';

				// line processing
				if(line.length() == 1){
					//process car observation
					send(camName, latitude, longitude, frontend);

					continue;
				}

				//remove \n
				line = line.substring(0, line.length() - 1);
				// comment
				if(COMMENT_LINE == line.charAt(0)){
					continue;

				}

				String[] tokens = line.split(",");
				if (tokens.length != 2) {
					System.out.println("Invalid input format, aborting...");
					return;
				}

				// sleep line
				if(SLEEP_LINE.equals(tokens[0])){
					Thread.sleep(Long.parseLong(tokens[1]));
					continue;
				}

				observations_in.add(new String[]{tokens[0], tokens[1]});
			}
			send(camName, latitude, longitude, frontend);
		}
		catch (StatusRuntimeException e) {
			if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
				System.out.println(e.getStatus().getDescription());
			}
			else {
				System.out.print("Error ");
			}
		}
		catch (Exception e) {
			System.out.print("Error ");
		}
		finally {
			System.out.println("> Closing");
		}
	}

	private static void send(String camName, double latitude, double longitude, SiloFrontend frontend) throws Exception {

		CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName(camName).setLatitude(latitude).setLongitude(longitude).build();

		//Before report, register camera in silo server instance
		frontend.camJoin(camJoinRequest);
		System.out.println("Camera successfuly connected to server: " + camName);

		//Creates report request
		ReportRequest.Builder req_builder = ReportRequest.newBuilder().setName(camName);
		for (String[] strings : observations_in) {
			Type type = getType(strings);
			String identifier = strings[1];

			//Creates observation dto
			Observation.Builder obs_builder = Observation.newBuilder();
			obs_builder.setType(type);
			obs_builder.setIdentifier(identifier);
			
		 	Observation observation = obs_builder.build();
			req_builder.addObservations(observation);
		}

		//After registering the camera, report observations
		frontend.report(req_builder.build());
		System.out.println("Observations registered successfully");

		//cleanup after each send
		observations_in.clear();
	}

	private static Type getType(String[] observation) {
		if (observation[0].equals(CAR)) return Type.CAR;
		else if (observation[0].equals(PERSON)) return Type.PERSON;
		else {
			return Type.UNKNOWN;
		}
	}
}
