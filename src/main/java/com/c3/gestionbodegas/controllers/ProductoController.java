package com.c3.gestionbodegas.controllers;

import java.util.List;

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

import com.c3.gestionbodegas.dto.mapper.ProductoMapper;
import com.c3.gestionbodegas.dto.producto.ProductoRequestDTO;
import com.c3.gestionbodegas.dto.producto.ProductoResponseDTO;
import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.exception.ResourceNotFoundException;
import com.c3.gestionbodegas.repository.BodegaRepository;
import com.c3.gestionbodegas.services.ProductoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoMapper productoMapper;
    private final BodegaRepository bodegaRepository;

    // ✅ Obtener todos los productos (DT3)
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodos();
        return ResponseEntity.ok(productoMapper.toDTOList(productos));
    }

    // ✅ Obtener productos paginados (DT9)
    @GetMapping("/paginado")
    public ResponseEntity<Page<ProductoResponseDTO>> obtenerTodosPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Producto> productos = productoService.obtenerTodosPaginado(pageable);
        return ResponseEntity.ok(productos.map(productoMapper::toDTO));
    }

    // ✅ Obtener un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Integer id) {
        Producto producto = productoService.obtenerPorId(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productoMapper.toDTO(producto));
    }

    // ✅ Crear un nuevo producto usando DTO (DT3)
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        Bodega bodega = null;
        if (dto.getBodegaId() != null) {
            bodega = bodegaRepository.findById(dto.getBodegaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada con ID: " + dto.getBodegaId()));
        }

        Producto nuevo = Producto.builder()
                .nombre(dto.getNombre())
                .categoria(dto.getCategoria())
                .stock(dto.getStock())
                .precio(dto.getPrecio())
                .bodega(bodega)
                .build();

        Producto guardado = productoService.guardar(nuevo);
        return ResponseEntity.ok(productoMapper.toDTO(guardado));
    }

    // ✅ Actualizar un producto existente usando DTO (DT3)
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoRequestDTO dto) {
        Bodega bodega = null;
        if (dto.getBodegaId() != null) {
            bodega = bodegaRepository.findById(dto.getBodegaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada con ID: " + dto.getBodegaId()));
        }

        Producto datosActualizados = Producto.builder()
                .nombre(dto.getNombre())
                .categoria(dto.getCategoria())
                .stock(dto.getStock())
                .precio(dto.getPrecio())
                .bodega(bodega)
                .build();

        Producto actualizado = productoService.actualizar(id, datosActualizados);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productoMapper.toDTO(actualizado));
    }

    // ✅ Eliminar un producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean eliminado = productoService.eliminar(id);
        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // ✅ Buscar producto por nombre exacto
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<ProductoResponseDTO> buscarPorNombre(@PathVariable String nombre) {
        Producto producto = productoService.buscarPorNombre(nombre);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productoMapper.toDTO(producto));
    }

    // ✅ Buscar productos por categoría (contiene, sin importar mayúsculas)
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorCategoria(@PathVariable String categoria) {
        List<Producto> productos = productoService.buscarPorCategoria(categoria);
        return ResponseEntity.ok(productoMapper.toDTOList(productos));
    }

    // ✅ Buscar productos con stock menor a X (para alertas)
    @GetMapping("/stock-bajo/{cantidad}")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorStockMenorA(@PathVariable Integer cantidad) {
        List<Producto> productos = productoService.buscarPorStockBajo(cantidad);
        return ResponseEntity.ok(productoMapper.toDTOList(productos));
    }

    // ✅ Verificar si existe un producto por nombre
    @GetMapping("/existe/{nombre}")
    public ResponseEntity<Boolean> existePorNombre(@PathVariable String nombre) {
        boolean existe = productoService.existePorNombre(nombre);
        return ResponseEntity.ok(existe);
    }

    // ✅ Obtener los productos más movidos (consulta personalizada)
    @GetMapping("/mas-movidos")
    public ResponseEntity<List<Object[]>> obtenerProductosMasMovidos() {
        List<Object[]> productos = productoService.obtenerProductosMasMovidos();
        return ResponseEntity.ok(productos);
    }
}
