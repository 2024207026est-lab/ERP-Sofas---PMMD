package pe.erp.sofas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.model.ConfiguracionGuardadaPMMD;
import pe.erp.sofas.model.UsuarioPMMD;
import pe.erp.sofas.repository.ConfiguracionGuardadaRepositoryPMMD;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersistenciaConfiguracionServiceTestPMMD {

    private PersistenciaConfiguracionServicePMMD servicePMMD;

    @Mock
    private ConfiguracionGuardadaRepositoryPMMD repositoryPMMD;

    private UsuarioPMMD usuarioTestPMMD;

    private ConfigSofaPMMD configTestPMMD;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servicePMMD = new PersistenciaConfiguracionServicePMMD(repositoryPMMD);

        usuarioTestPMMD = new UsuarioPMMD();
        usuarioTestPMMD.setId(1L);
        usuarioTestPMMD.setNombre("Test User");

        configTestPMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO,
            3,
            ConfigSofaPMMD.Chaise.NINGUNA,
            92,
            null,
            ConfigSofaPMMD.Brazo.RECTO,
            ConfigSofaPMMD.Patas.MADERA,
            ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23,
            1,
            null,
            ConfigSofaPMMD.Mecanismo.FIJO
        );
    }

    @Test
    void testGuardarConfiguracionPMMD() {
        ConfiguracionGuardadaPMMD guardadaPMMD = ConfiguracionGuardadaPMMD.builder()
            .id(1L)
            .usuario(usuarioTestPMMD)
            .nombre("Mi Moderno")
            .modelo(ConfigSofaPMMD.Modelo.MODERNO)
            .cuerpos(3)
            .build();

        when(repositoryPMMD.save(any(ConfiguracionGuardadaPMMD.class))).thenReturn(guardadaPMMD);

        ConfiguracionGuardadaPMMD resultadoPMMD = servicePMMD.guardar(usuarioTestPMMD, configTestPMMD, "Mi Moderno", "Descripción");

        assertNotNull(resultadoPMMD);
        assertEquals("Mi Moderno", resultadoPMMD.getNombre());
        assertEquals(usuarioTestPMMD, resultadoPMMD.getUsuario());
        verify(repositoryPMMD).save(any(ConfiguracionGuardadaPMMD.class));
    }

    @Test
    void testCargarConfiguracionPMMD() {
        ConfiguracionGuardadaPMMD guardadaPMMD = ConfiguracionGuardadaPMMD.builder()
            .id(1L)
            .usuario(usuarioTestPMMD)
            .nombre("Mi Moderno")
            .modelo(ConfigSofaPMMD.Modelo.MODERNO)
            .cuerpos(3)
            .build();

        when(repositoryPMMD.findByIdAndUsuario(1L, usuarioTestPMMD)).thenReturn(Optional.of(guardadaPMMD));

        ConfigSofaPMMD resultadoPMMD = servicePMMD.cargar(1L, usuarioTestPMMD);

        assertNotNull(resultadoPMMD);
        assertEquals(ConfigSofaPMMD.Modelo.MODERNO, resultadoPMMD.modelo());
        verify(repositoryPMMD).findByIdAndUsuario(1L, usuarioTestPMMD);
    }

    @Test
    void testListarPorUsuarioPMMD() {
        ConfiguracionGuardadaPMMD guardada1PMMD = ConfiguracionGuardadaPMMD.builder()
            .id(1L)
            .usuario(usuarioTestPMMD)
            .nombre("Config 1")
            .build();

        ConfiguracionGuardadaPMMD guardada2PMMD = ConfiguracionGuardadaPMMD.builder()
            .id(2L)
            .usuario(usuarioTestPMMD)
            .nombre("Config 2")
            .build();

        when(repositoryPMMD.findByUsuarioOrderByFechaCreacionDesc(usuarioTestPMMD))
            .thenReturn(List.of(guardada2PMMD, guardada1PMMD));

        List<ConfiguracionGuardadaPMMD> resultadoPMMD = servicePMMD.listarPorUsuario(usuarioTestPMMD);

        assertEquals(2, resultadoPMMD.size());
        verify(repositoryPMMD).findByUsuarioOrderByFechaCreacionDesc(usuarioTestPMMD);
    }

    @Test
    void testMarcarFavoritoPMMD() {
        ConfiguracionGuardadaPMMD guardadaPMMD = ConfiguracionGuardadaPMMD.builder()
            .id(1L)
            .usuario(usuarioTestPMMD)
            .nombre("Mi Moderno")
            .esFavorito(false)
            .build();

        when(repositoryPMMD.findByIdAndUsuario(1L, usuarioTestPMMD)).thenReturn(Optional.of(guardadaPMMD));
        when(repositoryPMMD.save(guardadaPMMD)).thenReturn(guardadaPMMD);

        servicePMMD.marcarFavorito(1L, usuarioTestPMMD, true);

        assertTrue(guardadaPMMD.getEsFavorito());
        verify(repositoryPMMD).save(guardadaPMMD);
    }

    @Test
    void testEliminarPMMD() {
        ConfiguracionGuardadaPMMD guardadaPMMD = ConfiguracionGuardadaPMMD.builder()
            .id(1L)
            .usuario(usuarioTestPMMD)
            .nombre("Mi Moderno")
            .build();

        when(repositoryPMMD.findByIdAndUsuario(1L, usuarioTestPMMD)).thenReturn(Optional.of(guardadaPMMD));

        servicePMMD.eliminar(1L, usuarioTestPMMD);

        verify(repositoryPMMD).delete(guardadaPMMD);
    }

    @Test
    void testContarPorUsuarioPMMD() {
        when(repositoryPMMD.countByUsuario(usuarioTestPMMD)).thenReturn(5L);

        long resultadoPMMD = servicePMMD.contarPorUsuario(usuarioTestPMMD);

        assertEquals(5L, resultadoPMMD);
        verify(repositoryPMMD).countByUsuario(usuarioTestPMMD);
    }
}
