package net.swofty.type.generic.resourcepack;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackUrlTest {
    private static final String LOOPBACK = "http://127.0.0.1:7270/abc.zip";

    @Test
    void loopbackUrlUsesTheAddressThePlayerConnectedTo() {
        assertEquals("http://192.168.1.50:7270/abc.zip", PackUrl.forConnection(LOOPBACK, "192.168.1.50").toString());
        assertEquals("http://play.example.com:7270/abc.zip", PackUrl.forConnection(LOOPBACK, "play.example.com").toString());
        assertEquals("http://play.example.com:7270/abc.zip",
                PackUrl.forConnection("http://localhost:7270/abc.zip", "play.example.com").toString());
    }

    @Test
    void configuredPublicUrlIsKept() {
        assertEquals("https://packs.example.com/abc.zip",
                PackUrl.forConnection("https://packs.example.com/abc.zip", "192.168.1.50").toString());
        assertEquals("http://10.0.0.5:7270/abc.zip",
                PackUrl.forConnection("http://10.0.0.5:7270/abc.zip", "192.168.1.50").toString());
    }

    @Test
    void missingConnectionAddressKeepsTheUrl() {
        assertEquals(LOOPBACK, PackUrl.forConnection(LOOPBACK, null).toString());
        assertEquals(LOOPBACK, PackUrl.forConnection(LOOPBACK, "  ").toString());
    }

    @Test
    void connectionAddressIsCleaned() {
        assertEquals("http://play.example.com:7270/abc.zip", PackUrl.forConnection(LOOPBACK, "play.example.com.").toString());
        assertEquals("http://play.example.com:7270/abc.zip", PackUrl.forConnection(LOOPBACK, "play.example.com\0FML3\0").toString());
        assertEquals("http://[2001:db8::1]:7270/abc.zip", PackUrl.forConnection(LOOPBACK, "2001:db8::1").toString());
        assertEquals("http://[2001:db8::1]:7270/abc.zip", PackUrl.forConnection(LOOPBACK, "[2001:db8::1]").toString());
    }
}
