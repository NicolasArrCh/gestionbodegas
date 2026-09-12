package com.c3.gestionbodegas.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c3.gestionbodegas.dto.bodega.BodegaRequestDTO;
import com.c3.gestionbodegas.dto.bodega.BodegaResponseDTO;
import com.c3.gestionbodegas.dto.mapper.BodegaMapper;
import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.exception.ResourceNotFoundException;
import com.c3.gestionbodegas.repository.UsuarioRepository;
import com.c3.gestionbodegas.services.BodegaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/bodegas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BodegaController {

    private final BodegaService bodegaService;
    private final BodegaMapper bodegaMapper;
    private final UsuarioRepository usuarioRepository;

    // ✅ Obtener todas las bodegas
    @GetMapping
    public ResponseEntity<List<BodegaResponseDTO>> obtenerTodas() {
        List<Bodega> bodegas = bodegaService.obtenerTodas();
        return ResponseEntity.ok(bodegaMapper.toDTOList(bodegas));
    }

    // ✅ Obtener bodegas paginadas (DT9)
    @GetMapping("/paginado")
    public ResponseEntity<Page<BodegaResponseDTO>> obtenerTodasPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Bodega> bodegas = bodegaService.obtenerTodasPaginado(pageable);
        return ResponseEntity.ok(bodegas.map(bodegaMapper::toDTO));
    }

    // ✅ Buscar una bodega por ID
    @GetMapping("/{id}")
    public ResponseEntity<BodegaResponseDTO> buscarPorId(@PathVariable Integer id) {
        Optional<Bodega> bodegaOpt = bodegaService.buscarPorId(id);
        return bodegaOpt.map(b -> ResponseEntity.ok(bodegaMapper.toDTO(b)))
                        .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Crear una nueva bodega usando DTO (DT3)
    @PostMapping
    public ResponseEntity<BodegaResponseDTO> crear(@Valid @RequestBody BodegaRequestDTO dto) {
        if (bodegaService.existePorNombre(dto.getNombre())) {
            return ResponseEntity.badRequest().build();
        }

        Usuario encargado = null;
        if (dto.getEncargadoId() != null) {
            encargado = usuarioRepository.findById(dto.getEncargadoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario encargado no encontrado con ID: " + dto.getEncargadoId()));
        }

        Bodega nueva = Bodega.builder()
                .nombre(dto.getNombre())
                .ubicacion(dto.getUbicacion())
                .capacidad(dto.getCapacidad())
                .encargado(encargado)
                .build();

        Bodega guardada = bodegaService.guardar(nueva);
        return ResponseEntity.ok(bodegaMapper.toDTO(guardada));
    }

    // ✅ Actualizar una bodega existente usando DTO (DT3)
    @PutMapping("/{id}")
    public ResponseEntity<BodegaResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody BodegaRequestDTO dto) {
        Optional<Bodega> existente = bodegaService.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Bodega actual = existente.get();
        actual.setNombre(dto.getNombre());
        actual.setUbicacion(dto.getUbicacion());
        actual.setCapacidad(dto.getCapacidad());

        if (dto.getEncargadoId() != null) {
            Usuario encargado = usuarioRepository.findById(dto.getEncargadoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario encargado no encontrado con ID: " + dto.getEncargadoId()));
            actual.setEncargado(encargado);
        }

        Bodega guardada = bodegaService.guardar(actual);
        return ResponseEntity.ok(bodegaMapper.toDTO(guardada));
    }

    // ✅ Eliminar una bodega por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Optional<Bodega> bodega = bodegaService.buscarPorId(id);
        if (bodega.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        bodegaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Buscar bodegas por nombre exacto
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<BodegaResponseDTO> buscarPorNombre(@PathVariable String nombre) {
        Bodega bodega = bodegaService.buscarPorNombre(nombre);
        if (bodega == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bodegaMapper.toDTO(bodega));
    }

    // ✅ Buscar bodegas por ubicación (coincidencia parcial)
    @GetMapping("/ubicacion/{ubicacion}")
    public ResponseEntity<List<BodegaResponseDTO>> buscarPorUbicacion(@PathVariable String ubicacion) {
        List<Bodega> bodegas = bodegaService.buscarPorUbicacion(ubicacion);
        return ResponseEntity.ok(bodegaMapper.toDTOList(bodegas));
    }

    // ✅ Buscar bodegas con capacidad menor a cierto valor
    @GetMapping("/capacidad/{capacidad}")
    public ResponseEntity<List<BodegaResponseDTO>> buscarPorCapacidadMenorA(@PathVariable Integer capacidad) {
        List<Bodega> bodegas = bodegaService.buscarPorCapacidadMenorA(capacidad);
        return ResponseEntity.ok(bodegaMapper.toDTOList(bodegas));
    }

    // ✅ Obtener resumen de stock total por bodega
    @GetMapping("/resumen-stock")
    public ResponseEntity<List<Object[]>> obtenerResumenStock() {
        List<Object[]> resumen = bodegaService.obtenerResumenStockPorBodega();
        return ResponseEntity.ok(resumen);
    }
}
