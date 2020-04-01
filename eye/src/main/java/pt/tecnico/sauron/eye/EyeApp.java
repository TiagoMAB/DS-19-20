package pt.tecnico.sauron.eye;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloClientApp;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.*;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class EyeApp {
	private static final String EXIT_CMD = "exit";
	private static final String COMMENT_LINE = "#";
	private static final String SLEEP_LINE = "zzz";
	private static final String CAR_LINE = "car";
	private static final String PERSON_LINE = "person";
	private static final String SEND_LINE = "\n";

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
			while (true) {
				try {
					String line = scanner.nextLine();
					
					// exit
					if (EXIT_CMD.equals(line))
						return;
					
					// comment
					if(COMMENT_LINE.equals(line.charAt(0)))
						continue;
					
					// line processing
					String tokens[] = line.split(",");
					// sleep line
					if(tokens[0].equals(SLEEP_LINE)){
						Thread.sleep(Long.parseLong(tokens[1]));
						System.out.println("SLEEP");
						continue;
					}
					
					if(tokens[0].equals(CAR_LINE)){
						//process car observation
						System.out.println("CAR LINE");
						continue;
					}

					if(tokens[0].equals(PERSON_LINE)){
						//process person observation
						System.out.println("PERSON LINE");
						continue;
					}

					if(tokens[0].equals(SEND_LINE)){
						//process car observation
						System.out.println("SEND LINE");
						continue;
					}
				} catch (StatusRuntimeException e) {
					System.out.println(e.getStatus().getDescription());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		catch (StatusRuntimeException e) {
			System.out.println(e.getStatus().getDescription());
		}
		finally {
			System.out.println("> Closing");
		}
	}
	
}
