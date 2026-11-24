package es.uclm.esi.iso2.radiodiagnostico.sistemasexternos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;

/** Implementacion simulada del sistema externo de historial clinico. */
public class HistorialClinicoSimulado implements HistorialClinico {

    private static final String CSV_NOMBRE = "HistorialClinicoBBDD.csv";
    private static final String[] CABECERA = {
            "id", "dniMedico", "dniPaciente", "especialidad", "motivoConsulta",
            "antecedentes", "historialEspecialidad", "consultaActual", "diagnostico",
            "pruebas", "tratamiento", "alta", "fecha", "anamnesis"
    };

    private final List<Informe> informesArchivados = new ArrayList<>();
    private final Path fichero;

    public HistorialClinicoSimulado() {
        fichero = localizarFichero();
        try {
            cargar();
        } catch (Exception excepcion) {
            throw new IllegalStateException(
                    "El sistema externo de historial clinico no esta disponible.", excepcion);
        }
    }

    @Override
    public Informe getAntecedentes(Informe informe) {
        validar(informe);
        String dni = informe.dniPaciente().trim();
        String especialidad = informe.especialidad().trim();
        return informe.conAntecedentes(
                "Antecedentes simulados del paciente " + dni + ".",
                "Historial simulado de " + especialidad + " para el paciente " + dni + ".");
    }

    @Override
    public Informe archivar(Informe informe) {
        validar(informe);
        if (informe.dniMedico() == null || informe.dniMedico().isBlank()) {
            throw new IllegalArgumentException("El DNI del medico es obligatorio.");
        }
        synchronized (informesArchivados) {
            long maximo = informesArchivados.stream()
                    .mapToLong(Informe::id).max().orElse(0L);
            Informe archivado = informe.id() <= 0
                    ? informe.conId(maximo + 1)
                    : informe.conId(informe.id());

            boolean reemplazado = false;
            for (int i = 0; i < informesArchivados.size(); i++) {
                if (informesArchivados.get(i).id() == archivado.id()) {
                    informesArchivados.set(i, archivado);
                    reemplazado = true;
                    break;
                }
            }
            if (!reemplazado) {
                informesArchivados.add(archivado);
            }
            try {
                escribir();
            } catch (Exception excepcion) {
                throw new IllegalStateException(
                        "El sistema externo de historial clinico no esta disponible.", excepcion);
            }
            return archivado;
        }
    }

    @Override
    public List<InformeResumen> getInformes(String dniPaciente) {
        if (dniPaciente == null || dniPaciente.isBlank()) {
            throw new IllegalArgumentException("El DNI del paciente es obligatorio.");
        }
        List<InformeResumen> resultado = new ArrayList<>();
        synchronized (informesArchivados) {
            for (Informe informe : informesArchivados) {
                if (dniPaciente.trim().equals(informe.dniPaciente())) {
                    resultado.add(new InformeResumen(
                            informe.id(), informe.especialidad(), informe.fecha()));
                }
            }
        }
        return resultado;
    }

    @Override
    public Informe getInforme(long idInforme) {
        if (idInforme <= 0) {
            throw new IllegalArgumentException("El identificador del informe no es valido.");
        }
        synchronized (informesArchivados) {
            for (Informe informe : informesArchivados) {
                if (informe.id() == idInforme) {
                    return informe;
                }
            }
        }
        return null;
    }

    private void validar(Informe informe) {
        if (informe == null) {
            throw new IllegalArgumentException("El informe es obligatorio.");
        }
        if (informe.dniPaciente() == null || informe.dniPaciente().isBlank()) {
            throw new IllegalArgumentException("El DNI del paciente es obligatorio.");
        }
        if (informe.especialidad() == null || informe.especialidad().isBlank()) {
            throw new IllegalArgumentException("La especialidad es obligatoria.");
        }
    }

    private static Path localizarFichero() {
        Path[] candidatos = {
                Paths.get("servidor", "src", "main", "resources", CSV_NOMBRE),
                Paths.get("src", "main", "resources", CSV_NOMBRE),
                Paths.get("servidor", "target", "classes", CSV_NOMBRE),
                Paths.get("target", "classes", CSV_NOMBRE)
        };
        for (Path candidato : candidatos) {
            if (Files.exists(candidato)) {
                return candidato;
            }
        }
        return candidatos[0];
    }

    private void cargar() throws Exception {
        informesArchivados.clear();
        if (!Files.exists(fichero)) {
            Files.createDirectories(fichero.getParent());
            return;
        }
        try (BufferedReader lector = Files.newBufferedReader(fichero, StandardCharsets.UTF_8)) {
            List<List<String>> filas = leerFilas(lector);
            if (filas.isEmpty()) {
                return;
            }
            Map<String, Integer> columnas = new LinkedHashMap<>();
            int inicio = 0;
            if (pareceCabecera(filas.get(0))) {
                for (int i = 0; i < filas.get(0).size(); i++) {
                    columnas.put(normalizar(filas.get(0).get(i)), i);
                }
                inicio = 1;
            }
            for (int i = inicio; i < filas.size(); i++) {
                List<String> fila = filas.get(i);
                if (fila.isEmpty() || fila.stream().allMatch(String::isBlank)) {
                    continue;
                }
                try {
                    Informe informe = columnas.isEmpty()
                            ? informePosicional(fila)
                            : informeCabecera(fila, columnas);
                    informesArchivados.add(informe);
                } catch (RuntimeException ignorada) {
                    // Una fila incompleta no impide cargar las demás.
                }
            }
        }
    }

    private Informe informeCabecera(List<String> fila, Map<String, Integer> columnas) {
        String dniMedico = valor(fila, columnas, "dnimedico");
        String dniPaciente = valor(fila, columnas, "dnipaciente");
        if (dniPaciente.isBlank()) {
            throw new IllegalArgumentException("La fila no contiene paciente.");
        }
        return new Informe(
                entero(valor(fila, columnas, "id")),
                dniMedico,
                dniPaciente,
                valor(fila, columnas, "especialidad"),
                valor(fila, columnas, "motivoconsulta"),
                valor(fila, columnas, "antecedentes"),
                valor(fila, columnas, "historialespecialidad"),
                valor(fila, columnas, "consultaactual"),
                valor(fila, columnas, "diagnostico"),
                valor(fila, columnas, "pruebas"),
                valor(fila, columnas, "tratamiento"),
                Boolean.parseBoolean(valor(fila, columnas, "alta")),
                fecha(valor(fila, columnas, "fecha")),
                valor(fila, columnas, "anamnesis"));
    }

    private Informe informePosicional(List<String> fila) {
        String dniPaciente = pos(fila, 2);
        if (dniPaciente.isBlank()) {
            throw new IllegalArgumentException("La fila no contiene paciente.");
        }
        return new Informe(
                entero(pos(fila, 0)),
                pos(fila, 1),
                dniPaciente,
                pos(fila, 3),
                pos(fila, 4),
                pos(fila, 5),
                pos(fila, 6),
                pos(fila, 7),
                pos(fila, 8),
                pos(fila, 9),
                pos(fila, 10),
                Boolean.parseBoolean(pos(fila, 11)),
                fecha(pos(fila, 12)),
                pos(fila, 13));
    }

    private void escribir() throws Exception {
        Files.createDirectories(fichero.getParent());
        try (BufferedWriter escritor = Files.newBufferedWriter(fichero, StandardCharsets.UTF_8)) {
            escritor.write(unirFila(CABECERA));
            escritor.newLine();
            for (Informe informe : informesArchivados) {
                String[] fila = {
                        Long.toString(informe.id()),
                        texto(informe.dniMedico()),
                        texto(informe.dniPaciente()),
                        texto(informe.especialidad()),
                        texto(informe.motivoConsulta()),
                        texto(informe.antecedentes()),
                        texto(informe.historialEspecialidad()),
                        texto(informe.consultaActual()),
                        texto(informe.diagnostico()),
                        texto(informe.pruebas()),
                        texto(informe.tratamiento()),
                        Boolean.toString(informe.alta()),
                        texto(informe.fecha() == null ? null : informe.fecha().toString()),
                        texto(informe.anamnesis())
                };
                escritor.write(unirFila(fila));
                escritor.newLine();
            }
        }
    }

    private static boolean pareceCabecera(List<String> fila) {
        for (String valor : fila) {
            String normalizado = normalizar(valor);
            if (normalizado.equals("id") || normalizado.contains("dni")
                    || normalizado.equals("especialidad") || normalizado.equals("fecha")) {
                return true;
            }
        }
        return false;
    }

    private static List<List<String>> leerFilas(BufferedReader lector) throws Exception {
        StringBuilder contenido = new StringBuilder();
        char[] buffer = new char[4096];
        int leidos;
        while ((leidos = lector.read(buffer)) != -1) {
            contenido.append(buffer, 0, leidos);
        }
        List<List<String>> filas = new ArrayList<>();
        List<String> fila = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean comillas = false;
        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);
            if (c == '"') {
                if (comillas && i + 1 < contenido.length() && contenido.charAt(i + 1) == '"') {
                    campo.append('"');
                    i++;
                } else {
                    comillas = !comillas;
                }
            } else if (c == ';' && !comillas) {
                fila.add(campo.toString());
                campo.setLength(0);
            } else if ((c == '\n' || c == '\r') && !comillas) {
                if (c == '\r' && i + 1 < contenido.length() && contenido.charAt(i + 1) == '\n') {
                    i++;
                }
                fila.add(campo.toString());
                campo.setLength(0);
                filas.add(fila);
                fila = new ArrayList<>();
            } else {
                campo.append(c);
            }
        }
        if (campo.length() > 0 || !fila.isEmpty()) {
            fila.add(campo.toString());
            filas.add(fila);
        }
        return filas;
    }

    private static String unirFila(String[] fila) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < fila.length; i++) {
            if (i > 0) {
                resultado.append(';');
            }
            resultado.append(escapar(fila[i]));
        }
        return resultado.toString();
    }

    private static String escapar(String valor) {
        String texto = texto(valor);
        if (texto.indexOf(';') >= 0 || texto.indexOf('"') >= 0
                || texto.indexOf('\n') >= 0 || texto.indexOf('\r') >= 0) {
            return '"' + texto.replace("\"", "\"\"") + '"';
        }
        return texto;
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static String normalizar(String valor) {
        return valor == null
                ? ""
                : valor.toLowerCase().replace("_", "").replace("-", "").replace(" ", "").trim();
    }

    private static String valor(List<String> fila, Map<String, Integer> columnas, String nombre) {
        Integer indice = columnas.get(normalizar(nombre));
        return indice != null && indice < fila.size() ? texto(fila.get(indice)) : "";
    }

    private static String pos(List<String> fila, int indice) {
        return indice < fila.size() ? texto(fila.get(indice)) : "";
    }

    private static long entero(String valor) {
        try {
            return Long.parseLong(valor.trim());
        } catch (Exception excepcion) {
            return 0L;
        }
    }

    private static LocalDate fecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (Exception excepcion) {
            return null;
        }
    }
}
