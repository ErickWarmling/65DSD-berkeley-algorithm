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

    public Servidor(LocalTime horaInicial) {
        relogio = new Relogio(horaInicial);
    }

    public void inicializarServidor(int tempoEsperaSegundos, int porta) throws IOException {
        System.out.println("╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  Algoritmo de Berkeley (65DSD) – Servidor                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");

        ServerSocket serverSocket = new ServerSocket(porta);
        log("Porta: " + porta + " | Tempo de espera: " + tempoEsperaSegundos + "s");
        log("Hora inicial do servidor → " + relogio);
        log("Aguardando clientes...");

        // Aceita conexões simultaneamente
        Thread acceptThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    socket.setSoTimeout(3000); //Evitar travamento
                    ClientHandler cliente = new ClientHandler(socket);
                    clientes.add(cliente);
                    String ip = socket.getInetAddress().getHostAddress();
                    System.out.println("Cliente conectado → " + ip);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();

        // Loop principal do servidor para aceitar conexões simultaneamente
        while (true) {
            try {
                Thread.sleep(tempoEsperaSegundos * 1000L);
            } catch (InterruptedException e) {}

            log("\nIniciando sincronização de relógios com " + clientes.size() + " cliente(s)");
            sincronizar();
            exibirResultadoFinal();
        }
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
            log("Solicitações enviadas (REQ_HORA) → " + clientes.size() + " clientes");

            // Recebe os horários dos clientes
            for (ClientHandler cliente : clientes) {
                try {
                    String resposta = cliente.receberMensagem();

                    if (resposta == null) {
                        removerCliente(cliente);
                        continue;
                    }

                    String[] partes = resposta.split(";");
                    if (partes[0].equals("HORA")) {
                        long horaCliente = Long.parseLong(partes[1]);
                        horarios.add(horaCliente);
                        cliente.setHoraCliente(horaCliente);

                        long diferenca = horaCliente - relogio.getSegundos();
                        String sinal = diferenca > 0 ? "+" : "";

                        log("Recebido de " + cliente.getSocket().getInetAddress().getHostAddress()
                                + " → " + horaCliente + " (" + sinal + diferenca + "s)");
                    }

                } catch (Exception e) {
                    removerCliente(cliente);
                }
            }

            if (horarios.size() <= 1) {
                log("Nenhum cliente disponível para sincronizar");
                return;
            }

            // Calcula a média
            long soma = horarios.stream().mapToLong(Long::longValue).sum();
            long media = soma / horarios.size();
            log("Hora média calculada: " + LocalTime.ofSecondOfDay(media));

            // Envia o ajuste para cada cliente
            for (ClientHandler cliente : clientes) {
                long ajusteHorario = media - cliente.getHoraCliente();
                cliente.enviarMensagem("AJUSTE_HORARIO;" + ajusteHorario);
                log("Enviando ajuste para " + cliente.getSocket().getInetAddress().getHostAddress() + " → " +
                        (ajusteHorario >= 0 ? "+" : "") + ajusteHorario);
            }

            // Aplica o ajuste no servidor
            long ajusteServidor = media - relogio.getSegundos();
            relogio.aplicarAjusteHorario(ajusteServidor);
            log("Servidor ajustado em " + (ajusteServidor >= 0 ? "+" : "") + ajusteServidor + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removerCliente(ClientHandler c) {
        try {
            log("Cliente desconectado: " + c.getSocket().getInetAddress().getHostAddress());
            c.fecharConexao();
            clientes.remove(c);
        } catch (Exception ignored) {}
    }

    private void log(String msg) {
        System.out.printf("[SERVIDOR]  %s%n", msg);
    }

    private void exibirResultadoFinal() {
        System.out.println("======================================================");
        System.out.println("        Hora final do servidor → " + relogio);
        System.out.println("======================================================");
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