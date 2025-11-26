import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler {

    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private long horaCliente;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printWriter = new PrintWriter(socket.getOutputStream(), true);
    }

    public void enviarMensagem(String msg) {
        printWriter.println(msg);
    }

    public String receberMensagem() throws IOException {
        return bufferedReader.readLine();
    }

    public void setHoraCliente(long hora) {
        this.horaCliente = hora;
    }

    public long getHoraCliente() {
        return horaCliente;
    }

    public Socket getSocket() {
        return socket;
    }

    public void fecharConexao() {
        try {
            bufferedReader.close();
            printWriter.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
