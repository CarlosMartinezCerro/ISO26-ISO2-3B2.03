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
import es.uclm.esi.iso2.radiodiagnostico.usuario.Medico;
import es.uclm.esi.iso2.radiodiagnostico.usuario.Paciente;

/** Implementacion simulada del sistema externo de historial clinico. */
public class HistorialClinicoSimulado implements HistorialClinico {

    private static final String CSV_NOMBRE = "HistorialClinicoBBDD.csv";
    private final List<Informe> informesArchivados = new ArrayList<>();
    private final Path fichero;

    public HistorialClinicoSimulado() {
        fichero = localizarFichero();
        try {
            cargar();
        } catch (Exception excepcion) {
            throw new IllegalStateException("El sistema externo de historial clinico no esta disponible.",
                    excepcion);
        }
    }

    @Override
    public void getAntecedentes(Informe informe) {
        validar(informe);
        String dni = informe.getPaciente().getDni().trim();
        String especialidad = informe.getEspecialidad().trim();

        informe.setAntecedentes("Antecedentes simulados del paciente " + dni + ".");
        informe.setHistorialEspecialidad(
                "Historial simulado de " + especialidad + " para el paciente " + dni + ".");
    }

    @Override
    public void archivar(Informe informe) {
        validar(informe);
        synchronized (informesArchivados) {
            long maximo = informesArchivados.stream()
                    .mapToLong(Informe::getId).max().orElse(0L);
            if (informe.getId() <= 0) {
                informe.setId(maximo + 1);
            } else {
                maximo = Math.max(maximo, informe.getId());
            }

            boolean reemplazado = false;
            for (int i = 0; i < informesArchivados.size(); i++) {
                if (informesArchivados.get(i).getId() == informe.getId()) {
                    informesArchivados.set(i, informe);
                    reemplazado = true;
                    break;
                }
            }
            if (!reemplazado) {
                informesArchivados.add(informe);
            }
            try {
                escribir();
            } catch (Exception excepcion) {
                throw new IllegalStateException("El sistema externo de historial clinico no esta disponible.",
                        excepcion);
            }
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
                if (informe.getPaciente() != null
                        && dniPaciente.trim().equals(informe.getPaciente().getDni())) {
                    resultado.add(new InformeResumen(informe.getId(), informe.getEspecialidad()));
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
                if (informe.getId() == idInforme) {
                    return informe;
                }
            }
        }
        return null;
    }

    /** Devuelve una copia inmodificable de los informes archivados. */
    /*public List<Informe> obtenerInformesArchivados() {
        synchronized (informesArchivados) {
            return List.copyOf(informesArchivados);
        }
    }*/

    private void validar(Informe informe) {
        if (informe == null) {
            throw new IllegalArgumentException("El informe es obligatorio.");
        }
        if (informe.getPaciente() == null
                || informe.getPaciente().getDni() == null
                || informe.getPaciente().getDni().isBlank()) {
            throw new IllegalArgumentException("El DNI del paciente es obligatorio.");
        }
        if (informe.getEspecialidad() == null || informe.getEspecialidad().isBlank()) {
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
            if (Files.exists(candidato)
                    || (Files.exists(candidato.getParent()) && !candidato.toString().contains("target"))) {
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
            int inicio = 0;
            Map<String, Integer> columnas = new LinkedHashMap<>();
            if (pareceCabecera(filas.get(0))) {
                List<String> cabecera = filas.get(0);
                for (int i = 0; i < cabecera.size(); i++) {
                    columnas.put(normalizar(cabecera.get(i)), i);
                }
                inicio = 1;
            } else {
                inicio = 0;
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
                    // Una fila incompleta o antigua no impide cargar las demás.
                }
            }
        }
    }

    private boolean pareceCabecera(List<String> fila) {
        for (String valor : fila) {
            String normalizado = normalizar(valor);
            if (normalizado.contains("id") || normalizado.contains("dni")
                    || normalizado.contains("especialidad") || normalizado.contains("fecha")) {
                return true;
            }
        }
        return false;
    }

    private Informe informeCabecera(List<String> fila, Map<String, Integer> columnas) {
        String dniMedico = valor(fila, columnas,
                "dnimedico", "medicodni", "medico_dni");
        Medico medico = dniMedico.isBlank() ? null : new Medico(
                valor(fila, columnas, "mediconombre", "medico_nombre"),
                valor(fila, columnas, "medicoapellido", "medico_apellido"),
                dniMedico,
                valor(fila, columnas, "mediconumerotelefono", "medico_numeroTelefono", "medico_telefono"),
                valor(fila, columnas, "mediconumeroseguridadsocial", "medico_numeroSeguridadSocial"),
                fecha(valor(fila, columnas, "medicofechanacimiento", "medico_fechaNacimiento")),
                valor(fila, columnas, "medicodomicilio", "medico_domicilio"),
                valor(fila, columnas, "medicoespecialidad", "medico_especialidad"));

        String dniPaciente = valor(fila, columnas,
                "dnipaciente", "pacientedni", "paciente_dni");
        Paciente paciente = dniPaciente.isBlank() ? null : new Paciente(
                valor(fila, columnas, "pacientenombre", "paciente_nombre"),
                valor(fila, columnas, "pacienteapellido", "paciente_apellido"),
                dniPaciente,
                valor(fila, columnas, "pacientenumerotelefono", "paciente_numeroTelefono", "paciente_telefono"),
                valor(fila, columnas, "pacientenumeroseguridadsocial", "paciente_numeroSeguridadSocial"),
                fecha(valor(fila, columnas, "pacientefechanacimiento", "paciente_fechaNacimiento")),
                valor(fila, columnas, "pacientedomicilio", "paciente_domicilio"));

        String especialidad = valor(fila, columnas, "especialidad");
        Informe informe = new Informe(
                entero(valor(fila, columnas, "id")),
                medico,
                paciente,
                especialidad,
                valor(fila, columnas, "motivoconsulta"),
                valor(fila, columnas, "consultaactual"),
                valor(fila, columnas, "diagnostico"),
                valor(fila, columnas, "pruebas"),
                valor(fila, columnas, "tratamiento"),
                Boolean.parseBoolean(valor(fila, columnas, "alta")),
                fecha(valor(fila, columnas, "fecha")),
                valor(fila, columnas, "anamnesis"));
        informe.setAntecedentes(valor(fila, columnas, "antecedentes"));
        informe.setHistorialEspecialidad(valor(fila, columnas, "historialespecialidad"));
        if (paciente == null) {
            throw new IllegalArgumentException("La fila no contiene paciente.");
        }
        return informe;
    }

    private Informe informePosicional(List<String> fila) {
        // Formato compatible: id;dniMedico;dniPaciente;especialidad;motivoConsulta;
        // antecedentes;historialEspecialidad;consultaActual;diagnostico;pruebas;
        // tratamiento;alta;fecha;anamnesis
        long id = entero(pos(fila, 0));
        String dniMedico = pos(fila, 1);
        String dniPaciente = pos(fila, 2);
        Medico medico = dniMedico.isBlank() ? null : new Medico("", "", dniMedico, "", "", null, "",
                pos(fila, 3));
        Paciente paciente = dniPaciente.isBlank() ? null : new Paciente("", "", dniPaciente, "",
                "", null, "");
        Informe informe = new Informe(id, medico, paciente, pos(fila, 3), pos(fila, 4),
                pos(fila, 7), pos(fila, 8), pos(fila, 9), pos(fila, 10),
                Boolean.parseBoolean(pos(fila, 11)), fecha(pos(fila, 12)), pos(fila, 13));
        informe.setAntecedentes(pos(fila, 5));
        informe.setHistorialEspecialidad(pos(fila, 6));
        if (paciente == null) {
            throw new IllegalArgumentException("La fila no contiene paciente.");
        }
        return informe;
    }

    private void escribir() throws Exception {
        Files.createDirectories(fichero.getParent());
        String[] cabecera = {
                "id",
                "medico_nombre", "medico_apellido", "dniMedico", "medico_numeroTelefono",
                "medico_numeroSeguridadSocial", "medico_fechaNacimiento", "medico_domicilio",
                "medico_especialidad",
                "paciente_nombre", "paciente_apellido", "dniPaciente", "paciente_numeroTelefono",
                "paciente_numeroSeguridadSocial", "paciente_fechaNacimiento", "paciente_domicilio",
                "especialidad", "motivoConsulta", "antecedentes", "historialEspecialidad",
                "consultaActual", "diagnostico", "pruebas", "tratamiento", "alta", "fecha", "anamnesis"
        };
        try (BufferedWriter escritor = Files.newBufferedWriter(fichero, StandardCharsets.UTF_8)) {
            escritor.write(unirCabecera(cabecera));
            escritor.newLine();
            synchronized (informesArchivados) {
                for (Informe informe : informesArchivados) {
                    Medico medico = informe.getMedico();
                    Paciente paciente = informe.getPaciente();
                    String[] fila = {
                            Long.toString(informe.getId()),
                            texto(medico == null ? null : medico.getNombre()),
                            texto(medico == null ? null : medico.getApellido()),
                            texto(medico == null ? null : medico.getDni()),
                            texto(medico == null ? null : medico.getNumeroTelefono()),
                            texto(medico == null ? null : medico.getNumeroSeguridadSocial()),
                            texto(medico == null || medico.getFechaNacimiento() == null
                                    ? null : medico.getFechaNacimiento().toString()),
                            texto(medico == null ? null : medico.getDomicilio()),
                            texto(medico == null ? null : medico.getEspecialidad()),
                            texto(paciente == null ? null : paciente.getNombre()),
                            texto(paciente == null ? null : paciente.getApellido()),
                            texto(paciente == null ? null : paciente.getDni()),
                            texto(paciente == null ? null : paciente.getNumeroTelefono()),
                            texto(paciente == null ? null : paciente.getNumeroSeguridadSocial()),
                            texto(paciente == null || paciente.getFechaNacimiento() == null
                                    ? null : paciente.getFechaNacimiento().toString()),
                            texto(paciente == null ? null : paciente.getDomicilio()),
                            texto(informe.getEspecialidad()),
                            texto(informe.getMotivoConsulta()),
                            texto(informe.obtenerAntecedentes()),
                            texto(informe.obtenerHistorial()),
                            texto(informe.getConsultaActual()),
                            texto(informe.getDiagnostico()),
                            texto(informe.getPruebas()),
                            texto(informe.getTratamiento()),
                            Boolean.toString(informe.isAlta()),
                            texto(informe.getFecha() == null ? null : informe.getFecha().toString()),
                            texto(informe.getAnamnesis())
                    };
                    escritor.write(unirFila(fila));
                    escritor.newLine();
                }
            }
        }
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

    private static String unirCabecera(String[] cabecera) {
        String[] copia = cabecera.clone();
        return unirFila(copia);
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
        if (valor == null) {
            return "";
        }
        return valor.toLowerCase().replace("_", "").replace("-", "").replace(" ", "").trim();
    }

    private static String valor(List<String> fila, Map<String, Integer> columnas, String... nombres) {
        for (String nombre : nombres) {
            Integer indice = columnas.get(normalizar(nombre));
            if (indice != null && indice < fila.size()) {
                return texto(fila.get(indice));
            }
        }
        return "";
    }

    private static String pos(List<String> fila, int indice) {
        return indice < fila.size() ? texto(fila.get(indice)) : "";
    }

    private static long entero(String valor) {
        if (valor == null || valor.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(valor.trim());
        } catch (NumberFormatException excepcion) {
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
