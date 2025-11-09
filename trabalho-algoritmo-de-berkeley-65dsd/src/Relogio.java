import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Relogio {

    private LocalTime hora;
    private static final DateTimeFormatter HORARIO_FORMATADO = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public Relogio (LocalTime horaInicial) {
        this.hora = horaInicial;
    }

    public synchronized LocalTime getHora() {
        return hora;
    }

    public synchronized void aplicarAjusteHorario(long segundos) {
        hora = hora.plusSeconds(segundos);
        System.out.println("Relógio ajustado para: " + hora);
    }

    public synchronized long getSegundos() {
        return hora.toSecondOfDay();
    }

    @Override
    public String toString() {
        return hora.format(HORARIO_FORMATADO);
    }
}