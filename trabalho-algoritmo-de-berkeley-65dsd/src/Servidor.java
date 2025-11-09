import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class Servidor {

    private Relogio relogio;
    private List<ClientHandler> clientes = new CopyOnWriteArrayList<>();
    private boolean sincronizacaoIniciada = false;

    public Servidor(LocalTime horaInicial) {
        relogio = new Relogio(horaInicial);
    }

    public void inicializarServidor(int tempoEsperaSegundos, int porta) throws IOException {
        ServerSocket serverSocket = new ServerSocket(porta);
        System.out.println("Servidor iniciado na porta: " + porta);
        System.out.println("Hora inicial do servidor: " + relogio);
        System.out.println("Aguardando clientes por " + tempoEsperaSegundos + " segundos");

        Thread acceptThread = new Thread(() -> {
            while (!sincronizacaoIniciada) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler cliente = new ClientHandler(socket);
                    clientes.add(cliente);
                    System.out.println("Cliente conectado: " + socket.getInetAddress());
                } catch (IOException e) {
                    if (!sincronizacaoIniciada) {
                        e.printStackTrace();
                    }
                }
            }
        });

        acceptThread.start();

        try {
            Thread.sleep(tempoEsperaSegundos * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sincronizacaoIniciada = true;
        System.out.println("\nIniciando sincronização de relógios com " + clientes.size() + " clientes");
        sincronizar();

        for (ClientHandler cliente : clientes) {
            cliente.fecharConexao();
        }
        serverSocket.close();
        System.out.println("Servidor encerrado!");
    }

    private void sincronizar() {
        try {
            // Adiciona o horário atual do servidor
            List<Long> horarios = new ArrayList<>();
            horarios.add(relogio.getSegundos());

            // Solicita o horários aos clientes
            for (ClientHandler cliente : clientes) {
                cliente.enviarMensagem("REQ_HORA");
            }

            // Recebe os horários dos clientes
            for (ClientHandler cliente : clientes) {
                String resposta = cliente.receberMensagem();
                String[] partes = resposta.split(";");
                if (partes[0].equals("HORA")) {
                    long horaCliente = Long.parseLong(partes[1]);
                    horarios.add(horaCliente);
                    cliente.setHoraCliente(horaCliente);
                }
            }

            // Calcula a média
            long soma = horarios.stream().mapToLong(Long::longValue).sum();
            long media = soma / horarios.size();
            System.out.println("Hora média calculada: " + LocalTime.ofSecondOfDay(media));

            // Envia o ajuste para cada cliente
            for (ClientHandler cliente : clientes) {
                long ajusteHorario = media - cliente.getHoraCliente();
                cliente.enviarMensagem("AJUSTE_HORARIO;" + ajusteHorario);
            }

            // Aplica o ajuste no servidor
            long ajusteServidor = media - relogio.getSegundos();
            relogio.aplicarAjusteHorario(ajusteServidor);
            System.out.println("Servidor finalizado com hora ajustada: " + relogio);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        try(FileInputStream fileInputStream = new FileInputStream("servidor.properties")) {
            props.load(fileInputStream);
        } catch (IOException e) {
            System.out.println("Não foi possível ler o servidor.properties");
        }

        int porta = Integer.parseInt(props.getProperty("porta"));
        int tempoEspera = Integer.parseInt(props.getProperty("tempoEsperaSegundos"));

        Servidor servidor = new Servidor(LocalTime.now());
        try {
            servidor.inicializarServidor(tempoEspera, porta);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}