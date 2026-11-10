package cr.ac.una.est.laboratorioestructuras.servicio;

import cr.ac.una.est.laboratorioestructuras.modelo.Posicion;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Repartimos los nodos alrededor de un círculo para que el dibujo quede ordenado.
public class DistribuidorCircular {

    private final double margen;

    //Guardamos cuánto espacio queremos dejar entre los nodos y el borde.
    public DistribuidorCircular(double margen) {
        if (!Double.isFinite(margen) || margen < 0) {
            throw new IllegalArgumentException(
                    "El margen debe ser un número finito no negativo");
        }
        this.margen = margen;
    }

    //Calculamos dónde va cada nodo dentro del área que tenemos.
    public Map<Integer, Posicion> calcular(
            List<Integer> ids,
            double ancho,
            double alto) {
        if (ids == null) {
            throw new IllegalArgumentException("La lista de IDs no puede ser null");
        }
        if (!Double.isFinite(ancho) || !Double.isFinite(alto)
                || ancho < 0 || alto < 0) {
            throw new IllegalArgumentException(
                    "El ancho y el alto deben ser finitos y no negativos");
        }

        Set<Integer> idsUnicos = new LinkedHashSet<>();
        for (Integer id : ids) {
            if (id == null) {
                throw new IllegalArgumentException(
                        "La lista de IDs no puede contener null");
            }
            if (!idsUnicos.add(id)) {
                throw new IllegalArgumentException("ID duplicado: " + id);
            }
        }

        Map<Integer, Posicion> posiciones = new LinkedHashMap<>();
        int cantidad = idsUnicos.size();
        if (cantidad == 0) {
            return Collections.unmodifiableMap(posiciones);
        }

        double centroX = ancho / 2.0;
        double centroY = alto / 2.0;

        if (cantidad == 1) {
            Integer id = idsUnicos.iterator().next();
            posiciones.put(id, new Posicion(centroX, centroY));
            return Collections.unmodifiableMap(posiciones);
        }

        double radio = Math.max(0.0, Math.min(ancho, alto) / 2.0 - margen);
        int indice = 0;

        for (Integer id : idsUnicos) {
            double angulo = -Math.PI / 2.0
                    + (2.0 * Math.PI * indice / cantidad);
            double x = centroX + radio * Math.cos(angulo);
            double y = centroY + radio * Math.sin(angulo);
            posiciones.put(id, new Posicion(x, y));
            indice++;
        }

        return Collections.unmodifiableMap(posiciones);
    }
}
