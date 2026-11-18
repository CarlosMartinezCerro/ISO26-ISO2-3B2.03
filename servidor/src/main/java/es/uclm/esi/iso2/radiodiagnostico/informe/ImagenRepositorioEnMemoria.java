package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImagenRepositorioEnMemoria implements ImagenRepositorio {

    private final List<Imagen> imagenes = new ArrayList<>();

    @Override
    public void guardar(Imagen imagen) {
        imagenes.add(imagen);
    }

    @Override
    public Optional<Imagen> buscarPorId(String id) {
        return imagenes.stream()
                .filter(imagen -> imagen.id().equals(id))
                .findFirst();
    }

    @Override
    public List<Imagen> buscarPorInforme(String idInforme) {
        return imagenes.stream()
                .filter(imagen -> imagen.idInforme().equals(idInforme))
                .toList();
    }

    @Override
    public List<Imagen> buscarPorPaciente(String dniPaciente) {
        return imagenes.stream()
                .filter(imagen -> imagen.dniPaciente().equalsIgnoreCase(dniPaciente))
                .toList();
    }

    @Override
    public int contar() {
        return imagenes.size();
    }
}