import java.time.LocalTime;

public class Relogio {

    private LocalTime hora;

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
    public synchronized String toString() {
        return hora.toString();
    }
}