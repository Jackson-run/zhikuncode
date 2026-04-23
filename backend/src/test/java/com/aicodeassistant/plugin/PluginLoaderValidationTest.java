package com.aicodeassistant.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.Environment;

import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PluginLoader JAR 验证逻辑测试 (P0)
 */
class PluginLoaderValidationTest {

    private PluginLoader loader;
    private Method validateJarMethod;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Environment env = mock(Environment.class);
        // 设置较小的 JAR 大小限制以便测试
        when(env.getProperty("plugin.max-jar-size", Long.class, 50 * 1024 * 1024L))
                .thenReturn(1024L); // 1KB 限制
        loader = new PluginLoader(env);

        // 通过反射获取 private validateJar 方法
        validateJarMethod = PluginLoader.class.getDeclaredMethod("validateJar", Path.class);
        validateJarMethod.setAccessible(true);
    }

    private boolean invokeValidateJar(Path path) throws Exception {
        return (boolean) validateJarMethod.invoke(loader, path);
    }

    @Test
    @DisplayName("不存在的 JAR 文件应返回 false")
    void shouldRejectNonExistentJarFile() throws Exception {
        Path nonExistent = tempDir.resolve("nonexistent.jar");
        assertFalse(invokeValidateJar(nonExistent));
    }

    @Test
    @DisplayName("非 JAR 格式的文件应返回 false")
    void shouldHandleInvalidJarFormat() throws Exception {
        // 创建一个文本文件，改名为 .jar
        Path fakeJar = tempDir.resolve("fake.jar");
        Files.writeString(fakeJar, "This is not a JAR file");
        assertFalse(invokeValidateJar(fakeJar));
    }

    @Test
    @DisplayName("超过大小限制的 JAR 应返回 false")
    void shouldRejectOversizedJar() throws Exception {
        // 创建一个超过 1KB 限制的合法 JAR 文件
        Path oversizedJar = tempDir.resolve("oversized.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        try (JarOutputStream jos = new JarOutputStream(
                new FileOutputStream(oversizedJar.toFile()), manifest)) {
            jos.setLevel(0); // 禁用压缩，确保文件大小 > 写入数据量
            // 添加 SPI 文件
            ZipEntry spiEntry = new ZipEntry(
                    "META-INF/services/com.aicodeassistant.plugin.PluginExtension");
            spiEntry.setMethod(ZipEntry.STORED);
            byte[] spiData = "com.example.TestPlugin".getBytes();
            spiEntry.setSize(spiData.length);
            spiEntry.setCompressedSize(spiData.length);
            spiEntry.setCrc(crc32(spiData));
            jos.putNextEntry(spiEntry);
            jos.write(spiData);
            jos.closeEntry();
            // 添加大量填充数据使文件超过 1KB
            ZipEntry padding = new ZipEntry("padding.dat");
            padding.setMethod(ZipEntry.STORED);
            byte[] data = new byte[4096];
            java.util.Arrays.fill(data, (byte) 'X');
            padding.setSize(data.length);
            padding.setCompressedSize(data.length);
            padding.setCrc(crc32(data));
            jos.putNextEntry(padding);
            jos.write(data);
            jos.closeEntry();
        }
        // 验证文件确实超过限制
        assertTrue(Files.size(oversizedJar) > 1024,
                "JAR should be > 1KB, actual: " + Files.size(oversizedJar));
        assertFalse(invokeValidateJar(oversizedJar));
    }

    private static long crc32(byte[] data) {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data);
        return crc.getValue();
    }

    @Test
    @DisplayName("缺少 SPI 注册文件的 JAR 应返回 false")
    void shouldRejectJarWithoutSpiFile() throws Exception {
        // 创建大小限制足够的 loader
        Environment env = mock(Environment.class);
        when(env.getProperty("plugin.max-jar-size", Long.class, 50 * 1024 * 1024L))
                .thenReturn(50 * 1024 * 1024L);
        PluginLoader bigLoader = new PluginLoader(env);
        Method method = PluginLoader.class.getDeclaredMethod("validateJar", Path.class);
        method.setAccessible(true);

        // 创建不含 SPI 文件的 JAR
        Path noSpiJar = tempDir.resolve("no-spi.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        try (JarOutputStream jos = new JarOutputStream(
                new FileOutputStream(noSpiJar.toFile()), manifest)) {
            ZipEntry entry = new ZipEntry("com/example/SomeClass.class");
            jos.putNextEntry(entry);
            jos.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});
            jos.closeEntry();
        }

        assertFalse((boolean) method.invoke(bigLoader, noSpiJar));
    }

    @Test
    @DisplayName("合法 JAR（含 SPI 文件且未超限）应返回 true")
    void shouldAcceptValidJar() throws Exception {
        // 创建大小限制足够的 loader
        Environment env = mock(Environment.class);
        when(env.getProperty("plugin.max-jar-size", Long.class, 50 * 1024 * 1024L))
                .thenReturn(50 * 1024 * 1024L);
        PluginLoader bigLoader = new PluginLoader(env);
        Method method = PluginLoader.class.getDeclaredMethod("validateJar", Path.class);
        method.setAccessible(true);

        // 创建合法 JAR
        Path validJar = tempDir.resolve("valid.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        try (JarOutputStream jos = new JarOutputStream(
                new FileOutputStream(validJar.toFile()), manifest)) {
            ZipEntry spiEntry = new ZipEntry(
                    "META-INF/services/com.aicodeassistant.plugin.PluginExtension");
            jos.putNextEntry(spiEntry);
            jos.write("com.example.TestPlugin".getBytes());
            jos.closeEntry();
        }

        assertTrue((boolean) method.invoke(bigLoader, validJar));
    }
}
