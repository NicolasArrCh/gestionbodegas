package com.c3.gestionbodegas.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.c3.gestionbodegas.services.BodegaService;
import com.c3.gestionbodegas.services.MovimientoInventarioService;
import com.c3.gestionbodegas.services.ProductoService;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    @Autowired
    private BodegaService bodegaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private MovimientoInventarioService movimientoService;

    /**
     * Endpoint de reporte general del sistema
     * GET /api/reportes/resumen-general
     */
    @GetMapping("/resumen-general")
    public ResponseEntity<Map<String, Object>> obtenerResumenGeneral() {
        Map<String, Object> reporte = new HashMap<>();

        try {
            // 📊 Stock total por bodega
            List<Object[]> stockPorBodega = bodegaService.obtenerResumenStockPorBodega();
            reporte.put("stockPorBodega", stockPorBodega != null ? stockPorBodega : new ArrayList<>());

            // 📦 Productos más movidos
            List<Object[]> productosMasMovidos = productoService.obtenerProductosMasMovidos();
            reporte.put("productosMasMovidos", productosMasMovidos != null ? productosMasMovidos : new ArrayList<>());

            // 🔻 Productos con stock bajo (< 10)
            List<Object[]> productosStockBajo = productoService.buscarPorStockBajo(10)
                    .stream()
                    .map(p -> new Object[]{p.getId(), p.getNombre(), p.getStock()})
                    .toList();
            reporte.put("productosStockBajo", productosStockBajo);

            // 📈 Total de bodegas
            reporte.put("totalBodegas", bodegaService.obtenerTodas().size());

            // 📦 Total de productos
            reporte.put("totalProductos", productoService.obtenerTodos().size());

            // ↔️ Total de movimientos
            reporte.put("totalMovimientos", movimientoService.obtenerTodos().size());

        } catch (Exception e) {
            System.err.println("❌ Error generando reporte: " + e.getMessage());
            e.printStackTrace();
            
            // Retornar reporte con valores por defecto en caso de error
            reporte.put("error", "No se pudieron cargar algunos datos del reporte");
            reporte.put("totalBodegas", 0);
            reporte.put("totalProductos", 0);
            reporte.put("totalMovimientos", 0);
        }

        return ResponseEntity.ok(reporte);
    }
}