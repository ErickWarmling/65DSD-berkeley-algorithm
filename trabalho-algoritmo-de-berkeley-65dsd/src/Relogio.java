import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Relogio {

    private LocalTime hora;
    private static final DateTimeFormatter HORARIO_FORMATADO = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public Relogio (LocalTime horaInicial) {
        this.hora = horaInicial;
        iniciarRelogio();
    }

    private void iniciarRelogio() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // 1 segundo real
                } catch (InterruptedException ignored) {}

                incrementar();
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private synchronized void incrementar() {
        hora = hora.plusSeconds(1);
    }

    public synchronized LocalTime getHora() {
        return hora;
    }

    public synchronized void aplicarAjusteHorario(long segundos) {
        hora = hora.plusSeconds(segundos);
        System.out.println("Relógio ajustado para: " + this);
    }

    public synchronized long getSegundos() {
        return hora.toSecondOfDay();
    }

    @Override
    public String toString() {
        return hora.format(HORARIO_FORMATADO);
    }
}