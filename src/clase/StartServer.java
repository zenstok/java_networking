package clase;

import java.util.Scanner;

public class StartServer {

	public static void main(String[] args) {
		System.out.println("Server is running, type 'exit' to close it.");
		try (Server server = new Server(23)) {
			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					if (scanner.hasNextLine() && "exit".equalsIgnoreCase(scanner.nextLine())) {
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

}
