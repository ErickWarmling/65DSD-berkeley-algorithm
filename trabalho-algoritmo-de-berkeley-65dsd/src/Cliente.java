import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Properties;

public class Cliente {

    private Relogio relogio;

    public Cliente(LocalTime horaInicial) {
        this.relogio = new Relogio(horaInicial);
    }

    public void conectar(String servidor, int porta) throws IOException {
        System.out.println("╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  Algoritmo de Berkeley (65DSD) – Cliente                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
        log("Conectando a " + servidor);
        log("Hora local inicial → " + relogio);

        Socket socket = new Socket(servidor, porta);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

        log("Conectado ao servidor com sucesso!\n");

        while (true) {
            String msg = bufferedReader.readLine();
            if (msg == null) {
                break;
            }

            String[] partes = msg.split(";");
            switch (partes[0]) {
                case "REQ_HORA":
                    long segundos = relogio.getSegundos();
                    printWriter.println("HORA;" + segundos);
                    log("Solicitação de hora recebida ← REQ_HORA");
                    log("Enviado hora ao servidor: → HORA;" + relogio);
                    break;
                case "AJUSTE_HORARIO":
                    long ajuste = Long.parseLong(partes[1]);
                    relogio.aplicarAjusteHorario(ajuste);
                    String sinal = ajuste >= 0 ? "+" : "";
                    log("Ajuste recebido: ← AJUSTE;" + sinal + ajuste + "s");
                    break;
            }
        }

        bufferedReader.close();
        printWriter.close();
        socket.close();
        System.out.println("Conexão encerrada");
        System.out.println("Hora final: " + relogio);
    }

    private void log(String msg) {
        System.out.printf("[Cliente]  %s%n", msg);
    }

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        try(FileInputStream fileInputStream = new FileInputStream("cliente.properties")) {
            props.load(fileInputStream);
        } catch (IOException e) {
            System.out.println("Não foi possível ler o cliente.properties");
        }

        String servidor = props.getProperty("servidor");
        int porta = Integer.parseInt(props.getProperty("porta"));

        Cliente cliente = new Cliente(LocalTime.of(17, 0, 10));
        cliente.conectar(servidor, porta);
    }
}