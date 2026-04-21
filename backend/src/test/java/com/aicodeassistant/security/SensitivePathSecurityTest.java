package com.aicodeassistant.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 敏感文件路径安全检测测试 — 验证 PathSecurityService.checkSensitiveFileRead()
 * 确保在 BYPASS 模式下也能拦截对敏感系统文件的读取操作。
 */
@DisplayName("Sensitive File Path Security Tests")
class SensitivePathSecurityTest {

    private PathSecurityService service;

    @BeforeEach
    void setUp() {
        service = new PathSecurityService();
    }

    // ===== 应被拦截的命令 =====

    @Test
    @DisplayName("cat /etc/shadow 应被拦截")
    void shouldBlockCatEtcShadow() {
        String result = service.checkSensitiveFileRead("cat /etc/shadow");
        assertThat(result).isNotNull();
        assertThat(result).contains("/etc/shadow");
    }

    @Test
    @DisplayName("grep password /etc/shadow 应被拦截")
    void shouldBlockGrepEtcShadow() {
        String result = service.checkSensitiveFileRead("grep password /etc/shadow");
        assertThat(result).isNotNull();
        assertThat(result).contains("/etc/shadow");
    }

    @Test
    @DisplayName("head -n 5 /etc/passwd 应被拦截")
    void shouldBlockHeadEtcPasswd() {
        String result = service.checkSensitiveFileRead("head -n 5 /etc/passwd");
        assertThat(result).isNotNull();
        assertThat(result).contains("/etc/passwd");
    }

    @Test
    @DisplayName("cat ~/.ssh/id_rsa 应被拦截")
    void shouldBlockCatSshKey() {
        String result = service.checkSensitiveFileRead("cat ~/.ssh/id_rsa");
        assertThat(result).isNotNull();
        assertThat(result).contains("id_rsa");
    }

    @Test
    @DisplayName("cat ~/.ssh/id_ed25519 应被拦截")
    void shouldBlockCatSshEd25519() {
        String result = service.checkSensitiveFileRead("cat ~/.ssh/id_ed25519");
        assertThat(result).isNotNull();
        assertThat(result).contains("id_ed25519");
    }

    @Test
    @DisplayName("cat ~/.aws/credentials 应被拦截")
    void shouldBlockCatAwsCredentials() {
        String result = service.checkSensitiveFileRead("cat ~/.aws/credentials");
        assertThat(result).isNotNull();
        assertThat(result).contains("credentials");
    }

    @Test
    @DisplayName("管道命令 cat /etc/shadow | grep root 应被拦截")
    void shouldBlockPipedSensitiveRead() {
        String result = service.checkSensitiveFileRead("cat /etc/shadow | grep root");
        assertThat(result).isNotNull();
        assertThat(result).contains("/etc/shadow");
    }

    @Test
    @DisplayName("tail -f /etc/sudoers 应被拦截")
    void shouldBlockTailSudoers() {
        String result = service.checkSensitiveFileRead("tail -f /etc/sudoers");
        assertThat(result).isNotNull();
        assertThat(result).contains("/etc/sudoers");
    }

    @Test
    @DisplayName("strings /proc/1/environ 应被拦截")
    void shouldBlockProcEnviron() {
        String result = service.checkSensitiveFileRead("strings /proc/1/environ");
        assertThat(result).isNotNull();
        assertThat(result).contains("/proc/");
    }

    @Test
    @DisplayName("cat /sys/class/dmi/id/product_serial 应被拦截")
    void shouldBlockSysAccess() {
        String result = service.checkSensitiveFileRead("cat /sys/class/dmi/id/product_serial");
        assertThat(result).isNotNull();
        assertThat(result).contains("/sys/");
    }

    @Test
    @DisplayName("less ~/.kube/config 应被拦截")
    void shouldBlockKubeConfig() {
        String result = service.checkSensitiveFileRead("less ~/.kube/config");
        assertThat(result).isNotNull();
        assertThat(result).contains("kube/config");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "cat /etc/shadow",
        "less /etc/passwd",
        "more /etc/sudoers",
        "head /etc/shadow",
        "tail /etc/passwd",
        "grep root /etc/shadow",
        "xxd /etc/shadow",
        "od /etc/shadow",
        "strings /etc/shadow",
        "base64 /etc/shadow"
    })
    @DisplayName("各种读取命令访问敏感文件都应被拦截")
    void shouldBlockVariousReadCommands(String command) {
        String result = service.checkSensitiveFileRead(command);
        assertThat(result).isNotNull()
                .describedAs("Command '%s' should be blocked", command);
    }

    // ===== 不应被拦截的命令 =====

    @Test
    @DisplayName("cat /etc/hosts 应不被拦截")
    void shouldAllowCatEtcHosts() {
        String result = service.checkSensitiveFileRead("cat /etc/hosts");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("cat /tmp/test.txt 应不被拦截")
    void shouldAllowCatTmpFile() {
        String result = service.checkSensitiveFileRead("cat /tmp/test.txt");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("echo hello 应不被拦截")
    void shouldAllowEchoCommand() {
        String result = service.checkSensitiveFileRead("echo hello");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("ls /etc/ 应不被拦截（ls 不是读取命令）")
    void shouldAllowLsCommand() {
        String result = service.checkSensitiveFileRead("ls /etc/shadow");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("cat /proc/cpuinfo 应不被拦截（白名单路径）")
    void shouldAllowSafeProcPaths() {
        String result = service.checkSensitiveFileRead("cat /proc/cpuinfo");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("cat /proc/meminfo 应不被拦截（白名单路径）")
    void shouldAllowProcMeminfo() {
        String result = service.checkSensitiveFileRead("cat /proc/meminfo");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("grep pattern /home/user/file.txt 应不被拦截")
    void shouldAllowNormalGrep() {
        String result = service.checkSensitiveFileRead("grep pattern /home/user/file.txt");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("null 和空命令应不被拦截")
    void shouldAllowNullOrEmpty() {
        assertThat(service.checkSensitiveFileRead(null)).isNull();
        assertThat(service.checkSensitiveFileRead("")).isNull();
        assertThat(service.checkSensitiveFileRead("   ")).isNull();
    }

    @Test
    @DisplayName("带完整路径的读取命令也应被拦截")
    void shouldBlockFullPathCommands() {
        String result = service.checkSensitiveFileRead("/usr/bin/cat /etc/shadow");
        assertThat(result).isNotNull();
        assertThat(result).contains("/etc/shadow");
    }

    @Test
    @DisplayName("分号分隔的多命令中有敏感文件读取应被拦截")
    void shouldBlockSemicolonSeparatedCommands() {
        String result = service.checkSensitiveFileRead("echo test; cat /etc/shadow");
        assertThat(result).isNotNull();
        assertThat(result).contains("/etc/shadow");
    }

    @Test
    @DisplayName("使用 $HOME 变量的敏感路径应被拦截")
    void shouldBlockHomeVarExpansion() {
        String result = service.checkSensitiveFileRead("cat $HOME/.ssh/id_rsa");
        assertThat(result).isNotNull();
        assertThat(result).contains("id_rsa");
    }
}
