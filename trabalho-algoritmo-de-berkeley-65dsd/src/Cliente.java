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
        Socket socket = new Socket(servidor, porta);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Conectado ao servidor. Hora local inicial: " + relogio);

        while (true) {
            String msg = bufferedReader.readLine();
            if (msg == null) {
                break;
            }

            String[] partes = msg.split(";");
            switch (partes[0]) {
                case "REQ_HORA":
                    printWriter.println("HORA;" + relogio.getSegundos());
                    System.out.println("Enviando hora ao servidor: " + relogio);
                    break;
                case "AJUSTE_HORARIO":
                    long ajuste = Long.parseLong(partes[1]);
                    relogio.aplicarAjusteHorario(ajuste);
                    break;
            }
        }

        bufferedReader.close();
        printWriter.close();
        socket.close();
        System.out.println("Conexão encerrada. Hora final: " + relogio);
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

        Cliente cliente = new Cliente(LocalTime.now());
        cliente.conectar(servidor, porta);
    }
}