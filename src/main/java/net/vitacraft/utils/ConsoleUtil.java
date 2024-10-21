package net.vitacraft.utils;

public class ConsoleUtil {

    public static void print(String message) {
        String coloredMessage = AnsiColorUtil.applyColors(message);
        System.out.println(coloredMessage);
    }

    public static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                // uses 'cmd.exe' to execute the 'cls' command
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // for unix-like systems
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (final Exception e) {
            // manual goofy clear
            for(int i=0; i<300; i++){
                System.out.println("");
            }
        }
    }
}